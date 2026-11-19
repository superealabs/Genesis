"""Compare une référence Genesis figée avec le moteur adaptatif et FreeMarker.

JAVA_HOME=/path/to/jdk21 python3 benchmarks/template-engine/prototype/run_adaptive_study.py
La référence est explicite et ne provient jamais d’un moteur déjà modifié.
Les classes, journaux, sorties et résultats bruts restent sous genesis-core/build/.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import statistics
import subprocess

ROOT = Path(__file__).resolve().parents[3]
SOURCE = ROOT / "genesis-core/src/main/java/org/labs/genesis/engine/GenesisTemplateEngine.java"
BUILD = ROOT / "genesis-core/build"
REPORTS = BUILD / "reports/adaptive-study"
DEFAULT_REFERENCE = BUILD / "adaptive-reference/src/org/labs/genesis/engine/GenesisTemplateEngine.java"
REFERENCE_SHA256 = "189b9dc68addc8dde90d88ab74fcadb832c22987ad16520786c2e8246ac5e12c"
REFERENCE_REVISION = "48ce0473fd28f08ed8b3db19e7d8638fe1c62f20"
METRICS = ("meanMs", "medianMs", "allocatedBytesPerOp")


def sha256(value):
    return hashlib.sha256(value).hexdigest()


def is_vue_list(name):
    return name.startswith(("frontend-2-List-Component-", "frontend-2-List-View-"))


def runtime_fingerprint():
    files = sorted(path for path in (ROOT / "genesis-core/src/main").rglob("*")
                   if path.is_file() and not path.name.startswith("."))
    return sha256(json.dumps({str(path.relative_to(ROOT)): sha256(path.read_bytes())
                              for path in files}, sort_keys=True).encode())


def summarize_runs(runs, reference_sha, adaptive_sha, confirmation=False):
    reports = {}
    for variant, entries in runs.items():
        if not entries:
            continue
        data = {"forks": len(entries)}
        for kind in ("corpus", "scaling"):
            if kind not in entries[0]:
                continue
            indexed = [{item["name"]: item for item in entry[kind]["results"]} for entry in entries]
            names = set(indexed[0])
            if any(set(run) != names for run in indexed):
                raise AssertionError(f"Case set changed across {variant} {kind} JVMs")
            data[kind] = {
                name: {
                    metric: statistics.median(run[name][metric] for run in indexed)
                    for metric in METRICS
                }
                for name in sorted(names)
            }
        if "corpus" in data:
            pipeline = [value for name, value in data["corpus"].items() if name.endswith("-pipeline")]
            data["pipelineBasketMs"] = sum(row["meanMs"] for row in pipeline)
            data["pipelineBasketAllocatedBytes"] = sum(row["allocatedBytesPerOp"] for row in pipeline)
        if "profile" in entries[0]:
            data["profile"] = {
                key: statistics.median(entry["profile"][key] for entry in entries)
                for key in (
                    "totalMs", "observedEngineMs", "publishedFileReadMs", "publishedFileWriteMs",
                    "topLevelRenderCalls", "uniqueTopLevelRenderInputs", "largeTopLevelRenderCalls",
                    "uniqueLargeTopLevelRenderInputs", "allRenderCallsIncludingLoopRecursion",
                )
            }
        reports[variant] = data
    baseline = reports["reference"]["corpus"]
    adaptive = reports["adaptive"]["corpus"]
    if set(baseline) != set(adaptive):
        raise AssertionError("Reference and adaptive corpus cases differ")
    rows = []
    for name in sorted(baseline):
        before = baseline[name]["meanMs"]
        after = adaptive[name]["meanMs"]
        delta = after - before
        rows.append({
            "name": name, "referenceMs": before, "adaptiveMs": after,
            "speedup": before / after, "gainFraction": 1 - after / before,
            "deltaMs": delta,
            "referenceAllocatedBytes": baseline[name]["allocatedBytesPerOp"],
            "adaptiveAllocatedBytes": adaptive[name]["allocatedBytesPerOp"],
            "vueList": is_vue_list(name),
            "suspectedRegression": after > before * 1.05 and delta > 0.001,
            "listGainTargetMet": not is_vue_list(name) or after <= before * 0.95,
        })
    before = reports["reference"]["pipelineBasketMs"]
    after = reports["adaptive"]["pipelineBasketMs"]
    return {
        "referenceSourceSha256": reference_sha,
        "adaptiveSourceSha256": adaptive_sha,
        "confirmationOnly": confirmation,
        "exactOutputs": True,
        "caseCount": len(rows),
        "pipelineBasketSpeedup": before / after if after else None,
        "fullBasketTargetMet": None if confirmation else before / after >= 1.5,
        "fiveForkProtocolMet": all(len(runs[variant]) >= 5 for variant in ("reference", "adaptive"))
                               and (confirmation or len(runs["freemarker"]) >= 5),
        "suspectedRegressions": [row["name"] for row in rows if row["suspectedRegression"]],
        "listGainTargetMisses": [row["name"] for row in rows if not row["listGainTargetMet"]],
        "cases": rows,
        "variants": reports,
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--forks", type=int, default=5)
    parser.add_argument("--reference-source", type=Path, default=DEFAULT_REFERENCE)
    parser.add_argument("--reference-sha256", default=REFERENCE_SHA256)
    parser.add_argument("--prepare-only", action="store_true")
    parser.add_argument("--resume", action="store_true", help="Reuse only complete runs with matching source and harness hashes")
    parser.add_argument("--confirm-cases", nargs="+", metavar="CASE", help="Measure only these corpus cases in fresh JVMs")
    args = parser.parse_args()
    if args.forks < 1:
        parser.error("forks must be positive; acceptance requires at least five")
    reference_path = args.reference_source.resolve()
    if reference_path == DEFAULT_REFERENCE and not reference_path.exists():
        # Reconstruire la référence figée après nettoyage, sans dupliquer le moteur dans Git.
        historical = subprocess.check_output([
            "git", "show", REFERENCE_REVISION + ":" + str(SOURCE.relative_to(ROOT)),
        ], cwd=ROOT).decode()
        for token in ("LOOP_START", "LOOP_END"):
            historical = historical.replace(
                f"template.substring(i).startsWith({token})", f"tokenAt(template, {token}, i)")
        if sha256(historical.encode()) != args.reference_sha256:
            raise ValueError("Historical reference reconstruction failed its SHA-256 check")
        reference_path.parent.mkdir(parents=True, exist_ok=True)
        reference_path.write_text(historical)
    reference_source = reference_path.read_bytes()
    adaptive_source = SOURCE.read_bytes()
    reference_sha = sha256(reference_source)
    adaptive_sha = sha256(adaptive_source)
    if reference_sha != args.reference_sha256:
        raise ValueError("Frozen reference SHA-256 does not match; supply an explicit reviewed reference")
    javac = Path(os.environ["JAVA_HOME"]) / "bin/javac"
    runtime_identity = {
        "javaHome": str(javac.parent.parent),
        "javaVersion": subprocess.check_output([str(javac.parent / "java"), "-version"],
                                                stderr=subprocess.STDOUT, text=True).strip(),
        "runtimeInputsSha256": runtime_fingerprint(),
    }
    classes = {}
    for variant, content in (("reference", reference_source), ("adaptive", adaptive_source)):
        directory = BUILD / "adaptive-study" / variant
        java = directory / "src/org/labs/genesis/engine/GenesisTemplateEngine.java"
        java.parent.mkdir(parents=True, exist_ok=True)
        java.write_bytes(content)
        classes[variant] = directory / "classes"
        if classes[variant].exists():
            shutil.rmtree(classes[variant])
        classes[variant].mkdir(exist_ok=True)
        sources = [java]
        if variant == "adaptive":
            # Compiler les types du package dans le même dossier isolé que le moteur.
            for name in ("RenderContext", "LoopInfo", "PlannedNode", "PlannedText",
                         "PlannedBranch", "PlannedIf", "ConditionalPlan"):
                helper = java.with_name(name + ".java")
                shutil.copy2(SOURCE.with_name(name + ".java"), helper)
                sources.append(helper)
        subprocess.run([
            str(javac), "-cp", str(BUILD / "classes/java/main"),
            "-d", str(classes[variant]), *map(str, sources),
        ], check=True, cwd=ROOT)
    REPORTS.mkdir(parents=True, exist_ok=True)
    hashes = {
        path: sha256((ROOT / path).read_bytes())
        for path in (
            "benchmarks/template-engine/benchmark.init.gradle",
            "benchmarks/template-engine/src/engine/GenesisTemplateCorpusTest.java",
            "benchmarks/template-engine/src/engine/GenesisFreeMarkerScalingTest.java",
            "benchmarks/template-engine/src/engine/GenesisGenerationProfileTest.java",
            "genesis-core/src/main/java/org/labs/utils/StringUtils.java",
        )
    }
    metadata = {
        "referenceSourceSha256": reference_sha, "adaptiveSourceSha256": adaptive_sha,
        "harnessSha256": hashes, "javaHome": str(javac.parent.parent),
        "runtimeIdentity": runtime_identity,
        "protocol": {"forks": args.forks, "listWarmupNs": 500_000_000, "listMeasurementNs": 1_000_000_000},
    }
    (REPORTS / "inputs.json").write_text(json.dumps(metadata, indent=2) + "\n")
    if args.prepare_only:
        print(json.dumps({**metadata, "classes": {key: str(value) for key, value in classes.items()}}, indent=2))
        return

    def study(case_names=None, confirmation=False):
        runs = {"reference": [], "adaptive": [], "freemarker": []}
        expected_snapshots = None
        expected_files = None
        directory = REPORTS / "confirmations" if confirmation else REPORTS
        directory.mkdir(parents=True, exist_ok=True)
        forks = 5 if confirmation else args.forks
        for fork in range(1, forks + 1):
            order = ["reference", "adaptive"] if fork % 2 else ["adaptive", "reference"]
            if not confirmation:
                if fork % 2:
                    order.append("freemarker")
                else:
                    order.insert(0, "freemarker")
            for variant in order:
                output = directory / variant / str(fork)
                output.mkdir(parents=True, exist_ok=True)
                engine_variant = "reference" if variant == "freemarker" else variant
                label = "adaptive-" + ("confirm-" if confirmation else "") + variant
                key = {
                    "sourceSha256": reference_sha if engine_variant == "reference" else adaptive_sha,
                    "harnessSha256": hashes, "variant": variant, "caseNames": case_names,
                    "runtimeIdentity": runtime_identity,
                    "confirmation": confirmation,
                }
                complete = output / "completed.json"
                if args.resume and not confirmation and complete.exists() and json.loads(complete.read_text())["inputs"] == key:
                    entry = json.loads(complete.read_text())["results"]
                    print(f"REUSE variant={variant} fork={fork} confirmation={confirmation}", flush=True)
                else:
                    command = [str(ROOT / "gradlew"), "-I", "benchmarks/template-engine/benchmark.init.gradle"]
                    if variant == "freemarker":
                        command += [":genesis-core:freeMarkerScaling", "-PscalingEngine=freemarker"]
                    else:
                        command.append(":genesis-core:templateCorpus")
                        if not confirmation:
                            command += [":genesis-core:freeMarkerScaling", ":genesis-core:generationProfile"]
                    command += [
                        "--offline", "--console=plain", f"-PcorpusVariant={label}", f"-PcorpusFork={fork}",
                        f"-PcorpusEngineClasses={classes[engine_variant]}",
                    ]
                    if case_names:
                        command.append("-PcorpusCases=" + ",".join(case_names))
                    if confirmation:
                        command.append("-PcorpusConfirmation=true")
                    print(f"RUN variant={variant} fork={fork}/{forks} confirmation={confirmation}", flush=True)
                    with (output / "run.log").open("w") as log:
                        subprocess.run(command, cwd=ROOT, stdout=log, stderr=subprocess.STDOUT,
                                       check=True, timeout=900)
                    entry = {}
                    if variant != "freemarker":
                        corpus = BUILD / "reports/template-corpus" / label / str(fork)
                        entry["corpus"] = json.loads((corpus / "results.json").read_text())
                        assert str(classes[engine_variant]) in entry["corpus"]["engineLocation"]
                        if not confirmation:
                            assert len(entry["corpus"]["results"]) == 198, "The full adaptive corpus must contain 198 workloads"
                        for result in entry["corpus"]["results"]:
                            if result["vueList"] or confirmation:
                                assert result["warmupNs"] >= 500_000_000
                                assert result["measuredNs"] >= 1_000_000_000
                        entry["snapshots"] = {
                            path.name: sha256(path.read_bytes())
                            for path in (corpus / "snapshots").glob("*.txt")
                        }
                        expected = {result["name"] + ".txt": result["outputSha256"]
                                    for result in entry["corpus"]["results"]}
                        assert len(expected) == len(entry["corpus"]["results"]), "Duplicate corpus case names"
                        assert entry["snapshots"] == expected, "Corpus snapshots do not match their reported hashes"
                        shutil.copy2(corpus / "results.json", output / "corpus.json")
                    if not confirmation:
                        scaling = BUILD / "reports/freemarker-scaling" / label / str(fork) / "results.json"
                        entry["scaling"] = json.loads(scaling.read_text())
                        assert str(classes[engine_variant]) in entry["scaling"]["engineLocation"]
                        shutil.copy2(scaling, output / "scaling.json")
                        if variant != "freemarker":
                            profile = BUILD / "reports/generation-profile" / label / str(fork) / "profile.json"
                            entry["profile"] = json.loads(profile.read_text())
                            assert str(classes[engine_variant]) in entry["profile"]["engineLocation"]
                            assert len(entry["profile"]["files"]) == 250
                            shutil.copy2(profile, output / "profile.json")
                    complete.write_text(json.dumps({"inputs": key, "results": entry}) + "\n")
                if variant != "freemarker":
                    if expected_snapshots is None:
                        expected_snapshots = entry["snapshots"]
                    assert entry["snapshots"] == expected_snapshots, f"Corpus changed: {variant}/{fork}"
                    if not confirmation:
                        if expected_files is None:
                            expected_files = entry["profile"]["files"]
                        assert entry["profile"]["files"] == expected_files, f"Generated files changed: {variant}/{fork}"
                runs[variant].append(entry)
                print(f"PASS variant={variant} fork={fork} exact_outputs=true", flush=True)
        summary = summarize_runs(runs, reference_sha, adaptive_sha, confirmation)
        summary["publishedFiles"] = 0 if confirmation else len(expected_files)
        (directory / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
        return summary

    if args.confirm_cases:
        summary = study(args.confirm_cases, confirmation=True)
        summary["confirmedRegressions"] = summary["suspectedRegressions"]
    else:
        summary = study()
        suspected = summary["suspectedRegressions"]
        summary["confirmedRegressions"] = []
        if suspected:
            confirmation = study(suspected, confirmation=True)
            summary["confirmedRegressions"] = confirmation["suspectedRegressions"]
            summary["confirmationCaseCount"] = len(suspected)
        summary["acceptancePassed"] = (
            summary["fiveForkProtocolMet"] and summary["fullBasketTargetMet"]
            and not summary["confirmedRegressions"] and not summary["listGainTargetMisses"]
        )
    assert SOURCE.read_bytes() == adaptive_source, "Production engine changed during the study"
    assert runtime_fingerprint() == runtime_identity["runtimeInputsSha256"], "Runtime sources or resources changed during the study"
    assert all(sha256((ROOT / path).read_bytes()) == expected for path, expected in hashes.items()), \
        "Benchmark or helper source changed during the study"
    destination = REPORTS / ("confirmation-summary.json" if args.confirm_cases else "summary.json")
    destination.write_text(json.dumps(summary, indent=2) + "\n")
    print(json.dumps({key: value for key, value in summary.items() if key not in ("cases", "variants")}, indent=2), flush=True)
    if summary.get("confirmedRegressions") or summary.get("acceptancePassed") is False:
        raise SystemExit(2)


if __name__ == "__main__":
    main()

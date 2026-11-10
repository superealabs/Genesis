package org.labs.genesis.config.langage.generator.ruleToCode;

import org.jetbrains.annotations.NotNull;
import org.labs.utils.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeInjector {

    public CodeInjector() {}

    public List<CodeBlock> splitCode(String bigString, int idFramework) {
        List<CodeBlock> blocks = new ArrayList<>();
        String pattern = "";
        String nameImport = "";
        if (idFramework == 1) {
            pattern = "@(Service|Repository):\\s*(\\w+)\\s*((?:import\\s+[\\w\\.\\*]+;\\s*)*)(.*?)(?=@Service|@Repository|$)";
            nameImport = "import";
        }
        if (idFramework == 2) {
            pattern = "@(Service|Repository):\\s*(\\w+)\\s*((?:using\\s+[\\w\\.\\*]+;\\s*)*)(.*?)(?=@Service|@Repository|$)";
            nameImport = "using";
        }
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher m = p.matcher(bigString);

        while (m.find()) {
            String layer = m.group(1).trim(); // Service or Repository
            String className = m.group(2).trim(); // Name of class
            String importsBlock = m.group(3).trim(); // All imports
            String code = m.group(4).trim(); // Rule code

            if (!importsBlock.isEmpty()) {
                String[] imports = importsBlock.split("\\r?\\n");
                for (String imp : imports) {
                    blocks.add(new CodeBlock(nameImport, layer, className, imp.trim()));
                }
            }
            if (!code.isEmpty()) {
                code = code.replaceAll("^```[a-zA-Z0-9_-]*\\s*", "");
                code = code.replaceAll("\\s*```$", "");
                blocks.add(new CodeBlock("none", layer, className, code.trim()));
            }
        }
        return blocks;
    }

    // INJECT CODE IN CLASS
    public Path injectBlockCondition(int idFramework, String projectPath, String projectName, Path filePath,
            CodeBlock block) throws Exception {
        String layer = block.layer.trim();
        layer = layer.replaceAll("\\s", "");

        String targetDir = "";
        String fileType = "";

        if (idFramework == 1) { // Spring Boot
            Path base = Paths.get(projectPath);
            if (projectName != null && !projectName.isEmpty()
                    && !Files.exists(base.resolve("pom.xml")) && !Files.exists(base.resolve("build.gradle"))
                    && (Files.exists(base.resolve(projectName).resolve("pom.xml"))
                            || Files.exists(base.resolve(projectName).resolve("build.gradle")))) {
                base = base.resolve(projectName);
            }
            YamlData yamlData = new YamlData();
            String[] meta = yamlData.extractGroupAndArtifact(base, idFramework);
            String groupIdPath = meta[0].replace(".", "/");
            String artifactId = meta[1].toLowerCase();

            String subDir = "services";
            if (layer.equalsIgnoreCase("Repository")
                    || (layer.equalsIgnoreCase(block.nameImport) && block.className.endsWith("Repository"))) {
                subDir = "repositories";
            }
            targetDir = base.toString() + "/src/main/java/" + groupIdPath + "/" + artifactId + "/" + subDir;
            fileType = ".java";
        }
        if (idFramework == 2) { // .NET
            String subDirName = layer.equalsIgnoreCase("Repository") ? "Repositories" : "Services";
            fileType = ".cs";

            Path base = Paths.get(projectPath);
            String majProject = projectName != null ? StringUtils.majStart(projectName) : "";

            List<Path> candidateDirs = new ArrayList<>();
            if (projectName != null && !projectName.isEmpty()) {
                candidateDirs.add(base.resolve(projectName).resolve(projectName).resolve(subDirName).resolve("Implementation"));
                candidateDirs.add(base.resolve(majProject).resolve(majProject).resolve(subDirName).resolve("Implementation"));
                candidateDirs.add(base.resolve(projectName).resolve(projectName).resolve(subDirName.toLowerCase()).resolve("implementation"));

                candidateDirs.add(base.resolve(projectName).resolve(subDirName).resolve("Implementation"));
                candidateDirs.add(base.resolve(majProject).resolve(subDirName).resolve("Implementation"));
                candidateDirs.add(base.resolve(projectName).resolve(subDirName.toLowerCase()).resolve("implementation"));
            }
            candidateDirs.add(base.resolve(subDirName).resolve("Implementation"));
            candidateDirs.add(base.resolve(subDirName.toLowerCase()).resolve("implementation"));

            Path foundDir = null;
            for (Path candidate : candidateDirs) {
                if (Files.exists(candidate.resolve(block.className + fileType))) {
                    foundDir = candidate;
                    break;
                }
            }
            if (foundDir == null) {
                for (Path candidate : candidateDirs) {
                    if (Files.isDirectory(candidate)) {
                        foundDir = candidate;
                        break;
                    }
                }
            }
            if (foundDir != null) {
                targetDir = foundDir.toString();
            } else {
                targetDir = base.resolve(majProject).resolve(majProject).resolve(subDirName).resolve("Implementation").toString();
            }
        }

        filePath = Paths.get(targetDir, block.className + fileType);
        return filePath;
    }

    public void injectBlocks(@NotNull List<CodeBlock> blocks, String projectPath, int idFramework, String projectName)
            throws Exception {
        Path filePath = null;
        for (CodeBlock block : blocks) {

            filePath = injectBlockCondition(idFramework, projectPath, projectName, filePath, block);
            if (block.nameImport.equals("import") || block.nameImport.equals("using")) {
                injectImportInClass(filePath, block.code);
            }
            if (block.nameImport.equals("none")) {
                injectCodeInClass(filePath, block.code);
            }
        }
    }

    public void injectCodeInClass(Path filePath, String codeToInject) throws Exception {
        if (!Files.exists(filePath)) {
            throw new Exception("File not found for code injection: " + filePath);
        }
        String content = Files.readString(filePath);
        int insertPos = content.lastIndexOf("}");
        if (insertPos == -1) {
            throw new Exception("Malformed class, closing brace not found");
        }

        // Si le fichier contient un namespace avec bloc (ex: namespace Foo { ... }),
        // le dernier '}' ferme le namespace, tandis que l'avant-dernier '}' ferme la classe.
        if (content.matches("(?s).*namespace\\s+[^{\\r\\n]+\\{.*")) {
            int secondLast = content.lastIndexOf("}", insertPos - 1);
            if (secondLast != -1) {
                insertPos = secondLast;
            }
        }

        String before = content.substring(0, insertPos).trim();
        String after = content.substring(insertPos);

        String codeIndented = indentCode(codeToInject);
        String newContent = before + "\n\n" + codeIndented + "\n" + after;
        Files.writeString(filePath, newContent);
    }

    public void injectImportInClass(Path filePath, String importToInject) throws Exception {
        if (!Files.exists(filePath)) {
            throw new Exception("File not found for import injection: " + filePath);
        }
        String content = Files.readString(filePath);

        if (!importToInject.trim().endsWith(";")) {
            importToInject = importToInject.trim() + ";";
        }
        if (content.contains(importToInject.trim())) {
            return;
        }
        int packageIndex = content.indexOf("package ");
        StringBuilder newContent = new StringBuilder();

        if (packageIndex != -1) {
            int endOfPackageLine = content.indexOf(";", packageIndex) + 1;

            newContent.append(content, 0, endOfPackageLine);
            newContent.append("\n\n");
            newContent.append(importToInject.trim()).append("\n");
            newContent.append(content.substring(endOfPackageLine).trim()).append("\n");
        } else {
            newContent.append(importToInject.trim()).append("\n\n").append(content);
        }
        Files.writeString(filePath, newContent.toString());
    }

    private String indentCode(String code) {
        StringBuilder sb = new StringBuilder();
        for (String line : code.split("\n")) {
            sb.append("    ").append(line).append("\n");
        }
        return sb.toString();
    }

    // DELETE CODE IN CLASS
    public void deleteBlocks(@NotNull List<CodeBlock> blocks, String projectPath, int idFramework, String projectName)
            throws Exception {
        Path filePath = null;
        for (CodeBlock block : blocks) {

            filePath = injectBlockCondition(idFramework, projectPath, projectName, filePath, block);

            if ("import".equalsIgnoreCase(block.nameImport) || "using".equalsIgnoreCase(block.nameImport)) {
                deleteImportInClass(filePath, block.code);
            } else if ("none".equalsIgnoreCase(block.nameImport)) {
                deleteCodeInClass(filePath, block.code);
            }
        }
    }

    public void deleteCodeInClass(Path filePath, String codeToDelete) throws Exception {
        if (!Files.exists(filePath)) return;
        String content = Files.readString(filePath);

        String codeIndented = indentCode(codeToDelete).trim();

        if (content.contains(codeIndented)) {
            content = content.replace(codeIndented, "").trim();
            Files.writeString(filePath, content);
        } else if (content.contains(codeToDelete.trim())) {
            content = content.replace(codeToDelete.trim(), "").trim();
            Files.writeString(filePath, content);
        }
    }

    public void deleteImportInClass(Path filePath, String importToDelete) throws Exception {
        if (!Files.exists(filePath)) return;
        String content = Files.readString(filePath);

        if (!importToDelete.trim().endsWith(";")) {
            importToDelete = importToDelete.trim() + ";";
        }

        if (content.contains(importToDelete.trim())) {
            content = content.replace(importToDelete.trim(), "").trim();
            Files.writeString(filePath, content);
        }
    }

}

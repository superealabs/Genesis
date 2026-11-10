package org.labs.genesis.config.langage.generator.ruleToCode.DataExtractor;

import org.labs.genesis.config.langage.generator.ruleToCode.YamlData;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DotNetMetaDataExtractor implements IMetaDataExtractor {

    private final int idFramework;

    public DotNetMetaDataExtractor(int idFramework) {
        this.idFramework = idFramework;
    }

    @Override
    public String extractMetaData(Path projectBasePath, String groupId, String projectName) throws Exception {
        YamlData yamlData = new YamlData();
        File srcDir = yamlData.pathModel(projectBasePath.toString(), groupId, projectName, this.idFramework);
        String typeClass = ".cs";
        List<Map<String, Object>> entities = new ArrayList<>();

        if (srcDir == null || !srcDir.exists() || !srcDir.isDirectory()) {
            return yamlData.convertToYaml(entities);
        }

        File[] files = srcDir.listFiles((dir, name) -> name.endsWith(typeClass));
        if (files == null) {
            return yamlData.convertToYaml(entities);
        }

        // Sort files for deterministic output across environments
        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            List<String> lines = Files.readAllLines(file.toPath());

            String className = lines.stream()
                    .filter(l -> l.trim().startsWith("public class"))
                    .map(l -> l.replace("public class", "").trim().split("[\\s:]")[0].trim())
                    .findFirst()
                    .orElse(null);

            if (className == null || className.isEmpty()) {
                continue;
            }

            Map<String, Object> entity = new LinkedHashMap<>();
            entity.put("name", className);

            List<Map<String, String>> fields = new ArrayList<>();
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("public") && trimmed.contains("{ get; set; }")) {
                    String[] parts = trimmed.split("\\s+");
                    int typeIdx = 1;
                    if (typeIdx < parts.length && ("virtual".equals(parts[typeIdx]) || "override".equals(parts[typeIdx]))) {
                        typeIdx++;
                    }
                    if (typeIdx + 1 < parts.length) {
                        Map<String, String> f = new LinkedHashMap<>();
                        f.put("type", parts[typeIdx].trim());
                        f.put("name", parts[typeIdx + 1].replace(";", "").trim());
                        fields.add(f);
                    }
                }
            }

            entity.put("fields", fields);
            entities.add(entity);
        }
        return yamlData.convertToYaml(entities);
    }
}

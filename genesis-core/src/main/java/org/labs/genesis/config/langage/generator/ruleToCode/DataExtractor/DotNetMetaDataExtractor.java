package org.labs.genesis.config.langage.generator.ruleToCode.DataExtractor;

import org.labs.genesis.config.langage.generator.ruleToCode.YamlData;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DotNetMetaDataExtractor implements IMetaDataExtractor {

    int idFramework ;
    public DotNetMetaDataExtractor( int idFramework ) {
        this.idFramework = idFramework;
    }

    @Override
    public String extractMetaData(Path projectBasePath, String groupId, String projectName) throws Exception {
        YamlData yamlData = new YamlData();
        File srcDir = yamlData.pathModel(projectBasePath.toString(), groupId, projectName, this.idFramework);
        String typeClass = ".cs";
        List<Map<String, Object>> entities = new ArrayList<>();

        for (File file : Objects.requireNonNull(srcDir.listFiles((dir, name) -> name.endsWith(typeClass)))) {
            List<String> lines = Files.readAllLines(file.toPath());

            String className = lines.stream()
                    .filter(l -> l.trim().startsWith("public class"))
                    .map(l -> l.replace("public class", "").trim().split(" ")[0])
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Class not found in " + file.getName()));

            Map<String, Object> entity = new LinkedHashMap<>();
            entity.put("name", className);

            List<Map<String, String>> fields = new ArrayList<>();
            lines.stream()
                    .filter(l -> l.trim().startsWith("public") && l.contains("{ get; set; }"))
                    .forEach(l -> {
                        String[] parts = l.trim().split(" ");
                        if (parts.length >= 3) {
                            Map<String, String> f = new LinkedHashMap<>();
                            f.put("type", parts[1].trim());
                            f.put("name", parts[2].replace(";", "").trim());
                            fields.add(f);
                        }
                    });

            entity.put("fields", fields);
            entities.add(entity);
        }
        return yamlData.convertToYaml(entities);
    }
}

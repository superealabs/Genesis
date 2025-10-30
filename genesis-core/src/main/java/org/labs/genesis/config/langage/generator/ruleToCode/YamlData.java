package org.labs.genesis.config.langage.generator.ruleToCode;


import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

import org.labs.genesis.config.langage.generator.ruleToCode.DataExtractor.DotNetMetaDataExtractor;
import org.labs.genesis.config.langage.generator.ruleToCode.DataExtractor.IMetaDataExtractor;
import org.labs.genesis.config.langage.generator.ruleToCode.DataExtractor.SpringMetaDataExtractor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class YamlData {

    //Method extract group id and project name in the framework
    public String[] extractGroupAndArtifact(Path projectDir , int frameworkId) throws Exception {
        //Spring boot option
        if ( frameworkId == 1 ) {
            Path pom = projectDir.resolve("pom.xml");
            Path gradle = projectDir.resolve("build.gradle");
            Path gradleKts = projectDir.resolve("build.gradle.kts");

            if (Files.exists(pom)) {
                // ----- Maven -----
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(pom.toFile());
                doc.getDocumentElement().normalize();

                NodeList children = doc.getDocumentElement().getChildNodes();
                String groupId = null;
                String artifactId = null;

                for (int i = 0; i < children.getLength(); i++) {
                    Node node = children.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        String name = node.getLocalName();
                        if ("groupId".equals(name) && groupId == null) {
                            groupId = node.getTextContent();
                        } else if ("artifactId".equals(name) && artifactId == null) {
                            artifactId = node.getTextContent();
                        }
                    }
                }
                return new String[]{groupId, artifactId};
            } else if (Files.exists(gradle) || Files.exists(gradleKts)) {
                // ----- Gradle -----
                Path gradleFile = Files.exists(gradle) ? gradle : gradleKts;
                List<String> lines = Files.readAllLines(gradleFile);
                String groupId = lines.stream()
                        .filter(line -> line.trim().startsWith("group"))
                        .map(line -> line.split("=")[1].trim().replace("\"", "").replace("'", ""))
                        .findFirst()
                        .orElse("unknown");

                String artifactId = projectDir.getFileName().toString();
                return new String[]{groupId, artifactId};
            } else {
                throw new Exception("Error no pom.xml and build.gradle in the project" + projectDir);
            }
        }
        return null ;
    }

    public String getProjectName(Path projectDir) {
        if (projectDir == null) {
            throw new IllegalArgumentException("Project directory cannot be null");
        }
        Path fileName = projectDir.getFileName();
        if (fileName == null) {
            throw new IllegalArgumentException("Invalid project path: " + projectDir);
        }
        return fileName.toString();
    }

    public String convertToYaml(List<Map<String, Object>> entities) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("entities", entities);

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        StringWriter stringWriter = new StringWriter();
        yaml.dump(root, stringWriter);

        return stringWriter.toString();
    }

    public File pathModel (String projectBasePath , String groupId, String projectName , int idFramework ) throws Exception {
        if ( idFramework == 1 ) { // Spring boot
            File modelsDir = new File(projectBasePath.toString(), "src/main/java/" + groupId.replace('.', '/') + "/" + projectName + "/models");
            if (!modelsDir.exists() || !modelsDir.isDirectory()) {
                throw new RuntimeException("Path not foud projet Spring boot : " + modelsDir.getAbsolutePath());
            }
            return modelsDir;
        }
        if (idFramework == 2) { // .NET
            File modelsDir = new File(projectBasePath.toString(), projectName + "/Models");
            if (!modelsDir.exists() || !modelsDir.isDirectory()) {
                throw new RuntimeException("Path not foud projet .NET : " + modelsDir.getAbsolutePath());
            }
            return modelsDir;
        }
        return null ;
    }

    public String extractMetaData(Path projectBasePath, String groupId, String projectName, int idFramework) throws Exception {
        IMetaDataExtractor extractor = switch (idFramework) {
            case 1 -> new SpringMetaDataExtractor(idFramework);
            case 2 -> new DotNetMetaDataExtractor(idFramework);
            default -> throw new IllegalArgumentException("Unsupported framework id: " + idFramework);
        };
        return extractor.extractMetaData(projectBasePath, groupId, projectName);
    }

}

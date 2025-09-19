package org.labs.genesis.config.langage.generator.ruleToCode;


import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class YamlData {

    //Method extract group id and project name in the framework
    public String[] extractGroupAndArtifact(Path projectDir) throws Exception {
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
        }
        else if (Files.exists(gradle) || Files.exists(gradleKts)) {
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
        }
        else {
            throw new Exception("Error no pom.xml and build.gradle in the project" + projectDir);
        }
    }

    public File pathModel (String projectBasePath , String groupId, String projectName , int idFramework ) throws Exception {
        if ( idFramework == 1 ) {
            return new File(projectBasePath.toString(), "src/main/java/" + groupId.replace('.', '/') + "/" + projectName + "/models");
        }
        return null ;
    }

    public String typeClass ( int idFramework ){
        String typeClass = "" ;
        if ( idFramework == 1 ) {
            typeClass = ".java" ;
        }
        return typeClass;
    }

    public String extractMetaData(Path projectBasePath , String groupId, String projectName, int idFramework ) throws Exception {

        File srcDir = pathModel( projectBasePath.toString() , groupId , projectName , idFramework ) ;
        String typeClass = typeClass(idFramework);

        List<Map<String, Object>> entities = new ArrayList<>();

        for (File file : Objects.requireNonNull(srcDir.listFiles((dir, name) -> name.endsWith(typeClass)))) {
            CompilationUnit cu = StaticJavaParser.parse(file);

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
                Map<String, Object> entity = new LinkedHashMap<>();
                entity.put("name", clazz.getNameAsString());
                List<Map<String, String>> fields = new ArrayList<>();
                for (FieldDeclaration field : clazz.getFields()) {
                    String fieldName = field.getVariable(0).getNameAsString();
                    String fieldType = field.getElementType().asString();

                    Map<String, String> f = new LinkedHashMap<>();
                    f.put("name", fieldName);
                    f.put("type", fieldType);
                    fields.add(f);
                }
                entity.put("fields", fields);
                entities.add(entity);
            });
        }

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
}

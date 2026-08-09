package org.labs.genesis.config.langage.generator.ruleToCode.DataExtractor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.labs.genesis.config.langage.generator.ruleToCode.YamlData;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class SpringMetaDataExtractor implements IMetaDataExtractor {

    int idFramework ;
    public SpringMetaDataExtractor(int idFramework ) {
        this.idFramework = idFramework;
    }
    @Override
    public String extractMetaData(Path projectBasePath, String groupId, String projectName) throws Exception {
        YamlData yamlData = new YamlData();
        File srcDir = yamlData.pathModel( projectBasePath.toString() , groupId , projectName , this.idFramework ) ;
        String typeClass = ".java";
        List<Map<String, Object>> entities = new ArrayList<>();

        for (File file : Objects.requireNonNull(srcDir.listFiles((dir, name) -> name.endsWith(typeClass)))) {
            CompilationUnit cu = StaticJavaParser.parse(file);
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
                Map<String, Object> entity = new LinkedHashMap<>();
                entity.put("name", clazz.getNameAsString());

                List<Map<String, String>> fields = new ArrayList<>();
                for (FieldDeclaration field : clazz.getFields()) {
                    Map<String, String> f = new LinkedHashMap<>();
                    f.put("name", field.getVariable(0).getNameAsString());
                    f.put("type", field.getElementType().asString());
                    fields.add(f);
                }

                entity.put("fields", fields);
                entities.add(entity);
            });
        }

        return yamlData.convertToYaml(entities);
    }
}

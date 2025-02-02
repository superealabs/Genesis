package org.labs.genesis.config.langage.generator.project;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.engine.GenesisTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectMetadataProvider {
    private static final GenesisTemplateEngine engine = new GenesisTemplateEngine();

    static HashMap<String, Object> getInitialHashMap(String destinationFolder, String projectName, String groupLink) {
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("destinationFolder", destinationFolder);
        metadata.put("projectName", projectName);
        metadata.put("groupLink", groupLink);
        metadata.put("groupLinkPath", groupLink.replace(".", "/"));
        return metadata;
    }

    static HashMap<String, Object> getConfigFileHashMap(String projectPort, Database database, Credentials credentials, Language language, Framework framework, Map<String, Object> frameworkOptions) throws Exception {
        HashMap<String, Object> configFile = new HashMap<>();
        configFile.put("projectPort", projectPort);

        if (framework.getUseDB()) {
            String databaseUrl = database.getConnectionString().get(language.getId());

            Map<String, Object> databaseMetadata = database.getDatabaseMetadataHashMap(credentials);
            databaseUrl = engine.render(databaseUrl, databaseMetadata);

            configFile.put("databaseUrl", databaseUrl);
            configFile.put("databaseUsername", database.getCredentials().getUser());
            configFile.put("databasePassword", database.getCredentials().getPwd());
            configFile.put("databaseType", database.getName());
        }
        configFile.putAll(frameworkOptions);

        return configFile;
    }

    static HashMap<String, Object> getDependencyFileHashMap(String projectDescription, Database database, Language language, Framework framework, Map<String, Object> langageConfiguration, Map<String, Object> frameworkConfiguration) {
        HashMap<String, Object> dependencyFileMap = new HashMap<>();
        dependencyFileMap.putAll(langageConfiguration);
        dependencyFileMap.putAll(frameworkConfiguration);
        dependencyFileMap.put("projectDescription", projectDescription);
        dependencyFileMap.put("useCloud", framework.getUseCloud());
        dependencyFileMap.put("useEurekaServer", framework.getUseEurekaServer());

        List<HashMap<String, String>> dependencies = getDependenciesHashMaps(framework);
        dependencyFileMap.put("dependencies", dependencies);

        if (database != null && framework.getUseDB()) {
            dependencyFileMap.put("useDB", true);

            Framework.Dependency databaseDependency = database.getDependencies().get(String.valueOf(language.getId()));

            dependencyFileMap.put("DBgroupId", databaseDependency.getGroupId());
            dependencyFileMap.put("DBartifactId", databaseDependency.getArtifactId());
            dependencyFileMap.put("DBversion", databaseDependency.getVersion());

        } else {
            dependencyFileMap.put("useDB", false);
            dependencyFileMap.put("DBgroupId", "{{removeLine}}");
            dependencyFileMap.put("DBartifactId", "{{removeLine}}");
            dependencyFileMap.put("DBversion", "{{removeLine}}");
        }

        return dependencyFileMap;
    }

    private static @NotNull List<HashMap<String, String>> getDependenciesHashMaps(Framework framework) {
        List<HashMap<String, String>> dependencies = new ArrayList<>();
        List<Framework.Dependency> dependenciesList = framework.getDependencies();

        for (Framework.Dependency dependency : dependenciesList) {
            HashMap<String, String> dependencyMap = new HashMap<>();
            dependencyMap.put("groupId", dependency.getGroupId());
            dependencyMap.put("artifactId", dependency.getArtifactId());
            dependencyMap.put("version", dependency.getVersion());
            dependencyMap.put("scope", dependency.getScope());
            dependencies.add(dependencyMap);
        }

        return dependencies;
    }

    static HashMap<String, Object> getProjectFilesEditsHashMap(String destinationFolder, String projectName, String groupLink, String projectPort, Database database, Credentials credentials, @NotNull Language language, String projectDescription, Map<String, Object> langageConfiguration, Framework framework, Map<String, Object> frameworkOptions) throws Exception {
        HashMap<String, Object> combinedMap = new HashMap<>();

        combinedMap.putAll(getConfigFileHashMap(projectPort, database, credentials, language, framework, frameworkOptions));
        combinedMap.putAll(getDependencyFileHashMap(projectDescription, database, language, framework, langageConfiguration, frameworkOptions));
        combinedMap.putAll(getInitialHashMap(destinationFolder, projectName, groupLink));

        return combinedMap;
    }
}

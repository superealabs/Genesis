package org.labs.genesis.config.langage.generator.project;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.StringUtils;

import java.util.*;

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
            configFile.put("databaseVersion", database.getDriverVersion());
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
        List<HashMap<String, String>> additionalSecurityDependencies = getFrameworkSecurityDependenciesHashMaps(framework, frameworkConfiguration);
        List<HashMap<String, String>> allDependencies = new ArrayList<>();
        allDependencies.addAll(dependencies);
        allDependencies.addAll(additionalSecurityDependencies);
        dependencyFileMap.put("dependencies", allDependencies);

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

        if (framework instanceof FrameworkMVC) {
            dependencyFileMap.put("isMvcProject", true);
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

    private static List<HashMap<String, String>> getFrameworkSecurityDependenciesHashMaps(Framework framework, Map<String, Object> frameworkConfiguration) {
        List<HashMap<String, String>> dependencies = new ArrayList<>();
        String securityType = (String) frameworkConfiguration.get("securityType");
        Optional<FrameworkSecurity> selectedSecurityOption = framework.getSelectedSecurityByName(securityType);

        selectedSecurityOption.ifPresent(security -> {
            try {
                List<Framework.Dependency> dependenciesList = security.getAdditionalDependencies();

                for (Framework.Dependency dependency : dependenciesList) {
                    HashMap<String, String> dependencyMap = new HashMap<>();
                    dependencyMap.put("groupId", dependency.getGroupId());
                    dependencyMap.put("artifactId", dependency.getArtifactId());
                    dependencyMap.put("version", dependency.getVersion());
                    dependencyMap.put("scope", dependency.getScope());
                    dependencies.add(dependencyMap);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return dependencies;
    }

    private static HashMap<String, Object> getFrameworkSecurityTrueBooleansHashMap(Framework framework, Map<String, Object> frameworkConfiguration) {
        HashMap<String, Object> frameworkSecurityBooleanMetadata = new HashMap<>();
        String securityType = (String) frameworkConfiguration.get("securityType");
        Optional<FrameworkSecurity> selectedSecurityOption = framework.getSelectedSecurityByName(securityType);
        selectedSecurityOption.ifPresent(security -> {
            for(String key : security.getMetadataBooleanTrueKeys()){
                frameworkSecurityBooleanMetadata.put(key, true);
            }
        });
        return frameworkSecurityBooleanMetadata;
    }

    static HashMap<String, Object> getProjectFilesEditsHashMap(String destinationFolder, String projectName, String groupLink, String projectPort, Database database, Credentials credentials, @NotNull Language language, String projectDescription, Map<String, Object> langageConfiguration, Framework framework, Map<String, Object> frameworkOptions) throws Exception {
        HashMap<String, Object> combinedMap = new HashMap<>();

        combinedMap.putAll(getConfigFileHashMap(projectPort, database, credentials, language, framework, frameworkOptions));
        combinedMap.putAll(getDependencyFileHashMap(projectDescription, database, language, framework, langageConfiguration, frameworkOptions));
        combinedMap.putAll(getInitialHashMap(destinationFolder, projectName, groupLink));
        combinedMap.putAll(getFrameworkSecurityTrueBooleansHashMap(framework,frameworkOptions));

        return combinedMap;
    }

}

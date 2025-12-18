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
            System.out.println(" id language = "+language.getId()+ " database "+database.getName());
            String databaseUrl = database.getConnectionString().get(language.getId());
            System.out.println("Database URL: " + databaseUrl);
            Map<String, Object> databaseMetadata = database.getDatabaseMetadataHashMap(credentials);
            if(databaseMetadata.get("database").equals(""))
            {
                databaseMetadata.put("database",credentials.getSID().toLowerCase());
            }
            databaseUrl = engine.render(databaseUrl, databaseMetadata);

            configFile.put("databaseUrl", databaseUrl);
            configFile.put("databaseUsername", database.getCredentials().getUser());
            configFile.put("databasePassword", database.getCredentials().getPwd());
            configFile.put("databaseType", database.getName());
            configFile.put("databaseVersion", database.getDriverVersion());

            // Django-specific database configuration
            configFile.put("databaseEngine", getDjangoDatabaseEngine(database.getName()));
            configFile.put("databaseName", database.getCredentials().getDatabaseName());
            // configFile.put("databaseUser", database.getCredentials().getUser());
            configFile.put("databaseHost", database.getCredentials().getHost());
            configFile.put("databasePort", database.getCredentials().getPort());
        }
        configFile.putAll(frameworkOptions);

        return configFile;
    }
    private static String getDjangoDatabaseEngine(String databaseType) {
        return switch (databaseType) {
            case "MySQL" -> "django.db.backends.mysql";
            case "PostgreSQL" -> "django.db.backends.postgresql";
            case "SQL Server" -> "django.db.backends.sqlite3"; // Fallback to SQLite for SQL Server
            case "Oracle" -> "django.db.backends.sqlite3"; // Fallback to SQLite for Oracle
            default -> "django.db.backends.sqlite3";
        };
    }

    static HashMap<String, Object> getDependencyFileHashMap(String projectDescription, Database database, Language language, Framework framework, Map<String, Object> langageConfiguration, Map<String, Object> frameworkConfiguration) {
        HashMap<String, Object> dependencyFileMap = new HashMap<>();
        dependencyFileMap.putAll(langageConfiguration);
        dependencyFileMap.putAll(frameworkConfiguration);
        dependencyFileMap.put("projectDescription", projectDescription);
        dependencyFileMap.put("useCloud", framework.getUseCloud());
        dependencyFileMap.put("useEurekaServer", framework.getUseEurekaServer());
        dependencyFileMap.put("useGatewaySecurity", framework.getUseGatewaySecurity());

        List<HashMap<String, String>> dependencies = getDependenciesHashMaps(framework);
        List<HashMap<String, String>> additionalSecurityDependencies = getFrameworkSecurityDependenciesHashMaps(framework, frameworkConfiguration);
        List<HashMap<String, String>> allDependencies = new ArrayList<>();
        allDependencies.addAll(dependencies);
        allDependencies.addAll(additionalSecurityDependencies);
        List<HashMap<String, String>> additionalCacheProviderDependencies = getFrameworkCachingDependenciesHashMaps(framework, frameworkConfiguration);
        allDependencies.addAll(additionalCacheProviderDependencies);
        // Filter out Django from dependencies to avoid duplication in requirements.txt (Django is already added explicitly)
        if (framework.getCoreFramework() != null && framework.getCoreFramework().equalsIgnoreCase("Django")) {
            allDependencies.removeIf(dep -> "Django".equals(dep.get("artifactId")));
        }
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
    private static List<HashMap<String, String>> getFrameworkCachingDependenciesHashMaps(Framework framework, Map<String, Object> frameworkConfiguration) {
        List<HashMap<String, String>> dependencies = new ArrayList<>();
        String cacheProvider = (String) frameworkConfiguration.get("cacheProvider");
        Optional<FrameworkCaching> selectedCacheProviderOption = framework.getSelectedCacheProviderByName(cacheProvider);

        selectedCacheProviderOption.ifPresent(frameworkCaching -> {
            try {
                List<Framework.Dependency> dependenciesList = frameworkCaching.getAdditionalDependencies();

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
    private static HashMap<String, Object> getFrameworkCachingTrueBooleansHashMap(Framework framework, Map<String, Object> frameworkConfiguration) {
        HashMap<String, Object> frameworkFrameworkCachingBooleanMetadata = new HashMap<>();
        String cacheProvider = (String) frameworkConfiguration.get("cacheProvider");
        Optional<FrameworkCaching> selectedFrameworkCachingOption = framework.getSelectedCacheProviderByName(cacheProvider);
        selectedFrameworkCachingOption.ifPresent(frameworkCaching -> {
            for(String key : frameworkCaching.getMetadataBooleanTrueKeys()){
                frameworkFrameworkCachingBooleanMetadata.put(key, true);
            }
        });
        return frameworkFrameworkCachingBooleanMetadata;
    }

    static HashMap<String, Object> getProjectFilesEditsHashMap(String destinationFolder, String projectName, String groupLink, String projectPort, Database database, Credentials credentials, @NotNull Language language, String projectDescription, Map<String, Object> langageConfiguration, Framework framework, Map<String, Object> frameworkOptions) throws Exception {
        HashMap<String, Object> combinedMap = new HashMap<>();
        System.out.println("Project files edits hashmap");
        combinedMap.putAll(getConfigFileHashMap(projectPort, database, credentials, language, framework, frameworkOptions));
        combinedMap.putAll(getDependencyFileHashMap(projectDescription, database, language, framework, langageConfiguration, frameworkOptions));
        combinedMap.putAll(getInitialHashMap(destinationFolder, projectName, groupLink));
        combinedMap.putAll(getFrameworkSecurityTrueBooleansHashMap(framework,frameworkOptions));
        combinedMap.putAll(getFrameworkCachingTrueBooleansHashMap(framework,frameworkOptions));

        // Ajouter enableAuth dans les métadonnées (par défaut true si non spécifié)
        boolean enableAuth = true;
        if (frameworkOptions != null && frameworkOptions.containsKey("enableAuth")) {
            Object enableAuthValue = frameworkOptions.get("enableAuth");
            if (enableAuthValue instanceof Boolean) {
                enableAuth = (Boolean) enableAuthValue;
            } else if (enableAuthValue instanceof String) {
                enableAuth = Boolean.parseBoolean((String) enableAuthValue);
            }
        }
        combinedMap.put("enableAuth", enableAuth);

        return combinedMap;
    }

}

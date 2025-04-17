package org.labs.genesis.config.langage.generator.project;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkConfiguration;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.UIViewsConfiguration;
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
            configFile.put("databaseVersion",database.getDriverVersion());
        }
        configFile.putAll(frameworkOptions);

        return configFile;
    }

    static HashMap<String, Object> getDependencyFileHashMap(String projectDescription, Database database, Language language, Framework framework, Map<String, Object> langageConfiguration, Map<String, Object> frameworkConfiguration, FrameworkConfiguration configuration) {
        HashMap<String, Object> dependencyFileMap = new HashMap<>();
        dependencyFileMap.putAll(langageConfiguration);
        dependencyFileMap.putAll(frameworkConfiguration);
        dependencyFileMap.put("projectDescription", projectDescription);
        dependencyFileMap.put("useCloud", framework.getUseCloud());
        dependencyFileMap.put("useEurekaServer", framework.getUseEurekaServer());

        List<HashMap<String, String>> dependencies = getDependenciesHashMaps(configuration);
        dependencyFileMap.put("dependencies", dependencies);

        if (database != null && framework.getUseDB()) {
            dependencyFileMap.put("useDB", true);

            FrameworkConfiguration.Dependency databaseDependency = database.getDependencies().get(String.valueOf(language.getId()));

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

    private static @NotNull List<HashMap<String, String>> getDependenciesHashMaps(FrameworkConfiguration framework) {
        List<HashMap<String, String>> dependencies = new ArrayList<>();
        List<FrameworkConfiguration.Dependency> dependenciesList = framework.getDependencies();

        for (FrameworkConfiguration.Dependency dependency : dependenciesList) {
            HashMap<String, String> dependencyMap = new HashMap<>();
            dependencyMap.put("groupId", dependency.getGroupId());
            dependencyMap.put("artifactId", dependency.getArtifactId());
            dependencyMap.put("version", dependency.getVersion());
            dependencyMap.put("scope", dependency.getScope());
            dependencies.add(dependencyMap);
        }

        return dependencies;
    }

    static HashMap<String, Object> getProjectFilesEditsHashMap(String destinationFolder, String projectName, String groupLink, String projectPort, Database database, Credentials credentials, @NotNull Language language, String projectDescription, Map<String, Object> langageConfiguration, Framework framework, Map<String, Object> frameworkOptions, FrameworkConfiguration frameworkConfiguration) throws Exception {
        HashMap<String, Object> combinedMap = new HashMap<>();

        combinedMap.putAll(getConfigFileHashMap(projectPort, database, credentials, language, framework, frameworkOptions));
        combinedMap.putAll(getDependencyFileHashMap(projectDescription, database, language, framework, langageConfiguration, frameworkOptions, frameworkConfiguration));
        combinedMap.putAll(getInitialHashMap(destinationFolder, projectName, groupLink));

        return combinedMap;
    }

    // PARTIE FRONT-END GENESIS-MVC

    public static HashMap<String, Object> getAltCreateViewHashMap(UIViewsConfiguration editor) {
        HashMap<String, Object> altMap = new HashMap<>();

        altMap.put("formCreateLink", editor.getLayout().getTableLoop().getFormCreateLink());

        return altMap;
    }

    public static HashMap<String, Object> getSelectHashMap(UIViewsConfiguration uiViewsConfiguration) {
        HashMap<String, Object> altMap = new HashMap<>();

        altMap.put("dataTextOption", uiViewsConfiguration.getLayout().getTableLoop().getDataTextOption());
        altMap.put("dataSelectLoop", uiViewsConfiguration.getLayout().getTableLoop().getDataSelectLoop());
        altMap.put("dataValueOption", uiViewsConfiguration.getLayout().getTableLoop().getDataValueOption());

        return altMap;
    }

    public static HashMap<String, Object> getAltListViewHashMap(UIViewsConfiguration editor) {
        HashMap<String, Object> altMap = new HashMap<>();

        altMap.put("listLink", editor.getLayout().getMenu().getListLink());
        altMap.put("createLink", editor.getLayout().getMenu().getCreateLink());
        altMap.put("dataLoop", editor.getLayout().getTableLoop().getDataLoop());
        altMap.put("dataKeys", editor.getLayout().getTableLoop().getDataKeys());
        altMap.put("dataValues", editor.getLayout().getTableLoop().getDataValues());
        altMap.put("dataForeignValues", editor.getLayout().getTableLoop().getDataForeignValues());

        altMap.put("dataCancelId", editor.getLayout().getTableLoop().getDataCancelId());
        altMap.put("dataModificationId", editor.getLayout().getTableLoop().getDataModificationId());

        altMap.put("dataCancelButton", editor.getLayout().getTableLoop().getDataCancelButton());
        altMap.put("dataModificationButton", editor.getLayout().getTableLoop().getDataModificationButton());

        altMap.put("dataCancelTitleModal", editor.getLayout().getTableLoop().getDataCancelTitleModal());
        altMap.put("dataModificationTitleModal", editor.getLayout().getTableLoop().getDataModificationTitleModal());

        altMap.put("dataEditEventButton", editor.getLayout().getTableLoop().getDataEditEventButton());
        altMap.put("dataDeleteEventButton", editor.getLayout().getTableLoop().getDataDeleteEventButton());

        altMap.put("formUpdateLink", editor.getLayout().getTableLoop().getFormUpdateLink());
        altMap.put("formDeleteLink", editor.getLayout().getTableLoop().getFormDeleteLink());

        return altMap;
    }
}

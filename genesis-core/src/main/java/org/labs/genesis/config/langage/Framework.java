package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.labs.genesis.config.Constantes;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class Framework {
    private int id;
    private int languageId;
    private String coreFramework;
    private String name;
    private String template;
    private Boolean useDB;
    private Boolean useCloud;
    private Boolean useEurekaServer;
    private Boolean isGateway;
    private Boolean useFrontendApp = true;
    private Boolean withGroupId;
    private List<FilesEdit> additionalFiles;
    private List<ConfigurationMetadata> configurations;
    private List<ConfigurationMetadata> eurekaClientConfigurations;
    private List<Dependency> dependencies;
    private Model model;
    private ModelDao modelDao;
    private Service service;
    private Controller controller;
    private List<FrameworkSecurity> frameworkSecurities;

    public void setFrameworkSecurities() throws IOException {
        this.frameworkSecurities = Arrays.stream(FileUtils.fromYaml(FrameworkSecurity[].class, Constantes.FRAMEWORK_SECURITY_YAML))
                .filter(fs -> fs.getFrameworkId() == this.id)
                .collect(Collectors.toList());
    }

    public Optional<FrameworkSecurity> getSelectedSecurityByName(String securityType){
        return this
                .getFrameworkSecurities()
                .stream()
                .filter(fs -> fs.getName().equalsIgnoreCase(securityType))
                .findFirst();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Getter
    @Setter
    @ToString
    public static class Dependency {
        private String groupId;
        private String artifactId;
        private String version;
        private String scope;
    }

    @Getter
    @Setter
    @ToString
    public static class Model {
        private Boolean toGenerate;
        private String modelImports;
        private String modelExtends;
        private String modelAnnotations;
        private String modelFieldContent;
        private String modelGetterSetter;
        private String modelConstructors;
        private String modelSavePath;
        private String modelForeignContextAttribute;
        private String modelPackage;
        private List<FilesEdit> modelAdditionalFiles;
        private Map<String, Object> validationAnnotations;
    }

    @Getter
    @Setter
    @ToString
    public static class ModelDao {
        private Boolean toGenerate;
        private String modelDaoImports;
        private String modelDaoAnnotations;
        private String modelDaoClassKeyword;
        private String modelDaoExtends;
        private String modelDaoName;
        private String modelDaoFieldContent;
        private String modelDaoMethodContent;
        private String modelDaoConstructors;
        private String modelDaoSavePath;
        private String modelDaoPackage;
        private List<FilesEdit> modelDaoAdditionalFiles;
    }

    @Getter
    @Setter
    @ToString
    public static class Service {
        private Boolean toGenerate;
        private String serviceImports;
        private String serviceClassKeyword;
        private String serviceAnnotations;
        private String serviceExtends;
        private String serviceName;
        private String serviceFieldContent;
        private String serviceConstructors;
        private String serviceMethodContent;
        private String serviceSavePath;
        private String servicePackage;
        private List<FilesEdit> serviceAdditionalFiles;
    }

    @Getter
    @Setter
    @ToString
    public static class Controller {
        private Boolean toGenerate;
        private String controllerImports;
        private String controllerAnnotations;
        private String controllerExtends;
        private String controllerName;
        private String controllerFieldContent;
        private String controllerConstructors;
        private String controllerMethodContent;
        private String controllerSavePath;
        private String controllerPackage;
        private List<FilesEdit> controllerAdditionalFiles;
    }
}
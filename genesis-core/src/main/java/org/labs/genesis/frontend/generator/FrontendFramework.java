package org.labs.genesis.frontend.generator;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.docker.DockerConf;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.genesis.frontend.generator.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FrontendFramework
{
    private int id;
    private int  languageId;
    private String coreFramework;
    private String name ;
    private String template;
    private String componentExtension;
    private List<FilesEdit> additionalFiles;
    private List<FilesEdit> conditionalFiles;
    private List<FilesEdit> authenticationFiles;
    private List<Component> components;
    private ServiceComponent serviceComponent;
    private ModelComponent modelComponent;
    private String initPath;
    private List<ComponentRoute> componentRoutes;
    private Map<String,String> validationRules;
    private FrontendLayout frontendLayout;
    private ProjectBranding projectBranding;
    private FrontendDestinationPaths frontendPaths;
    private List<FilesEdit> mereFiles = new ArrayList<>();
    private List<FilesEdit> filleFiles = new ArrayList<>();
    private String defaultPort = "9000" ;
    private DockerConf docker;

    public  FrontendFramework(){
        setProjectBranding(new ProjectBranding());
        setFrontendLayout(new FrontendLayout());
    }

    public void addRoute(ComponentRoute route){
        if (route.getLabel() == null || route.getLabel().isEmpty()){
            route.setLabel( route.getComponentName());
        }
        getComponentRoutes().add(route);
    }

    public void clearRoutes() {
        if(componentRoutes != null) {
            componentRoutes.clear();
        }
    }

    public List<ComponentRoute> getComponentRoutes(){
        if(componentRoutes == null){
            componentRoutes = new ArrayList<>();
        }
        return componentRoutes;
    }

    @Override
    public String toString() {
        return this.coreFramework;
    }
}

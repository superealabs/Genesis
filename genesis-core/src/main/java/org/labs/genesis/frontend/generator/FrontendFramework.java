package org.labs.genesis.frontend.generator;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.genesis.frontend.generator.model.Component;
import org.labs.genesis.frontend.generator.model.ComponentRoute;
import org.labs.genesis.frontend.generator.model.ModelComponent;
import org.labs.genesis.frontend.generator.model.ServiceComponent;

import java.util.ArrayList;
import java.util.List;

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
    private List<Component> components;
    private ServiceComponent serviceComponent;
    private ModelComponent modelComponent;
    private String initPath;
    private List<ComponentRoute> componentRoutes;

    public void addRoute(ComponentRoute route){
        getComponentRoutes().add(route);
    }

    public List<ComponentRoute> getComponentRoutes(){
        if(componentRoutes == null){
            componentRoutes = new ArrayList<>();
        }
        return componentRoutes;
    }
}

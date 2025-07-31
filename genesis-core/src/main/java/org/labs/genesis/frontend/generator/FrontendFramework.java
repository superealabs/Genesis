package org.labs.genesis.frontend.generator;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.genesis.frontend.generator.model.Component;
import org.labs.genesis.frontend.generator.model.ComponetRoute;
import org.labs.genesis.frontend.generator.model.ModelComponent;
import org.labs.genesis.frontend.generator.model.ServiceComponent;

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
    private List<ComponetRoute> componentRoutes;

    public void addRoute(ComponetRoute route){
        getComponentRoutes().add(route);
    }
    public  void addRoute(String componentName, String route){
        if (componentName == null && (route == null || route.isEmpty())) {
            return;
        }
        addRoute(new ComponetRoute(componentName, route));
    }
}

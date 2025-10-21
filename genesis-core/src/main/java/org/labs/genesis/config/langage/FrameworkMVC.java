package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.frontend.generator.model.FrontendDestinationPaths;
import org.labs.genesis.frontend.generator.model.FrontendLayout;
import org.labs.genesis.frontend.generator.model.ProjectBranding;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class FrameworkMVC extends Framework {
    private View view;
    private java.util.List<ViewsTemplate> viewsTemplate;
    private java.util.List<String> excludeProjectFilesEdits;
    private FrontendLayout frontendLayout;
    private ProjectBranding projectBranding;
    private FrontendDestinationPaths frontendPaths;

    public void setViewsTemplate() throws IOException {
        this.viewsTemplate = Arrays.stream(FileUtils.fromYaml(ViewsTemplate[].class, Constantes.VIEWS_TEMPLATES_YAML))
                .collect(Collectors.toList());
    }

    public ViewsTemplate findViewsTemplateById(int idViewsTemplate) {
        return viewsTemplate.stream()
                .filter(viewsTemplate -> viewsTemplate.getId() == idViewsTemplate)
                .findFirst()
                .orElse(null);
    }

    @Getter
    @Setter
    @ToString
    public static class View {
        private Boolean toGenerate;
        private String viewTemplateEngine;
        private String viewExtension;
        private String staticFilesPath;
        private String viewSavePath;
        private String rootPath;
        private String backLink;
        private String previousLink;
        private Layout layout;
        private List list;
        private Create create;
        private Edit edit;
        private Detail detail;
        private Error error;
        private java.util.List<FilesEdit> templateEngineFilesEdits;
        private java.util.List<Project.ProjectFiles> templateEngineFiles;
        private java.util.List<Project.ProjectFolders> templateEngineFolders;
    }


    @Getter
    @Setter
    @ToString
    public static class Layout {
        private String name;
        private String assetsImportLink;
        private String viewAnnotations;
        private String pageName;
        private String navLink;
        private String callContent;
        private String logoutLink;
        private String destinationPath;
    }

    @Getter
    @Setter
    @ToString
    public static class List {
        private String name;
        private String viewAnnotations;
        private String inputTagHelper;
        private String inputRadioTagHelper;
        private String inputDateTagHelper;
        private String selectTagHelper;
        private String deleteDataTagHelper;
        private String pageSizeTagHelper;
        private String dataValue;
        private String dataForeignValue;
        private String inlineLoopStatement;
        private String blockLoopStatementStart;
        private String blockLoopStatementEnd;
        private String filterLink;
        private String sortLink;
        private String detailsLink;
        private String createLink;
        private String updateLink;
        private String deleteLink;
        private String pageSizeChangeLink;
        private String previousPageLink;
        private String previousClassCondition;
        private String pagesListLoop;
        private String nextPageLink;
        private String nextClassCondition;
        private String onGoingPageLink;
        private String onGoingPageSizeTagHelper;
        private String totalElementsTagHelper;
        private String onGoingSortOrderTagHelper;
        private String activeSortAscCondition;
        private String activeSortDescCondition;
        private String onGoingPagesLoop;
        private String scriptSection;
    }

    @Getter
    @Setter
    @ToString
    public static class Detail {
        private String name;
        private String viewAnnotations;
        private String dataValue;
        private String dataForeignValue;
        private String deleteDataTagHelper;
        private String updateLink;
        private String deleteLink;
    }

    @Getter
    @Setter
    @ToString
    public static class Create {
        private String name;
        private String viewAnnotations;
        private String validationSection;
        private String validationTagHelper;
        private String selectValidationTagHelper;
        private String inputTagHelper;
        private String textAreaTagHelper;
        private String textAreaValidationTagHelper;
        private String checkedRadioTagHelper;
        private String selectTagHelper;
        private String createLink;
        private String scriptSection;
    }

    @Getter
    @Setter
    @ToString
    public static class Edit {
        private String name;
        private String viewAnnotations;
        private String validationSection;
        private String validationTagHelper;
        private String selectValidationTagHelper;
        private String inputTagHelper;
        private String textAreaTagHelper;
        private String textAreaValidationTagHelper;
        private String selectTagHelper;
        private String updateLink;
        private String scriptSection;
    }

    @Getter
    @Setter
    @ToString
    public static class Error {
        private String name;
        private String viewAnnotations;
        private String errorMessage;
        private String destinationPath;
    }
}

package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class ViewsTemplateEngine {
    private int id;
    private int frameworkMvcId;
    private String name;
    private String viewExtension;
    private Layout layout;
    private List list;
    private Create create;
    private Edit edit;
    private Detail detail;
    private java.util.List<FilesEdit> templateEngineFilesEdits;
    private java.util.List<Project.ProjectFiles> templateEngineFiles;
    private java.util.List<Project.ProjectFolders> templateEngineFolders;

    @Override
    public String toString() {
        return this.name;
    }

    @Getter
    @Setter
    @ToString
    public static class Layout {
        private String name;
        private String destinationPath;
        private String navLink;
        private String rootPath;
        private String pageName;
        private String callContent;
        private String currentViewContext;
    }

    @Getter
    @Setter
    @ToString
    public static class List {
        private String modelType;
        private String assetsImportLink;
        private String dataValue;
        private String dataForeignValue;
        private String inlineLoopStatement;
        private String blockLoopStatementStart;
        private String blockLoopStatementEnd;
        private String backLink;
        private String sortLink;
        private String detailsLink;
        private String createLink;
        private String updateLink;
        private String deleteLink;
        private String deleteTagHelper;
        private String pageSizeChangeLink;
        private String pageSizeTagHelper;
        private String previousPageLink;
        private String previousClassCondition;
        private String pagesListLoop;
        private String nextPageLink;
        private String nextClassCondition;
        private String onGoingPageLink;
        private String onGoingPageTagHelper;
        private String onGoingPagesLoop;
    }

    @Getter
    @Setter
    @ToString
    public static class Detail {
        private String modelType;
        private String dataValue;
        private String dataForeignValue;
        private String deleteLink;
        private String updateLink;
        private String backLink;
    }

    @Getter
    @Setter
    @ToString
    public static class Create {
        private String modelType;
        private String backLink;
        private String inputTagHelper;
        private String selectTagHelper;
        private String createLink;
//        private Input[] inputsList;
    }

    @Getter
    @Setter
    @ToString
    public static class Edit {
        private String modelType;
        private String backLink;
        private String inputTagHelper;
        private String selectTagHelper;
        private String updateLink;
//        private Input[] inputsList;
    }
}

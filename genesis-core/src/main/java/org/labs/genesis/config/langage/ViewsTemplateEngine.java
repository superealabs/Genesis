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
    }

    @Getter
    @Setter
    @ToString
    public static class List {
        private String modelType;
        private String dataValue;
        private String dataForeignValue;
        private String blockLoopStatementStart;
        private String blockLoopStatementEnd;
        private String inlineLoopStatement;
    }
}

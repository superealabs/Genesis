package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
public class ViewsTemplate {
    private int id;
    private String name;
    private String template;
    private String preview;
    private String layoutTemplate;
    private String listTemplate;
    private String formTemplate;
    private String detailTemplate;
    private List<FilesEdit> templateFilesEdits;
    private List<TemplateFiles> templateFiles;
    private List<TemplateFolders> templateFolders;

    @Override
    public String toString() {
        return this.name;
    }

    @Getter
    @Setter
    @ToString
    public static class TemplateFiles {
        private String fileType;
        private String fileName;
        private String sourcePath;
        private String destinationPath;
    }

    @Getter
    @Setter
    @ToString
    public static class TemplateFolders {
        private String folderName;
        private String folderType;
        private String sourcePath;
        private String destinationPath;
    }
}

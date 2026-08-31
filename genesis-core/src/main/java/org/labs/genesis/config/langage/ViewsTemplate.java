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
    private String createTemplate;
    private String editTemplate;
    private String formTemplate;
    private String detailTemplate;
    private String errorTemplate;
    private List<FilesEdit> templateFilesEdits;
    private List<Project.ProjectFiles> templateFiles;
    private List<Project.ProjectFolders> templateFolders;

    @Override
    public String toString() {
        return this.name;
    }
}

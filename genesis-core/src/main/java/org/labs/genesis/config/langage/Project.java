package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
public class Project {
    private int id;
    private String name;
    private List<String> coreFrameworks;
    private List<ProjectFiles> projectFiles;
    private List<ProjectFolders> projectFolders;
    private List<FilesEdit> projectFilesEdits;

    @Override
    public String toString() {
        return this.name;
    }
    @Getter
    @Setter
    @ToString
    public static class ProjectFiles {
        private String fileType;
        private String fileName;
        private String sourcePath;
        private String destinationPath;
    }

    @Getter
    @Setter
    @ToString
    public static class ProjectFolders {
        private String folderName;
        private String folderType;
        private String sourcePath;
        private String destinationPath;
    }

}

package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FilesEdit {
    private String fileType;
    private String fileName;
    private String extension;
    private String content;
    private String destinationPath;
}
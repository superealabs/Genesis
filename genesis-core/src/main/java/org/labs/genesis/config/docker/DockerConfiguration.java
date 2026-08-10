package org.labs.genesis.config.docker;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DockerConfiguration {
    String fileType;
    String fileName;
    String extension;
    String content;
    String destinationPath;

    static DockerConfiguration get(List<DockerConfiguration> config, String fileType) {
        return config.stream()
                .filter(docker -> fileType.equals(docker.getFileType()))
                .findFirst()
                .orElse(null);
    }
}

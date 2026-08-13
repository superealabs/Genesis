package org.labs.genesis.config.tools;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DockerConfiguration {

    private boolean useDocker;

    private boolean frontendDockerized;

    private String frontendContainer;

    private boolean backendDockerized;

    private String backendContainer;

    private String langVersion;

}
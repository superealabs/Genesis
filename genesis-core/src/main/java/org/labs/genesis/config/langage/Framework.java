package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Framework {
    private int id;
    private int languageId;
    private String coreFramework;
    private String fileName;
    private String name;
    private String template;
    private Boolean useDB;
    private Boolean isMVC;
    private Boolean useCloud;
    private Boolean useEurekaServer;
    private Boolean isGateway;
    private Boolean withGroupId;

}
package org.labs.genesis.wizards.fieldHandler;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseFields {
    String host;
    String port;
    String databaseName;
    String sid;
    String driverType;
    String user;
    String password;
}

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

    @Override
    public String toString() {
        return "DatabaseFields{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", sid='" + sid + '\'' +
                ", driverType='" + driverType + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}

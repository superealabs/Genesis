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
    String driverName;
    String user;
    String password;
    String driverType;

    @Override
    public String toString() {
        return "DatabaseFields{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", sid='" + sid + '\'' +
                ", driverType='" + driverName + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}

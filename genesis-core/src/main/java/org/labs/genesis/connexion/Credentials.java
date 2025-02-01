package org.labs.genesis.connexion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Credentials {
    private String databaseName, schemaName, user, pwd, host, port, driverType, SID;
    private boolean useSSL, allowPublicKeyRetrieval, trustCertificate;

    public Credentials setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public Credentials setUser(String user) {
        this.user = user;
        return this;
    }

    public Credentials setPwd(String pwd) {
        this.pwd = pwd;
        return this;
    }

    public Credentials setHost(String host) {
        this.host = host;
        return this;
    }

    public Credentials setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
        return this;
    }

    public Credentials setAllowPublicKeyRetrieval(boolean allowPublicKeyRetrieval) {
        this.allowPublicKeyRetrieval = allowPublicKeyRetrieval;
        return this;
    }

    public Credentials setTrustCertificate(boolean trustCertificate) {
        this.trustCertificate = trustCertificate;
        return this;
    }

    public Credentials setPort(String port) {
        this.port = port;
        return this;
    }

    public Credentials setDriverType(String driverType) {
        this.driverType = driverType;
        return this;
    }

    public Credentials setSID(String SID) {
        this.SID = SID;
        return this;
    }

    public Credentials setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }
}

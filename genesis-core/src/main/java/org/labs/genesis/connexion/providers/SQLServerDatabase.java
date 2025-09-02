package org.labs.genesis.connexion.providers;

import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;

public class SQLServerDatabase extends Database {
    @Override
    public String getJdbcUrl(Credentials credentials) {
        String port;
        if (credentials.getPort() != null)
            port = credentials.getPort();
        else port = getPort();
        return String.format("jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;encrypt=%s;trustServerCertificate=%s;",
                credentials.getHost(),
                port,
                credentials.getDatabaseName(),
                credentials.getUser(),
                credentials.getPwd(),
                credentials.isUseSSL(),
                credentials.isTrustCertificate());
    }
}

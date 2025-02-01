package org.labs.genesis.connexion.adapter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.providers.*;

import java.io.IOException;

public class DatabaseDeserializer extends JsonDeserializer<Database> {

    @Override
    public Database deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        String typeName = node.get("name").asText();

        Class<? extends Database> databaseType = switch (typeName) {
            case "MySQL" -> MySQLDatabase.class;
            case "PostgreSQL" -> PostgreSQLDatabase.class;
            case "Oracle" -> OracleDatabase.class;
            case "SQL Server" -> SQLServerDatabase.class;
            default -> throw new IllegalArgumentException("Unknow database type : " + typeName);
        };

        return parser.getCodec().treeToValue(node, databaseType);
    }
}
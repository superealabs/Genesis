package org.labs.genesis.config;

public class Constantes {
    public static final String DATABASE_JSON = "data_genesis/json/databases.json";
    public static final String LANGUAGE_JSON = "data_genesis/json/languages.json";
    public static final String INPUT_TYPE_MAPPING_JSON = "data_genesis/json/input-type-mapping.json";
    public static final String LLM_API_CONFIG_JSON = "data_genesis/json/llm-api-config.json";
    public static final String FRAMEWORK_YAML = "data_genesis/yaml/frameworks.yaml";
    public static final String FRAMEWORK_MVC_YAML = "data_genesis/yaml/frameworks-mvc.yaml";
    public static final String PROJECT_YAML = "data_genesis/yaml/projects.yaml";
    public static final String CONSTRAINT_QUERIES_YAML = "data_genesis/yaml/constraint-queries.yaml";
    public static final String FRAMEWORK_SECURITY_YAML = "data_genesis/yaml/framework-securities.yaml";
    public static final String VIEWS_TEMPLATE_ENGINE_YAML = "data_genesis/yaml/views-template-engine.yaml";
    public static final String VIEWS_TEMPLATES_YAML = "data_genesis/yaml/views-template.yaml";
    public static final String DATA_PATH = "data_genesis";
    public static final String TEMPLATES_PATH = "data_genesis/templates";
    public static final String MODEL_TEMPLATE_EXT = "genesis";
    public static final String TEMPLATE_EXT = "templ";

    // DATABASES
    public static final int MySQL_ID = 1;
    public static final int PostgreSQL_ID = 2;
    public static final int SQL_Server_ID = 3;
    public static final int Oracle_ID = 4;

    // LANGUAGES
    public static final int Java_ID = 1;
    public static final int CSharp_ID = 2;

    // FRAMEWORKS
    public static final int Spring_REST_API_ID = 1;
    public static final int NET_ID = 2;
    public static final int Spring_Eureka_Server_ID = 3;
    public static final int Spring_Api_Gateway_ID = 4;
    public static final int DOTNET_MVC_ID = 5;

    // PROJECTS
    public static final int Maven_ID = 1;
    public static final int ASP_ID = 2;

    // VIEWS TEMPLATE ENGINE
    public static final int Razor_ID = 1;

    // VIEWS TEMPLATE
    public static final int Template_1_ID = 1;
}

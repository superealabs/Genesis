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
    public static final String FRAMEWORK_CACHING_YAML = "data_genesis/yaml/framework-caching.yaml";
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
    public static final int TypeScript_ID = 4;
    public static final int Python_ID = 5;

    // FRAMEWORKS
    public static final int Spring_REST_API_ID = 1;
    public static final int NET_ID = 2;
    public static final int Spring_Eureka_Server_ID = 3;
    public static final int Spring_Api_Gateway_ID = 4;
    public static final int Django_ID = 7;
    public static final int Django_mvt = 6;
    public static final int DOTNET_MVC_ID = 5;
    public static final int Spring_MVC_ID = 6;
    public static final int ExpressJs_ID = 8;

    // PROJECTS
    public static final int Maven_ID = 1;
    public static final int ASP_ID = 2;
    public static final int Django_Project_ID = 4;

    // Frontend
    public  static  final   String FRONTEND_LANGUAGE_JSON = "data_genesis/frontend/json/frontend.json";
    public  static  final   String FRONTEND_FRAMEWORK_YAML = "data_genesis/frontend/yaml/framework_frontend.yaml";
    public  static  final   String LANGS_YAML = "data_genesis/frontend/yaml/interface_lang.yaml";
    public  static  final   int TYPESCRIPT_ID = 1;
    public  static  final   int ANGULAR_ID = 1;
    public  static  final   int VUE_JS_ID = 2;
    public  static  final   int REACT_ID = 3;
    public  static  final   String FRONTEND_SKELLETTON_DIRECTORY = "data_genesis/frontend/skeletoons/";
    public  static  final   String WEBAPP_DIR_TEMPLATE = "${destinationFolder}/${majStart(projectName)}${majStart(webappFolder)}";
    public  static  final   String FRONTEND_TEMPLATE_DIRECTORY = DATA_PATH+"/frontend/template";
    public static final int Node_ID = 3;

    // VIEWS TEMPLATE
    public static final int Template_1_ID = 1;
    public static final int Template_2_ID = 2;

    // SUB Generation
    public static final String GENESIS_CONTEXT_FILENAME = "genesis-context";
    public static final String GENESIS_CONTEXT_FILE_EXTENSION = "json";
    public static final String GENESIS_CONTEXT_FILE = GENESIS_CONTEXT_FILENAME+"."+GENESIS_CONTEXT_FILE_EXTENSION;


    public static final String GENESIS_GENERATION_TEMP_FILE_EXTENSION = "temp";
    public static final String GENESIS_BASE_FILES_HIDDEN_DIRECTORY_NAME = ".base";
}

package org.labs.handler;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.ConfigurationMetadata;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.config.langage.generator.project.FolderSelectorCombo;
import org.labs.genesis.config.langage.generator.project.GroqApiClient;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.SQLRunner;
import org.labs.utils.FileUtils;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.labs.genesis.config.ProjectGenerationContext.*;

public class ProjectGeneratorHandler {

    private final Credentials credentials;
    private final ProjectGenerator projectGenerator;

    public ProjectGeneratorHandler() {
        this.credentials = new Credentials();
        projectGenerator = new ProjectGenerator();
    }

    public void generateProject() {
        System.out.println("\n** Welcome to the GENESIS-CLI ** \n\n Let's get started 🚀\n");

        try (Scanner scanner = new Scanner(System.in)) {
            // Création du contexte
            ProjectGenerationContext context = new ProjectGenerationContext();

            // Étape 1 : Initialisation
            initializeProject(context, scanner);

            // Étape 2 : Configuration de la base de données
            if (context.getFramework().getUseDB()) {
                configureDatabase(context, scanner);
            }

            // Étape 3 : Options de génération pour les WEB API et configurations spécifiques
            configureGenerationOptions(context, scanner);

            // Étape 4 : Association avec le serveur Eureka
            associateEurekaServer(context, scanner);

            // FIN PARCOURS
            projectGenerator.generateProject(context);

            System.out.println("\nProject generated successfully! 👨🏽‍💻\n\nSee you on the next project 👋🏼\n");

        } catch (Exception e) {
            System.err.println("\nAn error occurred during project generation: \n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeProject(ProjectGenerationContext context, Scanner scanner) {
        // INITIALISATION ////////
        String projectName = getNonEmptyInput(scanner, "Enter the project name");
        context.setProjectName(projectName);

        int languageId = getLanguageSelection(scanner);
        Language language = ProjectGenerator.languages.get(languageId);
        context.setLanguage(language);

        HashMap<String, Object> languageConfiguration = configureLangage(scanner, language);
        context.setLanguageConfiguration(languageConfiguration);

        String coreFramework = getCoreFrameworkSelection(scanner, language);

        int projectId = getProjectId(scanner, coreFramework);
        Project project = ProjectGenerator.projects.get(projectId);
        context.setProject(project);

        String destinationFolder = FolderSelectorCombo.selectDestinationFolder(scanner);
        context.setDestinationFolder(destinationFolder);

        // TYPE DE PROJET
        int frameworkId = getFrameworkSelection(scanner, language, coreFramework);
        Framework framework = ProjectGenerator.frameworks.get(frameworkId);
        context.setFramework(framework);
    }

    private void configureDatabase(ProjectGenerationContext context, Scanner scanner) throws IOException {
        Database database;
        Connection connection;
        List<String> entityNames;
        String groupLink;

        int databaseId = getDatabaseId(scanner);
        database = ProjectGenerator.databases.get(databaseId);
        context.setDatabase(database);

        connection = configureCredentials(scanner, database);
        context.setCredentials(credentials);
        context.setConnection(connection);

        handleScriptExecution(scanner, database, connection);

        List<String> allTableNames = fetchEntityNames(database, connection);
        entityNames = handleEntitySelection(scanner, allTableNames);
        context.setEntityNames(entityNames);

        if (context.getFramework().getWithGroupId()) {
            groupLink = getNonEmptyInput(scanner, "Enter the group link");
            context.setGroupLink(groupLink);
        }
    }

    private void configureGenerationOptions(ProjectGenerationContext context, Scanner scanner) {
        List<String> generationOptions;
        boolean generateProjectStructure;
        String groupLink = context.getGroupLink();
        Framework framework = context.getFramework();

        if (framework.getUseDB()) {
            // OPTIONS DE GÉNÉRATION
            generationOptions = getGenerationOptions(scanner);
            context.setGenerationOptions(generationOptions);

            // OPTION POUR GÉNÉRER LA STRUCTURE DU PROJET OU PAS
            generateProjectStructure = getGenerateProjectStructureOption(scanner);
            context.setGenerateProjectStructure(generateProjectStructure);
        }

        // CONFIGURATION PERSONNALISÉE
        String projectPort;
        String projectDescription;
        HashMap<String, Object> frameworkConfiguration;

        if (context.isGenerateProjectStructure()) {
            if ((groupLink == null || groupLink.isBlank()) && framework.getWithGroupId()) {
                groupLink = getNonEmptyInput(scanner, "Enter the group link");
                context.setGroupLink(groupLink);
            }

            projectPort = getValidPort(scanner);
            context.setProjectPort(projectPort);

            projectDescription = getNonEmptyInput(scanner, "Enter the project description");
            context.setProjectDescription(projectDescription);

            frameworkConfiguration = configureFramework(scanner, framework);
            context.setFrameworkConfiguration(frameworkConfiguration);

            // AJOUTER LA CONFIGURATION POUR L'API GATEWAY SI NÉCESSAIRE
            if (framework.getIsGateway()) {
                configureApiGateway(scanner, frameworkConfiguration);
            }
        }
    }

    private void associateEurekaServer(ProjectGenerationContext context, Scanner scanner) {
        Framework framework = context.getFramework();
        Map<String, Object> frameworkConfiguration = context.getFrameworkConfiguration();

        boolean useEurekaServer;
        String input = getNonEmptyInput(scanner, "Use an Eureka Server? (y/n)");
        useEurekaServer = input.equalsIgnoreCase("y");

        if (useEurekaServer) {
            frameworkConfiguration.putAll(configureFrameworkWithEureka(scanner, framework));
            framework.setUseCloud(true);
            framework.setUseEurekaServer(true);
            context.setFrameworkConfiguration(frameworkConfiguration);
        }
    }

    private void configureApiGateway(Scanner scanner, Map<String, Object> frameworkConfiguration) {
        List<Map<String, Object>> routes = new ArrayList<>();
        List<String> httpMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE", "CONNECT");

        String addMoreRoutes;
        do {
            System.out.println("Configure a new route for the API Gateway.");

            String routeId = getNonEmptyInput(scanner, "Enter route ID");
            String uri = getNonEmptyInput(scanner, "Enter route URI (e.g., http://service1)");
            String path = getNonEmptyInput(scanner, "Enter route path (predicate) (e.g., /path1)");

            // Demander les méthodes HTTP avec indices
            System.out.println("Select HTTP methods for this route:");
            List<String> selectedMethods = handleOptionSelection(scanner, httpMethods, "HTTP method(s)");

            Map<String, Object> route = new HashMap<>();
            route.put("id", routeId);
            route.put("uri", uri);
            route.put("path", path);
            route.put("method", String.join(",", selectedMethods)); // Combine les méthodes sélectionnées

            routes.add(route);
            System.out.println();
            addMoreRoutes = getNonEmptyInput(scanner, "Do you want to add another route? (y/n)");

        } while (addMoreRoutes.equalsIgnoreCase("y"));

        frameworkConfiguration.put("routes", routes);

        // CONFIGURATION D'AUTHENTIFICATION POUR L'API GATEWAY
        String username = getNonEmptyInput(scanner, "Enter username for API Gateway authentication");
        String password = getNonEmptyInput(scanner, "Enter password for API Gateway authentication");
        String role = getNonEmptyInput(scanner, "Enter role for API Gateway authentication");

        frameworkConfiguration.put("username", username);
        frameworkConfiguration.put("password", password);
        frameworkConfiguration.put("role", role);
    }

    private List<String> handleOptionSelection(Scanner scanner, List<String> options, String optionType) {
        if (options.isEmpty()) {
            return new ArrayList<>();
        }

        while (true) {
            System.out.println("\nAvailable " + optionType + ":");
            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ") " + options.get(i));
            }
            System.out.println("\nEnter the numbers of the " + optionType.toLowerCase() + " you want to select, separated by commas (e.g., 1,3,5):");

            String input = scanner.nextLine().trim();
            List<String> selectedOptions = validateSelection(input, options);

            if (!selectedOptions.isEmpty()) {
                System.out.println("Selected " + optionType + ": " + String.join(", ", selectedOptions) + "\n");
                return selectedOptions;
            }

            System.out.println("\nInvalid selection. Please try again.");
        }
    }

    private List<String> validateSelection(String input, List<String> options) {
        return getSelectedOptions(input, options);
    }

    @NotNull
    private List<String> getSelectedOptions(String input, List<String> options) {
        List<String> selectedOptions = new ArrayList<>();
        String[] selectedIndexes = input.split(",");

        boolean hasInvalidSelection = false;

        for (String index : selectedIndexes) {
            try {
                int idx = Integer.parseInt(index.trim()) - 1;
                if (idx >= 0 && idx < options.size()) {
                    selectedOptions.add(options.get(idx));
                } else {
                    System.out.println("Invalid selection: " + index);
                    hasInvalidSelection = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + index);
                hasInvalidSelection = true;
            }
        }

        return hasInvalidSelection ? new ArrayList<>() : selectedOptions;
    }


    private boolean getGenerateProjectStructureOption(Scanner scanner) {
        System.out.println();
        while (true) {
            String input = getNonEmptyInput(scanner, "Do you want to generate the full project structure? (y/n)");
            if (input.equalsIgnoreCase("y")) {
                return true;
            } else if (input.equalsIgnoreCase("n")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }


    private List<String> getGenerationOptions(Scanner scanner) {
        System.out.println("Choose the components to generate:");
        System.out.println("1) " + COMPONENT_MODEL + " only");
        System.out.println("2) " + COMPONENT_MODEL + " + " + COMPONENT_DAO);
        System.out.println("3) " + COMPONENT_MODEL + " + " + COMPONENT_DAO + " + " + COMPONENT_SERVICE);
        System.out.println("4) Full stack (" + COMPONENT_MODEL + " + " + COMPONENT_DAO + " + " + COMPONENT_SERVICE + " + " + COMPONENT_CONTROLLER + ")");

        while (true) {
            System.out.print("Enter your choice (1-4): ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    return List.of(COMPONENT_MODEL);
                case "2":
                    return List.of(COMPONENT_MODEL, COMPONENT_DAO);
                case "3":
                    return List.of(COMPONENT_MODEL, COMPONENT_DAO, COMPONENT_SERVICE);
                case "4":
                    return List.of(COMPONENT_MODEL, COMPONENT_DAO, COMPONENT_SERVICE, COMPONENT_CONTROLLER);
                default:
                    System.out.println("Invalid input. Please enter a number between 1 and 4.");
            }
        }
    }


    private void handleScriptExecution(Scanner scanner, Database database, Connection connection) throws IOException {
        String input;
        while (true) {
            input = getNonEmptyInput(scanner, "Would you like to execute a script in the database? (y/n)");
            if (input.equalsIgnoreCase("y")) {
                break;
            } else if (input.equalsIgnoreCase("n")) {
                System.out.println("Skipping script execution.");
                return;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }

        while (true) {
            input = getNonEmptyInput(scanner, "Would you like to generate the script? (y/n)");
            if (input.equalsIgnoreCase("y")) {
                handleGeneratedScript(scanner, database, connection);
                break;
            } else if (input.equalsIgnoreCase("n")) {
                handleExistingScript(scanner, connection);
                break;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }


    private void handleGeneratedScript(Scanner scanner, Database database, Connection connection) throws IOException {
        String description, scriptContent, sqlFilePath;
        String scriptFilePath = null;
        while (true) {
            description = getNonEmptyInput(scanner, "Enter a description of the tables or modifications to create");
            try {
                scriptContent = GroqApiClient.generateSQL(database, description);
            } catch (Exception e) {
                System.out.println("\nAn error occurred during script generation: \n" + e.getMessage());
                String userInput = getNonEmptyInput(scanner, "Would you like to try again? (y/n)");
                if (userInput.equalsIgnoreCase("n")) {
                    break;
                } else {
                    continue;
                }
            }

            System.out.println("Enter the path where the SQL file should be saved");
            sqlFilePath = FolderSelectorCombo.selectDestinationFolder(scanner);

            scriptFilePath = createSQLFile(sqlFilePath, scriptContent);

            String regenerateInput = getNonEmptyInput(scanner, "Regenerate the script? (y/n)");
            if (regenerateInput.equalsIgnoreCase("y")) {
                assert scriptFilePath != null;
                Files.deleteIfExists(Path.of(scriptFilePath));
            }

            if (regenerateInput.equalsIgnoreCase("n")) {
                break;
            }
        }

        if (scriptFilePath != null) {
            while (true) {
                String confirmInput = getNonEmptyInput(scanner, "Confirm execution of the generated script? (y/n)");
                if (confirmInput.equalsIgnoreCase("y")) {
                    executeSQLScript(connection, scriptFilePath, scanner);
                    break;
                } else if (confirmInput.equalsIgnoreCase("n")) {
                    System.out.println("Skipping script execution.");
                    break;
                } else {
                    System.out.println("Invalid input. Please enter 'y' or 'n'.");
                }
            }
        }
    }


    private void handleExistingScript(Scanner scanner, Connection connection) throws FileNotFoundException {
        String scriptPath = FolderSelectorCombo.selectDestinationFolder(scanner);

        while (true) {
            System.out.println("Script content to be executed:");
            System.out.println(FileUtils.getFileContentSQL(scriptPath));

            String input = getNonEmptyInput(scanner, "Confirm execution of the script? (y/n)");
            if (input.equalsIgnoreCase("y")) {
                executeSQLScript(connection, scriptPath, scanner);
                break;
            } else if (input.equalsIgnoreCase("n")) {
                System.out.println("Skipping script execution.");
                break;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }

    private String createSQLFile(String sqlFilePath, String scriptContent) {
        try {
            String filename = "generated_script_" + LocalDateTime.now();
            String fullFilePath = sqlFilePath + "/" + filename + ".sql";

            FileUtils.createSimpleFile(sqlFilePath, filename, "sql", scriptContent);
            System.out.println("SQL file created or updated at: " + fullFilePath);
            return fullFilePath;
        } catch (IOException e) {
            System.out.println("Error creating SQL file: \n" + e.getMessage());
            return null;
        }
    }


    private void executeSQLScript(Connection connection, String scriptPath, Scanner scanner) {
        while (true) {
            try {
                String scriptContent = FileUtils.getFileContentSQL(scriptPath);
                SQLRunner.execute(connection, scriptContent);
                System.out.println("Script executed successfully. 🛠");
                break;
            } catch (Exception e) {
                System.out.println("Error executing script: \n" + e.getMessage());
                String retryInput = getNonEmptyInput(scanner, "Would you like to retry executing the script? (y/n)");
                if (!retryInput.equalsIgnoreCase("y")) {
                    System.out.println("Skipping script execution.");
                    break;
                }
            }
        }
    }


    private List<String> fetchEntityNames(Database database, Connection connection) {
        List<String> allTableNames = new ArrayList<>();
        try {
            allTableNames = database.getAllTableNames(connection);
            if (allTableNames.isEmpty()) {
                System.out.println("No tables found in the database.");
            } else {
                System.out.println("\nAvailable tables in the database:");
                for (int i = 0; i < allTableNames.size(); i++) {
                    System.out.println((i + 1) + ") " + allTableNames.get(i));
                }
                System.out.println("*  Select all tables");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching table names: " + e.getMessage());
        }
        return allTableNames;
    }

    private List<String> handleEntitySelection(Scanner scanner, List<String> allTableNames) {
        if (allTableNames.isEmpty()) {
            return new ArrayList<>();
        }

        while (true) {
            String input = getNonEmptyInput(scanner, "\nEnter the numbres of the tables you want to use, \nseparated by commas (e.g., 1,3,5) or enter * to select all");

            if (input.equals("*")) {
                System.out.println("All tables selected.");
                return new ArrayList<>();
            }

            List<String> selectedEntities = validateEntitySelection(input, allTableNames);

            if (!selectedEntities.isEmpty()) {
                System.out.println("Selected tables: " + String.join(", ", selectedEntities) + "\n");
                return selectedEntities;
            }

            System.out.println("\nInvalid selection. Please try again.");
        }
    }


    private List<String> validateEntitySelection(String input, List<String> allTableNames) {
        return getSelectedOptions(input, allTableNames);
    }


    private Connection configureCredentials(Scanner scanner, Database database) {
        configureCommonCredentials(scanner, database);
        configureDatabaseSpecificCredentials(scanner, database);

        System.out.println("Testing database connection...");
        Connection connection = testDatabaseConnection(database);

        if (connection != null) {
            System.out.println("\nConnection successful 🎉\n");
        } else {
            connection = handleConnectionFailure(scanner, database);
        }
        return connection;
    }

    private void configureCommonCredentials(Scanner scanner, Database database) {
        credentials.setHost(getDefaultInput(scanner, "Enter the database host", "localhost"));
        credentials.setPort(getDefaultInput(scanner, "Enter the database port", database.getPort()));
        credentials.setDatabaseName(getDefaultInput(scanner, "Enter the database name", "test"));
        credentials.setSchemaName(getDefaultInput(scanner, "Enter the schema name", "public"));
        credentials.setUser(getDefaultInput(scanner, "Enter the database user", "root"));
        credentials.setPwd(getPasswordInput(scanner, "Enter the database password", ""));
    }

    private void configureDatabaseSpecificCredentials(Scanner scanner, Database database) {
        switch (database.getName()) {
            case "MySQL":
                configureMySQLCredentials(scanner);
                break;
            case "Oracle":
                configureOracleCredentials(scanner, database);
                break;
            case "PostgreSQL":
                break;
            case "SQL Server":
                configureSQLServerCredentials(scanner);
                break;
            default:
                System.out.println("Database type not recognized. Please check the configuration.");
                break;
        }
    }

    private void configureMySQLCredentials(Scanner scanner) {
        credentials.setTrustCertificate(Boolean.parseBoolean(
                getDefaultInput(scanner, "Enable trust certificate for MySQL (true/false)", "true")
        ));
        credentials.setUseSSL(Boolean.parseBoolean(
                getDefaultInput(scanner, "Enable SSL for MySQL (true/false)", "true")
        ));
        credentials.setAllowPublicKeyRetrieval(Boolean.parseBoolean(
                getDefaultInput(scanner, "Allow public key retrieval for MySQL (true/false)", "true")
        ));
    }

    private void configureOracleCredentials(Scanner scanner, Database database) {
        credentials.setSID(getDefaultInput(scanner, "Enter the sid for Oracle",
                database.getSid() != null ? database.getSid() : "ORCLCDB"));
        credentials.setTrustCertificate(Boolean.parseBoolean(
                getDefaultInput(scanner, "Enable trust certificate for Oracle (true/false)", "true")
        ));
        credentials.setUseSSL(Boolean.parseBoolean(
                getDefaultInput(scanner, "Enable SSL for Oracle (true/false)", "true")
        ));
        credentials.setDriverType(getDefaultInput(scanner, "Enter the driver type for Oracle",
                database.getDriverType() != null ? database.getDriverType() : "thin"));
    }

    private void configureSQLServerCredentials(Scanner scanner) {
        credentials.setTrustCertificate(Boolean.parseBoolean(
                getDefaultInput(scanner, "Enable trust certificate for SQL Server (true/false)", "true")
        ));
        credentials.setUseSSL(Boolean.parseBoolean(
                getDefaultInput(scanner, "Enable SSL for SQL Server (true/false)", "true")
        ));
    }

    private Connection handleConnectionFailure(Scanner scanner, Database database) {
        String choice = getNonEmptyInput(scanner, "Connection failed. Would you like to modify the JDBC URL? (y/n)");
        if ("y".equals(choice)) {
            String previousJdbcUrl = database.getJdbcUrl(credentials);
            System.out.println("(Previous JDBC URL: \"" + previousJdbcUrl + "\")");

            String customUrl = getNonEmptyInput(scanner, "Enter the custom Connexion  String URL");
            Connection connection = testDatabaseConnection(database, customUrl);
            if (connection != null) {
                System.out.println("Connection successful with custom URL 🎉\n");
            } else {
                System.out.println("Connection failed with custom URL.");
            }
            return connection;
        }
        return null;
    }

    private Connection testDatabaseConnection(Database database) {
        try {
            Connection connection = database.getConnection(credentials);
            return connection.isValid(2) ? connection : null;
        } catch (Exception e) {
            String formattedMessage = String.join("\n", e.getMessage().split("\\."));
            System.out.println("Error testing connection: " + formattedMessage);
            return null;
        }
    }

    private Connection testDatabaseConnection(Database database, String customUrl) {
        try {
            Connection connection = database.getConnection(credentials, customUrl);
            return connection.isValid(2) ? connection : null;
        } catch (Exception e) {
            System.out.println("Error testing connection with custom URL: " + e.getMessage());
            return null;
        }
    }

    private String getDefaultInput(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt + " (default: " + defaultValue + "): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("Using default: " + defaultValue + "\n");
            return defaultValue;
        } else {
            System.out.println("Using: " + input + "\n");
            return input;
        }
    }

    private String getPasswordInput(Scanner scanner, String prompt, String defaultValue) {
        Console console = System.console();
        String password;

        if (console != null) {
            char[] passwordChars = console.readPassword(prompt + ": ");
            password = new String(passwordChars).trim();
        } else {
            System.out.print(prompt + " (default: " + (defaultValue.isEmpty() ? "<empty>" : "<hidden>") + "): ");
            password = scanner.nextLine().trim();
        }
        if (password.isEmpty()) {
            System.out.println("Using default password.\n");
            password = defaultValue;
        } else {
            System.out.println("Password entered.\n");
        }

        return password;
    }

    private String getNonEmptyInput(Scanner scanner, String prompt) {
        String input;
        do {
            System.out.print(prompt + ": ");
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Please enter a non-empty value.");
            }
        } while (input.isEmpty());

        System.out.println("Using: " + input + "\n");
        return input;
    }

    private String getValidPort(Scanner scanner) {
        while (true) {
            System.out.print("Enter the project port: ");
            String input = scanner.nextLine().trim();
            try {
                int port = Integer.parseInt(input);
                if (port > 0 && port <= 65535) {
                    System.out.println("Using: " + port + "\n");
                    return input;
                } else {
                    System.out.println("Error: Please enter a valid port number between 1 and 65535.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a valid number.\n");
            }
        }
    }

    private int getDatabaseId(Scanner scanner) {
        Map<Integer, String> databaseNames = ProjectGenerator.databases.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            if (entry.getValue() == null) {
                                throw new IllegalArgumentException("Entry with key " + entry.getKey() + " has a null value.");
                            }
                            return entry.getValue().getName();
                        }
                ));

        return getSelectionId(scanner, databaseNames, "Database");
    }

    private int getLanguageSelection(Scanner scanner) {
        Map<Integer, String> languageNames = ProjectGenerator.languages.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getName()
                ));

        if (languageNames.isEmpty()) {
            System.out.println("No valid languages found.");
            return -1;
        }

        return getSelectionId(scanner, languageNames, "Language");
    }

    private String getCoreFrameworkSelection(Scanner scanner, Language language) {
        Map<String, String> baseFrameworks = ProjectGenerator.frameworks.values().stream()
                .filter(framework -> framework.getLanguageId() == language.getId())
                .collect(Collectors.toMap(
                        Framework::getCoreFramework,
                        Framework::getCoreFramework,
                        (existing, replacement) -> existing
                ));

        return getSelectionStr(scanner, baseFrameworks, "Framework");
    }

    private String getSelectionStr(Scanner scanner, Map<String, String> options, String optionType) {
        List<String> keys = new ArrayList<>(options.keySet());
        Collections.sort(keys);

        while (true) {
            System.out.println(optionType + " options:");
            for (int i = 0; i < keys.size(); i++) {
                System.out.println((i + 1) + ") " + options.get(keys.get(i)));
            }

            System.out.print("Enter the " + optionType.toLowerCase() + " index: ");
            try {
                int index = Integer.parseInt(scanner.nextLine()) - 1;
                if (index >= 0 && index < keys.size()) {
                    String selectedOption = keys.get(index);
                    System.out.println("Using: " + (index + 1) + "- " + options.get(selectedOption) + "\n");
                    return selectedOption;
                } else {
                    System.out.println("Error: Invalid index. Please select a valid option." + "\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a valid number.\n");
            }
        }
    }


    private int getFrameworkSelection(Scanner scanner, Language language, String baseFramework) {
        Map<Integer, Framework> frameworks = ProjectGenerator.frameworks;

        Map<Integer, String> validFrameworkNames = frameworks.entrySet().stream()
                .filter(entry -> entry.getValue().getLanguageId() == language.getId() &&
                        entry.getValue().getCoreFramework().equalsIgnoreCase(baseFramework))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getName()
                ));

        if (validFrameworkNames.isEmpty()) {
            System.out.println("No valid frameworks found for the selected base framework.");
            return -1;
        }
        return getSelectionId(scanner, validFrameworkNames, "Project type");
    }


    private int getProjectId(Scanner scanner, String baseframework) {
        Map<Integer, Project> projects = ProjectGenerator.projects;
        Map<Integer, String> languageNames = projects.entrySet().stream()
                .filter(entry -> entry.getValue().getCoreFrameworks().contains(baseframework))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getName()
                ));

        if (languageNames.isEmpty()) {
            System.out.println("No valid projects found for the selected framework.\n");
            return -1;
        }

        return getSelectionId(scanner, languageNames, "Build tool");
    }

    private int getSelectionId(Scanner scanner, Map<Integer, String> options, String optionType) {
        List<Integer> keys = new ArrayList<>(options.keySet());
        Collections.sort(keys);

        while (true) {
            System.out.println(optionType + " options :");
            for (int i = 0; i < keys.size(); i++) {
                System.out.println((i + 1) + ") " + options.get(keys.get(i)));
            }

            System.out.print("Enter the " + optionType.toLowerCase() + " index: ");
            try {
                int index = Integer.parseInt(scanner.nextLine()) - 1;
                if (index >= 0 && index < keys.size()) {
                    int selectedId = keys.get(index);
                    System.out.println("Using: " + (index + 1) + "- " + options.get(selectedId) + "\n");
                    return selectedId;
                } else {
                    System.out.println("Error: Invalid index (" + options + ").Please select a valid option." + "\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a valid number.\n");
            }
        }
    }

    private HashMap<String, Object> configureFramework(Scanner scanner, Framework framework) {
        return configureOptions(scanner, framework.getConfigurations());
    }

    private HashMap<String, Object> configureFrameworkWithEureka(Scanner scanner, Framework framework) {
        HashMap<String, Object> config = configureOptions(scanner, framework.getEurekaClientConfigurations());
        framework.setUseCloud(true);
        framework.setUseEurekaServer(true);
        return config;
    }

    private HashMap<String, Object> configureLangage(Scanner scanner, Language language) {
        return configureOptions(scanner, language.getConfigurations());
    }

    private HashMap<String, Object> configureOptions(Scanner scanner, List<ConfigurationMetadata> configurations) {
        HashMap<String, Object> configMap = new HashMap<>();
        for (ConfigurationMetadata config : configurations) {
            String option = getUserInput(scanner, config);
            configMap.put(config.getVariableName(), option);
        }
        return configMap;
    }

    public String getUserInput(Scanner scanner, ConfigurationMetadata config) {
        System.out.println("Configuration: " + config.getName());

        List<String> options = config.getOptions();
        if (options == null || options.isEmpty()) {
            System.out.println("No options available for this configuration.");
            return config.getDefaultOption();
        }

        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ") " + options.get(i));
        }
        System.out.println((options.size() + 1) + ") Other");

        while (true) {
            try {
                System.out.print("Select an option ID (default: " + config.getDefaultOption() + "): ");
                String input = scanner.nextLine();

                if (input.isEmpty()) {
                    System.out.println("Using default option: " + config.getDefaultOption() + "\n");
                    return config.getDefaultOption();
                }

                int optionId = Integer.parseInt(input) - 1;

                if (optionId >= 0 && optionId < options.size()) {
                    System.out.println("Using option: " + optionId + " - " + options.get(optionId) + "\n");
                    return options.get(optionId);
                } else if (optionId == options.size()) {
                    System.out.print("Enter your custom option: ");
                    String customOption = scanner.nextLine();
                    System.out.println("Using custom option: " + customOption + "\n");
                    return customOption;
                } else {
                    System.out.println("Error: Invalid option. Please select a valid option.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a valid number.\n");
            }
        }
    }
}
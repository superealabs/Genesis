package org.labs.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.adapter.DatabaseDeserializer;
import org.labs.genesis.merge.FileMergeInput;
import org.labs.genesis.merge.MergeOutcome;
import org.labs.genesis.merge.MergeTool;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.Scanner;
import java.util.stream.Stream;

public class FileUtils {

    public static String getFileContent(String resourcePath) throws IOException {
        StringBuilder content = new StringBuilder();

        // Utilisation du class loader pour charger la ressource
        try (InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
            try (Scanner reader = new Scanner(inputStream)) {
                while (reader.hasNextLine()) {
                    content.append(reader.nextLine()).append("\n");
                }
            }
        }

        return content.toString();
    }

    public static String getFileContentSQL(String filePath) throws FileNotFoundException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        StringBuilder content = new StringBuilder();
        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith("--")) {
                    content.append(line).append(" ");
                }
            }
        }

        String finalContent = content.toString().trim();
        if (finalContent.isEmpty()) {
            throw new FileNotFoundException("The file is empty or contains only comments: " + filePath);
        }

        return finalContent.replaceAll("\\s+", " ").replaceAll(";\\s*", ";");
    }


    public static void createFileStructure(String filePath) {
        filePath = filePath.replace("\\", "/");
        String[] folders = filePath.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String folder : folders) {
            currentPath.append(folder).append("/");
            File file = new File(currentPath.toString());

            if (!file.exists()) {
                file.mkdir();
            }
        }
    }

    public static File createFile(String filePath, String fileName, String fileExtension, String fileContent) throws IOException {
        // creation de la structure du projet
        createFileStructure(filePath);

        // creation du fichier et son contenu
        return createSimpleFile(filePath, fileName, fileExtension, fileContent);
    }

    public static File createSimpleFile(String filePath, String fileName, String fileExtension, String fileContent) throws IOException {
        File file = new File(filePath + "/" + fileName + "." + fileExtension);
        if (file.exists()) {
            file.delete();
        }
        Files.write(file.toPath(), fileContent.getBytes());
        return  file;
    }

    public static void copyFile(String sourceFilePath, String destinationFilePath, String fileName) throws IOException {
        Path destinationPath = Paths.get(destinationFilePath, fileName);

        try (InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(sourceFilePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Source file not found in JAR: " + sourceFilePath);
            }

            Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (NoSuchFileException e) {
            System.out.println("Warning : " + e.getMessage() + "\nGenerated the missing file ...\n");
            createFileStructure(destinationPath.toString());

            try (InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(sourceFilePath)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Source file not found after retry: " + sourceFilePath);
                }

                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("Generated : " + destinationPath + "\n");
        } catch (IOException e) {
            throw new IOException("Error when copying file: " + e.getMessage(), e);
        }
    }


    public static void copyDirectory(String sourceDir, String destDir) throws IOException {
        URI resourceUri = getResourceUri(sourceDir);
        Path destPath = Paths.get(destDir);

        if (resourceUri.getScheme().equals("jar")) {
            // Gérer les fichiers dans un JAR
            try (FileSystem fileSystem = FileSystems.newFileSystem(resourceUri, Collections.emptyMap())) {
                Path jarPath = fileSystem.getPath(sourceDir);
                copyDirectoryFromJar(jarPath, destPath);
            }
        } else {
            // Gérer les fichiers dans le système de fichiers classique
            Path srcPath = Paths.get(resourceUri);
            copyDirectoryFromPath(srcPath, destPath);
        }
    }

    private static void copyDirectoryFromJar(Path sourceJarPath, Path destinationPath) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceJarPath)) { // Utilisation du try-with-resources
            paths.forEach(path -> {
                Path resolvedDestPath = destinationPath.resolve(sourceJarPath.relativize(path).toString());
                try {
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(resolvedDestPath);
                    } else {
                        Files.copy(path, resolvedDestPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("Error copying file from JAR: " + path, e);
                }
            });
        }
    }


    private static @NotNull URI getResourceUri(String sourceDir) throws IOException {
        // Obtenir le class loader actuel
        ClassLoader classLoader = FileUtils.class.getClassLoader();

        // Obtenir l'URL du répertoire source dans les ressources
        URL resourceUrl = classLoader.getResource(sourceDir);
        if (resourceUrl == null) {
            throw new FileNotFoundException("Source directory not found : " + sourceDir);
        }

        // Convertir l'URL en URI
        URI resourceUri;
        try {
            resourceUri = resourceUrl.toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Error during conversion from URL to URI : " + resourceUrl, e);
        }
        return resourceUri;
    }

    private static void copyDirectoryFromPath(Path srcPath, Path destPath) throws IOException {
        if (!Files.exists(destPath)) {
            Files.createDirectories(destPath);
        }

        try (Stream<Path> paths = Files.walk(srcPath)) {
            paths.forEach(path -> {
                Path destination = destPath.resolve(srcPath.relativize(path));
                try {
                    if (Files.isDirectory(path)) {
                        if (!Files.exists(destination)) {
                            Files.createDirectories(destination);
                        }
                    } else {
                        try (InputStream inputStream = Files.newInputStream(path)) {
                            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Erreur lors de la copie du fichier : " + path + " -> " + e.getMessage());
                }
            });
        }
    }

    private static void applyGeneratedMergeOutcome(MergeOutcome mergeOutcome) throws IOException {
        FileMergeInput mergeInput = mergeOutcome.input;
        if (mergeOutcome.hasConflict){
            Files.write(mergeOutcome.conflictFile.toPath(), mergeOutcome.mergedContent.getBytes(StandardCharsets.UTF_8));
        }else {
            // merge content into current file
            Files.write(mergeInput.currentFile.toPath(), mergeOutcome.mergedContent.getBytes(StandardCharsets.UTF_8));
        }
        // generated file into baseFile
        Files.write(mergeInput.baseFile.toPath(),   Files.readAllBytes(mergeInput.newFile.toPath()));
        deleteFile(mergeInput.newFile.getPath());
    }
    public static void createOrMergeFile(String destinationFolder, String filePath, String fileName, String fileExtension, String fileContent) throws IOException {
        File generatedFile = createGenerationTempFile(filePath, fileName, fileExtension, fileContent);
        File existingFile = new File(filePath + "/" + fileName + "." + fileExtension);
        File baseDirectory = generateBaseDirectoryIfAbsent(destinationFolder);
        File baseFile = new File(baseDirectory.getPath() + "/" + fileName + "." + fileExtension);

        FileMergeInput mergeInput = new FileMergeInput(baseFile, existingFile, generatedFile);
        MergeOutcome mergeOutcome = MergeTool.merge(mergeInput,true);
        applyGeneratedMergeOutcome(mergeOutcome);
    }


    public static File generateBaseDirectoryIfAbsent(String destinationFolder){
        if (destinationFolder != null && destinationFolder.contains("Webapp")){
            destinationFolder = destinationFolder.substring(0, destinationFolder.lastIndexOf('/'));
        }
        File baseDirectory = new File(destinationFolder+"/"+ Constantes.GENESIS_BASE_FILES_HIDDEN_DIRECTORY_NAME);
        if (!baseDirectory.exists()){
            createFileStructure(baseDirectory.getPath());
        }
        return baseDirectory;
    }

    public static File createGenerationTempFile(String filePath, String fileName, String fileExtension, String fileContent) throws IOException {
         File tempFile = createFile(filePath , fileName, fileExtension+"."+Constantes.GENESIS_GENERATION_TEMP_FILE_EXTENSION, fileContent);
         return  tempFile;
    }

    public static void createDirectory(String filePath) {
        String filename = "";
        String currentChar;

        File file;
        for (int i = 0; i < filePath.length(); ++i) {
            currentChar = String.valueOf(filePath.charAt(i));
            if (currentChar.equals("/")) {
                file = new File(filename);
                file.mkdir();
            }

            filename = filename + currentChar;
        }

        file = new File(filename);
        file.mkdir();
    }

    public static <T> T fromJson(Class<T> clazz, String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Database.class, new DatabaseDeserializer());
        objectMapper.registerModule(module);

        // Vérifier si c'est un chemin absolu (commence par / ou contient :)
        if (path.startsWith("/") || path.contains(":")) {
            // Chemin absolu sur le système de fichiers
            File file = new File(path);
            if (!file.exists()) {
                throw new FileNotFoundException("File not found : " + path);
            }
            return objectMapper.readValue(file, clazz);
        } else {
            // Chemin relatif depuis le classpath
            InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(path);
            if (inputStream == null) {
                throw new FileNotFoundException("File not found in classpath : " + path);
            }
            return objectMapper.readValue(inputStream, clazz);
        }
    }


    public static <T> T fromYamlFile(Class<T> clazz, String yamlFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        return objectMapper.readValue(new File(yamlFilePath), clazz);
    }

    public static <T> T fromYaml(Class<T> clazz, String resourcePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        // Charger le fichier depuis le classpath
        InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found : " + resourcePath);
        }

        return objectMapper.readValue(inputStream, clazz);
    }

    public static <T> void toJson(T object, String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        File file = new File(filePath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, object);
    }

    public static <T> void toYaml(T object, String yamlFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        File file = new File(yamlFilePath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        objectMapper.writeValue(file, object);
    }

    public static <T> String toJsonString(T object) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public static <T> String toYamlString(T object) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        return objectMapper.writeValueAsString(object);
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }
    public static void deleteFile(String filePath, String fileName, String fileExtension) {
        deleteFile(filePath + "/" + fileName + "." + fileExtension);
    }
}

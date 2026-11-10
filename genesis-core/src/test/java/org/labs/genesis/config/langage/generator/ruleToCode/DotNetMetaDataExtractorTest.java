package org.labs.genesis.config.langage.generator.ruleToCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.labs.genesis.config.langage.generator.ruleToCode.DataExtractor.DotNetMetaDataExtractor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class DotNetMetaDataExtractorTest {

    @Test
    public void testExtractMetaDataFromDotNetModel(@TempDir Path tempDir) throws Exception {
        // Create standard Genesis .NET project directory structure: ProjectName/Models
        Path projectPath = tempDir.resolve("ShopApp");
        Path modelsDir = projectPath.resolve("ShopApp").resolve("Models");
        Files.createDirectories(modelsDir);

        // Model 1: Product with virtual FK and nullable fields
        String productCs = """
                namespace ShopApp.Models;

                using System.ComponentModel.DataAnnotations;
                using System.ComponentModel.DataAnnotations.Schema;

                [Table("products")]
                public class Product : BaseEntity
                {
                    [Key]
                    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
                    [Column("id")]
                    public int Id { get; set; }

                    [Column("name")]
                    public string? Name { get; set; }

                    [Column("price")]
                    public decimal Price { get; set; }

                    [Column("category_id")]
                    public int? CategoryId { get; set; }

                    [ForeignKey(nameof(CategoryId))]
                    public virtual Category? Category { get; set; }
                }
                """;

        // Model 2: Category
        String categoryCs = """
                namespace ShopApp.Models;

                [Table("categories")]
                public class Category
                {
                    public int Id { get; set; }
                    public string Title { get; set; }
                }
                """;

        Files.writeString(modelsDir.resolve("Product.cs"), productCs);
        Files.writeString(modelsDir.resolve("Category.cs"), categoryCs);

        DotNetMetaDataExtractor extractor = new DotNetMetaDataExtractor(2);
        String yaml = extractor.extractMetaData(projectPath, "", "ShopApp");

        assertNotNull(yaml);
        assertTrue(yaml.contains("Category"), "YAML should contain Category entity");
        assertTrue(yaml.contains("Product"), "YAML should contain Product entity");
        assertTrue(yaml.contains("name: Id"), "YAML should contain Id field");
        assertTrue(yaml.contains("name: Name"), "YAML should contain Name field");
        assertTrue(yaml.contains("name: Category"), "YAML should contain Category field");
        // Ensure 'virtual' is not mistakenly extracted as the type
        assertFalse(yaml.contains("type: virtual"), "Type should not be 'virtual' for virtual navigation properties");
        assertTrue(yaml.contains("type: Category?"), "Category navigation property type should be Category?");
    }

    @Test
    public void testExtractMetaDataDirectModelsDirectory(@TempDir Path tempDir) throws Exception {
        // User selected the inner project or folder directly containing 'Models'
        Path modelsDir = tempDir.resolve("Models");
        Files.createDirectories(modelsDir);

        String employeeCs = """
                namespace Company.Models;

                public class Employee
                {
                    public int Id { get; set; }
                    public string FullName { get; set; }
                }
                """;
        Files.writeString(modelsDir.resolve("Employee.cs"), employeeCs);

        DotNetMetaDataExtractor extractor = new DotNetMetaDataExtractor(2);
        String yaml = extractor.extractMetaData(tempDir, "", "Company");

        assertNotNull(yaml);
        assertTrue(yaml.contains("Employee"));
        assertTrue(yaml.contains("FullName"));
    }

    @Test
    public void testExtractMetaDataEmptyDirectoryReturnsEmptyYaml(@TempDir Path tempDir) throws Exception {
        Path modelsDir = tempDir.resolve("Models");
        Files.createDirectories(modelsDir);

        DotNetMetaDataExtractor extractor = new DotNetMetaDataExtractor(2);
        String yaml = extractor.extractMetaData(tempDir, "", "TestProject");

        assertNotNull(yaml);
        assertTrue(yaml.contains("entities"));
    }

    @Test
    public void testYamlDataPathModelCandidates(@TempDir Path tempDir) throws Exception {
        YamlData yamlData = new YamlData();

        // 1. Check fallback when Models is inside projectName
        Path projDir = tempDir.resolve("Demo");
        Path innerModels = projDir.resolve("Demo").resolve("Models");
        Files.createDirectories(innerModels);

        File resolved = yamlData.pathModel(projDir.toString(), "", "Demo", 2);
        assertNotNull(resolved);
        assertEquals(innerModels.toFile().getAbsolutePath(), resolved.getAbsolutePath());

        // 2. Check exception when directory doesn't exist
        assertThrows(RuntimeException.class, () -> {
            yamlData.pathModel(tempDir.resolve("NonExistent").toString(), "", "Demo", 2);
        });
    }
}

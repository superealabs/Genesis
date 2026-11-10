package org.labs.genesis.config.langage.generator.ruleToCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodeInjectorTest {

    @Test
    public void testSplitCodeDotNetWithMarkdownStripsLanguageTag() {
        CodeInjector injector = new CodeInjector();
        String llmOutput = """
                @Service: OrderService
                using System;
                using System.Threading.Tasks;
                ```csharp
                public async Task<bool> ValidateOrderAsync(int id)
                {
                    return true;
                }
                ```
                @Repository: OrderRepository
                using System.Data;
                Task<bool> ExistsAsync(int id);
                """;

        List<CodeBlock> blocks = injector.splitCode(llmOutput, 2);
        assertNotNull(blocks);
        assertEquals(5, blocks.size(), "Should have 2 usings + 1 code for service, and 1 using + 1 code for repository");

        CodeBlock serviceCode = blocks.stream()
                .filter(b -> "none".equals(b.nameImport) && "OrderService".equals(b.className))
                .findFirst()
                .orElse(null);

        assertNotNull(serviceCode);
        assertFalse(serviceCode.code.startsWith("csharp"), "Code should not start with markdown language tag 'csharp'");
        assertTrue(serviceCode.code.contains("ValidateOrderAsync"));

        CodeBlock repoCode = blocks.stream()
                .filter(b -> "none".equals(b.nameImport) && "OrderRepository".equals(b.className))
                .findFirst()
                .orElse(null);

        assertNotNull(repoCode);
        assertTrue(repoCode.code.contains("ExistsAsync"));
    }

    @Test
    public void testSplitCodeSpringBoot() {
        CodeInjector injector = new CodeInjector();
        String llmOutput = """
                @Service: UserService
                import java.util.List;
                import java.util.Optional;
                ```java
                public Optional<User> findByEmail(String email) {
                    return Optional.empty();
                }
                ```
                """;

        List<CodeBlock> blocks = injector.splitCode(llmOutput, 1);
        assertNotNull(blocks);
        assertEquals(3, blocks.size());

        CodeBlock codeBlock = blocks.stream()
                .filter(b -> "none".equals(b.nameImport))
                .findFirst()
                .orElse(null);

        assertNotNull(codeBlock);
        assertFalse(codeBlock.code.startsWith("java"), "Code should not start with markdown language tag 'java'");
        assertTrue(codeBlock.code.contains("findByEmail"));
    }

    @Test
    public void testInjectAndRemoveInFileScopedCSharpClass(@TempDir Path tempDir) throws Exception {
        CodeInjector injector = new CodeInjector();
        Path csFile = tempDir.resolve("ProductService.cs");

        String initialContent = """
                using System;
                using ShopApp.Models;

                namespace ShopApp.Services.Implementation;

                public class ProductService : IProductService
                {
                    public void ExistingMethod()
                    {
                    }
                }
                """;
        Files.writeString(csFile, initialContent);

        // Inject using
        injector.injectImportInClass(csFile, "using System.Threading.Tasks;");
        String afterImport = Files.readString(csFile);
        assertTrue(afterImport.contains("using System.Threading.Tasks;"));

        // Inject method
        String method = "public async Task<bool> CheckStockAsync() { return true; }";
        injector.injectCodeInClass(csFile, method);
        String afterCode = Files.readString(csFile);
        assertTrue(afterCode.contains("CheckStockAsync"));
        assertTrue(afterCode.trim().endsWith("}"));

        // Delete method
        injector.deleteCodeInClass(csFile, method);
        String afterDelete = Files.readString(csFile);
        assertFalse(afterDelete.contains("CheckStockAsync"));

        // Delete using
        injector.deleteImportInClass(csFile, "using System.Threading.Tasks;");
        String afterDeleteImport = Files.readString(csFile);
        assertFalse(afterDeleteImport.contains("using System.Threading.Tasks;"));
    }

    @Test
    public void testInjectInBlockScopedCSharpClass(@TempDir Path tempDir) throws Exception {
        CodeInjector injector = new CodeInjector();
        Path csFile = tempDir.resolve("OrderService.cs");

        String initialContent = """
                using System;

                namespace ShopApp.Services.Implementation
                {
                    public class OrderService : IOrderService
                    {
                        public void InitialWork()
                        {
                        }
                    }
                }
                """;
        Files.writeString(csFile, initialContent);

        String method = "public void ExtraWork() { }";
        injector.injectCodeInClass(csFile, method);

        String updated = Files.readString(csFile);
        assertTrue(updated.contains("ExtraWork"));

        // Verify that ExtraWork is before the class closing brace and inside the namespace
        int methodPos = updated.indexOf("ExtraWork");
        int lastBrace = updated.lastIndexOf("}");
        int secondLastBrace = updated.lastIndexOf("}", lastBrace - 1);

        assertTrue(methodPos < secondLastBrace,
                "Injected method must be placed inside the class body, before the class closing brace");
    }

    @Test
    public void testInjectBlocksFullDirectoryResolutionDotNet(@TempDir Path tempDir) throws Exception {
        CodeInjector injector = new CodeInjector();

        // Create standard Genesis .NET folder structure:
        // tempDir / ShopApp / ShopApp / Services / Implementation / CartService.cs
        Path targetDir = tempDir.resolve("ShopApp")
                .resolve("ShopApp")
                .resolve("Services")
                .resolve("Implementation");
        Files.createDirectories(targetDir);

        Path serviceFile = targetDir.resolve("CartService.cs");
        String initialContent = """
                namespace ShopApp.Services.Implementation;

                public class CartService : ICartService
                {
                }
                """;
        Files.writeString(serviceFile, initialContent);

        String codeToInject = """
                @Service: CartService
                using System.Collections.Generic;
                public decimal ComputeTotal()
                {
                    return 0;
                }
                """;

        List<CodeBlock> blocks = injector.splitCode(codeToInject, 2);
        // basePath is tempDir, projectName is ShopApp
        injector.injectBlocks(blocks, tempDir.toString(), 2, "ShopApp");

        String updatedContent = Files.readString(serviceFile);
        assertTrue(updatedContent.contains("using System.Collections.Generic;"));
        assertTrue(updatedContent.contains("ComputeTotal"));
    }
}

package org.labs.genesis.api.service;

import org.junit.jupiter.api.Test;
import org.labs.genesis.frontend.generator.FrontendFramework;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FrontendFrameworkIsolationServiceTest {

    private final FrontendFrameworkIsolationService service =
            new FrontendFrameworkIsolationService();

    @Test
    void shouldPreserveLayoutAndBranding() {
        FrontendFramework original = new FrontendFramework();

        original.setName("React");
        original.setCoreFramework("React");
        original.setTemplate("react");

        original.getFrontendLayout()
                .setPrimaryColor("#2563EB");
        original.getFrontendLayout()
                .setSecondaryColor("#10B981");
        original.getFrontendLayout()
                .setNavbar("Sidebar");

        original.getProjectBranding()
                .setLogoLink("https://example.com/logo.png");
        original.getProjectBranding()
                .setFaviconLink("https://example.com/favicon.ico");

        FrontendFramework copy = service.copy(original);

        assertNotSame(original, copy);

        assertNotSame(
                original.getFrontendLayout(),
                copy.getFrontendLayout()
        );

        assertNotSame(
                original.getProjectBranding(),
                copy.getProjectBranding()
        );

        assertEquals(
                "#2563EB",
                copy.getFrontendLayout().getPrimaryColor()
        );

        assertEquals(
                "#10B981",
                copy.getFrontendLayout().getSecondaryColor()
        );

        assertEquals(
                "Sidebar",
                copy.getFrontendLayout().getNavbar()
        );

        assertEquals(
                "https://example.com/logo.png",
                copy.getProjectBranding().getLogoLink()
        );

        assertEquals(
                "https://example.com/favicon.ico",
                copy.getProjectBranding().getFaviconLink()
        );

        copy.getFrontendLayout()
                .setPrimaryColor("#FF0000");

        assertEquals(
                "#2563EB",
                original.getFrontendLayout().getPrimaryColor()
        );
    }


    @Test
    void shouldPreserveLocalBrandingFiles() throws Exception {
        Path logo = Files.createTempFile("genesis-logo-", ".png");
        Path favicon = Files.createTempFile("genesis-favicon-", ".ico");

        try {
            FrontendFramework original = new FrontendFramework();

            original.getProjectBranding()
                    .setLogoFile(logo.toFile());

            original.getProjectBranding()
                    .setFaviconFile(favicon.toFile());

            FrontendFramework copied = service.copy(original);

            assertNotSame(original, copied);

            assertEquals(
                    logo.toFile(),
                    copied.getProjectBranding().getLogoFile()
            );

            assertEquals(
                    favicon.toFile(),
                    copied.getProjectBranding().getFaviconFile()
            );

            assertEquals(
                    "png",
                    copied.getProjectBranding().getLogoExtension()
            );

        } finally {
            Files.deleteIfExists(logo);
            Files.deleteIfExists(favicon);
        }
    }
}

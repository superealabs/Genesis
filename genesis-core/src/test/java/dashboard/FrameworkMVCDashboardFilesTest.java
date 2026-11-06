package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.genesis.config.langage.FrameworkMVC;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FrameworkMVCDashboardFilesTest {

    @Test
    void dashboardFilesAreEmptyByDefault() {

        FrameworkMVC framework =
                new FrameworkMVC();

        assertNotNull(
                framework.getDashboardFiles()
        );

        assertTrue(
                framework.getDashboardFiles()
                        .isEmpty()
        );
    }

    @Test
    void dashboardFilesCanBeConfigured() {

        FrameworkMVC framework =
                new FrameworkMVC();

        FilesEdit service =
                new FilesEdit();

        service.setFileType(
                "DashboardService"
        );

        service.setFileName(
                "DashboardService"
        );

        service.setExtension(
                "java"
        );

        FilesEdit controller =
                new FilesEdit();

        controller.setFileType(
                "DashboardController"
        );

        controller.setFileName(
                "DashboardController"
        );

        controller.setExtension(
                "java"
        );

        framework.setDashboardFiles(
                List.of(
                        service,
                        controller
                )
        );

        assertEquals(
                2,
                framework.getDashboardFiles()
                        .size()
        );

        assertEquals(
                "DashboardService",
                framework.getDashboardFiles()
                        .get(0)
                        .getFileType()
        );

        assertEquals(
                "DashboardController",
                framework.getDashboardFiles()
                        .get(1)
                        .getFileType()
        );
    }
}
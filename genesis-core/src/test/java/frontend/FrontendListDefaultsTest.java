package frontend;

import org.junit.jupiter.api.Test;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.genesis.frontend.generator.model.FrontendLayout;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendListDefaultsTest {
    @Test
    void usesCssColorsInTheDefaultLayout() {
        var layout = new FrontendLayout();
        var metadata = FrameworkFrontendMetadataProvider.getLayoutHashMap(layout);

        assertThat(layout.getPrimaryColor()).matches("#[0-9a-fA-F]{6}");
        assertThat(layout.getSecondaryColor()).matches("#[0-9a-fA-F]{6}");
        assertThat(metadata.get("secondaryColor")).isEqualTo(layout.getSecondaryColor());
    }

    @Test
    void preservesTheSqlTableNameForColumnVisibilityConfiguration() {
        var table = new TableMetadata();
        table.setTableName("audit_customer");
        table.setClassName("AuditCustomer");

        var metadata = FrameworkFrontendMetadataProvider.getHashMapComponentSavePath(
                "/tmp/generated", "AuditVue", table);

        assertThat(metadata).containsEntry("tableName", "audit_customer")
                .containsEntry("EntityName", "AuditCustomer");
    }
}

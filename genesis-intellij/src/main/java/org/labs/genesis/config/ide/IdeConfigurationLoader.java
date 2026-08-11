package org.labs.genesis.config.ide;

import org.labs.utils.FileUtils;

import java.io.IOException;
import java.util.Arrays;

public class IdeConfigurationLoader {

    public static final String IDE_CONFIGURATIONS_YAML = "data_genesis/yaml/ide_configurations.yaml";

    private static IdeConfiguration cachedConfig;

    public static synchronized IdeConfiguration getIdeConfiguration() {
        if (cachedConfig != null) {
            return cachedConfig;
        }
        try {
            IdeConfiguration[] configs = FileUtils.fromYaml(IdeConfiguration[].class, IDE_CONFIGURATIONS_YAML);
            if (configs != null && configs.length > 0) {
                cachedConfig = configs[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cachedConfig;
    }

    public static DriverConfig findDriverConfig(IdeConfiguration ideConfig, String sgbdName) {
        if (ideConfig == null || ideConfig.getDataSourceTemplate() == null || ideConfig.getDataSourceTemplate().getDrivers() == null) {
            return null;
        }
        String targetSgbd = sgbdName != null ? sgbdName.toLowerCase() : "";
        for (DriverConfig driver : ideConfig.getDataSourceTemplate().getDrivers()) {
            if (targetSgbd.contains(driver.getSgbd().toLowerCase())) {
                return driver;
            }
        }
        // Fallback to postgresql if available
        return ideConfig.getDataSourceTemplate().getDrivers().get(0);
    }

    public static ImlTemplate findImlTemplate(IdeConfiguration ideConfig, String type) {
        if (ideConfig == null || ideConfig.getImlTemplates() == null) {
            return null;
        }
        for (ImlTemplate iml : ideConfig.getImlTemplates()) {
            if (iml.getType().equalsIgnoreCase(type)) {
                return iml;
            }
        }
        return null;
    }
}

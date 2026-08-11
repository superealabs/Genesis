package org.labs.genesis.config.ide;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriverConfig {
    private String sgbd;
    private String driverRef;
    private String jdbcDriver;
    private String urlPattern;
}

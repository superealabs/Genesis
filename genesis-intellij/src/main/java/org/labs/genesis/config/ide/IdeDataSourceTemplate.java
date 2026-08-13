package org.labs.genesis.config.ide;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdeDataSourceTemplate {
    private String content;
    private List<DriverConfig> drivers;
}

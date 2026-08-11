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
public class IdeConfiguration {
    private int id;
    private String ideName;
    private IdeDataSourceTemplate dataSourceTemplate;
    private IdeMiscTemplate miscTemplate;
    private IdeModulesTemplate modulesTemplate;
    private List<ImlTemplate> imlTemplates;
}

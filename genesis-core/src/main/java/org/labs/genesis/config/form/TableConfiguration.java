package org.labs.genesis.config.form;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class TableConfiguration {

    private String name;
    private List<FieldConfiguration> fields = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static  class FieldConfiguration {
        private String column;
        private String component;
        private Map<String, String> columns = new LinkedHashMap<>();
        private Map<String, Object> configuration = new LinkedHashMap<>();
    }
}

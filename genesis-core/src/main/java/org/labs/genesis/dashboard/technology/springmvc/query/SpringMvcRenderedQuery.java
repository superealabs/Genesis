package org.labs.genesis.dashboard.technology.springmvc.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class SpringMvcRenderedQuery {
    private final String sql;
    private final List<Object> parameters;
    private final Integer limit;
    private final List<String> aliases;
    public SpringMvcRenderedQuery(String sql, Integer limit) {
        this(sql, new ArrayList<>(), limit, new ArrayList<>());
    }
}
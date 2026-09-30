package org.labs.genesis.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFilter {
    private String columnName;
    private String operator;
    private Object value;
    private String relationToPrevious = "AND";
}

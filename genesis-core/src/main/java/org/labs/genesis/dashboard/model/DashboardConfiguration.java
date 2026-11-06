package org.labs.genesis.dashboard.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardConfiguration {
    private boolean enabled = true;
    private List<DashboardPage> pages = new ArrayList<>();
}
package org.labs.genesis.dashboard.query;

public interface DashboardQueryRenderer<T> {
    T render(DashboardQueryPlan plan);
}
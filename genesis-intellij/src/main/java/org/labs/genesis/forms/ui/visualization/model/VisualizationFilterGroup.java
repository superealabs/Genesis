package org.labs.genesis.forms.ui.visualization.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VisualizationFilterGroup implements VisualizationFilterNode {

    private final List<VisualizationFilterNode> children = new ArrayList<>();
    private FilterLogicalOperator relation = FilterLogicalOperator.AND;

    public void addChild(VisualizationFilterNode child) {
        children.add(child);
    }
}

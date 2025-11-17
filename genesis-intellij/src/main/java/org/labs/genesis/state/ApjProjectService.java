package org.labs.genesis.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@State(
    name = "GenesisApjProjectState",
    storages = @Storage("genesisApj.xml")
)
public class ApjProjectService implements PersistentStateComponent<ApjProjectState> {
    private ApjProjectState state = new ApjProjectState();

    public static ApjProjectService getInstance(Project project) {
        return project.getService(ApjProjectService.class);
    }

    @Override
    public ApjProjectState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull ApjProjectState state) {
        this.state = state;
    }
}

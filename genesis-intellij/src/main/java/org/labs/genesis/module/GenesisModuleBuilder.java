package org.labs.genesis.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labs.genesis.config.ProjectGenerationContext;

public class GenesisModuleBuilder extends ModuleBuilder {
    private final static GenesisModuleType moduleType = new GenesisModuleType();
    public static ProjectGenerationContext projectGenerationContext = new ProjectGenerationContext();
    public WizardContext wizardContext;

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel modifiableRootModel) {
        if (wizardContext != null) {
            String projectPath = wizardContext.getProjectFileDirectory();
            VirtualFile projectRoot = LocalFileSystem.getInstance()
                    .refreshAndFindFileByPath(projectPath);
            if (projectRoot != null) {
                modifiableRootModel.addContentEntry(projectRoot);
            }
        }
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        /*SpecificConfigurationWizardStep specificConfigurationWizardStep = new SpecificConfigurationWizardStep(projectGenerationContext);
        return new InitializationWizardStep(projectGenerationContext, specificConfigurationWizardStep);*/
        return null;
    }


    @Override
    public ModuleType<?> getModuleType() {
        return moduleType;
    }

    @NotNull
    @Override
    public String getName() {
        return "GENESIS-API";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "A polyglot code generator";
    }
}

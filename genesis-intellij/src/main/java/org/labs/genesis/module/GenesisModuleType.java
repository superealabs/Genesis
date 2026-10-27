package org.labs.genesis.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.icon.SdkIcons;
import org.labs.genesis.wizards.*;

import javax.swing.*;

import static org.labs.genesis.module.GenesisModuleBuilder.projectGenerationContext;
import static org.labs.genesis.module.GenesisModuleBuilder.listProjectGenerationContexts;

final class GenesisModuleType extends ModuleType<GenesisModuleBuilder> {
    private static final String ID = "GENESIS_MODULE_TYPE";

    GenesisModuleType() {
        super(ID);
    }

    @Override
    public ModuleWizardStep @NotNull [] createWizardSteps(@NotNull WizardContext wizardContext,
                                                          @NotNull GenesisModuleBuilder moduleBuilder,
                                                          @NotNull ModulesProvider modulesProvider) {

        GenerationContextManager generationContextManager = new GenerationContextManager(projectGenerationContext);
        DataSourceChoiceWizardStep dataSourceChoiceWizardStep = new DataSourceChoiceWizardStep(generationContextManager);
        SpecificConfigurationWizardStep specificConfigurationWizardStep = new SpecificConfigurationWizardStep(generationContextManager, listProjectGenerationContexts);
        DatabaseConfigurationWizardStep databaseConfigurationWizardStep = new DatabaseConfigurationWizardStep(generationContextManager, listProjectGenerationContexts, dataSourceChoiceWizardStep);
        ScriptDatabaseConfigurationWizardStep scriptDatabaseConfigurationWizardStep = new ScriptDatabaseConfigurationWizardStep(generationContextManager, dataSourceChoiceWizardStep);
        RelationshipConfigurationWizardStep relationshipConfigurationWizardStep = new RelationshipConfigurationWizardStep(generationContextManager);
        GenerationOptionWizardStep generationOptionWizardStep = new GenerationOptionWizardStep(generationContextManager, listProjectGenerationContexts, specificConfigurationWizardStep, relationshipConfigurationWizardStep);
        GitConfigurationWizardStep gitConfigurationWizardStep = new GitConfigurationWizardStep(generationContextManager);

        // Rule to code
        FirstWizardStep firstWizardStep = new FirstWizardStep(generationContextManager);
        RuleToCodeWizardStep ruleToCodeWizardStep = new RuleToCodeWizardStep(generationContextManager);
        RuleToCodeWizardAIStep ruleToCodeWizardAIStep = new RuleToCodeWizardAIStep(generationContextManager);

        // Sync Generation
        SynchGenerationWizardStep syncGenerationWizardStep = new SynchGenerationWizardStep(generationContextManager);
        SyncProjectLoaderWizardStep syncProjectLoaderWizardStep = new SyncProjectLoaderWizardStep(generationContextManager, relationshipConfigurationWizardStep, syncGenerationWizardStep);

        FrontendConfigurationWizardStep frontendConfigurationWizardStep = new FrontendConfigurationWizardStep(generationContextManager, listProjectGenerationContexts);
        InitializationWizardStep initializationWizardStep = new InitializationWizardStep(generationContextManager, listProjectGenerationContexts, specificConfigurationWizardStep, frontendConfigurationWizardStep);

        return new ModuleWizardStep[]{
                firstWizardStep,
                dataSourceChoiceWizardStep,
                ruleToCodeWizardStep,
                ruleToCodeWizardAIStep,
                syncProjectLoaderWizardStep,
                initializationWizardStep,
                databaseConfigurationWizardStep,
                scriptDatabaseConfigurationWizardStep,
                generationOptionWizardStep,
                relationshipConfigurationWizardStep,
                frontendConfigurationWizardStep,
                gitConfigurationWizardStep,
                specificConfigurationWizardStep,
                syncGenerationWizardStep,
        };
    }

    @NotNull
    @Override
    public GenesisModuleBuilder createModuleBuilder() {
        return new GenesisModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "Genesis";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Genesis : A Database-First code generator for Java Spring Boot and .NET for JetBrains IDEs";
    }

    @Override
    public @NotNull Icon getNodeIcon(boolean isOpened) {
        return SdkIcons.Sdk_default_icon;
    }
}

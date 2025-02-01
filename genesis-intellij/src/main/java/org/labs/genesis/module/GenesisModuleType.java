package org.labs.genesis.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.icon.SdkIcons;
import org.labs.genesis.wizards.DatabaseConfigurationWizardStep;
import org.labs.genesis.wizards.GenerationOptionWizardStep;
import org.labs.genesis.wizards.InitializationWizardStep;
import org.labs.genesis.wizards.SpecificConfigurationWizardStep;
import org.labs.genesis.wizards.conditionals.GenConfigConditionalWizardStep;
import org.labs.genesis.wizards.conditionals.InitConditionalWizardStep;

import javax.swing.*;

import static org.labs.genesis.module.GenesisModuleBuilder.projectGenerationContext;

final class GenesisModuleType extends ModuleType<GenesisModuleBuilder> {
    private static final String ID = "GENESIS_API_MODULE_TYPE";

    GenesisModuleType() {
        super(ID);
    }

    @Override
    public ModuleWizardStep @NotNull [] createWizardSteps(@NotNull WizardContext wizardContext,
                                                          @NotNull GenesisModuleBuilder moduleBuilder,
                                                          @NotNull ModulesProvider modulesProvider) {

        SpecificConfigurationWizardStep specificConfigurationWizardStep = new SpecificConfigurationWizardStep(projectGenerationContext);
        DatabaseConfigurationWizardStep databaseConfigurationWizardStep = new DatabaseConfigurationWizardStep(projectGenerationContext);
        InitConditionalWizardStep initConditionalWizardStep = new InitConditionalWizardStep(projectGenerationContext, databaseConfigurationWizardStep);
        GenerationOptionWizardStep generationOptionWizardStep = new GenerationOptionWizardStep(projectGenerationContext);
        GenConfigConditionalWizardStep genConfigConditionalWizardStep = new GenConfigConditionalWizardStep(projectGenerationContext, generationOptionWizardStep);

        return new ModuleWizardStep[]{
                new InitializationWizardStep(projectGenerationContext, specificConfigurationWizardStep),
                initConditionalWizardStep,
                genConfigConditionalWizardStep,
                specificConfigurationWizardStep
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
        return "GENESIS-API";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Let's build microservices.";
    }

    @Override
    public @NotNull Icon getNodeIcon(boolean isOpened) {
        return SdkIcons.Sdk_default_icon;
    }


}


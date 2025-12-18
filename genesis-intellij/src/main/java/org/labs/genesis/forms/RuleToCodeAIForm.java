package org.labs.genesis.forms;


import com.intellij.openapi.ui.Messages;
import lombok.Getter;
import lombok.Setter;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.project.LlmApiConfig;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.ruleToCode.*;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Getter
@Setter
public class RuleToCodeAIForm {
    private JPanel mainPanel;
    private JComboBox<String> aiOptionsComboBox;
    private JComboBox<LlmApiConfig> listLlmApiConfigsComboBox;
    private JButton integrateButton;
    private JButton deleteButton;
    private JButton generateCodeButton;

    private RTextScrollPane scrollPane;
    private RSyntaxTextArea codeContent;

    private JTextField tokenField;
    private JCheckBox checkPersonalToken;
    private final LlmApiClientRule llmApiClientRule;
    private final ProjectGenerationContext context;

    public RuleToCodeAIForm( ProjectGenerationContext context ) {
        this.llmApiClientRule = new LlmApiClientRule();
        listLlmApiConfigsComboBox = new JComboBox<>();
        this.context = context;
        populateLlmModelComboBox();
        setupListeners();
    }
    private void populateLlmModelComboBox() {
        List<LlmApiConfig> llmApiConfigList = ProjectGenerator.llmApiConfigs.values().stream().toList();
        for (LlmApiConfig llmApiConfig : llmApiConfigList) {
            aiOptionsComboBox.addItem(llmApiConfig.getName());
            listLlmApiConfigsComboBox.addItem(llmApiConfig);
        }
    }
    private void setupListeners() {
        configureCheckPersonalToken();
        // ---------------- Generate Code AI ----------------
        generateCodeFromLLM();
        // ---------------- Integrete code ----------------
        integrateCode();
        // ---------------- Delete code ----------------
        deleteCode();
    }
    private void configureCheckPersonalToken() {
        checkPersonalToken.addActionListener(e -> {
            boolean selected = checkPersonalToken.isSelected();
            tokenField.setEnabled(selected);
            this.llmApiClientRule.setUseCustomApiKey(selected);
        });
    }

    private void generateCodeFromLLM() {
        getGenerateCodeButton().addActionListener(e -> {
        try {
            Framework framework = context.getFramework();
            String[] meta = new String[0];
            YamlData yamlData = new YamlData();

            PromptManagement promptManagement = new PromptManagement();
            promptManagement.managementPrompt(framework.getId());
            String prompt = promptManagement.getGeneralPrompt() ;
            String under_prompt = promptManagement.getUnderPrompt();

            String yamlContent = context.getProjectDescription();
            String selectedAI = (String) aiOptionsComboBox.getSelectedItem();
            String pathProject = context.getDestinationFolder();
            Path path = Paths.get(pathProject) ;
            meta = yamlData.extractGroupAndArtifact( path , framework.getId());

            LlmApiConfig llmApiConfig = (LlmApiConfig) this.listLlmApiConfigsComboBox.getSelectedItem();
            this.llmApiClientRule.setDefaultModel(llmApiConfig.getModel());
            this.llmApiClientRule.setApiUrl(llmApiConfig.getApiUrl());
            if(this.llmApiClientRule.getUseCustomApiKey()) {
                this.llmApiClientRule.setApiKey(this.tokenField.getText().trim());
            } else {
                this.llmApiClientRule.setApiKeyFromFile();
            }

            String codeGenerated = llmApiClientRule.generateFunction( yamlContent , prompt , under_prompt , selectedAI , meta ) ;
            codeContent.setText(codeGenerated);

            Messages.showInfoMessage(
                    mainPanel,
                    "Code generation successful!",
                    "Success"
            );

        } catch (IllegalStateException ex) {
            Messages.showErrorDialog(
                    mainPanel,
                    ex.getMessage(),
                    "Error"
            );
        } catch (Exception ex) {
            Messages.showErrorDialog(
                    mainPanel,
                    "Failed to generate code : " + ex.getMessage(),
                    "Error"
            );
        }
        });
    }

    private void integrateCode(){
        getIntegrateButton().addActionListener(e -> {
            try {
                CodeInjector codeInjector = new CodeInjector();
                if( validateAiCodeGenerated() ){
                    String codeGenerated = this.codeContent.getText();
                    Framework framework = context.getFramework();
                    List<CodeBlock> blocks = codeInjector.splitCode(codeGenerated , framework.getId() );

                    String pathProject = context.getDestinationFolder();
                    Path path = Paths.get(pathProject) ;

                    String basePath = path.getParent().toString();
                    String projectName = path.getFileName().toString();

                    System.out.println("Blocs code\n" + blocks);
                    codeInjector.injectBlocks(blocks , basePath , framework.getId(), projectName );

                    Messages.showInfoMessage(
                            mainPanel,
                            "Code integrate successful!",
                            "Success"
                    );
                }


            } catch (Exception ex) {
                Messages.showErrorDialog(
                        mainPanel,
                        ex.getMessage(),
                        "Error"
                );
            }

        });
    }
    private void deleteCode(){
        getDeleteButton().addActionListener(e -> {
            try {
                CodeInjector codeInjector = new CodeInjector();
                if( validateAiCodeGenerated() ){
                    String codeGenerated = this.codeContent.getText();
                    Framework framework = context.getFramework();
                    List<CodeBlock> blocks = codeInjector.splitCode(codeGenerated , framework.getId() );

                    String pathProject = context.getDestinationFolder();
                    Path path = Paths.get(pathProject) ;

                    String basePath = path.getParent().toString();
                    String projectName = path.getFileName().toString();

                    System.out.println("Blocs code\n" + blocks);

                    codeInjector.deleteBlocks(blocks , basePath , framework.getId(), projectName );
                    Messages.showInfoMessage(
                            mainPanel,
                            "Delete code successful!",
                            "Success"
                    );
                }
            } catch (Exception ex) {
                Messages.showErrorDialog(
                        mainPanel,
                        ex.getMessage(),
                        "Error"
                );
            }

        });
    }

    private boolean validateAiCodeGenerated() {
            String code =this.codeContent.getText();
            if(code.isEmpty()){
                Messages.showErrorDialog(
                        mainPanel,
                        "The code can not be empty",
                        "Error"
                );
                return false;
            }
            return true;
        }
}

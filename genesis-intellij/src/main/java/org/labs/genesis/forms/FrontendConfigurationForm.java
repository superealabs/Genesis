package org.labs.genesis.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBList;
import lombok.Getter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.model.InterfaceLang;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.listener.ColorPicker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Objects;

@Getter
public class FrontendConfigurationForm {
    private JPanel mainPanel;
    private JComboBox<FrontendLanguage> frontendLanguageOptions;
    private JComboBox<FrontendFramework> frontendFrameworkOptions;
    private JLabel languageLabel;
    private JLabel frameworkLabel;
    private JLabel templateEngine;
    private JComboBox<String> viewsTemplateEngineOptions;
    private JComboBox<String> navbarSelect;
    private JTextField primaryColorField;
    private JButton primaryColorPickerButton;
    private JLabel primaryColorLabel;
    private JTextField secondaryColorField;
    private JLabel secondaryColorLabel;
    private JButton secondaryColorPickerButton;
    private TextFieldWithBrowseButton logoFileField;
    private TextFieldWithBrowseButton faviconFileField;
    private JTextField logoLinkField;
    private JTextField faviconLinkField;
    private JBList<InterfaceLang> interfaceLangOptions;
    private JCheckBox frontendGeneration;
    private JLabel logoFileLabel;
    private JLabel logoLinkLabel;
    private JPanel favicon;
    private JLabel faviconFileLabel;
    private JLabel faviconLinkLabel;
    private JLabel navbarLabel;
    private JLabel languagesLabel;
    private JLabel brandingLabel;
    private JTextField portInput;
    private JLabel portLabel;

    private JComboBox<String> comboBoxProjectList;
    private JComboBox<ProjectGenerationContext> contextList ;
    private JLabel labelListProject;
    private JButton addFrontendButton;
    private final List<ProjectGenerationContext> listProjectGenerationContexts ;

    private File logoFile;
    private File faviconFile;

    public FrontendConfigurationForm(List<ProjectGenerationContext> listProjectGenerationContexts) {
        this.listProjectGenerationContexts = listProjectGenerationContexts;
        contextList = new JComboBox<>();
        frontendGeneration.setSelected(false);
        initializeListners();
        initializeOptions();
    }
    public void refreshUI(boolean isMultiProject) {
        if (isMultiProject) {
            addListProject();
            comboBoxProjectList.setVisible(true);
            labelListProject.setVisible(true);
            addFrontendButton.setVisible(true);
        } else {
            comboBoxProjectList.setVisible(false);
            labelListProject.setVisible(false);
            addFrontendButton.setVisible(false);
        }
    }

    public void addListProject() {
        DefaultComboBoxModel<String> nameModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<ProjectGenerationContext> contextModel = new DefaultComboBoxModel<>();

        for (ProjectGenerationContext context : listProjectGenerationContexts) {
            nameModel.addElement(context.getProjectName() + " " + context.getFramework().getName() + " " +context.getDatabase().getName() + " " + context.getCredentials().getDatabaseName());
            contextModel.addElement(context);
        }
        comboBoxProjectList.setModel(nameModel);
        comboBoxProjectList.setVisible(true);
        comboBoxProjectList.revalidate();
        comboBoxProjectList.repaint();

        contextList.setModel(contextModel);
        contextList.setVisible(true);
        contextList.revalidate();
        contextList.repaint();

        comboBoxProjectList.addActionListener(e -> {
            int index = comboBoxProjectList.getSelectedIndex();
            if (index >= 0 && index < contextList.getModel().getSize()) {
                contextList.setSelectedIndex(index);
            }
        });

    }

    private void initializeListners(){
        primaryColorPickerButton.addActionListener(new ColorPicker(primaryColorField ,Color.decode("#537cc2")));
        secondaryColorPickerButton.addActionListener(new ColorPicker(secondaryColorField ,new Color(0x53,0x7c,0xc2,0x26)));
        frontendGeneration.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (frontendGeneration.isSelected()) {
                    disableForm();
                } else {
                    enableForm();
                }
            }
        });

        FileChooserDescriptor faviconChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select favicon file")
                .withDescription("Choose a .ico file to load into the editor")
                .withFileFilter(file -> {
                    String extension = file.getExtension();
                    return extension.equals("ico") || extension.equals("svg");
                });

        FileChooserDescriptor logoChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select logo file")
                .withDescription("Choose image or icon file to load into the editor")
                .withFileFilter(file -> {
                    String extension = file.getExtension();
                    return extension.equals("png") || extension.equals("jpeg") || extension.equals("jpg") || extension.equals("svg") || extension.equals("ico");
                });

        logoFileField.addBrowseFolderListener(null, logoChooserDescriptor);
        logoFileField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                onLogoFileChange();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                onLogoFileChange();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                onLogoFileChange();
            }
        });
        logoLinkField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onLogoLinkChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onLogoLinkChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onLogoLinkChange();
            }
        });

        faviconFileField.addBrowseFolderListener(null, faviconChooserDescriptor);
        faviconFileField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                onFaviconFileChange();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                onFaviconFileChange();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                onFaviconFileChange();
            }
        });
        faviconLinkField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                onFaviconLinkChange();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                onFaviconLinkChange();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                onFaviconLinkChange();
            }
        });
    }
    private void onLogoFileChange() {
        String filePath = logoFileField.getText();
        if (filePath == null || filePath.isEmpty()) return;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return;
        logoFile = file;
    }
    private void onLogoLinkChange() {
        String text = logoLinkField.getText();
        boolean hasText = text != null && !text.trim().isEmpty();
        // disable the file chooser if link is typed
        logoFileField.setEnabled(!hasText);
        logoFileLabel.setEnabled(!hasText);
    }

    private void onFaviconFileChange() {
        String filePath = faviconFileField.getText();
        if (filePath == null || filePath.isEmpty()) return;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return;
        faviconFile = file;
    }
    private void onFaviconLinkChange() {
        String text = faviconLinkField.getText();
        boolean hasText = text != null && !text.trim().isEmpty();
        // disable the file chooser if link is typed
        faviconFileField.setEnabled(!hasText);
        faviconFileLabel.setEnabled(!hasText);
    }

    private  void initializeOptions(){
        populateLanguageOptions();
        populateFrameworkOptions((FrontendLanguage) frontendLanguageOptions.getSelectedItem());
        interfaceLangOptions.setListData(ProjectGenerator.langs.values().toArray(new InterfaceLang[0]));
        InterfaceLang defaultLanguage = ProjectGenerator.langs.get(1);
        if (defaultLanguage != null) {
            interfaceLangOptions.setSelectedValue(defaultLanguage, true);
        }
        navbarSelect.setModel(new DefaultComboBoxModel<>(new String[]{"Sidebar","Topbar"}));

        languageLabel.setVisible(true);
        frameworkLabel.setVisible(true);
        frontendLanguageOptions.setVisible(true);
        frontendFrameworkOptions.setVisible(true);
        portInput.setVisible(true);

        templateEngine.setVisible(false);
        viewsTemplateEngineOptions.setVisible(false);
    }

    private void populateLanguageOptions() {
        List<FrontendLanguage> frontendLanguages = ProjectGenerator.frontendLanguage.values().stream().toList();
        for (FrontendLanguage frontendLanguage : frontendLanguages) {
            frontendLanguageOptions.addItem(frontendLanguage);
        }
        frontendLanguageOptions.setSelectedIndex(0);
    }

    private void populateFrameworkOptions(FrontendLanguage frontendLanguage) {
        ActionListener[] listeners = frontendFrameworkOptions.getActionListeners();
        for (ActionListener listener : listeners) {
            frontendFrameworkOptions.removeActionListener(listener);
        }

        frontendFrameworkOptions.removeAllItems();
        List<FrontendFramework> frameworks = ProjectGenerator.frontendFrameworks.values().stream()
                .filter(f -> f.getLanguageId() == frontendLanguage.getId())
                .distinct()
                .toList();

        for (FrontendFramework framework : frameworks) {
            frontendFrameworkOptions.addItem(framework);
        }

        if (frontendFrameworkOptions.getItemCount() > 0) {
            frontendFrameworkOptions.setSelectedIndex(0);
            portInput.setText(((FrontendFramework) Objects.requireNonNull(frontendFrameworkOptions.getSelectedItem())).getDefaultPort());
        }
        frontendFrameworkOptions.addActionListener(e -> {
            FrontendFramework selectedFramework = (FrontendFramework) frontendFrameworkOptions.getSelectedItem();
            if (selectedFramework != null) {
                portInput.setText(selectedFramework.getDefaultPort());
            }
        });
    }

    private void disableForm(){
        setAccessiblePanel(mainPanel, false);
        getFrontendGeneration().setEnabled(true);
        getInterfaceLangOptions().setEnabled(false);
    }

    private void enableForm(){
        setAccessiblePanel(mainPanel, true);
        getFrontendGeneration().setEnabled(true);
        getInterfaceLangOptions().setEnabled(true);
    }

    private void setAccessiblePanel(JPanel panel, boolean accessible){
        panel.setEnabled(accessible);
        for (Component component : panel.getComponents()) {
            if (component instanceof JPanel){
                setAccessiblePanel((JPanel) component, accessible);
            }
            component.setEnabled(accessible);
        }
    }

    private void configureMVCOptions(FrameworkMVC frameworkMVC) {
        templateEngine.setVisible(true);
        viewsTemplateEngineOptions.setVisible(true);

        languageLabel.setVisible(false);
        frameworkLabel.setVisible(false);
        frontendLanguageOptions.setVisible(false);
        frontendFrameworkOptions.setVisible(false);
        portInput.setVisible(false);

        viewsTemplateEngineOptions.removeAllItems();
        viewsTemplateEngineOptions.addItem(frameworkMVC.getView().getViewTemplateEngine());
    }

    public void updateFormWithFrameworkMVCOptions(FrameworkMVC frameworkMVC) {
        configureMVCOptions(frameworkMVC);
    }

    public void updateFormWithFrontendFrameworkOptions() {
        initializeOptions();
    }
}

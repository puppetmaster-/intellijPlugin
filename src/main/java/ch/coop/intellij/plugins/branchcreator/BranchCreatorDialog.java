package ch.coop.intellij.plugins.branchcreator;

import ch.coop.intellij.plugins.CoopPluginSettings;
import ch.coop.intellij.plugins.helper.UIHelper;
import ch.coop.intellij.plugins.services.JiraService;
import ch.coop.intellij.plugins.vcs.VcsCommandException;
import ch.coop.intellij.plugins.vcs.VcsHandler;
import ch.coop.intellij.plugins.vcs.VcsHandlerManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class BranchCreatorDialog extends DialogWrapper {
    private Project project;
    private JPanel contentPane;
    private GridBagConstraints gbc;
    private JTextField jiraIssueIdField;
    private JTextField shortDescriptionField;
    private JLabel newBranchNameValueLabel;
    private JComboBox<String> jiraIssueIdComboBox;
    private JComboBox<String> repositoryComboBox; // Dropdown für Repository-Verzeichnisse
    private JLabel configureLink;
    private JLabel vcsInfoLabel; // Label für VCS-Informationen
    private JLabel currentBranchLabel; // Label für den aktuellen Branch

    private final CoopPluginSettings settings;
    private final JiraService jiraService;
    private final VcsHandlerManager vcsHandlerManager = new VcsHandlerManager();

    public BranchCreatorDialog(@NotNull Project project) {
        super(true);
        this.project = project;
        this.settings = CoopPluginSettings.getInstance();
        this.jiraService = new JiraService(project, settings.getState().jiraApiUrl, settings.getState().jiraApiToken);
        init();
        setTitle("Create Branch");
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        System.out.println("createCenterPanel");
        initLayout();
        initComponents();
        return contentPane;
    }

    private void initLayout() {
        System.out.println("initLayout");
        contentPane = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Abstand zwischen den Komponenten
    }

    private void initComponents() {
        System.out.println("initComponents");

        // Repository-Verzeichnisse ermitteln
        List<String> repositoryPaths = new ArrayList<>();
        for (VcsHandler handler : vcsHandlerManager.getHandlers()) {
            repositoryPaths.addAll(handler.getRepositoryPaths(project));
        }

        // Repository-Auswahl
        repositoryComboBox = new ComboBox<>(repositoryPaths.toArray(new String[0]));
        repositoryComboBox.addActionListener(e -> updateVcsInfo()); // Listener für Repository-Auswahl
        addLabelAndComponent(0, "Repository:", repositoryComboBox);

        // VCS-Informationen (initial leer)
        vcsInfoLabel = new JLabel();
        addLabelAndComponent(1, "VCS:", vcsInfoLabel);

        // Aktueller Branch (initial leer)
        currentBranchLabel = new JLabel();
        addLabelAndComponent(2, "Current Branch:", currentBranchLabel);

        // Jira Issue ID
        jiraIssueIdComboBox = new JComboBox<>();
        jiraIssueIdComboBox.setEditable(true);
        addLabelAndComponent(3, "Jira Issue ID:", jiraIssueIdComboBox);

        // Short Description
        shortDescriptionField = new JTextField();
        shortDescriptionField.setToolTipText("Enter a short description for the branch.");
        addLabelAndComponent(4, "Short Description:", shortDescriptionField);

        // Branch Name Preview
        newBranchNameValueLabel = new JLabel();
        newBranchNameValueLabel.setText("<html><b>" + newBranchNameValueLabel.getText() + "</b></html>");
        addLabelAndComponent(5, "New Branch Name:", newBranchNameValueLabel);

        // Klickbarer Textlink zur Konfiguration
        configureLink = UIHelper.createConfigLink("Configure Repo Branch Creator", "Repo Branch Creator Settings",project);
        addComponent(6, configureLink); // Leeres Label, um den Link korrekt auszurichten

        // Add document listeners to update the branch name preview and trigger validation
        jiraIssueIdComboBox.getEditor().getEditorComponent().addPropertyChangeListener("document", evt -> {
            updateNewBranchNamePreview();
            searchJiraIssues(jiraIssueIdComboBox.getEditor().getItem().toString());
            validate(); // Validierung bei Änderungen in der Jira Issue ID
        });

        shortDescriptionField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateNewBranchNamePreview();
                validate(); // Validierung bei Änderungen in der Short Description
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateNewBranchNamePreview();
                validate(); // Validierung bei Änderungen in der Short Description
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateNewBranchNamePreview();
                validate(); // Validierung bei Änderungen in der Short Description
            }
        });

        jiraIssueIdComboBox.requestFocusInWindow();
    }

    private void updateVcsInfo() {
        // Ausgewähltes Repository-Verzeichnis
        String selectedRepository = (String) repositoryComboBox.getSelectedItem();
        if (selectedRepository == null) {
            vcsInfoLabel.setText("No repository selected");
            currentBranchLabel.setText("N/A");
            return;
        }

        // VCS-Handler für das ausgewählte Verzeichnis
        VcsHandler handler = vcsHandlerManager.getSupportedHandler(project);
        if (handler != null) {
            try {
                // VCS-Typ anzeigen
                vcsInfoLabel.setText(handler.getClass().getSimpleName().replace("Handler", ""));

                // Aktuellen Branch anzeigen
                String currentBranch = handler.getCurrentBranch(project, selectedRepository);
                currentBranchLabel.setText(currentBranch != null ? currentBranch : "N/A");
            } catch (VcsCommandException e) {
                vcsInfoLabel.setText("Error");
                currentBranchLabel.setText("Error: " + e.getMessage());
            }
        } else {
            vcsInfoLabel.setText("No supported VCS");
            currentBranchLabel.setText("N/A");
        }
    }

    private void updateNewBranchNamePreview() {
        String jiraIssueId = jiraIssueIdComboBox.getEditor().getItem().toString();
        String shortDescription = shortDescriptionField.getText().replace(" ", settings.getState().spaceReplacement);
        String newBranchName = generateNewBranchName(jiraIssueId, shortDescription);
        newBranchNameValueLabel.setText("<html><b>" + newBranchName + "</b></html>");
    }

    @NotNull
    String generateNewBranchName(@NotNull String jiraIssueId, @NotNull String shortDescription) {
        String sanitizedJiraIssueId = sanitizeBranchName(jiraIssueId);
        String sanitizedShortDescription = sanitizeBranchName(shortDescription);
        return settings.getState().prefix + sanitizedJiraIssueId + "-" + sanitizedShortDescription;
    }

    @NotNull
    String sanitizeBranchName(String input) {
        return input.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String jiraIssueId = jiraIssueIdComboBox.getEditor().getItem().toString();
        String shortDescription = shortDescriptionField.getText();

        if (jiraIssueId.isEmpty()) {
            return new ValidationInfo("Jira Issue ID is required", jiraIssueIdComboBox);
        }
        if (!jiraIssueId.matches("[A-Z]+-[0-9]+")) { // Beispiel: JIRA-1234
            return new ValidationInfo("Jira Issue ID must be in the format 'PROJECT-1234'", jiraIssueIdComboBox);
        }
        if (shortDescription.isEmpty()) {
            return new ValidationInfo("Short Description is required", shortDescriptionField);
        }
        if (shortDescription.length() > 50) {
            return new ValidationInfo("Short Description must be less than 50 characters", shortDescriptionField);
        }
        return null;
    }

    @Override
    protected Action @NotNull [] createActions() {
        // Standardaktionen (OK und Cancel) holen
        Action[] actions = super.createActions();

        // OK-Button (Create Branch) nach rechts verschieben
        Action okAction = actions[0]; // OK-Button
        Action cancelAction = actions[1]; // Cancel-Button

        // Reihenfolge ändern: Cancel links, OK rechts
        return new Action[]{cancelAction, okAction};
    }

    @Override
    protected @NotNull Action getOKAction() {
        // Text des OK-Buttons ändern
        return new DialogWrapperAction("Create Branch") {
            @Override
            protected void doAction(ActionEvent e) {
                // Standard-OK-Logik ausführen
                doOKAction();
            }
        };
    }

    @Override
    protected void doOKAction() {
        // Validierung durchführen
        ValidationInfo validationInfo = doValidate();
        if (validationInfo != null) {
            Messages.showErrorDialog(project, validationInfo.message, "Validation Error");
            return;
        }

        // Bestätigungsdialog anzeigen
        String confirmationMessage = "Are you sure you want to create the branch";
        if (settings.getState().autoPush) {
            confirmationMessage += " and push it to the remote repository";
        }
        confirmationMessage += "?";

        int result = Messages.showYesNoDialog(
                project,
                confirmationMessage,
                "Confirm Branch Creation",
                Messages.getQuestionIcon()
        );

        if (result != Messages.YES) {
            return; // Abbruch, wenn der Benutzer nicht bestätigt
        }

        // Branch erstellen
//        String jiraIssueId = jiraIssueIdComboBox.getEditor().getItem().toString();
//        String shortDescription = shortDescriptionField.getText().replace(" ", settings.getState().spaceReplacement);
//        String branchName = generateNewBranchName(jiraIssueId, shortDescription);
//        createBranch(branchName);

        // Dialog schließen
        super.doOKAction();
    }

//    private void createBranch(String branchName) {
//        // Ausgewähltes Repository-Verzeichnis
//        String selectedRepository = (String) repositoryComboBox.getSelectedItem();
//        if (selectedRepository == null) {
//            Messages.showErrorDialog(project, "No repository selected.", "Error");
//            return;
//        }
//
//        // VCS-Handler für das ausgewählte Verzeichnis
//        VcsHandler handler = vcsHandlerManager.getSupportedHandler(project);
//        if (handler != null) {
//            try {
//                handler.createBranch(project, branchName, selectedRepository, true);
//            } catch (Exception ex) {
//                Messages.showErrorDialog(project, "Failed to create branch: " + ex.getMessage(), "Error");
//            }
//        } else {
//            Messages.showErrorDialog(project, "No supported VCS repository found.", "Error");
//        }
//    }

    public String getNewBranchName() {
        return newBranchNameValueLabel.getText();
    }
    /**
     * Gibt den ausgewählten Repository-Pfad zurück.
     *
     * @return Der ausgewählte Repository-Pfad oder null, wenn keiner ausgewählt wurde.
     */
    public String getSelectedRepositoryPath() {
        return (String) repositoryComboBox.getSelectedItem();
    }

    private void searchJiraIssues(String query) {
        List<String> issues = jiraService.searchJiraIssues(query);
        updateJiraIssueIdComboBox(issues);
    }

    private void updateJiraIssueIdComboBox(List<String> issues) {
        jiraIssueIdComboBox.removeAllItems();
        for (String issue : issues) {
            jiraIssueIdComboBox.addItem(issue);
        }
    }

    protected void addLabelAndComponent(int gridy, String labelText, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        contentPane.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        contentPane.add(component, gbc);
    }

    protected void addComponent(int gridy, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 2; // Spannt die Komponente über beide Spalten
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        contentPane.add(component, gbc);

        // Reset gridwidth für nachfolgende Komponenten
        gbc.gridwidth = 1;
    }
}
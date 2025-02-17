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
    private JTextField shortDescriptionField;
    private JLabel newBranchNameValueLabel;
    private JComboBox<String> jiraIssueIdComboBox;
    private JComboBox<String> repositoryComboBox;
    private JLabel configureLink;
    private JLabel vcsInfoLabel;
    private JLabel currentBranchLabel;

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
        gbc.insets = new Insets(5, 5, 5, 5);
    }

    private void initComponents() {
        System.out.println("initComponents");

        List<String> repositoryPaths = new ArrayList<>();
        for (VcsHandler handler : vcsHandlerManager.getHandlers()) {
            repositoryPaths.addAll(handler.getRepositoryPaths(project));
        }

        repositoryComboBox = new ComboBox<>(repositoryPaths.toArray(new String[0]));
        repositoryComboBox.addActionListener(e -> updateVcsInfo());
        addLabelAndComponent(0, "Repository:", repositoryComboBox);

        vcsInfoLabel = new JLabel();
        addLabelAndComponent(1, "VCS:", vcsInfoLabel);

        currentBranchLabel = new JLabel();
        addLabelAndComponent(2, "Current Branch:", currentBranchLabel);

        jiraIssueIdComboBox = new JComboBox<>();
        jiraIssueIdComboBox.setEditable(true);
        addLabelAndComponent(3, "Jira Issue ID:", jiraIssueIdComboBox);

        shortDescriptionField = new JTextField();
        shortDescriptionField.setToolTipText("Enter a short description for the branch.");
        addLabelAndComponent(4, "Short Description:", shortDescriptionField);

        newBranchNameValueLabel = new JLabel();
        newBranchNameValueLabel.setText("<html><b>" + newBranchNameValueLabel.getText() + "</b></html>");
        addLabelAndComponent(5, "New Branch Name:", newBranchNameValueLabel);

        configureLink = UIHelper.createConfigLink("Configure Repo Branch Creator", "Repo Branch Creator Settings",project);
        addComponent(6, configureLink);

        jiraIssueIdComboBox.getEditor().getEditorComponent().addPropertyChangeListener("document", evt -> {
            updateNewBranchNamePreview();
            searchJiraIssues(jiraIssueIdComboBox.getEditor().getItem().toString());
            validate();
        });

        shortDescriptionField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateNewBranchNamePreview();
                validate();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateNewBranchNamePreview();
                validate();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateNewBranchNamePreview();
                validate();
            }
        });

        jiraIssueIdComboBox.requestFocusInWindow();
    }

    private void updateVcsInfo() {
        String selectedRepository = (String) repositoryComboBox.getSelectedItem();
        if (selectedRepository == null) {
            vcsInfoLabel.setText("No repository selected");
            currentBranchLabel.setText("N/A");
            return;
        }

        VcsHandler handler = vcsHandlerManager.getSupportedHandler(project);
        if (handler != null) {
            try {
                vcsInfoLabel.setText(handler.getClass().getSimpleName().replace("Handler", ""));

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
        Action[] actions = super.createActions();

        Action okAction = actions[0];
        Action cancelAction = actions[1];

        return new Action[]{cancelAction, okAction};
    }

    @Override
    protected @NotNull Action getOKAction() {
        return new DialogWrapperAction("Create Branch") {
            @Override
            protected void doAction(ActionEvent e) {
                doOKAction();
            }
        };
    }

    @Override
    protected void doOKAction() {
        ValidationInfo validationInfo = doValidate();
        if (validationInfo != null) {
            Messages.showErrorDialog(project, validationInfo.message, "Validation Error");
            return;
        }

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
            return;
        }

        super.doOKAction();
    }

    public String getNewBranchName() {
        return newBranchNameValueLabel.getText();
    }

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
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        contentPane.add(component, gbc);

        gbc.gridwidth = 1;
    }
}
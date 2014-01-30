
package org.opentravel.schemas.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.widgets.WidgetFactory;

public class NewRepositoryWizard extends Wizard {

    private final RepositoryPage newRepositoryPage;
    private String location;
    private String userId;
    private String password;

    public NewRepositoryWizard(RepositoryPage repositoryPage) {
        this.newRepositoryPage = repositoryPage;
        setWindowTitle(Messages.getString("wizard.repository.new.title"));
    }

    public static NewRepositoryWizard createNewRepositoryWizard() {
        return new NewRepositoryWizard(new RepositoryPage());
    }

    public static NewRepositoryWizard createChangeCredentialWizard(RepositoryNode repository) {
        NewRepositoryWizard wizard = new NewRepositoryWizard(new ChangeCredentialsPage(repository));
        wizard.setWindowTitle(Messages.getString("wizard.repository.credentials.title"));
        return wizard;
    }

    @Override
    public void addPages() {
        addPage(newRepositoryPage);
    }

    @Override
    public boolean performFinish() {
        location = newRepositoryPage.urlText.getText();
        userId = newRepositoryPage.getUserId();
        password = newRepositoryPage.passwordText.getText();
        return newRepositoryPage.isPageComplete();
    }

    static class ChangeCredentialsPage extends RepositoryPage {

        private final RepositoryNode repositoryNode;

        public ChangeCredentialsPage(RepositoryNode repository) {
            super();
            this.repositoryNode = repository;
            setTitle(Messages.getString("wizard.repository.credentials.description"));
            setDescription(Messages.getString("wizard.repository.credentials.details"));
        }

        @Override
        public void createControl(Composite parent) {
            super.createControl(parent);
            urlText.setEditable(false);
            urlText.setText(getRepositoryLocation(repositoryNode));
        }

        private String getRepositoryLocation(RepositoryNode repositoryNode) {
            return repositoryNode.getLocation();
        }
    }

    static class RepositoryPage extends WizardPage implements ModifyListener, SelectionListener {
        protected Text urlText;
        protected Text userIdText;
        protected Text passwordText;
        protected Button anonymouseButton;

        /**
         * Create the wizard.
         */
        public RepositoryPage() {
            super("wizardPage");
            setTitle(Messages.getString("wizard.repository.new.description"));
            setDescription(Messages.getString("wizard.repository.new.details"));
            setPageComplete(false);
        }

        /**
         * Create contents of the wizard.
         * 
         * @param parent
         */
        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NULL);

            setControl(container);
            container.setLayout(new GridLayout(2, false));

            Label lblNewLabel = new Label(container, SWT.NONE);
            lblNewLabel.setText("URL:");

            urlText = WidgetFactory.createText(container, SWT.BORDER);
            urlText.addModifyListener(this);
            urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            urlText.addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(FocusEvent e) {
                    if (!isValidUrl(urlText.getText())) {
                        setPageComplete(false);
                    }
                }

            });

            Group grpCredentials = new Group(container, SWT.NONE);
            grpCredentials.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
            grpCredentials.setText("Credentials");
            grpCredentials.setLayout(new GridLayout(2, false));

            Label loginLabel = new Label(grpCredentials, SWT.NONE);
            loginLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            loginLabel.setText("Login:");

            userIdText = WidgetFactory.createText(grpCredentials, SWT.BORDER);
            userIdText.addModifyListener(this);
            userIdText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label passwordLabel = new Label(grpCredentials, SWT.NONE);
            passwordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            passwordLabel.setText("Password:");

            passwordText = WidgetFactory.createText(grpCredentials, SWT.BORDER | SWT.PASSWORD);
            passwordText.addModifyListener(this);
            passwordText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label anonymousLabel = new Label(grpCredentials, SWT.NONE);
            anonymousLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            anonymousLabel.setText("Anonymous:");

            anonymouseButton = new Button(grpCredentials, SWT.CHECK);
            anonymouseButton.addSelectionListener(this);
            anonymouseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }

        @Override
        public void modifyText(ModifyEvent e) {
            validate();
        }

        private void validate() {
            boolean valid = isValidUrl(urlText.getText());
            if (!isAnonymous()) {
                valid = valid && !isEmpty(userIdText) && !isEmpty(passwordText);
            }
            setPageComplete(valid);
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            boolean state = !isAnonymous();
            userIdText.setEnabled(state);
            passwordText.setEnabled(state);
            validate();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }

        private boolean isAnonymous() {
            return anonymouseButton.getSelection();
        }

        public String getUserId() {
            if (isAnonymous()) {
                return "";
            }
            return userIdText.getText();
        }

        private boolean isValidUrl(String string) {
            try {
                new URL(string);
            } catch (MalformedURLException e1) {
                setErrorMessage("Invalid URL");
                return false;
            }
            setErrorMessage(null);
            return true;
        }

        private boolean isEmpty(Text text) {
            return text.getText().isEmpty();
        }

    }

    public String getLocation() {
        return location;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

}

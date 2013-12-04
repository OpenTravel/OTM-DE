package com.sabre.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.trees.repository.RepositoryNode;
import com.sabre.schemas.wizards.NewRepositoryWizard;

public class RepositoryChangeCredentials extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil
                .getCurrentSelection(event);
        RepositoryNode node = (RepositoryNode) selection.getFirstElement();
        NewRepositoryWizard wizard = NewRepositoryWizard.createChangeCredentialWizard(node);
        WizardDialog dialog = new WizardDialog(OtmRegistry.getActiveShell(), wizard);
        int ret = dialog.open();
        if (ret == Window.OK) {
            changeCredentials(node, wizard);
        }
        return null;
    }

    private void changeCredentials(RepositoryNode node, NewRepositoryWizard wizard) {
        OtmRegistry.getMainController().getRepositoryController()
                .changeCredentials(wizard.getLocation(), wizard.getUserId(), wizard.getPassword());
    }

}

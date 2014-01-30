
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewRepositoryWizard;

public class NewRepository extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        NewRepositoryWizard wizard = NewRepositoryWizard.createNewRepositoryWizard();
        WizardDialog dialog = new WizardDialog(OtmRegistry.getActiveShell(), wizard);
        int ret = dialog.open();
        if (ret == Window.OK) {
            boolean success = addRepository(wizard);
            if (!success) {
                DialogUserNotifier.openInformation(Messages.getString("repository.error.title"),
                        Messages.getString("repository.warning.invalidLocation"));
            }
        }
        return null;
    }

    private boolean addRepository(NewRepositoryWizard wizard) {
        return OtmRegistry
                .getMainController()
                .getRepositoryController()
                .addRemoteRepository(wizard.getLocation(), wizard.getUserId(), wizard.getPassword());

    }

}

/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.wizards.NewRepositoryWizard;

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

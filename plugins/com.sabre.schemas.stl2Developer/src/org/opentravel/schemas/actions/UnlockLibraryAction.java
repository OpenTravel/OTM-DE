/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.OtmRegistry;

import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * Unlock a library in it's repository.
 * 
 * @author Dave Hollander
 * 
 */
public class UnlockLibraryAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.library.unlock");

    enum UnlockOperation {
        UNLOCK_WITHOUT_COMMIT(Messages.getString("action.library.unlock.revert")), UNLOCK_AND_COMMIT(
                Messages.getString("action.library.unlock.commit")), CANCEL(
                IDialogConstants.CANCEL_LABEL);
        private final String label;

        public String getLabel() {
            return label;
        }

        private UnlockOperation(String label) {
            this.label = label;
        }
    }

    public UnlockLibraryAction() {
        super(propDefault);
    }

    public UnlockLibraryAction(final StringProperties props) {
        super(props);
    }

    @Override
    public void run() {
        switch (askUser()) {
            case UNLOCK_AND_COMMIT:
                mc.getRepositoryController().unlock(true);
                break;
            case UNLOCK_WITHOUT_COMMIT:
                mc.getRepositoryController().unlock(false);
                break;
            case CANCEL:
            default:
                break;
        }
    }

    @Override
    public boolean isEnabled(Node node) {
        Node n = getMainController().getCurrentNode_NavigatorView();
        if (n == null || n.getLibrary() == null)
            return false;

        return n.getLibrary().getProjectItem().getState().equals(RepositoryItemState.MANAGED_WIP);
    }

    private UnlockOperation askUser() {
        List<UnlockOperation> operations = Arrays.asList(UnlockOperation.values());
        String[] labels = toStringArray(operations);
        MessageDialog dialog = new MessageDialog(OtmRegistry.getActiveShell(),
                Messages.getString("action.library.unlock.title"), null,
                Messages.getString("action.library.unlock.message"),
                MessageDialog.QUESTION_WITH_CANCEL, labels,
                operations.indexOf(UnlockOperation.CANCEL));

        int ret = dialog.open();
        return operations.get(ret);
    }

    private String[] toStringArray(List<UnlockOperation> operations) {
        String[] ret = new String[operations.size()];
        for (int i = 0; i < operations.size(); i++) {
            ret[i] = operations.get(i).getLabel();
        }
        return ret;
    }
}

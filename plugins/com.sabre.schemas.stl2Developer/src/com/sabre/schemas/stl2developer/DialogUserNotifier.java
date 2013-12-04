/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.stl2developer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Agnieszka Janowska
 * 
 */
public class DialogUserNotifier {
    static final Logger LOGGER = LoggerFactory.getLogger(DialogUserNotifier.class);

    public static void openWarning(final String title, final String message) {
        if (!OtmRegistry.getMainWindow().hasDisplay())
            return;
        MessageDialog.openWarning(OtmRegistry.getActiveShell(), title, message);
    }

    public static void openInformation(final String title, final String message) {
        if (!OtmRegistry.getMainWindow().hasDisplay())
            return;

        MessageDialog.openInformation(OtmRegistry.getActiveShell(), title, message);
    }

    public static void openError(final String title, final String message) {
        if (!OtmRegistry.getMainWindow().hasDisplay()) {
            LOGGER.warn("Error Dialog: " + message);
            return;
        }
        MessageDialog.openError(OtmRegistry.getActiveShell(), title, message);
    }

    public static boolean openConfirm(final String title, final String message) {
        if (!OtmRegistry.getMainWindow().hasDisplay()) {
            LOGGER.warn("Confirm Dialog: " + message);
            return true;
        }
        boolean ret;
        ret = MessageDialog.openConfirm(OtmRegistry.getActiveShell(), title, message);
        return ret;
    }

    public static boolean openQuestion(final String title, final String question) {
        if (!OtmRegistry.getMainWindow().hasDisplay())
            return false;
        boolean ret;
        ret = MessageDialog.openQuestion(OtmRegistry.getActiveShell(), title, question);
        return ret;
    }

    /*
     * this method returns: 0 - for YES 1 - for NO 2 - for CANCEL(non-Javadoc)
     * 
     * @see com.sabre.schemas.stl2developer.UserNotifier#openQuestionWithCancel(java.lang.String,
     * java.lang.String)
     */
    public static int openQuestionWithCancel(final String title, final String question) {
        if (!OtmRegistry.getMainWindow().hasDisplay())
            return 2;
        final MessageDialog dg = new MessageDialog(OtmRegistry.getActiveShell(), title, null,
                question, MessageDialog.QUESTION_WITH_CANCEL, new String[] {
                        IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        return dg.open();
    }
}

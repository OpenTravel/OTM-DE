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
package org.opentravel.schemas.stl2developer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.opentravel.schemas.properties.Messages;
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

	// TODO - use OpenInformationMsg instead of this
	public static void openInformation(final String title, final String message) {
		if (!OtmRegistry.getMainWindow().hasDisplay())
			return;
		MessageDialog.openInformation(OtmRegistry.getActiveShell(), title, message);
	}

	/**
	 * Open information dialog using the title and message defined in messages.properties
	 * 
	 * @param title
	 * @param message
	 */
	public static void openInformationMsg(final String title, final String message) {
		if (!OtmRegistry.getMainWindow().hasDisplay())
			return;
		MessageDialog.openInformation(OtmRegistry.getActiveShell(), Messages.getString(title),
				Messages.getString(message));
	}

	// // Please pass exception or null to openError()
	// @Deprecated
	// public static void openError(final String title, final String message) {
	// if (!OtmRegistry.getMainWindow().hasDisplay()) {
	// LOGGER.warn("Error DialogX: " + message);
	// return;
	// }
	// MessageDialog.openError(OtmRegistry.getActiveShell(), title, message);
	// }

	public static void openError(final String title, final String message, final Throwable e) {
		LOGGER.warn("Error Dialog: " + message);
		if (e != null)
			e.printStackTrace();

		if (OtmRegistry.getMainWindow().hasDisplay())
			MessageDialog.openError(OtmRegistry.getActiveShell(), title, message);
	}

	/**
	 * @return true if the user presses the OK button, false otherwise
	 */
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

	/**
	 * this method returns: 0 - for YES 1 - for NO 2 - for CANCEL(non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.stl2developer.UserNotifier#openQuestionWithCancel(java.lang.String, java.lang.String)
	 */
	public static int openQuestionWithCancel(final String title, final String question) {
		if (!OtmRegistry.getMainWindow().hasDisplay())
			return 2;
		final MessageDialog dg = new MessageDialog(OtmRegistry.getActiveShell(), title, null, question,
				MessageDialog.QUESTION_WITH_CANCEL,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL },
				0);
		return dg.open();
	}

	/**
	 * 
	 * @param title
	 * @param question
	 * @param labels
	 * @return - index to button pressed (0, 1, ...)
	 */
	public static int openQuestionWithButtons(final String title, final String question, final String[] labels) {
		if (!OtmRegistry.getMainWindow().hasDisplay())
			return 2;
		final MessageDialog dg = new MessageDialog(OtmRegistry.getActiveShell(), title, null, question,
				MessageDialog.QUESTION, labels, 0);
		return dg.open();
	}

	/**
	 * Post the status message and refresh the UI display. Intended for use when background tasks complete.
	 * 
	 * @param msg
	 */
	public static void syncWithUi(final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				OtmRegistry.getMainController().postStatus(msg);
				OtmRegistry.getMainController().refresh();
			}
		});
	}

	// public static void syncErrorWithUi(final String msg) {
	// Display.getDefault().asyncExec(new Runnable() {
	// @Override
	// public void run() {
	// if (!msg.isEmpty())
	// openError("Error", msg);
	// }
	// });
	// }

	public static void syncErrorWithUi(final String msg, final Throwable e) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!msg.isEmpty())
					openError("Error", msg, e);
			}
		});
	}

}

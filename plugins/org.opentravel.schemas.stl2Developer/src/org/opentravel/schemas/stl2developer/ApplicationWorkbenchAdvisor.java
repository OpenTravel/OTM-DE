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

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ModelController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.ModelContentsData;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.preferences.GeneralPreferencePage;
import org.opentravel.schemas.properties.Messages;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "org.opentravel.schemas.stl2Developer.perspective";
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationWorkbenchAdvisor.class);
	private static final String EXIT_WITHOUT_SAVE_FLAG = "-closeWithoutSave";

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	@Override
	public void initialize(final IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		configurer.setSaveAndRestore(true);

		// IEditorRegistry reg = configurer.getWorkbench().getEditorRegistry();
		// IEditorDescriptor ed = configurer.getWorkbench().getEditorRegistry()
		// .findEditor("org.opentravel.schemas.stl2Developer.ExampleView");
		// WindowManager wm = configurer.getWorkbenchWindowManager();
		// if (wm != null)
		// for (Window w : configurer.getWorkbenchWindowManager().getWindows())
		// LOGGER.debug("Window");

		// // activate proxy settings

		// Activator.getDefault().getProxyService();
		// LOGGER.debug("Loaded Proxy Settings");
	}

	// TODO - follow advise from: http://blog.eclipse-tips.com/2009/08/remember-state.html
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#postStartup()
	 */
	@Override
	public void postStartup() {
		// store current product version is property. later used in about dialog (by mapping file)
		Version productV = Platform.getProduct().getDefiningBundle().getVersion();
		System.setProperty("otm.version", productV.toString());

		// Load the projects open from last session with a progress monitor
		((DefaultProjectController) OtmRegistry.getMainController().getProjectController()).initProjects();
		LOGGER.debug("post startup.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#postShutdown()
	 */
	@Override
	public void postShutdown() {
		// TODO Auto-generated method stub
		super.postShutdown();
		// LOGGER.debug("post shutdown.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public IStatus saveState(IMemento memento) {
		// TODO Auto-generated method stub
		IStatus status = super.saveState(memento);
		ModelContentsData mcd = new ModelContentsData();
		mcd.saveState(memento);
		// LOGGER.debug("save state. " + memento);

		return status;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#restoreState(org.eclipse.ui.IMemento)
	 */
	@Override
	public IStatus restoreState(IMemento memento) {
		// TODO Auto-generated method stub
		IStatus status = super.restoreState(memento);
		// LOGGER.debug("restore state. " + memento);
		return status;
	}

	@Override
	public boolean preShutdown() {
		// Make sure they do not open next time
		OtmRegistry.closeDeprecatedViews();

		if (getWorkbenchConfigurer() != null && !getWorkbenchConfigurer().emergencyClosing()) {
			try {
				return confirmExit(OtmRegistry.getMainController());
			} catch (Exception ex) {
				// log exception and close application
				LOGGER.error("Error while closing application: " + ex.getMessage());
			}
		}
		return true;
	}

	public boolean confirmExit(MainController mc) {
		final ModelController modelController = mc.getModelController();
		final ModelNode modelNode = mc.getModelNode();

		if (modelNode != null) {
			int answer = IDialogConstants.CANCEL_ID;
			if (saveOnExitWithoutConfiramation()) {
				answer = IDialogConstants.NO_ID;
			} else {
				answer = askUser(modelNode.getLibraries());
			}
			return closeActions(answer, modelController, mc.getProjectController(), modelNode);
		}
		return true;
	}

	private int askUser(List<LibraryNode> libraries) {
		if (existUserLibrary(libraries)) {
			return getSaveAndExitConfirmation();
		} else {
			return getUserConfirmation();
		}
	}

	private boolean saveOnExitWithoutConfiramation() {
		for (String arg : Platform.getCommandLineArgs()) {
			if (EXIT_WITHOUT_SAVE_FLAG.equals(arg))
				return true;
		}
		return false;
	}

	private boolean closeActions(int answer, ModelController modelController, ProjectController projectController,
			ModelNode modelNode) {
		switch (answer) {
		case IDialogConstants.OK_ID:
			modelController.saveModel(modelNode);
		case IDialogConstants.NO_ID:
			projectController.saveState();
			return true;
		case IDialogConstants.CANCEL_ID:
		default:
			return false;
		}
	}

	private boolean existUserLibrary(List<LibraryNode> libraries) {
		for (LibraryNode l : libraries) {
			if (!l.isBuiltIn() && (l.isEditable())) {
				return true;
			}
		}
		return false;
	}

	private int getSaveAndExitConfirmation() {
		int answer = DialogUserNotifier.openQuestionWithCancel(Messages.getString("dialog.exit.title"),
				Messages.getString("dialog.exit.save.question"));
		switch (answer) {
		case 0:
			return IDialogConstants.OK_ID;
		case 1:
			return IDialogConstants.NO_ID;
		case 2:
		default:
			return IDialogConstants.CANCEL_ID;
		}

	}

	private int getUserConfirmation() {
		MessageDialog dialog = new MessageDialog(OtmRegistry.getActiveShell(), Messages.getString("dialog.exit.title"),
				null, Messages.getString("dialog.exit.question"), MessageDialog.CONFIRM,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL }, 1);
		return dialog.open();
	}

	@Override
	public String getMainPreferencePageId() {
		return GeneralPreferencePage.ID;
	}

}

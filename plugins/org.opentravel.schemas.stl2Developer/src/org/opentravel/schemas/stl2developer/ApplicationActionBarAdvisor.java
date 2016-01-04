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

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.opentravel.schemas.actions.ChangeAction;
import org.opentravel.schemas.actions.NewLibraryAction;
import org.opentravel.schemas.actions.NewProjectAction;
import org.opentravel.schemas.actions.OpenLibraryAction;
import org.opentravel.schemas.commands.CloseLibrariesHandler;
import org.opentravel.schemas.commands.CloseProjectHandler;
import org.opentravel.schemas.commands.CompileHandler;
import org.opentravel.schemas.commands.SaveLibrariesHandler;
import org.opentravel.schemas.commands.SaveLibraryHandler;
import org.opentravel.schemas.commands.ValidateHandler;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.utils.RCPUtils;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions added to a workbench window.
 * Each window will be populated with new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private IWorkbenchAction quitAction;
	private IWorkbenchAction about;

	public ApplicationActionBarAdvisor(final IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {

		MainWindow mainWindow = OtmRegistry.getMainWindow();
		final MenuManager projectMenu = new MenuManager("Project", "Project");
		final MenuManager libraryMenu = new MenuManager("Library", "Library");
		final MenuManager editMenu = new MenuManager("Edit", "Edit");
		final MenuManager windowMenu = new MenuManager("Window", "Window");
		final MenuManager helpMenu = new MenuManager("Help", "Help");

		IContributionItem compileHandler = RCPUtils.createCommandContributionItem(PlatformUI.getWorkbench(),
				CompileHandler.COMMAND_ID, null, null, null);
		IContributionItem closeProjectHandler = RCPUtils.createCommandContributionItem(PlatformUI.getWorkbench(),
				CloseProjectHandler.COMMAND_ID, null, null, null);
		NewProjectAction newProjectAction = new NewProjectAction();
		newProjectAction.setId("newProject");

		projectMenu.add(closeProjectHandler);
		projectMenu.add(newProjectAction);
		projectMenu.add(compileHandler);
		projectMenu.add(new Separator());
		projectMenu.add(quitAction);

		IContributionItem validate = RCPUtils.createCommandContributionItem(PlatformUI.getWorkbench(),
				ValidateHandler.COMMAND_ID, null, null, ValidateHandler.getIcon());
		IContributionItem saveSelectedLibrary = RCPUtils.createCommandContributionItem(PlatformUI.getWorkbench(),
				SaveLibraryHandler.COMMAND_ID, null, null, SaveLibrariesHandler.getIcon());
		IContributionItem saveAll = RCPUtils.createCommandContributionItem(PlatformUI.getWorkbench(),
				SaveLibrariesHandler.COMMAND_ID, null, null, SaveLibrariesHandler.getIcon());
		IContributionItem closeLibrary = RCPUtils.createCommandContributionItem(PlatformUI.getWorkbench(),
				CloseLibrariesHandler.COMMAND_ID, null, null, null);

		libraryMenu.add(new NewLibraryAction(mainWindow, new ExternalizedStringProperties("action.new")));
		libraryMenu.add(new OpenLibraryAction());
		libraryMenu.add(validate);
		libraryMenu.add(new Separator());
		libraryMenu.add(saveSelectedLibrary);
		libraryMenu.add(saveAll);
		libraryMenu.add(new Separator());
		libraryMenu.add(closeLibrary);

		editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
		editMenu.add(new ChangeAction(mainWindow, new ExternalizedStringProperties("action.changeObject")));
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));

		helpMenu.add(about);

		menuBar.add(projectMenu);
		menuBar.add(libraryMenu);
		menuBar.add(editMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);

	}

	@Override
	protected void makeActions(IWorkbenchWindow window) {
		IWorkbenchAction deleteAction = ActionFactory.DELETE.create(window);
		register(deleteAction);
		quitAction = ActionFactory.QUIT.create(window);
		register(quitAction);
		register(ActionFactory.SHOW_EDITOR.create(window));
		about = ActionFactory.ABOUT.create(window);
		register(about);

	}
}

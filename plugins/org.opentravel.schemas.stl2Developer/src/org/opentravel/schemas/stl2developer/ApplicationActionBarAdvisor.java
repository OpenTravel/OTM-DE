
package org.opentravel.schemas.stl2developer;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
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
import org.opentravel.schemas.actions.CloseProjectAction;
import org.opentravel.schemas.actions.CompileAction;
import org.opentravel.schemas.actions.MergeNodesAction;
import org.opentravel.schemas.actions.NewLibraryAction;
import org.opentravel.schemas.actions.NewProjectAction;
import org.opentravel.schemas.actions.OpenLibraryAction;
import org.opentravel.schemas.actions.RemoveAllLibrariesAction;
import org.opentravel.schemas.actions.RemoveLibrariesAction;
import org.opentravel.schemas.actions.SaveSelectedLibraryAsAction;
import org.opentravel.schemas.actions.ValidateAction;
import org.opentravel.schemas.commands.SaveLibrariesHandler;
import org.opentravel.schemas.commands.SaveLibraryHandler;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.utils.RCPUtils;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions added to
 * a workbench window. Each window will be populated with new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    private IWorkbenchAction quitAction;
    private IWorkbenchAction about;
    private IAction validateAction;
    private IAction compileAction;

    private static IAction mergeAction;
    private static IAction closeProject;
    private static IAction closeLibrary;
    private static IAction closeAllLibraryInProjectes;

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

        projectMenu.add(closeProject);
        NewProjectAction newProjectAction = new NewProjectAction();
        newProjectAction.setId("newProject");
        projectMenu.add(newProjectAction);
        projectMenu.add(compileAction);
        projectMenu.add(new Separator());
        projectMenu.add(quitAction);

        libraryMenu.add(new NewLibraryAction(mainWindow, new ExternalizedStringProperties(
                "action.new")));
        libraryMenu.add(new OpenLibraryAction());

        libraryMenu.add(validateAction);
        libraryMenu.add(new Separator());
        IContributionItem saveSelectedLibrary = RCPUtils.createCommandContributionItem(PlatformUI
                .getWorkbench(), SaveLibraryHandler.COMMAND_ID, null, null, Images
                .getImageRegistry().getDescriptor(Images.Save));
        libraryMenu.add(saveSelectedLibrary);
        IContributionItem saveAll = RCPUtils.createCommandContributionItem(PlatformUI
                .getWorkbench(), SaveLibrariesHandler.COMMAND_ID, null, null, Images
                .getImageRegistry().getDescriptor(Images.SaveAll));
        libraryMenu.add(saveAll);
        libraryMenu.add(new SaveSelectedLibraryAsAction(mainWindow,
                new ExternalizedStringProperties("action.saveSelectedAs")));
        libraryMenu.add(new Separator());
        libraryMenu.add(closeLibrary);
        libraryMenu.add(closeAllLibraryInProjectes);

        editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
        editMenu.add(new ChangeAction(mainWindow, new ExternalizedStringProperties(
                "action.changeObject")));
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
        closeProject = new CloseProjectAction();
        register(closeProject);
        closeLibrary = new RemoveLibrariesAction();
        register(closeLibrary);
        closeAllLibraryInProjectes = new RemoveAllLibrariesAction();
        register(closeAllLibraryInProjectes);
        mergeAction = new MergeNodesAction();
        register(mergeAction);
        about = ActionFactory.ABOUT.create(window);
        register(about);
        compileAction = new CompileAction();
        register(compileAction);
        validateAction = new ValidateAction();
        register(validateAction);

    }

    public IAction getMergeAction() {
        return mergeAction;
    }

    public static IAction getCloseProject() {
        return closeProject;
    }

    public static IAction getCloseLibrary() {
        return closeLibrary;
    }

    public static IAction getCloseAllLibraryInProjectes() {
        return closeAllLibraryInProjectes;
    }
}

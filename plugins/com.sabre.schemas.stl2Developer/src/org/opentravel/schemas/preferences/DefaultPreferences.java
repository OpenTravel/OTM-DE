package org.opentravel.schemas.preferences;

import java.io.File;

import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.opentravel.schemas.trees.repository.RemoteSearchToogleHandler;
import org.opentravel.schemas.views.RepositoryView;

public class DefaultPreferences {

    private static final String DEFAULT_DIRECTORY = "C:" + File.separator
            + "runtime-OT2Editor.product" + File.separator;

    public static String getProjectExtension() {
        return "otp";
    }

    public static String getDefaultProjectDirectory() {
        // Use hard coded path only for junit tests
        String defaultPath = DEFAULT_DIRECTORY;
        if (Platform.isRunning()) {// if (OtmRegistry.getMainWindow().hasDisplay())
            defaultPath = Platform.getInstanceLocation().getURL().getPath();
        }
        return defaultPath;
    }

    public static String getDefaultProjectPath() {
        return getDefaultProjectDirectory() + "DefaultProject." + getProjectExtension();
    }

    /**
     * @return true if {@link RepositoryView} filter should search repositories content for given
     *         phrase.
     */
    public static boolean getRepositoryRemoteSearch() {
        ICommandService cmd = (ICommandService) PlatformUI.getWorkbench().getService(
                ICommandService.class);
        State s = cmd.getCommand(RemoteSearchToogleHandler.ID).getState(
                RegistryToggleState.STATE_ID);
        return (Boolean) s.getValue();
    }

}

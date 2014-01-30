
package org.opentravel.schemas.stl2developer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

public class VariablesSourceProvider extends AbstractSourceProvider {
    public final static String SESSION_STATE = "org.opentravel.schemas.stl2Developer.variables.openFile";
    private final static String LOGGED_IN = "loggedIn";
    private final static String LOGGED_OUT = "loggedOut";
    boolean loggedIn;

    @Override
    public String[] getProvidedSourceNames() {
        return new String[] { SESSION_STATE };
    }

    @Override
    public Map<String, String> getCurrentState() {
        final Map<String, String> currentState = new HashMap<String, String>(1);
        final String cs = loggedIn ? LOGGED_IN : LOGGED_OUT;
        currentState.put(SESSION_STATE, cs);
        return currentState;
    }

    @Override
    public void dispose() {
    }

    public void setLoggedIn(final boolean loggedIn) {
        if (this.loggedIn == loggedIn) {
            return; // no change
        }
        this.loggedIn = loggedIn;
        final String currentState = loggedIn ? LOGGED_IN : LOGGED_OUT;
        fireSourceChanged(ISources.WORKBENCH, SESSION_STATE, currentState);
    }
}

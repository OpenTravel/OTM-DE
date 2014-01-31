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

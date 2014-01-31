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
package org.opentravel.schemas.testers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.PlatformUI;
import org.opentravel.schemas.navigation.INavigationService;

public class IsNavaigationEnabled extends PropertyTester {

    public static final String ID = "stl2Developer.navigation.tester";
    public static final String FORWARD_IS_EMPTY = "forwardIsEmpty";
    public static final String BACKWARD_IS_EMPTY = "backwardIsEmpty";

    public IsNavaigationEnabled() {
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (getNavigationController() == null) {
            return false;
        }
        if (FORWARD_IS_EMPTY.equals(property)) {
            return isForwardEmpty();
        } else if (BACKWARD_IS_EMPTY.equals(property)) {
            return isBackwardEmpty();
        }

        return false;
    }

    private boolean isBackwardEmpty() {
        return getNavigationController().getBackwardEvent().isEmpty();
    }

    private boolean isForwardEmpty() {
        return getNavigationController().getForwardEvent().isEmpty();
    }

    private INavigationService getNavigationController() {
        return (INavigationService) PlatformUI.getWorkbench().getService(INavigationService.class);
    }

    /**
     * @return the collection of all possible test properties.
     */
    public static Collection<String> getPropertiesNames() {
        List<String> props = new ArrayList<String>();
        props.add(ID + "." + FORWARD_IS_EMPTY);
        props.add(ID + "." + BACKWARD_IS_EMPTY);
        return props;
    }
}

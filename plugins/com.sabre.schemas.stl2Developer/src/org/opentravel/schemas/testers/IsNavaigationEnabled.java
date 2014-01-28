/*
 * Copyright (c) 2012, Sabre Inc.
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

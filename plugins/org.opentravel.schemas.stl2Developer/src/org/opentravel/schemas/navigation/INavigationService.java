
package org.opentravel.schemas.navigation;

import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.ISelectionListener;
import org.opentravel.schemas.navigation.DefaultNavigationService.IdSelectionChangedEvent;

public interface INavigationService extends ISelectionListener {

    public interface ISelectionHandler {

        void setSelection(TreeSet<IdSelectionChangedEvent> events);

    }

    public abstract Collection<SelectionChangedEvent> getBackwardEvent();

    public abstract Collection<SelectionChangedEvent> getForwardEvent();

    public abstract Collection<SelectionChangedEvent> getCurrentSelection();

    public abstract void goBackward();

    public abstract void goForward();

    public abstract void setSelectionHandler(ISelectionHandler handler);

}
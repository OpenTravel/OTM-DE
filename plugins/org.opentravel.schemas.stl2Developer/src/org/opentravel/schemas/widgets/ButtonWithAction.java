
package org.opentravel.schemas.widgets;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Pawel Jedruch
 * 
 */
public class ButtonWithAction implements IPropertyChangeListener, SelectionListener {

    private IAction action;
    private Button button;

    public ButtonWithAction(Composite parent, int style, IAction action) {
        button = new Button(parent, style);
        this.action = action;
        this.action.addPropertyChangeListener(this);
        button.addSelectionListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (IAction.ENABLED.equals(event.getProperty())) {
            button.setEnabled((Boolean) event.getNewValue());
        } else if (IAction.CHECKED.equals(event.getProperty())) {
            button.setSelection((Boolean) event.getNewValue());
        }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        action.setChecked(button.getSelection());
        action.run();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }
}

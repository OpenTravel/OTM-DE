
package org.opentravel.schemas.views.propertyview.editors;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.opentravel.schemas.widgets.PulldownButton;

/**
 * @author Pawel Jedruch
 * 
 */
public class PulldownButtonCellEditor extends FormCellEditor {

    private PulldownButton button;
    private List<Action> actions;

    public PulldownButtonCellEditor(List<Action> list) {
        super();
        this.actions = list;
    }

    @Override
    protected Control createControl(Composite parent) {
        button = new PulldownButton(parent, SWT.FLAT);
        for (Action a : actions) {
            button.getMenuManager().add(new ActionWrapper(a));
        }
        return button;
    }

    @Override
    protected Object doGetValue() {
        // since action can refresh PropertiesView (this will remove button) we need to check if it
        // still exist.
        // TODO: do not delete on refresh
        if (button.isDisposed())
            return null;
        return button.getText();

    }

    @Override
    protected void doSetFocus() {
        button.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        button.setText((String) value);
    }

    class ActionWrapper extends Action {

        private IAction action;

        public ActionWrapper(IAction action) {
            this.action = action;
        }

        @Override
        public void run() {
            this.action.run();
            PulldownButtonCellEditor.this.fireApplyEditorValue();
        }

        @Override
        public String getText() {
            return action.getText();
        }

        @Override
        public ImageDescriptor getImageDescriptor() {
            return action.getImageDescriptor();
        }

        @Override
        public boolean isChecked() {
            return action.isChecked();
        }

        @Override
        public int getAccelerator() {
            return action.getAccelerator();
        }

        @Override
        public String getActionDefinitionId() {
            return action.getActionDefinitionId();
        }

        @Override
        public String getDescription() {
            return action.getDescription();
        }

        @Override
        public ImageDescriptor getDisabledImageDescriptor() {
            return action.getDisabledImageDescriptor();
        }

        @Override
        public HelpListener getHelpListener() {
            return action.getHelpListener();
        }

        @Override
        public ImageDescriptor getHoverImageDescriptor() {
            return action.getHoverImageDescriptor();
        }

        @Override
        public String getId() {
            return action.getId();
        }

        @Override
        public IMenuCreator getMenuCreator() {
            return action.getMenuCreator();
        }

        @Override
        public int getStyle() {
            return action.getStyle();
        }

        @Override
        public String getToolTipText() {
            return action.getToolTipText();
        }

        @Override
        public boolean isEnabled() {
            return action.isEnabled();
        }

        @Override
        public boolean isHandled() {
            return action.isHandled();
        }

    }

}

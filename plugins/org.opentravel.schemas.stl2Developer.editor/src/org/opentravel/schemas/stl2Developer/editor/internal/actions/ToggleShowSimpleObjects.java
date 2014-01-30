
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.opentravel.schemas.stl2Developer.editor.i18n.Messages;
import org.opentravel.schemas.stl2Developer.editor.internal.Activator;
import org.opentravel.schemas.stl2Developer.editor.internal.Features;
import org.opentravel.schemas.stl2Developer.editor.view.DependeciesView;

/**
 * @author Pawel Jedruch
 * 
 */
public class ToggleShowSimpleObjects extends Action {
    private DependeciesView view;

    public ToggleShowSimpleObjects(DependeciesView view) {
        super("", IAction.AS_CHECK_BOX); //$NON-NLS-1$
        setText(getLabel());
        setImageDescriptor(Activator.getImageDescriptor("icons/disabled_simple_objects_16.png")); //$NON-NLS-1$
        this.view = view;
    }

    @Override
    public void run() {
        setText(getLabel());
        view.refresh();
    }

    private String getLabel() {
        if (isChecked()) {
            return Messages.ToggleShowSimpleObjects_Enable;
        } else {
            return Messages.ToggleShowSimpleObjects_Disable;
        }
    }

    @Override
    public boolean isChecked() {
        return !Features.isShowingSimpleObjectsAsUsedType();
    }

    @Override
    public void setChecked(boolean checked) {
        Features.setShowSimpleObjectsAsUsedType(!checked);
        super.setChecked(checked);
    }

}

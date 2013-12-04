package com.sabre.schemas.navigation;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;

import com.sabre.schemas.views.TypeView;

/**
 * This class is used as marked to notify the {@link ISelectionListener} about selection changed
 * caused by double-click.
 * 
 * Example usage:
 * 
 * <pre>
 * 
 * &#064;Override
 * public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 *     if (selection instanceof DoubleClickSelection) {
 *         // doubleClick
 *     } else {
 *         // default
 *     }
 * }
 * </pre>
 * 
 * @see TypeView#selectionChanged(org.eclipse.ui.IWorkbenchPart,
 *      org.eclipse.jface.viewers.ISelection)
 * @author Pawel Jedruch
 * 
 */
public class DoubleClickSelection extends StructuredSelection {

    public DoubleClickSelection(StructuredSelection selection) {
        super(selection.toList());
    }

}
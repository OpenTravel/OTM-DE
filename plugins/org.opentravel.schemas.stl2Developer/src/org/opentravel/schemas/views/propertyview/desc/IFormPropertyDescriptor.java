
package org.opentravel.schemas.views.propertyview.desc;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Pawel Jedruch
 * 
 */
public interface IFormPropertyDescriptor {

    /**
     * @param toolkit
     * @return
     */
    CellEditor createPropertyEditor(FormToolkit toolkit);

    /**
     * @return
     */
    GridData getCustomGridData();

}

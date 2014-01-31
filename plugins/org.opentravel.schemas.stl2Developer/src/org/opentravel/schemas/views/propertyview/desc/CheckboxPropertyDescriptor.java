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
package org.opentravel.schemas.views.propertyview.desc;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.opentravel.schemas.views.propertyview.editors.CheckboxCellEditor;

/**
 * @author Pawel Jedruch
 * 
 */
public class CheckboxPropertyDescriptor extends PropertyDescriptor implements
        IFormPropertyDescriptor, IReadonlyPropertyDescriptor {

    private boolean isReadonly = false;

    public CheckboxPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    @Override
    public CellEditor createPropertyEditor(FormToolkit toolkit) {
        return new CheckboxCellEditor(toolkit);
    }

    @Override
    public GridData getCustomGridData() {
        return GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).create();
    }

    @Override
    public boolean isReadonly() {
        return isReadonly;
    }

    public void setReadonly(boolean isReadonly) {
        this.isReadonly = isReadonly;
    }

}

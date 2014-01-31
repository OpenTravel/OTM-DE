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
import org.opentravel.schemas.views.propertyview.editors.FormTextCellEditor;

/**
 * @author Pawel Jedruch
 * 
 */
public class TextFormPropertyDescriptor extends PropertyDescriptor implements
        IFormPropertyDescriptor {

    private boolean isMultiline;

    public TextFormPropertyDescriptor(Object id, String displayName) {
        this(id, displayName, false);
    }

    public TextFormPropertyDescriptor(Object id, String displayName, boolean isMultiline) {
        super(id, displayName);
        this.isMultiline = isMultiline;
    }

    @Override
    public CellEditor createPropertyEditor(FormToolkit toolkit) {
        if (isMultiline) {
            return new FormTextCellEditor(toolkit, isMultiline);
        } else {
            return new FormTextCellEditor(toolkit);
        }
    }

    @Override
    public GridData getCustomGridData() {
        if (isMultiline) {
            return GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 60).grab(true, false).create();
        }
        return null;
    }

}

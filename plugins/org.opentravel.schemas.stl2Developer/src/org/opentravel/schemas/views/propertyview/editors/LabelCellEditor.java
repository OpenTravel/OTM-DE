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
package org.opentravel.schemas.views.propertyview.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Pawel Jedruch
 * 
 */
public class LabelCellEditor extends FormCellEditor {

    private FormToolkit toolkit;
    private Label label;

    public LabelCellEditor(FormToolkit toolkit) {
        super();
        this.toolkit = toolkit;
    }

    @Override
    protected Control createControl(Composite parent) {
        label = toolkit.createLabel(parent, "", SWT.WRAP);
        return label;
    }

    @Override
    protected Object doGetValue() {
        return label.getText();
    }

    @Override
    protected void doSetFocus() {
        label.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        label.setText((String) value);
    }

}

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
package org.opentravel.schemas.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.opentravel.schemas.utils.NativeDeleteFocusListener;

/**
 * @author Pawel Jedruch
 * 
 */
public class WidgetFactory {

    public static Text createText(Composite parent, int style) {
        Text text = new Text(parent, style);
        NativeDeleteFocusListener.attachListener(text);
        return text;
    }

    public static Combo createCombo(Composite parent, int style) {
        Combo combo = new Combo(parent, style);
        NativeDeleteFocusListener.attachListener(combo);
        return combo;
    }

    public static CCombo createCCombo(Composite parent, int style) {
        CCombo combo = new CCombo(parent, style | SWT.FLAT);
        return combo;
    }

    public static FormToolkit createFormToolkit(Display display) {
        return new NativeSupportFormToolkit(display);
    }

    static class NativeSupportFormToolkit extends FormToolkit {

        public NativeSupportFormToolkit(Display display) {
            super(display);
        }

        @Override
        public Text createText(Composite parent, String value, int style) {
            Text t = super.createText(parent, value, style);
            NativeDeleteFocusListener.attachListener(t);
            return t;
        }
    }

}

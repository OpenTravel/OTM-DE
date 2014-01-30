
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

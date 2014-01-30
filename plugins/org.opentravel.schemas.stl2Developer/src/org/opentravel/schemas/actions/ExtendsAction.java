
package org.opentravel.schemas.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.ExtensionSelectionWizard;

/**
 * Action that handles the selection and assignment of extensions for cores, business objects,
 * operations, and extension point facets.
 * 
 * @author S. Livezey
 */
public class ExtendsAction extends OtmAbstractAction {

    private Text extendsField;
    private Button extendsSelector;

    public ExtendsAction(MainWindow mainWindow, StringProperties props, Text extendsField,
            Button extendsSelector) {
        super(mainWindow, props);
        this.extendsField = extendsField;
        this.extendsSelector = extendsSelector;

        extendsSelector.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                run();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }

        });
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Node n = (Node) OtmRegistry.getTypeView().getCurrentNode();

        if (n != null) {
            ExtensionSelectionWizard wizard = new ExtensionSelectionWizard(n);
            wizard.postExtensionSelectionWizard(OtmRegistry.getActiveShell());
        }
    }

    /**
     * @see org.eclipse.jface.action.Action#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (extendsField != null)
            extendsField.setEnabled(enabled);
        if (extendsSelector != null)
            extendsSelector.setEnabled(enabled);
    }

}

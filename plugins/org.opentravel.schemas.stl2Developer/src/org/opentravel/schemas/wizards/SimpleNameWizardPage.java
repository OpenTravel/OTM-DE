
package org.opentravel.schemas.wizards;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Agnieszka Janowska
 * 
 */
public class SimpleNameWizardPage extends WizardPage implements ModifyListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleNameWizardPage.class);

    private String fieldLabel = "Name:";
    private String fieldToolTip = "Name of the new property";
    private Text name;
    private ArrayList<Text> names = new ArrayList<Text>();
    private final FormValidator formValidator;
    private final List<ModifyListener> modifyListeners;
    private String defaultValue;
    private int nameCount = 1; // number of names to allow them to enter

    protected SimpleNameWizardPage(final String pageName, final String title,
            final FormValidator validator) {
        super(pageName, title, null);
        formValidator = validator;
        modifyListeners = new LinkedList<ModifyListener>();
    }

    protected SimpleNameWizardPage(final String pageName, final String title, int count,
            final FormValidator validator) {
        super(pageName, title, null);
        formValidator = validator;
        modifyListeners = new LinkedList<ModifyListener>();
        setNameCount(count);
    }

    @Override
    public void createControl(final Composite parent) {
        final GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(layout);

        final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        final Label label = new Label(container, SWT.NULL);
        label.setText(getFieldLabel());

        if (nameCount == 1) {
            name = WidgetFactory.createText(container, SWT.BORDER | SWT.SINGLE);
            name.setToolTipText(getFieldToolTip());
            name.addModifyListener(this);
            name.setLayoutData(gd);
            if (defaultValue != null) {
                name.setText(getDefault());
            }
        } else
            for (int i = 0; i < nameCount; i++) {
                name = WidgetFactory.createText(container, SWT.BORDER | SWT.SINGLE);
                names.add(name);
                name.setToolTipText(getFieldToolTip());
                name.addModifyListener(this);
                name.setLayoutData(gd);
            }

        setControl(container);
        setPageComplete(false);
        container.redraw();
    }

    /**
     * @return the fieldLabel
     */
    public String getFieldLabel() {
        return fieldLabel;
    }

    /**
     * @param fieldLabel
     *            the fieldLabel to set
     */
    public void setFieldLabel(final String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    /**
     * @return the fieldToolTip
     */
    public String getFieldToolTip() {
        return fieldToolTip;
    }

    /**
     * @param fieldToolTip
     *            the fieldToolTip to set
     */
    public void setFieldToolTip(final String fieldToolTip) {
        this.fieldToolTip = fieldToolTip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
     */
    @Override
    public void modifyText(final ModifyEvent e) {
        notifyModifyListeners(e);
        if (!(e.widget instanceof Text))
            return;
        final String text = ((Text) e.widget).getText();
        String message = null;
        boolean complete = false;
        try {
            if (text != null && !text.isEmpty()) {
                formValidator.validate();
                complete = true;
            }
        } catch (final ValidationException ex) {
            message = ex.getMessage();
            LOGGER.debug("Validation output " + ex.getMessage());
        }
        setPageComplete(complete);
        setErrorMessage(message);
        getWizard().getContainer().updateButtons();
    }

    public String getText() {
        return name.getText();
    }

    public void addModifyListener(final ModifyListener listener) {
        modifyListeners.add(listener);
    }

    public void removeModifyListener(final ModifyListener listener) {
        modifyListeners.remove(listener);
    }

    private void notifyModifyListeners(final ModifyEvent event) {
        for (final ModifyListener listener : modifyListeners) {
            listener.modifyText(event);
        }
    }

    public void setDefault(final String def) {
        defaultValue = def;
    }

    public String getDefault() {
        return defaultValue;
    }

    /**
     * @return the nameCount
     */
    public int getNameCount() {
        return nameCount;
    }

    /**
     * @param nameCount
     *            the nameCount to set
     */
    public void setNameCount(int nameCount) {
        this.nameCount = nameCount;
    }

    /**
     * @return the names
     */
    public String[] getNames() {
        ArrayList<String> sl = new ArrayList<String>();
        for (Text field : names) {
            String text = field.getText();
            if (text != null && !text.isEmpty())
                sl.add(field.getText());
        }
        String[] strings = new String[sl.size()];
        for (int i = 0; i < sl.size(); i++)
            strings[i] = sl.get(i);
        return strings;
    }

}

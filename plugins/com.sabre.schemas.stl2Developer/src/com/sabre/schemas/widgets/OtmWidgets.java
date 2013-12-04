/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.sabre.schemas.controllers.OtmActions;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.Messages;

public class OtmWidgets {

    // See properties/messages.properties for definitions
    // Spinners
    public static int[] repeatCount = { 40, 41 };
    public static int[] minLenSpinner = { 42, 43 };
    public static int[] maxLenSpinner = { 44, 45 };
    public static int[] fractionDigitsSpinner = { 46, 47 };
    public static int[] totalDigitsSpinner = { 48, 49 };

    // Buttons
    public static int[] newComponent = { 32, 33, SWT.PUSH };
    public static int[] newProperty = { 34, 35, SWT.PUSH };
    public static int[] deleteProperty = { 36, 37, SWT.PUSH };
    public static int[] mandatoryButton = { 38, 39, SWT.CHECK };
    public static int[] validateButton = { 56, 57, SWT.PUSH };
    public static int[] listButton = { 58, 59, SWT.CHECK };
    public static int[] goTo = { 78, 79, SWT.PUSH };

    // Combo selectors
    public static int[] roleCombo = { 50, 51 };
    public static int[] contextCombo = { 52, 53 };

    public static int[] typeSelector = { 76, 77, SWT.PUSH };
    public static int[] contextSelector = { 90, 91, SWT.PUSH };
    public static int[] extendsSelector = { 92, 93, SWT.PUSH };
    public static int[] clearExtends = { 94, 95, SWT.PUSH };

    public static int[] commitButton = { 80, 81, SWT.PUSH };
    public static int[] incVersionButton = { 82, 83, SWT.PUSH };
    public static int[] manageButton = { 98, 99, SWT.PUSH };

    protected static OtmActions actionHandler;
    protected static OtmHandlers otmHandlers;
    protected static int widgetIndex = 0; // identifier of what widget it is

    /**
     * A class to simplify passing arguments to formatter.
     * 
     * @author Dave Hollander
     * 
     */
    public class SpinnerData {
        public int lable;
        public int toolTip;
        public int min = 0;
        public int max = 100000;
        public int increment = 1;
        public int pageIncrement = 100;

        public SpinnerData() {
            min = 0;
            max = 100000;
            increment = 1;
            pageIncrement = 100;
        }

        public void setRange(final int minV, final int maxV, final int inc, final int page) {
            min = minV;
            max = maxV;
            increment = inc;
            pageIncrement = page;
        }
    }

    /**
     * Widget constructor. Loads up the widget hash table with all the widgets
     * 
     * @param actions
     */
    public OtmWidgets(final OtmActions actions, final OtmHandlers handlers) {
        actionHandler = actions;
        otmHandlers = handlers;
    }

    /**
     * Clear all the fields belonging to the parentNode composite.
     * 
     * @param composite
     */
    public void clearTextFields(final Composite composite) {
        for (final Control c : composite.getChildren()) {
            if (!c.isDisposed() && c instanceof Text) {
                final Text t = (Text) c;
                t.setText("");
                t.setEnabled(false);
            }
        }
    }

    /**
     * Place a button at the current location in the screen layout.
     * 
     * @param parentNode
     *            Composite
     * @param properties
     *            - one of the definitions from the class containing indexes into
     *            messages.properties
     * @param event
     *            - value returned from actionGetEventID()
     * @param handler
     * @return - the button is returned so that layout data can be applied
     */
    public Button formatButton(final Composite parent, final int[] properties, final int event,
            final SelectionAdapter handler) {

        final Button button = new Button(parent, properties[2]);
        assignButtonEvent(button, null, event, Messages.getString("OtmW." + properties[0]),
                Messages.getString("OtmW." + properties[1]), handler);
        return button;
    }

    public void assignButtonEvent(final Button button, final Node n, final int event,
            final String label, final String toolTip, final SelectionAdapter handler) {
        final OtmEventData wd = new OtmEventData();
        wd.label = label;
        wd.businessEvent = event; // OtmActions event number
        wd.actionHandler = actionHandler; // handler call back

        wd.widget = button;
        wd.node = n;
        button.setText(wd.label);
        button.setData(wd);
        button.setToolTipText(toolTip);
        button.setEnabled(false);

        if (handler != null) {
            button.addSelectionListener(handler);
        }
    }

    public Spinner formatSpinner(final Composite parent, final int[] properties, final int event,
            final SpinnerData sd, final SelectionAdapter handler) {

        final OtmEventData wd = new OtmEventData();
        wd.label = Messages.getString("OtmW." + properties[0]); //$NON-NLS-1$
        final String toolTip = Messages.getString("OtmW." + properties[1]); //$NON-NLS-1$

        wd.businessEvent = event;
        wd.actionHandler = actionHandler;

        final Label label = new Label(parent, SWT.TRAIL);
        label.setText(wd.label);

        final Spinner spinner = new Spinner(parent, SWT.BORDER);
        wd.widget = spinner;
        spinner.setMinimum(sd.min);
        spinner.setMaximum(sd.max);
        spinner.setSelection(0);
        spinner.setIncrement(sd.increment);
        spinner.setPageIncrement(sd.pageIncrement);
        spinner.setToolTipText(toolTip);
        spinner.setEnabled(false);
        spinner.setData(wd);

        spinner.addSelectionListener(handler);
        wd.widget = spinner;
        return spinner;
    }

    /**
     * Post a sash in the current grid. The 1st field does not get a visible sash, width-1 fields
     * do.
     * 
     * @param parent
     * @param width
     * @param color
     */
    public void postSash(final Composite parent, int width, Color color) {
        final Sash sash1 = new Sash(parent, SWT.HORIZONTAL);
        final Sash sash = new Sash(parent, SWT.HORIZONTAL);
        final GridData sGD = new GridData();
        sGD.horizontalSpan = width - 1;
        sGD.horizontalAlignment = SWT.FILL;
        sGD.grabExcessHorizontalSpace = true;
        sGD.grabExcessVerticalSpace = true;
        sash.setLayoutData(sGD);
        sash.setBackground(color);
        sash.setEnabled(false);
    }

    public void postSpinner(final Spinner sd, final int value, final boolean enabled) {
        sd.setSelection(value);
        sd.setEnabled(enabled);
    }

    public Combo formatCombo(final FormToolkit toolkit, final Composite parent,
            final int[] properties, final int event, final ModifyListener handler,
            final boolean editable) {
        final OtmEventData wd = new OtmEventData();
        wd.label = Messages.getString("OtmW." + properties[0]); //$NON-NLS-1$
        final String toolTip = Messages.getString("OtmW." + properties[1]); //$NON-NLS-1$
        wd.businessEvent = event;
        wd.actionHandler = actionHandler;

        // Label label = new Label(parent, SWT.NONE);
        // label.setText(wd.label);
        // label.setBackground(toolkit.getColors().getBackground());
        int mode = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
        if (!editable) {
            mode |= SWT.READ_ONLY;
        }
        final Combo combo = WidgetFactory.createCombo(parent, mode);
        // combo.setItems(values);
        combo.setText("label");
        combo.setToolTipText(toolTip);
        combo.setData(wd);

        combo.addModifyListener(handler); // set dirty flag
        return combo;
    }

    /**
     * Format and post a combo box on the parentNode composite.
     * 
     * @param editable
     *            TODO
     * 
     * @return - the combo widget
     */
    public Combo formatCombo(final Composite parent, final int[] properties, final String[] values,
            final int event, final ModifyListener handler, final boolean editable) {
        final OtmEventData wd = new OtmEventData();
        wd.label = Messages.getString("OtmW." + properties[0]); //$NON-NLS-1$
        final String toolTip = Messages.getString("OtmW." + properties[1]); //$NON-NLS-1$
        wd.businessEvent = event;
        wd.actionHandler = actionHandler;

        new Label(parent, SWT.NONE).setText(wd.label);
        int mode = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
        if (!editable) {
            mode |= SWT.READ_ONLY;
        }
        final Combo combo = WidgetFactory.createCombo(parent, mode);
        combo.setItems(values);
        combo.setText("label");
        combo.setToolTipText(toolTip);
        combo.setData(wd);

        combo.addModifyListener(handler); // set dirty flag
        return combo;
    }

}

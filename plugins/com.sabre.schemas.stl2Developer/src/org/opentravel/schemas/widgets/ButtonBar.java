/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Naive implementation of button bar, similar to tool bar, but with proper buttons TODO: think
 * about CoolBar based implementation (but then it changes the appearance of the buttons, do we want
 * that?)
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ButtonBar extends Composite {

    private static final int DEFAULT_BUTTON_WIDTH = 50;
    private static final int DEFAULT_BUTTON_HEIGHT = SWT.DEFAULT;

    private final RowLayout layout;
    private int buttonsWidth = DEFAULT_BUTTON_WIDTH;
    private int buttonsHeight = DEFAULT_BUTTON_HEIGHT;

    /**
     * @param parent
     * @param style
     */
    public ButtonBar(final Composite parent, final int style) {
        super(parent, style);
        layout = new RowLayout();
        layout.wrap = false;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.spacing = 5;
        layout.center = true;
        layout.pack = true;
        if ((style & SWT.VERTICAL) == SWT.VERTICAL) {
            layout.type = SWT.VERTICAL;
        } else {
            layout.type = SWT.HORIZONTAL;
        }
        setLayout(layout);
        setBackground(parent.getBackground());
    }

    /**
     * @return
     */
    public List<Button> getButtons() {
        final Control[] children = getChildren();
        final List<Button> items = new ArrayList<Button>(children.length);
        for (final Control c : children) {
            if (c instanceof Button) {
                items.add((Button) c);
            }
        }
        return items;
    }

    @Override
    public void dispose() {
        for (final Control c : getChildren()) {
            c.dispose();
        }
        super.dispose();
    }

    private boolean shouldBeLaidOut(final Button button) {
        final int check = (button.getStyle() & SWT.CHECK) | (button.getStyle() & SWT.RADIO)
                | (button.getStyle() & SWT.TOGGLE);
        return check == 0;
    }

    public void layoutButtons() {
        for (final Control button : getChildren()) {
            if (button instanceof Button) {
                if (shouldBeLaidOut((Button) button)) {
                    button.setLayoutData(new RowData(buttonsWidth, buttonsHeight));
                }
            }
        }
        pack(true);
    }

    /**
     * @param i
     * @return
     */
    public Button getButton(final int i) {
        Button item;
        try {
            item = getButtons().get(i);
        } catch (final IndexOutOfBoundsException e) {
            item = null;
        }
        return item;
    }

    /**
     * @return the buttonsSize
     */
    public int getButtonsWidth() {
        return buttonsWidth;
    }

    /**
     * @param buttonsSize
     *            the buttonsSize to set
     */
    public void setButtonsWidth(final int buttonsSize) {
        buttonsWidth = buttonsSize;
    }

    /**
     * @return the buttonsHeight
     */
    public int getButtonsHeight() {
        return buttonsHeight;
    }

    /**
     * @param buttonsHeight
     *            the buttonsHeight to set
     */
    public void setButtonsHeight(final int buttonsHeight) {
        this.buttonsHeight = buttonsHeight;
    }

}

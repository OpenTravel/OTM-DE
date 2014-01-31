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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.widgets.OtmHandlers.ButtonSelectionHandler;

/**
 * Maintains a set of buttons and associated table editors.
 * 
 * @author Dave Hollander
 * 
 */
public class TableEditorButtonSet {
    private final ArrayList<TableEditor> tblEditors = new ArrayList<TableEditor>();
    private final ArrayList<Button> tblButtons = new ArrayList<Button>();
    private final Table table;
    private final int event;
    private final String label;
    private final String toolTip;
    private final ButtonSelectionHandler handler;
    private final OtmWidgets widgets;

    /**
     * Set up a set of table editor buttons.
     * 
     * @param widgets
     *            - the OtmWidget class used to register buttons with the event handler
     * @param table
     *            - table onto which the buttons will be placed.
     * @param eventNumber
     *            - OtmActions defined event number for when button is selected
     * @param properties
     *            - OtmWidgets properties definitions.
     * @param handler
     *            - Button selection handler
     * 
     *            example: buttonSet = new TableEditorButtonSet(widgets, table,
     *            OtmActions.typeSelector(), OtmWidgets.typeSelector, handlers.new
     *            ButtonSelectionHandler());
     */
    public TableEditorButtonSet(final OtmWidgets widgets, final Table table, final int eventNumber,
            final int[] properties, final ButtonSelectionHandler handler) {
        this.table = table;
        event = eventNumber;
        label = Messages.getString("OtmW." + properties[0]);
        toolTip = Messages.getString("OtmW." + properties[1]);
        this.handler = handler;
        this.widgets = widgets;
    }

    /**
     * WARNING - you must remove the table editor when you clear the table.
     * 
     * @param node
     *            - set into event data for the button
     * @param table
     * @param item
     * @return
     * @return
     */
    public Button addButton(final Node n, final TableItem item, final int column) {
        final Button button = new Button(table, SWT.PUSH);
        widgets.assignButtonEvent(button, n, event, label, toolTip, handler);
        button.setEnabled(true);
        button.pack();

        final TableEditor editor = new TableEditor(table);
        editor.minimumWidth = 20;
        // editor.minimumWidth = button.getSize ().x; // doesn't work for me
        editor.horizontalAlignment = SWT.RIGHT;
        editor.setEditor(button, item, column);
        tblButtons.add(button);
        tblEditors.add(editor);
        return button;
    }

    /**
     * Removed buttons and editors from the table. Must be done whenever the table is cleared or
     * items destroyed.
     */
    public void clearButtons() {
        for (final TableEditor ed : tblEditors) {
            ed.dispose();
        }
        for (final Button b : tblButtons) {
            b.dispose();
        }
        tblEditors.clear();
        tblButtons.clear();
    }

}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.widgets;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.controllers.OtmActions;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.controllers.NodeUtils;
import com.sabre.schemas.node.properties.AttributeNode;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.ElementReferenceNode;
import com.sabre.schemas.node.properties.SimpleAttributeNode;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * {@link LibraryTablePoster} decorated with 'Go To' buttons on types
 * 
 * @author Agnieszka Janowska
 * 
 */
public class LibraryTablePosterWithButtons extends LibraryTablePoster {

    private final TableEditorButtonSet buttonSet;

    public LibraryTablePosterWithButtons(final Table table, final MainWindow mainWindow) {
        super(table, mainWindow.getColorProvider());
        MainController mc = OtmRegistry.getMainController();
        // Create a table editor button set to manage buttons added to the table.
        buttonSet = new TableEditorButtonSet(mc.getWidgets(), table, OtmActions.typeSelector(),
                OtmWidgets.typeSelector, mc.getHandlers().new ButtonSelectionHandler());

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemas.stl2developer.LibraryTablePoster#postTableRow(com.sabre.schemas.node.Node)
     */
    @Override
    protected TableItem postTableRow(final Node n) {
        final TableItem item = super.postTableRow(n);

        if (!n.isInheritedProperty()
                && n.isEditable()
                && (n instanceof AttributeNode || n instanceof ElementNode
                        || n instanceof SimpleAttributeNode || n instanceof ElementReferenceNode)) {
            Button button = buttonSet.addButton(n, item, 2); // Put typeSelection buttons on the
                                                             // row.
            if (NodeUtils.checker(n).isPatch().existInPreviousVersions().get()) {
                button.setEnabled(false);
            }
        }
        return item;
    }

    @Override
    public void clearTable() {
        super.clearTable();
        buttonSet.clearButtons();
    }

}

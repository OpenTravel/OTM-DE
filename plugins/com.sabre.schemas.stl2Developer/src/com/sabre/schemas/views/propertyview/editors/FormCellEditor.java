/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.views.propertyview.editors;

import org.eclipse.jface.viewers.CellEditor;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class FormCellEditor extends CellEditor {

    @Override
    public void deactivate() {
        // Do nothing since control should be always visible
    }

}

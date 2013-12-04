/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.trees.documentation;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.sabre.schemas.node.DocumentationNode;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class DocumentationTreeLabelProvider extends LabelProvider {

    @Override
    public String getText(final Object element) {
        if (element instanceof DocumentationNode) {
            return ((DocumentationNode) element).getLabel();
        }
        return "Unknown object type";
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof DocumentationNode) {
            return ((DocumentationNode) element).getImage();
        }
        return null;
    }

}

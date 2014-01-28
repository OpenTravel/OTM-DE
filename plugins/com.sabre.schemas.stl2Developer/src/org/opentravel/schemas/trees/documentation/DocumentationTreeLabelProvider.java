/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.trees.documentation;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.DocumentationNode;

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

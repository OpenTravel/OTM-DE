/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.views.example;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Images;

/**
 * @author Pawel Jedruch
 * 
 */
public class ErrorExampleModel extends ExampleModel {

    /**
     * @param child
     */
    public ErrorExampleModel(Node child) {
        super(child);
        this.setLabelProvider(new LabelProvider() {

            @Override
            public Image getImage(Object element) {
                return Images.getImageRegistry().get(Images.ErrorDecoration);
            }

            @Override
            public String getText(Object element) {
                return getNode().getName();
            }

        });
    }

}

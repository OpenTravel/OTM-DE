/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.views.example;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.Images;

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

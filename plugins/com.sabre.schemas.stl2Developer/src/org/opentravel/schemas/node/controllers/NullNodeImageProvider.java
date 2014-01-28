/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node.controllers;

import org.eclipse.swt.graphics.Image;

/**
 * @author Agnieszka Janowska
 * 
 */
public final class NullNodeImageProvider implements NodeImageProvider {

    @Override
    public Image getImage() {
        return null;
    }
}

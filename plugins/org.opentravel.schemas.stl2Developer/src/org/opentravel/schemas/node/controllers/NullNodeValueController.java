/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Agnieszka Janowska
 * 
 */
public final class NullNodeValueController implements NodeValueController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NullNodeValueController.class);

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public void setValue(String value) {
        LOGGER.warn("Ignored attempt to set value " + value + " using a null value controller.");
    }
}

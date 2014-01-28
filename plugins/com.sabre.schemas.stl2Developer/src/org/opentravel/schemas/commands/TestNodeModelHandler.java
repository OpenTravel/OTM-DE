/*
 * Copyright (c) 2012, Sabre Inc.
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.node.NodeModelTestUtils;

public class TestNodeModelHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        NodeModelTestUtils.testNodeModel();
        return null;
    }

}

/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.trees.repository;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.views.RepositoryView;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class AbstractSearchToogleHandler extends AbstractHandler {

    @Override
    public final Object execute(ExecutionEvent event) throws ExecutionException {
        RepositoryView repoView = (RepositoryView) HandlerUtil.getActivePart(event);
        Command command = event.getCommand();
        boolean newValue = !HandlerUtil.toggleCommandState(command);
        update(repoView, newValue);
        return null;
    }

    protected abstract void update(RepositoryView repoView, boolean newValue);

}

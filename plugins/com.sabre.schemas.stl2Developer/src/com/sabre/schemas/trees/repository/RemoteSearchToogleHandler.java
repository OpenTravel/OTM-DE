/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.trees.repository;

import com.sabre.schemas.views.RepositoryView;

/**
 * @author Pawel Jedruch
 * 
 */
public class RemoteSearchToogleHandler extends AbstractSearchToogleHandler {

    public static final String ID = "com.sabre.schemas.commands.repository.remoteSearch";

    @Override
    protected void update(RepositoryView repoView, boolean newValue) {
        repoView.getRepositoryMenus().setIncludeRepositorySearch(newValue);
    }

}

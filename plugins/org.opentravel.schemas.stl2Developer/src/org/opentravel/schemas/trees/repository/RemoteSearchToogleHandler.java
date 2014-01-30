
package org.opentravel.schemas.trees.repository;

import org.opentravel.schemas.views.RepositoryView;

/**
 * @author Pawel Jedruch
 * 
 */
public class RemoteSearchToogleHandler extends AbstractSearchToogleHandler {

    public static final String ID = "org.opentravel.schemas.commands.repository.remoteSearch";

    @Override
    protected void update(RepositoryView repoView, boolean newValue) {
        repoView.getRepositoryMenus().setIncludeRepositorySearch(newValue);
    }

}

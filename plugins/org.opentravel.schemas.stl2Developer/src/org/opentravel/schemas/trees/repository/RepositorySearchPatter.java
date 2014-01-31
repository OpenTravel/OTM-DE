/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.trees.repository;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;

import org.opentravel.schemacompiler.repository.RepositoryItem;

/**
 * @author Pawel Jedruch
 * 
 */
public class RepositorySearchPatter extends PatternFilter {

    private List<RepositoryItem> filteredRepositories;
    private boolean searchRepostiroy;

    @Override
    protected synchronized boolean isLeafMatch(Viewer viewer, Object element) {
        if (searchRepostiroy) {
            return searchRepository(element);
        } else {
            return super.isLeafMatch(viewer, element);
        }
    }

    @Override
    protected boolean isParentMatch(Viewer viewer, Object element) {
        if (!searchRepostiroy) {
            if (element instanceof RepositoryNode) {
                RepositoryNode r = (RepositoryNode) element;
                if (r.wasVisited()) {
                    return super.isParentMatch(viewer, element);
                }
            }
            return false;// super.isParentMatch(viewer, element);
        } else {
            return super.isParentMatch(viewer, element);
        }
    }

    private boolean searchRepository(Object element) {
        if (element instanceof RepositoryItemNode) {
            return exist(filteredRepositories, ((RepositoryItemNode) element).getItem());
        }
        return false;
    }

    private boolean exist(List<RepositoryItem> filteredRepositories2, RepositoryItem element) {
        for (RepositoryItem ri : filteredRepositories2) {
            if (isEqual(ri, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param filteredRepositories
     */
    public synchronized void setVisibleRepositories(List<RepositoryItem> filteredRepositories) {
        this.filteredRepositories = filteredRepositories;

    }

    private boolean isEqual(RepositoryItem r1, RepositoryItem r2) {
        if (isNotEqual(r1.getBaseNamespace(), r2.getBaseNamespace())
                || isNotEqual(r1.getFilename(), r2.getFilename())
                || isNotEqual(r1.getLibraryName(), r2.getLibraryName())
                || isNotEqual(r1.getLockedByUser(), r2.getLockedByUser())
                || isNotEqual(r1.getNamespace(), r2.getNamespace())
                || isNotEqual(r1.getState(), r2.getState())
                || isNotEqual(r1.getStatus(), r2.getStatus())
                || isNotEqual(r1.getVersion(), r2.getVersion())
                || isNotEqual(r1.getVersionScheme(), r2.getVersionScheme()))
            return false;
        else
            return true;
    }

    private <T> boolean isNotEqual(T o1, T o2) {
        if (o1 != null) {
            return !o1.equals(o2);
        } else if (o2 == null) {
            return false;
        }
        return true;
    }

    public void searchRepository(boolean b) {
        searchRepostiroy = b;
    }

}

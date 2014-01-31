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
package org.opentravel.schemas.trees.context;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.ContextNode;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ContextTreeContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(final Object element) {
        return getChildren(element);
    }

    @Override
    public Object[] getChildren(final Object element) {
        Object[] toRet = null;
        if (element instanceof ContextNode) {
            ContextNode node = (ContextNode) element;
            List<ContextNode> navChildren = node.getChildren();
            toRet = navChildren != null ? navChildren.toArray() : null;
        }
        return toRet;
    }

    @Override
    public boolean hasChildren(final Object element) {
        if (element instanceof ContextNode) {
            ContextNode node = (ContextNode) element;
            return !node.getChildren().isEmpty();
        }
        return false;
    }

    @Override
    public Object getParent(final Object element) {
        if (element instanceof ContextNode) {
            ContextNode node = (ContextNode) element;
            return node.getParent();
        }
        return null;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object old_input, final Object new_input) {
    }

}

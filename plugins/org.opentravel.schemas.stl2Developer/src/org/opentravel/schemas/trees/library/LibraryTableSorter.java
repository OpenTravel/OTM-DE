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
package org.opentravel.schemas.trees.library;

import org.eclipse.jface.viewers.ViewerSorter;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.PropertyNode;

public class LibraryTableSorter extends ViewerSorter {
    @Override
    public int category(final Object element) {
        final INode n = (INode) element;
        if (n instanceof PropertyNode) {
            if (n.getName().equals("Attribute")) {
                return 0;
            } else if (n.getName().equals("Element")) {
                return 2;
            } else if (n.getName().equals("Indicator")) {
                return 1;
            }
        }
        return 0;
    }
}

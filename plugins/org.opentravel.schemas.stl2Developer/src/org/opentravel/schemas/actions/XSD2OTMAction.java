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
package org.opentravel.schemas.actions;

import java.util.List;

import org.opentravel.schemas.navigation.GlobalSelectionProvider;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;

public class XSD2OTMAction extends AbstractGlobalSelectionAction {

    public XSD2OTMAction() {
        super("action.xsd2otm", GlobalSelectionProvider.NAVIGATION_VIEW);
        setText("Convert to OTM");
    }

    @Override
    protected boolean isEnabled(Object object) {
        return getSourceValue() != null;
    }

    @Override
    public LibraryNode getSourceValue() {
        @SuppressWarnings("unchecked")
        List<Node> nodes = (List<Node>) super.getSourceValue();
        if (nodes.size() == 1) {
            Node node = nodes.get(0);
            if (node instanceof LibraryNode) {
                LibraryNode lib = (LibraryNode) node;
                if (lib.isXSDSchema()) {
                    return lib;
                }
            }
        }
        return null;
    }

    @Override
    public void run() {
        super.run();
    }

}

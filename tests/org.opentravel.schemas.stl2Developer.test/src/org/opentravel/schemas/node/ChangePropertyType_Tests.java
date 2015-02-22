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
/**
 * 
 */
package org.opentravel.schemas.node;

import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeModelTestUtils;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ChangePropertyType_Tests {
    private final static Logger LOGGER = LoggerFactory.getLogger(ChangePropertyType_Tests.class);

    ModelNode model = null;
    TestNode nt = new NodeTesters().new TestNode();
    LoadFiles lf = new LoadFiles();
    LibraryTests lt = new LibraryTests();

    @Test
    public void changePropertyTypeTests() throws Exception {
        MainController mc = new MainController();

        lf.loadTestGroupA(mc);
        for (LibraryNode ln : Node.getAllLibraries()) {
            ln.visitAllNodes(nt);
            visitAllProperties(ln);
        }
        NodeModelTestUtils.testNodeModel();
    }

    public void visitAllProperties(LibraryNode ln) {
        VisitProperty visitor = new VisitProperty();
        for (Node n : ln.getDescendants_TypeUsers()) {
            if (n instanceof PropertyNode)
                visitor.visit(n);
        }
    }

    class VisitProperty implements NodeVisitor {

        @Override
        public void visit(INode n) {
            if (!(n instanceof PropertyNode))
                return;
            PropertyNode p = (PropertyNode) n;
            switch (p.getPropertyType()) {
                case ROLE:
                case ENUM_LITERAL:
                case SIMPLE:
                case ALIAS:
                    return;
            }

            for (PropertyNodeType t : PropertyNodeType.values()) {
                switch (t) {
                    case ELEMENT:
                    case ID_REFERENCE:
                    case ATTRIBUTE:
                    case INDICATOR:
                    case INDICATOR_ELEMENT:
                        // LOGGER.debug("Changing " + p.getPropertyType() + " "
                        // + n.getNameWithPrefix() + " to " + t);
                        p = p.changePropertyRole(t);
                        nt.visit(p);
                        break;
                }
                p.getOwningComponent().visitAllNodes(nt);
            }
            // LOGGER.debug("Tested " + n + "\n");
        }
    }
}

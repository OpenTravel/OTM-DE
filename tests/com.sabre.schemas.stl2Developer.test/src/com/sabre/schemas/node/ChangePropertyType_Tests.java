/**
 * 
 */
package com.sabre.schemas.node;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.Node.NodeVisitor;
import com.sabre.schemas.node.Node_Tests.TestNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.testUtils.LoadFiles;

/**
 * @author Dave Hollander
 * 
 */
public class ChangePropertyType_Tests {
    private final static Logger LOGGER = LoggerFactory.getLogger(ChangePropertyType_Tests.class);

    ModelNode model = null;
    TestNode nt = new Node_Tests().new TestNode();
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

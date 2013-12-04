/**
 * 
 */
package com.sabre.schemas.node;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.testUtils.LoadFiles;

/**
 * @author Dave Hollander
 * 
 */
// TODO - test deleting the source and the clone with full visit node afterwards
public class Clone_Tests {
    private static final Logger LOGGER = LoggerFactory.getLogger(Clone_Tests.class);

    ModelNode model = null;
    Node_Tests tt = new Node_Tests();

    @Test
    public void cloneTest() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        model = mc.getModelNode();

        LibraryNode source = lf.loadFile5Clean(mc);
        // test cloning within library.
        source.setEditable(true);
        cloneMembers(source, source);

        LOGGER.debug("Testing cloning properties.");
        for (Node ne : source.getDescendants_NamedTypes())
            cloneProperties(ne);
        tt.visitAllNodes(source);

        // commented some libs out to keep the total time down
        LibraryNode target = lf.loadEmpty(mc);
        lf.loadTestGroupA(mc);
        lf.loadXfileDsse(mc); // use an xsd source
        lf.loadXfile3(mc);

        // lf.loadFilePNRB(mc);
        // lf.loadXfile1(mc);
        // lf.loadXfile2(mc);

        lf.cleanModel();
        Node.getModelNode().visitAllNodes(tt.new TestNode());

        LOGGER.debug("\n");
        LOGGER.debug("Testing cloning to new library.");
        for (LibraryNode ln : Node.getAllLibraries()) {
            if (ln.getNamespace().equals(target.getNamespace()))
                continue;
            if (ln.isBuiltIn())
                continue; // these have errors
            ln.setEditable(true);
            cloneMembers(ln, target);
            LOGGER.debug("Cloned members of " + ln);
        }
        LOGGER.debug("Done cloning - starting final check.");
        Node.getModelNode().visitAllNodes(tt.new TestNode());
    }

    private int cloneMembers(LibraryNode ln, LibraryNode target) {
        int mbrCount = 0, equCount = 0;
        Node clone;

        for (Node n : ln.getDescendants_NamedTypes()) {
            // Assert.assertNotNull(n.cloneNew(null)); // no library, so it will fail node tests
            equCount = countEquivelents(n);
            if (n.isService())
                continue;
            if (ln == target)
                clone = n.clone("_COPY");
            else
                clone = n.clone(target, null);
            if (clone != null) {
                tt.visitAllNodes(clone);
                if (countEquivelents(clone) != equCount)
                    LOGGER.debug("Equ error on " + clone);
            }
            mbrCount++;
        }
        return mbrCount;
    }

    private int countEquivelents(Node n) {
        for (Node p : n.getDescendants()) {
            if (p instanceof ElementNode) {
                return ((TLProperty) p.getTLModelObject()).getEquivalents().size();
            }
        }
        return 0;
    }

    private void cloneProperties(Node n) {
        if (n.isNamedType())
            for (Node p : n.getDescendants()) {
                if (p instanceof PropertyNode) {
                    if (p.getParent() instanceof ComponentNode)
                        ((ComponentNode) p.getParent()).addProperty(p.clone());
                    else
                        LOGGER.debug(p + "has invalid class of parent.");
                }
            }
        tt.visitAllNodes(n);
    }

}

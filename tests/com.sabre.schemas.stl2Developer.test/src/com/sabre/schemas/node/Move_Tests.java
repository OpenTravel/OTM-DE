/**
 * 
 */
package com.sabre.schemas.node;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.ContextController;
import com.sabre.schemas.controllers.ContextModelManager;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.Node_Tests.TestNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.ModelCheckUtils;

/**
 * @author Dave Hollander
 * 
 */
public class Move_Tests {
    private static final Logger LOGGER = LoggerFactory.getLogger(Move_Tests.class);

    ModelNode model = null;
    TestNode nt = new Node_Tests().new TestNode();
    LibraryNode target = null;
    LibraryNode xsdTarget = null;
    ContextModelManager cmm = null;
    ContextController cc = null;

    @Test
    public void moveTest() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        model = mc.getModelNode();

        cc = mc.getContextController();

        lf.loadTestGroupA(mc);
        target = lf.loadFilePNRB(mc);
        xsdTarget = lf.loadXfile3(mc);
        Assert.assertNotNull(target);
        for (LibraryNode ln : model.getUserLibraries()) {
            LOGGER.debug("");
            LOGGER.debug("Starting testing on library " + ln);
            // ln.visitAllNodes(nt);
            moveProperties(ln);
            // ln.visitAllNodes(nt);
            moveObjects(ln, target);
            ln.visitAllNodes(nt);
        }

    }

    /**
     * Move all named objects from source library to target.
     * 
     * @param src
     * @param target
     */
    private void moveObjects(LibraryNode src, LibraryNode target) {
        if (!src.isTLLibrary())
            return;
        int libContexts0 = cc.getAvailableContextIds(src).size();
        int libContexts1, libContexts2, libContexts3;
        for (Node n : src.getDescendants_NamedTypes()) {
            libContexts1 = cc.getAvailableContextIds(target).size();
            if (n.getName().equals("PaymentCard"))
                LOGGER.debug("HERE" + n);
            // 1. why are the counts off? what is missing or double counted?
            // 2. why does payment card fail? It is the test, not the models.
            src.moveMember(n, target);

            LOGGER.debug("Moved " + n);
            // target.visitAllNodes(nt);
            if (!ModelCheckUtils.checkModelCounts(target.getLibrary()))
                ModelCheckUtils.compareModels(target.getLibrary());
            libContexts2 = cc.getAvailableContextIds(target).size();
            Assert.assertTrue(libContexts1 <= libContexts2);
        }
        libContexts3 = cc.getAvailableContextIds(target).size();
        LOGGER.debug("Moved all members of " + src);
    }

    // TODO - what about context? How to test???

    /**
     * Move all the properties around.
     * 
     * @param ln
     */
    private void moveProperties(LibraryNode ln) {
        for (Node n : ln.getDescendants_NamedTypes()) {
            if (n.isCoreObject()) {
                FacetNode fn = (FacetNode) ((CoreObjectNode) n).getSummaryFacet();
                moveProperty(fn, (FacetNode) ((CoreObjectNode) n).getDetailFacet());
            }
            if (n.isBusinessObject()) {
                FacetNode fn = ((BusinessObjectNode) n).getSummaryFacet();
                moveProperty(fn, (FacetNode) ((BusinessObjectNode) n).getDetailFacet());
            }
        }
    }

    // moves within facet then from summary to detailed.
    private void moveProperty(FacetNode fn, FacetNode destF) {
        // kids include aliases, elements, attributes, indicators
        int kidCnt = fn.getChildren().size();
        if (kidCnt > 0) {
            INode kn = fn.getChildren().get(kidCnt - 1);
            if (kn instanceof PropertyNode) {
                PropertyNode pn = (PropertyNode) kn;
                for (int i = kidCnt; i > 1; i--) {
                    pn.moveProperty(1);
                }
                nt.visit(fn);

                // it should be at top now...
                Assert.assertFalse(pn.moveProperty(1));
                for (int i = kidCnt; i > 1; i--) {
                    pn.moveProperty(2);
                    Assert.assertEquals(kidCnt, fn.getChildren().size());
                }

                // Move it between facets
                pn.moveProperty(destF);
                pn.moveProperty(fn);
                nt.visit(fn);
                Assert.assertEquals(kidCnt, fn.getChildren().size());
                nt.visit(fn);
            }
        }
    }

}

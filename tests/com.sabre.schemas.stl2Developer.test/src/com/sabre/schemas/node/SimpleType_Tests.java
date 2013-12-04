/**
 * 
 */
package com.sabre.schemas.node;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.testUtils.LoadFiles;

/**
 * @author Dave Hollander
 * 
 */
public class SimpleType_Tests {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleType_Tests.class);

    @Test
    public void checkSimpleTypes() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        int loadedCnt = lf.loadTestGroupA(mc);
        Assert.assertEquals(5, loadedCnt);

        int libCnt = 0;
        for (LibraryNode ln : Node.getAllLibraries()) {
            checkCounts(ln);
            visitSimpleTypes(ln);
            libCnt++;
        }
        Assert.assertEquals(7, libCnt);

        // enable when importer all fixed up.
        visitSimpleTypes(lf.loadXfile1(mc));

    }

    private void checkCounts(LibraryNode lib) {
        int simpleCnt = 0;
        for (Node type : lib.getDescendants_NamedTypes()) {
            if (type.isSimpleType()) {
                simpleCnt++;
            }
        }
        Assert.assertEquals(simpleCnt, lib.getNamedSimpleTypes().size());
    }

    private void visitSimpleTypes(LibraryNode ln) {
        for (SimpleTypeNode st : ln.getNamedSimpleTypes()) {
            Assert.assertNotNull(st);
            Assert.assertNotNull(st.getLibrary());
            Assert.assertNotNull(st.getBaseType());
            Assert.assertNotNull(st.getTypeClass());
            if (st.isTypeUser()) {
                Assert.assertNotNull(st.getTypeClass().getTypeNode());
            }

            // Check names
            Assert.assertFalse(st.getName().isEmpty());

            // Type Names
            String an = st.getTypeName();
            String tn = st.getTypeClass().getTypeNode().getName();
            // Get Type Name modifies answers from Implied nodes.
            if (!(st.getTypeClass().getTypeNode() instanceof ImpliedNode)) {
                if (!an.equals(tn)) {
                    LOGGER.debug("Name error: " + an + " =? " + tn);
                }
                Assert.assertEquals(tn, an);
            }

            // // Check type namespace
            String anp = st.getAssignedPrefix();
            String tnp = st.getTypeClass().getTypeNode().getNamePrefix();
            if (!anp.isEmpty()) // Prefixes can be empty, but empty is changed by code
                Assert.assertEquals(tnp, anp);
        }
    }

}

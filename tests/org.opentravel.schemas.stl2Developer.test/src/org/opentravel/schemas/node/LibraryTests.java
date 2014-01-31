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

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultLibraryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
// TODO - validate assertion: Every new library is obliged to register itself
// within the
// NamespaceHandler.

public class LibraryTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTests.class);

    private Node_Tests testVisitor;
    private MainController mc;
    private LoadFiles lf;

    @Before
    public void beforeEachTest() {
        mc = new MainController();
        lf = new LoadFiles();
        testVisitor = new Node_Tests();
    }

    @Test
    public void checkBuiltIns() {
        for (INode n : Node.getAllLibraries()) {
            Assert.assertTrue(n instanceof LibraryNode);
            visitLibrary((LibraryNode) n);
        }
    }

    @Test
    public void checkLibraries() throws Exception {

        LibraryNode l1 = lf.loadFile1(mc);
        visitLibrary(l1);

        testNewWizard((ProjectNode) l1.getParent());

        lf.loadFile2(mc);
        lf.loadFile3(mc);
        lf.loadFile4(mc);
        lf.loadFile5(mc);

        for (LibraryNode ln : Node.getAllLibraries()) {
            visitLibrary(ln);
        }

        // If not editable,most of the other tests will fail.
        for (LibraryNode ln : Node.getAllUserLibraries()) {
            Assert.assertTrue(ln.isEditable());
            Assert.assertFalse(ln.getPath().isEmpty());
            Assert.assertTrue(ln.getNamespace().equals(ln.getTLaLib().getNamespace()));
            Assert.assertTrue(ln.getNamePrefix().equals(ln.getTLaLib().getPrefix()));
        }

        // Make sure we can create new empty libraries as used by wizard
        LibraryNode newLib = new LibraryNode(l1.getParent());
        Assert.assertNotNull(newLib);

        for (LibraryNode ln : Node.getAllLibraries()) {
            removeAllMembers(ln);
        }
    }

    private void removeAllMembers(LibraryNode ln) {
        for (Node n : ln.getDescendants_NamedTypes()) {
            ln.removeMember(n); // May change type assignments!
        }
        Assert.assertTrue(ln.getDescendants_NamedTypes().size() < 1);
    }

    /**
     * Check the library. Checks library structures then all children. Asserts error if the library
     * is empty!
     * 
     * @param ln
     */
    protected void visitLibrary(LibraryNode ln) {
        if (ln.isXSDSchema()) {
            Assert.assertNotNull(ln.getGeneratedLibrary());
            Assert.assertTrue(ln.hasGeneratedChildren());
        }
        Assert.assertTrue(ln.getChildren().size() > 1);
        Assert.assertTrue(ln.getDescendants_NamedTypes().size() > 1);

        if (ln.getName().equals("OTA2_BuiltIns_v2.0.0")) {
            Assert.assertEquals(85, ln.getDescendants_NamedTypes().size());
        }

        if (ln.getName().equals("XMLSchema")) {
            Assert.assertEquals(20, ln.getDescendants_NamedTypes().size());
        }

        Assert.assertTrue(ln.getChildren().size() == ln.getNavChildren().size());
        Assert.assertTrue(ln.getParent() instanceof ProjectNode);

        Assert.assertNotNull(ln.getTLaLib());

        Assert.assertFalse(ln.getName().isEmpty());
        Assert.assertFalse(ln.getNamespace().isEmpty());
        Assert.assertFalse(ln.getNamePrefix().isEmpty());

        if (ln.isTLLibrary()) {
            Assert.assertFalse(ln.getContextIds().isEmpty());
        }

        Assert.assertFalse(ln.getPath().isEmpty());

        for (Node n : ln.getChildren()) {
            visitNode(n);
        }
    }

    public void visitNode(Node n) {
        // LOGGER.debug("Visit Node: " + n + " of type " + n.getClass().getSimpleName());
        Assert.assertNotNull(n);
        Assert.assertNotNull(n.getParent());
        Assert.assertNotNull(n.getLibrary());
        Assert.assertNotNull(n.modelObject);
        Assert.assertNotNull(n.getTLModelObject());
        Assert.assertTrue(n.getTypeClass().verifyAssignment());

        Assert.assertNotNull(n.getTypeClass());
        if (n.isTypeUser()) {
            // LOGGER.debug("Visit Node: " + n + " of type " + n.getClass().getSimpleName());
            boolean x = n.getTypeClass().verifyAssignment();
            // Resolver may not have run
            // Assert.assertNotNull(n.getType());
            Assert.assertEquals(n.getType(), n.getAssignedType());
        }

        Assert.assertFalse(n.getName().isEmpty());
        for (Node nn : n.getChildren()) {
            visitNode(nn);
        }
    }

    // Emulates the logic within the wizard
    private void testNewWizard(ProjectNode parent) {
        final String InitialVersionNumber = "0_1";
        final String prefix = "T1T";
        final DefaultLibraryController lc = new DefaultLibraryController(mc);
        final LibraryNode ln = new LibraryNode(parent);
        final String baseNS = parent.getNamespace();
        final ProjectNode pn = mc.getProjectController().getDefaultProject();
        final int libCnt = pn.getLibraries().size();
        // Strip the project file
        String path = pn.getPath();
        path = new File(path).getParentFile().getPath();
        path = new File(path, "Test.otm").getPath();
        final String name = "Test";

        String ns = ln.getNsHandler().createValidNamespace(baseNS, InitialVersionNumber);
        ln.getTLaLib().setNamespace(ns);
        ln.getTLaLib().setPrefix(prefix);
        ln.setPath(path);
        ln.setName(name);
        Assert.assertEquals(name, ln.getName());
        LOGGER.debug("Done setting up for wizard complete.Path = " + path);

        // This code runs after the wizard completes
        LibraryNode resultingLib = lc.createNewLibraryFromPrototype(ln);
        LOGGER.debug("new library created. Cnt = " + pn.getLibraries().size());
        Assert.assertEquals(libCnt + 1, pn.getLibraries().size());

        // Leave something in it
        NewComponent_Tests nct = new NewComponent_Tests();
        nct.createNewComponents(resultingLib);

        // resultingLib.getRepositoryDisplayName();
        visitLibrary(resultingLib);
    }

    @Test
    public void checkStatus() {
        LibraryNode ln = lf.loadFile5Clean(mc);

        Assert.assertEquals(ln.getEditStatus(), NodeEditStatus.NOT_EDITABLE);
        Assert.assertFalse(ln.getEditStatusMsg().isEmpty());
        Assert.assertFalse(ln.isManaged());
        Assert.assertFalse(ln.isLocked());
        Assert.assertFalse(ln.isInProjectNS());
        Assert.assertTrue(ln.isMajorVersion());
        Assert.assertTrue(ln.isMinorOrMajorVersion());
        Assert.assertFalse(ln.isPatchVersion());

        ln.setNamespace(ln.getProject().getNamespace() + "test/v1_2_3");
        Assert.assertNotNull(ln.getNsHandler());
        String n = ln.getNamespace();
        Assert.assertFalse(ln.getNamespace().isEmpty());
        n = ln.getNSExtension();
        Assert.assertTrue(ln.getNSExtension().equals("test"));
        n = ln.getNSVersion();
        Assert.assertTrue(ln.getNSVersion().equals("1.2.3"));
        Assert.assertTrue(ln.isPatchVersion());

        ln.setNamespace(ln.getProject().getNamespace() + "test/v1_2");
        n = ln.getNSVersion();
        Assert.assertTrue(ln.getNSVersion().equals("1.2.0"));
        Assert.assertTrue(ln.isMinorOrMajorVersion());
    }

    @Test
    public void checkNS() {

    }
}

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

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.FamilyNode;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TestTypes;

/**
 * @author Dave Hollander
 * 
 */
public class Family_Tests {
    ModelNode model = null;
    TestTypes tt = new TestTypes();

    Node_Tests nt = new Node_Tests();
    LoadFiles lf = new LoadFiles();
    LibraryTests lt = new LibraryTests();
    MockLibrary ml = null;
    LibraryNode ln = null;
    MainController mc;
    DefaultProjectController pc;
    ProjectNode defaultProject;

    @Before
    public void beforeAllTests() {
        mc = new MainController();
        ml = new MockLibrary();
        pc = (DefaultProjectController) mc.getProjectController();
        defaultProject = pc.getDefaultProject();
    }

    @Test
    public void mockFamilyTest() {
        ln = ml.createNewLibrary("http://www.sabre.com/test", "test", defaultProject);
        Node simpleNav = null;

        // Find the simple type node.
        for (Node n : ln.getChildren()) {
            if (n.getName().equals("Simple Objects"))
                simpleNav = n;
        }
        Assert.assertNotNull(simpleNav);

        ml.addSimpleTypeToLibrary(ln, "A");
        ml.addSimpleTypeToLibrary(ln, "B");
        ml.addSimpleTypeToLibrary(ln, "C");
        Assert.assertEquals(3, simpleNav.getChildren().size());

        // These three should create families
        ml.addSimpleTypeToLibrary(ln, "A");
        ml.addSimpleTypeToLibrary(ln, "B");
        ml.addSimpleTypeToLibrary(ln, "C");
        Assert.assertEquals(3, simpleNav.getChildren().size());

    }

    @Test
    public void FamilyTests() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        model = mc.getModelNode();

        lf.loadTestGroupA(mc);
        for (LibraryNode ln : model.getUserLibraries()) {
            int beforeCnt = ln.getDescendants_NamedTypes().size();
            tt.visitAllNodes(ln);

            siblingTest(ln);
            tt.visitAllNodes(ln);
            Assert.assertEquals(beforeCnt, ln.getDescendants_NamedTypes().size());

            deleteFamilies(ln);
            // You can't visit nodes because the deleted families may be used as a type on one of
            // the remaining types. tt.visitAllNodes(ln);
        }
    }

    private void deleteFamilies(LibraryNode ln) {
        ArrayList<FamilyNode> families = new ArrayList<FamilyNode>();
        for (INode nav : ln.getChildren())
            for (Node n : nav.getChildren()) {
                if (n.isFamily())
                    families.add((FamilyNode) n);
            }
        for (FamilyNode f : families) {
            Node parent = f.getParent();
            Assert.assertNotNull(parent);
            Assert.assertTrue(f instanceof FamilyNode);
            Assert.assertTrue(f.getChildren().size() > 0);
            ArrayList<Node> kids = new ArrayList<Node>(f.getChildren());
            for (INode k : kids) {
                k.removeFromLibrary();
            }
            Assert.assertEquals(0, f.getChildren().size());
            Assert.assertNull(f.getParent());

        }
    }

    private void siblingTest(LibraryNode ln) {
        Node nn1 = null;
        Node nn2 = null;
        Node nn3 = null;
        int siblingCount = 0;

        for (Node n : ln.getDescendants_NamedTypes()) {
            tt.visitTypeNode(n);
            Assert.assertFalse(n instanceof FamilyNode);
            siblingCount = 0;
            if (n.getParent().isFamily())
                siblingCount = n.getParent().getChildren().size();
            nn1 = n.clone(); // makes duplication in library
            nn2 = n.clone();
            nn3 = n.clone();
            INode parent = n.getParent();
            if (nn1 != null) {
                tt.visitTypeNode(nn1); // skip services
                Assert.assertTrue(nn1.getParent() instanceof FamilyNode);
                nn1.removeFromLibrary();
            }
            if (nn2 != null) {
                tt.visitTypeNode(nn2); // skip services
                Assert.assertTrue(nn2.getParent() instanceof FamilyNode);
                nn2.removeFromLibrary();
            }
            if (nn3 != null) {
                tt.visitTypeNode(nn3); // skip services
                Assert.assertTrue(nn3.getParent() instanceof FamilyNode);
                n.replaceWith(nn3);
                n = nn3;
                tt.visitTypeNode(n);
            }
            if (siblingCount > 0) {
                Assert.assertEquals(siblingCount, n.getParent().getChildren().size());
                Assert.assertTrue(n.getParent() instanceof FamilyNode);
            }
        }

    }
}

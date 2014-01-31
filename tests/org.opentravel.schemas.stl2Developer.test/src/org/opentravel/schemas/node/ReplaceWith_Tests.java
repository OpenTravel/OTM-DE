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

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TestTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ReplaceWith_Tests {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceWith_Tests.class);

    ModelNode model = null;
    TestTypes tt = new TestTypes();

    // Use purpose built objects to test specific behaviors.
    @Test
    public void ReplaceTypesTest() throws Exception {
        DefaultProjectController pc;
        MainController mc = new MainController();
        MockLibrary ml = new MockLibrary();
        pc = (DefaultProjectController) mc.getProjectController();
        ProjectNode defaultProject = pc.getDefaultProject();
        NewComponent_Tests nc = new NewComponent_Tests();
        LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        nc.createNewComponents(ln);

        tt.visitAllNodes(ln);

        SimpleTypeNode simple = null;
        EnumerationClosedNode closed = null;
        VWA_Node vwa = null;
        CoreObjectNode core = null, core2 = null;
        EnumerationOpenNode open = null;
        BusinessObjectNode bo = null, bo2 = null;
        ServiceNode svc = null;
        ExtensionPointNode ex = null;
        for (Node n : ln.getDescendants_NamedTypes()) {
            if (n instanceof SimpleTypeNode)
                simple = (SimpleTypeNode) n;
            if (n instanceof EnumerationClosedNode)
                closed = (EnumerationClosedNode) n;
            if (n instanceof EnumerationOpenNode)
                open = (EnumerationOpenNode) n;
            if (n instanceof VWA_Node)
                vwa = (VWA_Node) n;
            if (n instanceof CoreObjectNode)
                core = (CoreObjectNode) n;
            if (n instanceof BusinessObjectNode)
                bo = (BusinessObjectNode) n;
            if (n instanceof ExtensionPointNode)
                ex = (ExtensionPointNode) n;
            if (n instanceof ServiceNode)
                svc = (ServiceNode) n;
        }
        core2 = (CoreObjectNode) core.clone();
        core2.setName("core2");
        core.setExtendsType(core2);
        bo2 = (BusinessObjectNode) bo.clone();
        bo2.setName("bo2");
        bo2.setExtendsType(bo);

        replaceProperties(bo, core2, core);
        replaceProperties(bo2, core, core2);

        replaceProperties(svc, simple, core);
        replaceProperties(ex, simple, core);
        replaceProperties(bo, simple, core);
        replaceProperties(core, simple, core);
        replaceProperties(vwa, simple, core);
        tt.visitAllNodes(ln);
    }

    private void replaceProperties(Node owner, Node p1, Node p2) {
        // Set then replace all the properties of a BO.
        for (Node n : owner.getDescendants_TypeUsers()) {
            n.setAssignedType(p1);
        }
        p1.replaceTypesWith(p2, owner.getLibrary());
    }

    @Test
    public void ReplaceTest() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        model = mc.getModelNode();

        LibraryNode l5 = lf.loadFile5(mc);
        l5.setEditable(true);
        LibraryNode l1 = lf.loadFile1(mc);
        l1.setEditable(true);
        // tt.visitAllNodes(l5);
        // tt.visitAllNodes(l1);
        int beforeCnt1 = l1.getDescendants_NamedTypes().size();
        int beforeCnt5 = l5.getDescendants_NamedTypes().size();

        replaceMembers(l1, l1);
        replaceMembers(l1, l5);

        tt.visitAllNodes(l1);
        tt.visitAllNodes(l5);
        Assert.assertEquals(beforeCnt1, l1.getDescendants_NamedTypes().size());
        Assert.assertEquals(beforeCnt5, l5.getDescendants_NamedTypes().size());
    }

    @Test
    public void swap() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        model = mc.getModelNode();

        LibraryNode l5 = lf.loadFile5(mc);
        l5.setEditable(true);
        LibraryNode l1 = lf.loadFile1(mc);
        l1.setEditable(true);
        tt.visitAllNodes(l1);
        tt.visitAllNodes(l5);

        swap(l1, l5);

        tt.visitAllNodes(l1);
        tt.visitAllNodes(l5);
    }

    @Test
    public void CombinedTest() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        model = mc.getModelNode();

        LibraryNode l5 = lf.loadFile5(mc);
        l5.setEditable(true);
        LibraryNode l1 = lf.loadFile1(mc);
        l1.setEditable(true);
        // tt.visitAllNodes(l5);
        // tt.visitAllNodes(l1);
        int beforeCnt1 = l1.getDescendants_NamedTypes().size();
        int beforeCnt5 = l5.getDescendants_NamedTypes().size();

        replaceMembers(l1, l5);
        replaceMembers(l5, l1);
        tt.visitAllNodes(l1);
        tt.visitAllNodes(l5);

        Assert.assertEquals(beforeCnt1, l1.getDescendants_NamedTypes().size());
        Assert.assertEquals(beforeCnt5, l5.getDescendants_NamedTypes().size());

        swap(l1, l5);
        tt.visitAllNodes(l1);
        tt.visitAllNodes(l5);
        // Assert.assertEquals(beforeCnt5, l5.getDescendants_NamedTypes().size());
    }

    /**
     * For all named types in target,
     * 
     * @param ls
     * @param lt
     */
    private void replaceMembers(LibraryNode ls, LibraryNode lt) {
        // Sort the list so that the order is consistent with each test.
        List<Node> targets = lt.getDescendants_NamedTypes();
        Collections.sort(targets, lt.new NodeComparable());
        List<Node> sources = ls.getDescendants_NamedTypes();
        Collections.sort(sources, ls.new NodeComparable());
        int cnt = sources.size();

        // Replace types with one pseudo-randomly selected from target library.
        for (Node n : targets) {
            if (n.getWhereUsed().size() > 0) {
                // Note - many of these will not be allowed.
                n.replaceTypesWith(sources.get(--cnt));
                // LOGGER.debug(" replaced " + n + " with " + sources.get(cnt));
                Assert.assertTrue(sources.get(cnt).getLibrary() != null);
                Assert.assertTrue(n.getLibrary() != null);
            }
            if (cnt <= 0)
                cnt = sources.size();
        }
        // LOGGER.debug("Replaced " + sources.size() + " - " + cnt);
    }

    /**
     * Remove type providers from source library and replace with its counter parts in the target
     * library.
     * 
     * @param source
     *            - library will get smaller
     * @param target
     *            - library will stay same size but have replaced types.
     */
    private void swap(LibraryNode source, LibraryNode target) {
        // Now, replace the nodes within their structures.
        int i = 1;
        for (Node n : target.getDescendants_NamedTypes()) {
            if (n.isService())
                continue;
            Node lsNode = NodeFinders.findTypeProviderByQName(
                    new QName(source.getNamespace(), n.getName()), source);
            // n.replaceWith(lsNode);
            if (lsNode != null) {
                // If the swap fails, the node n will not be i a library but will be used as a type.
                // Re-add it to a library.
                // If swap is successful, n will be disconnected from library and will fail tests.
                n.swap(lsNode);
                tt.visitTypeNode(lsNode);
            }
        }
    }

}

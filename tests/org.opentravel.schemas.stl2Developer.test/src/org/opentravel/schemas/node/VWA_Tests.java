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

import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.modelObject.SimpleAttributeMO;
import org.opentravel.schemas.modelObject.SimpleFacetMO;
import org.opentravel.schemas.modelObject.ValueWithAttributesAttributeFacetMO;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.SimpleFacetNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.Node_Tests.TestNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;

import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * @author Dave Hollander
 * 
 */
public class VWA_Tests {
    ModelNode model = null;
    MockLibrary mockLibrary = null;
    LibraryNode ln = null;
    MainController mc;
    DefaultProjectController pc;
    ProjectNode defaultProject;
    TestNode tn = new Node_Tests().new TestNode();

    @Before
    public void beforeEachTest() {
        mc = new MainController();
        mockLibrary = new MockLibrary();
        pc = (DefaultProjectController) mc.getProjectController();
        defaultProject = pc.getDefaultProject();
    }

    @Test
    public void changeToVWA() {
        MockLibrary ml = new MockLibrary();
        MainController mc = new MainController();
        DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
        ProjectNode defaultProject = pc.getDefaultProject();

        LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");
        CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "co");
        VWA_Node tVwa = null, vwa = ml.addVWA_ToLibrary(ln, "vwa");
        int typeCount = ln.getDescendants_NamedTypes().size();

        tVwa = (VWA_Node) core.changeToVWA();
        checkVWA(tVwa);
        tVwa = (VWA_Node) vwa.changeToVWA();
        checkVWA(tVwa);

        tn.visit(ln);
        Assert.assertEquals(typeCount, ln.getDescendants_NamedTypes().size());
    }

    @Test
    public void VWATest() throws Exception {
        MainController thisModel = new MainController();
        LoadFiles lf = new LoadFiles();
        model = thisModel.getModelNode();

        // test the lib with only vwa
        LibraryNode vwaLib = lf.loadFile3(thisModel);
        for (Node vwa : vwaLib.getDescendants_NamedTypes()) {
            Assert.assertTrue(vwa.isValueWithAttributes());
            Assert.assertTrue(vwa instanceof VWA_Node);
            checkVWA((VWA_Node) vwa);
        }
        vwaLib.close();

        // test all libs
        lf.loadTestGroupA(thisModel);
        for (LibraryNode ln : model.getUserLibraries()) {
            List<Node> types = ln.getDescendants_NamedTypes();
            for (Node n : types) {
                if (n.isValueWithAttributes())
                    checkVWA((VWA_Node) n);
            }
        }
        // make sure we can build one.
        makeVwa("TEST_V1", vwaLib);
    }

    @Test
    public void getTypeQName() {
        ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
        ln.setEditable(true);
        VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "VWA_Test");
        QName typeQname = vwa.getTLTypeQName();
        SimpleAttributeNode sa = (SimpleAttributeNode) vwa.getSimpleFacet().getSimpleAttribute();
        Assert.assertNotNull(sa);

        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        vwa.setSimpleType(aType);
        typeQname = sa.getTLTypeQName();
        Assert.assertEquals("date", typeQname.getLocalPart());
        Assert.assertEquals(Node.XSD_NAMESPACE, typeQname.getNamespaceURI());
    }

    @Test
    public void typeSetting() {
        ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
        ln.setEditable(true);

        // Check explicitly set by code.
        VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "VWA_Test");
        SimpleAttributeNode sa = (SimpleAttributeNode) vwa.getSimpleFacet().getSimpleAttribute();
        Assert.assertNotNull(sa);
        Assert.assertNotNull(sa.getTypeNode());
        Assert.assertNotNull(vwa.getAttributeFacet().getChildren().get(0));

        // TODO - add test to assure that read in VWAs are typed
        // TODO - add test to assure that read in VWAs in a library chain are typed.
    }

    @Test
    public void mockVWATest() {
        ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
        ln.setEditable(true);
        VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "VWA_Test");
        Assert.assertEquals("VWA_Test", vwa.getName());
        Assert.assertTrue(vwa.getSimpleFacet() instanceof SimpleFacetNode);
        SimpleFacetNode sfn = vwa.getSimpleFacet();
        Assert.assertTrue(vwa.getSimpleType() != null);
        Assert.assertTrue(sfn.getSimpleAttribute().getType() == vwa.getSimpleType());

        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        Assert.assertFalse(vwa.setAssignedType(aType));
        Assert.assertFalse(sfn.setAssignedType(aType));
        Assert.assertTrue(vwa.setSimpleType(aType));
        Assert.assertTrue(vwa.getSimpleType() == aType);

        String OTA_NS = "http://opentravel.org/common/v02";
        Node oType = NodeFinders.findNodeByName("CodeList", OTA_NS);
        Assert.assertTrue(vwa.setSimpleType(oType));
        Assert.assertTrue(vwa.getSimpleType() == oType);
        Assert.assertTrue(vwa.getTypeClass().verifyAssignment());
        Assert.assertTrue(vwa.getSimpleFacet().getTypeClass().verifyAssignment());
        Assert.assertTrue(vwa.getSimpleFacet().getSimpleAttribute().getTypeClass()
                .verifyAssignment());
    }

    private void checkVWA(VWA_Node vwa) {
        Assert.assertNotNull(vwa.getLibrary());

        // must have only two children
        Assert.assertTrue(vwa.getChildren().size() == 2);

        // Simple Facet (SimpleFacetMO)
        Assert.assertNotNull(vwa.getSimpleFacet());
        INode sf = vwa.getSimpleFacet();
        Assert.assertTrue(sf.getModelObject() instanceof SimpleFacetMO);

        // Owns one property of type SimpleAttributeMO
        Assert.assertTrue(vwa.getSimpleFacet().getChildren().size() == 1);
        Node sp = vwa.getSimpleFacet().getChildren().get(0);
        Assert.assertTrue(sp instanceof PropertyNode);
        Assert.assertTrue(sp.getModelObject() instanceof SimpleAttributeMO);
        Assert.assertTrue(sp.getType() != null);
        Assert.assertFalse(sp.getType().getName().isEmpty());
        Assert.assertTrue(sp.getTypeClass().getTypeOwner() == sp);
        Assert.assertTrue(sp.getLibrary() == vwa.getLibrary());

        // ValueWithAttributesAttributeFacetMO
        Node af = vwa.getAttributeFacet();
        Assert.assertNotNull(af);
        Assert.assertTrue(af.getModelObject() instanceof ValueWithAttributesAttributeFacetMO);
        Assert.assertTrue(af.getLibrary() == vwa.getLibrary());

        for (Node ap : af.getChildren()) {
            Assert.assertTrue(ap instanceof PropertyNode);
            // could be attribute or indicator
            // Assert.assertTrue(ap.getModelObject().isSimpleAssignable());
            // May be null if resolver has not run.
            // Assert.assertTrue(ap.getType() != null);
            // May be empty if resolver has not run.
            // Assert.assertFalse(ap.getType().getName().isEmpty());
            Assert.assertTrue(ap.getTypeClass().getTypeOwner() == ap);
            Assert.assertTrue(ap.getLibrary() == vwa.getLibrary());
        }
    }

    protected void makeVwa(String name, LibraryNode ln) {
        TLValueWithAttributes tlVWA = new TLValueWithAttributes();
        tlVWA.setName(name);
        for (int attCnt = 1; attCnt < 100; attCnt++) {
            TLAttribute tlA = new TLAttribute();
            tlA.setName(name + "_a" + attCnt);
            tlVWA.addAttribute(tlA);
        }

        VWA_Node v = (VWA_Node) NodeFactory.newComponent(tlVWA);
        ln.addMember(v);
        checkVWA(v);
    }
}

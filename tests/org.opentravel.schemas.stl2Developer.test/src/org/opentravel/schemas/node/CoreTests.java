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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.modelObject.SimpleAttributeMO;
import org.opentravel.schemas.modelObject.SimpleFacetMO;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.SimpleFacetNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.Node_Tests.TestNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class CoreTests {
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
    public void changeToCore() {
        MockLibrary ml = new MockLibrary();
        MainController mc = new MainController();
        DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
        ProjectNode defaultProject = pc.getDefaultProject();

        LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");
        CoreObjectNode tco = null, core = ml.addCoreObjectToLibrary(ln, "co");
        VWA_Node vwa = ml.addVWA_ToLibrary(ln, "vwa");
        int typeCount = ln.getDescendants_NamedTypes().size();

        tco = (CoreObjectNode) core.changeToCoreObject();
        checkCore(tco);
        tco = (CoreObjectNode) vwa.changeToCoreObject();
        checkCore(tco);

        tn.visit(ln);
        Assert.assertEquals(typeCount, ln.getDescendants_NamedTypes().size());
    }

    @Test
    public void coreTest() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        // model = mc.getModelNode();

        LibraryNode coreLib = lf.loadFile4(mc);
        for (Node core : coreLib.getDescendants_NamedTypes()) {
            if (core.isCoreObject())
                checkCore((CoreObjectNode) core);
        }
    }

    @Test
    public void mockCoreTest() {
        ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
        CoreObjectNode core = mockLibrary.addCoreObjectToLibrary(ln, "CoreTest");
        Assert.assertEquals("CoreTest", core.getName());
        Assert.assertTrue(core.getSimpleFacet() instanceof SimpleFacetNode);
        SimpleFacetNode sfn = core.getSimpleFacet();
        Assert.assertTrue(core.getSimpleType() != null);
        Assert.assertTrue(sfn.getSimpleAttribute().getType() == core.getSimpleType());

        Node aType = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
        Assert.assertFalse(core.setAssignedType(aType));
        Assert.assertFalse(sfn.setAssignedType(aType));
        // works - Assert.assertTrue(sfn.getSimpleAttribute().setAssignedType(aType));
        Assert.assertTrue(core.setSimpleType(aType));
        Assert.assertTrue(core.getSimpleType() == aType);
    }

    private void checkCore(CoreObjectNode core) {
        Assert.assertNotNull(core.getLibrary());
        Assert.assertTrue(core.isCoreObject());
        Assert.assertTrue(core instanceof CoreObjectNode);

        // must have 6 children
        Assert.assertEquals(6, core.getChildren().size());

        // Simple Facet (SimpleFacetMO)
        Assert.assertNotNull(core.getSimpleFacet());
        INode sf = core.getSimpleFacet();
        Assert.assertTrue(sf.getModelObject() instanceof SimpleFacetMO);

        // Owns one property of type SimpleAttributeMO
        Assert.assertTrue(core.getSimpleFacet().getChildren().size() == 1);
        Node sp = core.getSimpleFacet().getChildren().get(0);
        Assert.assertTrue(sp instanceof PropertyNode);
        Assert.assertTrue(sp.getModelObject() instanceof SimpleAttributeMO);
        Assert.assertTrue(sp.getType() != null);
        Assert.assertFalse(sp.getType().getName().isEmpty());
        Assert.assertTrue(sp.getTypeClass().getTypeOwner() == sp);
        Assert.assertTrue(sp.getLibrary() == core.getLibrary());

        Assert.assertNotNull(core.getSimpleFacet());
        Assert.assertNotNull(core.getSummaryFacet());
        Assert.assertNotNull(core.getDetailFacet());

        for (Node property : core.getSummaryFacet().getChildren()) {
            Assert.assertTrue(property instanceof PropertyNode);
            Assert.assertTrue(property.getType() != null);
            // Assert.assertTrue(property.getTypeClass().getTypeOwner() == property);
            Assert.assertTrue(property.getLibrary() == core.getLibrary());
        }
        for (Node property : core.getDetailFacet().getChildren()) {
            Assert.assertTrue(property instanceof PropertyNode);
            Assert.assertTrue(property.getType() != null);
            Assert.assertFalse(property.getType().getName().isEmpty());
            Assert.assertTrue(property.getTypeClass().getTypeOwner() == property);
            Assert.assertTrue(property.getLibrary() == core.getLibrary());
        }

        Assert.assertNotNull(core.getSimpleListFacet());
        Assert.assertNotNull(core.getDetailListFacet());
    }
    // Problem = getType(IndicatorMO) is null
}

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
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.Node_Tests.TestNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class BusinessObjectTests {
    TestNode tn = new Node_Tests().new TestNode();

    @Test
    public void businessObjectTest() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();

        LibraryNode lib = lf.loadFile4(mc);
        for (Node bo : lib.getDescendants_NamedTypes()) {
            if (bo.isBusinessObject())
                checkBO((BusinessObjectNode) bo);
        }
    }

    @Test
    public void changeToBO() {
        MockLibrary ml = new MockLibrary();
        MainController mc = new MainController();
        DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
        ProjectNode defaultProject = pc.getDefaultProject();

        LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        BusinessObjectNode tbo = null, bo = ml.addBusinessObjectToLibrary(ln, "bo");
        CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "co");
        VWA_Node vwa = ml.addVWA_ToLibrary(ln, "vwa");
        int typeCount = ln.getDescendants_NamedTypes().size();

        tbo = (BusinessObjectNode) core.changeToBusinessObject();
        checkBO(tbo);
        tbo = (BusinessObjectNode) vwa.changeToBusinessObject();
        checkBO(tbo);

        tn.visit(ln);
        Assert.assertEquals(typeCount, ln.getDescendants_NamedTypes().size());
    }

    private void checkBO(BusinessObjectNode bo) {
        tn.visit(bo);

        Assert.assertNotNull(bo.getLibrary());
        Assert.assertTrue(bo.isBusinessObject());
        Assert.assertTrue(bo instanceof BusinessObjectNode);

        // must have 3 children
        Assert.assertTrue(3 <= bo.getChildren().size());

        Assert.assertNull(bo.getAttributeFacet());
        Assert.assertNotNull(bo.getSummaryFacet());
        Assert.assertNotNull(bo.getDetailFacet());

        for (Node property : bo.getSummaryFacet().getChildren()) {
            Assert.assertTrue(property instanceof PropertyNode);
            Assert.assertTrue(property.getType() != null);
            Assert.assertTrue(property.getTypeClass().getTypeOwner() == property);
            Assert.assertTrue(property.getLibrary() == bo.getLibrary());
        }
        for (Node property : bo.getDetailFacet().getChildren()) {
            Assert.assertTrue(property instanceof PropertyNode);
            Assert.assertTrue(property.getType() != null);
            Assert.assertFalse(property.getType().getName().isEmpty());
            Assert.assertTrue(property.getTypeClass().getTypeOwner() == property);
            Assert.assertTrue(property.getLibrary() == bo.getLibrary());
        }

        tn.visit(bo);
    }
}

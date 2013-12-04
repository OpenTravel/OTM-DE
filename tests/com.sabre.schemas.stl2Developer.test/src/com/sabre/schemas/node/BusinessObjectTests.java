/**
 * 
 */
package com.sabre.schemas.node;

import org.junit.Assert;
import org.junit.Test;

import com.sabre.schemas.controllers.DefaultProjectController;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.Node_Tests.TestNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;

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

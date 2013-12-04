/**
 * 
 */
package com.sabre.schemas.node;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLFacetType;
import com.sabre.schemas.controllers.DefaultProjectController;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.Node_Tests.TestNode;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class InheritedChildren_Tests {
    private final static Logger LOGGER = LoggerFactory.getLogger(ComponentNode.class);

    ModelNode model = null;
    TestNode tn = new Node_Tests().new TestNode();
    LoadFiles lf = new LoadFiles();
    LibraryTests lt = new LibraryTests();
    MockLibrary ml = null;
    LibraryNode ln = null;
    MainController mc;
    DefaultProjectController pc;
    ProjectNode defaultProject;
    BusinessObjectNode baseBO, extensionBO;

    @Before
    public void beforeAllTests() {
        mc = new MainController();
        ml = new MockLibrary();
        pc = (DefaultProjectController) mc.getProjectController();
        defaultProject = pc.getDefaultProject();
        ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        baseBO = ml.addBusinessObjectToLibrary(ln, "BaseBO");
        extensionBO = ml.addBusinessObjectToLibrary(ln, "ExtensionBO");
        extensionBO.setExtendsType(baseBO);
        Assert.assertFalse(extensionBO.getExtendsTypeName().isEmpty());
    }

    @Test
    public void initTest() {
        List<Node> inherited = null;
        FacetNode f;
        inherited = extensionBO.getInheritedChildren();
        Assert.assertTrue(inherited.isEmpty()); // none on the BO
        inherited = extensionBO.getSummaryFacet().getInheritedChildren();
        Assert.assertFalse(inherited.isEmpty());
        Assert.assertEquals(1, inherited.size());
        Assert.assertTrue(inherited.get(0).isInheritedProperty());

        f = new FacetNode(baseBO, "Custom", "", TLFacetType.CUSTOM);
        inherited = extensionBO.getInheritedChildren();
        Assert.assertEquals(0, inherited.size());
        LOGGER.debug("Done");
    }

    @Test
    public void settingBase() {
        BusinessObjectNode bo2 = ml.addBusinessObjectToLibrary(ln, "Bo2");
        FacetNode sf = bo2.getSummaryFacet();
        List<?> children = sf.getChildren();
        bo2.setExtendsType(baseBO);
        Assert.assertEquals(sf, bo2.getSummaryFacet());
        List<?> inherited = sf.getInheritedChildren();
        LOGGER.debug("Done");
    }
}

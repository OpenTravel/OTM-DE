/**
 * 
 */
package com.sabre.schemas.node;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemas.controllers.DefaultProjectController;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.Node_Tests.TestNode;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class Enumeration_Tests {
    ModelNode model = null;
    TestNode tn = new Node_Tests().new TestNode();
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
    public void createEnums() {
        ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        TLClosedEnumeration tlc = new TLClosedEnumeration();
        tlc.setName("ClosedEnum");
        TLEnumValue tlcv1 = new TLEnumValue();
        tlcv1.setLiteral("value 1");
        tlc.addValue(tlcv1);
        EnumerationClosedNode closedEnum = new EnumerationClosedNode(tlc);
        Assert.assertNotNull(closedEnum);
        Assert.assertEquals(1, closedEnum.getChildren().size());

        EnumerationOpenNode openEnum = ml.addOpenEnumToLibrary(ln, "OpenEnum");
        Assert.assertNotNull(openEnum);
        Assert.assertEquals(1, openEnum.getChildren().size());
    }

    @Test
    public void changeEnums() throws Exception {
        ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        EnumerationOpenNode openEnum = ml.addOpenEnumToLibrary(ln, "OpenEnum");
        EnumerationClosedNode closedEnum = ml.addClosedEnumToLibrary(ln, "ClosedEnum");

        EnumerationOpenNode o2 = new EnumerationOpenNode(closedEnum);
        EnumerationClosedNode c2 = new EnumerationClosedNode(openEnum);

    }

}

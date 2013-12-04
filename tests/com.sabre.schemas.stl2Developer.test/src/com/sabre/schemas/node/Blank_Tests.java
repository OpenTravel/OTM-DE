/**
 * 
 */
package com.sabre.schemas.node;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sabre.schemas.controllers.DefaultProjectController;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.Node_Tests.TestNode;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class Blank_Tests {
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
    public void mockTest() {
        ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
    }

    @Test
    public void cloneTest() throws Exception {
        MainController mc = new MainController();

        lf.loadXfile3(mc);
        for (LibraryNode ln : Node.getAllLibraries()) {
            ln.visitAllNodes(tn);
            Assert.assertNotNull(ln);
        }
        NodeModelTestUtils.testNodeModel();
    }

}

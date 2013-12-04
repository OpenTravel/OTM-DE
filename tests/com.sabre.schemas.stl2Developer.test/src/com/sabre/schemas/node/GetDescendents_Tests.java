/**
 * 
 */
package com.sabre.schemas.node;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sabre.schemas.controllers.DefaultProjectController;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class GetDescendents_Tests {
    ModelNode model = null;
    Node_Tests nt = new Node_Tests();
    LoadFiles lf = new LoadFiles();
    LibraryTests lt = new LibraryTests();
    MockLibrary mockLibrary = null;
    LibraryNode ln = null;
    MainController mc;
    DefaultProjectController pc;
    ProjectNode defaultProject;
    String OTA = "OTA2_BuiltIns_v2.0.0"; // name
    String XSD = "XMLSchema";

    @Before
    public void beforeAllTests() {
        mc = new MainController();
        mockLibrary = new MockLibrary();
        pc = (DefaultProjectController) mc.getProjectController();
        defaultProject = pc.getDefaultProject();
    }

    @Test
    public void visitAllTypeUsers() {
        ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
        CoreObjectNode co = mockLibrary.addCoreObjectToLibrary(ln, "");
        BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "");
        VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "");

        Assert.assertNotNull(co.getSimpleFacet().getSimpleAttribute().getTLTypeObject());
        Assert.assertNotNull(co.getSimpleFacet().getSimpleAttribute().getTLTypeQName());
        co.visitAllTypeUsers(nt.new TestNode());
        Assert.assertEquals(2, co.getDescendants_TypeUsers().size());
        Assert.assertNotNull(vwa.getSimpleFacet().getSimpleAttribute().getTLTypeObject());
        Assert.assertNotNull(vwa.getSimpleFacet().getSimpleAttribute().getTLTypeQName());
        vwa.visitAllTypeUsers(nt.new TestNode());
        Assert.assertEquals(2, vwa.getDescendants_TypeUsers().size());

        Assert.assertEquals(2, bo.getDescendants_TypeUsers().size());
    }

    @Test
    public void mockDescendents() {
        ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
        ln.setEditable(true);
        CoreObjectNode co = mockLibrary.addCoreObjectToLibrary(ln, "");
        VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "");
        EnumerationOpenNode oe = mockLibrary.addOpenEnumToLibrary(ln, "");
        EnumerationClosedNode ce = mockLibrary.addClosedEnumToLibrary(ln, "");
        co.setSimpleType(ce);
        vwa.setSimpleType(oe);

        List<Node> all = ln.getDescendants();
        Assert.assertEquals(26, all.size());
        List<Node> named = ln.getDescendants_NamedTypes();
        Assert.assertEquals(5, named.size());
        List<Node> users = ln.getDescendants_TypeUsers();
        Assert.assertEquals(6, users.size());
    }

    @Test
    public void xsdBuiltinDescendents() throws Exception {

        for (LibraryNode n : Node.getAllLibraries()) {
            if (n.getName().equals(XSD))
                ln = n;
        }
        Assert.assertNotNull(ln);

        List<Node> all = ln.getDescendants();
        Assert.assertEquals(23, all.size());
        List<Node> named = ln.getDescendants_NamedTypes();
        Assert.assertEquals(20, named.size());
        List<Node> users = ln.getDescendants_TypeUsers();
        Assert.assertEquals(20, users.size());
    }

    @Test
    public void OTA_Descendents() throws Exception {

        for (LibraryNode n : Node.getAllLibraries()) {
            if (n.getName().equals(OTA))
                ln = n;
        }
        Assert.assertNotNull(ln);

        List<Node> all = ln.getDescendants();
        Assert.assertEquals(495, all.size());
        List<Node> named = ln.getDescendants_NamedTypes();
        Assert.assertEquals(85, named.size());
        List<Node> users = ln.getDescendants_TypeUsers();
        Assert.assertEquals(130, users.size());
    }
}

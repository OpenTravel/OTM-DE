/**
 * 
 */
package com.sabre.schemas.node;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemas.controllers.DefaultProjectController;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.Node_Tests.TestNode;
import com.sabre.schemas.node.OperationNode.ResourceOperationTypes;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;
import com.sabre.schemas.types.TypeResolver;

/**
 * @author Dave Hollander
 * 
 */
public class ServiceTests {
    ModelNode model = null;
    TestNode tn = new Node_Tests().new TestNode();
    LoadFiles lf = new LoadFiles();
    LibraryTests lt = new LibraryTests();

    MockLibrary ml = new MockLibrary();
    LibraryNode ln = null;
    MainController mc;
    DefaultProjectController pc;
    ProjectNode defaultProject;

    @Before
    public void beforeEachTest() {
        mc = new MainController();
        pc = (DefaultProjectController) mc.getProjectController();
        defaultProject = pc.getDefaultProject();
    }

    @Test
    public void mockServiceTest() {
        MainController mc = new MainController();
        ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        String mySubjectName = "MySubject";
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, mySubjectName);
        ServiceNode svc = null;
        svc = new ServiceNode(new TLService(), ln);
        Assert.assertFalse(svc.getLabel().isEmpty());
        Assert.assertNotNull(ln.getServiceRoot());
        svc.delete();

        TLService tlSvc = new TLService();
        TLOperation oper = null;
        oper = new TLOperation();
        oper.setName("A");
        tlSvc.addOperation(oper);
        oper = new TLOperation();
        oper.setName("B");
        tlSvc.addOperation(oper);

        svc = new ServiceNode(tlSvc, ln);
        Assert.assertFalse(svc.getLabel().isEmpty());
        Assert.assertEquals(2, svc.getChildren().size());

        for (Node op : svc.getChildren()) {
            Assert.assertEquals(3, op.getChildren().size());
        }
        svc.delete();

        svc = new ServiceNode(bo);
        // Only 4 because the bo has no query facet.
        Assert.assertEquals(4, svc.getChildren().size());
        List<Node> users = svc.getChildren_TypeUsers();
        List<Node> descendents = svc.getDescendants_TypeUsers();
        List<Node> boUsers = bo.getTypeUsers();
        Assert.assertNotNull(descendents); // 12
        Assert.assertNotNull(boUsers); // 8. Some are typed by facets.

        // Assure old services get replaced in the library and TL Model
        ServiceNode newSvc = new ServiceNode((TLService) svc.getTLModelObject(), ln);
        svc.setName("OldService");
        TLModelElement oldTLSvc = svc.getTLModelObject();
        Assert.assertNotSame(oldTLSvc, tlSvc);

        svc = new ServiceNode(tlSvc, ln);
        Assert.assertNotSame(oldTLSvc, svc.getTLModelObject());
        Assert.assertNotSame(newSvc, svc);

        svc.visitAllNodes(tn);

        // Make sure services created from model object can be resolved.
        TypeResolver tr = new TypeResolver();
        tr.resolveTypes(ln);
        ln.visitAllNodes(tn);

        // Make sure services created from GUI can be resolved.
        svc = new ServiceNode(bo.getDetailFacet());
        tr = new TypeResolver();
        tr.resolveTypes(ln);
        ln.visitAllNodes(tn);

        OperationNode op = new OperationNode(svc, "happy", ResourceOperationTypes.QUERY, bo);
        svc.visitAllNodes(tn);
    }

}

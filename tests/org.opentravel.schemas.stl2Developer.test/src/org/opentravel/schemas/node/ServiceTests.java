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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.OperationNode;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.Node_Tests.TestNode;
import org.opentravel.schemas.node.OperationNode.ResourceOperationTypes;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeResolver;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;

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

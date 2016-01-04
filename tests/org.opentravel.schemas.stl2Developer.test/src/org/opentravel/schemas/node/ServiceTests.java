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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.repository.RepositoryIntegrationTestBase;
import org.opentravel.schemas.node.OperationNode.ResourceOperationTypes;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ServiceTests extends RepositoryIntegrationTestBase {
	private final static Logger LOGGER = LoggerFactory.getLogger(ServiceTests.class);

	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();

	MockLibrary ml = new MockLibrary();
	LibraryNode ln = null;
	// MainController mc;
	// DefaultProjectController pc;
	// ProjectNode defaultProject;

	private LibraryNode majorLibrary = null;
	private LibraryNode minorLibrary = null;
	private LibraryNode patchLibrary = null;
	private LibraryChainNode chain = null;

	@Before
	public void beforeEachTest() {
		// should be done in base class
		// mc = new MainController();
		// pc = (DefaultProjectController) mc.getProjectController();
		// defaultProject = pc.getDefaultProject();
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
		svc = new ServiceNode((Node) bo.getDetailFacet());
		tr = new TypeResolver();
		tr.resolveTypes(ln);
		ln.visitAllNodes(tn);

		OperationNode op = new OperationNode(svc, "happy", ResourceOperationTypes.QUERY, bo);
		svc.visitAllNodes(tn);
	}

	@Test
	public void ServicesInVersions() throws LibrarySaveException, RepositoryException {
		LOGGER.debug("Before test.");
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "SvcTest");
		majorLibrary = LibraryNodeBuilder.create("SvcTestLibrary", getRepositoryForTest().getNamespace() + "/Test/Svc",
				"prefix", new Version(1, 0, 0)).build(uploadProject, pc);
		chain = rc.manage(getRepositoryForTest(), Collections.singletonList(majorLibrary)).get(0);
		boolean locked = rc.lock(chain.getHead());
		Assert.assertTrue(locked);
		Assert.assertTrue(majorLibrary.isEditable());
		Assert.assertEquals(RepositoryItemState.MANAGED_WIP, chain.getHead().getProjectItem().getState());
		LOGGER.debug("Managed major library in repository.");

		// Add a service to the major library
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(majorLibrary, "Business1");
		ServiceNode svc = new ServiceNode(bo);
		// Only 4 because the bo has no query facet.
		assertEquals(4, svc.getChildren().size());
		List<Node> users = svc.getChildren_TypeUsers();
		List<Node> descendents = svc.getDescendants_TypeUsers();
		List<Node> boUsers = bo.getTypeUsers();
		assertNotNull(descendents); // 12
		assertNotNull(boUsers); // 8. Some are typed by facets.

		// Create a minor library and add an operation to the service
		minorLibrary = rc.createMinorVersion(majorLibrary);
		OperationNode minorOp = new OperationNode(svc, "MinorOp");
		users = svc.getChildren_TypeUsers();
		descendents = svc.getDescendants_TypeUsers();
		assertEquals(4, svc.getChildren().size()); // should still be 4
		assertNotNull(descendents); // 12
		assertNotNull(boUsers); // 8. Some are typed by facets.

		// There should now be services in both major and minor libraries.
		for (LibraryNode ln : chain.getLibraries()) {
			assertNotNull(ln.getServiceRoot().getChildren().get(0));
		}
	}

	@Override
	public RepositoryNode getRepositoryForTest() {
		for (RepositoryNode rn : rc.getAll()) {
			if (rn.isRemote()) {
				return rn;
			}
		}
		throw new IllegalStateException("Missing remote repository. Check your configuration.");
	}

}

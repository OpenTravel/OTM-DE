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

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.objectMembers.OperationNode.ServiceOperationTypes;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ServiceTests extends BaseProjectTest {
	// public class ServiceTests extends RepositoryIntegrationTestBase {
	private final static Logger LOGGER = LoggerFactory.getLogger(ServiceTests.class);

	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	Library_FunctionTests lt = new Library_FunctionTests();

	MockLibrary ml = new MockLibrary();
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	private LibraryNode majorLibrary = null;
	// private LibraryNode minorLibrary = null;
	// private LibraryNode patchLibrary = null;
	private LibraryChainNode chain = null;

	@Override
	@Before
	public void beforeEachTest() throws Exception {
		LOGGER.debug("***Before Service Object Tests ----------------------");
		callBeforeEachTest();
		defaultProject = testProject;
		ln = ml.createNewLibrary("http://test.com", "CoreTest", defaultProject);
		ln.setEditable(true);

		// new LibraryChainNode(ln); // test in a chain
		ln.setEditable(true);
		assertTrue(ln.isEditable());

	}

	public void check(ServiceNode svc) {
		assertTrue(svc.getParent() instanceof NavNode);
		assertTrue(svc.getTLModelObject() instanceof TLService);
		assertTrue(svc == Node.GetNode(svc.getTLModelObject()));
		assertTrue("TL Service must be this service.",
				svc.getTLModelObject() == ((TLLibrary) svc.getLibrary().getTLModelObject()).getService());

		for (Node n : svc.getChildren())
			assertTrue(n instanceof OperationNode);
	}

	@Test
	public void constructorFromMockLibrary_Test() {
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "TestCore");
		ServiceNode svc = ml.addService(ln, "TestService");
		assertTrue(svc.getChildren().size() == 0);
	}

	@Test
	public void constructorFromLib_Test() {
		ServiceNode svc = new ServiceNode(ln);
		assertTrue(svc.getChildren().size() == 0);
	}

	@Test
	public void SVC_deleteTests() {
		ServiceNode svc = new ServiceNode(ln);
		assertTrue(svc.getChildren().size() == 0);

		svc.delete();
		assert svc.isDeleted();
	}

	@Test
	public void constructorFromBO_Test() {
		// Given - a BO in a library
		BusinessObjectNode bo = null;
		bo = ml.addBusinessObjectToLibrary(ln, "TestSubjectBO");
		assert bo != null;
		int OperationCount = ServiceOperationTypes.values().length - 1; // Not including queries

		// When - create service from the BO
		ServiceNode svc = new ServiceNode(bo);

		// Then - assure children handler for service root is correct.
		List<?> services = ln.getServiceRoot().childrenHandler.get();
		assert !services.isEmpty();
		assert services.contains(svc);
		// Then
		assertTrue(svc.getName().equals(bo.getName()));
		assertTrue(svc.getChildren().size() >= OperationCount);
		for (Node on : svc.getChildren()) {
			assertTrue(on instanceof OperationNode);
			assertTrue(on.getChildren().size() == 3);
			ml.check(on);
		}

		ml.check(ln);
	}

	@Test
	public void constructorFromBOinManagedLib_Test() {
		// Given - library is managed
		LibraryChainNode lcn = ml.createNewManagedLibrary_Empty("http://example.com/test", "ManagedLib",
				defaultProject);
		ln = lcn.getHead();

		// Given - a BO in a library
		BusinessObjectNode bo = null;
		bo = ml.addBusinessObjectToLibrary(ln, "TestSubjectBO");
		assert bo != null;
		int OperationCount = ServiceOperationTypes.values().length - 1; // Not including queries

		// When - create service from the BO
		ServiceNode svc = new ServiceNode(bo);

		// Then - assure children handler for service root is correct.
		List<?> services = ln.getServiceRoot().childrenHandler.get();
		assert !services.isEmpty();
		assert services.contains(svc);

		// Then - assure service is in the aggregate
		services = lcn.getServiceAggregate().getChildrenHandler().get();
		assert !services.isEmpty();
		assert services.contains(svc);

		// Then
		assertTrue(svc.getName().equals(bo.getName()));
		assertTrue(svc.getChildren().size() >= OperationCount);
		for (Node on : svc.getChildren()) {
			assertTrue(on instanceof OperationNode);
			assertTrue(on.getChildren().size() == 3);
			ml.check(on);
		}

		ml.check(ln);
	}

	@Test
	public void operationConstructor() {
		// OperationNode on = new OperationNode(service, name, type, businessObject)
	}

	@Test
	public void constructorFromTL_Tests() {
		// Given - a tl service in a library
		TLService tlSvc = new TLService();
		((TLLibrary) ln.getTLModelObject()).setService(tlSvc);

		// When - constructor called
		ServiceNode svc = new ServiceNode(tlSvc, ln);
		// Then - service node is OK
		assertTrue(svc != null);
		check(svc);
	}

	// FIXME
	// @Test
	public void mockServiceTest() {
		MainController mc = OtmRegistry.getMainController();
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
		Assert.assertEquals(5, svc.getChildren().size());
		List<Node> users = svc.getChildren_TypeUsers();
		List<TypeUser> descendents = svc.getDescendants_TypeUsers();
		Collection<TypeUser> boUsers = bo.getWhereAssigned();
		Assert.assertNotNull(descendents); // 12
		Assert.assertNotNull(boUsers); // 8. Some are typed by facets.

		// Assure old services get replaced in the library and TL Model
		ServiceNode newSvc = new ServiceNode(svc.getTLModelObject(), ln);
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
		// svc = new ServiceNode((Node) bo.getFacet_Detail());
		// tr = new TypeResolver();
		// tr.resolveTypes(ln);
		// ln.visitAllNodes(tn);

		OperationNode op = new OperationNode(svc, "happy", ServiceOperationTypes.QUERY, bo);
		svc.visitAllNodes(tn);
	}

	// FIXME
	// @Test
	// public void ServicesInVersions() throws LibrarySaveException, RepositoryException {
	// LOGGER.debug("Before test.");
	// ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "SvcTest");
	// majorLibrary = LibraryNodeBuilder.create("SvcTestLibrary", getRepositoryForTest().getNamespace() + "/Test/Svc",
	// "prefix", new Version(1, 0, 0)).build(uploadProject, pc);
	// chain = rc.manage(getRepositoryForTest(), Collections.singletonList(majorLibrary)).get(0);
	// boolean locked = rc.lock(chain.getHead());
	// Assert.assertTrue(locked);
	// Assert.assertTrue(majorLibrary.isEditable());
	// Assert.assertEquals(RepositoryItemState.MANAGED_WIP, chain.getHead().getProjectItem().getState());
	// LOGGER.debug("Managed major library in repository.");
	//
	// // Add a service to the major library
	// BusinessObjectNode bo = ml.addBusinessObjectToLibrary(majorLibrary, "Business1");
	// ServiceNode svc = new ServiceNode(bo);
	// // 5 because the bo has custom and query facet.
	// assertEquals(5, svc.getChildren().size());
	// List<Node> users = svc.getChildren_TypeUsers();
	// List<TypeUser> descendents = svc.getDescendants_TypeUsers();
	// Collection<TypeUser> boUsers = bo.getWhereAssigned();
	// assertNotNull(descendents); // 12
	// assertNotNull(boUsers); // 8. Some are typed by facets.
	//
	// // Create a minor library and add an operation to the service
	// minorLibrary = rc.createMinorVersion(majorLibrary);
	// OperationNode minorOp = new OperationNode(svc, "MinorOp");
	// users = svc.getChildren_TypeUsers();
	// descendents = svc.getDescendants_TypeUsers();
	// assertEquals(5, svc.getChildren().size()); // should still be 5
	// assertNotNull(descendents); // 15
	// assertNotNull(boUsers); // 10. Some are typed by facets.
	//
	// // There should now be services in both major and minor libraries.
	// for (LibraryNode ln : chain.getLibraries()) {
	// assertNotNull(ln.getServiceRoot().getChildren().get(0));
	// }
	// }
	//
	// @Override
	// public RepositoryNode getRepositoryForTest() {
	// for (RepositoryNode rn : rc.getAll()) {
	// if (rn.isRemote()) {
	// return rn;
	// }
	// }
	// throw new IllegalStateException("Missing remote repository. Check your configuration.");
	// }

}

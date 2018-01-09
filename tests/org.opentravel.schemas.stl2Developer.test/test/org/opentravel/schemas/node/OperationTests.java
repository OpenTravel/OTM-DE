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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.OperationFacetNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.objectMembers.OperationNode.ServiceOperationTypes;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class OperationTests extends BaseProjectTest {
	// public class ServiceTests extends RepositoryIntegrationTestBase {
	private final static Logger LOGGER = LoggerFactory.getLogger(OperationTests.class);

	LoadFiles lf = new LoadFiles();
	Library_FunctionTests lt = new Library_FunctionTests();
	MockLibrary ml = new MockLibrary();

	LibraryNode ln = null;
	private LibraryChainNode chain = null;

	BusinessObjectNode bo = null;
	CoreObjectNode core = null;

	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeEachTest() throws Exception {
		LOGGER.debug("***Before Operation Object Tests ----------------------");
		callBeforeEachTest();
		defaultProject = testProject;
		ln = ml.createNewLibrary("http://test.com", "CoreTest", defaultProject);
		ln.setEditable(true);
		assertTrue(ln.isEditable());

		// chain = new LibraryChainNode(ln); // test in a chain
		// ln.setEditable(true);
		// assertTrue(ln.isEditable());
		// assertNotNull(chain);
		assertTrue(ln != null);

		core = ml.addCoreObjectToLibrary(ln, "TestCore");
		bo = ml.addBusinessObjectToLibrary(ln, "TestSubjectBO");
		assertTrue(bo != null);
	}

	public void check(OperationNode op) {
		for (Node n : op.getChildren())
			assertTrue(n instanceof OperationFacetNode);
	}

	/**
	 * Create a named TLOperation child of the passed service.
	 * 
	 * @param owner
	 * @param name
	 * @return
	 */
	public TLOperation createTL(TLService owner, String name) {
		TLOperation tlOp = new TLOperation();
		// Setters must be done before adding to service
		tlOp.setName(name);
		tlOp.setRequest(new TLFacet());
		tlOp.setResponse(new TLFacet());
		tlOp.setNotification(new TLFacet());
		owner.addOperation(tlOp);

		assertTrue(tlOp.getOwningService() == owner);
		assertTrue(owner.getOperations().contains(tlOp));
		assertTrue(tlOp.getName().equals(name));

		return tlOp;
	}

	@Test
	public void constructorFromTL_Test() {
		// Given - a TLOperation added to the service
		ServiceNode svc = ml.addService(ln, "TestSvc");
		String op1Name = "TestOp1";
		TLOperation tlOp = createTL(svc.getTLModelObject(), op1Name);

		// When - constructor called
		OperationNode op = new OperationNode(tlOp);

		// Then - service tests
		assertFalse(svc.getChildren().size() == 1); // constructor does not do parent
		// Then - operation tests
		assertTrue(op != null);
		assertTrue(op.getName().equals(op1Name));
		assertTrue(op.getChildren().size() == 3);

		check(op);
	}

	@Test
	public void constructorWithSvc_Test() {
		// Given - an empty service
		ServiceNode svc = new ServiceNode(core.getLibrary());

		// When - constructor called
		OperationNode op = new OperationNode(svc, "Op1");

		// Then
		assertTrue(op.getChildren().size() == 3);
		assertNotNull(op);

		check(op);
	}

	@Test
	public void constructorWithBO_Test() {
		// Given - an empty service
		ServiceNode svc = new ServiceNode(core.getLibrary());

		// When - used as used by ServiceNode.addCRUDQ_Operations
		for (ServiceOperationTypes op : ServiceOperationTypes.values())
			if (!op.equals(ServiceOperationTypes.QUERY))
				new OperationNode(svc, op.displayName, op, bo);
		for (Node n : bo.getQueryFacets())
			new OperationNode(svc, n.getLabel(), ServiceOperationTypes.QUERY, bo);

		// Then
		for (Node on : svc.getChildren()) {
			assertTrue(on instanceof OperationNode);
			assertTrue(on.getChildren().size() == 3);
			check((OperationNode) on);
		}
	}

	@Test
	public void factory_newMember_Test() {
		// newMember(svc, tlObj);
	}
}

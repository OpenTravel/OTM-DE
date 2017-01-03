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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ParentRef;
import org.opentravel.schemas.node.resources.ResourceBuilder;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ResourceObjectTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceObjectTests.class);

	ModelNode model = null;
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	TestNode tn = new NodeTesters().new TestNode();

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void constructorTests() {
		// Given - a library and the objects used in constructors
		LibraryNode ln = ml.createNewLibrary("http://example.com/resource", "RT", pc.getDefaultProject());
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "MyBo");
		Node node = bo;
		TLResource mbr = new TLResource();
		mbr.setName("MyTlResource");
		mbr.setBusinessObjectRef(bo.getTLModelObject());

		// When - used in LibraryNode.generateLibrary()
		TLResource tlr = new ResourceBuilder().buildTL(); // get a populated tl resource
		tlr.setBusinessObjectRef(bo.getTLModelObject());
		ResourceNode rn1 = new ResourceNode(tlr, ln);

		// When - used in tests
		ResourceNode rn2 = ml.addResource(bo);

		// When - used in NodeFactory
		ResourceNode rn3 = new ResourceNode(mbr);
		ln.addMember(rn3);

		// When - used in ResourceCommandHandler to launch wizard
		ResourceNode rn4 = new ResourceNode(node);
		// When - builder used as in ResourceCommandHandler
		new ResourceBuilder().build(rn4, bo);

		// Then - must be complete
		checkResource(rn1);
		checkResource(rn2);
		checkResource(rn3);
		checkResource(rn4);
	}

	@Test
	public void fileReadTest() throws Exception {
		LibraryNode testLib = new LoadFiles().loadFile6(mc);
		new LibraryChainNode(testLib); // Test in a chain

		for (Node n : testLib.getDescendants_LibraryMembers()) {
			if (n instanceof ResourceNode)
				checkResource((ResourceNode) n);
		}
	}

	@Test
	public void deleteResourceTest() {
		// Given - a library and resource
		LibraryNode ln = ml.createNewLibrary("http://example.com/resource", "RT", pc.getDefaultProject());
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "MyBo");
		ResourceNode rn = ml.addResource(bo);
		checkResource(rn);

		// Given
		Collection<TypeUser> l1 = bo.getWhereAssigned();
		Collection<TypeUser> l2 = bo.getWhereUsedAndDescendants();
		assertTrue("Resource must be in subject's where assigned list.", bo.getWhereAssigned().contains(rn));
		assertTrue("Resource must have a subject.", rn.getSubject() == bo);
		assertTrue("Resource must be in subject's where-used list.", bo.getWhereUsedAndDescendants().contains(rn));

		// When - the resource is deleted
		rn.delete();

		// Then
		assertTrue("Resource must be deleted.", rn.isDeleted());
		assertTrue("Resource must NOT be in subject's where-used list.", !bo.getWhereUsedAndDescendants().contains(rn));
	}

	@Test
	public void actionExample_Tests() {

		// Given - a valid resource using mock library provided business object
		LibraryNode ln = ml.createNewLibrary(pc, "ResourceTestLib");
		BusinessObjectNode bo = null;
		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode) {
				bo = (BusinessObjectNode) n;
				break;
			}
		ResourceNode resource = ml.addResource(bo);
		assertTrue("Resource was created.", resource != null);

		// When
		// Then - examples are created
		for (ActionNode action : resource.getActions()) {
			String url = action.getExample().getURL();
			LOGGER.debug("Example: " + url + ".");
			assertTrue("Action has example.", !url.isEmpty());
			if (url.startsWith("GET"))
				assertTrue("Get example must be correct.",
						url.startsWith("GET http://example.com/ResourceTestLibInitialBOs/{TestID}"));
		}
	}

	@Test
	public void actionExampleWithBaseResource_Tests() {

		// Given - a valid resource using mock library provided business object
		LibraryNode ln = ml.createNewLibrary(pc, "ResourceTestLib");
		BusinessObjectNode bo = null;
		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode) {
				bo = (BusinessObjectNode) n;
				break;
			}
		ResourceNode resource = ml.addResource(bo);
		assertTrue("Resource was created.", resource != null);
		// Given a second resource
		BusinessObjectNode parentBO = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		ResourceNode parentResource = ml.addResource(parentBO);

		// When - parent resource is set on resource
		resource.toggleParent(parentResource.getName());
		ParentRef parentRef = resource.getParentRef();
		parentRef.setParamGroup("ID");

		// Then - extension is OK and examples are created
		// TODO - how to test to assure that changes were made?
		assertTrue("Parent reference is OK.", resource.getParentRef().getParentResource() == parentResource);
		assertTrue("Parent has URL path contribution.", !resource.getParentRef().getUrlContribution().isEmpty());

		for (ActionNode action : resource.getActions()) {
			String url = action.getExample().getURL();
			LOGGER.debug("Example: " + url + ".");
			assertTrue("Action has example.", !url.isEmpty());
			assertTrue("Example contains parent name", url.contains(parentBO.getName()));
		}

		// Given a grandparent resource
		BusinessObjectNode grandParentBO = ml.addBusinessObjectToLibrary(ln, "GrandParent");
		ResourceNode grandParentR = ml.addResource(grandParentBO);

		// When - parent resource is set on parent resource
		parentRef = parentResource.setParentRef(grandParentR.getName(), "ID");

		// Then - examples include grandparent
		for (ActionNode action : resource.getActions()) {
			String url = action.getExample().getURL();
			LOGGER.debug("Example: " + url + ".");
			assertTrue("Action must have example URL.", !url.isEmpty());
			assertTrue("Example must contain grand parent name", url.contains(grandParentBO.getName()));
		}

	}

	@Test
	public void deleteParentResource_Tests() {

		// Given - a valid resource using mock library provided business object
		LibraryNode ln = ml.createNewLibrary(pc, "ResourceTestLib");
		BusinessObjectNode bo = null;
		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode) {
				bo = (BusinessObjectNode) n;
				break;
			}
		bo.setName("SubResource");
		ResourceNode resource = ml.addResource(bo);
		assertTrue("Resource was created.", resource != null);

		// Given a second resource
		BusinessObjectNode parentBO = ml.addBusinessObjectToLibrary(ln, "ParentBO");
		ResourceNode parentResource = ml.addResource(parentBO);

		// When - parent resource is set on resource with paramGroup
		ParentRef parentRef = resource.setParentRef(parentResource.getName(), "ID");

		// Then - there is a parent contribution
		assertTrue("Parent makes URL contribution.", !parentRef.getUrlContribution().isEmpty());

		// When - parent resource is deleted
		parentRef.delete();

		// Then - the node, tlRef and contribution are gone
		assertTrue("Parent has empty URL contribution.", parentRef.getUrlContribution().isEmpty());
		assertTrue("TLResource does not have parentRefs", resource.getTLModelObject().getParentRefs().isEmpty());
		assertTrue("Resource does not have ParentRef child.", !resource.getChildren().contains(parentRef));
	}

	private void checkResource(ResourceNode resource) {
		LOGGER.debug("Checking resource: " + resource);

		Assert.assertTrue(resource instanceof ResourceNode);

		// Validate model and tl object
		assertTrue(resource.getTLModelObject() instanceof TLResource);
		assertNotNull(resource.getTLModelObject().getListeners());
		TLResource tlr = (TLResource) resource.getTLModelObject();

		// Validate that the resource is in the where used list for its subject
		assertTrue("Must have a subject.", resource.getSubject() != null);
		assertTrue("Subject must have resource in its where assigned list.", resource.getSubject().getWhereAssigned()
				.contains(resource));
		// LOGGER.debug("Subject must have resource in its where assigned list: "
		// + resource.getSubject().getWhereAssigned().contains(resource));

		// Make sure it is in the library
		assertTrue("Must have library set.", resource.getLibrary() != null);
		List<TypeUser> users = resource.getLibrary().getDescendants_TypeUsers();
		assertTrue("Must be in library.", users.contains(resource));
		if (tlr.getOwningLibrary() != null)
			Assert.assertNotNull(resource.getLibrary());

		Object o;
		for (ResourceMemberInterface rmi : resource.getActionFacets())
			checkResource(rmi);
		for (ResourceMemberInterface rmi : resource.getActions()) {
			checkResource(rmi);
			for (Node child : rmi.getChildren())
				checkResource((ResourceMemberInterface) child);
		}
		for (ResourceMemberInterface rmi : resource.getParameterGroups(false)) {
			checkResource(rmi);
			for (Node child : rmi.getChildren())
				checkResource((ResourceMemberInterface) child);
		}

		o = tlr.getBusinessObjectRef();
		o = tlr.getBusinessObjectRefName();
		o = tlr.getBaseNamespace();
		o = tlr.getBasePath();
		o = tlr.getExtension();
		o = tlr.getListeners();
		o = tlr.getLocalName();
		o = tlr.getName();
		o = tlr.getNamespace();
		o = tlr.getParentRefs();
		o = tlr.getVersion();

	}

	public void checkResource(ResourceMemberInterface resource) {
		// LOGGER.debug("Checking " + resource + " " + resource.getClass().getSimpleName());
		assert resource.getParent() != null;
		assert resource.getName() != null;
		assert resource.getLabel() != null;
		assert resource.getTLModelObject() != null;
		assert resource.getTLModelObject().getListeners() != null;
		assert !resource.getTLModelObject().getListeners().isEmpty();
		assert Node.GetNode(resource.getTLModelObject()) == resource;
		resource.getFields(); // don't crash
	}
}

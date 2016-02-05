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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.resources.ActionFacet;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ActionRequest;
import org.opentravel.schemas.node.resources.ActionResponse;
import org.opentravel.schemas.node.resources.ParamGroup;
import org.opentravel.schemas.node.resources.ResourceBuilder;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.resources.ResourceParameter;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ResourceObjectTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceObjectTests.class);

	ModelNode model = null;
	MockLibrary mockLibrary = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	TestNode tn = new NodeTesters().new TestNode();

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		mockLibrary = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void constructorTests() {
		LibraryNode ln = mockLibrary.createNewLibrary("http://example.com/resource", "RT", pc.getDefaultProject());
		TLResource resource = buildTLResource();
		ResourceNode rn = new ResourceNode(resource, ln);
		ln.addMember(rn);
		checkResource(rn);
		checkResource(new ParamGroup(resource.getParamGroups().get(0)));
		checkResource(new ResourceParameter(resource.getParamGroups().get(0).getParameters().get(0)));
		checkResource(new ActionNode(resource.getActions().get(0)));
		checkResource(new ActionRequest(resource.getActions().get(0).getRequest()));
		checkResource(new ActionResponse(resource.getActions().get(0).getResponses().get(0)));
		checkResource(new ActionFacet(resource.getActionFacets().get(0)));

		List<Node> users = ln.getDescendants_TypeUsers();
		assertTrue(users.contains(rn));
	}

	private TLResource buildTLResource() {
		// final String NAME = "testName";
		// TLResource resource = new TLResource();
		// resource.setName(NAME);
		//
		// TLParamGroup params = new TLParamGroup();
		// resource.addParamGroup(params);
		// params.setName(NAME);
		// TLParameter parameter = new TLParameter();
		// params.addParameter(parameter);
		// parameter.setFieldRefName(NAME);
		//
		// TLAction action = new TLAction();
		// resource.addAction(action);
		// action.setActionId(NAME);
		// TLActionResponse response = new TLActionResponse();
		// action.addResponse(response);
		// response.setPayloadTypeName(NAME);
		// TLActionRequest request = new TLActionRequest();
		// action.setRequest(request);
		// request.setPayloadTypeName(NAME);
		//
		// TLActionFacet facet = new TLActionFacet();
		// resource.addActionFacet(facet);
		// facet.setName(NAME);
		return new ResourceBuilder().buildTL();
	}

	@Test
	public void fileReadTest() throws Exception {
		LibraryNode testLib = new LoadFiles().loadFile6(mc);
		new LibraryChainNode(testLib); // Test in a chain

		for (Node n : testLib.getDescendants_NamedTypes()) {
			if (n instanceof ResourceNode)
				checkResource((ResourceNode) n);
		}
	}

	private void checkResource(ResourceNode resource) {
		LOGGER.debug("Checking resource: " + resource);

		Assert.assertTrue(resource instanceof ResourceNode);

		// Validate model and tl object
		assertTrue(resource.getTLModelObject() instanceof TLResource);
		assertNotNull(resource.getTLModelObject().getListeners());
		TLResource tr = (TLResource) resource.getTLModelObject();

		if (tr.getOwningLibrary() != null)
			Assert.assertNotNull(resource.getLibrary());

		Object o;
		o = tr.getActionFacets();
		o = tr.getActions();
		o = tr.getBusinessObjectRef();
		o = tr.getBusinessObjectRefName();
		o = tr.getBaseNamespace();
		o = tr.getBasePath();
		o = tr.getExtension();
		o = tr.getListeners();
		o = tr.getLocalName();
		o = tr.getName();
		o = tr.getNamespace();
		o = tr.getParamGroups();
		o = tr.getParentRefs();
		o = tr.getVersion();

	}

	private void checkResource(ResourceMemberInterface resource) {
		assert resource.getParent() != null;
		assert resource.getName() != null;
		assert resource.getLabel() != null;
		assert resource.getTLModelObject() != null;
		assert resource.getTLModelObject().getListeners() != null;
		assert !resource.getTLModelObject().getListeners().isEmpty();
		resource.getFields(); // don't crash
	}
}

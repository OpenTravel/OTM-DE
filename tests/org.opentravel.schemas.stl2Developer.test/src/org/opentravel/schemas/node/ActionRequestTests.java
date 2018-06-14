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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ActionRequest;
import org.opentravel.schemas.node.resources.ParamGroup;
import org.opentravel.schemas.node.resources.ParentRef;
import org.opentravel.schemas.node.resources.ResourceBuilder;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action requests are responsible for creating path templates and URLs.
 * 
 * @author Dave Hollander
 * 
 */
public class ActionRequestTests extends BaseTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionRequestTests.class);

	private BusinessObjectNode baseBo;
	private BusinessObjectNode childBo;

	@Before
	public void beforeEachOfTheseTests() {

		// Given - two BO with query facets
		ln = ml.createNewLibrary(pc, "TestLib");
		baseBo = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		childBo = ml.addBusinessObjectToLibrary(ln, "ChildBO");
		FacetProviderNode bq = baseBo.addFacet("BaseQuery", TLFacetType.QUERY);
		FacetProviderNode cq = childBo.addFacet("ChildQuery", TLFacetType.QUERY);
		SimpleTypeNode st = ml.addSimpleTypeToLibrary(ln, "QueryData");
		new ElementNode(bq, "QB", st);
		new ElementNode(cq, "QC", st);

		assertTrue("Must have ID facet.", baseBo.getFacet_ID() != null);
		assertTrue("Must have ID facet.", childBo.getFacet_ID() != null);
		assertTrue("Must have ID facet children.", baseBo.getFacet_ID().getChildren().size() > 0);
		assertTrue("Must have ID facet children.", childBo.getFacet_ID().getChildren().size() > 0);
		// Make sure id properties do not have BO name in them
		int cnt = 1;
		for (Node prop : baseBo.getFacet_ID().getChildren())
			prop.setName("identifier" + cnt++);
		for (Node prop : childBo.getFacet_ID().getChildren())
			prop.setName("identifierC" + cnt++);
	}

	@Test
	public void constructorTests() {
		// Given -
		// When -
		// new ActionRequest(tlActionReq);
		// new ActionRequest(actionNode);

		// Then -
	}

	private ResourceNode runBuilder(BusinessObjectNode bo) {
		ResourceNode newR = new ResourceNode(ln, bo); // create named empty resource
		new ResourceBuilder().build(newR, bo);
		return newR;
	}

	private List<ActionRequest> getRequests(ResourceNode rn) {
		List<ActionRequest> requests = new ArrayList<>();
		for (ActionNode action : rn.getActions())
			requests.add(action.getRequest());
		return requests;
	}

	private ParamGroup getIDGroup(ResourceNode rn) {
		ParamGroup pg = rn.getParameterGroups(true).get(0);
		assertTrue(pg.isIdGroup());
		return pg;
	}

	@Test
	public void AR_builderTests() {
		// Given -
		// When - invoked as done in ResourceCommandHandler
		ResourceNode newR = runBuilder(baseBo);

		// Then - get the action requests
		List<ActionRequest> requests = getRequests(newR);
		assertTrue("Must have built requests.", !requests.isEmpty());

		// Then - each request
	}

	@Test
	public void AR_changeParamGroupTests() {
		// Given -
		ResourceNode newR = runBuilder(baseBo);
		List<ActionRequest> requests = getRequests(newR);

		// When - set them all to NONE
		for (ActionRequest rq : requests)
			rq.setParamGroup("NONE");

		// Then -
		for (ActionRequest rq : requests)
			assertTrue("Must not have parameter.", !rq.getURL().contains("{"));

		// When - set to ID group
		for (ActionRequest rq : requests)
			rq.setParamGroup(getIDGroup(newR).getName());
		// Then -
		for (ActionRequest rq : requests) {
			assertTrue(rq.getParamGroup() == getIDGroup(newR));
			assertTrue("Must have parameter.", rq.getURL().contains("{"));
		}
	}

	@Test
	public void AR_userEditTests() {
		// Given -

		//
		// When - created by the resource builder
		ResourceNode newR = runBuilder(baseBo);
		List<ActionRequest> requests = getRequests(newR);

		// Then - initial build must have path template
		for (ActionRequest rq : requests) {
			String url = rq.getPathTemplate();
			assertTrue("Must have collection name in template.", rq.getPathTemplate().contains(baseBo.getName()));
		}

		//
		// When - path template over-riden
		for (ActionRequest rq : requests)
			rq.setPathTemplate("");

		// Then - template must not contain collection name
		for (ActionRequest rq : requests) {
			// Note - the name could be in the payload in the URL but not path template
			String url = rq.getPathTemplate();
			assertTrue("Must not have collection name in template.", !rq.getPathTemplate().contains(baseBo.getName()));
		}

		//
		// When - path template over-riden
		String userCollection = "MyCollection";
		for (ActionRequest rq : requests)
			rq.setPathTemplate(userCollection);

		// Then - urls must contain new collection name
		for (ActionRequest rq : requests)
			assertTrue("Must have new collection name in URL.", rq.getURL().contains(userCollection));

		//
		// When - path template overridden but still has identifiers in the string
		String oldID = "{OLD_ID}";
		userCollection = "MyCollection2/" + oldID;
		for (ActionRequest rq : requests)
			rq.setPathTemplate(userCollection);

		// Then - URLs must not repeat identifier
		for (ActionRequest rq : requests) {
			String url = rq.getURL();
			assertTrue("Must have new collection name in URL.", !rq.getURL().contains(oldID));
		}

		// TODO - make sure there are no "//" after the system one
		// TODO - make sure there are is "/" between base and path template
	}

	@Test
	public void AR_changeMethodTests() {
		// Given -
		//
		// When - created by the resource builder
		ResourceNode newR = runBuilder(baseBo);
		List<ActionRequest> requests = getRequests(newR);
		// Then - there should be one of each method used

		// TODO
		// When -
		// Then -
	}

	final static String BASEPATH = "BasePath";

	@Test
	public void AR_basePathTests() {
		// Given -
		// When - created by the resource builder
		ResourceNode newR = runBuilder(baseBo);
		// When - base path assigned to resource
		newR.setBasePath(BASEPATH);

		// Then -
		List<ActionRequest> requests = getRequests(newR);
		for (ActionRequest rq : requests) {
			String bp = rq.getURL();
			assertTrue("Request URL must contain base path.", bp.contains(BASEPATH));
		}
	}

	@Test
	public void AR_changeActionFacetTests() {
		// Given -
		// When -
		// Then -
	}

	@Test
	public void AR_changeMimeTypeTests() {
		// Given -
		// When -
		// Then -
	}

	@Test
	public void AR_parentRefTests() {
		// TODO - make sure the parent ID params are present
		// TODO - make sure /es are correct
		// TODO - make sure path templeates are correct
		// Given -
		ResourceNode baseR = runBuilder(baseBo);
		ResourceNode childR = runBuilder(childBo);

		// When -
		ParentRef pRef = new ParentRef(childR);
		pRef.setParent(baseR.getName());
		pRef.setParamGroup("ID");

		// Then -
		assertTrue(pRef != null);
	}

	@Test
	public void AR_versionTests() {
		// Given -
		// When -
		// Then -
	}

	public void check(ActionRequest request) {
		// LOGGER.debug("Checking " + request + " " + request.getClass().getSimpleName());
		request.getFields(); // don't crash
	}
}

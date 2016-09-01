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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.modelObject.TLValueWithAttributesFacet;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.QueryFacetNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;
import org.opentravel.schemas.utils.FacetNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class FacetsTests {
	static final Logger LOGGER = LoggerFactory.getLogger(FacetsTests.class);

	ModelNode model = null;
	TestTypes tt = new TestTypes();

	NodeTesters nt = new NodeTesters();
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
	public void constructorTests() {

		// Given two libraries, one managed one not managed
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);
		assertTrue("Library must exist.", ln != null);
		assertTrue("Library must exist.", ln_inChain != null);
		constructorTL_Tests();
	}

	private void constructorTL_Tests() {
		// Given - one of each TLFacet type.
		TLFacet tlf = new TLFacet();
		assertTrue("Facet must not be null.", tlf != null);
		TLAbstractFacet tlAf = new TLFacet();
		assertTrue("Facet must not be null.", tlAf != null);
		TLListFacet tlLf = new TLListFacet(tlAf); // tlaf should be simple or detail.
		assertTrue("Facet must not be null.", tlLf != null);
		TLSimpleFacet tlSf = new TLSimpleFacet();
		assertTrue("Facet must not be null.", tlSf != null);
		TLValueWithAttributes tlVWA = new TLValueWithAttributes();
		assertTrue("Facet must not be null.", tlVWA != null);
		TLValueWithAttributesFacet tlVf = new TLValueWithAttributesFacet(tlVWA);
		assertTrue("Facet must not be null.", tlVf != null);
	}

	@Test
	public void buildersTests() {
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);

		FacetNode facetNode1 = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").build();
		FacetNode facetNode2 = FacetNodeBuilder.create(ln).addAttributes("A1", "A2", "A3").build();
		FacetNode facetNode3 = FacetNodeBuilder.create(ln).addIndicators("I1", "I2").addAliases("Alias1").build();
		assertTrue("Built node 1 must not be null.", facetNode1 != null);
		assertTrue("Built node 2 must not be null.", facetNode2 != null);
		assertTrue("Built node 3 must not be null.", facetNode3 != null);
	}

	@Test
	public void queryFacetTests() throws Exception {
		lf.loadTestGroupA(mc);
		for (LibraryNode lib : pc.getDefaultProject().getLibraries()) {
			lib.setEditable(true);
			assertTrue("Library must be editable.", lib.isEditable());
			queryFacetTests(lib);
		}
	}

	private void queryFacetTests(LibraryNode lib) {
		LOGGER.debug("Checking query facets in " + lib);
		for (Node n : lib.getDescendants()) {
			if (n instanceof FacetNode)
				if (n.isQueryFacet())
					checkQueryFacet((FacetNode) n);
		}
	}

	public void checkQueryFacet(FacetNode qn) {
		LOGGER.debug("Checking Facet: " + qn);
		if (!(qn instanceof QueryFacetNode))
			LOGGER.debug("Not created in factory.");

		assertTrue("Must be query facet.", qn.isQueryFacet());
		assertTrue("Must be renamable.", qn.isRenameable());
		assertTrue("Must be delete-able.", qn.isDeleteable());
		assertTrue("Must be assignable.", qn.isAssignable());
		assertTrue("Must be assignable to complex.", qn.isComplexAssignable());
		assertTrue("Must be type provider.", qn.isTypeProvider());
		assertTrue("Must be valid parent to attributes.", qn.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertTrue("Must be valid parent to elements.", qn.isValidParentOf(PropertyNodeType.ELEMENT));

		assertFalse("Must NOT be assignable to element ref", qn.isAssignableToElementRef());
		assertFalse("Must NOT be assignable to simple.", qn.isAssignableToSimple());
		assertFalse("Must NOT be assignable to simple.", qn.isSimpleAssignable());
		assertFalse("Must NOT be assignable to VWA.", qn.isAssignableToVWA());
		assertFalse("Must NOT be custom facet.", qn.isCustomFacet());
		assertFalse("Must NOT be default facet.", qn.isDefaultFacet());
		assertFalse("Must NOT be named type.", qn.isNamedType());

		// Behaviors
		AttributeNode attr = new AttributeNode(qn, "att1");
		ElementNode ele = new ElementNode(qn, "ele1");
		assertTrue("Must be able to add attributes.", attr.getParent() == qn);
		assertTrue("Must be able to add elements.", ele.getParent() == qn);
	}
}

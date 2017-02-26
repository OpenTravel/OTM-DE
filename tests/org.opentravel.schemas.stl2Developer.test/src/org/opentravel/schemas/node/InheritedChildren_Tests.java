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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 * @see ChoiceObjectTests#ChoiceFacetsTests()
 * 
 */
public class InheritedChildren_Tests {
	private final static Logger LOGGER = LoggerFactory.getLogger(ComponentNode.class);

	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	BusinessObjectNode baseBO, extensionBO;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
	}

	public void setUpExtendedBO() {
		// Given -- two business objects. extensionBO extends baseBO
		//
		baseBO = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		// If a fully populated BO is used it will be invalid because of too many id attributes
		extensionBO = ml.addBusinessObjectToLibrary_Empty(ln, "ExtensionBO");
		extensionBO.setExtension(baseBO);
		assertTrue("Must extension must extend base.", !extensionBO.getExtendsTypeName().isEmpty());
		assertTrue("Base where extended must contain extension.", baseBO.getWhereExtendedHandler().getWhereExtended()
				.contains(extensionBO));
		assertTrue("Extension base type must be baseBO.", extensionBO.getExtensionBase() == baseBO);
	}

	@Test
	public void inheritedTests() {
		// Given extensionBO extends baseBO
		setUpExtendedBO();

		// Then - inherited children
		List<Node> inherited = extensionBO.getSummaryFacet().getInheritedChildren();
		Assert.assertFalse(inherited.isEmpty());
		assertTrue("All base properties must be inherited.",
				baseBO.getSummaryFacet().getChildren().size() == inherited.size());
		Assert.assertTrue(inherited.get(0).isInheritedProperty());

		baseBO.addFacet("C2", TLFacetType.CUSTOM);
		List<TLContextualFacet> inf = FacetCodegenUtils.findGhostFacets(extensionBO.getTLModelObject(),
				TLFacetType.CUSTOM);
		assertTrue("Ghosts must be found.", !inf.isEmpty());
		List<Node> iKids = extensionBO.getInheritedChildren();
		assertTrue("extended object has inherited children.", !extensionBO.getInheritedChildren().isEmpty());

		LOGGER.debug("Done");
	}

	@Test
	public void settingBase() {
		setUpExtendedBO();
		assertTrue("Library must be valid.", ln.isValid()); // validates TL library
		BusinessObjectNode bo2 = ml.addBusinessObjectToLibrary_Empty(ln, "Bo2");
		FacetNode sf = bo2.getSummaryFacet();
		List<?> children = sf.getChildren();
		bo2.setExtension(baseBO);
		Assert.assertEquals(sf, bo2.getSummaryFacet());
		List<?> inherited = sf.getInheritedChildren();
		LOGGER.debug("Done");
	}

}

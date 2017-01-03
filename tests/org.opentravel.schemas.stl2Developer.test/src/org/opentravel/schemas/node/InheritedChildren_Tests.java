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
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
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

	@Test
	public void ChoiceFacetsTests() {
		// Given 3 choice groups
		ChoiceObjectNode ch1 = ml.addChoice(ln, "Ch1");
		int baseCount = ch1.getChoiceFacets().size();
		ChoiceObjectNode ch2 = new ChoiceObjectNode(new TLChoiceObject());
		ch2.setName("Ch2");
		ln.addMember(ch2);
		ch2.addFacet("Ch2CF1");
		ChoiceObjectNode ch3 = new ChoiceObjectNode(new TLChoiceObject());
		ch3.setName("Ch3");
		ln.addMember(ch3);

		// When extended
		ch2.setExtension(ch1);
		ch3.setExtension(ch2);

		// Then
		assertTrue("Ch1 must be extended by ch2.", ch1.getWhereExtendedHandler().getWhereExtended().contains(ch2));
		assertTrue("Ch2 must extend ch1.", ch2.getExtensionBase() == ch1);
		assertTrue("Ch2 must have 2 children.", ch2.getChildren().size() == 2);
		assertTrue("Ch2 shared facet must NOT have any children.", ch2.getSharedFacet().getChildren().isEmpty());
		assertTrue("Ch3 must extend ch2.", ch3.getExtensionBase() == ch2);
		assertTrue("Ch3 must have 1 child.", ch3.getChildren().size() == 1);
		assertTrue("Ch3 shared facet must NOT have any children.", ch3.getSharedFacet().getChildren().isEmpty());

		// Then - look for ghost facets from TL Model
		List<TLContextualFacet> inf2 = FacetCodegenUtils.findGhostFacets(ch2.getTLModelObject(), TLFacetType.CHOICE);
		List<TLContextualFacet> inf3 = FacetCodegenUtils.findGhostFacets(ch3.getTLModelObject(), TLFacetType.CHOICE);
		assertTrue("Ch2 must have 2 ghosts.", inf2.size() == 2);
		assertTrue("Ch3 must have 3 ghosts.", inf3.size() == 3);

		// When - the inherited children are initialized
		ch2.initInheritedChildren();
		ch3.initInheritedChildren();
		// Then - children remain unchanged.
		assertTrue("Ch2 must have 2 children.", ch2.getChildren().size() == 2);
		assertTrue("Ch3 must have 1 child.", ch3.getChildren().size() == 1);
		// Then - inherited children are present.
		assertTrue("Ch2 must inherit base choice facets.", ch2.getInheritedChildren().size() == baseCount);
		assertTrue("Ch3 must inherit base and c2 choice facets.", ch3.getInheritedChildren().size() == baseCount
				+ ch2.getChoiceFacets().size());
		// Then - the inherited tree filter depends on isInherited.
		for (Node n : ch3.getInheritedChildren())
			assertTrue("Must be inherited.", n.isInheritedProperty());

		//
		// When - adding and deleting facets to base types
		//
		// Given starting inherited count.
		int ch3Count = ch3.getInheritedChildren().size();
		// When
		ContextualFacetNode ch2cf2 = ch2.addFacet("Ch2CF2");
		ch3Count++;
		// Then
		assertTrue("Ch3 must have 1 more inherited child.", ch3.getInheritedChildren().size() == ch3Count);
		// When
		ContextualFacetNode ch1cf3 = ch1.addFacet("Ch1CF3");
		ch3Count++;
		// Then
		assertTrue("Ch3 must have 1 more inherited child.", ch3.getInheritedChildren().size() == ch3Count);

		// When deleted
		ch2cf2.delete();
		ch3Count--;
		assertTrue("Ch2 must not have deleted facet.", !ch2.getChoiceFacets().contains(ch2cf2));
		assertTrue("Ch3 must have 1 less inherited child.", ch3.getInheritedChildren().size() == ch3Count);

		ch1cf3.delete();
		ch3Count--;
		assertTrue("Ch3 must have 1 less inherited child.", ch3.getInheritedChildren().size() == ch3Count);

	}
}

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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;

/**
 * @author Dave Hollander
 * 
 */
public class ChoiceObjectTests {
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
		LibraryNode ln = mockLibrary.createNewLibrary("http://example.com/choice", "CT", pc.getDefaultProject());
		ChoiceObjectNode cn = mockLibrary.addChoice(ln, "ChoiceTest1");

		checkChoice(cn);
	}

	@Test
	public void getFacetsTests() {
		LibraryNode ln2 = mockLibrary.createNewLibrary("http://example.com/choice", "CT", pc.getDefaultProject());
		ChoiceObjectNode c1 = mockLibrary.addChoice(ln2, "Choice");

		assertTrue("Must have shared facet.", c1.getSharedFacet() != null);

		int cfCnt = c1.getChoiceFacets().size();
		c1.addFacet("cf1");
		c1.addFacet("cf2");
		assertTrue("Must have two more choice facets.", c1.getChoiceFacets().size() == cfCnt + 2);
	}

	@Test
	public void fileReadTest() throws Exception {
		LibraryNode testLib = new LoadFiles().loadFile6(mc);
		LibraryNode ln2 = mockLibrary.createNewLibrary("http://example.com/choice", "CT", pc.getDefaultProject());
		ChoiceObjectNode extendedChoice = mockLibrary.addChoice(ln2, "ExtendedChoice");

		new LibraryChainNode(testLib); // Test in a chain

		for (Node choice : testLib.getDescendants_NamedTypes()) {
			if (choice instanceof ChoiceObjectNode) {
				checkChoice((ChoiceObjectNode) choice);

				extendedChoice.setExtension(choice);
				checkChoice((ChoiceObjectNode) choice);
				checkChoice(extendedChoice);
			}
		}
	}

	@Test
	public void extensionTests() {
		// Given the choice test file with 2 choice objects
		LibraryNode ln = new LoadFiles().loadFile_Choice(defaultProject);
		// new LibraryChainNode(ln); // Test in a chain

		ChoiceObjectNode choice = null;
		ChoiceObjectNode extChoice = null;
		for (Node n : ln.getDescendants_NamedTypes())
			if (n instanceof ChoiceObjectNode) {
				if (((ChoiceObjectNode) n).getExtensionBase() == null)
					choice = (ChoiceObjectNode) n;
				else
					extChoice = (ChoiceObjectNode) n;
			}
		assertTrue("Must have base choice object.", choice != null);
		assertTrue("Choice must have 3 contextual facets.", getContextualFacets(choice).size() == 3);
		assertTrue("Must have extended choice object.", extChoice != null);
		assertTrue("Extended choice must have 4 contextual facets.", getContextualFacets(extChoice).size() == 4);

		// Given - the choice extension should work exactly like business object.
		BusinessObjectNode bo = null;
		BusinessObjectNode exBo = null;
		for (Node n : ln.getDescendants_NamedTypes())
			if (n instanceof BusinessObjectNode) {
				if (((BusinessObjectNode) n).getExtensionBase() == null)
					bo = (BusinessObjectNode) n;
				else
					exBo = (BusinessObjectNode) n;
			}
		assertTrue("Must have base business object.", bo != null);
		assertTrue("BO must have 4 contextual facets.", getContextualFacets(bo).size() == 4);
		assertTrue("Must have extended business object.", exBo != null);
		assertTrue("Extended BO must have 6 contextual facets.", getContextualFacets(exBo).size() == 6);
	}

	private List<ContextualFacetNode> getContextualFacets(Node container) {
		ArrayList<ContextualFacetNode> facets = new ArrayList<ContextualFacetNode>();
		for (Node n : container.getDescendants())
			if (n instanceof ContextualFacetNode)
				facets.add((ContextualFacetNode) n);
		return facets;
	}

	public void checkChoice(ChoiceObjectNode choice) {
		Assert.assertTrue(choice instanceof ChoiceObjectNode);

		// Validate model and tl object
		assertTrue(choice.getTLModelObject() instanceof TLChoiceObject);
		assertNotNull(choice.getTLModelObject().getListeners());
		TLChoiceObject tlChoice = (TLChoiceObject) choice.getTLModelObject();

		if (tlChoice.getOwningLibrary() != null)
			Assert.assertNotNull(choice.getLibrary());
		String s = tlChoice.getName();

		// must have shared facet
		assertNotNull(choice.getSharedFacet());
		s = ((FacetNode) choice.getSharedFacet()).getName();
		s = ((FacetNode) choice.getSharedFacet()).getLabel();

		// make sure this does not NPE
		List<PropertyOwnerInterface> choices = choice.getChoiceFacets();
		// can be empty - assertTrue(!choices.isEmpty());

		// For choice facets the Name and label should be not empty
		for (PropertyOwnerInterface poi : choice.getChoiceFacets()) {
			assertTrue(poi instanceof FacetNode);
			FacetNode f = (FacetNode) poi;
			String name = f.getName();
			assertFalse(name.isEmpty());
			String label = f.getLabel();
			assertFalse(f.getLabel().isEmpty());
			assertTrue(((Node) poi).getParent() == choice);
		}

		// Does this extend another choice? If so, examine inherited children
		boolean hasBaseClass = choice.getExtensionBase() != null;
		if (hasBaseClass) {
			Node baseClass = choice.getExtensionBase();
			// Test File 6 has an extended choice - make sure it inherits correctly.
			if (choice.getName().equals("ExtendedChoice")) {
				for (Node n : choice.getChildren())
					if (n instanceof FacetNode) {
						assertTrue(((Node) n).getParent() != null);

						List<TLAttribute> tlAttrs = PropertyCodegenUtils.getInheritedFacetAttributes((TLFacet) n
								.getTLModelObject());
						List<Node> inheritedList = n.getInheritedChildren();
						if (inheritedList.isEmpty()) {
							List<Node> x = n.getInheritedChildren();
						}

						// assert !inheritedList.isEmpty();
						// assert inheritedList.size() == 3;
					}
			}
			assertNotNull(baseClass);
		}

		//
		// TODO - add test case where a new facet is added to the extension
		//

		// Get Equivalent
		// Get Aliases
		assertNotNull(choice.getAliases());

	}
}

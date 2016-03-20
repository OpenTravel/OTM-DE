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
	public void fileReadTest() throws Exception {
		LibraryNode testLib = new LoadFiles().loadFile6(mc);
		new LibraryChainNode(testLib); // Test in a chain

		for (Node choice : testLib.getDescendants_NamedTypes()) {
			if (choice instanceof ChoiceObjectNode)
				checkChoice((ChoiceObjectNode) choice);
		}
	}

	// @Test
	// public void mockCoreTest() {
	// ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
	// new LibraryChainNode(ln); // Test in a chain
	// }

	private void checkChoice(ChoiceObjectNode choice) {
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
		assertTrue(!choices.isEmpty());

		// For choice facets the Name and label should be not empty
		for (PropertyOwnerInterface poi : choice.getChoiceFacets()) {
			assertTrue(poi instanceof FacetNode);
			FacetNode f = (FacetNode) poi;
			String name = f.getName();
			assertFalse(name.isEmpty());
			String label = f.getLabel();
			assertFalse(f.getLabel().isEmpty());
		}

		// Does this extend another choice? If so, examine inherited children
		boolean hasBaseClass = choice.getExtensionBase() != null;
		if (hasBaseClass) {
			Node baseClass = choice.getExtensionBase();
			// Test File 6 has an extended choice - make sure it inherits correctly.
			if (choice.getName().equals("ExtendedChoice")) {
				for (Node n : choice.getChildren())
					if (n instanceof FacetNode) {
						List<TLAttribute> tlAttrs = PropertyCodegenUtils.getInheritedFacetAttributes((TLFacet) n
								.getTLModelObject());
						List<Node> inheritedList = n.getInheritedChildren();
						if (inheritedList.isEmpty()) {
							List<Node> x = n.getInheritedChildren();
						}

						assert !inheritedList.isEmpty();
						assert inheritedList.size() == 3;
						// check parent and owner of inherited...assure they are not n and n.getOwner()
						//
						// make sure we inherit ChoiceB
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

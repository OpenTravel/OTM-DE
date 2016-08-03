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
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class AliasTests {
	static final Logger LOGGER = LoggerFactory.getLogger(AliasTests.class);

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
	public void aliasConstructors() {

		// Given two libraries, one managed one not managed
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);
		assertTrue("Library must exist.", ln != null);
		assertTrue("Library must exist.", ln_inChain != null);

		testConstructors(ln);
		testConstructors(ln_inChain);
	}

	@Test
	public void aliasMethods() {
		// TODO
	}

	private void testConstructors(LibraryNode ln) {
		// Given - objects used in constructors
		TLAlias tlAlias = new TLAlias();
		BusinessObjectNode parent1 = ml.addBusinessObjectToLibrary(ln, "bo2");
		CoreObjectNode parent2 = ml.addCoreObjectToLibrary(ln, "co1");
		List<Node> navChildren = new ArrayList<Node>();

		// When - Aliases created using each constructor
		AliasNode a1 = new AliasNode(parent1, "a1");
		AliasNode a2 = new AliasNode(parent2, tlAlias);

		// Then - alias 1
		assertTrue("Alias 1 must have parent.", a1.getParent() == parent1);
		assertTrue("Alias TL Object must be present.", a1.getTLModelObject() instanceof TLAlias);
		assertTrue("Parent TLObject must have alias.", !((TLBusinessObject) parent1.getTLModelObject()).getAliases()
				.isEmpty());
		// Then - must not NPE as accessed in library tree content providers
		navChildren.addAll(a1.getNavChildren()); // must not NPE

		// Then - alias 2
		assertTrue("Alias 2 must have parent.", a2.getParent() == parent2);
		assertTrue("Alias TL Object must be present.", a2.getTLModelObject() instanceof TLAlias);
		assertTrue("Parent TLObject must have alias.", !((TLCoreObject) parent2.getTLModelObject()).getAliases()
				.isEmpty());
		navChildren.addAll(a2.getNavChildren()); // must not NPE

		// Then - Children aliases are created
		for (Node facet : parent1.getChildren()) {
			if (!(facet instanceof FacetNode))
				continue;
			boolean found = false;
			for (Node child : facet.getChildren()) {
				assertTrue("Child's owning component must be parent.", child.getOwningComponent() == parent1);
				assertTrue("Child's parent must be facet.", child.getParent() == facet);
				navChildren.addAll(child.getNavChildren()); // must not NPE
				if (child instanceof AliasNode)
					found = true;
			}
			assertTrue("Facets must have an alias as child node.", found == true);
		}
	}
}

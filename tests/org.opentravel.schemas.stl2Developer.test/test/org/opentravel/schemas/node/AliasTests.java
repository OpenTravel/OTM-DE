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
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;
import org.opentravel.schemas.types.TypeProvider;
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
	Library_FunctionTests lt = new Library_FunctionTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeAllTests() {
		mc = OtmRegistry.getMainController();
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
		// Given - a business object with an alias
		String name = "a1";
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		BusinessObjectNode parent1 = ml.addBusinessObjectToLibrary(ln, "bo2");
		AliasNode a1 = new AliasNode(parent1, name);
		for (Node n : parent1.getDescendants())
			if (n instanceof AliasNode)
				assertTrue(n.getName().contains(name));

		// When - Set Name
		name = "SomeNewName543321";
		a1.setName(name);
		// Then - assure all aliases get renamed
		for (Node n : parent1.getDescendants())
			if (n instanceof AliasNode)
				assertTrue(n.getName().contains(name));

		// When - deleted
		for (Node n : parent1.getChildren())
			if (n instanceof AliasNode)
				n.delete();
		// Then - assure all aliases get removed
		boolean found = false;
		for (Node n : parent1.getDescendants())
			if (n instanceof AliasNode)
				found = true;
		assertTrue(found == false);

		// TODO
	}

	private void testConstructors(LibraryNode ln) {
		// Given - objects used in constructors
		final String a2Name = "Co1Alias1";
		TLAlias tlAlias = new TLAlias();
		tlAlias.setName(a2Name);
		BusinessObjectNode parent1 = ml.addBusinessObjectToLibrary(ln, "bo2");
		CoreObjectNode parent2 = ml.addCoreObjectToLibrary(ln, "co1");
		List<Node> navChildren = new ArrayList<Node>();
		assertTrue(!parent1.getChildren().isEmpty());
		assertTrue(!parent2.getChildren().isEmpty());

		// When - Aliases created using each constructor
		AliasNode a1 = new AliasNode(parent1, "a1");
		AliasNode a2 = new AliasNode(parent2, tlAlias);

		// Then - alias 1
		assertTrue(a1.getName().equals("a1"));
		assertTrue("Alias 1 must have parent.", a1.getParent() == parent1);
		assertTrue("Alias TL Object must be present.", a1.getTLModelObject() instanceof TLAlias);
		assertTrue("Parent TLObject must have alias.", !parent1.getTLModelObject().getAliases().isEmpty());
		// Then - must not NPE as accessed in library tree content providers
		assertTrue(parent1.getChildren().contains(a1));
		List<Node> sKids = parent1.getFacet_Summary().getChildren();
		boolean found = false;
		for (Node n : sKids)
			if (n instanceof AliasNode)
				found = true;
		assertTrue(found);
		navChildren.addAll(a1.getNavChildren(false)); // must not NPE
		assertTrue("Must not be null.", a1.getNavChildren(false) != null);
		check(a1);

		// Then - alias 2
		assertTrue(a2.getName().equals(a2Name));
		assertTrue("Alias TL Object must be present.", a2.getTLModelObject() instanceof TLAlias);
		assertTrue("Alias 2 must have parent.", a2.getParent() == parent2);
		List<Node> cKids = parent2.getChildren();
		assertTrue("Core must have alias2.", parent2.getChildren().contains(a2));
		assertTrue("Parent TLObject must have alias.", !parent2.getTLModelObject().getAliases().isEmpty());
		assertTrue("Must not be null.", a2.getNavChildren(false) != null);
		check(a2);
		// Then - Children aliases are created
		for (Node facet : parent1.getChildren()) {
			if (!(facet instanceof FacetOMNode))
				continue;
			AliasNode fa = null;
			for (Node child : facet.getChildren()) {
				if (child instanceof AliasNode) {
					assertTrue("Child's owning component must be parent.", child.getOwningComponent() == parent1);
					assertTrue("Child's parent must be facet.", child.getParent() == facet);
					assertTrue("Must not be null.", child.getNavChildren(false) != null);
					fa = (AliasNode) child;
				}
			}
			// Aliases are not produced for empty facets
			if (!facet.getChildren().isEmpty())
				assertTrue("Facets must have an alias as child node.", fa != null);
			check(fa);
		}
	}

	public boolean check(AliasNode alias) {
		if (alias == null)
			return false;
		assertTrue("Alias must have parent.", alias.getParent() != null);
		assertTrue("Alias TL Object must be present.", alias.getTLModelObject() instanceof TLAlias);
		assertTrue("Parent TLObject must have alias.", !((TLAliasOwner) alias.getParent().getTLModelObject())
				.getAliases().isEmpty());

		assertTrue("Alias must have owning component.", alias.getOwningComponent() != null);
		assertTrue(alias.getOwningComponent() instanceof TypeProvider);
		assertTrue("Owing component test must run.",
				!((TypeProvider) alias.getOwningComponent()).isRenameableWhereUsed());
		return true;
	}
}

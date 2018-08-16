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

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class AliasTests extends BaseTest {
	static final Logger LOGGER = LoggerFactory.getLogger(AliasTests.class);

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
			if (n instanceof AliasNode) {
				n.delete();
				assert n.isDeleted();
			}
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
		List<Node> navChildren = new ArrayList<>();
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

	@Test
	public void Alias_asTypeTests() {
		// Given - a list of all aliases created in test
		ArrayList<AliasNode> aliases = new ArrayList<>();
		// Given - one of each type with an alias
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		ml.addOneOfEach(ln, "AsType");
		for (LibraryMemberInterface lm : ln.get_LibraryMembers())
			if (lm instanceof AliasOwner) {
				aliases.add(new AliasNode((AliasOwner) lm, lm.getName() + "_Alias"));

				// Get all aliases on the children of LM
				for (Node n : lm.getChildren())
					for (Node cn : n.getChildren())
						if (cn instanceof AliasNode)
							aliases.add((AliasNode) cn);
			}

		// Given - an element to assign to
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");
		ElementNode ele = new ElementNode(bo.getFacet_Summary(), "E1");

		// When - alias assigned to element
		TypeProvider tp = null;
		for (AliasNode a : aliases) {
			tp = ele.setAssignedType(a);
			// All the names are modified a little bit.
			assertTrue("Must be assigned a TLAlias.", ele.getAssignedTLObject() instanceof TLAlias);
			assertTrue("Element must have aliasNode as assigned type.", ele.getAssignedType() instanceof AliasNode);
			assertTrue("Element must have alias node assigned.", a == ele.getAssignedType());
		}

		// When - alias assigned to attributes
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "TestVWA");
		AttributeNode attr = new AttributeNode(vwa.getFacet_Attributes(), "a1");
		for (AliasNode a : aliases) {
			// Then - if allowed, the actual type is assigned and the alias is not
			tp = attr.setAssignedType(a);
			if (attr.canAssign(a))
				assertTrue("Alias must have caused a type provider to be assigned.", tp != null);
			if (tp != null) {
				assertTrue("Must NOT be assigned a TLAlias.", !(attr.getAssignedTLObject() instanceof TLAlias));
				assertTrue("Must be assigned a to alias parent.", attr.getAssignedType() == a.getParent());
			}
		}
	}

	public boolean check(AliasNode alias) {
		if (alias == null)
			return false;
		assertTrue("Alias must have parent.", alias.getParent() != null);
		assertTrue("Alias TL Object must be present.", alias.getTLModelObject() instanceof TLAlias);
		assertTrue("Parent TLObject must have alias.",
				!((TLAliasOwner) alias.getParent().getTLModelObject()).getAliases().isEmpty());

		assertTrue("Alias must have owning component.", alias.getOwningComponent() != null);
		assertTrue(alias.getOwningComponent() instanceof TypeProvider);
		assertTrue("Owing component test must run.",
				!((TypeProvider) alias.getOwningComponent()).isRenameableWhereUsed());
		return true;
	}
}

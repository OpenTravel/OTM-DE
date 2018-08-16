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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import junit.framework.Assert;
import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.TypeUserAssignmentListener;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereExtendedHandler.WhereExtendedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ReplaceWith_Tests extends BaseTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceWith_Tests.class);

	@Test
	public void ReplaceAll_Tests() throws Exception {
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		LibraryNode ln2 = ml.createNewLibrary(defaultProject.getNSRoot() + "/t", "test2", defaultProject);

		// Given - each type provider in a core is being used as a type
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty(ln, "BO");
		CoreObjectNode core = ml.addCoreObjectToLibrary_Empty(ln, "Test");
		int i = 1;
		ElementNode e = null;
		e = new ElementNode(bo.getFacet_Summary(), "n" + i++);
		e.setAssignedType(core);
		for (TypeProvider d : core.getDescendants_TypeProviders()) {
			e = new ElementNode(bo.getFacet_Summary(), "n" + i++);
			e.setAssignedType(d);
		}
		// Then - all core type providers must be assigned
		for (TypeProvider p : core.getDescendants_TypeProviders())
			assertTrue("Must NOT be empty.", !p.getWhereAssigned().isEmpty());

		// Given - a different core with same name in a different library where all children have namespaces
		CoreObjectNode replacement = ml.addCoreObjectToLibrary_Empty(ln2, "Test");
		assertTrue("Core names must be same for exact replacement matches.",
				core.getName().equals(replacement.getName()));
		for (TypeProvider c : replacement.getDescendants_TypeProviders())
			assertTrue("Must have namespace.", c.getNamespace().equals(ln2.getNamespace()));

		// When - 1st core is replaced by second core
		((TypeProvider) core).getWhereAssignedHandler().replaceAll(replacement, null);

		// Then - all property types should be from replacement lib
		for (Node p : bo.getFacet_Summary().getChildren()) {
			TypeProvider type = ((TypeUser) p).getAssignedType();
			assertTrue("Must be in ln2 library.", type.getLibrary() == ln2);
			assertTrue("Must have ln2 namespace", ((Node) type).getNamespace().equals(ln2.getNamespace()));
			assertTrue("Must have listener", hasAssignmentListener((TypeUser) p, type));
		}
		// Then - all original core type providers should not be assigned
		for (TypeProvider p : core.getDescendants_TypeProviders())
			assertTrue("Must be empty.", p.getWhereAssigned().isEmpty());

		// Then - all replacement core type providers should be assigned
		for (TypeProvider p : replacement.getDescendants_TypeProviders())
			assertTrue("Must NOT be empty.", !p.getWhereAssigned().isEmpty());
	}

	@Test
	public void ExtensionHanderSet_Tests() throws Exception {
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);

		// Given - extension is an instance of base
		Node base = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		ExtensionOwner extension = ml.addBusinessObjectToLibrary(ln, "ExtBO");
		extension.setExtension(base);
		assertTrue(extension.isInstanceOf(base));
		assertTrue(hasWhereExtendedListener(extension, base));
		assertTrue(base.getWhereExtendedHandler().getWhereExtended().contains(extension));

		// When - cleared by setting to null
		extension.getExtensionHandler().set(null);
		assertFalse(extension.isInstanceOf(base));
		assertFalse(hasWhereExtendedListener(extension, base));
		assertFalse(base.getWhereExtendedHandler().getWhereExtended().contains(extension));

		// When replaced with new base
		extension.setExtension(base);
		Node newBase = ml.addBusinessObjectToLibrary_Empty(ln, "NewBaseBO");
		extension.getExtensionHandler().set(newBase);

		// Then - assure base assignment and listeners are correct.
		assertTrue(extension.isInstanceOf(newBase));
		assertTrue(hasWhereExtendedListener(extension, newBase));
		assertTrue(newBase.getWhereExtendedHandler().getWhereExtended().contains(extension));
		// And - old base type is no longer extended
		assertFalse(extension.isInstanceOf(base));
		assertFalse(hasWhereExtendedListener(extension, base));
		assertFalse(base.getWhereExtendedHandler().getWhereExtended().contains(extension));

		// When - using Node.replaceTypesWith()

		// Extension Point
		// Core
		// Choice
		// VWA
		// Open Enum
		// Closed enum
	}

	@Test
	public void ReplaceBaseTypesInDifferentLibrary_Test() throws Exception {
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		LibraryNode ln2 = ml.createNewLibrary(defaultProject.getNSRoot() + "/ln2", "ln2", defaultProject);

		// Given - one core extends another
		CoreObjectNode baseCore = ml.addCoreObjectToLibrary_Empty(ln, "base");
		CoreObjectNode extCore = ml.addCoreObjectToLibrary(ln, "ext");
		((ExtensionOwner) extCore).setExtension(baseCore);

		// When base core is replaced with a new one in different library
		CoreObjectNode newBase = ml.addCoreObjectToLibrary_Empty(ln2, "newCore");
		baseCore.replaceTypesWith(newBase, null);

		// Then - ext should extend new base
		assertTrue(newBase.getWhereExtendedHandler().getWhereExtended().contains(extCore));
		assertFalse(baseCore.getWhereExtendedHandler().getWhereExtended().contains(extCore));
	}

	public boolean hasWhereExtendedListener(ExtensionOwner extension, Node base) {
		for (ModelElementListener l : extension.getTLModelObject().getListeners())
			if (l instanceof WhereExtendedListener)
				return ((WhereExtendedListener) l).getNode() == base;
		return false;
	}

	public boolean hasAssignmentListener(TypeUser user, TypeProvider type) {
		boolean result = false;
		int count = 0;
		for (ModelElementListener l : user.getTLModelObject().getListeners())
			if (l instanceof TypeUserAssignmentListener) {
				if (((TypeUserAssignmentListener) l).getNode() == type)
					result = true;
				count++;
			}
		if (count > 1)
			LOGGER.debug("Error - too many where assigned listeners.");
		return result;
	}

	// Use purpose built objects to test specific behaviors.
	@Test
	public void ReplaceTypesTest() throws Exception {
		// Given - controllers, project and unmanaged library
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);

		// Given - one of each object type
		ml.addOneOfEach(ln, "RTT");
		ml.check(ln);

		// Given - local variables for different object types
		SimpleTypeNode simple = null;
		VWA_Node vwa = null;
		CoreObjectNode core = null, core2 = null;
		BusinessObjectNode bo = null, bo2 = null;
		for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers()) {
			if (n instanceof SimpleTypeNode)
				simple = (SimpleTypeNode) n;
			else if (n instanceof VWA_Node)
				vwa = (VWA_Node) n;
			else if (n instanceof CoreObjectNode)
				core = (CoreObjectNode) n;
			else if (n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
		}
		assertNotNull(core);
		assertNotNull(bo);
		assertNotNull(simple);
		assertNotNull(vwa);

		// Given - clone made of core
		core2 = (CoreObjectNode) core.clone(core.getLibrary(), null);
		core2.setName("core2");
		ml.check(core2);

		// Given - clone made of bo
		bo2 = (BusinessObjectNode) bo.clone(bo.getLibrary(), null);
		bo2.setName("bo2");
		ml.check(bo2);

		replaceProperties(bo, core2, core);
		replaceProperties(bo2, core, core2);

		replaceProperties(bo, simple, core);
		replaceProperties(core, simple, core2);
		replaceProperties(vwa, simple, core);
	}

	/**
	 * Assign p1 to all of owner's type users. Then use replaceTypeWith() to replace p1 assignments with p2.
	 * 
	 * @param owner
	 * @param p1
	 * @param p2
	 */
	private void replaceProperties(Node owner, TypeProvider p1, Node p2) {
		ml.check(owner);
		for (TypeUser n : owner.getDescendants_TypeUsers())
			// Only do one so the owner remains valid
			if (n.setAssignedType(p1) != null)
				break;

		ml.check(owner);

		((Node) p1).replaceTypesWith((TypeProvider) p2, owner.getLibrary());

		// Then - check object
		ml.check(owner);
	}

	@Test
	public void ReplaceTest() throws Exception {
		LibraryNode l5 = lf.loadFile5(mc);
		l5.setEditable(true);
		LibraryNode l1 = lf.loadFile1(mc);
		l1.setEditable(true);
		int beforeCnt1 = l1.getDescendants_LibraryMembers().size();
		int beforeCnt5 = l5.getDescendants_LibraryMembers().size();

		ml.check(l1, false);
		ml.check(l5, false);

		replaceMembers(l1, l1);
		replaceMembers(l1, l5);

		ml.check(l1, false);
		ml.check(l5, false);

		Assert.assertEquals(beforeCnt1, l1.getDescendants_LibraryMembers().size());
		Assert.assertEquals(beforeCnt5, l5.getDescendants_LibraryMembers().size());
	}

	@Test
	public void swap() throws Exception {
		LibraryNode l5 = lf.loadFile5(mc);
		l5.setEditable(true);
		LibraryNode l1 = lf.loadFile1(mc);
		l1.setEditable(true);
		ml.check(l1, false);
		ml.check(l5, false);
		Node oldPhones5 = l5.findLibraryMemberByName("Phones");
		assert oldPhones5 != null;

		List<TypeUser> phonesUsers = oldPhones5.getDescendants_TypeUsers();
		List<TypeProvider> phonesProviders = new ArrayList<>();
		for (TypeUser user : phonesUsers)
			phonesProviders.add(user.getAssignedType());

		// When
		swap(l1, l5);

		// Then
		ml.check(l1, false);
		ml.check(l5, false);
	}

	@Test
	public void CombinedTest() throws Exception {

		LibraryNode l5 = lf.loadFile5(mc);
		l5.setEditable(true);
		LibraryNode l1 = lf.loadFile1(mc);
		l1.setEditable(true);
		ml.check(l1, false);
		ml.check(l5, false);

		int beforeCnt1 = l1.getDescendants_LibraryMembers().size();
		int beforeCnt5 = l5.getDescendants_LibraryMembers().size();

		replaceMembers(l1, l5);
		replaceMembers(l5, l1);
		ml.check(l1, false); // constraints may be invalid
		ml.check(l5, false);

		Assert.assertEquals(beforeCnt1, l1.getDescendants_LibraryMembers().size());
		Assert.assertEquals(beforeCnt5, l5.getDescendants_LibraryMembers().size());

		swap(l1, l5);
		ml.check(l1, false);
		ml.check(l5, false);
		// Assert.assertEquals(beforeCnt5, l5.getDescendants_NamedTypes().size());
	}

	/**
	 * For all named types in target,
	 * 
	 * @param ls
	 * @param lt
	 */
	private void replaceMembers(LibraryNode ls, LibraryNode lt) {
		// Sort the list so that the order is consistent with each test.
		List<TypeProvider> targets = lt.getDescendants_TypeProviders();
		Collections.sort(targets, lt.new TypeProviderComparable());
		List<TypeProvider> sources = ls.getDescendants_TypeProviders();
		Collections.sort(sources, ls.new TypeProviderComparable());
		int cnt = sources.size();

		// Replace types with one pseudo-randomly selected from target library.
		for (TypeProvider n : targets) {
			if (n.getWhereAssigned().size() > 0) {
				// Note - many of these will not be allowed.
				((Node) n).replaceTypesWith(sources.get(--cnt), null);
				// LOGGER.debug(" replaced " + n + " with " + sources.get(cnt));
				Assert.assertTrue(sources.get(cnt).getLibrary() != null);
				Assert.assertTrue(n.getLibrary() != null);
			}
			if (cnt <= 0)
				cnt = sources.size();
		}
		// LOGGER.debug("Replaced " + sources.size() + " - " + cnt);
	}

	/**
	 * Remove type providers from source library and replace with its counter parts in the target library.
	 * 
	 * @param source
	 *            - library will get smaller
	 * @param target
	 *            - library will stay same size but have replaced types.
	 */
	private void swap(LibraryNode source, LibraryNode target) {
		Map<String, Node> sources = new HashMap<>();
		for (Node n : source.getDescendants_LibraryMembersAsNodes())
			sources.put(n.getName(), n);

		// Now, replace the nodes within their structures.
		for (Node n : target.getDescendants_LibraryMembersAsNodes()) {
			if (n instanceof ServiceNode)
				continue;
			Node lsNode = sources.get(n.getName());
			if (lsNode != null) {
				ml.check(lsNode, false);
				ml.check(n, false);
				n.replaceTypesWith((TypeProvider) lsNode, null);
				ml.check(lsNode, false);
				ml.check(n, false);
			} else
				LOGGER.debug(n + " was not found in source library.");
		}
	}

}

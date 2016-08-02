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

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

//import junit.framework.Assert;
import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TestTypes;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereAssignedHandler.WhereAssignedListener;
import org.opentravel.schemas.types.WhereExtendedHandler.WhereExtendedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ReplaceWith_Tests {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceWith_Tests.class);

	ModelNode model = null;
	TestTypes tt = new TestTypes();

	@Test
	public void ReplaceAll_Tests() throws Exception {
		DefaultProjectController pc;
		MainController mc = new MainController();
		MockLibrary ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		LibraryNode ln2 = ml.createNewLibrary(defaultProject.getNSRoot() + "/t", "test2", defaultProject);

		// Given - each type provider in a core is being used as a type
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty(ln, "BO");
		CoreObjectNode core = ml.addCoreObjectToLibrary_Empty(ln, "Test");
		int i = 1;
		ElementNode e = null;
		e = new ElementNode(bo.getSummaryFacet(), "n" + i++);
		e.setAssignedType(core);
		for (TypeProvider d : core.getDescendants_TypeProviders()) {
			e = new ElementNode(bo.getSummaryFacet(), "n" + i++);
			e.setAssignedType(d);
		}
		// Given - a different core in a different library where all children have namespaces
		CoreObjectNode replacement = ml.addCoreObjectToLibrary_Empty(ln2, "Test");
		for (TypeProvider c : replacement.getDescendants_TypeProviders())
			assertTrue("Must have namespace.", !((Node) c).getNamespace().isEmpty());

		// When - 1st core is replaced by second core
		((TypeProvider) core).getWhereAssignedHandler().replaceAll(replacement);

		// Then - all property types should be from replacement lib
		for (Node p : bo.getSummaryFacet().getChildren()) {
			TypeProvider type = ((TypeUser) p).getAssignedType();
			assertTrue("Must be in ln2 library.", type.getLibrary() == ln2);
			assertTrue("Must have ln2 namespace", ((Node) type).getNamespace().equals(ln2.getNamespace()));
			assertTrue("Must have listener", hasWhereAssignedListener((TypeUser) p, type));
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
		DefaultProjectController pc;
		MainController mc = new MainController();
		MockLibrary ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
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
		Node newBase = ml.addBusinessObjectToLibrary(ln, "NewBaseBO");
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
		DefaultProjectController pc;
		MainController mc = new MainController();
		MockLibrary ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
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

	public boolean hasWhereAssignedListener(TypeUser user, TypeProvider type) {
		for (ModelElementListener l : user.getTLModelObject().getListeners())
			if (l instanceof WhereAssignedListener)
				return ((WhereAssignedListener) l).getNode() == type;
		return false;
	}

	// Use purpose built objects to test specific behaviors.
	@Test
	public void ReplaceTypesTest() throws Exception {
		DefaultProjectController pc;
		MainController mc = new MainController();
		MockLibrary ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
		NewComponent_Tests nc = new NewComponent_Tests();
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		nc.createNewComponents(ln);

		tt.visitAllNodes(ln);

		SimpleTypeNode simple = null;
		EnumerationClosedNode closed = null;
		VWA_Node vwa = null;
		CoreObjectNode core = null, core2 = null;
		EnumerationOpenNode open = null;
		BusinessObjectNode bo = null, bo2 = null;
		ServiceNode svc = null;
		// ExtensionPointNode ex = null;
		for (Node n : ln.getDescendants_NamedTypes()) {
			if (n instanceof SimpleTypeNode)
				simple = (SimpleTypeNode) n;
			if (n instanceof EnumerationClosedNode)
				closed = (EnumerationClosedNode) n;
			if (n instanceof EnumerationOpenNode)
				open = (EnumerationOpenNode) n;
			if (n instanceof VWA_Node)
				vwa = (VWA_Node) n;
			if (n instanceof CoreObjectNode)
				core = (CoreObjectNode) n;
			if (n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
			// if (n instanceof ExtensionPointNode)
			// ex = (ExtensionPointNode) n;
			if (n instanceof ServiceNode)
				svc = (ServiceNode) n;
		}
		core2 = (CoreObjectNode) core.clone();
		core2.setName("core2");
		core.setExtension(core2);
		bo2 = (BusinessObjectNode) bo.clone();
		bo2.setName("bo2");
		bo2.setExtension(bo);

		replaceProperties(bo, core2, core);
		replaceProperties(bo2, core, core2);

		replaceProperties(svc, simple, core);
		// replaceProperties(ex, simple, core);
		replaceProperties(bo, simple, core);
		replaceProperties(core, simple, core);
		replaceProperties(vwa, simple, core);
		tt.visitAllNodes(ln);
	}

	private void replaceProperties(Node owner, TypeProvider p1, Node p2) {
		// Set then replace all the properties of a BO.
		for (TypeUser n : owner.getDescendants_TypeUsers()) {
			n.setAssignedType(p1);
		}
		((Node) p1).replaceTypesWith(p2, owner.getLibrary());
	}

	@Test
	public void ReplaceTest() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();

		LibraryNode l5 = lf.loadFile5(mc);
		l5.setEditable(true);
		LibraryNode l1 = lf.loadFile1(mc);
		l1.setEditable(true);
		// tt.visitAllNodes(l5);
		// tt.visitAllNodes(l1);
		int beforeCnt1 = l1.getDescendants_NamedTypes().size();
		int beforeCnt5 = l5.getDescendants_NamedTypes().size();

		replaceMembers(l1, l1);
		replaceMembers(l1, l5);

		tt.visitAllNodes(l1);
		tt.visitAllNodes(l5);
		Assert.assertEquals(beforeCnt1, l1.getDescendants_NamedTypes().size());
		Assert.assertEquals(beforeCnt5, l5.getDescendants_NamedTypes().size());
	}

	@Test
	public void swap() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();

		LibraryNode l5 = lf.loadFile5(mc);
		l5.setEditable(true);
		LibraryNode l1 = lf.loadFile1(mc);
		l1.setEditable(true);
		tt.visitAllNodes(l1);
		tt.visitAllNodes(l5);

		swap(l1, l5);

		tt.visitAllNodes(l1);
		tt.visitAllNodes(l5);
	}

	@Test
	public void CombinedTest() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();

		LibraryNode l5 = lf.loadFile5(mc);
		l5.setEditable(true);
		LibraryNode l1 = lf.loadFile1(mc);
		l1.setEditable(true);
		// tt.visitAllNodes(l5);
		// tt.visitAllNodes(l1);
		int beforeCnt1 = l1.getDescendants_NamedTypes().size();
		int beforeCnt5 = l5.getDescendants_NamedTypes().size();

		replaceMembers(l1, l5);
		replaceMembers(l5, l1);
		tt.visitAllNodes(l1);
		tt.visitAllNodes(l5);

		Assert.assertEquals(beforeCnt1, l1.getDescendants_NamedTypes().size());
		Assert.assertEquals(beforeCnt5, l5.getDescendants_NamedTypes().size());

		swap(l1, l5);
		tt.visitAllNodes(l1);
		tt.visitAllNodes(l5);
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
		List<Node> targets = lt.getDescendants_NamedTypes();
		Collections.sort(targets, lt.new NodeComparable());
		List<Node> sources = ls.getDescendants_NamedTypes();
		Collections.sort(sources, ls.new NodeComparable());
		int cnt = sources.size();

		// Replace types with one pseudo-randomly selected from target library.
		for (Node n : targets) {
			if (n instanceof TypeProvider && ((TypeProvider) n).getWhereAssigned().size() > 0) {
				// Note - many of these will not be allowed.
				n.replaceTypesWith(sources.get(--cnt), null);
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
		// Now, replace the nodes within their structures.
		int i = 1;
		for (Node n : target.getDescendants_NamedTypes()) {
			if (n instanceof ServiceNode)
				continue;
			Node lsNode = NodeFinders.findTypeProviderByQName(new QName(source.getNamespace(), n.getName()), source);
			// Node lsNode = source.findNode(n.getName(), source.getNamespace());
			if (lsNode != null) {
				n.swap(lsNode);
				tt.visitTypeNode(lsNode);
			} else
				LOGGER.debug(n + " was not found in source library.");
		}
	}

}

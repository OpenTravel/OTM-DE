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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class BusinessObjectTests {
	static final Logger LOGGER = LoggerFactory.getLogger(MockLibrary.class);

	TestNode tn = new NodeTesters().new TestNode();

	@Test
	public void businessObjectTest() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();

		LibraryNode lib = lf.loadFile4(mc);
		int i = 0;
		for (Node bo : lib.getDescendants_NamedTypes()) {
			i++;
			if (bo instanceof BusinessObjectNode)
				checkBO((BusinessObjectNode) bo);
		}
		// Repeat test with library in a chain
		LibraryChainNode lcn = new LibraryChainNode(lib);
		for (Node bo : lcn.getDescendants_NamedTypes()) {
			i--;
			if (bo instanceof BusinessObjectNode)
				checkBO((BusinessObjectNode) bo);
		}
		Assert.assertEquals(0, i); // Make sure we didn't lose objects when library was managed
	}

	@Test
	public void extendedBO() {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		MockLibrary ml = new MockLibrary();

		LibraryNode ln = lf.loadFile4(mc);
		LibraryChainNode lcn = new LibraryChainNode(ln); // Test in managed library
		ln.setEditable(true);

		BusinessObjectNode extendedBO = ml.addBusinessObjectToLibrary(ln, "ExtendedBO");
		assertNotNull("Null object created.", extendedBO);

		for (Node n : ln.getDescendants_NamedTypes())
			if (n instanceof BusinessObjectNode && n != extendedBO) {
				extendedBO.setExtension(n);
				checkBO((BusinessObjectNode) n);
				checkBO(extendedBO);
			}

	}

	@Test
	public void changeToBO() {
		MockLibrary ml = new MockLibrary();
		MainController mc = new MainController();
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();

		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode tbo = null;
		ml.addBusinessObjectToLibrary(ln, "bo");
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "vwa");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "co");
		new ElementNode(core.getSummaryFacet(), "TE2").setAssignedType(vwa);

		int typeCount = ln.getDescendants_NamedTypes().size();
		Assert.assertEquals(1, vwa.getWhereAssigned().size());

		tbo = (BusinessObjectNode) core.changeToBusinessObject();
		BusinessObjectNode tboVwa = (BusinessObjectNode) vwa.changeToBusinessObject();

		// Fail if in the list more than once.
		Assert.assertTrue("Not child", tbo.getParent().getChildren().contains(tbo));
		Assert.assertTrue(tbo.getParent().getChildren().indexOf(tbo) == tbo.getParent().getChildren().lastIndexOf(tbo));
		Assert.assertTrue(tboVwa.getParent().getChildren().indexOf(tboVwa) == tboVwa.getParent().getChildren()
				.lastIndexOf(tboVwa));

		core.setLibrary(tbo.getLibrary());
		core.delete(); // does nothing unless lib is not null
		vwa.setLibrary(tboVwa.getLibrary());
		vwa.delete();

		checkBO(tbo);
		checkBO(tboVwa);
		tn.visit(ln);
		Assert.assertEquals(typeCount, ln.getDescendants_NamedTypes().size());
		Assert.assertEquals(1, tboVwa.getWhereAssigned().size());

		// FIXME
		// is CO duplicated in the family?

		// Same test, but as part of a chain
		LibraryChainNode lcn = new LibraryChainNode(ln); // make sure is version safe
		core = ml.addCoreObjectToLibrary(ln, "co2");
		vwa = ml.addVWA_ToLibrary(ln, "vwa2");
		new ElementNode(core.getSummaryFacet(), "TestElement").setAssignedType(vwa);

		typeCount = ln.getDescendants_NamedTypes().size();
		Assert.assertEquals(typeCount, lcn.getDescendants_NamedTypes().size()); // check get descendants

		tbo = (BusinessObjectNode) core.changeToBusinessObject();
		tboVwa = (BusinessObjectNode) vwa.changeToBusinessObject();

		core.setLibrary(tbo.getLibrary());
		core.delete(); // does nothing unless lib is not null
		vwa.setLibrary(tboVwa.getLibrary());
		vwa.delete();

		int tc2 = lcn.getDescendants_NamedTypes().size();
		// on a chain only aggregates are counted. Must be equal to library count.
		Assert.assertEquals(tc2, typeCount);
		// printDescendants(ln);
		// printDescendants(lcn);

		Assert.assertEquals(typeCount, ln.getDescendants_NamedTypes().size());
		Assert.assertEquals(1, tboVwa.getWhereAssigned().size());
	}

	/**
	 * Check the business object.
	 */
	public void checkBO(BusinessObjectNode bo) {
		tn.visit(bo);

		Assert.assertNotNull(bo.getLibrary());
		Assert.assertTrue(bo instanceof BusinessObjectNode);
		Assert.assertNull(bo.getAttributeFacet());

		// must have 3 children
		Assert.assertTrue(3 <= bo.getChildren().size());

		assertNotNull("Must have id facet.", bo.getIDFacet());
		assertTrue("Facet parent must be bo.", ((Node) bo.getIDFacet()).getParent() == bo);

		assertNotNull("Must have summary facet.", bo.getSummaryFacet());
		assertTrue("Facet parent must be bo.", ((Node) bo.getSummaryFacet()).getParent() == bo);
		for (Node property : bo.getSummaryFacet().getChildren()) {
			assertTrue(property instanceof PropertyNode);
			assertTrue(property.getType() != null);
			assertTrue(property.getLibrary() == bo.getLibrary());
			assertTrue(property.getParent() == bo.getSummaryFacet());
		}

		assertNotNull("Must have detail facet.", bo.getDetailFacet());
		assertTrue("Facet parent must be bo.", ((Node) bo.getDetailFacet()).getParent() == bo);
		for (Node property : bo.getDetailFacet().getChildren()) {
			assertTrue(property instanceof PropertyNode);
			assertTrue(property.getType() != null);
			assertTrue("Must have name.", !property.getType().getName().isEmpty());
			assertTrue(property.getLibrary() == bo.getLibrary());
			assertTrue(property.getParent() == bo.getDetailFacet());
		}

		tn.visit(bo);
	}

	@Test
	public void facetAsTypeOnLoad() {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		LibraryNode ln = lf.loadFile1(mc);
		ln.setEditable(true);

		// Find an element to use to make sure all facets can be assigned as a type
		TypeUser user = null;
		for (TypeUser n : ln.getDescendants_TypeUsers())
			if (!((Node) n).getOwningComponent().getName().equals("Profile") && n instanceof ElementNode) {
				user = n;
				break;
			}
		assert user != null;
		assert user.isEditable();

		// File 1 has a business object Profile with 5 facets and 1 alias
		BusinessObjectNode bo = null;
		for (Node n : ln.getDescendants_NamedTypes())
			if (n.getName().equals("Profile"))
				bo = (BusinessObjectNode) n;
		assert bo != null;
		// Check facets
		int facetCnt = 0;
		for (Node n : bo.getChildren())
			if (n instanceof FacetNode) {
				facetCnt++;
				user.setAssignedType((TypeProvider) n);
				assert user.getAssignedType() == n;
			}
		assert facetCnt == 5;
		// check alias
		int aliasCnt = 0;
		for (Node n : bo.getChildren())
			if (n instanceof AliasNode) {
				aliasCnt++;
				user.setAssignedType((TypeProvider) n);
				assert user.getAssignedType() == n;
			}
		assert aliasCnt == 1;
	}

	@Test
	public void nameChange() {
		// On name change, all users of the BO and its aliases and facets also need to change.
		MockLibrary ml = new MockLibrary();
		MainController mc = new MainController();
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);

		final String boName = "initialBOName";
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, boName);
		bo.addAlias("boAlias");
		AliasNode alias1 = bo.getAliases().get(0);
		assertNotNull(alias1);
		AliasNode aliasSummary = null;
		for (Node n : bo.getSummaryFacet().getChildren())
			if (n instanceof AliasNode)
				aliasSummary = (AliasNode) n;
		assertNotNull(aliasSummary);

		// create a core that uses all the type provider nodes of the bo
		CoreObjectNode co = ml.addCoreObjectToLibrary(ln, "user");
		PropertyNode p1 = new ElementNode(co.getSummaryFacet(), "p1");
		p1.setAssignedType(bo);
		// assertTrue(p1.getName().equals(boName));
		// FIXME assertTrue("Invalid name: " + p1 + " should be " + bo, p1.getName().equals(boName));
		PropertyNode p2 = new ElementNode(co.getSummaryFacet(), "p2");
		p2.setAssignedType(alias1);
		PropertyNode p3 = new ElementNode(co.getSummaryFacet(), "p3");
		p3.setAssignedType(bo.getSummaryFacet());
		PropertyNode p4 = new ElementNode(co.getSummaryFacet(), "p4");
		p4.setAssignedType(aliasSummary);

		// Change the BO name and assure the properties names changed
		String ChangedName = "changedName";
		bo.setName(ChangedName);
		ChangedName = bo.getName(); // get the "fixed" name
		assertTrue(p1.getName().equals(ChangedName));
		assertTrue(p2.getName().equals(alias1.getName()));
		assertTrue(p3.getName().startsWith(ChangedName));

		// Alias on BOs must also be applied to the elements where used.
		String aliasName2 = "aliasName2";
		alias1.setName(aliasName2);
		aliasName2 = alias1.getName(); // get the "fixed" name
		assertTrue(p2.getName().equals(aliasName2));
		assertTrue(p4.getName().startsWith(aliasName2));
	}

	@Test
	public void FacetAsType() {
		// Facets as types throw the resolver off because they have type names not types.
		MockLibrary ml = new MockLibrary();
		MainController mc = new MainController();
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "tbo");
		TypeUser user = bo.getDescendants_TypeUsers().get(1);

		// NamedEntity userType = user.getTLTypeObject();
		Assert.assertNotNull(user.getAssignedTLObject());

		user.setAssignedType((TypeProvider) bo.getDetailFacet());
		Assert.assertNotNull(user.getTLModelObject());
	}

}

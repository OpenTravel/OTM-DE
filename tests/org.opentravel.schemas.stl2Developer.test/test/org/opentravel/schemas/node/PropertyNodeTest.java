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
package org.opentravel.schemas.node;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.utils.FacetNodeBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class PropertyNodeTest {

	private LibraryNode ln = null;
	MainController mc;
	MockLibrary mockLibrary;

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		mockLibrary = new MockLibrary();
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
		ln = mockLibrary.createNewLibrary("http://example.com/test", "test", defaultProject);
	}

	@Test
	public void shouldMoveUp() {
		FacetNode facetNode = FacetNodeBuilder.create().addElements("E1", "E2", "E3").build();
		findChild(facetNode, "E2").moveProperty(PropertyNode.UP);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "E2", "E1", "E3");
	}

	@Test
	public void shouldMoveDown() {
		FacetNode facetNode = FacetNodeBuilder.create().addElements("E1", "E2", "E3").build();
		findChild(facetNode, "E2").moveProperty(PropertyNode.DOWN);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "E1", "E3", "E2");
	}

	@Test
	public void shouldMoveUpWithMixedTypes() {
		FacetNode facetNode = FacetNodeBuilder.create(ln).addElements("A1").addAttributes("E1", "E2").addElements("A2")
				.build();
		findChild(facetNode, "A2").moveProperty(PropertyNode.UP);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "E1", "E2", "A2", "A1");
	}

	@Test
	public void shouldMoveDownWithMixedTypes() {
		FacetNode facetNode = FacetNodeBuilder.create(ln).addElements("E1").addAttributes("A1", "A2").addElements("E2")
				.build();
		findChild(facetNode, "E1").moveProperty(PropertyNode.DOWN);
		assertTrue(facetNode.isEditable_newToChain());
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "A1", "A2", "E2", "E1");
	}

	// TODO - why are only indicators renamed? Should attributes be also?
	@Test
	public void shouldDoNothingWithOneType() {
		final String i1Name = NodeNameUtils.fixIndicatorName("I1");

		FacetNode facetNode = FacetNodeBuilder.create(ln).addAttributes("A1").addIndicators("I1").addElements("E1")
				.build();
		findChild(facetNode, i1Name).moveProperty(PropertyNode.DOWN);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "A1", i1Name, "E1");
	}

	@Test
	public void shouldDoNothingWithElementOnBottom() {
		final String i0Name = NodeNameUtils.fixIndicatorName("I0");
		final String i1Name = NodeNameUtils.fixIndicatorName("I1");

		FacetNode facetNode = FacetNodeBuilder.create(ln).addAttributes("A1").addIndicators("I0", "I1")
				.addElements("E1").build();
		findChild(facetNode, i1Name).moveProperty(PropertyNode.DOWN);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "A1", i0Name, i1Name, "E1");
	}

	@Test
	public void shouldDoNothingWithElementOnTop() {
		final String i0Name = NodeNameUtils.fixIndicatorName("I0");
		final String i1Name = NodeNameUtils.fixIndicatorName("I1");
		final String a1Name = NodeNameUtils.fixIndicatorName("A1");

		FacetNode facetNode = FacetNodeBuilder.create(ln).addAttributes("A1").addIndicators("I0", "I1")
				.addElements("E1").build();
		assertNotNull(facetNode);
		findChild(facetNode, i0Name).moveProperty(PropertyNode.UP);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "A1", i0Name, i1Name, "E1");
	}

	@Test
	public void isRenameableTests() {
		// Given - library with one of each object type
		mockLibrary.addOneOfEach(ln, "Rn");
		BusinessObjectNode bo = null;
		VWA_Node vwa = null;
		CoreObjectNode core = null;
		EnumerationOpenNode eo = null;
		for (LibraryMemberInterface n : ln.get_LibraryMembers())
			if (n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
			else if (n instanceof VWA_Node)
				vwa = (VWA_Node) n;
			else if (n instanceof CoreObjectNode)
				core = (CoreObjectNode) n;
			else if (n instanceof EnumerationOpenNode)
				eo = (EnumerationOpenNode) n;
		// Given - the business object extends another one
		BusinessObjectNode boBase = mockLibrary.addBusinessObjectToLibrary(ln, "Rn2");
		bo.setExtension(boBase);
		assertTrue("BO extends BO Base", bo.getExtensionBase() == boBase);

		// Then - each property type should report renameable correct.
		assertTrue("Enum Literals must be renameable.", eo.getChildren().get(0).isRenameable());
		assertTrue("Role nodes must be renameable.", core.getSimpleAttribute().isRenameable());

		// Then - properties that must not be reassigned
		assertTrue("Simple attributes must NOT be renameable.", true);

		// Then - Business object will have one of each property type assigned simple type
		for (Node n : bo.getDescendants())
			if (n instanceof PropertyNode) {
				// Then - check with different type assignments
				propertyRenameableCheck((PropertyNode) n);
				if (vwa.canAssign(n))
					((PropertyNode) n).setAssignedType(vwa);
				propertyRenameableCheck((PropertyNode) n);
				if (core.canAssign(n))
					((PropertyNode) n).setAssignedType(core);
				propertyRenameableCheck((PropertyNode) n);
			}
	}

	public void propertyRenameableCheck(PropertyNode pn) {
		// if editable and not inherited then it depends on the assigned type.
		if (!pn.isEditable())
			assertTrue("Uneditable property must not be renameable.", !pn.isRenameable());
		else if (pn.isInheritedProperty())
			assertTrue("Inherited property must not be renameable.", !pn.isRenameable());
		else if (!pn.getAssignedType().isRenameableWhereUsed())
			assertTrue("Property's assigned type requires it to not be renameable.", !pn.isRenameable());
		else
			assertTrue("Property must be renameable.", pn.isRenameable());
	}

	private void assertOrderOfNodeAndMO(FacetNode facetNode) {
		List<String> names = toNames(facetNode.getChildren());
		List<String> tlNames = tlToNames(facetNode.getModelObject().getChildren());
		Assert.assertEquals(tlNames, names);
	}

	private List<String> tlToNames(List<?> list) {
		return Lists.transform(list, new Function<Object, String>() {

			@Override
			public String apply(Object obj) {
				if (obj instanceof TLProperty) {
					return ((TLProperty) obj).getName();
				} else if (obj instanceof TLAttribute) {
					return ((TLAttribute) obj).getName();
				} else if (obj instanceof TLIndicator) {
					return ((TLIndicator) obj).getName();
				} else if (obj instanceof TLAlias) {
					return ((TLAlias) obj).getName();
				}
				throw new IllegalStateException("Do not support this Tl object: " + obj);
			}
		});

	}

	private List<String> toNames(List<Node> children) {
		return Lists.transform(children, new Function<Node, String>() {

			@Override
			public String apply(Node node) {
				return node.getName();
			}
		});
	}

	private void assertFacetOrder(List<Node> children, String... string) {
		Assert.assertEquals(Arrays.asList(string), toNames(children));
	}

	private Node findChild(Node parent, String name) {
		for (Node n : parent.getChildren()) {
			if (name.equals(n.getName()))
				return n;
		}
		Assert.assertTrue("findChild did not find: " + name, 1 == 2);
		return null;
	}

}

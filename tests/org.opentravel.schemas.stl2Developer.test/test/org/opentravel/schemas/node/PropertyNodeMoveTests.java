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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.AttributeReferenceNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.utils.FacetNodeBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class PropertyNodeMoveTests extends BaseTest {

	private static final String I0 = "I0";
	private static final String I1 = "I1";
	private static final String A1 = "A1";
	private static final String A2 = "A2";
	private static final String E1 = "E1";
	private static final String E2 = "E2";
	private static final String E3 = "E3";
	final String i0Name = NodeNameUtils.fixIndicatorName(I0);
	final String i1Name = NodeNameUtils.fixIndicatorName(I1);
	final String a1Name = NodeNameUtils.fixAttributeName(A1);
	final String a2Name = NodeNameUtils.fixAttributeName(A2);

	@Before
	public void beforeEachOfTheseTests() {
		ln = ml.createNewLibrary("http://example.com/test", "test", defaultProject);
	}

	@Test
	public void shouldMoveUp() {
		FacetProviderNode facetNode = FacetNodeBuilder.create(ln).addElements(E1, E2, E3).build();
		findChild(facetNode, E2).moveUp();
		assertFacetOrder(facetNode.getChildren(), E2, E1, E3);

		// assert TL facet Order - order is controlled at property node level so one check is enough
		findChild(facetNode, E2).moveUp(); // should be ignored
		findChild(facetNode, E3).moveDown(); // should be ignored
		assertTrue(facetNode.getTLModelObject().getElements().get(0).getName().equals(E2));
		assertTrue(facetNode.getTLModelObject().getElements().get(1).getName().equals(E1));
		assertTrue(facetNode.getTLModelObject().getElements().get(2).getName().equals(E3));
	}

	@Test
	public void shouldMoveDown() {
		FacetProviderNode facetNode = FacetNodeBuilder.create(ln).addElements(E1, E2, E3).build();
		findChild(facetNode, E2).moveDown();
		assertFacetOrder(facetNode.getChildren(), E1, E3, E2);
	}

	@Test
	public void shouldMoveUpWithMixedTypes() {
		FacetProviderNode facetNode = FacetNodeBuilder.create(ln).addElements(E1).addAttributes(A1, A2).addElements(E2)
				.build();
		findChild(facetNode, a2Name).moveUp();
		assertFacetOrder(facetNode.getChildren(), a2Name, a1Name, E1, E2);
	}

	@Test
	public void shouldMoveDownWithMixedTypes() {
		FacetProviderNode facetNode = FacetNodeBuilder.create(ln).addElements(E1).addAttributes(A1, A2).addElements(E2)
				.build();
		findChild(facetNode, E1).moveDown();
		assertTrue(facetNode.isEditable_newToChain());
		assertFacetOrder(facetNode.getChildren(), a1Name, a2Name, E2, E1);
	}

	@Test
	public void shouldDoNothingWithOneType() {
		FacetProviderNode facetNode = FacetNodeBuilder.create(ln).addAttributes(A1).addIndicators(I1).addElements(E1)
				.build();
		findChild(facetNode, i1Name).moveDown();
		assertFacetOrder(facetNode.getChildren(), a1Name, i1Name, E1);
	}

	@Test
	public void shouldDoNothingWithElementOnBottom() {
		FacetProviderNode facetNode = FacetNodeBuilder.create(ln).addAttributes(A1).addIndicators(I0, I1)
				.addElements(E1).build();
		findChild(facetNode, i1Name).moveDown();
		assertFacetOrder(facetNode.getChildren(), a1Name, i0Name, i1Name, E1);
	}

	@Test
	public void shouldDoNothingWithElementOnTop() {
		FacetProviderNode facetNode = new FacetProviderNode(new TLFacet());
		assertTrue(facetNode != null);
		new AttributeNode(facetNode, A1);
		new IndicatorNode(facetNode, I0);
		new IndicatorNode(facetNode, I1);
		new ElementNode(facetNode, E1);
		assertTrue(!facetNode.getChildren().isEmpty());

		// When - moved
		findChild(facetNode, i0Name).moveUp();
		// Then
		assertFacetOrder(facetNode.getChildren(), a1Name, i0Name, i1Name, E1);
	}

	@Test
	public void shouldMoveWithMixedTypes() {
		FacetProviderNode facetNode = FacetNodeBuilder.create(ln).addElements(E1, E2).addAttributes(A1, A2)
				.addIndicators(I0, I1).build();
		new AttributeReferenceNode(facetNode);
		new AttributeReferenceNode(facetNode);
		new IdNode(facetNode, "id1");
		new IdNode(facetNode, "id2");
		new ElementReferenceNode(facetNode);
		new ElementReferenceNode(facetNode);
		new IndicatorElementNode(facetNode, "IE1");
		new IndicatorElementNode(facetNode, "IE2");

		// Move each property up and down 5 times to make sure no faults
		for (Node n : facetNode.getChildren_New()) {
			if (!(n instanceof PropertyNode))
				continue;
			PropertyNode pn = (PropertyNode) n;
			for (int i = 0; i < 5; i++)
				pn.moveUp();
			for (int i = 0; i < 5; i++)
				pn.moveDown();
		}
	}

	@Test
	public void shouldMoveCoreRoles() {
		CoreObjectNode core = ml.addCoreObjectToLibrary_Empty(ln, "C1");
		RoleNode r1 = new RoleNode(core.getFacet_Role(), "R1");
		RoleNode r2 = new RoleNode(core.getFacet_Role(), "R2");
		new RoleNode(core.getFacet_Role(), "R3");

		assertTrue(r1 == core.getFacet_Role().getChildren().get(0));
		assertTrue(r1.getName().equals("R1"));

		r1.moveUp();
		assertTrue(r1 == core.getFacet_Role().getChildren().get(0));

		r1.moveDown();
		assertTrue(r2 == core.getFacet_Role().getChildren().get(0));

		// don't throw exception
		for (int i = 0; i < 5; i++)
			r1.moveDown();
		for (int i = 0; i < 5; i++)
			r1.moveUp();
	}

	@Test
	public void shouldMoveEnumLiterals() {
		EnumerationClosedNode ec = ml.addClosedEnumToLibrary(ln, "EC"); // has 1 literal
		EnumLiteralNode l2 = new EnumLiteralNode(ec, "l2");
		EnumLiteralNode l3 = new EnumLiteralNode(ec, "l3");

		l3.moveUp();
		l3.moveUp();
		assertTrue(l3 == ec.getChildren().get(0));
		// don't throw exception
		for (int i = 0; i < 5; i++)
			l3.moveDown();
		for (int i = 0; i < 5; i++)
			l3.moveUp();
	}

	public void propertyRenameableCheck(PropertyNode pn) {
		// if editable and not inherited then it depends on the assigned type.
		if (!pn.isEditable())
			assertTrue("Uneditable property must not be renameable.", !pn.isRenameable());
		else if (pn.isInherited())
			assertTrue("Inherited property must not be renameable.", !pn.isRenameable());
		else if (pn instanceof TypedPropertyNode && !((TypedPropertyNode) pn).getAssignedType().isRenameableWhereUsed())
			assertTrue("Property's assigned type requires it to not be renameable.", !pn.isRenameable());
		else
			assertTrue("Property must be renameable.", pn.isRenameable());
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

	private PropertyNode findChild(FacetInterface parent, String name) {
		Node n = parent.get(name);
		assertTrue("FindChild did not find: " + name, n instanceof PropertyNode);
		return (PropertyNode) n;
	}

}

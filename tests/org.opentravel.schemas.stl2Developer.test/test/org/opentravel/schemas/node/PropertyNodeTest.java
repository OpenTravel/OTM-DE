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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.utils.FacetNodeBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class PropertyNodeTest {

	@Before
	public void beforeEachTest() {
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
		FacetNode facetNode = FacetNodeBuilder.create().addElements("A1").addAttributes("E1", "E2").addElements("A2")
				.build();
		findChild(facetNode, "A2").moveProperty(PropertyNode.UP);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "E1", "E2", "A2", "A1");
	}

	@Test
	public void shouldMoveDownWithMixedTypes() {
		FacetNode facetNode = FacetNodeBuilder.create().addElements("E1").addAttributes("A1", "A2").addElements("E2")
				.build();
		findChild(facetNode, "E1").moveProperty(PropertyNode.DOWN);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "A1", "A2", "E2", "E1");
	}

	@Test
	public void shouldDoNothingWithOneType() {
		FacetNode facetNode = FacetNodeBuilder.create().addAttributes("A1").addIndicators("I1").addElements("E1")
				.build();
		findChild(facetNode, "I1").moveProperty(PropertyNode.DOWN);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "A1", "I1", "E1");
	}

	@Test
	public void shouldDoNothingWithElementOnBottom() {
		FacetNode facetNode = FacetNodeBuilder.create().addAttributes("A1").addIndicators("I0", "I1").addElements("E1")
				.build();
		findChild(facetNode, "I1").moveProperty(PropertyNode.DOWN);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "A1", "I0", "I1", "E1");
	}

	@Test
	public void shouldDoNothingWithElementOnTop() {
		FacetNode facetNode = FacetNodeBuilder.create().addAttributes("A1").addIndicators("I0", "I1").addElements("E1")
				.build();
		findChild(facetNode, "I0").moveProperty(PropertyNode.UP);
		assertOrderOfNodeAndMO(facetNode);
		assertFacetOrder(facetNode.getChildren(), "A1", "I0", "I1", "E1");
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
		return null;
	}

}

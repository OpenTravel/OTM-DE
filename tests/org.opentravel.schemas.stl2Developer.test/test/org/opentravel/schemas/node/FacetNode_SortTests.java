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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class FacetNode_SortTests {

	// TODO - add tests for facets with inherited children
	// TODO - add tests for facets with ID and references

	@Test
	public void facet_sortElementTest() {
		TLFacet facet = new TLFacet();
		TLProperty e2 = createElement("e2");
		TLProperty e3 = createElement("e3");
		TLProperty e1 = createElement("e1");
		facet.addElement(e2);
		facet.addElement(e3);
		facet.addElement(e1);
		FacetProviderNode node = new FacetProviderNode(facet);
		node.sort();

		List<String> actualList = Lists.transform(node.getChildren(), new Function<Node, String>() {

			@Override
			public String apply(Node node) {
				return node.getName();
			}
		});
		List<String> expectedList = Lists.transform(Arrays.asList(e1, e2, e3), new Function<TLProperty, String>() {

			@Override
			public String apply(TLProperty node) {
				return node.getName();
			}
		});
		assertEquals(expectedList, actualList);
	}

	@Test
	public void facet_sortAttributesTest() {
		TLFacet facet = new TLFacet();
		TLAttribute a3 = createAttribute("a3");
		TLAttribute a2 = createAttribute("a2");
		TLAttribute a1 = createAttribute("a1");
		facet.addAttribute(a2);
		facet.addAttribute(a3);
		facet.addAttribute(a1);
		FacetProviderNode node = new FacetProviderNode(facet);
		node.sort();

		List<String> actualList = Lists.transform(node.getChildren(), new Function<Node, String>() {

			@Override
			public String apply(Node node) {
				return node.getName();
			}
		});
		List<String> expectedList = Lists.transform(Arrays.asList(a1, a2, a3), new Function<TLAttribute, String>() {

			@Override
			public String apply(TLAttribute node) {
				return node.getName();
			}
		});
		assertEquals(expectedList, actualList);
	}

	@Test
	public void facets_sortIndicatorsTest() {
		TLFacet facet = new TLFacet();
		TLIndicator i3 = createIndicator("i3");
		TLIndicator i2 = createIndicator("i2");
		TLIndicator i1 = createIndicator("i1");
		facet.addIndicator(i2);
		facet.addIndicator(i3);
		facet.addIndicator(i1);
		FacetProviderNode node = new FacetProviderNode(facet);
		node.sort();

		List<String> actualList = Lists.transform(node.getChildren(), new Function<Node, String>() {

			@Override
			public String apply(Node node) {
				return node.getName();
			}
		});
		List<String> expectedList = Lists.transform(Arrays.asList(i1, i2, i3), new Function<TLIndicator, String>() {

			@Override
			public String apply(TLIndicator node) {
				return node.getName();
			}
		});
		assertEquals(expectedList, actualList);
	}

	private TLProperty createElement(String name) {
		TLProperty tlProperty = new TLProperty();
		tlProperty.setName(name);
		return tlProperty;
	}

	private TLIndicator createIndicator(String name) {
		TLIndicator tlIndicator = new TLIndicator();
		tlIndicator.setName(name);
		return tlIndicator;
	}

	private TLAttribute createAttribute(String name) {
		TLAttribute tlAttribute = new TLAttribute();
		tlAttribute.setName(name);
		return tlAttribute;
	}

}

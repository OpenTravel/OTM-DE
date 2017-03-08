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
package org.opentravel.schemas.modelObject;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;

public class FacetMOSortTests {

	private TLFacet facet;
	private TLProperty e3;
	private TLProperty e2;
	private TLProperty e1;
	private TLAttribute a3;
	private TLAttribute a2;
	private TLAttribute a1;
	private TLIndicator i3;
	private TLIndicator i2;
	private TLIndicator i1;
	private FacetMO facetMO;

	@Before
	public void beforeEachTest() {
		facet = new TLFacet();
		e3 = createElement("e3");
		e2 = createElement("e2");
		e1 = createElement("e1");
		facet.addElement(e2);
		facet.addElement(e3);
		facet.addElement(e1);

		a3 = createAttribute("a3");
		a2 = createAttribute("a2");
		a1 = createAttribute("a1");
		facet.addAttribute(a2);
		facet.addAttribute(a3);
		facet.addAttribute(a1);

		i3 = createIndicator("i3");
		i2 = createIndicator("i2");
		i1 = createIndicator("i1");
		facet.addIndicator(i2);
		facet.addIndicator(i3);
		facet.addIndicator(i1);
		facetMO = new FacetMO(facet);
	}

	@Test
	public void shouldSortElements() {
		facetMO.sort();

		List<TLProperty> expectedOrder = Arrays.asList(e1, e2, e3);
		assertEquals(expectedOrder, facet.getElements());
	}

	@Test
	public void shouldSortAttributes() {
		facetMO.sort();

		List<TLAttribute> expectedOrder = Arrays.asList(a1, a2, a3);
		assertEquals(expectedOrder, facet.getAttributes());

	}

	@Test
	public void shouldSortIndicators() {
		facetMO.sort();

		List<TLIndicator> expectedOrder = Arrays.asList(i1, i2, i3);
		assertEquals(expectedOrder, facet.getIndicators());

	}

	private TLIndicator createIndicator(String name) {
		TLIndicator tlProperty = new TLIndicator();
		tlProperty.setName(name);
		return tlProperty;
	}

	private TLAttribute createAttribute(String name) {
		TLAttribute tlProperty = new TLAttribute();
		tlProperty.setName(name);
		return tlProperty;
	}

	private TLProperty createElement(String name) {
		TLProperty tlProperty = new TLProperty();
		tlProperty.setName(name);
		return tlProperty;
	}

}

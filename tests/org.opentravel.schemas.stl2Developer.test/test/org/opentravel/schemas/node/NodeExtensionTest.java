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

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;

public class NodeExtensionTest extends BaseProjectTest {

	private LibraryNode lib;

	@Override
	protected void callBeforeEachTest() throws LibrarySaveException {
		lib = LibraryNodeBuilder.create("Example", "http://example.org", "p", new Version(1, 1, 1)).build(
				defaultProject, pc);
		lib.setEditable(true);
	}

	@Test
	public void isExtendedByTypeShouldReturnTrueForSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(lib);
		Assert.assertTrue(boExtend.isExtendedBy(boBase));
	}

	@Test
	public void isExtendedByShouldReturnFalseForSuperSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(lib);
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2").extend(boExtend).get(lib);
		Assert.assertFalse(boExtend2.isExtendedBy(boBase));
	}

	@Test
	public void isInstanceOfShouldReturnTrueForSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(lib);
		Assert.assertTrue(boExtend.isInstanceOf(boBase));
	}

	@Test
	public void isInstanceOfShouldReturnTrueForSuperSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(lib);
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2").extend(boExtend).get(lib);
		Assert.assertTrue(boExtend2.isInstanceOf(boBase));
	}

	@Test
	public void isInstanceOfShouldReturnForSuperSuperTypeExtendingSubclass() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(lib);
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2").extend(boExtend).get(lib);
		Assert.assertFalse(boBase.isInstanceOf(boExtend2));
	}

	@Test
	public void isInstanceOfShouldReturnFalseForSuperSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").get(lib);
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2").extend(boExtend).get(lib);
		Assert.assertFalse(boExtend2.isInstanceOf(boBase));
	}
}

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

	@Test
	public void multipleExtensionsShouldBeTracked() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("baseBO").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("bExtend").extend(boBase).get(lib);
		Assert.assertTrue(boExtend.isExtendedBy(boBase));
		assert boBase.getWhereExtendedHandler() != null;
		assert boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend);

		// Test adding more extensions of the boBase - make sure it works and whereExtended is OK
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("bExtend2").extend(boBase).get(lib);
		BusinessObjectNode boExtend3 = ComponentNodeBuilder.createBusinessObject("bExtend3").extend(boBase).get(lib);

		Assert.assertTrue(boExtend.isExtendedBy(boBase));
		Assert.assertTrue(boExtend2.isExtendedBy(boBase));
		Assert.assertTrue(boExtend3.isExtendedBy(boBase));

		assert boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend);
		assert boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend2);
		assert boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend3);

		assert boBase.getWhereExtendedHandler().getWhereExtendedCount() == 3;

	}

	@Test
	public void removingExtensionsShouldClearWhereExtended() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("baseBO").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("bExtend").extend(boBase).get(lib);
		boExtend.setExtension(null);
		Assert.assertFalse(boExtend.isExtendedBy(boBase));
		assert !boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend);

		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("baseCore").get(lib);
		CoreObjectNode coExtend = ComponentNodeBuilder.createCoreObject("cExtend").extend(coBase).get(lib);
		coExtend.setExtension(null);
		Assert.assertFalse(coExtend.isExtendedBy(coBase));
		assert !coBase.getWhereExtendedHandler().getWhereExtended().contains(coExtend);

		ChoiceObjectNode choBase = ComponentNodeBuilder.createChoiceObject("baseChoice").get(lib);
		ChoiceObjectNode choExtend = ComponentNodeBuilder.createChoiceObject("chExtend").extend(choBase).get(lib);
		choExtend.setExtension(null);
		Assert.assertFalse(choExtend.isExtendedBy(choBase));
		assert !choBase.getWhereExtendedHandler().getWhereExtended().contains(choExtend);

		VWA_Node voBase = ComponentNodeBuilder.createVWA("baseVWA").get(lib);
		VWA_Node voExtend = ComponentNodeBuilder.createVWA("vExtend").extend(voBase).get(lib);
		voExtend.setExtension(null);
		Assert.assertFalse(voExtend.isExtendedBy(voBase));
		assert !voBase.getWhereExtendedHandler().getWhereExtended().contains(voExtend);

		ExtensionPointNode epBase = ComponentNodeBuilder.createExtensionPoint("epBase").get(lib);
		ExtensionPointNode epExtend = ComponentNodeBuilder.createExtensionPoint("epExtends").extend(epBase).get(lib);
		epExtend.setExtension(null);
		Assert.assertFalse(epExtend.isExtendedBy(epBase));
		assert !epBase.getWhereExtendedHandler().getWhereExtended().contains(epExtend);

		EnumerationOpenNode eoBase = ComponentNodeBuilder.createEnumerationOpen("eoBase").get(lib);
		EnumerationOpenNode eoExtend = ComponentNodeBuilder.createEnumerationOpen("eoExtends").extend(eoBase).get(lib);
		eoExtend.setExtension(null);
		Assert.assertFalse(eoExtend.isExtendedBy(eoBase));
		assert !eoBase.getWhereExtendedHandler().getWhereExtended().contains(eoExtend);

		EnumerationClosedNode ecBase = ComponentNodeBuilder.createEnumerationClosed("ecBase").get(lib);
		EnumerationClosedNode ecExtend = ComponentNodeBuilder.createEnumerationClosed("ecExt").extend(ecBase).get(lib);
		ecExtend.setExtension(null);
		Assert.assertFalse(ecExtend.isExtendedBy(ecBase));
		assert !ecBase.getWhereExtendedHandler().getWhereExtended().contains(ecExtend);
	}

	@Test
	public void allExtendedObjectsShouldAllReassignment() {
		// TODO extend with different base then check count (change base type)
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("baseBO").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("bExtend").extend(boBase).get(lib);
		BusinessObjectNode boBase2 = ComponentNodeBuilder.createBusinessObject("baseBO2").get(lib);
		boExtend.setExtension(boBase2);
		assert boBase2.getWhereExtendedHandler().getWhereExtended().contains(boExtend);
		assert !boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend);

		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("baseCore").get(lib);
		CoreObjectNode coBase2 = ComponentNodeBuilder.createCoreObject("baseCore2").get(lib);
		CoreObjectNode coExtend = ComponentNodeBuilder.createCoreObject("cExtend").extend(coBase).get(lib);
		coExtend.setExtension(coBase2);
		assert coBase2.getWhereExtendedHandler().getWhereExtended().contains(coExtend);
		assert !coBase.getWhereExtendedHandler().getWhereExtended().contains(coExtend);

		ChoiceObjectNode choBase = ComponentNodeBuilder.createChoiceObject("baseChoice").get(lib);
		ChoiceObjectNode choBase2 = ComponentNodeBuilder.createChoiceObject("baseChoice2").get(lib);
		ChoiceObjectNode choExtend = ComponentNodeBuilder.createChoiceObject("chExtend").extend(choBase).get(lib);
		choExtend.setExtension(choBase2);
		assert choBase2.getWhereExtendedHandler().getWhereExtended().contains(choExtend);
		assert !choBase.getWhereExtendedHandler().getWhereExtended().contains(choExtend);

		VWA_Node voBase = ComponentNodeBuilder.createVWA("baseVWA").get(lib);
		VWA_Node voBase2 = ComponentNodeBuilder.createVWA("baseVWA2").get(lib);
		VWA_Node voExtend = ComponentNodeBuilder.createVWA("vExtend").extend(voBase).get(lib);
		voExtend.setExtension(voBase2);
		assert voBase2.getWhereExtendedHandler().getWhereExtended().contains(voExtend);
		assert !voBase.getWhereExtendedHandler().getWhereExtended().contains(voExtend);

		ExtensionPointNode epBase = ComponentNodeBuilder.createExtensionPoint("epBase").get(lib);
		ExtensionPointNode epBase2 = ComponentNodeBuilder.createExtensionPoint("epBase2").get(lib);
		ExtensionPointNode epExtend = ComponentNodeBuilder.createExtensionPoint("epExtends").extend(epBase).get(lib);
		epExtend.setExtension(epBase2);
		assert epBase2.getWhereExtendedHandler().getWhereExtended().contains(epExtend);
		assert !epBase.getWhereExtendedHandler().getWhereExtended().contains(epExtend);

		EnumerationOpenNode eoBase = ComponentNodeBuilder.createEnumerationOpen("eoBase").get(lib);
		EnumerationOpenNode eoBase2 = ComponentNodeBuilder.createEnumerationOpen("eoBase2").get(lib);
		EnumerationOpenNode eoExtend = ComponentNodeBuilder.createEnumerationOpen("eoExtends").extend(eoBase).get(lib);
		eoExtend.setExtension(eoBase2);
		assert eoBase2.getWhereExtendedHandler().getWhereExtended().contains(eoExtend);
		assert !eoBase.getWhereExtendedHandler().getWhereExtended().contains(eoExtend);

		EnumerationClosedNode ecBase = ComponentNodeBuilder.createEnumerationClosed("ecBase").get(lib);
		EnumerationClosedNode ecBase2 = ComponentNodeBuilder.createEnumerationClosed("ecBase2").get(lib);
		EnumerationClosedNode ecExtend = ComponentNodeBuilder.createEnumerationClosed("ecExt").extend(ecBase).get(lib);
		ecExtend.setExtension(ecBase2);
		assert ecBase2.getWhereExtendedHandler().getWhereExtended().contains(ecExtend);
		assert !ecBase.getWhereExtendedHandler().getWhereExtended().contains(ecExtend);

	}

	@Test
	public void allExtendedObjectsShouldReturnTrueForSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("baseBO").get(lib);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("bExtend").extend(boBase).get(lib);
		Assert.assertTrue(boExtend.isExtendedBy(boBase));
		assert boBase.getWhereExtendedHandler() != null;
		assert boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend);

		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("baseCore").get(lib);
		CoreObjectNode coExtend = ComponentNodeBuilder.createCoreObject("cExtend").extend(coBase).get(lib);
		Assert.assertTrue(coExtend.isExtendedBy(coBase));
		assert coBase.getWhereExtendedHandler().getWhereExtended().contains(coExtend);

		ChoiceObjectNode choBase = ComponentNodeBuilder.createChoiceObject("baseChoice").get(lib);
		ChoiceObjectNode choExtend = ComponentNodeBuilder.createChoiceObject("chExtend").extend(choBase).get(lib);
		Assert.assertTrue(choExtend.isExtendedBy(choBase));
		assert choBase.getWhereExtendedHandler().getWhereExtended().contains(choExtend);

		VWA_Node voBase = ComponentNodeBuilder.createVWA("baseVWA").get(lib);
		VWA_Node voExtend = ComponentNodeBuilder.createVWA("vExtend").extend(voBase).get(lib);
		Assert.assertTrue(voExtend.isExtendedBy(voBase));
		assert voBase.getWhereExtendedHandler().getWhereExtended().contains(voExtend);
		// FIXME - ep should extend a facet AND each other
		ExtensionPointNode epBase = ComponentNodeBuilder.createExtensionPoint("epBase").get(lib);
		ExtensionPointNode epExtend = ComponentNodeBuilder.createExtensionPoint("epExtends").extend(epBase).get(lib);
		Assert.assertTrue(epExtend.isExtendedBy(epBase));
		assert epBase.getWhereExtendedHandler().getWhereExtended().contains(epExtend);

		EnumerationOpenNode eoBase = ComponentNodeBuilder.createEnumerationOpen("eoBase").get(lib);
		EnumerationOpenNode eoExtend = ComponentNodeBuilder.createEnumerationOpen("eoExtends").extend(eoBase).get(lib);
		Assert.assertTrue(eoExtend.isExtendedBy(eoBase));
		assert eoBase.getWhereExtendedHandler().getWhereExtended().contains(eoExtend);

		EnumerationClosedNode ecBase = ComponentNodeBuilder.createEnumerationClosed("ecBase").get(lib);
		EnumerationClosedNode ecExtend = ComponentNodeBuilder.createEnumerationClosed("ecExt").extend(ecBase).get(lib);
		Assert.assertTrue(ecExtend.isExtendedBy(ecBase));
		assert ecBase.getWhereExtendedHandler().getWhereExtended().contains(ecExtend);

		// With all objects types set up, test the handler getter.
		assert boExtend.getExtensionHandler().get() == boBase;
		assert coExtend.getExtensionHandler().get() == coBase;
		assert choExtend.getExtensionHandler().get() == choBase;
		assert voExtend.getExtensionHandler().get() == voBase;
		assert epExtend.getExtensionHandler().get() == epBase;
		assert eoExtend.getExtensionHandler().get() == eoBase;
		assert ecExtend.getExtensionHandler().get() == ecBase;
	}

}

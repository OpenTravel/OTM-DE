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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeExtensionTest extends BaseProjectTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeExtensionTest.class);

	ProjectNode defaultProject;
	LoadFiles lf = new LoadFiles();
	MockLibrary ml = new MockLibrary();
	LibraryChainNode lcn = null;
	private LibraryNode ln;

	@Before
	public void beforeExtensionTests() throws LibrarySaveException {
		// lib = LibraryNodeBuilder.create("Example", "http://example.org", "p", new Version(1, 1,
		// 1)).build(testProject,
		// pc);
		// lib.setEditable(true);
		LOGGER.debug("***Before Extension Tests ----------------------");
		// callBeforeEachTest();
		defaultProject = pc.getDefaultProject();
		ln = ml.createNewLibrary("http://test.com", "CoreTest", defaultProject);
		ln.setEditable(true);
		testProject = defaultProject;

	}

	@Test
	public void isExtendedByTypeShouldReturnTrueForSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(ln);
		Assert.assertTrue(boExtend.isExtendedBy(boBase));

	}

	// Make sure the libraries are correct.
	@Test
	public void extensionDoesNotChangeAssignedLibrary() throws LibrarySaveException {
		LibraryNode libE = LibraryNodeBuilder.create("Example2", "http://example.org", "p", new Version(1, 1, 1))
				.build(testProject, pc);
		new LibraryChainNode(libE);
		libE.setEditable(true);

		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(libE);

		Assert.assertTrue(boExtend.isExtendedBy(boBase));
		assertTrue("base must be in base library.",
				boBase.getLibrary().getDescendants_LibraryMembers().contains(boBase));
		assertTrue("extension must be in extension library.", boExtend.getLibrary() == libE);
		assertTrue("extension must be in extension library.",
				boExtend.getLibrary().getDescendants_LibraryMembers().contains(boExtend));
	}

	@Test
	public void isExtendedByShouldReturnFalseForSuperSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(ln);
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2").extend(boExtend).get(ln);
		Assert.assertFalse(boExtend2.isExtendedBy(boBase));
	}

	@Test
	public void isInstanceOfShouldReturnTrueForSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(ln);
		Assert.assertTrue(boExtend.isInstanceOf(boBase));
	}

	@Test
	public void isInstanceOfShouldReturnTrueForSuperSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(ln);
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2").extend(boExtend).get(ln);
		Assert.assertTrue(boExtend2.isInstanceOf(boBase));
	}

	@Test
	public void isInstanceOfShouldReturnForSuperSuperTypeExtendingSubclass() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").extend(boBase).get(ln);
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2").extend(boExtend).get(ln);
		Assert.assertFalse(boBase.isInstanceOf(boExtend2));
	}

	@Test
	public void isInstanceOfShouldReturnFalseForSuperSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend").get(ln);
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("Extend2").extend(boExtend).get(ln);
		Assert.assertFalse(boExtend2.isInstanceOf(boBase));
	}

	@Test
	public void multipleExtensionsShouldBeTracked() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("baseBO").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("bExtend").extend(boBase).get(ln);
		Assert.assertTrue(boExtend.isExtendedBy(boBase));
		assert boBase.getWhereExtendedHandler() != null;
		assert boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend);

		// Test adding more extensions of the boBase - make sure it works and whereExtended is OK
		BusinessObjectNode boExtend2 = ComponentNodeBuilder.createBusinessObject("bExtend2").extend(boBase).get(ln);
		BusinessObjectNode boExtend3 = ComponentNodeBuilder.createBusinessObject("bExtend3").extend(boBase).get(ln);

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
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("baseBO").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("bExtend").extend(boBase).get(ln);
		boExtend.setExtension(null);
		Assert.assertFalse(boExtend.isExtendedBy(boBase));
		assert !boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend);

		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("baseCore").get(ln);
		CoreObjectNode coExtend = ComponentNodeBuilder.createCoreObject("cExtend").extend(coBase).get(ln);
		coExtend.setExtension(null);
		Assert.assertFalse(coExtend.isExtendedBy(coBase));
		assert !coBase.getWhereExtendedHandler().getWhereExtended().contains(coExtend);

		ChoiceObjectNode choBase = ComponentNodeBuilder.createChoiceObject("baseChoice").get(ln);
		ChoiceObjectNode choExtend = ComponentNodeBuilder.createChoiceObject("chExtend").extend(choBase).get(ln);
		choExtend.setExtension(null);
		Assert.assertFalse(choExtend.isExtendedBy(choBase));
		assert !choBase.getWhereExtendedHandler().getWhereExtended().contains(choExtend);

		VWA_Node voBase = ComponentNodeBuilder.createVWA("baseVWA").get(ln);
		VWA_Node voExtend = ComponentNodeBuilder.createVWA("vExtend").extend(voBase).get(ln);
		voExtend.setExtension(null);
		Assert.assertFalse(voExtend.isExtendedBy(voBase));
		assert !voBase.getWhereExtendedHandler().getWhereExtended().contains(voExtend);

		// TODO - what is proper test for extension points?
		// ExtensionPointNode epBase = ComponentNodeBuilder.createExtensionPoint("epBase").get(lib);
		// ExtensionPointNode epExtend = ComponentNodeBuilder.createExtensionPoint("epExtends").extend(epBase).get(lib);
		// epExtend.setExtension(null);
		// Assert.assertFalse(epExtend.isExtendedBy(epBase));
		// assert !epBase.getWhereExtendedHandler().getWhereExtended().contains(epExtend);

		EnumerationOpenNode eoBase = ComponentNodeBuilder.createEnumerationOpen("eoBase").get(ln);
		EnumerationOpenNode eoExtend = ComponentNodeBuilder.createEnumerationOpen("eoExtends").extend(eoBase).get(ln);
		eoExtend.setExtension(null);
		Assert.assertFalse(eoExtend.isExtendedBy(eoBase));
		assert !eoBase.getWhereExtendedHandler().getWhereExtended().contains(eoExtend);

		EnumerationClosedNode ecBase = ComponentNodeBuilder.createEnumerationClosed("ecBase").get(ln);
		EnumerationClosedNode ecExtend = ComponentNodeBuilder.createEnumerationClosed("ecExt").extend(ecBase).get(ln);
		ecExtend.setExtension(null);
		Assert.assertFalse(ecExtend.isExtendedBy(ecBase));
		assert !ecBase.getWhereExtendedHandler().getWhereExtended().contains(ecExtend);
	}

	@Test
	public void allExtendedObjectsShouldAllReassignment() {
		// TODO extend with different base then check count (change base type)
		// BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("baseBO").get(ln);
		// BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("bExtend").extend(boBase).get(ln);
		// BusinessObjectNode boBase2 = ComponentNodeBuilder.createBusinessObject("baseBO2").get(ln);
		BusinessObjectNode boBase = ml.addBusinessObject_ResourceSubject(ln, "baseBO");
		BusinessObjectNode boExtend = ml.addBusinessObjectToLibrary_Empty(ln, "bExtend");
		boExtend.setExtension(boBase);
		BusinessObjectNode boBase2 = ml.addBusinessObjectToLibrary(ln, "baseBO2");
		boExtend.setExtension(boBase2);
		assert boBase2.getWhereExtendedHandler().getWhereExtended().contains(boExtend);
		assert !boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend);

		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("baseCore").get(ln);
		CoreObjectNode coBase2 = ComponentNodeBuilder.createCoreObject("baseCore2").get(ln);
		CoreObjectNode coExtend = ComponentNodeBuilder.createCoreObject("cExtend").extend(coBase).get(ln);
		coExtend.setExtension(coBase2);
		assert coBase2.getWhereExtendedHandler().getWhereExtended().contains(coExtend);
		assert !coBase.getWhereExtendedHandler().getWhereExtended().contains(coExtend);

		ChoiceObjectNode choBase = ComponentNodeBuilder.createChoiceObject("baseChoice").get(ln);
		ChoiceObjectNode choBase2 = ComponentNodeBuilder.createChoiceObject("baseChoice2").get(ln);
		ChoiceObjectNode choExtend = ComponentNodeBuilder.createChoiceObject("chExtend").extend(choBase).get(ln);
		choExtend.setExtension(choBase2);
		assert choBase2.getWhereExtendedHandler().getWhereExtended().contains(choExtend);
		assert !choBase.getWhereExtendedHandler().getWhereExtended().contains(choExtend);

		VWA_Node voBase = ComponentNodeBuilder.createVWA("baseVWA").get(ln);
		VWA_Node voBase2 = ComponentNodeBuilder.createVWA("baseVWA2").get(ln);
		VWA_Node voExtend = ComponentNodeBuilder.createVWA("vExtend").extend(voBase).get(ln);
		voExtend.setExtension(voBase2);
		assert voBase2.getWhereExtendedHandler().getWhereExtended().contains(voExtend);
		assert !voBase.getWhereExtendedHandler().getWhereExtended().contains(voExtend);

		// TODO - what is proper test for extension points?
		// ExtensionPointNode epBase = ComponentNodeBuilder.createExtensionPoint("epBase").get(lib);
		// ExtensionPointNode epBase2 = ComponentNodeBuilder.createExtensionPoint("epBase2").get(lib);
		// ExtensionPointNode epExtend = ComponentNodeBuilder.createExtensionPoint("epExtends").extend(epBase).get(lib);
		// epExtend.setExtension(epBase2);
		// assert epBase2.getWhereExtendedHandler().getWhereExtended().contains(epExtend);
		// assert !epBase.getWhereExtendedHandler().getWhereExtended().contains(epExtend);

		EnumerationOpenNode eoBase = ComponentNodeBuilder.createEnumerationOpen("eoBase").get(ln);
		EnumerationOpenNode eoBase2 = ComponentNodeBuilder.createEnumerationOpen("eoBase2").get(ln);
		EnumerationOpenNode eoExtend = ComponentNodeBuilder.createEnumerationOpen("eoExtends").extend(eoBase).get(ln);
		eoExtend.setExtension(eoBase2);
		assert eoBase2.getWhereExtendedHandler().getWhereExtended().contains(eoExtend);
		assert !eoBase.getWhereExtendedHandler().getWhereExtended().contains(eoExtend);

		EnumerationClosedNode ecBase = ComponentNodeBuilder.createEnumerationClosed("ecBase").get(ln);
		EnumerationClosedNode ecBase2 = ComponentNodeBuilder.createEnumerationClosed("ecBase2").get(ln);
		EnumerationClosedNode ecExtend = ComponentNodeBuilder.createEnumerationClosed("ecExt").extend(ecBase).get(ln);
		ecExtend.setExtension(ecBase2);
		assert ecBase2.getWhereExtendedHandler().getWhereExtended().contains(ecExtend);
		assert !ecBase.getWhereExtendedHandler().getWhereExtended().contains(ecExtend);

	}

	@Test
	public void allExtendedObjectsShouldReturnTrueForSuperType() {
		BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("baseBO").get(ln);
		BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("bExtend").extend(boBase).get(ln);
		Assert.assertTrue(boExtend.isExtendedBy(boBase));
		assert boBase.getWhereExtendedHandler() != null;
		assert boBase.getWhereExtendedHandler().getWhereExtended().contains(boExtend);

		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("baseCore").get(ln);
		CoreObjectNode coExtend = ComponentNodeBuilder.createCoreObject("cExtend").extend(coBase).get(ln);
		Assert.assertTrue(coExtend.isExtendedBy(coBase));
		assert coBase.getWhereExtendedHandler().getWhereExtended().contains(coExtend);

		ChoiceObjectNode choBase = ComponentNodeBuilder.createChoiceObject("baseChoice").get(ln);
		ChoiceObjectNode choExtend = ComponentNodeBuilder.createChoiceObject("chExtend").extend(choBase).get(ln);
		Assert.assertTrue(choExtend.isExtendedBy(choBase));
		assert choBase.getWhereExtendedHandler().getWhereExtended().contains(choExtend);

		VWA_Node voBase = ml.addVWA_ToLibrary(ln, "baseVWA");
		VWA_Node voExtend = ml.addVWA_ToLibrary(ln, "vExtend");
		voExtend.setExtension(voBase);
		Assert.assertTrue(voExtend.isExtendedBy(voBase));
		assert voBase.getWhereExtendedHandler().getWhereExtended().contains(voExtend);

		// FIXME - ep should extend a facet AND each other
		// TODO - what is proper test for extension points?
		// ExtensionPointNode epBase = ComponentNodeBuilder.createExtensionPoint("epBase").get(lib);
		// ExtensionPointNode epExtend = ComponentNodeBuilder.createExtensionPoint("epExtends").extend(epBase).get(lib);
		// Assert.assertTrue(epExtend.isExtendedBy(epBase));
		// assert epBase.getWhereExtendedHandler().getWhereExtended().contains(epExtend);

		EnumerationOpenNode eoBase = ComponentNodeBuilder.createEnumerationOpen("eoBase").get(ln);
		EnumerationOpenNode eoExtend = ComponentNodeBuilder.createEnumerationOpen("eoExtends").extend(eoBase).get(ln);
		Assert.assertTrue(eoExtend.isExtendedBy(eoBase));
		assert eoBase.getWhereExtendedHandler().getWhereExtended().contains(eoExtend);

		EnumerationClosedNode ecBase = ComponentNodeBuilder.createEnumerationClosed("ecBase").get(ln);
		EnumerationClosedNode ecExtend = ComponentNodeBuilder.createEnumerationClosed("ecExt").extend(ecBase).get(ln);
		Assert.assertTrue(ecExtend.isExtendedBy(ecBase));
		assert ecBase.getWhereExtendedHandler().getWhereExtended().contains(ecExtend);

		// With all objects types set up, test the handler getter.
		assert boExtend.getExtensionHandler().get() == boBase;
		assert coExtend.getExtensionHandler().get() == coBase;
		assert choExtend.getExtensionHandler().get() == choBase;
		assert voExtend.getExtensionHandler().get() == voBase;
		// assert epExtend.getExtensionHandler().get() == epBase;
		assert eoExtend.getExtensionHandler().get() == eoBase;
		assert ecExtend.getExtensionHandler().get() == ecBase;

		// Check namespace retrieval
		String targetNS = ln.getNamespace();
		assertTrue(boExtend.getExtendsTypeNS().equals(targetNS));
		assertTrue(coExtend.getExtendsTypeNS().equals(targetNS));
		assertTrue(choExtend.getExtendsTypeNS().equals(targetNS));
		assertTrue(eoExtend.getExtendsTypeNS().equals(targetNS));
		assertTrue(ecExtend.getExtendsTypeNS().equals(targetNS));
		// Throws error when using ModelObjects since it did not override
		// String vNS = voExtend.getExtendsTypeNS();
		assertTrue(voExtend.getExtendsTypeNS().equals(targetNS));
	}

}

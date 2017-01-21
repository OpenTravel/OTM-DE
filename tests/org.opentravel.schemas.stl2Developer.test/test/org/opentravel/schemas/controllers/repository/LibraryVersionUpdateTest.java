package org.opentravel.schemas.controllers.repository;

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

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ChoiceObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Ignore("Tests currently failing and need attention.")
public class LibraryVersionUpdateTest extends RepositoryIntegrationTestBase {
	static final Logger LOGGER = LoggerFactory.getLogger(LibraryVersionUpdateTest.class);

	MockLibrary ml = new MockLibrary();
	private LibraryNode lib1 = null;
	private LibraryNode lib2 = null;
	private LibraryChainNode chain1 = null;
	private LibraryChainNode chain2 = null;
	private Node xsdString;
	private ProjectNode uploadProject = null;
	// from base class - protected static DefaultRepositoryController rc;

	private BusinessObjectNode bo = null;
	private CoreObjectNode co = null;
	private VWA_Node vwa = null;
	private SimpleTypeNode simple = null;
	private EnumerationClosedNode cEnum = null;
	private EnumerationOpenNode oEnum = null;
	private ExtensionPointNode ep = null;
	private BusinessObjectNode sbo = null;
	private BusinessObjectNode nbo = null;
	private CoreObjectNode core2 = null;
	private LibraryNode minorLibrary = null;
	private LibraryNode patchLibrary = null;
	private CoreObjectNode mCo = null;
	private ExtensionPointNode ePatch = null;

	private LibraryNode versionedLib2;

	@Override
	public RepositoryNode getRepositoryForTest() {
		for (RepositoryNode rn : rc.getAll()) {
			if (rn.isRemote()) {
				return rn;
			}
		}
		throw new IllegalStateException("Missing remote repository. Check your configuration.");
	}

	@Before
	public void runBeforeEachTest() throws LibrarySaveException, RepositoryException {
		LOGGER.debug("Before test.");
		xsdString = NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");

		lib1 = LibraryNodeBuilder.create("TestLibrary1", getRepositoryForTest().getNamespace() + "/Test/NS1",
				"prefix1", new Version(1, 0, 0)).build(uploadProject, pc);

		lib2 = LibraryNodeBuilder.create("TestLibrary2", getRepositoryForTest().getNamespace() + "/Test/NS2",
				"prefix2", new Version(1, 0, 0)).build(uploadProject, pc);

		chain1 = rc.manage(getRepositoryForTest(), Collections.singletonList(lib1)).get(0);
		chain2 = rc.manage(getRepositoryForTest(), Collections.singletonList(lib2)).get(0);
		boolean locked = rc.lock(chain1.getHead());
		locked = rc.lock(chain2.getHead());

		assertTrue("Repository controller must not be null.", rc != null);
		assertTrue("Repository must be available.", getRepositoryForTest() != null);
		assertTrue("Project must not be null.", uploadProject != null);
		assertTrue("MockLibrary must not be null.", ml != null);
		assertTrue("xsdString must not be null.", xsdString != null);
		assertTrue("lib1 must not be null.", lib1 != null);
		assertTrue("lib1 must be editable.", lib1.isEditable());
		assertTrue("lib2 must not be null. ", lib2 != null);
		assertTrue("lib2 must be editable.", lib2.isEditable());
		assertTrue("chain must be locked.", locked);
		assertTrue("chain must be MANAGED_WIP.", RepositoryItemState.MANAGED_WIP == chain2.getHead().getProjectItem()
				.getState());

		// Use repository controller to version the libraries.
		// patchLibrary = rc.createPatchVersion(chain.getHead());
		// minorLibrary = rc.createMinorVersion(chain.getHead());
		// LibraryNode major2 = rc.createMajorVersion(chain.getHead());

		LOGGER.debug("Before tests done.");
	}

	// Create two libraries where one uses types from the other then version the type provider
	@Test
	public void updateVersionTest_AssignedTypes() throws RepositoryException {
		// Given - two managed, locked and editable libraries.

		// Given - a simple type in the provider library to assign
		versionedLib2 = lib2;
		SimpleTypeNode simpleType = ml.addSimpleTypeToLibrary(versionedLib2, "simpleType");

		// Given - user library containing the objects that will get updated and assign them to the found type
		LibraryNode userLib = lib1;
		ml.addOneOfEach(userLib, "User");
		for (TypeUser user : userLib.getDescendants_TypeUsers())
			user.setAssignedType(simpleType);

		// When - provider lib is Versioned
		versionedLib2 = rc.createMajorVersion(versionedLib2);
		// Then
		assertTrue("Must have major version of provider library.", versionedLib2 != null);
		assertTrue("Must have type providers", !versionedLib2.getDescendants_TypeProviders().isEmpty());
		assertTrue("Must be new library.", versionedLib2 != lib2);
		assertTrue("Major versions must be head of chain.", versionedLib2 == versionedLib2.getChain().getHead());
		// Then - type users still use the type from the old version
		assertTrue("Assigned type is NOT in major version.", simpleType.getLibrary() != versionedLib2);
		for (TypeUser user : userLib.getDescendants_TypeUsers())
			if (!(user.getAssignedType() instanceof ImpliedNode) && user.getRequiredType() == null)
				if (user.getAssignedType() != simpleType)
					LOGGER.debug("AssignedType = " + user.getAssignedType());
				else
					assertTrue("Type user must be assigned to simple type.", user.getAssignedType() == simpleType);

		//
		// Library level assigned type replacement Business Logic
		// - setup the map and prepare for the call used by the Version Update Handler.
		//
		// Given - a replacement map of used libraries and their later versions.
		// Walk selected library type users and collect all used libraries (type assignments and extensions)
		List<LibraryNode> usedLibs = userLib.getAssignedLibraries();
		assertTrue("Must have simple type library in list.", usedLibs.contains(simpleType.getLibrary()));
		HashMap<LibraryNode, LibraryNode> replacementMap = rc.getVersionUpdateMap(usedLibs, true);
		assertTrue("Replacement map must map simple type lib to major version.",
				replacementMap.get(simpleType.getLibrary()) == versionedLib2);

		// When - call used by Version Update Handler t0 replace type users using the replacement map
		userLib.replaceTypeUsers(replacementMap);

		// Then
		assertTrue(simpleType.getWhereAssigned().isEmpty());
		for (TypeUser user : userLib.getDescendants_TypeUsers()) {
			if (!(user.getAssignedType() instanceof ImpliedNode) && user.getRequiredType() == null) {
				if (user.getAssignedType().getLibrary() != versionedLib2)
					LOGGER.debug("Error - " + user + " assigned type is in wrong library: "
							+ ((Node) user.getAssignedType()).getNameWithPrefix());
				assertTrue("Must be in providerLib.", user.getAssignedType().getLibrary() == versionedLib2);
			}
		}
		// TODO - test with finalOnly set to true on getVersionUpdateMap()
	}

	@Test
	public void updateVersionTest_BaseTypes() throws RepositoryException {
		// Create two libraries where one extends types from the other then version the type provider

		// Create Extension Owners in the provider library
		LibraryNode baseLib = lib2;
		BusinessObjectNode boType = ml.addBusinessObjectToLibrary(baseLib, "boType");
		ChoiceObjectNode choiceType = ml.addChoice(baseLib, "choiceType");
		CoreObjectNode coreType = ml.addCoreObjectToLibrary(baseLib, "coreType");
		EnumerationClosedNode ecType = ml.addClosedEnumToLibrary(baseLib, "ecType");
		EnumerationOpenNode eoType = ml.addOpenEnumToLibrary(baseLib, "eoType");
		VWA_Node vwaType = ml.addVWA_ToLibrary(baseLib, "vwaType");

		// ??? Create user library containing the objects that will get updated and assign them to the found type
		// Given - library 1 with one of each object extending objects in library 2
		// LibraryNode userLib = lib1;
		BusinessObjectNode boExtension = ml.addBusinessObjectToLibrary(lib1, "boExtension", false); // No id in id facet
		ChoiceObjectNode choiceExtension = ml.addChoice(lib1, "choiceExtension");
		CoreObjectNode coreExtension = ml.addCoreObjectToLibrary(lib1, "coreExtension");
		EnumerationClosedNode ecExtension = ml.addClosedEnumToLibrary(lib1, "ecExtension");
		EnumerationOpenNode eoExtension = ml.addOpenEnumToLibrary(lib1, "eoExtension");
		VWA_Node vwaExtension = ml.addVWA_ToLibrary(lib1, "vwaExtension");
		boExtension.setExtension(boType);
		choiceExtension.setExtension(choiceType);
		coreExtension.setExtension(coreType);
		ecExtension.setExtension(ecType);
		eoExtension.setExtension(eoType);
		vwaExtension.setExtension(vwaType);
		assertTrue("BoExtension must extend boType.", boExtension.getExtensionBase() == boType);

		// Then - baseLib must list lib1 as where used
		assertTrue("Lib1 must not use other libraries.", lib1.getWhereUsedHandler().getWhereUsed().isEmpty());
		assertTrue("baseLib must use other libraries.", !baseLib.getWhereUsedHandler().getWhereUsed().isEmpty());
		// Then - Lib1 must list baseLib as an assigned library
		assertTrue("Lib1 must have at least one assigned library.", !lib1.getAssignedLibraries().isEmpty());
		assertTrue("baseLib must NOT have an assigned library.", baseLib.getAssignedLibraries().isEmpty());

		// Given - both libraries are valid.
		ValidationFindings findings1 = lib1.validate();
		ValidationFindings findings2 = baseLib.validate();
		MockLibrary.printFindings(findings1);
		MockLibrary.printFindings(findings2);
		assertTrue("Library must be valid.", lib1.validate().isEmpty());
		assertTrue("Library must be valid.", baseLib.validate().isEmpty());

		AbstractLibrary tlLib = baseLib.getTLLibrary();

		// When - create major version of library baseLib containing the base types
		LibraryNode versionedbaseLib = rc.createMajorVersion(baseLib);
		// After major version, the baseLib is no longer in the tlModel or LibraryModelManager
		uploadProject.add(tlLib);

		// Then -
		assertTrue("Must have major version of library 2.", versionedbaseLib != null);
		assertTrue("Must have type providers", !versionedbaseLib.getDescendants_TypeProviders().isEmpty());
		assertTrue("Must be new library.", versionedbaseLib != baseLib);
		assertTrue("Major versions must be head of chain.", versionedbaseLib == versionedbaseLib.getChain().getHead());

		// Then -
		Node newBase = boExtension.getExtensionBase();
		assertTrue("BoExtension must still extend boType.", boExtension.getExtensionBase() == boType);

		// When -
		// Business Logic - setup the map and prepare for the call used by the Version Update Handler.
		//
		// Create replacement map
		HashMap<LibraryNode, LibraryNode> replacementMap = rc.getVersionUpdateMap(lib1.getAssignedLibraries(), true);
		// TODO - test with finalOnly set to true on getVersionUpdateMap()

		// Use calls used by Version Update Handler to replace type users using the replacement map
		lib1.replaceAllUsers(replacementMap);

		// Then - Make sure it worked
		Node newBase2 = boExtension.getExtensionBase();
		assertTrue("BoExtension must extend boType.", boExtension.getExtensionBase() == boType);

		for (ExtensionOwner owner : lib1.getDescendants_ExtensionOwners()) {
			Node base = owner.getExtensionBase();
			if (!(owner.getExtensionBase() instanceof ImpliedNode)) {
				if (owner.getExtensionBase() == null || owner.getExtensionBase().getLibrary() == null)
					LOGGER.debug("Error - " + owner.getNameWithPrefix() + " does not have extension base. ");
				if (owner.getExtensionBase().getLibrary() != versionedbaseLib)
					LOGGER.debug("Error - " + owner + " assigned type is in wrong library: "
							+ ((Node) owner.getExtensionBase()).getNameWithPrefix());
				assertTrue("Extension Owner must be in providerLib version 2.",
						owner.getExtensionBase().getLibrary() == versionedbaseLib);
			}
		}
	}
}

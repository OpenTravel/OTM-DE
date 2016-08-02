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
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ChoiceObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
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

	private LibraryNode providerLib;

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

	@Test
	public void updateVersionTest_AssignedTypes() throws RepositoryException {
		// Create two libraries where one uses types from the other then version the type provider

		// Find a simple type in the provider library to assign
		providerLib = lib2;
		SimpleTypeNode simpleType = ml.addSimpleTypeToLibrary(providerLib, "simpleType");

		// Create user library containing the objects that will get updated and assign them to the found type
		LibraryNode userLib = lib1;
		ml.addOneOfEach(userLib, "User");
		for (TypeUser user : userLib.getDescendants_TypeUsers())
			user.setAssignedType(simpleType);

		// Version the provider library
		providerLib = rc.createMajorVersion(providerLib);
		assertTrue("Must have major version of provider library.", providerLib != null);
		assertTrue("Must have type providers", !providerLib.getDescendants_TypeProviders().isEmpty());
		assertTrue("Must be new library.", providerLib != lib2);
		assertTrue("Major versions must be head of chain.", providerLib == providerLib.getChain().getHead());

		// Business Logic - setup the map and prepare for the call used by the Version Update Handler.
		//
		// Walk selected library type users and collect all used libraries (type assignments and extensions)
		List<LibraryNode> usedLibs = userLib.getAssignedLibraries();
		// Create replacement map
		HashMap<LibraryNode, LibraryNode> replacementMap = rc.getVersionUpdateMap(usedLibs, true);
		// TODO - test with finalOnly set to true on getVersionUpdateMap()
		// Use calls used by Version Update Handler t0 replace type users using the replacement map
		userLib.replaceTypeUsers(replacementMap);

		// Make sure it worked
		for (TypeUser user : userLib.getDescendants_TypeUsers()) {
			if (!(user.getAssignedType() instanceof ImpliedNode)) {
				if (user.getAssignedType().getLibrary() != providerLib)
					LOGGER.debug("Error - " + user + " assigned type is in wrong library: "
							+ ((Node) user.getAssignedType()).getNameWithPrefix());
				assertTrue("Must be in providerLib.", user.getAssignedType().getLibrary() == providerLib);
			}
		}
	}

	@Test
	public void updateVersionTest_BaseTypes() throws RepositoryException {
		// Create two libraries where one extends types from the other then version the type provider

		// Find a Extension Owners in the provider library to assign
		BusinessObjectNode boType = ml.addBusinessObjectToLibrary(lib2, "boType");
		ChoiceObjectNode choiceType = ml.addChoice(lib2, "choiceType");
		CoreObjectNode coreType = ml.addCoreObjectToLibrary(lib2, "coreType");
		EnumerationClosedNode ecType = ml.addClosedEnumToLibrary(lib2, "ecType");
		EnumerationOpenNode eoType = ml.addOpenEnumToLibrary(lib2, "eoType");
		// ExtensionPointNode epType = ml.a(lib2, "epType");
		VWA_Node vwaType = ml.addVWA_ToLibrary(lib2, "vwaType");

		// Create user library containing the objects that will get updated and assign them to the found type
		LibraryNode userLib = lib1;
		BusinessObjectNode boExtension = ml.addBusinessObjectToLibrary(lib1, "boExtension");
		boExtension.setExtension(boType);
		assertTrue("Business object must extend boType.", boExtension.getExtensionBase() == boType);
		ChoiceObjectNode choiceExtension = ml.addChoice(lib1, "choiceExtension");
		choiceExtension.setExtension(choiceType);
		CoreObjectNode coreExtension = ml.addCoreObjectToLibrary(lib1, "coreExtension");
		coreExtension.setExtension(coreType);
		EnumerationClosedNode ecExtension = ml.addClosedEnumToLibrary(lib1, "ecExtension");
		ecExtension.setExtension(ecType);
		EnumerationOpenNode eoExtension = ml.addOpenEnumToLibrary(lib1, "eoExtension");
		eoExtension.setExtension(eoType);
		VWA_Node vwaExtension = ml.addVWA_ToLibrary(lib1, "vwaExtension");
		vwaExtension.setExtension(vwaType);

		// Version the provider library
		assertTrue("Library must be valid.", lib2.validate().isEmpty());
		providerLib = rc.createMajorVersion(lib2);
		assertTrue("Must have major version of provider library.", providerLib != null);
		assertTrue("Must have type providers", !providerLib.getDescendants_TypeProviders().isEmpty());
		assertTrue("Must be new library.", providerLib != lib2);
		assertTrue("Major versions must be head of chain.", providerLib == providerLib.getChain().getHead());

		// Business Logic - setup the map and prepare for the call used by the Version Update Handler.
		//
		// Walk selected library type users and collect all used libraries (type assignments and extensions)
		List<LibraryNode> usedLibs = userLib.getAssignedLibraries();
		assertTrue("There must be one assigned library.", !usedLibs.isEmpty());

		// Create replacement map
		HashMap<LibraryNode, LibraryNode> replacementMap = rc.getVersionUpdateMap(usedLibs, true);
		// TODO - test with finalOnly set to true on getVersionUpdateMap()

		// Use calls used by Version Update Handler to replace type users using the replacement map
		userLib.replaceAllUsers(replacementMap);

		// Make sure it worked
		for (ExtensionOwner owner : userLib.getDescendants_ExtensionOwners()) {
			if (!(owner.getExtensionBase() instanceof ImpliedNode)) {
				if (owner.getExtensionBase().getLibrary() != providerLib)
					LOGGER.debug("Error - " + owner + " assigned type is in wrong library: "
							+ ((Node) owner.getExtensionBase()).getNameWithPrefix());
				assertTrue("Extension Owner must be in providerLib version 2.",
						owner.getExtensionBase().getLibrary() == providerLib);
			}
		}
	}
}

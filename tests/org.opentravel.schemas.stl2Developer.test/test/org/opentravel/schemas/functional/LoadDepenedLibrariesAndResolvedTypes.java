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
package org.opentravel.schemas.functional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.repository.RepositoryIntegrationTestBase;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.opentravel.schemas.utils.PropertyNodeBuilder;
import org.osgi.framework.Version;

/**
 * @author Pawel Jedruch
 * 
 */
public class LoadDepenedLibrariesAndResolvedTypes extends RepositoryIntegrationTestBase {

	private ProjectNode uploadProject;
	private LibraryNode baseLib;
	private LibraryNode extLib;

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
	public void beforeEachTest2() throws RepositoryException, LibrarySaveException {
		uploadProject = createProject("RepositoryProject", getRepositoryForTest(), "dependencies");
		baseLib = LibraryNodeBuilder.create("Base", uploadProject.getNamespace(), "o1", new Version(1, 0, 0)).build(
				uploadProject, pc);
		SimpleTypeNode simpleInBase = ComponentNodeBuilder.createSimpleObject("MyString")
				.assignType(NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE)).get();
		baseLib.addMember(simpleInBase);

		extLib = LibraryNodeBuilder.create("Ext", uploadProject.getNamespace(), "o1", new Version(1, 0, 0)).build(
				uploadProject, pc);
		PropertyNode withAssignedType = PropertyNodeBuilder.create(PropertyNodeType.ATTRIBUTE).setName("Attribute")
				.build();
		VWA_Node vwa = ComponentNodeBuilder.createVWA("VWA").addAttribute(withAssignedType).get();
		extLib.addMember(vwa);
		withAssignedType.setAssignedType(simpleInBase);

		Assert.assertTrue("", uploadProject != null);
		Assert.assertTrue("", baseLib != null);
		Assert.assertTrue("", extLib != null);
		Assert.assertTrue("", baseLib.isEditable());
		Assert.assertTrue("", extLib.isEditable());
		Assert.assertTrue("", baseLib.getProject() == uploadProject);
		Assert.assertTrue("", extLib.getProject() == uploadProject);
		Assert.assertTrue("", simpleInBase != null);
		Assert.assertTrue("", withAssignedType != null);
		Assert.assertTrue("", withAssignedType.getAssignedType() == simpleInBase);
		Assert.assertTrue("", vwa != null);
	}

	@Test
	public void manageOneByOneStartingFromBaseLibrary() throws RepositoryException {
		// When - given libraries are managed in repository
		LibraryChainNode baseChain = rc.manage(getRepositoryForTest(), Collections.singletonList(baseLib)).get(0);
		LibraryChainNode extChain = rc.manage(getRepositoryForTest(), Collections.singletonList(extLib)).get(0);
		// Then
		assertAllLibrariesLoadedCorrectly(baseChain, extChain);
	}

	@Test
	public void manageBothLibraries() throws RepositoryException, LibrarySaveException {
		List<LibraryChainNode> chains = rc.manage(getRepositoryForTest(), Arrays.asList(extLib, baseLib));
		LibraryChainNode extChain = findLibrary(extLib.getName(), chains);
		LibraryChainNode baseChain = findLibrary(baseLib.getName(), chains);

		assertAllLibrariesLoadedCorrectly(baseChain, extChain);
	}

	@Test
	public void manageOnlyLibWithIncludes() throws RepositoryException, LibrarySaveException {
		List<LibraryChainNode> chains = rc.manage(getRepositoryForTest(), Collections.singletonList(extLib));
		LibraryChainNode extChain = findLibrary(extLib.getName(), chains);
		LibraryChainNode baseChain = findLibrary(baseLib.getName(), chains);

		assertAllLibrariesLoadedCorrectly(baseChain, extChain);

	}

	private void assertAllLibrariesLoadedCorrectly(LibraryChainNode baseChain, LibraryChainNode extChain) {
		// Given - an empty project with libraries in the repository

		// find repository item before delete.
		RepositoryItemNode nodeToRetrive = findRepositoryItem(extChain, getRepositoryForTest());

		// Remove libraries from TL and GUI models
		mc.getProjectController().remove((LibraryNavNode) baseChain.getParent());
		mc.getProjectController().remove((LibraryNavNode) extChain.getParent());
		Assert.assertEquals(0, uploadProject.getChildren().size());

		// When - load only 1 library which should also load the other to resolve dependencies
		pc.add(uploadProject, nodeToRetrive.getItem());

		// Then - make sure that base library is loaded and type are resolved
		Assert.assertEquals(2, uploadProject.getChildren().size());
		LibraryChainNode lib = findLibrary(extLib.getName(), uploadProject.getChildren());
		Assert.assertTrue("Ext Library must be in project.", lib != null);
		VWA_Node vwaNode = (VWA_Node) lib.getDescendants_LibraryMemberNodes().get(0);
		Assert.assertTrue("VWA must be found.", vwaNode instanceof VWA_Node);
		AttributeNode attr = (AttributeNode) vwaNode.getFacet_Attributes().getChildren().get(0);
		Assert.assertTrue("attribute must have assigned type.", !attr.isUnAssigned());
	}

	private LibraryChainNode findLibrary(String name, Collection<? extends Node> libs) {
		for (Node n : libs) {
			if (n instanceof LibraryNavNode)
				n = (Node) ((LibraryNavNode) n).getThisLib();
			if (n instanceof LibraryChainNode) {
				LibraryChainNode lch = (LibraryChainNode) n;
				if (name.equals(lch.getHead().getName()))
					return lch;
			}
		}
		return null;
	}
}

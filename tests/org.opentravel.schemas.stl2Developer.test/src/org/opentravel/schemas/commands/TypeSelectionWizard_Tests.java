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
/**
 * 
 */
package org.opentravel.schemas.commands;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.trees.type.TypeTree;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeProviderAndOwners;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class TypeSelectionWizard_Tests extends BaseProjectTest {
	static final Logger LOGGER = LoggerFactory.getLogger(MockLibrary.class);

	MockLibrary ml = null;
	LibraryNode ln = null;
	ModelNode model = null;
	LoadFiles lf = null;

	@Before
	public void beforeAllTests() {
		ml = new MockLibrary();
		lf = new LoadFiles();
	}

	/**
	 * Type Selection Wizard uses getChildren_TypeProviders() for the type tree.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void TS_typeTreeGetChildren_Tests() throws Exception {
		List<TypeProvider> providers = new ArrayList<TypeProvider>();

		// Given - a group of managed libraries
		lf.loadTestGroupAc(mc);
		for (LibraryNode ln : Node.getAllUserLibraries()) {
			LibraryChainNode lcn = new LibraryChainNode(ln);
			getProviders(lcn, providers);
		}
		assert !Node.getModelNode().getChildren().isEmpty();
		List<TypeProviderAndOwners> tpos = Node.getModelNode().getChildrenHandler().getChildren_TypeProviders();
		assert !tpos.isEmpty();
		getProviders(Node.getModelNode(), providers);

		assert providers.size() > 100; // should be much bigger
	}

	private void getProviders(Node root, List<TypeProvider> list) {
		if (root instanceof LibraryNavNode)
			assert root.getChildren_TypeProviders() != null;
		for (TypeProviderAndOwners tpo : root.getChildren_TypeProviders())
			if (tpo instanceof TypeProvider)
				list.add((TypeProvider) tpo);
			else
				getProviders((Node) tpo, list);
	}

	@Test
	public void TS_typeTree_Tests() throws Exception {
		lf.loadTestGroupAc(mc);
		assert !Node.getModelNode().getChildren().isEmpty();
		TypeTree typeTree = new TypeTree(Node.getModelNode(), null); // sets up type tree view part with
																		// TypeTreeContentProvider
		assert typeTree != null;

		// TreeViewer typeViewer;
		//
		// typeViewer = new TreeViewer(parent);
		// typeViewer.setContentProvider(contentProvider);
		// typeViewer.setLabelProvider(new LibraryTreeLabelProvider());
		// typeViewer.setSorter(new TypeTreeSorter());

	}

}

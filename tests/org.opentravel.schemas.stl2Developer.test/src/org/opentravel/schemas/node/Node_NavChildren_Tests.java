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
package org.opentravel.schemas.node;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.types.TestTypes;
import org.opentravel.schemas.types.TypeUserNode;

/**
 * Test the aggregate nodes which are navigation nodes used for library chains.
 * 
 * @author Dave Hollander
 * 
 */
public class Node_NavChildren_Tests {
	ModelNode model = null;
	TestTypes tt = new TestTypes();

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	LibraryNode ln_inChain;
	LibraryChainNode lcn;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();

		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		lcn = new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);
		ml.addOneOfEach(ln_inChain, "OE");

		TestNode tn = new NodeTesters().new TestNode();
		ln_inChain.visitAllNodes(tn);
	}

	@Test
	public void getTests() {
		// check for NPEs
		for (Node n : ln_inChain.getDescendants()) {
			n.getNavChildren();
		}
		// check overridden values
		for (Node n : ln_inChain.getDescendants()) {
			if ((n instanceof LibraryNode) && (n.parent instanceof VersionAggregateNode))
				Assert.assertTrue(n.getNavChildren().isEmpty());
			else if (n instanceof RepositoryNode)
				Assert.assertNull(n.getNavChildren());
			else if (n instanceof AliasNode)
				Assert.assertNull(n.getNavChildren());
			else if (n instanceof FacetNode)
				Assert.assertNull(null); // only aliases are nav children
			else if (n instanceof PropertyNode)
				Assert.assertNull(null); // only aliases are nav children
			else if (n instanceof TypeUserNode)
				Assert.assertNull(null); //
			else if (n instanceof VersionNode)
				Assert.assertNull(null); // returns children of newest version
			else if (n instanceof FamilyNode)
				Assert.assertNull(null); // returns children of newest version
			else if (n instanceof ComponentNode)
				Assert.assertNull(null); // list only contains members for which isNavChild = true
			else {
				if (n.getNavChildren().size() != n.getChildren().size()) {
					List<Node> kids = n.getNavChildren();
					kids = n.getChildren();
				}
				Assert.assertEquals(n.getNavChildren().size(), n.getChildren().size());
			}
		}
	}

	@Test
	public void isTests() {
		// check for NPEs
		for (Node n : ln_inChain.getDescendants()) {
			n.isNavChild();
			n.isNavigation();
		}
	}

	@Test
	public void hasTests() {
		// check for NPEs
		for (Node n : ln_inChain.getDescendants()) {
			n.hasNavChildren();
			n.hasNavChildrenWithProperties();
		}

	}

}

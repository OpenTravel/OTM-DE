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
package org.opentravel.schemas.controllers;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;

/**
 * @author Dave Hollander
 * 
 */
public class Blank_Tests extends BaseTest {
	LibraryNode ln_inChain;
	LibraryChainNode lcn;

	@Before
	public void beforeEachOfTheseTests() {
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		lcn = new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);
	}

	@Test
	public void impliedNodeTests() throws Exception {
		// Test inner find loop
		Node n = null;
		for (LibraryNode ln : ModelNode.getAllLibraries())
			if (ln.isXSDSchema()) {
				n = mockNodeFindNode(ln, "date");
				assert n != null;
				n = mockNodeFindNode(ln, "ID");
				assert n != null;
			}

		// Used throughout construction and getDefaultType()
		TypeProvider emptyNode = (TypeProvider) ModelNode.getEmptyNode();
		TypeProvider sType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypeProvider idType = (TypeProvider) NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE);
		assertTrue("Test Setup Error - no empty type.", emptyNode != null);
		assertTrue("Test Setup Error - no date.", sType != null);
		assertTrue("Test Setup Error - no idType.", idType != null);
	}

	private Node mockNodeFindNode(Node root, String name) {
		Node c = null;
		for (final Node n : root.getChildren()) {
			if (n.getName().equals(name) && !n.isNavigation()) {
				return n;
			} else if ((c = mockNodeFindNode(n, name)) != null) {
				return c;
			}
		}
		return null;
	}

	@Test
	public void blankTest() throws Exception {
		ml.addOneOfEach(ln_inChain, "OE");
		ml.check();
	}

}

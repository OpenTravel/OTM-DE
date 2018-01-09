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

import junit.framework.Assert;

import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.NodeTesters;

/**
 * Make sure all nodes can trace up the tree to the model node.
 * 
 * @author Dave Hollander
 *
 */
public class TestParent {
	LoadFiles lf = new LoadFiles();
	NodeTesters nt = new NodeTesters();
	Library_FunctionTests lt = new Library_FunctionTests();

	@Test
	public void testGetParent() throws Exception {
		MainController mc = OtmRegistry.getMainController();

		lf.loadTestGroupA(mc);
		for (LibraryNode ln : Node.getAllLibraries()) {
			for (Node n : ln.getDescendants_LibraryMemberNodes())
				parentVisitor(n);
		}

	}

	private void parentVisitor(Node target) {
		Node testNode = null;
		Node parent = target;
		do {
			testNode = parent;
			parent = parent.getParent();
		} while (parent != null);
		Assert.assertTrue(testNode instanceof ModelNode);
	}
}

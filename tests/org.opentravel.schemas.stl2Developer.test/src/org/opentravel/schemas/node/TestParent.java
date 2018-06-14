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
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.BaseTest;

/**
 * Make sure all nodes can trace up the tree to the model node.
 * 
 * @author Dave Hollander
 *
 */
public class TestParent extends BaseTest {

	@Test
	public void testGetParent() throws Exception {
		lf.loadTestGroupA(mc);
		for (LibraryNode ln : Node.getAllLibraries()) {
			for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
				parentVisitor(n);
		}

	}

	private void parentVisitor(LibraryMemberInterface n) {
		Node testNode = null;
		Node parent = (Node) n;
		do {
			testNode = parent;
			parent = parent.getParent();
		} while (parent != null);
		Assert.assertTrue("Must be at model node.", testNode instanceof ModelNode);
	}
}

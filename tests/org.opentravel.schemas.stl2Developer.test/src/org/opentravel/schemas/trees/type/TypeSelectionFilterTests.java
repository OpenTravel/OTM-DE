package org.opentravel.schemas.trees.type;

import static org.junit.Assert.assertTrue;

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

import org.junit.Test;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;

/**
 * 
 * Test of the type tree selection filters.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeSelectionFilterTests extends BaseTest {

	TypeSelectionFilter filter;

	/**
	 * Test the base TypeSelectionFilter
	 * 
	 * @throws Exception
	 */
	@Test
	public void baseFilterTest() throws Exception {
		lf.loadTestGroupA(mc); // get some data
		filter = new TypeSelectionFilter();

		boolean result;
		// Run every node and make sure it returns result
		for (LibraryNode ln : Node.getAllLibraries())
			for (Node n : ln.getDescendants()) {
				result = filter.select(null, null, n);
				if (n instanceof TypeProvider)
					assert result; // All type providers must pass
			}
	}

	@Test
	public void vwaSimpleFilterTest() throws Exception {
		lf.loadTestGroupA(mc); // get some data
		filter = new TypeTreeVWASimpleTypeOnlyFilter();

		boolean result;
		// Run every node and make sure it returns result
		for (LibraryNode ln : Node.getAllLibraries())
			for (Node n : ln.getDescendants()) {

				// Make sure filter does not fail.
				filter.isValidSelection(n);

				// Make sure this logic is consistent and test succeeds
				if (n.isVWASimpleAssignable()) {
					assertTrue("Must be assignable", n.isAssignable());
					assertTrue("Must pass valid selection test.", filter.isValidSelection(n));
				}
			}
	}
}

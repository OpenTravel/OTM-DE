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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.trees.type.TypeTreeVersionSelectionFilter;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class GetAncestors_Tests extends BaseProjectTest {
	static final Logger LOGGER = LoggerFactory.getLogger(MockLibrary.class);

	MockLibrary ml = new MockLibrary();
	LibraryNode ln = null;

	String OTA = "OTA2_BuiltIns_v2.0.0"; // name
	String XSD = "XMLSchema";
	static String PREFIX = "PL1";

	List<Node> foundNodes = new ArrayList<>();
	TypeTreeVersionSelectionFilter filter = null;
	List<Node> ancestors = new ArrayList<>();

	/**
	 * 5/18/2018 - The type selection wizard's TypeTreeVersionSelectionFilter assigning a later version of a type fails
	 * to find the proper libraryNavNode in the ancestor list.
	 * <p>
	 * Create ancestor array from seed VWA then verify that top-down traversal will find the seed VWA
	 */
	@Test
	public void ANC_getVWA() {
		List<Node> ancestors = new ArrayList<>();
		ProjectNode project = createProject("Project1", rc.getLocalRepository(), "IT1");

		ln = ml.createNewLibrary(project.getNamespace(), "test", project);
		ml.addOneOfEach(ln.getHead(), PREFIX);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "TBB");
		ml.addAllProperties(bo.getFacet_Summary());

		VWA_Node seed = ml.addVWA_ToLibrary(ln, "VWA1");
		assertTrue("Must not have versions in unmanaged library.", seed.getLaterVersions() == null);

	}

	@Test
	public void ANC_getVWA_Managed() {
		ProjectNode project = createProject("Project1", rc.getLocalRepository(), "IT1");

		LibraryNode ln = ml.createNewManagedLibrary(project.getNamespace(), "test2", project).getHead();
		ml.addOneOfEach(ln.getHead(), PREFIX + "2");
		BusinessObjectNode bo2 = ml.addBusinessObjectToLibrary(ln, "TBB2");
		ml.addAllProperties(bo2.getFacet_Summary());

		VWA_Node seed = ml.addVWA_ToLibrary(ln, "VWA1");
		assertTrue("Must not have versions in unversioned library.", seed.getLaterVersions() == null);

		ancestors.addAll(seed.getAncestors());

		// filter = new TypeTreeVersionSelectionFilter(seed);
		find(seed, ln);
		assert !foundNodes.isEmpty();
	}

	private void find(Node target, Node treeNode) {
		LOGGER.debug("Testing " + treeNode + " to see if it is or contains " + target);
		if (treeNode == target)
			foundNodes.add(treeNode);

		// ancestors.contains() is logic in filter
		for (Node n : treeNode.getChildren())
			// if (filter.select(null, null, n))
			if (ancestors.contains(n))
				find(target, n);

	}
}

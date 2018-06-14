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
package org.opentravel.schemas.node.managers;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionManager;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class VersionManagerTests extends BaseTest {
	static final Logger LOGGER = LoggerFactory.getLogger(VersionManagerTests.class);

	CoreObjectNode base;
	LibraryChainNode chain;

	@Before
	public void beforeEachOfTheseTests() {
		//
		ln = ml.createNewLibrary(pc, "Lib1");
		chain = new LibraryChainNode(ln);
		base = ml.addCoreObjectToLibrary(ln, "BaseCO");
		assertTrue("Must have version node.", base.getVersionNode() != null);
	}

	@Test
	public void constructorTest() {
		VersionManager vm = new VersionManager();
	}

	@Test
	public void addTests() {
		CoreObjectNode a = ml.addCoreObjectToLibrary(ln, "A");
		CoreObjectNode b = ml.addCoreObjectToLibrary(ln, "B");
		CoreObjectNode c = ml.addCoreObjectToLibrary(ln, "C");
		CoreObjectNode d = ml.addCoreObjectToLibrary(ln, "D");
		VersionManager vm = new VersionManager();

		vm.add(a);
		vm.add(d);
		vm.add(c);
		vm.add(b);
		assertTrue("D is newest.", vm.get() == d);
		assertTrue("A is oldest.", vm.getOldestVersion() == a);
		assertTrue("C is previous", vm.getPreviousVersion() == c);

		List<Node> preA = vm.getOlderVersions(a);
		assertTrue(preA.isEmpty());
		List<Node> preB = vm.getOlderVersions(b);
		assertTrue(preB.contains(a));
		List<Node> preC = vm.getOlderVersions(c);
		assertTrue(preC.contains(a));
		List<Node> preD = vm.getOlderVersions(d);
		assertTrue(!preD.isEmpty());
	}
}

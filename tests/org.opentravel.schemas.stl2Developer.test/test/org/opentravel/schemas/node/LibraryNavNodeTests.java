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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.utils.BaseProjectTest;

public class LibraryNavNodeTests extends BaseProjectTest {

	MockLibrary ml = new MockLibrary();

	/**
	 * Check the structure of the passed VWA
	 */
	public void check(LibraryNavNode lnn, boolean validate) {
		assertTrue("Must have project node as parent.", lnn.getParent() instanceof ProjectNode);
		assertTrue("Must have TL Object.", lnn.getTLModelObject() instanceof AbstractLibrary);

		// Check children - make sure they are libraryNavNodes and their parent is this project
		for (Node n : lnn.getChildren()) {
			assertTrue("Child must be library nav node.", n instanceof LibraryInterface);
			// assertTrue("Child might have lnn as parent.", n.getParent() == lnn);
			ml.check(n, validate);
		}
	}

	@Test
	public void LNN_constructorTests() {
		// Given - a library
		TLLibrary tlLib = ml.createTLLibrary("testProject", pc.getDefaultUnmanagedNS());

		// When - a project is created
		ProjectNode pn = new ProjectNode();
		assertTrue(pn.getParent() == Node.getModelNode());

		pn.addToTL(tlLib);

	}

}

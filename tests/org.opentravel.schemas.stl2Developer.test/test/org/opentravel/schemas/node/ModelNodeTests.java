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
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelNodeTests extends BaseTest {
	static final Logger LOGGER = LoggerFactory.getLogger(ModelNodeTests.class);

	/**
	 * Check the structure of the passed Model
	 */
	public void check(ModelNode mn, boolean validate) {
		LOGGER.debug("Checking Model " + mn);

		assertTrue("Must have null as parent.", mn.getParent() == null);
		assertTrue("Must have TL Model.", mn.getTLModel() instanceof TLModel);
		assertTrue("Must have Library Manager.", mn.getLibraryManager() instanceof LibraryModelManager);

		assertTrue("Must have implied.", ModelNode.getEmptyNode() != null);
		assertTrue("Must have implied.", ModelNode.getIndicatorNode() != null);
		assertTrue("Must have implied.", ModelNode.getUnassignedNode() != null);
		assertTrue("Must have implied.", ModelNode.getUndefinedNode() != null);

		// Check children - make sure they are libraryNavNodes and their parent is this project
		for (Node n : mn.getChildren()) {
			assertTrue("Must be implied or project node.", (n instanceof ImpliedNode || n instanceof ProjectNode));
			assertTrue("Child's parent must be this model node.", n.getParent() == mn);
			if (n instanceof ProjectNode) {
				// TLModel pom = ((ProjectNode) n).getTLProject().getModel();
				// TLModel tom = mn.getTLModel();
				assertTrue("Project't tlProject must have this model.",
						((ProjectNode) n).getTLProject().getModel() == mn.getTLModel());
			}
			ml.check(n, validate);
		}
	}

	@Test
	public void MN_checkTests() {
		// Given - a library
		TLLibrary tlLib = ml.createTLLibrary("testProject", pc.getDefaultUnmanagedNS());

		// When - a project is created
		ProjectNode pn = new ProjectNode();
		assertTrue(pn.getParent() == Node.getModelNode());

		pn.addToTL(tlLib);

		check(Node.getModelNode(), true);
	}

	// @Test
	// public void ML_constructorTL_Tests() {
	//// ProjectManager pm = pc.getDefaultProject().getTLProject().getProjectManager();
	//// Project tlp = new Project(pm);
	//// ProjectNode pn2 = new ProjectNode(tlp);
	//
	// }

}

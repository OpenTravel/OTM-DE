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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.node.LibraryTests;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TestTypes;

/**
 * @author Dave Hollander
 * 
 */
public class RepositoryControllerTests {
	ModelNode model = null;
	TestTypes tt = new TestTypes();

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	TestNode tn = new NodeTesters().new TestNode();
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

	}

	@Test
	public void blankTest() throws Exception {
		ml.addOneOfEach(ln_inChain, "OE");
		ln_inChain.visitAllNodes(tn);
	}

	@Test
	public void createVersionErrors() throws Exception {
		// These creates should create NULL libraries because ln is not in a repository.
		DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
		LibraryNode major = rc.createMajorVersion(ln);
		Assert.assertNull(major);
		LibraryNode minor = rc.createMinorVersion(ln);
		Assert.assertNull(minor);
		LibraryNode patch = rc.createPatchVersion(ln);
		Assert.assertNull(patch);

	}
}

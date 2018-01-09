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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;

/**
 * @author Dave Hollander
 * 
 */
public class Blank_Tests {
	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	Library_FunctionTests lt = new Library_FunctionTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeAllTests() {
		mc = OtmRegistry.getMainController(); // New one for each test
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void mockTest() {
		Assert.assertNotNull(mc);
		Assert.assertNotNull(ml);
		Assert.assertNotNull(pc);
		Assert.assertNotNull(defaultProject);
		ml.createNewManagedLibrary("test", defaultProject);
		// 9/12/2015 - this fails when run in ALLTESTS but is OK when run alone.
		// ml.createNewManagedLibrary("http://example.blankTest", "test", defaultProject);
		// ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		// LibraryChainNode lcn = new LibraryChainNode(ln);
	}

}

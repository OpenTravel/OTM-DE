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
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;

/**
 * @author Dave Hollander
 * 
 */
public class Enumeration_Tests {
	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void createEnums() {
		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		TLClosedEnumeration tlc = new TLClosedEnumeration();
		tlc.setName("ClosedEnum");
		TLEnumValue tlcv1 = new TLEnumValue();
		tlcv1.setLiteral("value 1");
		tlc.addValue(tlcv1);
		EnumerationClosedNode closedEnum = new EnumerationClosedNode(tlc);
		Assert.assertNotNull(closedEnum);
		Assert.assertEquals(1, closedEnum.getChildren().size());

		EnumerationOpenNode openEnum = ml.addOpenEnumToLibrary(ln, "OpenEnum");
		Assert.assertNotNull(openEnum);
		Assert.assertEquals(1, openEnum.getChildren().size());
	}

	@Test
	public void changeEnums() throws Exception {
		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		EnumerationOpenNode openEnum = ml.addOpenEnumToLibrary(ln, "OpenEnum");
		EnumerationClosedNode closedEnum = ml.addClosedEnumToLibrary(ln, "ClosedEnum");

		EnumerationOpenNode o2 = new EnumerationOpenNode(closedEnum);
		EnumerationClosedNode c2 = new EnumerationClosedNode(openEnum);

	}

	@Test
	public void changeEnumsManaged() throws Exception {
		LibraryChainNode lcn = ml.createNewManagedLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		ln = lcn.getHead();

		EnumerationOpenNode openEnum = ml.addOpenEnumToLibrary(ln, "OpenEnum");
		EnumerationClosedNode closedEnum = ml.addClosedEnumToLibrary(ln, "ClosedEnum");

		EnumerationOpenNode o2 = new EnumerationOpenNode(closedEnum);
		EnumerationClosedNode c2 = new EnumerationClosedNode(openEnum);

		Assert.assertTrue(ln.isValid()); // validates TL library
		NodeTesters tt = new NodeTesters();
		tt.visitAllNodes(ln);

	}
}

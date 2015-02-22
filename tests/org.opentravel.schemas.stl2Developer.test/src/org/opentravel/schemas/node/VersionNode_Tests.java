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
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;

/**
 * @author Dave Hollander
 * 
 */
public class VersionNode_Tests {
	ModelNode model = null;
	TestTypes tt = new TestTypes();

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
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
	public void constructor() {
		ComponentNode s1 = (ComponentNode) makeSimple("s_1");
		ComponentNode s2 = (ComponentNode) makeSimple("s_2");
		VersionNode v = null;

		try {
			v = new VersionNode(s1); // no library
			Assert.assertFalse(true); // should never reach here
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) s1.getTLModelObject());
		s1.setLibrary(ln_inChain);
		try {
			v = new VersionNode(s1); // no parent
			Assert.assertFalse(true); // should never reach here
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		ln_inChain.linkMember(s1);
		v = new VersionNode(s1);
		Assert.assertNotNull(v.getLibrary());
		Assert.assertEquals(v, s1.getParent());
		Assert.assertTrue(s1.family.equals(v.family));
		Assert.assertEquals(s1, v.getNewestVersion());
		Assert.assertNull(v.getPreviousVersion());

		//
		// Now test with a family member
		//
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) s2.getTLModelObject());
		ln_inChain.linkMember(s2);
		Assert.assertEquals(1, ln_inChain.getSimpleRoot().getChildren().size());
		v = new VersionNode(s2);
		Assert.assertNotNull(v.getLibrary());
		Assert.assertEquals(v, s2.getParent());
		Assert.assertEquals(1, ln_inChain.getSimpleRoot().getChildren().size());

	}

	private Node makeSimple(String name) {
		Node n = new SimpleTypeNode(new TLSimple());
		n.setName(name);
		n.setAssignedType(NodeFinders.findNodeByName("int", Node.XSD_NAMESPACE));
		return n;
	}

}

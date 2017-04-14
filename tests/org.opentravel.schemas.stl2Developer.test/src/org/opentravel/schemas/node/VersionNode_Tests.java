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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
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
		ComponentNode s1 = (ComponentNode) ml.createSimple("s_1");
		ComponentNode s2 = (ComponentNode) ml.createSimple("s_2");
		ComponentNode s3 = (ComponentNode) ml.createSimple("s_3");
		VersionNode v = null;

		// When - illegal state
		assertTrue("S1 must not have library.", s1.getLibrary() == null);
		try {
			v = new VersionNode(s1); // no library
			Assert.assertFalse(true); // should never reach here
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
			assertTrue(v == null);
		}

		// When - illegal state
		try {
			v = new VersionNode(s1); // should fail because it already is wrapped.
			Assert.assertFalse(true); // should never reach here
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		// When added to library chain - creates Version Node, sets library
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) s1.getTLModelObject());
		// Then
		assertTrue("Must have version node.", s1.getVersionNode() != null);
		assertTrue("Must have library.", s1.getLibrary() == ln_inChain);

		// When - added to a chain as is done when opening chains
		try {
			lcn.add(s2); // must be in a library
			Assert.assertFalse(true); // should never reach here
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// When - add to library uses constructor to add to aggregate
		ln_inChain.setEditable(true);
		assertTrue(ln_inChain.isEditable());
		ln_inChain.addMember(s2);
		ln_inChain.addMember(s3);
		// Then -
		assertTrue("S2 must be added to aggregate.", lcn.getSimpleAggregate().contains(s2));
		assertTrue("Must have version node.", s2.getVersionNode() != null);
		assertTrue("S3 must be added to aggregate.", lcn.getSimpleAggregate().contains(s3));
		assertTrue("Must have version node.", s3.getVersionNode() != null);

		// then
		v = s1.getVersionNode();
		Assert.assertNotNull(v.getLibrary());
		Assert.assertEquals(v, s1.getParent());
		Assert.assertEquals(s1, v.getNewestVersion());
		Assert.assertNull(v.getPreviousVersion());
	}

	@Test
	public void projectLoadTest() {
		ProjectNode pn = lf.loadVersionTestProject(pc);
		assertTrue(pn != null);

		// Pre-check assertions
		List<LibraryNode> libs = pn.getLibraries();
		assertTrue(libs.size() == 3);
		assertTrue("Project has one library.", pn.getChildren().size() == 1);
		LibraryNavNode lnn = (LibraryNavNode) pn.getChildren().get(0);
		assertTrue(lnn != null);
		LibraryChainNode lcn = (LibraryChainNode) lnn.getChildren().get(0);
		assertTrue(lcn != null);
		VersionAggregateNode van = lcn.getVersions();
		assertTrue("Version aggregate has 3 libraries.", van.getChildren().size() == 3);
		AggregateNode ca = lcn.getComplexAggregate();
		assertTrue(!ca.getChildren().isEmpty());

		// Find the business object
		BusinessObjectNode bo = null;
		for (Node n : ca.getChildren())
			if (n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
		assertTrue(bo != null);

		// Check version node
		VersionNode vn = bo.getVersionNode();
		assertTrue("BO must have a version node.", vn != null);
		assertTrue("BO parent must be a version node.", bo.getParent() == vn);
		assertTrue("BO must be a child of version node.", vn.getChildren().contains(bo));
		assertTrue("BO must be Version Node head.", vn.getHead() == bo);
		assertTrue("Version node previous must NOT be bo.", vn.getPreviousVersion() != bo);
		assertTrue("Version node previous must be a child of vn.", vn.getChildren().contains(vn.getPreviousVersion()));
		// These will not work until converted over to single VN for all versions of same object.
		// for (Node c : vn.getChildren()) {
		// assertTrue("VN Child head must be bo.", c.getVersionNode().getHead() == bo);
		// assertTrue("VN Child must share version node.", c.getVersionNode() == vn);
		// }

	}

}

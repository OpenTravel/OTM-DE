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
import org.opentravel.schemas.node.AggregateNode.AggregateType;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;

/**
 * Test the aggregate nodes which are navigation nodes used for library chains.
 * 
 * @author Dave Hollander
 * 
 */
public class Aggregate_Tests {
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
	public void aggregateConstructors() {

		AggregateNode ac = new AggregateNode(AggregateType.ComplexTypes, lcn);
		Assert.assertNotNull(ac);
		Assert.assertEquals(lcn, ac.getParent());
		AggregateNode as = new AggregateNode(AggregateType.SimpleTypes, lcn);
		Assert.assertNotNull(as);
		Assert.assertEquals(lcn, as.getParent());
		AggregateNode asvc = new AggregateNode(AggregateType.Service, lcn);
		Assert.assertNotNull(asvc);
		Assert.assertEquals(lcn, asvc.getParent());
		AggregateNode av = new AggregateNode(AggregateType.Versions, lcn);
		Assert.assertNotNull(av);
		Assert.assertEquals(lcn, av.getParent());

		Assert.assertNotNull(ac.getLibrary());
		Assert.assertNotNull(as.getLibrary());
		Assert.assertNotNull(asvc.getLibrary());
		Assert.assertNotNull(av.getLibrary());
	}

	@Test
	public void addChildren() {
		AggregateNode ac = new AggregateNode(AggregateType.ComplexTypes, lcn);
		AggregateNode as = new AggregateNode(AggregateType.SimpleTypes, lcn);
		ComponentNode s1 = (ComponentNode) makeSimple("s_1");
		ComponentNode s2 = (ComponentNode) makeSimple("s_2");
		ComponentNode s3 = (ComponentNode) makeSimple("s_3");
		ComponentNode nf = (ComponentNode) makeSimple("nf");

		try {
			ac.add(s1);
			Assert.assertFalse(true); // should never reach here
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		try {
			as.add(s1);
			Assert.assertFalse(true); // should never reach here
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		ln_inChain.addMember(s1);
		as.add(s1);
		Assert.assertEquals(1, as.getChildren().size());

		// Put s3 in the library but NOT the chain
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) nf.getTLModelObject());
		ln_inChain.linkMember(nf);
		as.add(nf); // Not family member
		Assert.assertEquals(2, as.getChildren().size());

		// Put s2 in the library but NOT the chain
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) s2.getTLModelObject());
		ln_inChain.linkMember(s2);
		as.add(s2); // Invokes family code
		Assert.assertEquals(2, as.getChildren().size());

		AggregateFamilyNode afn = null;
		for (Node n : as.getChildren())
			if (n instanceof AggregateFamilyNode)
				afn = (AggregateFamilyNode) n;
		Assert.assertNotNull(afn);
		Assert.assertEquals(2, afn.getChildren().size());

		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) s3.getTLModelObject());
		ln_inChain.linkMember(s3);
		as.add(s3); // Invokes family code
		Assert.assertEquals(2, as.getChildren().size());
		Assert.assertEquals(3, afn.getChildren().size());

		// Test replacing logic
		ComponentNode s3d = (ComponentNode) makeSimple("s_3d", ln_inChain);
		ComponentNode nd = (ComponentNode) makeSimple("nf", ln_inChain);

		as.add(nd);
		Assert.assertEquals(3, as.getChildren().size());
		Assert.assertEquals(3, afn.getChildren().size());
		// test replacing existing node in family
		as.add(s3d);
		Assert.assertEquals(3, as.getChildren().size());
		Assert.assertEquals(4, afn.getChildren().size());

		// TODO
		// Test if adding to newer version of library in chain
		// Test if adding in family to newer version of library in chain

		// Test Get Children
		Assert.assertEquals(0, as.getNavChildren().size());
		Assert.assertEquals(3, as.getChildren().size());

		// Test Remove
		as.remove(s1);
		Assert.assertEquals(3, as.getChildren().size());
		Assert.assertEquals(3, afn.getChildren().size());
		as.remove(nd);
		Assert.assertEquals(2, as.getChildren().size());
		as.remove(nd); // should fail without error
		Assert.assertEquals(2, as.getChildren().size());
		as.remove(nf);
		as.remove(s2);
		as.remove(s3);
		as.remove(s3d);
		Assert.assertEquals(0, as.getChildren().size());
	}

	private Node makeSimple(String name) {
		Node n = new SimpleTypeNode(new TLSimple());
		n.setName(name);
		n.setAssignedType(NodeFinders.findNodeByName("int", Node.XSD_NAMESPACE));
		return n;
	}

	private Node makeSimple(String name, LibraryNode ln) {
		Node n = makeSimple(name);
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) n.getTLModelObject());
		ln_inChain.linkMember(n);
		return n;
	}
}

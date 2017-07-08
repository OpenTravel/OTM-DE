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

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.AggregateNode.AggregateType;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;
import org.opentravel.schemas.types.TypeProvider;

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
		ln_inChain.setEditable(true);

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
	public void addChildren2() {
		// 4 simple types
		AggregateNode as = (AggregateNode) lcn.getSimpleAggregate();
		ComponentNode s1 = (ComponentNode) makeSimple("s_1");
		ln_inChain.addMember(s1);
		Assert.assertEquals(1, as.getChildren().size());
		ComponentNode nf = (ComponentNode) makeSimple("nf");
		ln_inChain.addMember(nf);
		Assert.assertEquals(2, as.getChildren().size());

		//
		ComponentNode s2 = (ComponentNode) makeSimple("s_2");
		ln_inChain.addMember(s2);
		Assert.assertEquals(3, as.getChildren().size());

		// Test duplicate names - should be added because it is in the same library
		ComponentNode nf2 = (ComponentNode) makeSimple("nf");
		ln_inChain.addMember(nf2); // should be duplicate names in child list.
		Assert.assertEquals(4, as.getChildren().size());

		ComponentNode s3 = (ComponentNode) makeSimple("s_1");
		ln_inChain.addMember(s3);
		Assert.assertEquals(5, as.getChildren().size());

		// Test name matches family name
		ComponentNode s4 = (ComponentNode) makeSimple("s");
		ln_inChain.addMember(s4);
		Assert.assertEquals(6, as.getChildren().size());

		// Test an older version
	}

	@Test
	public void addChildren() {
		// Given - aggregate nodes from a library chain and 4 simples
		AggregateNode simpleAgg = (AggregateNode) lcn.getSimpleAggregate();
		AggregateNode complexAgg = (AggregateNode) lcn.getComplexAggregate();
		ComponentNode s1 = (ComponentNode) makeSimple("s_11");
		ComponentNode s2 = (ComponentNode) makeSimple("s_21");
		ComponentNode s3 = (ComponentNode) makeSimple("s_31");
		ComponentNode nf = (ComponentNode) makeSimple("nf1");

		// Check pre-tests
		// try {
		// complexAgg.add(s1);
		// Assert.assertFalse(true); // should never reach here
		// } catch (IllegalStateException e) {
		// Assert.assertNotNull(e); // should error because of wrong type
		// }
		//
		// try {
		// simpleAgg.add(s1);
		// Assert.assertFalse(true); // should never reach here
		// } catch (IllegalArgumentException e) {
		// Assert.assertNotNull(e); // should error because of simple has no library
		// }

		// addMember() - broadly used. used in node constructors.
		// linkMember() - protected, used by addMember(), used in initial library generation
		//
		// ln_inChain is a library node within the lcn chain.
		ln_inChain.addMember(s1);
		// simpleAgg.add(s1); // not needed but safe
		Assert.assertEquals(1, simpleAgg.getChildren().size());

		// Put nf in the library
		ln_inChain.addMember(nf);
		Assert.assertEquals(2, simpleAgg.getChildren().size());

		// Put s2 in
		ln_inChain.addMember(s2);
		Assert.assertEquals(3, simpleAgg.getChildren().size());

		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) s3.getTLModelObject());
		ln_inChain.addMember(s3);
		assertEquals(4, simpleAgg.getChildren().size());

		// Create 2 simples and add to chain
		ComponentNode s3d = (ComponentNode) makeSimple("s_3d", ln_inChain);
		ComponentNode nd = (ComponentNode) makeSimple("nf", ln_inChain);

		simpleAgg.add(nd);
		simpleAgg.add(s3d);
		assertEquals(6, simpleAgg.getChildren().size());

		// TODO
		// Test if adding to newer version of library in chain
		// Test if adding in family to newer version of library in chain

		// Test Get Children
		Assert.assertEquals(6, simpleAgg.getNavChildren(false).size()); // not overriden, should be child count.
		Assert.assertEquals(6, simpleAgg.getChildren().size());

		// Test Remove
		simpleAgg.remove(s1);
		simpleAgg.remove(nd);
		simpleAgg.remove(nd); // should fail without error
		simpleAgg.remove(nf);
		simpleAgg.remove(s2);
		simpleAgg.remove(s3);
		simpleAgg.remove(s3d);
		Assert.assertEquals(0, simpleAgg.getChildren().size());
	}

	/**
	 * 
	 * @return new simple type with name and type set
	 */
	private Node makeSimple(String name) {
		Node n = new SimpleTypeNode(new TLSimple());
		n.setName(name);
		((SimpleTypeNode) n).setAssignedType((TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE));
		return n;
	}

	/**
	 * 
	 * @return new simple type with name and type set added to TL library and linkMember into chain.
	 */
	private Node makeSimple(String name, LibraryNode ln) {
		Node n = makeSimple(name);
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) n.getTLModelObject());
		ln_inChain.linkMember(n);
		return n;
	}
}

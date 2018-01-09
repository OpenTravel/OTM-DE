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

import javax.xml.namespace.QName;

import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.NodeTesters;

/**
 * @author Dave Hollander
 * 
 */
public class Find_Tests {
	ModelNode model = null;
	NodeTesters tt = new NodeTesters();

	@Test
	public void FinderTest() throws Exception {
		// MainController mc = OtmRegistry.getMainController();
		// LoadFiles lf = new LoadFiles();
		// model = mc.getModelNode();
		//
		// lf.loadTestGroupA(mc);
		// for (LibraryNode ln : model.getUserLibraries()) {
		// tt.visitAllNodes(ln);
		// }
		//
		// QName qn = new QName("NameSpace", "Name");
		// Assert.assertNull(NodeFinders.findTypeProviderByQName(qn));
		//
		// qn = new QName(NS, "OutboundFlight");
		// Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));
		//
		// Node alias = NodeFinders.findTypeProviderByQName(qn);
		// qn = new QName(NS, "Card");
		// Assert.assertNotNull(alias);
		//
		// qn = new QName(NS, "Card");
		// Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));
		//
		// qn = new QName(NS, "TravelerProfile");
		// Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));
		//
		// qn = new QName("http://services.sabre.com/STL/Examples/v02", "SimpleVWA");
		// Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));
		//
		// qn = new QName("http://services.sabre.com/STL/Test4/v02", "BasicCore");
		// Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));
		// Assert.assertNotNull(NodeFinders.findNodeByName("BasicCore", "http://services.sabre.com/STL/Test4/v02"));
	}

	@Test
	public void FinderManagedTest() throws Exception {
		MainController mc = OtmRegistry.getMainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();

		lf.loadTestGroupA(mc);
		for (LibraryNode ln : model.getUserLibraries()) {
			if (!ln.isInChain())
				new LibraryChainNode(ln); // make the library managed.
			tt.visitAllNodes(ln);
		}

		String NS = "http://www.opentravel.org/Sandbox/junits/ns1/v1";
		String NS2 = "http://www.opentravel.org/Sandbox/junits/ns2/v1";
		String NS3 = "http://www.opentravel.org/Sandbox/junits/v1";

		// An alias on Flight in test 1
		QName qn = new QName(NS, "OutboundFlight");
		assertTrue("Must find node: " + qn, NodeFinders.findNodeByQName(qn) != null);

		qn = new QName(NS, "Card");
		assertTrue("Must find node: " + qn, NodeFinders.findNodeByQName(qn) != null);

		qn = new QName(NS, "TravelerProfile");
		assertTrue("Must find node: " + qn, NodeFinders.findNodeByQName(qn) != null);

		// Test 3
		qn = new QName(NS3, "SimpleVWA");
		assertTrue("Must find node: " + qn, NodeFinders.findNodeByQName(qn) != null);
		// Test 2
		qn = new QName(NS2, "BasicCore");
		assertTrue("Must find node: " + qn, NodeFinders.findNodeByQName(qn) != null);
	}

}

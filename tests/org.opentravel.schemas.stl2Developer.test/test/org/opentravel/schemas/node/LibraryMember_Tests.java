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

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test all library members.
 * 
 * <p>
 * Becuase of the unrooted test issues with this test setup, this set of tests will be normally commented out, but used
 * when trouble shooting to assure all the basic library member methods work.
 * <p>
 * Code here should just invoke tests in the individual library member tests.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryMember_Tests extends BaseProjectTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(LibraryMember_Tests.class);

	TypeProvider emptyNode = null;
	TypeProvider sType = null;

	ProjectNode defaultProject;
	LoadFiles lf = new LoadFiles();
	MockLibrary ml = new MockLibrary();
	LibraryChainNode lcn = null;
	LibraryNode ln = null;

	@Before
	public void beforeEachTest() throws Exception {
		LOGGER.debug("***Before Library Member Tests ----------------------");
		// callBeforeEachTest();
		// defaultProject = testProject;
		defaultProject = pc.getDefaultProject();
		ln = ml.createNewLibrary("http://test.com", "CoreTest", defaultProject);
		ln.setEditable(true);

		emptyNode = (TypeProvider) ModelNode.getEmptyNode();
		sType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		assertTrue(sType != null);

	}

	@Test
	public void LM_ConstructorsTests() {
		// BusinessObjectTests BOT = new BusinessObjectTests();
		// CoreObjectTests COT = new CoreObjectTests();
		// ChoiceObjectTests CHT = new ChoiceObjectTests();
		// Enumeration_Tests ENT = new Enumeration_Tests();
		// ExtensionPointNode_Tests EPT = new ExtensionPointNode_Tests();
		// SimpleTypeNodeTests STT = new SimpleTypeNodeTests();
		//
		// BOT.BO_ConstructorsTests();
		// EPT.EP_ConstructorsTests();
		// COT.CO_ConstructorTests();
		// CHT.CH_ConstructorTests();
		// ENT.EN_ConstructorsTests();
		// STT.ST_ConstructorTests();
	}

	@Test
	public void LM_FactoryTest() {
		// NodeFactoryTest NFT = new NodeFactoryTest();
		// NFT.createAllTLLibraryMembers();
	}

	/**
	 * Check the structure of the passed library member
	 */
	public void check(LibraryMemberInterface lm) {
		LOGGER.debug("Checking Library Member: " + lm);
		ml.check((Node) lm);

	}

}

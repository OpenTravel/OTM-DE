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

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.CustomFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ContextualFacetTests {
	static final Logger LOGGER = LoggerFactory.getLogger(ContextualFacetTests.class);

	ModelNode model = null;
	MockLibrary ml = new MockLibrary();
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	LoadFiles lf = null;
	TestNode tn = new NodeTesters().new TestNode();
	TypeProvider emptyNode = null;
	TypeProvider sType = null;

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		lf = new LoadFiles();
		emptyNode = (TypeProvider) ModelNode.getEmptyNode();
		sType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
	}

	@Test
	public void ContextualFacets_v15() {
		// Given a business object in a library to be editable
		OTM16Upgrade.otm16Enabled = false;
		LibraryNode ln = ml.createNewLibrary_Empty("http://example.com", "TestLib1", defaultProject);
		ln.setEditable(true);
		BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		bo.setName("TestBO");
		ln.addMember(bo);
		assertTrue(bo.isEditable_newToChain()); // required to add facets
		// Given - an id facet property to make the bo valid
		TypeProvider string = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		new ElementNode(bo.getFacet_ID(), "TestEleInID" + bo.getName(), string);
		//
		ml.check(bo);

		// When - using addFacet to add a custom facet
		ContextualFacetNode cf = bo.addFacet("Custom1", TLFacetType.CUSTOM);
		// Then
		assertTrue(cf != null);
		assertTrue(cf.getParent() == bo);
		assertTrue(cf.getOwningComponent() == bo);
		assertTrue(cf instanceof CustomFacetNode);
		assertTrue("Identity listener must be set.", Node.GetNode(((CustomFacetNode) cf).getTLModelObject()) == cf);

		//
		// Simulate construction in LibraryChildrenHandler - get tlCFs first using newObjectNode which does NOT add to
		// library or tlLibrary
		//
		// Given - a TLContextualFacet member of a TLBusinessObject
		TLBusinessObject tlBO = new TLBusinessObject();
		tlBO.setName("BO2");
		TLContextualFacet tlCf = ContextualFacetNode.createTL("Custom2", TLFacetType.CUSTOM);
		tlBO.addCustomFacet(tlCf);
		//
		// When - factory used to add a custom facet
		ContextualFacetNode cf2 = (ContextualFacetNode) NodeFactory.newLibraryMember(tlCf);
		// Then - cf2 not created. Will be added when factory works on tlBO
		assertTrue(cf2 == null);
		// When - BO created using main factory
		BusinessObjectNode bo2 = (BusinessObjectNode) NodeFactory.newChild(null, tlBO);
		// Then - bo2cf is created
		assertTrue(!bo2.getCustomFacets().isEmpty());
		CustomFacetNode bo2cf = (CustomFacetNode) bo2.findChildByName("Custom2");
		assertTrue(bo2cf != null);
		assertTrue(bo2cf.getParent() == bo2);
		assertTrue(bo2cf.getOwningComponent() == bo2);
		assertTrue(bo2cf.getLibrary() == null);
		assertTrue(bo2cf.getTLModelObject() == tlCf);
		assertTrue(Node.GetNode(bo2cf.getTLModelObject()) == bo2cf);

		// Then
		ml.check(bo);

		// When - version the library
		LibraryChainNode lcn = new LibraryChainNode(ln);
		// Then - assure contextual facets are NOT wrapped in version nodes
		for (Node n : bo.getChildren())
			assertTrue(n instanceof FacetNode);
	}

	@Test
	public void ContextualFacets_v16() {
		// Given - an editable library
		OTM16Upgrade.otm16Enabled = true;
		LibraryNode ln = ml.createNewLibrary_Empty("http://example.com/t2", "TestLib2", defaultProject);
		// Given - make the library versioned
		// new LibraryChainNode(ln);
		ln.setEditable(true);

		// Given - a business object in library
		BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		bo.setName("TestBO");
		ln.addMember(bo);
		assertTrue(bo.isEditable_newToChain()); // required to add facets
		// Given - an id facet property to make the bo valid
		TypeProvider string = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		assertTrue(string != null);
		new ElementNode(bo.getFacet_ID(), "TestEleInID" + bo.getName(), string);
		//
		int count = ln.getDescendants_LibraryMembers().size();
		ml.check(bo);
		ml.check(ln);

		// When - addFacet() used to add a custom facet
		ContextualFacetNode cf = bo.addFacet("Custom1", TLFacetType.CUSTOM);
		// Then - check contextual facet
		assertTrue(cf != null);
		assertTrue(cf instanceof CustomFacetNode);
		assertTrue(!(cf instanceof ContributedFacetNode));
		assertTrue("Identity listener must be set.", Node.GetNode(((CustomFacetNode) cf).getTLModelObject()) == cf);
		assertTrue(ln.contains(cf));
		assertTrue(cf.getLibrary() == ln);
		// Only true if v15
		// assertTrue("Contextual Facet parent must be nav node", cf.getParent() == bo.getParent());
		// True in non-versioned, v16 library
		assertTrue(cf.getParent() instanceof NavNode);
		// ??? - contextual facets are NOT Versioned!
		// True in versioned library
		// ??? - assertTrue(cf.getParent() instanceof VersionNode);

		// Then - check contributed facet
		ContributedFacetNode contrib = cf.getWhereContributed();
		assertTrue(contrib != null);
		assertTrue(contrib.getOwningComponent() == bo);
		// Not true - assertTrue(cf.getOwningComponent() == bo);
		ml.check(cf);

		// When - adding elements and attributes to contextual facet
		new AttributeNode(cf, "att1");
		new ElementNode(cf, "Ele1");
		// Then
		assertTrue("Must find child.", cf.findChildByName("att1") != null);
		assertTrue("Must find child.", cf.findChildByName("Ele1") != null);
		assertTrue("Must find child.", contrib.findChildByName("att1") != null);
		assertTrue("Must find child.", contrib.findChildByName("Ele1") != null);

		// When - adding elements and attributes to contributed facet
		new AttributeNode(contrib, "att2");
		new ElementNode(contrib, "Ele2");
		// Then
		assertTrue("Must find child.", cf.findChildByName("att2") != null);
		assertTrue("Must find child.", cf.findChildByName("Ele2") != null);
		assertTrue("Must find child.", contrib.findChildByName("att2") != null);
		assertTrue("Must find child.", contrib.findChildByName("Ele2") != null);

		//
		// Simulate construction in LibraryChildrenHandler - get tlCFs first using newObjectNode which does NOT add to
		// library or tlLibrary
		//
		// Given - a TLContextualFacet member of a TLBusinessObject
		TLBusinessObject tlBO = new TLBusinessObject();
		tlBO.setName("BO2");
		TLContextualFacet tlCf = ContextualFacetNode.createTL("Custom2", TLFacetType.CUSTOM);
		tlBO.addCustomFacet(tlCf);
		//
		// When - factory used to add a custom facet
		ContextualFacetNode cf2 = (ContextualFacetNode) NodeFactory.newLibraryMember(tlCf);
		// Then - cf2 created but not added to any library and does NOT have contributed
		assertTrue(cf2 != null);
		assertTrue(cf2.getWhereContributed() == null);
		assertTrue(cf2.getLibrary() == null);
		assertTrue(cf2.getParent() == null);
		assertTrue("Identity listener must be set.", Node.GetNode(cf2.getTLModelObject()) == cf2);
		// When - added to library
		ln.addMember(cf2);
		assertTrue(cf2.getLibrary() == ln);
		assertTrue(cf2.getParent() instanceof NavNode);

		// When - BO created using main factory
		BusinessObjectNode bo2 = (BusinessObjectNode) NodeFactory.newChild(ln, tlBO);
		List<Node> kids = bo2.getChildren();
		assertTrue(cf2.getWhereContributed() != null);
		bo2.getChildren().contains(cf2.getWhereContributed());
		// Then
		assertTrue(cf2.getName().startsWith(bo2.getName()));
		assertTrue(cf2.getWhereContributed().getName().startsWith(bo2.getName()));

		// When - adding other facet types
		bo.addFacet("q1", TLFacetType.QUERY);
		bo.addFacet("u1", TLFacetType.UPDATE);

		// Then
		ml.check(ln);

		// Then - assure contextual facets are NOT wrapped in version nodes
		for (Node n : bo.getChildren())
			assertTrue(n instanceof FacetNode);

		OTM16Upgrade.otm16Enabled = false;
	}

	/**
	 * all tests to be used in these tests and by other junits
	 */
	public void check(ContextualFacetNode cf) {
		// TODO - migrate code from FacetNode
	}

}

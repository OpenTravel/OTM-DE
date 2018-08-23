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
package org.opentravel.schemas.actions;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opentravel.schemas.actions.ChangeActionController.HistoryItem;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.SubType;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test change actions.
 * <ul>
 * <li>Type
 * <li>Containing Library - edited node has different library than its TL library
 * <li>Property's Owning Facets
 * </ul>
 * 
 * @author Dave Hollander
 * 
 */
public class ChangeNodeActionTests extends BaseTest {
	static final Logger LOGGER = LoggerFactory.getLogger(ChangeNodeActionTests.class);

	LibraryNode destLib;
	ChangeActionController controller = new ChangeActionController();

	@Test
	public void change_libraryChangeTests() {
		ln = ml.createNewLibrary_Empty(defaultProject.getNamespace(), "ChangeTest", defaultProject);
		destLib = ml.createNewLibrary_Empty(defaultProject.getNamespace(), "ChangeTestDest", defaultProject);
		addOneOfEachChangeable(ln, "1");

		ml.check();

		// When - moved to dest library
		List<HistoryItem> items = new ArrayList<>();
		for (LibraryMemberInterface lm : ln.get_LibraryMembers()) {
			items.add(controller.changeLibrary(lm, destLib));
			assertTrue(destLib.getTLModelObject().getNamedMembers().contains(lm.getTLModelObject()));
		}
		// Then - they are all in dest library
		for (LibraryMemberInterface lm : defaultProject.getDescendants_LibraryMembers())
			assertTrue(lm.getLibrary() == destLib);

		for (HistoryItem item : items)
			controller.undo(item);

		// Then - they are all back in 1st library
		for (LibraryMemberInterface lm : defaultProject.getDescendants_LibraryMembers())
			assertTrue(lm.getLibrary() == ln);
		ml.check();
	}

	@Test
	public void change_objectTypeChangeTests() {
		ln = ml.createNewLibrary_Empty(defaultProject.getNamespace(), "ChangeTest", defaultProject);
		destLib = ml.createNewLibrary_Empty(defaultProject.getNamespace(), "ChangeTestDest", defaultProject);
		addOneOfEachChangeable(ln, "1");

		ml.check();

		// When - rotated through all allowed sub-types
		List<HistoryItem> items = new ArrayList<>();
		for (LibraryMemberInterface lm : ln.get_LibraryMembers())
			for (SubType st : getSubTypes()) {
				items.add(controller.changeObject(lm, st));
				ml.check(ln);
			}

		for (HistoryItem item : items)
			controller.undo(item);

		ml.check();
	}

	@Test
	public void change_owningFacetTests() {
		controller = new ChangeActionController();
		// Given - a BO with properties in detail facet
		ln = ml.createNewLibrary_Empty(defaultProject.getNamespace(), "ChangeTest", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "ChangeBO");
		List<PropertyNode> newProps = ml.addProperties(bo.getFacet_Detail(), "1");
		ml.check();

		// When - move properties to summary
		List<HistoryItem> items = new ArrayList<>();
		for (PropertyNode property : newProps)
			items.add(controller.changeOwningFacet(property, bo.getFacet_Summary()));

		// Then
		for (PropertyNode property : newProps)
			assertTrue(property.getParent() == bo.getFacet_Summary());
		ml.check();

		// When - undo
		for (HistoryItem item : items)
			controller.undo(item);

		// Then
		for (PropertyNode property : newProps)
			assertTrue(property.getParent() == bo.getFacet_Detail());
		ml.check();
	}

	@Test
	public void change_changeFromSimple() {
		ln = ml.createNewLibrary_Empty(defaultProject.getNamespace(), "ChangeTest", defaultProject);
		// Given a core
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "ChangeCore");
		ml.addAllProperties(core.getFacet_Summary(), "t2", ml.getXsdInt());
		core.setAssignedType(ml.getXsdString());
		int coreSize = core.getFacet_Summary().getChildren().size();
		ml.check();

		// When - changed from simple
		HistoryItem item = controller.changeFromSimple(core, core.getFacet_Summary());
		// Then
		assertTrue(coreSize + 1 == core.getFacet_Summary().getChildren().size());
		ml.check();

		// When undone
		controller.undo(item);
		// Then
		assertTrue(coreSize == core.getFacet_Summary().getChildren().size());
		ml.check();

		// Given a VWA
		AttributeNode ca1 = new AttributeNode(core.getFacet_Summary(), "ca1", ml.getXsdInt());
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "ChangeVWA");
		AttributeNode va1 = new AttributeNode(vwa.getFacet_Attributes(), "va1", ml.getXsdDate());
		int vwaSize = vwa.getFacet_Attributes().getChildren().size();
		ml.check();

		// When - changed from simple
		item = controller.changeFromSimple(vwa, vwa.getFacet_Attributes());

		// Then
		assertTrue(vwaSize + 1 == vwa.getFacet_Attributes().getChildren().size());
		ml.check();

		// When undone
		controller.undo(item);
		// Then
		assertTrue(vwaSize == vwa.getFacet_Attributes().getChildren().size());
		ml.check();

	}

	@Test
	public void change_changeSimple() {
		ln = ml.createNewLibrary_Empty(defaultProject.getNamespace(), "ChangeTest", defaultProject);
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "ChangeCore");
		AttributeNode ca1 = new AttributeNode(core.getFacet_Summary(), "ca1", ml.getXsdInt());
		ml.addAllProperties(core.getFacet_Summary(), "t2", ml.getXsdInt());
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "ChangeVWA");
		AttributeNode va1 = new AttributeNode(vwa.getFacet_Attributes(), "va1", ml.getXsdDate());
		int coreSize = core.getFacet_Summary().getChildren().size();
		int vwaSize = vwa.getFacet_Attributes().getChildren().size();

		ml.check();

		// When - changed to simple
		List<HistoryItem> items = new ArrayList<>();
		items.add(controller.changeToSimple(va1));
		for (Node p : core.getFacet_Summary().getChildren_New())
			items.add(controller.changeToSimple((PropertyNode) p));

		// Then
		assertTrue("Must have fewer attributes after change.",
				vwaSize == 1 + vwa.getFacet_Attributes().getChildren().size());
		assertTrue(vwa.getSimpleAttribute().getAssignedType() == ml.getXsdDate());

		assertTrue(core.getSimpleAttribute().getAssignedType() == ml.getXsdInt());
		// Indicators are not supported as simple types
		assertTrue("Must have fewer attributes after change.", 6 == core.getFacet_Summary().getChildren().size());
		ml.check();

		// When undone
		for (HistoryItem item : items)
			controller.undo(item);

		// Then
		assertTrue("Must have all attributes after change.", vwaSize == vwa.getFacet_Attributes().getChildren().size());
		assertTrue("Must have all attributes after change.", coreSize == core.getFacet_Summary().getChildren().size());
		ml.check();
	}

	private List<SubType> getSubTypes() {
		List<SubType> allowedObjectTypes = new ArrayList<>();
		allowedObjectTypes
				.addAll(Arrays.asList(SubType.BUSINESS_OBJECT, SubType.CORE_OBJECT, SubType.VALUE_WITH_ATTRS));
		return allowedObjectTypes;
	}

	private void addOneOfEachChangeable(LibraryNode ln, String suffix) {
		ml.addBusinessObjectToLibrary(ln, "ChangeBO" + suffix);
		ml.addCoreObjectToLibrary(ln, "ChangeCore" + suffix);
		ml.addVWA_ToLibrary(ln, "ChangeVWA" + suffix);
	}
}

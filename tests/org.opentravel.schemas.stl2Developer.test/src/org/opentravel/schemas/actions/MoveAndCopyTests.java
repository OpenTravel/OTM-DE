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
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Move and Copy methods and commands.
 * 
 * @author Dave Hollander
 * 
 */
public class MoveAndCopyTests extends BaseTest {
	static final Logger LOGGER = LoggerFactory.getLogger(MoveAndCopyTests.class);

	@Test
	public void move_MockObjectMoveTests() {
		// Given - Library in default project made editable with one of each library member type
		ln = ml.createNewLibrary_Empty(defaultProject.getNamespace(), "L1", defaultProject);
		for (LibraryNode ln : defaultProject.getLibraries())
			ln.setEditable(true);
		ml.addOneOfEach(ln, "T1");
		// Given - a business object to be extended
		BusinessObjectNode sourceBO = ml.addBusinessObjectToLibrary(ln, "SourceBO");
		// Given - a core to test the simple facet assignments
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "TestCore");
		core.setAssignedType(ml.getXsdDate());
		// Given - the contextual facets will have to adjust when owners are moved.
		List<ContextualFacetNode> cfList = ln.getDescendants_ContextualFacets();

		// Given - a second project
		ProjectNode pn = createProject();
		LibraryNode dest = ml.createNewManagedLibrary("DestLib", pn).getHead();
		assert dest.isEditable();
		// Given - a bo that extends a bo to be moved with elements and attributes
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty(dest, "TargetBO");
		bo.setExtension(sourceBO);
		assert bo.getExtensionBase() == sourceBO;
		assert sourceBO.getWhereExtendedHandler().getWhereExtended().contains(bo);
		ElementNode ele = new ElementNode(bo.getFacet_Summary(), "E1", ml.getXsdString());
		AttributeNode attr = new AttributeNode(bo.getFacet_Summary(), "a1");
		attr.setAssignedType(core);
		assert attr.getAssignedType() == core;

		// Given - the model is valid
		ml.check();

		for (LibraryMemberInterface lm : ln.get_LibraryMembers()) {
			if (lm instanceof TypeProvider) {
				ele.setAssignedType((TypeProvider) lm);
				assert ele.getAssignedType() == lm;
			}

			// When - Move to dest library
			LOGGER.debug("Ready to move " + lm + " to " + dest);
			dest.addMember(lm);

			// Assignment is still valid
			LOGGER.debug("After move, ele is assigned " + ele.getAssignedType());
			if (lm instanceof TypeProvider)
				assert ele.getAssignedType() == lm;

			// Core assignment still valid
			if (lm instanceof CoreObjectNode && lm.getName().equals(core.getName()))
				assertTrue("Attribute must still be assigned to core.", attr.getAssignedType() == core);
		}

		ml.check();

		// TODO - make assignment to core simple type before move then test it
	}

	/**
	 * Run tests against default project with loaded files.
	 * 
	 * @throws Exception
	 */
	@Test
	public void moveTestGroupATest() throws Exception {
		OTM16Upgrade.otm16Enabled = true;

		// Given - libraries loaded into default project made editable
		lf.loadTestGroupA(mc);
		for (LibraryNode ln : defaultProject.getLibraries())
			ln.setEditable(true);

		// Given - a second project
		ProjectNode pn = createProject();
		ln = ml.createNewManagedLibrary("DestLib", pn).getHead();
		assert ln.isEditable();

		// Given - the model is valid
		ml.check();

		// Given - the action class for move
		MoveObjectToLibraryAction action = new MoveObjectToLibraryAction(null, ln);

		ArrayList<AliasNode> destAliases = new ArrayList<>();

		// When - each of the loaded objects is moved to new library
		for (LibraryMemberInterface lm : defaultProject.getDescendants_LibraryMembers()) {
			// Pre-check assertions
			LibraryNode sourceLib = lm.getLibrary();
			assert sourceLib.contains((Node) lm);
			assert sourceLib.isEditable();
			assert !ln.contains((Node) lm);
			ml.check((Node) lm);

			Collection<TypeUser> users, stUsers = null;
			if (lm instanceof CoreObjectNode && lm.getName().equals("PaymentCard")) {
				LOGGER.debug("Moving core objects used as types causes problems.");
				users = ((CoreObjectNode) lm).getWhereAssigned();
				stUsers = ((CoreObjectNode) lm).getFacet_Simple().getWhereAssigned();
			}

			// If there is a name collision then the resulting library will not be valid
			Node nameMatch = ln.findLibraryMemberByName(lm.getName());
			assert nameMatch == null;
			if (lm.getAliases() != null)
				destAliases.addAll(lm.getAliases());

			LOGGER.debug("Moving " + lm.getClass().getSimpleName() + " " + lm);
			action.moveNode((ComponentNode) lm, ln);

			assertTrue("Must have removed LM", !sourceLib.contains((Node) lm));
			assertTrue("Must have added LM", ln.contains((Node) lm));

			if (lm.getName().equals("Profile"))
				LOGGER.debug("Error case");

			ml.check((Node) lm);
		}

		// Then - resulting model must be valid - no name collisions in Group A
		ml.check();

		// Then - source libraries must be empty
		List<LibraryMemberInterface> shouldBeEmpty = defaultProject.getDescendants_LibraryMembers();
		for (LibraryNode ln : defaultProject.getLibraries())
			assertTrue("Source library " + ln + " must be empty.", ln.isEmpty());

		OTM16Upgrade.otm16Enabled = false;
	}

}

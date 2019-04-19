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

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.node.typeProviders.TypeProviders;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.FacetOwners;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test Move and Copy methods and commands.
 * 
 * @author Dave Hollander
 * 
 */
public class MoveAndCopyTests extends BaseTest {
    static final Logger LOGGER = LoggerFactory.getLogger( MoveAndCopyTests.class );

    @Test
    public void move_MockObjectMoveTests() {
        // Given - Library in default project made editable with one of each library member type
        ln = ml.createNewLibrary_Empty( defaultProject.getNamespace(), "L1", defaultProject );
        for (LibraryNode ln : defaultProject.getLibraries())
            ln.setEditable( true );
        ml.addOneOfEach( ln, "T1" );
        // Given - a business object to be extended
        BusinessObjectNode sourceBO = ml.addBusinessObjectToLibrary( ln, "SourceBO" );
        // Given - a core to test the simple facet assignments
        CoreObjectNode core = ml.addCoreObjectToLibrary( ln, "TestCore" );
        core.setAssignedType( ml.getXsdDate() );
        // Given - the contextual facets will have to adjust when owners are moved.
        List<ContextualFacetNode> cfList = ln.getDescendants_ContextualFacets();

        // make assignment to core simple type before move then test it
        AttributeNode assignedCoreSimple =
            new AttributeNode( sourceBO.getFacet_Simple(), "assignedCoreSimple", core.getFacet_Simple() );
        assert assignedCoreSimple.getAssignedType() == core.getFacet_Simple();
        assert core.getFacet_Simple().getWhereAssigned().contains( assignedCoreSimple );

        // Given - a second project
        ProjectNode pn = createProject();
        LibraryNode dest = ml.createNewManagedLibrary( "DestLib", pn ).getHead();
        assert dest.isEditable();
        // Given - a bo that extends a bo to be moved with elements and attributes
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty( dest, "TargetBO" );
        bo.setExtension( sourceBO );
        assert bo.getExtensionBase() == sourceBO;
        assert sourceBO.getWhereExtendedHandler().getWhereExtended().contains( bo );
        ElementNode ele = new ElementNode( bo.getFacet_Summary(), "E1", ml.getXsdString() );
        AttributeNode attr = new AttributeNode( bo.getFacet_Summary(), "a1" );
        attr.setAssignedType( core );
        assert attr.getAssignedType() == core;

        // Given - the model is valid
        ml.check();

        for (LibraryMemberInterface lm : ln.get_LibraryMembers()) {
            if (lm instanceof TypeProvider) {
                ele.setAssignedType( (TypeProvider) lm );
                assert ele.getAssignedType() == lm;
            }

            // When - Move to dest library
            // LOGGER.debug("Ready to move " + lm + " to " + dest);
            dest.addMember( lm );

            // Assignment is still valid
            // LOGGER.debug("After move, ele is assigned " + ele.getAssignedType());
            if (lm instanceof TypeProvider)
                assert ele.getAssignedType() == lm;

            // Core assignment still valid
            if (lm instanceof CoreObjectNode && lm.getName().equals( core.getName() ))
                assertTrue( "Attribute must still be assigned to core.", attr.getAssignedType() == core );
        }

        ml.check();

        // check assignment to core simple type
        assert assignedCoreSimple.getAssignedType() == core.getFacet_Simple();
        assert core.getFacet_Simple().getWhereAssigned().contains( assignedCoreSimple );

    }

    /**
     * Test assumptions in LibraryNode.addMember(). Add member is used to add new objects as well as to move objects
     * 
     * @throws Exception
     */
    @Test
    public void moveTest_addMember_add() throws Exception {
        // Given - an empty library
        ln = ml.createNewLibrary_Empty( pc.getDefaultUnmanagedNS(), "Lib1", defaultProject );

        // Given - an invalid core object to add
        TLCoreObject tlCore = new TLCoreObject();
        CoreObjectNode core = new CoreObjectNode( tlCore );

        // When added
        ln.addMember( core );

        // Then
        assertTrue( "Library must contain core object.", ln.contains( core ) );
    }

    /**
     * Test assumptions in LibraryNode.addMember(). Add member is used to add new objects as well as to move objects
     * 
     * @throws Exception
     */
    @Test
    public void moveTest_addMember_move() throws Exception {
        // Given - an empty library
        ln = ml.createNewLibrary_Empty( pc.getDefaultUnmanagedNS(), "Lib1", defaultProject );
        LibraryNode ln2 = ml.createNewLibrary_Empty( pc.getDefaultUnmanagedNS(), "Lib2", defaultProject );

        // Given - an invalid core object to added to ln
        TLCoreObject tlCore = new TLCoreObject();
        CoreObjectNode core = new CoreObjectNode( tlCore );
        Collection<TypeUser> ru = core.getFacet_Role().getWhereAssigned();
        RoleFacetNode rf = core.getFacet_Role();

        ln.getTLLibrary().addNamedMember( core.getTLModelObject() );
        core.setLibrary( ln );
        ln.getChildrenHandler().add( (LibraryMemberInterface) core );

        // ln.addMember(core);
        // assert ln.contains(core);
        Collection<TypeUser> ru2 = core.getFacet_Role().getWhereAssigned();
        assert ru2.size() == ru.size();

        // Given - an element assigned core as type
        CoreObjectNode c2 = ml.addCoreObjectToLibrary( ln, "C2" );
        ElementNode ele1 = new ElementNode( c2.getFacet_Attributes(), "E1", core );
        assert ele1.getAssignedType() == core;

        // // Given - an attribute assigned core simple facet as type
        // VWA_Node vwa = ml.addVWA_ToLibrary(ln, "vwa");
        // AttributeNode att1 = new AttributeNode(vwa.getFacet_Attributes(), "att1", core.getFacet_Simple());
        // assert att1.getAssignedType() == core.getFacet_Simple();

        // Try adding to TL library with out removing first.

        // When moved to lib2
        ln2.addMember( core );

        // Then -
        assertTrue( ln2.contains( core ) );
        assertTrue( !ln.contains( core ) );
        assert ele1.getAssignedType() == core;
        // assert att1.getAssignedType() == core.getFacet_Simple();
    }

    /**
     * Run tests against default project with loaded files.
     * 
     * @throws Exception
     */
    @Test
    public void moveTestGroupATest() throws Exception {
        // Given - libraries loaded into default project made editable
        lf.loadTestGroupA( mc );
        for (LibraryNode ln : defaultProject.getLibraries())
            ln.setEditable( true );

        // Given - a second project
        ProjectNode pn = createProject();
        ln = ml.createNewManagedLibrary( "DestLib", pn ).getHead();
        assert ln.isEditable();

        // Given - the model is valid
        ml.check();

        // Given - the action class for move
        MoveObjectToLibraryAction action = new MoveObjectToLibraryAction( null, ln );
        LibraryMemberInterface lastTroubleMaker = null;

        // Check loaded project for name collisions - if so, validation will fail after moves
        boolean canBeValid = true;
        List<String> sourceLibMemberNames = new ArrayList<>();
        for (LibraryMemberInterface lm : defaultProject.getDescendants_LibraryMembers()) {
            if (!sourceLibMemberNames.contains( lm.getName() ))
                sourceLibMemberNames.add( lm.getName() );
            else {
                canBeValid = false;
                LOGGER.debug( lm + " is a duplicate name." );
            }
        }

        ArrayList<AliasNode> destAliases = new ArrayList<>();

        // When - each of the loaded objects is moved to new library
        for (LibraryMemberInterface lm : defaultProject.getDescendants_LibraryMembers()) {
            // Pre-check assertions
            LibraryNode sourceLib = lm.getLibrary();
            assert sourceLib.contains( (Node) lm );
            assert sourceLib.isEditable();
            assert !ln.contains( (Node) lm );

            ml.check( (Node) lm, canBeValid );

            Collection<TypeUser> users, stUsers = null, stUsersMoved = null;
            Collection<AliasNode> aliases;
            // Given - the core object is correct before moving
            if (lm instanceof CoreObjectNode && lm.getName().equals( "PaymentCard" )) {
                LOGGER.debug( "Moving core objects used as types causes problems." );
                users = ((CoreObjectNode) lm).getWhereAssigned();
                stUsers = ((CoreObjectNode) lm).getFacet_Simple().getWhereAssigned();
                LOGGER.debug( "Simple Facet " + lm + " where used count "
                    + ((TypeProviders) ((FacetOwners) lm).getFacet_Simple()).getWhereUsedCount() );
                for (TypeUser user : stUsers) {
                    TLModelElement type = user.getAssignedTLObject();
                    assertTrue( "Must have assigned TL Type.", type != null );
                }
                aliases = lm.getAliases();
                for (AliasNode alias : aliases) {
                    LOGGER.debug( "Alias " + alias + " where used count " + alias.getWhereUsedCount() );
                    LOGGER.debug( "Alias " + alias + " where assigned count " + alias.getWhereAssignedCount() );
                    // TODO - move to Alias tests
                    assertTrue( "Counts must be equal.", alias.getWhereUsedCount() == alias.getWhereAssignedCount() );
                }
            }

            if (lm.getAliases() != null)
                destAliases.addAll( lm.getAliases() );

            // Trap for debugging
            String troubleMakerName = "ProfileService";
            if (lm.getName().equals( troubleMakerName )) {
                LOGGER.debug( "Ready to move trouble maker: " + lm );
                ml.check( (Node) lm, canBeValid );
                lastTroubleMaker = lm;
            }

            //
            // When - moved using action class
            ///
            // LOGGER.debug("Moving " + lm.getClass().getSimpleName() + " " + lm);
            action.moveNode( (ComponentNode) lm, ln );

            assertTrue( "Must have removed LM from source library.", !sourceLib.contains( (Node) lm ) );
            assertTrue( "Must have added LM to destination library.", ln.contains( (Node) lm ) );

            // Then - moved core object must still be correct
            if (lm instanceof CoreObjectNode && lm.getName().equals( "PaymentCard" )) {
                stUsersMoved = ((CoreObjectNode) lm).getFacet_Simple().getWhereAssigned();
                assertTrue( "Users count must match.", stUsers.size() == stUsersMoved.size() );
                for (TypeUser user : stUsersMoved) {
                    TLModelElement type = user.getAssignedTLObject();
                    assertTrue( "Must have assigned TL Type.", type != null );
                }
                LOGGER.debug( "Simple Facet " + lm + " where used count "
                    + ((TypeProviders) ((FacetOwners) lm).getFacet_Simple()).getWhereUsedCount() );
                aliases = lm.getAliases();
                if (aliases != null)
                    for (AliasNode alias : aliases)
                        LOGGER.debug( "Alias " + alias + " used as type " + alias.getWhereUsedCount() );
            }

            if (lm.getName().equals( troubleMakerName )) {
                LOGGER.debug( "Error case" );
                ml.check( (Node) lm, canBeValid );
            }

            ml.check( (Node) lm, canBeValid );
        }

        // Then - resulting model must be valid if there were no name collisions
        ml.check( Node.getModelNode(), canBeValid );

        // Then - source libraries must be empty
        List<LibraryMemberInterface> shouldBeEmpty = defaultProject.getDescendants_LibraryMembers();
        for (LibraryNode ln : defaultProject.getLibraries())
            assertTrue( "Source library " + ln + " must be empty.", ln.isEmpty() );
    }

}

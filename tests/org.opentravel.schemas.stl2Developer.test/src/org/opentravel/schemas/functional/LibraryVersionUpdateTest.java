
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

package org.opentravel.schemas.functional;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseRepositoryTest;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.whereused.LibraryProviderNode;
import org.opentravel.schemas.types.whereused.LibraryUsersToUpdateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LibraryVersionUpdateTest extends BaseRepositoryTest {
    static final Logger LOGGER = LoggerFactory.getLogger( LibraryVersionUpdateTest.class );

    private LibraryNode lib1 = null;
    private LibraryNode lib2 = null;
    private LibraryChainNode chain1 = null;
    private LibraryChainNode chain2 = null;
    private Node xsdString;
    private ProjectNode uploadProject = null;

    private BusinessObjectNode bo = null;
    private CoreObjectNode co = null;
    private VWA_Node vwa = null;
    private SimpleTypeNode simple = null;
    private EnumerationClosedNode cEnum = null;
    private EnumerationOpenNode oEnum = null;
    private ExtensionPointNode ep = null;
    private BusinessObjectNode sbo = null;
    private BusinessObjectNode nbo = null;
    private CoreObjectNode core2 = null;
    private LibraryNode minorLibrary = null;
    private LibraryNode patchLibrary = null;
    private CoreObjectNode mCo = null;
    private ExtensionPointNode ePatch = null;

    private LibraryNode providerLib;

    @Override
    public RepositoryNode getRepositoryForTest() {
        for (RepositoryNode rn : rc.getAll()) {
            if (rn.isRemote()) {
                return rn;
            }
        }
        throw new IllegalStateException( "Missing remote repository. Check your configuration." );
    }

    /**
     * Create 2 libraries in the repository, major version, locked for editing.
     * 
     * @throws LibrarySaveException
     * @throws RepositoryException
     */
    @Before
    public void runBeforeEachTest() throws LibrarySaveException, RepositoryException {
        LOGGER.debug( "Before test." );
        xsdString = NodeFinders.findNodeByName( "string", ModelNode.XSD_NAMESPACE );
        uploadProject = createProject( "ToUploadLibrary", getRepositoryForTest(), "Test" );

        lib1 = ml.createNewLibrary_Empty( getRepositoryForTest().getNamespace() + "/Test/NS1", "TestLibrary1",
            uploadProject );
        lib2 = ml.createNewLibrary_Empty( getRepositoryForTest().getNamespace() + "/Test/NS2", "TestLibrary2",
            uploadProject );

        chain1 = rc.manage( getRepositoryForTest(), Collections.singletonList( lib1 ) ).get( 0 );
        chain2 = rc.manage( getRepositoryForTest(), Collections.singletonList( lib2 ) ).get( 0 );
        boolean locked = rc.lock( chain1.getHead() );
        locked = rc.lock( chain2.getHead() );

        assertTrue( "Repository controller must not be null.", rc != null );
        assertTrue( "Repository must be available.", getRepositoryForTest() != null );
        assertTrue( "Project must not be null.", uploadProject != null );
        assertTrue( "MockLibrary must not be null.", ml != null );
        assertTrue( "xsdString must not be null.", xsdString != null );
        assertTrue( "lib1 must not be null.", lib1 != null );
        assertTrue( "lib1 must be editable.", lib1.isEditable() );
        assertTrue( "lib2 must not be null. ", lib2 != null );
        assertTrue( "lib2 must be editable.", lib2.isEditable() );
        assertTrue( "chain must be locked.", locked );
        assertTrue( "chain must be MANAGED_WIP.",
            RepositoryItemState.MANAGED_WIP == chain2.getHead().getProjectItem().getState() );
        assertTrue( "Must be major verison.", lib1.isMajorVersion() );
        assertTrue( "Must be major verison.", lib2.isMajorVersion() );
        assertTrue( "Must be empty.", lib1.isEmpty() );
        assertTrue( "Must be empty.", lib2.isEmpty() );

        // Use repository controller to version the libraries.
        // patchLibrary = rc.createPatchVersion(chain.getHead());
        // minorLibrary = rc.createMinorVersion(chain.getHead());
        // LibraryNode major2 = rc.createMajorVersion(chain.getHead());

        LOGGER.debug( "Before tests done." );
    }

    // Simply make sure the setup works
    @Test
    public void VF_versionFunctionTest() throws RepositoryException {
        // Given - initial state - 2 versioned and editable libraries

        // When - a simple type in library 2
        SimpleTypeNode simpleType = ml.addSimpleTypeToLibrary( lib2, "simpleType" );
        assertTrue( simpleType != null );
        assertTrue( simpleType.getLibrary() == lib2 );
        assertTrue( "Library must not be empty.", !lib2.isEmpty() );
        assert simpleType.getWhereAssigned().isEmpty();

        // When - add all types in library 1
        ml.addOneOfEach( lib1, "TypeUser" );

        testMajorFunctions( lib1, lib2, true );
    }

    /**
     * Non-destructive test for all major library functionality on objects in the two libraries. Does change all of the
     * objects.
     * 
     * @param doDelete if true, make test destructive and delete all members.
     */
    private void testMajorFunctions(LibraryNode lib1, LibraryNode lib2, boolean doDelete) {
        SimpleTypeNode simpleType = ml.addSimpleTypeToLibrary( lib2, "simpleType" );
        // Set the valid flag based on starting state
        boolean wasValid = lib1.isValid();
        if (wasValid)
            wasValid = lib2.isValid();

        // Assure libraries are major versions
        assert lib1.isMajorVersion();
        assert lib2.isMajorVersion();

        // When - assigned to simpleType to all type users
        for (TypeUser user : lib1.getDescendants_TypeUsers())
            if (user.canAssign( simpleType ))
                assert user.setAssignedType( simpleType ) != null;
        for (TypeUser user : lib2.getDescendants_TypeUsers())
            if (user.canAssign( simpleType ))
                assert user.setAssignedType( simpleType ) != null;

        // Given - a list of all library members
        final String suffix = "XXXFooYYYZZZ";
        List<LibraryMemberInterface> members = lib2.get_LibraryMembers();
        members.addAll( lib1.get_LibraryMembers() );

        for (LibraryMemberInterface lm : members) {
            // Verify member is OK
            ml.check( (Node) lm, wasValid );

            // When - renamed
            String oldName = lm.getName();
            lm.setName( oldName + suffix );
            assertTrue( "Name must change in major library.", lm.getName().equals( oldName + suffix ) );
            lm.setName( oldName ); // Restore name

            // When - moved
            LibraryNode oldLib = lm.getLibrary();
            if (oldLib == lib1)
                lib2.addMember( lm );
            else
                lib1.addMember( lm );
            assertTrue( !oldLib.contains( (Node) lm ) );
            oldLib.addMember( lm );
            assertTrue( oldLib.contains( (Node) lm ) );

            // Verify results is still OK
            ml.check( (Node) lm, wasValid );
        }
        ml.check( lib1, wasValid );
        ml.check( lib2, wasValid );

        if (doDelete) {
            // When - deleted
            for (LibraryMemberInterface lm : members)
                lm.delete();
            assertTrue( "Library must  be empty.", lib1.isEmpty() );
            assertTrue( "Library must  be empty.", lib2.isEmpty() );
        }
    }

    @Test
    public void VF_createVersionErrors() throws Exception {
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        LibraryNode ln_inChain = ml.createNewLibrary( "http://www.test.com/test1c", "test1c", defaultProject );
        lcn = new LibraryChainNode( ln_inChain );

        // These creates should create NULL libraries because ln is not in a repository.
        DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
        LOGGER.debug( "Error Dialogs Expected." );
        LibraryNode major = rc.createMajorVersion( ln );
        assertNull( major );
        LibraryNode minor = rc.createMinorVersion( ln );
        assertNull( minor );
        LibraryNode patch = rc.createPatchVersion( ln );
        assertNull( patch );
    }

    @Test
    public void VF_minorVersionTest() throws RepositoryException {
        // Given - two managed, locked and editable libraries.

        // Given - a simple type in the provider library to assign
        providerLib = lib2;
        SimpleTypeNode simpleType = ml.addSimpleTypeToLibrary( providerLib, "simpleType" );

        // Given - user library containing the objects that will get updated and assign them to the found type
        LibraryNode userLib = lib1;
        ml.addOneOfEach( userLib, "User" );
        for (TypeUser user : userLib.getDescendants_TypeUsers())
            if (user.getRequiredType() == null) {
                user.setAssignedType( simpleType );
                LOGGER.debug( "Assigned " + simpleType + " to: " + user );
            }

        verifyAssignments( userLib, simpleType );

        // When - provider lib is Versioned
        assertTrue( "Library must be promoted to FINAL.", makeFinal( providerLib ) );
        providerLib = rc.createMinorVersion( providerLib );

        // Then - providerLib is correct new version
        assertTrue( "Must have version of provider library.", providerLib != null );
        assertTrue( "Must be new library.", providerLib != lib2 );
        assertTrue( "Major versions must be head of chain.", providerLib == providerLib.getChain().getHead() );

        // Then - original provider lib (lib2) must still contain the simple type
        assertTrue( "Must NOT have type providers", providerLib.getDescendants_TypeProviders().isEmpty() );
        assertTrue( "Must have type providers", !lib2.getDescendants_TypeProviders().isEmpty() );

        // Then - type users still use the type from the old, base version of provider library (lib2)
        assertTrue( "Assigned simple type is NOT in major version.", simpleType.getLibrary() != providerLib );
        verifyAssignments( userLib, simpleType );
    }

    // Create two libraries where one uses types from the other then version the type provider
    @Test
    public void VF_updateVersionTest_AssignedTypes() throws RepositoryException {
        // Given - two managed, locked and editable libraries.

        // Given - a simple type in the provider library to assign
        providerLib = lib2;
        SimpleTypeNode simpleType = ml.addSimpleTypeToLibrary( providerLib, "simpleType" );

        // Given - user library containing the objects that will get updated and assign them to the found type
        LibraryNode userLib = lib1;
        ml.addOneOfEach( userLib, "User" );
        for (TypeUser user : userLib.getDescendants_TypeUsers())
            if (user.getRequiredType() == null) {
                user.setAssignedType( simpleType );
                LOGGER.debug( "Assigned " + simpleType + " to: " + user );
            }

        verifyAssignments( userLib, simpleType );

        // When - provider lib is Versioned to MAJOR
        assertTrue( "Library must be promoted to FINAL.", makeFinal( providerLib ) );
        providerLib = rc.createMajorVersion( providerLib );

        // Then - providerLib is correct new version
        assertTrue( "Must have major version of provider library.", providerLib != null );
        assertTrue( "Must be new library.", providerLib != lib2 );
        assertTrue( "Major versions must be head of chain.", providerLib == providerLib.getChain().getHead() );
        assertTrue( "Must have type providers", !providerLib.getDescendants_TypeProviders().isEmpty() );

        TypeProvider simpleTypeV2 = (TypeProvider) providerLib.findLibraryMemberByName( simpleType.getName() );
        assertTrue( "New version must have simple type with same name.", simpleTypeV2 != null );

        // Then - the original library must still be in model
        assertTrue( lib2 != null );
        ml.check( lib2 );
        assertTrue( lib2.getTLModelObject().getOwningModel() != null );

        // Then - type users still use the type from the old version
        assertTrue( "Assigned simple type is NOT in major version.", simpleType.getLibrary() != providerLib );
        verifyAssignments( userLib, simpleType );

        // Then - userLib whereUsed contains original provider lib (lib2)
        List<LibraryNode> usedLibs = userLib.getAssignedLibraries( false );
        assertTrue( "Must have simple type library in list.", usedLibs.contains( simpleType.getLibrary() ) );

        // Library level assigned type replacement Business Logic in the Version Update Handler.
        //

        // Given - a provider node for lib2 which would be display as child of "Uses" for user library
        // Must have LibraryProviderNode to do version update
        LibraryProviderNode thisLPN = userLib.getLibraryProviderNode( lib2 );
        assert thisLPN != null;

        // Given - the update helper used by VersionUpdateHandler to do version updates
        LibraryUsersToUpdateHelper helper = new LibraryUsersToUpdateHelper( thisLPN );
        assert !helper.isEmpty();

        // When - the helper replaces current type assignments with those from new major version
        helper.replace( providerLib );

        // Then - types must be updated
        verifyAssignments( userLib, simpleTypeV2 );
    }

    /**
     * Verify assignments of type to all type users in library
     * 
     * @param ln
     * @param type
     */
    private void verifyAssignments(LibraryNode ln, TypeProvider type) {
        for (TypeUser user : ln.getDescendants_TypeUsers())
            if (user.getRequiredType() == null)
                if (user.getAssignedType() != type)
                    LOGGER.debug( "AssignedType = " + user.getAssignedType() );
                else
                    assertTrue( "Type user must be assigned to simple type.", user.getAssignedType() == type );
    }

    // FIXME
    @Test
    public void updateVersionTest_ContextualFacets() throws RepositoryException {
        // Given two libraries, lib1 has cf_owners and lib2 has the facets
        BusinessObjectNode boType = ml.addBusinessObjectToLibrary( lib1, "boType" );
        ChoiceObjectNode choiceType = ml.addChoice( lib1, "choiceType" );
        // Remove all existing contextual facets
        List<AbstractContextualFacet> cfs = new ArrayList<>();
        cfs.addAll( boType.getContextualFacets( false ) );
        cfs.addAll( choiceType.getContextualFacets( false ) );
        for (AbstractContextualFacet cf : cfs)
            cf.delete();

        ContextualFacetNode choice1 = ml.addChoiceFacet( lib2, "Ch1", choiceType );
        ContextualFacetNode custom1 = ml.addCustomFacet( lib2, "Cu1", boType );
        ContextualFacetNode query1 = ml.addQueryFacet( lib2, "q1", boType );
        assertTrue( !lib2.getDescendants_ContextualFacets().isEmpty() );

        checkFacetLibraries( lib2, boType, choiceType );
        ml.check();

        //
        // When - create major version of library containing the facets
        //
        assertTrue( "Library must be promoted to FINAL.", makeFinal( lib2 ) );
        LibraryNode vLib2 = rc.createMajorVersion( lib2 );
        ml.check( Node.getModelNode(), true );
        // 8/27/2018 - made valid by renaming new facets
        // Is invalid due to new facets being injected with same name

        // Then - original library must contain contextual facets
        assertTrue( !lib2.getDescendants_ContextualFacets().isEmpty() );
        assertTrue( lib2.contains( query1 ) );

        // Then - versioned library must contain contextual facets
        assertTrue( !vLib2.getDescendants_ContextualFacets().isEmpty() );
        assertTrue( !vLib2.contains( query1 ) );

        // FIXME - design question: should v2 contextual facets have owner? They do now.
        // Then - version-ed library's contextual facets have no owner
        for (ContextualFacetNode cf : vLib2.getDescendants_ContextualFacets())
            assertTrue( cf.getOwningComponent() != null );
        // // Then - objects must contain the original facets
        // checkFacetLibraries(lib2, boType, choiceType);

        ml.check( Node.getModelNode(), true );
    }

    private void checkFacetLibraries(LibraryNode ln, ContextualFacetOwnerInterface... owners) {
        for (ContextualFacetOwnerInterface owner : owners)
            for (AbstractContextualFacet cf : owner.getContextualFacets( false ))
                assertTrue( cf.getLibrary() == ln );
    }

    // FIXME
    @Test
    public void updateVersionTest_Resources() throws RepositoryException {
        assert false;
    }

    public void checkExtensions(LibraryNode baseLib, ExtensionOwner... owners) {
        for (ExtensionOwner owner : owners) {
            assertTrue( "Must extend object from passed library.", owner.getExtensionBase().getLibrary() == baseLib );
        }
    }

    // FIXME
    @Test
    public void updateVersionTest_BaseTypes() throws RepositoryException {

        // Create two libraries where one extends types from the other then version the type provider

        // Create Extension Owners in the provider library
        LibraryNode baseLib = lib2;
        BusinessObjectNode boType = ml.addBusinessObjectToLibrary( baseLib, "boType" );
        ChoiceObjectNode choiceType = ml.addChoice( baseLib, "choiceType" );
        CoreObjectNode coreType = ml.addCoreObjectToLibrary( baseLib, "coreType" );
        EnumerationClosedNode ecType = ml.addClosedEnumToLibrary( baseLib, "ecType" );
        EnumerationOpenNode eoType = ml.addOpenEnumToLibrary( baseLib, "eoType" );
        VWA_Node vwaType = ml.addVWA_ToLibrary( baseLib, "vwaType" );
        ml.check();

        // Given - library 1 with one of each object extending objects in library 2
        // Create invalid BO - no ID or else ID facet will be invalid when extends base
        BusinessObjectNode boExtension = ml.addBusinessObjectToLibrary( lib1, "boExtension", false );
        ChoiceObjectNode choiceExtension = ml.addChoice( lib1, "choiceExtension" );
        CoreObjectNode coreExtension = ml.addCoreObjectToLibraryNoID( lib1, "coreExtension" );
        EnumerationClosedNode ecExtension = ml.addClosedEnumToLibrary( lib1, "ecExtension" );
        EnumerationOpenNode eoExtension = ml.addOpenEnumToLibrary( lib1, "eoExtension" );
        VWA_Node vwaExtension = ml.addVWA_ToLibrary( lib1, "vwaExtension" );
        ml.check( Node.getModelNode(), false );

        // Given - all extensions set
        boExtension.setExtension( boType );
        choiceExtension.setExtension( choiceType );
        coreExtension.setExtension( coreType );
        ecExtension.setExtension( ecType );
        eoExtension.setExtension( eoType );
        vwaExtension.setExtension( vwaType );
        assertTrue( "BoExtension must extend boType.", boExtension.getExtensionBase() == boType );
        checkExtensions( baseLib, boExtension, choiceExtension, coreExtension, ecExtension, eoExtension, vwaExtension );
        ml.check();

        // Then - baseLib must list lib1 as where used
        assertTrue( "Lib1 must not use other libraries.", lib1.getWhereUsedHandler().getWhereUsed().isEmpty() );
        assertTrue( "baseLib must be used by other libraries.",
            !baseLib.getWhereUsedHandler().getWhereUsed().isEmpty() );
        // Then - Lib1 must list baseLib as an assigned library
        assertTrue( "Lib1 must have at least one assigned library.", !lib1.getAssignedLibraries( false ).isEmpty() );
        assertTrue( "baseLib must NOT have an assigned library.", baseLib.getAssignedLibraries( false ).isEmpty() );

        // Given - both libraries are valid.
        ml.check( baseLib );
        ml.check( lib1 );

        //
        // When - create major version of library baseLib containing the base types
        //
        assertTrue( "Library must be promoted to FINAL.", makeFinal( baseLib ) );
        LibraryNode versionedbaseLib = rc.createMajorVersion( baseLib );

        // Then - check new version of the base library
        assertTrue( "Must have major version of library 2.", versionedbaseLib != null );
        assertTrue( "Must have type providers", !versionedbaseLib.getDescendants_TypeProviders().isEmpty() );
        assertTrue( "Must be new library.", versionedbaseLib != baseLib );
        assertTrue( "Major versions must be head of chain.",
            versionedbaseLib == versionedbaseLib.getChain().getHead() );

        // Then - Assert that extension base remained in original version of the library
        assertTrue( "BoExtension must still extend boType.", boExtension.getExtensionBase() == boType );
        checkExtensions( baseLib, boExtension, choiceExtension, coreExtension, ecExtension, eoExtension, vwaExtension );

        //
        // TEST - type assignments replaced from old to new base library.
        //
        List<ExtensionOwner> superTypes = new ArrayList<>();
        superTypes.add( boExtension );
        superTypes.add( choiceExtension );
        superTypes.add( coreExtension );
        superTypes.add( ecExtension );
        superTypes.add( eoExtension );
        superTypes.add( vwaExtension );
        versionedbaseLib.replaceAllExtensions( superTypes );

        // Then
        checkExtensions( versionedbaseLib, boExtension, choiceExtension, coreExtension, ecExtension, eoExtension,
            vwaExtension );
        for (ExtensionOwner owner : lib1.getDescendants_ExtensionOwners()) {
            Node base = owner.getExtensionBase();
            if (!(owner.getExtensionBase() instanceof ImpliedNode)) {
                if (owner.getExtensionBase() == null || owner.getExtensionBase().getLibrary() == null)
                    LOGGER.debug( "Error - " + ((Node) owner).getNameWithPrefix() + " does not have extension base. " );
                if (owner.getExtensionBase().getLibrary() != versionedbaseLib)
                    LOGGER.debug( "Error - " + owner + " assigned type is in wrong library: "
                        + owner.getExtensionBase().getNameWithPrefix() );
                assertTrue( "Extension Owner must be in providerLib version 2.",
                    owner.getExtensionBase().getLibrary() == versionedbaseLib );
            }
        }
    }

    //
    // TODO - find where LibraryUsersToUpdateHelper is tested or create junit for it.
    //
    // // Given - a provider node for baseLib which would be display as child of "Uses" for user library
    // // Must have LibraryProviderNode to do version update
    // Collection<LibraryNode> whereUsed = baseLib.getWhereUsedHandler().getWhereUsed();
    //
    // assertTrue("baseLib must be used by other libraries.",
    // !baseLib.getWhereUsedHandler().getWhereUsed().isEmpty());
    // LibraryProviderNode thisLPN = baseLib.getLibraryProviderNode(lib1);
    // assert thisLPN != null;
    //
    // // Given - the update helper used by VersionUpdateHandler to do version updates
    // LibraryUsersToUpdateHelper helper = new LibraryUsersToUpdateHelper(thisLPN);
    // assert !helper.isEmpty();
    //
    // // When - the helper replaces current type assignments with those from new major version
    // helper.replace(providerLib);

}

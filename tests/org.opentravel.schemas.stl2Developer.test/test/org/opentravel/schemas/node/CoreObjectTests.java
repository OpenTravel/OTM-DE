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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.CoreSimpleFacetNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class CoreObjectTests extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger( CoreObjectTests.class );

    LibraryChainNode lcn = null;
    LibraryNode ln = null;

    @Before
    public void beforeEachCoreTest() {
        LOGGER.debug( "***Before Core Object Tests ----------------------" );
        ln = ml.createNewLibrary( "http://test.com", "CoreTest", defaultProject );
        ln.setEditable( true );
    }

    /**
     * constructor tests
     * 
     * @throws Exception
     */
    @Test
    public void CO_ConstructorTests() throws Exception {
        if (ln == null)
            beforeEachTest();

        // Given - tl core object
        TLCoreObject tlc = buildTLCoreObject( "TestCore1" );
        // When - constructed
        CoreObjectNode core1 = new CoreObjectNode( tlc );
        // Then - pass check tests
        ln.addMember( core1 );
        check( core1, false );
        if (!core1.isValid())
            ml.printValidationFindings( core1 );
        assertTrue( core1.isValid() ); // you cant build bo unless valid

        // Given - business object
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TestBO2" );
        CoreObjectNode core2 = new CoreObjectNode( bo );
        ln.removeMember( bo );
        check( core2 );

        // Given - vwa
        VWA_Node vwa = ml.addVWA_ToLibrary( ln, "TestVWA" );
        CoreObjectNode core3 = new CoreObjectNode( vwa );
        ln.removeMember( vwa );
        check( core3 );

        // check mock library
        CoreObjectNode core4 = ml.addCoreObjectToLibrary( ln, "Tc" );
        check( core4 );
    }

    /**
     * factory tests
     */
    @Test
    public void CO_FactoryTests() {
        CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newLibraryMember( buildTLCoreObject( "test2" ) );
        ln.addMember( newNode );
        check( newNode );
    }

    /**
     * Simple Facet tests
     */
    @Test
    public void CO_SimpleFacetTests() {
        // When - a core is created
        CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newLibraryMember( buildTLCoreObject( "test2" ) );
        ln.addMember( newNode );
        check( newNode );

        // Then - simple facet
        CoreSimpleFacetNode sf = newNode.getFacet_Simple();
        assert sf != null;
        TypeProvider st = sf.getAssignedType();
        assert st != null;

        // When - simple assigned to an attribute
        CoreObjectNode coreWithAttr =
            (CoreObjectNode) NodeFactory.newLibraryMember( buildTLCoreObject( "CoreWithAttr" ) );
        AttributeNode att1 = new AttributeNode( coreWithAttr.getFacet_Summary(), "att1", sf );
        AttributeNode att2 = new AttributeNode( coreWithAttr.getFacet_Summary(), "att2", newNode );

        // Then - simple facet is assigned
        TypeProvider at = att1.getAssignedType();
        assertTrue( "Simple facet must be assigned to att1.", att1.getAssignedType() == sf );
        assertTrue( "Simple facet must be assigned to att1.", sf.getWhereAssigned().contains( att1 ) );
        assertTrue( "Core must be assigned to att2.", att2.getAssignedType() == newNode );
        assertTrue( "Core must be assigned to att2.", newNode.getWhereAssigned().contains( att2 ) );

        // When - provider is moved to a new library
        LibraryNode ln2 = ml.createNewLibrary_Empty( defaultProject.getNamespace(), "Dest2", defaultProject );
        ln2.addMember( newNode );
        assertTrue( "Simple facet must be assigned to att1.", att1.getAssignedType() == sf );
        assertTrue( "Simple facet must be assigned to att1.", sf.getWhereAssigned().contains( att1 ) );
        assertTrue( "Core must be assigned to att2.", att2.getAssignedType() == newNode );
        assertTrue( "Core must be assigned to att2.", newNode.getWhereAssigned().contains( att2 ) );

        // When - user is moved to new library
        ln2.addMember( coreWithAttr );
        assertTrue( "Simple facet must be assigned to att1.", att1.getAssignedType() == sf );
        assertTrue( "Simple facet must be assigned to att1.", sf.getWhereAssigned().contains( att1 ) );
        assertTrue( "Core must be assigned to att2.", att2.getAssignedType() == newNode );
        assertTrue( "Core must be assigned to att2.", newNode.getWhereAssigned().contains( att2 ) );

        // When - attribute is assigned a different type
        att1.setAssignedType( ml.getXsdDate() );
        assertTrue( "Simple facet must NOT be assigned to att1.", att1.getAssignedType() != sf );
        assertTrue( "Simple facet must NOT be assigned to att1.", !sf.getWhereAssigned().contains( att1 ) );

        ml.check();
    }

    @Test
    public void CO_AsTypeTests() {
        // When - a core is created
        CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newLibraryMember( buildTLCoreObject( "test2" ) );
        ln.addMember( newNode );
        check( newNode );
        FacetProviderNode fs = newNode.getFacet_Summary();
        // When - a core is created
        CoreObjectNode coreWithAttr =
            (CoreObjectNode) NodeFactory.newLibraryMember( buildTLCoreObject( "CoreWithAttr" ) );

        // When - assigned to an attribute
        AttributeNode att1 = new AttributeNode( coreWithAttr.getFacet_Summary(), "att1", newNode );
        // When - summary assigned to an element
        ElementNode ele2 = new ElementNode( coreWithAttr.getFacet_Summary(), "ele2", fs );

        // Then - is assigned
        TypeProvider at = att1.getAssignedType();
        assertTrue( "Core must be assigned to att1.", att1.getAssignedType() == newNode );
        assertTrue( "Core must be assigned to att1.", newNode.getWhereAssigned().contains( att1 ) );
        assertTrue( "Core summary must be assigned to ele2.", ele2.getAssignedType() == fs );
        assertTrue( "Core summary must be assigned to ele2.", fs.getWhereAssigned().contains( ele2 ) );

        // When - provider is moved to a new library
        LibraryNode ln2 = ml.createNewLibrary_Empty( defaultProject.getNamespace(), "Dest2", defaultProject );
        ln2.addMember( newNode );
        assertTrue( "Core must be assigned to att1.", att1.getAssignedType() == newNode );
        assertTrue( "Core must be assigned to att1.", newNode.getWhereAssigned().contains( att1 ) );
        assertTrue( "Core summary must be assigned to ele2.", ele2.getAssignedType() == newNode.getFacet_Summary() );
        assertTrue( "Core summary must be assigned to ele2.",
            newNode.getFacet_Summary().getWhereAssigned().contains( ele2 ) );

        // When - user is moved to new library
        ln2.addMember( coreWithAttr );
        assertTrue( "Core must be assigned to att1.", att1.getAssignedType() == newNode );
        assertTrue( "Core must be assigned to att1.", newNode.getWhereAssigned().contains( att1 ) );
        assertTrue( "Core summary must be assigned to ele2.", ele2.getAssignedType() == newNode.getFacet_Summary() );
        assertTrue( "Core summary must be assigned to ele2.",
            newNode.getFacet_Summary().getWhereAssigned().contains( ele2 ) );

        // When - attribute is assigned a different type
        att1.setAssignedType( ml.getXsdDate() );
        assertTrue( "Core must NOT be assigned to att1.", att1.getAssignedType() != newNode );
        assertTrue( "Core must NOT be assigned to att1.", !newNode.getWhereAssigned().contains( att1 ) );

        ml.check();
    }

    /**
     * Role Facet tests
     */
    @Test
    public void CO_RoleTests() {
        // When - a core is created with no role values
        CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newLibraryMember( buildTLCoreObject( "test2" ) );
        assertTrue( "Role facet must not be assigned as type by factory.",
            newNode.getFacet_Role().getWhereAssigned().isEmpty() );
        ln.addMember( newNode );
        check( newNode );

        RoleFacetNode roleFacet = newNode.getFacet_Role();
        assert roleFacet instanceof RoleFacetNode;

        // Then
        assertTrue( "Role facet must have TLRoleEnumeration.",
            roleFacet.getTLModelObject() instanceof TLRoleEnumeration );
        TLRoleEnumeration tlRF = roleFacet.getTLModelObject();
        assertTrue( "TL Roles must be empty.", tlRF.getRoles().isEmpty() );
        List<Node> roles = roleFacet.getChildren();
        assertTrue( "Node factory must not create roles.", roles.isEmpty() );
        assertTrue( "Role facet must not be assigned as type by factory.", roleFacet.getWhereAssigned().isEmpty() );

        // When - a core is created with role values
        TLCoreObject tlCore = buildTLCoreObject( "TestWithRoles" );
        TLRole tlRoleA = new TLRole();
        tlRoleA.setName( "A" );
        tlCore.getRoleEnumeration().addRole( tlRoleA );
        TLRole tlRoleB = new TLRole();
        tlRoleB.setName( "B" );
        tlCore.getRoleEnumeration().addRole( tlRoleB );
        CoreObjectNode coreWithRoles = (CoreObjectNode) NodeFactory.newLibraryMember( tlCore );
        ln.addMember( coreWithRoles );
        check( coreWithRoles );

        // Then
        roleFacet = coreWithRoles.getFacet_Role();
        assert roleFacet instanceof RoleFacetNode;
        tlRF = roleFacet.getTLModelObject();
        assertTrue( "TL Roles must NOT be empty.", !tlRF.getRoles().isEmpty() );
        roles = roleFacet.getChildren();
        assertTrue( "must have roles.", !roles.isEmpty() );

        // When - role facet assigned as type
        ElementNode ele1 = new ElementNode( newNode.getFacet_Summary(), "n", roleFacet );
        // Then
        assertTrue( "Role facet must be assigned as type.", roleFacet.getWhereAssigned().contains( ele1 ) );
        assertTrue( "Role facet must  assigned as type .", ele1.getAssignedType() == roleFacet );

        // When - provider node is moved to another library, it is still assigned as type
        LibraryNode ln2 = ml.createNewLibrary_Empty( defaultProject.getNamespace(), "Dest", defaultProject );
        ln2.addMember( coreWithRoles );
        assertTrue( "Role facet must be assigned as type.", roleFacet.getWhereAssigned().contains( ele1 ) );
        assertTrue( "Role facet must  assigned as type .", ele1.getAssignedType() == roleFacet );

        // When - user node is moved to new library it still has assigned type
        ln2.addMember( newNode );
        assertTrue( "Role facet must be assigned as type.", roleFacet.getWhereAssigned().contains( ele1 ) );
        assertTrue( "Role facet must  assigned as type .", ele1.getAssignedType() == roleFacet );

        LOGGER.debug( "CHECK" );
    }

    /**
     * load from library tests
     * 
     * @throws Exception
     */
    @Test
    public void CO_FileLoadTests() throws Exception {
        lf.loadTestGroupA( mc );
        mc.getModelNode();
        for (LibraryNode lib : Node.getAllUserLibraries())
            for (LibraryMemberInterface n : lib.getDescendants_LibraryMembers())
                if (n instanceof CoreObjectNode)
                    check( (CoreObjectNode) n, false );
    }

    @Test
    public void CO_FileLoadTests2() throws Exception {
        List<LibraryNode> preLibs = Node.getAllUserLibraries();
        if (preLibs.size() > 1)
            LOGGER.debug( "Warning - libraries loaded before test starts." );

        lf.loadTestGroupAc( mc );
        mc.getModelNode();
        for (LibraryNode lib : Node.getAllUserLibraries())
            for (LibraryMemberInterface n : lib.getDescendants_LibraryMembers())
                if (n instanceof CoreObjectNode)
                    check( (CoreObjectNode) n, !lib.getName().equals( "Test5" ) ); // Test5 is not valid
        // FIXME - Passes when run alone.
        // When run alone, 3 libs before loading test group then after
        // there are 9 libraries, including testFile5.otm
        // Fails validation when run with other tests.
        // When run with other tests, there are 9 libraries, including testFile5.otm
    }

    /**
     * assigned type tests
     */
    @Test
    public void CO_TypeAssignmentTests() {
        // Given - a core object
        CoreObjectNode core = ml.addCoreObjectToLibrary_Empty( ln, "CoreTest" );
        TypeProvider cType = core.getAssignedType();
        assertTrue( cType == ModelNode.getEmptyNode() );
        TypeProvider dType = ml.getXsdDate();
        TypeProvider sType = ml.getXsdString();

        // When - initial assignment
        TypeProvider result = core.setAssignedType( dType );
        assert result != null;
        cType = core.getAssignedType();
        assertTrue( cType == dType ); // assignment worked

        // Then
        assertTrue( "Type must be same from Core and simple attribute methods.",
            core.getSimpleAttribute().getAssignedType() == core.getAssignedType() );

        // When - set with simple attribute
        assertTrue( "Assigning type must return true. ", core.getSimpleAttribute().setAssignedType( sType ) != null );
        assertTrue( "Type must be as assigned.", core.getAssignedType() == sType );
        // When - set with core method
        Assert.assertTrue( core.setAssignedType( dType ) != null );
        Assert.assertTrue( "Type must be as assigned.", core.getAssignedType() == dType );
    }

    @Test
    public void CO_ExtensionTests() {
        ProjectNode proj = mc.getProjectController().getDefaultProject();
        assertNotNull( "Null project", proj );
        ln = lf.loadFile4( mc );
        lcn = new LibraryChainNode( ln ); // Test in managed library
        ln.setEditable( true );

        LibraryNode ln2 = ml.createNewLibrary( "http://test.com", "tl2", proj );
        LibraryChainNode lcn2 = new LibraryChainNode( ln2 );
        ln2.setEditable( true );

        CoreObjectNode extendedCO = ml.addCoreObjectToLibrary( ln2, "ExtendedCO" );
        assertNotNull( "Null object created.", extendedCO );
        // Access before assigning base to insure updated when assigned a base type
        List<Node> iKids = ((Node) extendedCO.getFacet_Default()).getInheritedChildren();
        assertTrue( iKids.isEmpty() );

        for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
            if (n instanceof CoreObjectNode && n != extendedCO) {
                extendedCO.setExtension( (Node) n );
                check( (CoreObjectNode) n );
                check( extendedCO );
                iKids = ((Node) extendedCO.getFacet_Default()).getInheritedChildren();
                if (!((Node) n).getFacet_Default().getChildren().isEmpty())
                    assertTrue( !iKids.isEmpty() );
            }

    }

    @Test
    public void CO_ChangeToTests() {
        // The change to method adds swap() to the constructor generated core.
        //
        CoreObjectNode tco = null;
        // Given - a chain with BO and VWA
        lcn = new LibraryChainNode( ln ); // Test in a chain
        ln.setEditable( true );
        // v1.6 - will have BO and custom and query facets
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "bo" );
        VWA_Node vwa = ml.addVWA_ToLibrary( ln, "vwa" );
        // Given - an element to assign types to
        BusinessObjectNode typeUser = ml.addBusinessObjectToLibrary( ln, "userBO" );
        ElementNode ele = new ElementNode( typeUser.getFacet_Summary(), "EleUser" );
        // Given - the number of library members in library (must not change)
        int typeCount = ln.getDescendants_LibraryMembers().size();
        // List<LibraryMemberInterface> originalMembers = ln.getDescendants_LibraryMembers();
        // Given - an element assigned the bo as a type
        ele.setAssignedType( bo );

        // When - changed to core
        tco = new CoreObjectNode( bo );
        bo.replaceWith( tco );
        typeCount = typeCount - 2; // two contextual facets
        // Then - the core is valid and element is assigned the core
        check( tco );
        assertTrue( bo.getLibrary() != ln );
        assertTrue( "Type assignment must be to the new core.", ele.getAssignedType() == tco );
        assertTrue( "New core must have element in where used list.", tco.getWhereAssigned().contains( ele ) );
        assertTrue( "Library must contain new core.", ln.contains( tco ) );
        assertEquals( "Count must match", typeCount, ln.getDescendants_LibraryMembers().size() );

        // Repeat with VWA
        ele.setAssignedType( vwa );
        tco = new CoreObjectNode( vwa );
        vwa.replaceWith( tco );
        check( tco );
        assertTrue( vwa.getLibrary() != ln );
        assertTrue( "Type assignment must be to the new core.", ele.getAssignedType() == tco );
        assertTrue( "Library must contain new core.", ln.contains( tco ) );

        // List<LibraryMemberInterface> members = ln.getDescendants_LibraryMembers();
        assertEquals( "Count must match", typeCount, ln.getDescendants_LibraryMembers().size() );
    }

    @Test
    public void CO_AssignmentTests() {
        // Assign all assignable parts of the core and verify assignments

        // Given - a business object with elements and attributes to assign to
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TestBO" );
        ElementNode ele = new ElementNode( bo.getFacet_Summary(), "ele1" );
        AttributeNode att = new AttributeNode( bo.getFacet_Summary(), "attr1" );

        // Given a core object
        CoreObjectNode core = ml.addCoreObjectToLibrary( ln, "TestCore" );

        // When assigned to attribute
        testAssignment( core, att );
        testAssignment( core, ele );

        // When type provider descendants are assigned
        for (TypeProvider tp : core.getDescendants_TypeProviders()) {
            testAssignment( tp, att );
            testAssignment( tp, ele );
        }

        // Given - an alias on core
        // AliasNode alias = new AliasNode(core, "CoreAlias");
        // Attributes change the alias into the parent object on assignment
        // testAssignment(alias, att);
    }

    @Test
    public void CO_MoveTests() {

        // Given a 2nd library to move the core to
        LibraryNode destLib = ml.createNewLibrary( pc, "DestLib" );

        // Given a core object with an alias
        CoreObjectNode core = ml.addCoreObjectToLibrary( ln, "TestCore" );
        AliasNode alias = new AliasNode( core, "TestCoreAlias" );

        // Given - a business object with elements and attributes to assign to
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TestBO" );

        // Given - core assigned to element and attribute
        ElementNode ele1 = new ElementNode( bo.getFacet_Summary(), "ele1" );
        AttributeNode att1 = new AttributeNode( bo.getFacet_Summary(), "attr1" );
        testAssignment( core, att1 );
        testAssignment( core, ele1 );

        // Given - alias assigned to element and attribute
        ElementNode ele2 = new ElementNode( bo.getFacet_Summary(), "ele2" );
        AttributeNode att2 = new AttributeNode( bo.getFacet_Summary(), "attr2" );
        testAssignment( alias, ele2 );
        testAssignment( alias, att2 ); // assigns core not alias

        // Given - core simple facet assigned to element and attribute
        ElementNode ele3 = new ElementNode( bo.getFacet_Summary(), "ele3" );
        AttributeNode att3 = new AttributeNode( bo.getFacet_Summary(), "attr3" );
        testAssignment( core.getFacet_Simple(), att3 );
        testAssignment( core.getFacet_Simple(), ele3 );

        // Given - core detail facet assigned to element (Not attribute)
        ElementNode ele4 = new ElementNode( bo.getFacet_Summary(), "ele4" );
        testAssignment( core.getFacet_Detail(), ele4 );

        // Given - core roles
        // TODO - skipped for now since GUI does not allow assignments

        // Given - library is valid
        ml.check();

        // When - core is moved
        LOGGER.debug( "When: Moving " + core + " to " + destLib );
        destLib.addMember( core );

        // Then - model is still valid
        ml.check();

    }

    /**
     * Set the type user to the type provider and verify results.
     * 
     * @param tp
     * @param u
     */
    private void testAssignment(TypeProvider tp, TypeUser u) {
        String name = u.getName();

        // Do initial assignment and record if an substitution occurred
        tp = u.setAssignedType( tp );

        LOGGER.debug( "Test assignment of " + tp + " to " + u + " result = " + tp );
        if (tp != null) {
            // Verify assignment
            TypeProvider nu = u.getAssignedType();
            Collection<TypeUser> nw = tp.getWhereAssigned();
            assertTrue( "Provider must be assigned type.", u.getAssignedType() == tp );
            assertTrue( "Provider must have attribute in where used.", tp.getWhereAssigned().contains( u ) );
            // verify removed when assigned to something else (xsd:int)
            u.setAssignedType( ml.getXsdInt() );
            assertTrue( "Provider must NOT be assigned type.", u.getAssignedType() != tp );
            assertTrue( "Provider must NOT have attribute in where used.", !tp.getWhereAssigned().contains( u ) );
            u.setName( name );
            // Leave the type assigned
            u.setAssignedType( tp );
            assertTrue( "Provider must be assigned type.", u.getAssignedType() == tp );
            assertTrue( "Provider must have attribute in where used.", tp.getWhereAssigned().contains( u ) );
        }
    }

    @Test
    public void CO_NameChangeTests() {
        // On name change, all users of the BO and its aliases and facets also need to change.

        // Given - a Core Object with alias
        final String coreName = "initialcoreName";
        CoreObjectNode core = ml.addCoreObjectToLibrary_Empty( ln, coreName );
        AliasNode alias1 = core.addAlias( "coreAlias" );
        AliasNode aliasSummary = null;
        for (Node n : core.getFacet_Summary().getChildren())
            if (n instanceof AliasNode)
                aliasSummary = (AliasNode) n;
        // Then - the alias must exist on the core and it's facet
        assertNotNull( alias1 );
        assertNotNull( aliasSummary );

        // When - a core is created that has elements that use the core and aliases as properties
        CoreObjectNode elements = ml.addCoreObjectToLibrary( ln, "user" );
        FacetProviderNode eleOwner = elements.getFacet_Summary();

        // When - assigned core as type
        ElementNode e1 = new ElementNode( eleOwner, "p1", core );
        assertTrue( "Element name must be the core name.", e1.getName().equals( core.getName() ) );
        assertTrue( "Core must be assigned as type.", core.getWhereAssigned().contains( e1 ) );

        // When - assigned alias as type
        e1 = new ElementNode( eleOwner, "p2", alias1 );
        assertTrue( "Element name must be alias name.", e1.getName().equals( alias1.getName() ) );
        assertTrue( "Facet alias must be assigned as type.", alias1.getWhereAssigned().contains( e1 ) );

        // When - assigned summary facet as type
        e1 = new ElementNode( eleOwner, "p3", core.getFacet_Summary() );
        assertTrue( "Element name must be facet name.", e1.getName().equals( core.getFacet_Summary().getName() ) );
        assertTrue( "Element name must start with core name.", e1.getName().startsWith( core.getName() ) );
        assertTrue( "Summary Facet must be assigned as type.",
            core.getFacet_Summary().getWhereAssigned().contains( e1 ) );

        // When - assigned alias from summary facet
        e1 = new ElementNode( eleOwner, "p4", aliasSummary );
        assertTrue( "Element name must start with alias name.", e1.getName().startsWith( alias1.getName() ) );
        assertTrue( "Summary Facet alias must be assigned as type.", aliasSummary.getWhereAssigned().contains( e1 ) );

        // When - Change the core name
        String changedName = "changedName";
        core.setName( changedName );
        changedName = NodeNameUtils.fixCoreObjectName( changedName ); // get the "fixed" name
        assertTrue( changedName.equals( core.getName() ) );

        // Then - the elements and facets name must change.
        for (Node n : eleOwner.getChildren()) {
            TypeUser tn = (TypeUser) n;
            if (tn.getAssignedType() == core)
                assertTrue( tn.getName().equals( changedName ) );
            else if (tn.getAssignedType() == alias1)
                assertTrue( tn.getName().equals( alias1.getName() ) );
            else if (tn.getAssignedType() == core.getFacet_Summary())
                assertTrue( tn.getName().equals( changedName ) );
            else if (tn.getAssignedType() == aliasSummary)
                assertTrue( tn.getName().startsWith( alias1.getName() ) );
            else
                assert true; // no-op - created by Mock Library to make core valid
        }
        // NOTE - not valid!

        // When - alias name changed
        String aliasName2 = "aliasName2";
        alias1.setName( aliasName2 );
        aliasName2 = alias1.getName(); // get the "fixed" name
        // Then - all aliases on core must change name
        assertTrue( "Alias Name must change.", alias1.getName().equals( aliasName2 ) );
        assertTrue( "Alias on summary facet must change.", aliasSummary.getName().startsWith( aliasName2 ) );

        // Then - must find an element with the alias name
        int found = 0;
        for (Node n : eleOwner.getChildren()) {
            TypeUser tn = (TypeUser) n;
            if (tn.getAssignedType() == core)
                assertTrue( tn.getName().equals( changedName ) );
            else if (tn.getAssignedType() == alias1)
                assertTrue( tn.getName().equals( aliasName2 ) );
            else if (tn.getAssignedType() == core.getFacet_Summary())
                assertTrue( tn.getName().equals( changedName ) );
            // else if (tn.getAssignedType() == aliasSummary)
            // assertTrue("Error reported on 3/29/2018", tn.getName().startsWith(aliasName2)); // report to steve on
            // 3/29/2018
            else
                assert true; // no-op - created by Mock Library to make core valid
        }
    }

    /**
     * Build a TL core object with a simple type set and attribute and indicator in summary facet.
     * 
     * @param name
     * @return
     */
    public TLCoreObject buildTLCoreObject(String name) {
        if (name == null || name.isEmpty())
            name = "TestCore";
        TypeProvider type = ml.getXsdString();
        NamedEntity tlType = (NamedEntity) type.getTLModelObject();

        // Create tl core, set name and type
        TLCoreObject tlc = new TLCoreObject();
        tlc.setName( name );
        tlc.getSimpleFacet().setSimpleType( tlType );

        // Add attribute and indicator properties
        TLAttribute tla = new TLAttribute();
        tla.setName( name + "Attr1" );
        tla.setType( (TLPropertyType) tlType );
        TLIndicator tli = new TLIndicator();
        tli.setName( name + "Ind1" );
        tlc.getSummaryFacet().addAttribute( tla );
        tlc.getSummaryFacet().addIndicator( tli );
        return tlc;
    }

    /**
     * checkCore - all tests to be used in these tests and by other junits
     */
    public void check(CoreObjectNode core) {
        check( core, true );
    }

    public void check(CoreObjectNode core, boolean validate) {
        assertTrue( core.getLibrary() != null );

        // Core must only have 6 children + aliases
        List<Node> kids = core.getChildren();
        int cSize = 6 + core.getAliases().size();
        if (kids.size() != cSize)
            LOGGER.debug( "Error in core children count." );
        assertTrue( "Core children count must be " + cSize, core.getChildren().size() == cSize );

        // Facets
        for (Node child : core.getChildren()) {
            ml.check( child, validate );
        }
        assertTrue( core.getFacet_Simple() != null );
        assertTrue( core.getFacet_Default() != null );
        assertTrue( core.getFacet_Summary() != null );
        assertTrue( core.getFacet_Detail() != null );
        assertTrue( core.getFacet_Role() != null );
        assertTrue( core.getSimpleAttribute() != null );
        assertTrue( core.getType() == null );

        // Done in MockLibrary with printout of errors
        // if (validate)
        // assertTrue(core.isValid());

    }
}

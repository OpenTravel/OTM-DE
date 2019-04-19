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

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.CustomFacetNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class GetDescendents_Tests extends BaseProjectTest {
    static final Logger LOGGER = LoggerFactory.getLogger( MockLibrary.class );

    MockLibrary ml = new MockLibrary();
    LibraryNode ln = null;
    String OTA = "OTA2_BuiltIns_v2.0.0"; // name
    String XSD = "XMLSchema";
    static String PREFIX = "PL1";

    @Test
    public void DESC_Descendants() {
        ProjectNode project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        int emptyModelCount = Node.getModelNode().getDescendants().size();

        ln = ml.createNewLibrary( project.getNamespace(), "test", project );
        ml.addOneOfEach( ln.getHead(), PREFIX );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TBB" );
        ml.addAllProperties( bo.getFacet_Summary() );
        int lib1Count = ln.getDescendants().size();

        LibraryNode ln2 = ml.createNewManagedLibrary( project.getNamespace(), "test2", project ).getHead();
        ml.addOneOfEach( ln2.getHead(), PREFIX + "2" );
        BusinessObjectNode bo2 = ml.addBusinessObjectToLibrary( ln2, "TBB2" );
        ml.addAllProperties( bo2.getFacet_Summary() );
        int lib2Count = ln2.getDescendants().size();

        int fullModelCount = Node.getModelNode().getDescendants().size();
        int expected = 301;

        if (fullModelCount != expected) {
            LOGGER.debug( "Counts: full= " + fullModelCount + " empty=" + emptyModelCount + " ln=" + lib1Count
                + " ln2= " + lib2Count );
            // Counts: full= 301 empty=62 ln=106 ln2= 106
        }

        List<Node> descendants = Node.getModelNode().getDescendants();
        assert descendants.size() == expected; // after property node refactor

        // Then
        for (Node d : Node.getModelNode().getDescendants()) {
            descendants.remove( d );
            assertTrue( "Must only be one of these in list.", !descendants.contains( d ) );
        }
    }

    @Test
    public void DESC_LibraryMembers() {

        ln = ml.createNewLibrary( "http://example.com/test", "test", defaultProject );
        List<LibraryMemberInterface> members = ln.getDescendants_LibraryMembers();

        int cnt = ml.addOneOfEach( ln.getHead(), PREFIX );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TBB" );
        ml.addAllProperties( bo.getFacet_Summary() );

        members = ln.getDescendants_LibraryMembers();
        assert members.size() == 9;

        // Then users tests
        for (LibraryMemberInterface member : ln.getDescendants_LibraryMembers()) {
            members.remove( member );
            assertTrue( "Must only be one of these in list.", !members.contains( member ) );
        }
    }

    @Test
    public void DESC_LibraryMember_Tests() throws LibrarySaveException {

        // Given an unmanaged library
        LibraryNode ln = ml.createNewLibrary_Empty( "http://example.com", "LM_Tests", defaultProject );
        assertTrue( "Initial library must be empty.", 0 == ln.getDescendants_LibraryMembers().size() );
        assertTrue( "Library Must be editable.", ln.isEditable() );
        // When adding one of everything to it
        int count = ml.addOneOfEach( ln, "OE" );
        // Then the counts must match
        assertTrue( "Must have correct library member count.", count == ln.getDescendants_LibraryMembers().size() );

        // Given an managed library
        LibraryChainNode lcn = ml.createNewManagedLibrary_Empty( defaultProject.getNamespace(), "OE2", defaultProject );
        ln = lcn.getHead();
        assertTrue( "Library Must be editable.", ln.isEditable() );
        assertTrue( "Initial library must be empty.", 0 == ln.getDescendants_LibraryMembers().size() );
        // When adding one of everything to it
        count = ml.addOneOfEach( ln, "OE" );
        // Then the counts must match
        List<LibraryMemberInterface> dList = ln.getDescendants_LibraryMembers();
        LOGGER.debug( "Descendant list has " + dList.size() + " members." );
        assertTrue( "Must have correct library member count.", count == ln.getDescendants_LibraryMembers().size() );
    }

    @Test
    public void DESC_SimpleMembers() {

        // Given a project, libraries
        ProjectNode project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        LibraryChainNode lcn = ml.createNewManagedLibrary( "Lib2", project );

        // When all type providers added
        ml.addOneOfEach( lcn.getHead(), PREFIX );
        ArrayList<SimpleMemberInterface> simples = lcn.getDescendants_SimpleMembers();

        // Then simple members tests
        assertTrue( "Must have 2 members.", simples.size() == 2 );
        for (SimpleMemberInterface simple : simples) {
            assertTrue( "Must begin with PL1.", simple.getName().startsWith( PREFIX ) );
            simples.remove( simple );
            assertTrue( "Must only be one of these in list.", !simples.contains( simple ) );
        }
    }

    @Test
    public void DESC_TypeProviders() {

        // Given a project, libraries
        ProjectNode project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        LibraryChainNode providerLib = ml.createNewManagedLibrary( "Lib2", project );
        List<TypeProvider> providers = providerLib.getDescendants_TypeProviders();

        // When all type providers added
        ml.addOneOfEach( providerLib.getHead(), PREFIX );
        providers = providerLib.getDescendants_TypeProviders();

        // Then each provider must be listed only once
        for (TypeProvider tp : providerLib.getDescendants_TypeProviders()) {
            providers.remove( tp );
            assertTrue( "Must not find removed object.", !providers.contains( tp ) );
        }
    }

    @Test
    public void DESC_TypeUsers() {
        ln = ml.createNewLibrary( "http://example.com/test", "test", defaultProject );
        ml.addOneOfEach( ln.getHead(), PREFIX );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TBB" );
        ml.addAllProperties( bo.getFacet_Summary() );

        List<TypeUser> users = ln.getDescendants_TypeUsers();

        // Then users tests
        for (TypeUser user : ln.getDescendants_TypeUsers()) {
            users.remove( user );
            assertTrue( "Must only be one of these in list.", !users.contains( user ) );
        }
    }

    @Test
    public void DESC_ContextualFacets() {
        ln = ml.createNewLibrary( "http://example.com/test", "test", defaultProject );
        LibraryNode ln2 = ml.createNewLibrary( "http://example.com/test2", "test2", defaultProject );

        ml.addOneOfEach( ln.getHead(), PREFIX );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TBB" );
        ChoiceObjectNode co = ml.addChoice( ln, "CBB" );
        List<ContextualFacetNode> cfs = ln.getDescendants_ContextualFacets();
        assert cfs.size() == 10;

        // Given some contextual facets in multiple libraries
        ContextualFacetNode f1 = (ContextualFacetNode) bo.addFacet( "C1", TLFacetType.CUSTOM );
        ContextualFacetNode f2 = (ContextualFacetNode) bo.addFacet( "Q1", TLFacetType.QUERY );
        ContextualFacetNode f3 = (ContextualFacetNode) co.addFacet( "C1", TLFacetType.CHOICE );
        ContextualFacetNode f4 = (ContextualFacetNode) co.addFacet( "C2", TLFacetType.CHOICE );

        cfs = ln.getDescendants_ContextualFacets();
        assert cfs.size() == 14; // lib1 facets
        // When 2 are moved to different library
        ln2.addMember( f1 );
        ln2.addMember( f4 );

        // Then ln only has 12
        cfs = ln.getDescendants_ContextualFacets();
        assert cfs.size() == 12;

        // Then lib2 now has 4
        List<ContextualFacetNode> cfsLib2 = ln2.getDescendants_ContextualFacets();
        assert cfsLib2.size() == 4; // 2 moved, two when lib created

        // Then model has 14
        List<ContextualFacetNode> cfsModel = Node.getModelNode().getDescendants_ContextualFacets();
        assert cfsModel.size() == 16; // all facets

        for (ContextualFacetNode cf : ln.getDescendants_ContextualFacets()) {
            cfs.remove( cf );
            assertTrue( "Must only be one of these in list.", !cfs.contains( cf ) );
        }

        // Then Contributed tests
        List<ContributedFacetNode> contribs = ln.getDescendants_ContributedFacets();
        assert contribs.size() == 14; // Facet moved, not where contributed

        // Then contributed tests
        for (ContributedFacetNode cf : ln.getDescendants_ContributedFacets()) {
            contribs.remove( cf );
            assertTrue( "Must only be one of these in list.", !contribs.contains( cf ) );
        }

        // When custom facet added to custom facet
        CustomFacetNode ccf = new CustomFacetNode();
        ccf.setName( "CF1" );
        ccf.setOwner( f1 );
        ln.addMember( ccf );

        // Then
        cfs = ln.getDescendants_ContextualFacets();
        assert cfs.size() == 13;

        // Finally - cf owners
        List<ContextualFacetOwnerInterface> cfOwners = ln.getDescendants_ContextualFacetOwners();
        assert cfOwners.size() == 18;

        // Then contributed tests
        for (ContributedFacetNode cfo : ln.getDescendants_ContributedFacets()) {
            cfOwners.remove( cfo );
            assertTrue( "Must only be one of these in list.", !cfOwners.contains( cfo ) );
        }
    }

    @Test
    public void DESC_SpotChecks() {
        ln = ml.createNewLibrary( "http://example.com/test", "test", defaultProject );
        CoreObjectNode co = ml.addCoreObjectToLibrary( ln, "" );
        VWA_Node vwa = ml.addVWA_ToLibrary( ln, "" );
        EnumerationOpenNode oe = ml.addOpenEnumToLibrary( ln, "" );
        EnumerationClosedNode ce = ml.addClosedEnumToLibrary( ln, "" );
        co.setAssignedType( ce );
        vwa.setAssignedType( oe );

        // Then - spot check descendant lists
        assertTrue( "Library must contain core.", ln.getDescendants().contains( co ) );
        assertTrue( "Library must contain core summary.", ln.getDescendants().contains( co.getFacet_Summary() ) );
        assertTrue( "Library must contain vwa.", ln.getDescendants().contains( vwa ) );

        assertTrue( "Library must contain open enum.", ln.getDescendants_LibraryMembers().contains( oe ) );
        assertTrue( "Library must contain closed enum.", ln.getDescendants_LibraryMembers().contains( ce ) );

        assertTrue( "Library must contain core roles.",
            ln.getDescendants_TypeProviders().contains( co.getFacet_Role() ) );
        assertTrue( "Library must contain core element.",
            ln.getDescendants_TypeUsers().contains( co.getFacet_Summary().getChildren().get( 0 ) ) );
    }

    @Test
    public void DESC_ExtensionOwners() throws Exception {
        // Given a library that should be ignored
        LibraryNode ln = ml.createNewLibrary( "http://example.com/test", "test", defaultProject );
        ml.addOneOfEach( ln, PREFIX );

        List<ExtensionOwner> exOwners = ln.getDescendants_ExtensionOwners();
        assert exOwners.size() == 7;

        // Then contributed tests
        for (ExtensionOwner exo : ln.getDescendants_ExtensionOwners()) {
            exOwners.remove( exo );
            assertTrue( "Must only be one of these in list.", !exOwners.contains( exo ) );
        }
    }

    @Test
    public void DESC_XsdBuiltin() throws Exception {
        // Given the built in xsd library
        for (LibraryNode n : Node.getAllLibraries()) {
            if (n.getName().equals( XSD ))
                ln = n;
        }
        Assert.assertNotNull( ln );

        // Then assure counts
        // 20 xsd simple types, 0 complex, 0 resources
        List<Node> all = ln.getDescendants();
        Assert.assertEquals( 24, all.size() ); // 4 nav nodes and 20 simple type nodes
        List<LibraryMemberInterface> named = ln.getDescendants_LibraryMembers();
        Assert.assertEquals( 20, named.size() );
        List<TypeUser> users = ln.getDescendants_TypeUsers();
        Assert.assertEquals( 20, users.size() );
        List<TypeProvider> providers = ln.getDescendants_TypeProviders();
        Assert.assertEquals( 20, providers.size() );
    }

    @Test
    public void DESC_AssignedTypes() {
        LibraryNode ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );
        Node bo = ml.addNestedTypes( ln );

        List<Node> types = bo.getDescendants_AssignedTypes( true );

        Assert.assertNotNull( types );
        Assert.assertEquals( 2, types.size() );

        types = Node.getModelNode().getDescendants_AssignedTypes( false );
        assert types.size() == 9;
    }

}

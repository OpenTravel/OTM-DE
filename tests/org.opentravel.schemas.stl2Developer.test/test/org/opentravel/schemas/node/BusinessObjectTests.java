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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemas.actions.ChangeActionController;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class BusinessObjectTests extends BaseTest {
    static final Logger LOGGER = LoggerFactory.getLogger( BusinessObjectTests.class );

    private static final String USER_NAME_TE2 = "TE2";
    TypeProvider emptyNode = null;
    TypeProvider sType = null;

    private ChangeActionController controller = new ChangeActionController();

    @Before
    public void beforeEachEachOfTheseTests() {
        emptyNode = (TypeProvider) ModelNode.getEmptyNode();
        sType = (TypeProvider) NodeFinders.findNodeByName( "date", ModelNode.XSD_NAMESPACE );
    }

    @Test
    public void BO_ConstructorsTests() {

    }

    // factory tests
    @Test
    public void BO_FactoryTests() {

    }

    @Test
    public void BO_MockLibraryTest() {
        // Given a business object with one of each contextual facet
        LibraryNode ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "bo" );

        // Assure the mock library created a valid BO
        check( bo );
    }

    // load from library tests
    @Test
    public void BO_LibraryLoadTests() throws Exception {
        List<LibraryNode> startLibs = mc.getModelNode().getUserLibraries();
        lf.loadTestGroupA( mc );

        List<LibraryNode> libs = mc.getModelNode().getUserLibraries();
        for (LibraryNode lib : libs) {
            for (LibraryMemberInterface bo : lib.getDescendants_LibraryMembers()) {
                if (bo instanceof BusinessObjectNode)
                    check( (BusinessObjectNode) bo );
            }
            if (lib.isInChain())
                continue;
            // Repeat test with library in a chain
            LibraryChainNode lcn = new LibraryChainNode( lib );
            for (LibraryMemberInterface bo : lcn.getDescendants_LibraryMembers()) {
                if (bo instanceof BusinessObjectNode)
                    check( (BusinessObjectNode) bo );
            }
        }
    }

    // Simulate process in addMOChildren
    // load from library tests
    @Test
    public void BO_LibraryLoadTests_v16() throws Exception {
        lf.loadFile_FacetBase( defaultProject );

        List<LibraryNode> libs = mc.getModelNode().getUserLibraries();
        assertTrue( libs.size() > 0 );
        LibraryNode lib = libs.get( 0 );
        lib.setEditable( true );

        for (ContextualFacetNode cf : lib.getDescendants_ContextualFacets()) {
            // if (cf instanceof ContributedFacetNode) {
            // if (((ContributedFacetNode) cf).getContributor() == null)
            // LOGGER.debug("Missing contributor " + ((ContributedFacetNode) cf).getContributor());
            // } else
            if (cf.getWhereContributed() == null)
                LOGGER.debug( "Missing contributed " + cf.getWhereContributed() );

            // if (cf instanceof ContributedFacetNode)
            // assertTrue(((ContributedFacetNode) cf).getContributor() != null);
            // else
            // assertTrue(cf.getWhereContributed() != null);
            assertTrue( cf.getParent() != null );
            // ml.check(cf);
        }

        for (LibraryMemberInterface bo : lib.getDescendants_LibraryMembers()) {
            if (bo instanceof BusinessObjectNode)
                check( (BusinessObjectNode) bo );
        }

        // Repeat test with library in a chain
        // FIXME - fix resource then try this
        // LibraryChainNode lcn = new LibraryChainNode(lib);
        // for (Node bo : lcn.getDescendants_LibraryMembers()) {
        // if (bo instanceof BusinessObjectNode)
        // check((BusinessObjectNode) bo);
        // }
    }

    /**
     * all tests to be used in these tests and by other junits
     */
    public void check(BusinessObjectNode bo) {
        check( bo, true );
    }

    private void checkContextualFacetsForDuplicateTLAliases(BusinessObjectNode bo) {
        TLContextualFacet tl;
        for (AbstractContextualFacet cf : bo.getContextualFacets( false )) {
            tl = cf.getTLModelObject();
            ArrayList<String> aliasNames = new ArrayList<>();
            for (TLAlias tla : tl.getAliases()) {
                assertTrue( "Contextual facet must have uniquely named TLAliases.",
                    !aliasNames.contains( tla.getName() ) );
                aliasNames.add( tla.getName() );
            }
        }
    }

    public void check(BusinessObjectNode bo, boolean validate) {
        if (bo.isDeleted()) {
            LOGGER.debug( "Skipping tests - business object " + bo + " is deleted" );
            return;
        }

        // Check fixed structure
        assertTrue( "Must have identity listener.", Node.GetNode( bo.getTLModelObject() ) == bo );
        assertTrue( "Must have id facet.", bo.getFacet_ID() != null );
        assertTrue( "ID Facet parent must be bo.", ((Node) bo.getFacet_ID()).getParent() == bo );
        assertTrue( "TL Facet must report this is ID facet.", bo.getFacet_ID().isFacet( TLFacetType.ID ) );

        assertTrue( "Must have summary facet.", bo.getFacet_Summary() != null );
        assertTrue( "Summary Facet parent must be bo.", ((Node) bo.getFacet_Summary()).getParent() == bo );
        assertTrue( "TL Facet must report this is Summary facet.",
            bo.getFacet_Summary().isFacet( TLFacetType.SUMMARY ) );

        assertTrue( "Must have detail facet.", bo.getFacet_Detail() != null );
        assertTrue( "Facet parent must be bo.", ((Node) bo.getFacet_Detail()).getParent() == bo );
        assertTrue( "TL Facet must report this is Detail facet.", bo.getFacet_Detail().isFacet( TLFacetType.DETAIL ) );
        assertTrue( bo.getFacet_Attributes() == null ); // It does not have one.
        assertTrue( "Must have TL Busness Object.", bo.getTLModelObject() instanceof TLBusinessObject );

        // Is assertions
        // assertTrue("If editable it must also be aliasable.", bo.isAliasable() == bo.isEditable_newToChain());
        assertTrue( "", bo.isExtensibleObject() );
        assertTrue( "", bo.isNamedEntity() );
        assertTrue( "", bo instanceof TypeProvider );

        // check name and label
        assertTrue( bo.getName() != null );
        assertTrue( "BO must have a name.", !bo.getName().isEmpty() );
        assertTrue( "BO must have a label.", !bo.getLabel().isEmpty() );

        // Check all aliases
        ArrayList<String> aNames = new ArrayList<>();
        for (Node d : bo.getDescendants())
            // for (Node n : d.getChildren())
            if (d instanceof AliasNode) {
                assertTrue( "Alias names must be unique.", !aNames.contains( d.getName() ) );
                aNames.add( d.getName() );
            }
        checkContextualFacetsForDuplicateTLAliases( bo );

        // Check all descendants
        assertTrue( bo.getLibrary() != null );
        LibraryNode thisLib = bo.getLibrary();
        for (Node n : bo.getDescendants()) {
            if (n instanceof ContributedFacetNode) {
                ml.check( n, validate );
                assertTrue( "Must have identity listener of contributor contextual facet.",
                    Node.GetNode( n.getTLModelObject() ) == ((ContributedFacetNode) n).get() );
            } else if (n instanceof ContextualFacetNode) {
                ml.check( n, validate );
            } else if (n.getOwningComponent() instanceof ContextualFacetNode) {
                // May be in a different library
                // ml.check(n, validate);
            } else if (n.getOwningComponent() instanceof FacadeInterface) {
                LibraryMemberInterface oc = n.getOwningComponent();
                assertTrue( "Must have identity listener.",
                    Node.GetNode( n.getTLModelObject() ) == ((FacadeInterface) n.getOwningComponent()).get() );
            } else {
                assertTrue( n.getLibrary() == bo.getLibrary() );
                assertTrue( "Business object must be owning component.", n.getOwningComponent() == bo );
                assertTrue( "Must not be deleted.", !n.isDeleted() );
                assertTrue( "Must have identity listener.", Node.GetNode( n.getTLModelObject() ) == n );
            }
        }

        // Parent Links
        assertTrue( "BO must be child of parent.", bo.getParent().getChildren().contains( bo ) );
        assertTrue( "BO must be in list only once.",
            bo.getParent().getChildren().indexOf( bo ) == bo.getParent().getChildren().lastIndexOf( bo ) );

        // must have at least 3 children
        assertTrue( 3 <= bo.getChildren().size() );

        // Check all the children
        for (Node n : bo.getChildren()) {
            ml.check( n, validate );
            assertTrue( !(n instanceof VersionNode) );
        }
    }

    /**
     * Business Object Specific Tests *******************************************************
     * 
     */
    @Test
    public void BO_ExensionTests() {
        // MainController mc = OtmRegistry.getMainController();
        LoadFiles lf = new LoadFiles();
        // MockLibrary ml = new MockLibrary();

        // Lib4 is in chain when run as a group
        List<LibraryNode> libs = ModelNode.getLibraryModelManager().getUserLibraries();

        LibraryNode ln = lf.loadFile4( mc );
        assertTrue( "Loaded lib must not be in chain.", !ln.isInChain() );

        LibraryChainNode lcn = new LibraryChainNode( ln ); // Test in managed library
        ln.setEditable( true );

        BusinessObjectNode extendedBO = ml.addBusinessObjectToLibrary_Empty( ln, "ExtendedBO" );
        assertNotNull( "Null object created.", extendedBO );

        for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
            if (n instanceof BusinessObjectNode && n != extendedBO) {
                extendedBO.setExtension( (Node) n );
                check( (BusinessObjectNode) n );
                check( extendedBO );
            }
        // see also org.opentravel.schemas.node.InheritedChildren_Tests
    }

    @Test
    public void BO_ChangeToTests() {
        MockLibrary ml = new MockLibrary();
        MainController mc = OtmRegistry.getMainController();
        DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
        ProjectNode defaultProject = pc.getDefaultProject();
        TypeProvider stringType = (TypeProvider) NodeFinders.findNodeByName( "string", ModelNode.XSD_NAMESPACE );

        // Given a library, with core and vwa objects
        LibraryNode ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );
        ml.addBusinessObjectToLibrary( ln, "bo" );
        VWA_Node vwa = ml.addVWA_ToLibrary( ln, "vwa" );
        CoreObjectNode core = ml.addCoreObjectToLibrary( ln, "co" );
        assertTrue( core.isDeleteable() );
        // Given - an element on Core with user name assigned vwa as type
        ElementNode e1 = new ElementNode( core.getFacet_Summary(), USER_NAME_TE2, vwa );
        assertTrue( "VWA must be assigned to core element.", vwa.getWhereAssigned().contains( e1 ) );
        assertTrue( "Element must have user assigned name.", e1.getName().equals( USER_NAME_TE2 ) );

        //
        // When - Core is changed to Business Object
        BusinessObjectNode tboCore = new BusinessObjectNode( core );
        core.replaceWith( tboCore );
        assertTrue( ln.contains( tboCore ) );
        assertTrue( !ln.contains( core ) );
        // Then - an element with assigned name exists.
        e1 = (ElementNode) tboCore.getFacet_Summary().get( USER_NAME_TE2 );
        assertTrue( e1 != null );
        assertTrue( "VWA must now be assigned to tboCore element.", vwa.getWhereAssigned().contains( e1 ) );
        // Then - VWA Old assignment is removed
        e1 = (ElementNode) core.getFacet_Summary().get( USER_NAME_TE2 );
        assertTrue( "VWA must NOT be assigned to Core element.", !vwa.getWhereAssigned().contains( e1 ) );
        //
        // When - VWA is changed to business object
        BusinessObjectNode tboVwa = new BusinessObjectNode( vwa );
        vwa.replaceWith( tboVwa );
        // Then - the element that was assigned to VWA must be the bo name because elements assigned to a BO can not
        // change their name
        e1 = (ElementNode) tboCore.getFacet_Summary().get( tboVwa.getName() );
        assertTrue( e1 != null );
        // FIXME - replace does a replace, but not on the right element.

        // Given - added id facets to make the business objects valid
        new ElementNode( tboCore.getFacet_ID(), "TestEleInID" + tboCore.getName(), stringType );
        new ElementNode( tboVwa.getFacet_ID(), "TestEleInID" + tboVwa.getName(), stringType );

        // Then
        check( tboCore );
        check( tboVwa );
        // Then - library and assignments are correct
        assertTrue( "Core is not in a library.", core.getLibrary() == null );
        assertTrue( "VWA is not in a library.", vwa.getLibrary() == null );
        assertTrue( "tboCore is in a library.", tboCore.getLibrary() != null );
        assertTrue( "tboVWA is in a library.", tboVwa.getLibrary() != null );
        e1 = (ElementNode) tboCore.getFacet_Summary().get( tboVwa.getName() );
        assertTrue( e1 != null );
        assertTrue( "tboVWA must be assigned to element.", tboVwa.getWhereAssigned().contains( e1 ) );

        //
        // Same test, but as part of a chain
        LibraryChainNode lcn = new LibraryChainNode( ln ); // make sure is version safe
        core = ml.addCoreObjectToLibrary( ln, "co2" );
        vwa = ml.addVWA_ToLibrary( ln, "vwa2" );
        new ElementNode( core.getFacet_Summary(), "TestElement" ).setAssignedType( vwa );

        tboCore = new BusinessObjectNode( core );
        core.replaceWith( tboCore );
        tboVwa = new BusinessObjectNode( vwa );
        vwa.replaceWith( tboVwa );
        new ElementNode( tboCore.getFacet_ID(), "TestEleInID" + tboCore.getName(), stringType );
        new ElementNode( tboVwa.getFacet_ID(), "TestEleInID" + tboVwa.getName(), stringType );
        check( tboCore );
        check( tboVwa );

        // TODO - validate where assigned was changed
    }

    static String NAME = "Test1";
    static String PREFIX = "tga";

    @Test
    public void BO_FacetAsTypeTests() {
        // Make sure the library is empty before starting
        if (ln != null) {
            List<LibraryMemberInterface> lms = ln.get_LibraryMembers();
            assert lms.isEmpty();
        }
        // Make sure the TL project is empty
        List<ProjectItem> items = defaultProject.getTLProject().getProjectItems();
        assert items.isEmpty();

        ln = lf.loadFile1( mc );
        ln.setEditable( true );

        // Make sure the file loaded members correctly
        assertTrue( ln.getName().equals( NAME ) );
        assertTrue( ln.getPrefix().equals( PREFIX ) );
        for (LibraryMemberInterface lm : ln.get_LibraryMembers())
            assertTrue( !lm.isDeleted() );

        // Profile element is in the service in File1.otm
        assertTrue( !ln.getServiceRoot().getChildren().isEmpty() );
        ServiceNode svc = ln.getServiceRoot().getService();
        if (svc != null && !svc.isEmpty()) {
            List<TypeUser> svcTypeUsers = ln.getServiceRoot().getDescendants_TypeUsers();
            assertTrue( !svcTypeUsers.isEmpty() );
        }
        assert !svc.isDeleted();

        // Find an element to use to make sure all facets can be assigned as a type
        TypeUser user = null;
        for (TypeUser n : ln.getDescendants_TypeUsers())
            if (!((Node) n).getOwningComponent().getName().equals( "Profile" ) && n instanceof ElementNode) {
                user = n;
                break;
            }
        if (user == null)
            LOGGER.error( "Missing Profile type user." );
        assert user != null;
        assert user.isEditable();

        // File 1 has a business object Profile with 5 facets and 1 alias
        BusinessObjectNode bo = null;
        List<LibraryMemberInterface> members = ln.getDescendants_LibraryMembers();
        for (LibraryMemberInterface n : members)
            if (n.getName().equals( "Profile" ) && n instanceof BusinessObjectNode)
                bo = (BusinessObjectNode) n;
        assertTrue( "Profile object must be in test 1.", bo != null );

        // Check facets - Children = 3 FacetProviderNodes, 2 ContributedFacetNodes and one AliasNode
        final int expectedFacetCount = 5;
        int facetCnt = 0;
        for (Node n : bo.getChildren()) {
            if (n instanceof ContributedFacetNode)
                n = ((ContributedFacetNode) n).get(); // get the actual facet provider

            if (n instanceof FacetProviderNode) {
                facetCnt++;
                user.setAssignedType( (FacetProviderNode) n );
                assertTrue( "User must be assigned facet as type.", user.getAssignedType() == n );
                assertTrue( "Facet must have user in where assigned list.",
                    ((FacetProviderNode) n).getWhereAssigned().contains( user ) );
            }
        }
        assertTrue( "Profile business object in test 1 must have " + expectedFacetCount + " facets.",
            facetCnt == expectedFacetCount );

        // check alias
        int aliasCnt = 0;
        for (Node n : bo.getChildren())
            if (n instanceof AliasNode) {
                aliasCnt++;
                user.setAssignedType( (TypeProvider) n );
                assert user.getAssignedType() == n;
            }
        assert aliasCnt == 1;
    }

    @Test
    public void BO_NameChangeTests() {
        // On name change, all users of the BO and its aliases and facets also need to change.
        LibraryNode ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );

        // Given - a Business Object with alias
        final String boName = "initialBOName";
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, boName );
        AliasNode alias1 = bo.addAlias( "boAlias" );
        AliasNode aliasSummary = null;
        for (Node n : bo.getFacet_Summary().getChildren())
            if (n instanceof AliasNode)
                aliasSummary = (AliasNode) n;
        // Then the alias must exist on the bo and it's facet
        assertNotNull( alias1 );
        assertNotNull( aliasSummary );

        // When - a core is created that has elements that use the BO and aliases as properties
        CoreObjectNode co = ml.addCoreObjectToLibrary( ln, "user" );
        PropertyNode pBO = new ElementNode( co.getFacet_Summary(), "p1", bo );
        PropertyNode pAlias1 = new ElementNode( co.getFacet_Summary(), "p2", alias1 );
        PropertyNode pBOSummary = new ElementNode( co.getFacet_Summary(), "p3", bo.getFacet_Summary() );
        PropertyNode pBOSumAlias = new ElementNode( co.getFacet_Summary(), "p4", aliasSummary );
        // Then - the facet alias has where used
        assertTrue( "Facet alias must be assigned as type.", !aliasSummary.getWhereAssigned().isEmpty() );
        // Then - the elements are named after their type
        assertTrue( "Element name must be the BO name.", pBO.getName().equals( bo.getName() ) );
        assertTrue( "Element name must be alias name.", pAlias1.getName().contains( alias1.getName() ) );
        assertTrue( "Element name must NOT be facet name.",
            !pBOSummary.getName().equals( bo.getFacet_Summary().getName() ) );
        // Then - assigned facet name will be constructed by compiler using owning object and facet type.
        assertTrue( "Element name must start with BO name.", pBOSummary.getName().startsWith( bo.getName() ) );
        assertTrue( "Element name must contain facet name.",
            pBOSummary.getName().contains( bo.getFacet_Summary().getName() ) );
        assertTrue( "Element name must start with alias name.", pBOSumAlias.getName().startsWith( alias1.getName() ) );

        // When - Change the BO name
        String changedName = "changedName";
        bo.setName( changedName );
        changedName = NodeNameUtils.fixBusinessObjectName( changedName ); // get the "fixed" name

        // Then - the business object name and facets must change.
        assertTrue( "Business Object name must be fixed name.", pBO.getName().equals( changedName ) );
        assertTrue( "Alias name must be unchanged.", pAlias1.getName().equals( alias1.getName() ) );
        assertTrue( "Facet name must start with BO name.", pBOSummary.getName().startsWith( changedName ) );
        // Then - the facet alias has where used
        assertTrue( "Facet alias must be assigned as type.", !aliasSummary.getWhereAssigned().isEmpty() );
        // Then - the elements are named after their type
        assertTrue( "Element name must be the BO name.", pBO.getName().equals( changedName ) );
        assertTrue( "Element name must contain facet name.",
            pBOSummary.getName().contains( bo.getFacet_Summary().getName() ) );
        assertTrue( "Element name must start with BO name.", pBOSummary.getName().startsWith( changedName ) );
        assertTrue( "Element name must start with alias name.", pBOSumAlias.getName().startsWith( alias1.getName() ) );
        assertTrue( "Element name must start with alias name.", pAlias1.getName().startsWith( alias1.getName() ) );

        // When - alias name changed
        String aliasName2 = "aliasName2";
        alias1.setName( aliasName2 );
        aliasName2 = alias1.getName(); // get the "fixed" name
        // FIXME - what events happen? Why isn't the element pAlias1 touched?

        // Then - all aliases on BO must change name
        assertTrue( "Alias Name must change.", pAlias1.getName().equals( aliasName2 ) );
        assertTrue( "Alias on summary facet must change.", aliasSummary.getName().startsWith( aliasName2 ) );

        // Then - all type users of those aliases must change name
        // FIXME - needs to be studied - assertTrue("Element name must start with changed alias name.",
        // pBOSumAlias.getName().startsWith(aliasName2));
        assertTrue( "Element name must start with changed alias name.", pAlias1.getName().startsWith( aliasName2 ) );
    }

}

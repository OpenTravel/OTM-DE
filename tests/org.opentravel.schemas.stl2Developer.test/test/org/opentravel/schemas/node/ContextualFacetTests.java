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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.commands.ContextualFacetHandler;
import org.opentravel.schemas.node.handlers.children.NavNodeChildrenHandler;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.SharedFacetNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.ChoiceFacetNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.CustomFacetNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class ContextualFacetTests extends BaseTest {
    static final Logger LOGGER = LoggerFactory.getLogger( ContextualFacetTests.class );

    @Test
    public void CF_HandlerTests() {
        ContextualFacetHandler handler = new ContextualFacetHandler();
        ln = ml.createNewLibrary_Empty( defaultProject.getNamespace(), "T1", defaultProject );
        ChoiceObjectNode ch = ml.addChoice( ln, "Ch1" );
        List<AbstractContextualFacet> cfs = ch.getChoiceFacets();
        assert ch.getChoiceFacets().size() == 2;
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "Bo1" );
        assert bo.getContextualFacets( false ).size() == 2;

        // When - add Choice facet
        handler.addContextualFacet( ch );
        // When - add Query facet
        handler.addContextualFacet( bo, TLFacetType.QUERY );
        // When - add Custom facet
        handler.addContextualFacet( bo, TLFacetType.CUSTOM );
        // To Do - When - add Update facet

        // Then
        ml.check( ch );
        assertTrue( ch.getChoiceFacets().size() == 3 );
        for (AbstractContextualFacet cf : ch.getChoiceFacets())
            assertTrue( cf instanceof ContextualFacetNode );
        ml.check( bo );
        assertTrue( bo.getContextualFacets( false ).size() == 4 );
        for (AbstractContextualFacet af : bo.getContextualFacets( false ))
            assertTrue( af instanceof ContextualFacetNode );
        ml.check( ln );
    }

    @Test
    public void CF_moveTests() {
        // Given - an editable library
        LibraryNode srcLib = ml.createNewLibrary( pc, "CF_MoveTest1" );
        LibraryNode destLib = ml.createNewLibrary( pc, "CF_MoveTest2" );

        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( srcLib, "Tbo", true );
        bo.addAlias( "TboAlias" );
        List<AbstractContextualFacet> cfs = bo.getContextualFacets( false );

        ml.check();

        // When the facets are moved
        for (AbstractContextualFacet cf : cfs) {
            assert cf instanceof LibraryMemberInterface;
            destLib.addMember( (LibraryMemberInterface) cf );
        }
        ml.check();

        // When the bo is moved
        destLib.addMember( bo );
        ml.check();
        // Check alias assignments
    }

    @Test
    public void ContextualFacets_v16() {
        // Given - an editable library
        LibraryNode ln = ml.createNewLibrary_Empty( "http://example.com/t2", "TestLib2", defaultProject );
        // Given - make the library versioned
        // new LibraryChainNode(ln);
        ln.setEditable( true );

        // Given - a business object in library
        BusinessObjectNode bo = new BusinessObjectNode( new TLBusinessObject() );
        bo.setName( "TestBO" );
        ln.addMember( bo );
        assertTrue( bo.isEditable_newToChain() ); // required to add facets
        // Given - an id facet property to make the bo valid
        TypeProvider string = ml.getXsdString();
        new ElementNode( bo.getFacet_ID(), "TestEleInID" + bo.getName(), string );
        //
        int count = ln.getDescendants_LibraryMembers().size();
        ml.check( bo );
        ml.check( ln );

        // When - addFacet() used to add a custom facet
        AbstractContextualFacet cf = bo.addFacet( "Custom1", TLFacetType.CUSTOM );
        // Then - check contextual facet
        assertTrue( cf != null );
        assertTrue( cf instanceof CustomFacetNode );
        // assertTrue(!(cf instanceof ContributedFacetNode));
        assertTrue( "Identity listener must be set.", Node.GetNode( ((CustomFacetNode) cf).getTLModelObject() ) == cf );
        assertTrue( ln.contains( cf ) );
        assertTrue( cf.getLibrary() == ln );
        // Only true if v15
        // assertTrue("Contextual Facet parent must be nav node", cf.getParent() == bo.getParent());
        // True in non-versioned, v16 library
        assertTrue( cf.getParent() instanceof NavNode );
        // ??? - contextual facets are NOT Versioned!
        // True in versioned library
        // ??? - assertTrue(cf.getParent() instanceof VersionNode);

        // Then - check contributed facet
        ContributedFacetNode contrib = ((ContextualFacetNode) cf).getWhereContributed();
        assertTrue( contrib != null );
        assertTrue( contrib.getOwningComponent() == bo );
        // Not true - assertTrue(cf.getOwningComponent() == bo);
        ml.check( cf );

        // When - adding elements and attributes to contextual facet
        new AttributeNode( cf, "att1", string );
        new ElementNode( cf, "Ele1", string );
        // Then
        assertTrue( "Must find child.", cf.findChildByName( "att1" ) != null );
        assertTrue( "Must find child.", cf.findChildByName( "Ele1" ) != null );
        assertTrue( "Must find child.", contrib.findChildByName( "att1" ) != null );
        assertTrue( "Must find child.", contrib.findChildByName( "Ele1" ) != null );

        // When - adding elements and attributes to contributed facet
        new AttributeNode( contrib.getContributor(), "att2", string );
        new ElementNode( contrib.getContributor(), "Ele2", string );
        // new AttributeNode(contrib, "att2");
        // new ElementNode(contrib, "Ele2");
        // Then
        assertTrue( "Must find child.", cf.findChildByName( "att2" ) != null );
        assertTrue( "Must find child.", cf.findChildByName( "Ele2" ) != null );
        assertTrue( "Must find child.", contrib.findChildByName( "att2" ) != null );
        assertTrue( "Must find child.", contrib.findChildByName( "Ele2" ) != null );

        //
        // Simulate construction in LibraryChildrenHandler - get tlCFs first using newObjectNode which does NOT add to
        // library or tlLibrary
        //
        // Given - a TLContextualFacet member of a TLBusinessObject
        TLBusinessObject tlBO = new TLBusinessObject();
        tlBO.setName( "BO2" );
        TLContextualFacet tlCf = ContextualFacetNode.createTL( "Custom2", TLFacetType.CUSTOM );
        tlBO.addCustomFacet( tlCf );
        //
        // When - factory used to add a custom facet
        ContextualFacetNode cf2 = (ContextualFacetNode) NodeFactory.newLibraryMember( tlCf );
        // Then - cf2 created but not added to any library and does NOT have contributed
        assertTrue( cf2 != null );
        assertTrue( cf2.getWhereContributed() == null );
        assertTrue( cf2.getLibrary() == null );
        assertTrue( cf2.getParent() == null );
        assertTrue( "Identity listener must be set.", Node.GetNode( cf2.getTLModelObject() ) == cf2 );
        // When - added to library
        ln.addMember( cf2 );
        assertTrue( cf2.getLibrary() == ln );
        assertTrue( cf2.getParent() instanceof NavNode );

        // When - BO created using main factory
        BusinessObjectNode bo2 = (BusinessObjectNode) NodeFactory.newChild( ln, tlBO );
        List<Node> kids = bo2.getChildren();
        assertTrue( cf2.getWhereContributed() != null );
        bo2.getChildren().contains( cf2.getWhereContributed() );
        // Then
        assertTrue( cf2.getName().startsWith( bo2.getName() ) );
        assertTrue( cf2.getWhereContributed().getName().startsWith( bo2.getName() ) );

        // When - adding other facet types
        bo.addFacet( "q1", TLFacetType.QUERY );
        bo.addFacet( "u1", TLFacetType.UPDATE );

        // Then
        ml.check( ln );

        // Then - assure contextual facets are NOT wrapped in version nodes
        for (Node n : bo.getChildren())
            assertTrue( !(n instanceof VersionNode) );
    }

    /**
     * all tests to be used in these tests and by other junits
     */
    public void check(AbstractContextualFacet cf) {
        check( cf, true );
    }

    public void check(AbstractContextualFacet cf, boolean validate) {
        // TL Structure
        assertTrue( cf.getTLModelObject() instanceof TLContextualFacet );
        if (cf.getWhereContributed() != null) {
            assertTrue( "Contexutal facet must have TL owning entity.",
                cf.getTLModelObject().getOwningEntity() != null );
            assertTrue(
                cf.getWhereContributed().getParent().getTLModelObject() == cf.getTLModelObject().getOwningEntity() );
        } else {
            if (validate)
                // assertTrue("Must have contributed facet to be valid.", false);
                LOGGER.error( "Must have contributed facet to be valid: " + cf );
            else
                LOGGER.warn( "Contextual facet " + cf + " is missing where contributed." );
        }
        // setName()
        //
        final String NEWNAME = "myName";
        final String oldName = cf.getName();
        if (cf.getLibrary().isEditable() && !cf.isInherited()) {
            assertTrue( "Must be renamable.", cf.isRenameable() );
            cf.setName( NEWNAME );
            String n = cf.getName();
            assertTrue( "Facet must contain new name.",
                cf.getName().contains( NodeNameUtils.fixContextualFacetName( cf, NEWNAME ) ) );
            cf.setName( oldName );
            assertTrue( "Must be delete-able.", cf.isDeleteable() );
        }

        // Inherited statements
        //
        assertTrue( "Must be assignable.", cf.isAssignable() );
        assertTrue( "Must be valid parent to attributes.", cf.canOwn( PropertyNodeType.ATTRIBUTE ) );
        assertTrue( "Must be valid parent to elements.", cf.canOwn( PropertyNodeType.ELEMENT ) );

        assertFalse( "Must NOT be assignable to element ref", cf.isAssignableToElementRef() );
        assertFalse( "Must NOT be assignable to simple.", cf.isAssignableToSimple() );
        assertFalse( "Must NOT be assignable to simple.", cf.isSimpleAssignable() );
        assertFalse( "Must NOT be assignable to VWA.", cf.isAssignableToVWA() );

        // Behaviors
        //
        AttributeNode attr = new AttributeNode( cf, "att1" );
        ElementNode ele = new ElementNode( cf, "ele1" );
        assertTrue( "Must be able to add attributes.", attr.getParent() == cf );
        assertTrue( "Must be able to add elements.", ele.getParent() == cf );
        assertTrue( cf.getChildren().contains( ele ) );
        attr.delete();
        ele.delete();
        assertFalse( cf.getChildren().contains( attr ) );
        assertFalse( cf.getChildren().contains( ele ) );

        // Contributed/contextual relationship
        assertTrue( "Contextual facet must be version 1.6 facet.", cf instanceof ContextualFacetNode );
        ContributedFacetNode contrib = ((ContextualFacetNode) cf).getWhereContributed();
        if (contrib != null) {
            assertTrue( "Must have contributor.", contrib.getContributor() == cf );
            assertTrue( "Must have parent.", contrib.getParent() != null );
            assertTrue( contrib.getOwningComponent() instanceof ContextualFacetOwnerInterface );
            // Not all owners are library members
            assertTrue( contrib.getParent() instanceof ContextualFacetOwnerInterface );
            ContextualFacetOwnerInterface owner = (ContextualFacetOwnerInterface) contrib.getParent();
            // Retrieving will replace nodes so it will not be contained
            if (((Node) owner).findChildByName( cf.getLocalName() ) == null)
                LOGGER.error( "Can't find contributor: " + cf.getLocalName() );
            assertTrue( ((Node) owner).findChildByName( cf.getLocalName() ) != null );
        }
    }

    // Make sure contextual facets are children of chain aggregate node
    @Test
    public void CF_chainAggregate_Tests() {
        // Given - a library in a chain
        LibraryChainNode lcn = ml.createNewManagedLibrary_Empty( "http://www.test.com/test1", "test1", defaultProject );
        ln = lcn.getHead();
        assert ln.isEditable();

        // Given - Choice and Business Objects in the chain
        ContextualFacetOwnerInterface choice1 = ml.addChoice( ln, "Choice1" );
        ContextualFacetOwnerInterface bo1 = ml.addBusinessObjectToLibrary( ln, "Bo1" );
        assertTrue( lcn.contains( (Node) choice1 ) );
        assertTrue( lcn.contains( (Node) bo1 ) );

        // kids must contain contextual facets
        List<Node> lcnKids = lcn.getComplexAggregate().getChildren();
        assert !lcnKids.isEmpty();
        boolean found = false;
        for (Node n : lcnKids)
            if (n instanceof VersionNode)
                if (((VersionNode) n).get() instanceof ContextualFacetNode)
                    found = true;
        assert found;

        AbstractContextualFacet cc1 = ((ChoiceObjectNode) choice1).addFacet( "Ncf1" );
        AbstractContextualFacet bc1 = ((BusinessObjectNode) bo1).addFacet( "Ncf1", TLFacetType.CUSTOM );
        lcnKids = lcn.getComplexAggregate().getChildren();
        assertTrue( "Must find version node for added facet.", lcnKids.contains( cc1.getVersionNode() ) );
        assertTrue( "Must find version node for added facet.", lcnKids.contains( bc1.getVersionNode() ) );
    }

    /**
     * Test detecting duplicate names warning (isUnique)
     */
    @Test
    public void CF_uniqueProperites_Tests() {
        // Given - Choice object in a library
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        LibraryNode ln2 = ml.createNewLibrary( "http://www.test.com/test2", "test2", defaultProject );
        ChoiceObjectNode choice = ml.addChoice( ln, "Choice1" );
        SharedFacetNode shared = choice.getFacet_Shared();
        assert shared != null;

        // Given - Choice facet
        TLContextualFacet tlCF1 = ContextualFacetNode.createTL( "CF1", TLFacetType.CHOICE );
        ChoiceFacetNode cf1 = new ChoiceFacetNode( tlCF1 );

        AttributeNode attr1 = new AttributeNode( shared, "a1" );
        AttributeNode attr1a = new AttributeNode( shared, "a1" );
        assertTrue( "Must fail unique test.", !attr1a.isUnique() );

        AttributeNode attr2 = new AttributeNode( cf1, "a2" );
        AttributeNode attr2a = new AttributeNode( cf1, "a2" );
        assertTrue( "Must fail unique test.", !attr2a.isUnique() );

        AttributeNode attr3 = new AttributeNode( cf1, "a1" );
        assertTrue( "Must fail unique test.", !attr3.isUnique() );

    }

    /**
     * Add contextual facets. Assure they are created and the contributed facets are managed correctly.
     */
    @Test
    public void CF_setOwner_Tests() {
        // Given - 2 BOs in a library
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        // ContextualFacetOwnerInterface bo1 = ml.addBusinessObjectToLibrary(ln, "BaseBO1");
        // ContextualFacetOwnerInterface bo2 = ml.addBusinessObjectToLibrary(ln, "BaseBO2");
        // Given - 2 Choice in a library
        ContextualFacetOwnerInterface choice1 = ml.addChoice( ln, "Choice1" );
        ContextualFacetOwnerInterface choice2 = ml.addChoice( ln, "Choice2" );

        List<LibraryMemberInterface> libMbrs = ln.get_LibraryMembers();
        // List<AbstractContextualFacet> bo1Mbrs = bo1.getContextualFacets(false);
        // List<AbstractContextualFacet> bo2Mbrs = bo2.getContextualFacets(false);
        List<AbstractContextualFacet> choice1Mbrs = choice1.getContextualFacets( false );
        List<AbstractContextualFacet> choice2Mbrs = choice2.getContextualFacets( false );

        // Given - 2 Choice facets
        TLContextualFacet tlCF1 = ContextualFacetNode.createTL( "CF1", TLFacetType.CHOICE );
        TLContextualFacet tlCF2 = ContextualFacetNode.createTL( "CF2", TLFacetType.CHOICE );
        AbstractContextualFacet cf1 = new ChoiceFacetNode( tlCF1 );
        AbstractContextualFacet cf2 = new ChoiceFacetNode( tlCF2 );

        // When - TL facet added using AbstractContextualFacet setOwner method
        cf1.setOwner( choice1 );
        cf2.setOwner( choice2 );

        choice1Mbrs = choice1.getContextualFacets( false );
        choice2Mbrs = choice2.getContextualFacets( false );
        ml.check( ln );

        // When - cf1 is set to be owned by choice 2
        cf1.setOwner( choice2 );
        choice1Mbrs = choice1.getContextualFacets( false );
        choice2Mbrs = choice2.getContextualFacets( false );

        // Then
        assertTrue( "No longer owned.", !choice1Mbrs.contains( cf1 ) );
        assertTrue( "Is now owned.", choice2Mbrs.contains( cf1 ) );

        // When - use the business object to create the facet
        // AbstractContextualFacet c1 = bo.addFacet("BaseC1", TLFacetType.CUSTOM);
        // AttributeNode a1 = new AttributeNode(c1, "cAttr1");
    }

    /**
     * Add contextual facets to contextual facets. Assure they are created and the contributed facets are managed
     * correctly.
     */
    @Test
    public void CF_setOwner_CF_Tests() {
        // Given - Choice Object in a library
        ln = ml.createNewLibrary( "http://www.test.com/test2", "test2", defaultProject );
        ContextualFacetOwnerInterface choice1 = ml.addChoice( ln, "Choice1" );

        List<LibraryMemberInterface> libMbrs = ln.get_LibraryMembers();
        List<AbstractContextualFacet> choice1Mbrs = choice1.getContextualFacets( false );
        List<AbstractContextualFacet> cf1Mbrs = null;

        // Given - 2 Choice facets
        TLContextualFacet tlCF1 = ContextualFacetNode.createTL( "CF1", TLFacetType.CHOICE );
        TLContextualFacet tlCF2 = ContextualFacetNode.createTL( "CF2", TLFacetType.CHOICE );
        AbstractContextualFacet cf1 = new ChoiceFacetNode( tlCF1 );
        AbstractContextualFacet cf2 = new ChoiceFacetNode( tlCF2 );

        // When - TL facet added using AbstractContextualFacet setOwner method
        cf1.setOwner( choice1 );
        cf2.setOwner( cf1 );

        // Then - contributor set and has parent
        assert cf1.getWhereContributed() != null;
        assert cf2.getWhereContributed() != null;
        assertTrue( "Must have parent", cf1.getWhereContributed().getParent() != null );
        assertTrue( "Must have parent", cf2.getWhereContributed().getParent() != null );

        choice1Mbrs = choice1.getContextualFacets( false );
        cf1Mbrs = cf1.getContextualFacets( false );
        ml.check( ln );
    }

    @Test
    public void Facets_InheritanceTests() {
        // Given - a BO in a library
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        BusinessObjectNode baseBO = ml.addBusinessObjectToLibrary( ln, "BaseBO" );
        AbstractContextualFacet c1 = baseBO.addFacet( "BaseC1", TLFacetType.CUSTOM );
        AttributeNode a1 = new AttributeNode( c1, "cAttr1" );

        // Then - finding c1 facet must work because it is used in children handler
        ContributedFacetNode x1 = (ContributedFacetNode) baseBO.findChildByName( c1.getLocalName() );
        assertTrue( "Must be able to find c1 by name.", x1.get() == c1 );
        // Then - c1 must have children to be inherited.
        assertTrue( "Summary must have children.", !baseBO.getFacet_Summary().getChildren().isEmpty() );
        assertTrue( "Summary must not have inherited children.",
            baseBO.getFacet_Summary().getInheritedChildren().isEmpty() );

        // Given - a second, empty BO to be extended
        BusinessObjectNode extendedBO = ml.addBusinessObjectToLibrary_Empty( ln, "ExBO" );
        new ElementNode( extendedBO.getFacet_Summary(), "ExEle" );

        ml.printListners( baseBO );
        ml.printListners( c1 );
        //
        // When - objects are extended
        extendedBO.setExtension( baseBO );
        assertTrue( "ExtendedBO extends BaseBO.", extendedBO.isExtendedBy( baseBO ) );

        ml.printListners( baseBO );
        ml.printListners( c1 );
        ml.printListners( extendedBO ); // FIXME - what good is the Where Extended listener???

        // NOTE - at this point there will NOT be any inheritance listeners or inherited facets.
        // They will be created on-the-fly/on-demand when children handler accessed.

        // When - inherited children are retrieved
        // This retrieval must result in newly created inherited children and inheritance listeners
        List<Node> exList = extendedBO.getInheritedChildren();
        assertTrue( "Must have inherited child.", !exList.isEmpty() );
        // assertTrue("Must have inherited child.", !extendedBO.getInheritedChildren().isEmpty());
        checkInheritanceListeners( c1, null, 1 );

        // When - inherited children retrieved again
        // This retrieval SHOULD (but will not) reuse the inherited list in the children handler
        List<?> iKids = extendedBO.getChildrenHandler().getInheritedChildren();
        checkInheritanceListeners( c1, null, 1 );

        // When - children then inherited children are retrieved as done in getContextualFacets()
        extendedBO.getChildrenHandler().get();
        checkInheritanceListeners( c1, null, 1 );
        extendedBO.getChildrenHandler().getInheritedChildren();
        // Then - handlers are still OK
        checkInheritanceListeners( c1, null, 1 );

        // When - inherited children retrieved again
        // This retrieval SHOULD also reuse the inherited list in the children handler
        AbstractContextualFacet inheritedCustom = null;
        List<AbstractContextualFacet> customFacets = extendedBO.getContextualFacets( true );
        checkInheritanceListeners( c1, null, 1 );
        for (AbstractContextualFacet cf : customFacets) {
            // LOGGER.debug("Does " + c1.getLocalName() + " == " + cf.getLocalName() + "?");
            if (c1.getLocalName().equals( cf.getLocalName() ))
                inheritedCustom = cf;
        }
        checkInheritanceListeners( c1, inheritedCustom, 1 );

        // Then - there must be an inherited facet.
        assertTrue( "Must have inherited c1 custom facet.", inheritedCustom != null );
        assertTrue( "InheritedFrom must be the c1 custom facet.", inheritedCustom.getInheritedFrom() == c1 );

        // Then - verify listeners are correct
        ml.printListners( c1 );
        for (ModelElementListener l : c1.getTLModelObject().getListeners())
            if (l instanceof InheritanceDependencyListener) {
                assertTrue( ((InheritanceDependencyListener) l).getNode() == inheritedCustom );
                assertTrue( ((InheritanceDependencyListener) l).getHandler() == inheritedCustom.getParent()
                    .getChildrenHandler() );
            }

        // Then - there must be inherited children in the facets.
        //
        List<Node> baseKids = baseBO.getFacet_Summary().getChildren();
        assertTrue( "Base BO summary must have properties.", !baseKids.isEmpty() );
        List<Node> exKids = extendedBO.getFacet_Summary().getChildren();
        assertTrue( "Extended BO summary must have properties.", !exKids.isEmpty() );
        List<Node> inheritedKids = extendedBO.getFacet_Summary().getInheritedChildren();
        assertTrue( "Extended BO summary must have inherited properties.", !inheritedKids.isEmpty() );
        // Then - verify listeners are correct
        for (Node i : inheritedKids) {
            ComponentNode ci = (ComponentNode) i;
            assertTrue( baseBO.getFacet_Summary().contains( ci.getInheritedFrom() ) );
            for (ModelElementListener l : ci.getInheritedFrom().getTLModelObject().getListeners())
                if (l instanceof InheritanceDependencyListener) {
                    assertTrue( ((InheritanceDependencyListener) l).getNode() == i );
                    assertTrue(
                        ((InheritanceDependencyListener) l).getHandler() == i.getParent().getChildrenHandler() );
                }
        }

        // FIXME - there are no InheritanceDependencyListeners on c1
        //
        // Tests to assure changes to objects are synchronized with the inherited "ghosts".
        //
        // When - custom facet name changes to include owner and newName
        String newName = "ChangedName";
        String startingName = inheritedCustom.getName();
        c1.setName( newName );
        assertTrue( c1.getName().contains( newName ) );

        // Then - listener wont be removed until inherited children retrieved from exBO
        for (ModelElementListener l : c1.getTLModelObject().getListeners())
            if (l instanceof InheritanceDependencyListener)
                assertTrue( ((InheritanceDependencyListener) l).getNode() == inheritedCustom );

        // Then - the old inherited custom node is no longer valid.
        // clear does not delete - re-read does.
        // assertTrue(inheritedCustom.isDeleted());
        // Then - check the new inherited custom.
        AbstractContextualFacet changedCF = null;
        List<AbstractContextualFacet> x2List = extendedBO.getCustomFacets();
        assertTrue( "Must be empty since getCustomFacets() does not return inherited.", x2List.isEmpty() );
        x2List = extendedBO.getContextualFacets( true );
        for (AbstractContextualFacet cf : x2List)
            if (c1.getLocalName().equals( cf.getLocalName() ))
                changedCF = cf;
        assertTrue( !changedCF.getName().equals( startingName ) );
        assertTrue( changedCF.getName().contains( newName ) );
        // Then - listener will only be to new custom
        for (ModelElementListener l : c1.getTLModelObject().getListeners())
            if (l instanceof InheritanceDependencyListener)
                assertTrue( ((InheritanceDependencyListener) l).getNode() == changedCF );

        // When - add an attribute
        AttributeNode a2 = new AttributeNode( c1, "cAttr2" );
        // Then - inherited custom must NOT have that attr in its children
        Node ia2 = inheritedCustom.findChildByName( a2.getName() );
        assertTrue( "Must not find attribute.", inheritedCustom.findChildByName( a2.getName() ) == null );
        // Then - changed custom must have new attr
        for (Node n : changedCF.getInheritedChildren())
            if (n.getName().equals( a2.getName() ))
                ia2 = n;
        assertTrue( "Must find ghost node with a2's name.", ia2 != null );

        // When - delete the attribute in c1 (base custom)
        a1.delete();
        // Then - node with name of a1 must not be in inherited children
        assertTrue( "Must not find a1 by name.", changedCF.findChildByName( a1.getName() ) == null );
    }

    // Pass 0 if count is unknown or not to be checked.
    private void checkInheritanceListeners(Node owner, Node subject, int countExpected) {
        ml.printListners( owner );
        for (ModelElementListener l : owner.getTLModelObject().getListeners())
            if (l instanceof InheritanceDependencyListener) {
                countExpected--;
                InheritanceDependencyListener il = (InheritanceDependencyListener) l;
                if (owner instanceof AbstractContextualFacet)
                    assertTrue( "Handler must be for nav node.", il.getHandler() instanceof NavNodeChildrenHandler );
                if (subject != null) {
                    assertTrue( il.getNode() == subject );
                    assertTrue( il.getHandler() == subject.getParent().getChildrenHandler() );
                }
            }
        assertTrue( "Wrong listener count.", countExpected >= 0 );
    }

    // private void printListners(Node node) {
    // for (ModelElementListener tl : node.getTLModelObject().getListeners())
    // if (tl instanceof BaseNodeListener) {
    // LOGGER.debug("Listener on " + node + " of type " + tl.getClass().getSimpleName() + " GetNode() = "
    // + ((BaseNodeListener) tl).getNode());
    // if (((BaseNodeListener) tl).getNode().isDeleted())
    // LOGGER.debug(((BaseNodeListener) tl).getNode() + " is deleted.");
    // }
    // }

}

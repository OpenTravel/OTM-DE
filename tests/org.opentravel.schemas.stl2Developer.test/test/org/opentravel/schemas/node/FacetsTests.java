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

import org.junit.After;
import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.OperationFacetNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.objectMembers.VWA_SimpleFacetFacadeNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ChoiceFacetNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.CoreSimpleFacetNode;
import org.opentravel.schemas.node.typeProviders.CustomFacetNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.ListFacetNode;
import org.opentravel.schemas.node.typeProviders.QueryFacetNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.node.typeProviders.UpdateFacetNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.FacetNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class FacetsTests extends BaseTest {
    static final Logger LOGGER = LoggerFactory.getLogger( FacetsTests.class );

    // Library_FunctionTests lt = new Library_FunctionTests();

    @After
    public void afterEachOfTheseTests() {
        LOGGER.debug( "After ran." );
    }

    @Test
    public void Facets_TLConstructorTests() {

        // Given two libraries, one managed one not managed
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        LibraryNode ln_inChain = ml.createNewLibrary( "http://www.test.com/test1c", "test1c", defaultProject );
        new LibraryChainNode( ln_inChain );
        ln_inChain.setEditable( true );
        assertTrue( "Library must exist.", ln != null );
        assertTrue( "Library must exist.", ln_inChain != null );

        // Given - one of each TLFacet type.
        TLFacet tlf = new TLFacet();
        assertTrue( "Facet must not be null.", tlf != null );
        TLAbstractFacet tlAf = new TLFacet();
        assertTrue( "Facet must not be null.", tlAf != null );
        TLListFacet tlLf = new TLListFacet( tlAf ); // tlaf should be simple or detail.
        assertTrue( "Facet must not be null.", tlLf != null );
        TLSimpleFacet tlSf = new TLSimpleFacet();
        assertTrue( "Facet must not be null.", tlSf != null );
        TLValueWithAttributes tlVWA = new TLValueWithAttributes();
        assertTrue( "Facet must not be null.", tlVWA != null );
        // TLnValueWithAttributesFacet tlVf = new TLnValueWithAttributesFacet(tlVWA);
        // assertTrue("Facet must not be null.", tlVf != null);
    }

    @Test
    public void buildersTests() {
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );

        FacetInterface facetNode1 = FacetNodeBuilder.create( ln ).addElements( "E1", "E2", "E3" ).build();
        FacetInterface facetNode2 = FacetNodeBuilder.create( ln ).addAttributes( "A1", "A2", "A3" ).build();
        FacetInterface facetNode3 =
            FacetNodeBuilder.create( ln ).addIndicators( "I1", "I2" ).addAliases( "Alias1" ).build();
        assertTrue( "Built node 1 must not be null.", facetNode1 != null );
        assertTrue( "Built node 2 must not be null.", facetNode2 != null );
        assertTrue( "Built node 3 must not be null.", facetNode3 != null );
    }

    @Test
    public void Facets_InheritanceTests_v15() {

        // Given - a BO in a library
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        BusinessObjectNode baseBO = ml.addBusinessObjectToLibrary( ln, "BaseBO" );
        AbstractContextualFacet c1 = baseBO.addFacet( "BaseC1", TLFacetType.CUSTOM );
        AttributeNode a1 = new AttributeNode( c1, "cAttr1" );
        // Then - finding c1 facet must work because it is used in children handler
        assertTrue( "Must be able to find c1 by name.", baseBO.findChildByName( c1.getName() ) == c1 );
        // Then - c1 must have children to be inherited.
        assertTrue( "Summary must have children.", !baseBO.getFacet_Summary().getChildren().isEmpty() );
        assertTrue( "Summary must not have inherited children.",
            baseBO.getFacet_Summary().getInheritedChildren().isEmpty() );

        // Given - a second, empty BO to be extended
        BusinessObjectNode extendedBO = ml.addBusinessObjectToLibrary_Empty( ln, "ExBO" );
        new ElementNode( extendedBO.getFacet_Summary(), "ExEle" );

        printListners( baseBO );
        printListners( c1 );
        //
        // When - objects are extended
        extendedBO.setExtension( baseBO );
        assertTrue( "ExtendedBO extends BaseBO.", extendedBO.isExtendedBy( baseBO ) );

        printListners( baseBO );
        printListners( c1 );

        // Then - there must be an inherited facet.
        //
        assertTrue( "Must have inherited child.", !extendedBO.getInheritedChildren().isEmpty() );
        AbstractContextualFacet inheritedCustom = null;
        List<AbstractContextualFacet> customFacets = extendedBO.getContextualFacets( true );
        for (AbstractContextualFacet cf : customFacets) {
            LOGGER.debug( "Does " + c1.getLocalName() + " == " + cf.getLocalName() + "?" );
            if (c1.getLocalName().equals( cf.getLocalName() ))
                inheritedCustom = cf;
        }
        assertTrue( "Must have inherited c1 custom facet.", inheritedCustom != null );
        assertTrue( "InheritedFrom must be the c1 custom facet.", inheritedCustom.getInheritedFrom() == c1 );
        // Then - verify listeners are correct
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

        // Then - 1.5 does not delete the inherited.
        // assertTrue(inheritedCustom.isDeleted());
        // Then - check the new inherited custom.
        for (AbstractContextualFacet cf : extendedBO.getContextualFacets( true ))
            if (c1.getLocalName().equals( cf.getLocalName() ))
                inheritedCustom = cf;
        assertTrue( !inheritedCustom.getName().equals( startingName ) );
        assertTrue( inheritedCustom.getName().contains( newName ) );
        // Then - listener will only be to new custom
        for (ModelElementListener l : c1.getTLModelObject().getListeners())
            if (l instanceof InheritanceDependencyListener)
                assertTrue( ((InheritanceDependencyListener) l).getNode() == inheritedCustom );

        // When - add an attribute
        AttributeNode a2 = new AttributeNode( c1, "cAttr2" );
        Node c1Kid = c1.findChildByName( "cAttr2" );
        assert c1Kid != null;
        // Inherited custom is no longer valid...find it again.
        for (AbstractContextualFacet cf : extendedBO.getContextualFacets( true ))
            if (c1.getLocalName().equals( cf.getLocalName() ))
                inheritedCustom = cf;
        assert !inheritedCustom.isDeleted();

        // Then - inherited custom must have that attr in its inherited children
        Node ia2 = inheritedCustom.findChildByName( a2.getName() );
        List<Node> icInherited = inheritedCustom.getInheritedChildren();
        assertTrue( "Must find attribute.", inheritedCustom.findChildByName( a2.getName() ) != null );
        for (Node n : inheritedCustom.getInheritedChildren())
            if (n.getName().equals( a2.getName() ))
                ia2 = n;
        assertTrue( "Must find ghost node with a2's name.", ia2 != null );

        // When - delete the attribute in c1 (base custom)
        a1.delete();
        // Then - node with name of a1 must not be in inherited children
        assertTrue( "Must not find a1 by name.", inheritedCustom.findChildByName( a1.getName() ) == null );
    }

    private void printListners(Node node) {
        for (ModelElementListener tl : node.getTLModelObject().getListeners())
            if (tl instanceof BaseNodeListener) {
                LOGGER.debug( "Listener on " + node + " of type " + tl.getClass().getSimpleName() + " GetNode() = "
                    + ((BaseNodeListener) tl).getNode() );
                if (((BaseNodeListener) tl).getNode().isDeleted())
                    LOGGER.debug( ((BaseNodeListener) tl).getNode() + " is deleted." );
            }
    }

    @Test
    public void Facets_SortTests() {
        // Given - a BO in a library that has one of each type of property
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        BusinessObjectNode baseBO = ml.addBusinessObjectToLibrary( ln, "BaseBO" );
        assertTrue( baseBO != null );
        assertTrue( baseBO.getFacet_Summary() != null );

        // Given - 3 of each property type in summary facet
        ml.addAllProperties( baseBO.getFacet_Summary() );
        assertTrue( baseBO.getFacet_Summary().getTLModelObject() != null );
        // IndicatorNode i1 = checkIndicatorOrder(baseBO.getFacet_Summary());
        IndicatorNode i1 = null;
        for (Node n : baseBO.getFacet_Summary().getChildren())
            if (n instanceof IndicatorNode) {
                i1 = (IndicatorNode) n;
                break;
            }
        int i1Index = baseBO.getFacet_Summary().getTLModelObject().getIndicators().indexOf( i1.getTLModelObject() );
        // ElementNode e1 = checkElementOrder(baseBO.getFacet_Summary());
        // AttributeNode a1 = checkAttributeOrder(baseBO.getFacet_Summary());

        // When moved down
        i1.moveDown();
        i1.moveDown();
        assertTrue(
            baseBO.getFacet_Summary().getTLModelObject().getIndicators().indexOf( i1.getTLModelObject() ) != i1Index );
        // e1.moveDown();
        // a1.moveDown();

        // When sorted
        baseBO.getFacet_Summary().sort();
        // // Then order is synchronized (order checked by manual inspection)
        // checkOrder(baseBO.getFacet_Summary());
    }

    @Test
    public void Facets_MergeTests() {

        // Given - a BO in a library
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        BusinessObjectNode baseBO = ml.addBusinessObjectToLibrary( ln, "BaseBO" );
        // Given - a second BO
        BusinessObjectNode sourceBO = ml.addBusinessObjectToLibrary( ln, "SourceBO" );
        int originalSize = baseBO.getCustomFacets().size();

        // When - merged
        baseBO.merge( sourceBO );
        // Then - there must be additional custom facets.
        assertTrue( baseBO.getCustomFacets().size() > originalSize );
    }

    @Test
    public void Facets_CopyFacetTests() {
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );

        // Given a facet with 8 mixed properties
        FacetInterface facetNode1 = FacetNodeBuilder.create( ln ).addElements( "E1", "E2", "E3" )
            .addIndicators( "I1", "I2" ).addAttributes( "A1", "A2", "A3" ).build();
        assertTrue( "Starting facet must have 8 properties.", facetNode1.getChildren().size() == 8 );

        // Given - fn is a Summary facet
        TLFacet tlf = new TLFacet();
        tlf.setFacetType( TLFacetType.SUMMARY );
        FacetInterface fn = (FacetInterface) NodeFactory.newChild( null, tlf );
        // Given - an is a VWA Attribute facetj with 1 attribute
        VWA_Node vwa = ml.addVWA_ToLibrary( ln, "myVWA" ); // vwa with one attr
        AttributeFacetNode an = vwa.getFacet_Attributes();

        // When copied
        // fn.copy(facetNode1);
        // an.copy(facetNode1);
        fn.add( facetNode1.getProperties(), true );
        an.add( facetNode1.getProperties(), true );

        // Then both should have 8 properties
        assertTrue( "Facet must have 8 properties.", fn.getChildren().size() == 8 );
        assertTrue( "Attribute facet must have 9 properties.", an.getChildren().size() == 9 );
    }

    @Test
    public void Facets_copyContextualFacet_Tests() {
        // Given libraries loaded from file that contain contextual facets
        // NOTE: Only version 1.6 and later.
        // Note: load order is important if the compiler is to resolve contributors
        // Lib1 has two of each contextual facet type, one contributing to base lib objects
        LibraryNode lib1 = lf.loadFile_Facets1( defaultProject );
        LibraryNode lib2 = lf.loadFile_Facets2( defaultProject ); // one of each
        LibraryNode baseLib = lf.loadFile_FacetBase( defaultProject );
        lib1.setEditable( false );
        lib2.setEditable( false );
        baseLib.setEditable( true );

        // Given - a destination library
        LibraryNode destLib = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        // int destCF_Count = destLib.getDescendants_ContextualFacets().size();

        // When - facets copied from non-editable library
        // Then - destination library should contain a facet with that name
        ContextualFacetNode found = null;
        for (ContextualFacetNode cf : lib1.getDescendants_ContextualFacets()) {
            // if (cf instanceof ContributedFacetNode)
            // continue;
            destLib.copyMember( cf );
            List<ContextualFacetNode> destCFs = destLib.getDescendants_ContextualFacets();
            for (ContextualFacetNode candidate : destLib.getDescendants_ContextualFacets()) {
                // LOGGER.debug("Does " + candidate.getLocalName() + " equal " + cf.getLocalName());
                if (candidate.getLocalName().equals( cf.getLocalName() ))
                    found = candidate;
            }
            assert found != null;
            // Then the copy must be Ok
            checkFacet( found );
            found = null;
        }
    }

    // TODO
    // createPropertyTests()
    // getComponentType

    public void checkAllFacetsInLibrary(LibraryNode lib) {
        // LOGGER.debug("Checking all facets in " + lib);
        for (Node n : lib.getDescendants()) {
            if (n instanceof FacetInterface)
                check( (FacetInterface) n );
        }
    }

    @Test
    public void Facets_editableTests_v16() {
        // Given libraries loaded from file that contain contextual facets
        // NOTE: Only version 1.6 and later.
        // Note: load order is important if the compiler is to resolve contributors
        // Lib1 has two of each contextual facet type, one contributing to base lib objects
        LibraryNode lib1 = lf.loadFile_Facets1( defaultProject );
        LibraryNode lib2 = lf.loadFile_Facets2( defaultProject ); // one of each
        LibraryNode baseLib = lf.loadFile_FacetBase( defaultProject );
        lib1.setEditable( false );
        lib2.setEditable( false );
        baseLib.setEditable( true );
        assert baseLib.isEditable();

        // When - a contextual facet is in editable base library
        for (Node n : baseLib.getDescendants_ContextualFacets())
            // Then contextual (not contributed) facets must be editable
            if (n instanceof ContributedFacetNode)
                assertTrue( !n.isEditable() ); // never edit contributed facets
            else if (n instanceof InheritedInterface)
                assertTrue( !n.isEditable() ); // never edit inherited facets
            else if (!n.isEditable())
                LOGGER.debug( "ERROR " + n.isEditable() );
        // assertTrue(n.isEditable());

        // When - a contextual facet is in non-editable library 1
        // Then - contextual not contributed facets must NOT be editable
        for (Node n : lib1.getDescendants_ContextualFacets())
            if (n instanceof ContributedFacetNode)
                assertTrue( !n.isEditable() ); // never edit contextual facets
            else
                assertTrue( "Must not be editable.", !n.isEditable() );

        // When - a contextual facet is in editable library 1
        lib1.setEditable( true );
        baseLib.setEditable( false );
        // Then - contextual facets must be editable
        for (Node n : lib1.getDescendants_ContextualFacets())
            if (n instanceof ContributedFacetNode)
                assertTrue( !n.isEditable() ); // never edit contextual facets
            else {
                assertTrue( "Must be editable.", n.isEditable() );
                assertTrue( "Must be in Lib1", n.getLibrary() == lib1 );
                // Then - children must also be editable
                for (Node child : n.getChildren()) {
                    if (!(child instanceof ContributedFacetNode)) {
                        assertTrue( "Child must be in facet's library.", child.getLibrary() == n.getLibrary() );
                        assertTrue( "Child must be editable.", child.isEditable() );
                    }
                }
            }
    }

    /**
     * Construct a custom facet and its contributor and verify their relationships.
     */
    @Test
    public void Facets_BuildContributed_v16() {
        // Given - two libraries
        ln = ml.createNewLibrary( pc, "bc" );
        assertTrue( ln.isEditable() );
        LibraryNode ln2 = ml.createNewLibrary_Empty( ln.getNamespace(), "bc2", pc.getDefaultProject() );
        assertTrue( ln2.isEditable() );

        // Given - a business object in the first library
        BusinessObjectNode bo = null;
        for (LibraryMemberInterface n : ln.get_LibraryMembers())
            if (n instanceof BusinessObjectNode)
                bo = (BusinessObjectNode) n;
        bo.getChildrenHandler().clear();

        // Given - a tlContextualFacet contributing to the bo in lib2
        TLContextualFacet tlc = new TLContextualFacet();
        tlc.setFacetType( TLFacetType.CUSTOM );
        tlc.setName( "Custom1" );

        // When - new custom facet node created without TLOwner or TLLibrary set
        CustomFacetNode cf = new CustomFacetNode( tlc );
        // Then -
        assertTrue( cf.getTLModelObject() == tlc );
        assertTrue( "Listener is correct.", Node.GetNode( tlc ) == cf );
        assertTrue( "No where contributed location.", cf.getWhereContributed() == null );

        // When - Add member to library
        ln2.addMember( cf );
        // Then -
        assertTrue( ln2.getDescendants_ContextualFacets().contains( cf ) );
        assertTrue( cf.getParent() instanceof NavNode );
        assertTrue( cf.canBeLibraryMember() );
        assertTrue( cf.getLibrary() == ln2 );
        assertTrue( "No where contributed location.", cf.getWhereContributed() == null );

        // When - owner set on CF
        cf.setOwner( bo );
        // Then -
        assertTrue( cf.getWhereContributed() != null );
        assertTrue( cf.getTLModelObject() == tlc );
        assertTrue( tlc.getOwningEntity() == bo.getTLModelObject() );
        assertTrue( "tlc identity listener must be the contextual facet.", Node.GetNode( tlc ) == cf );
        // Then - contributed component must be correct
        ContributedFacetNode contributed = cf.getWhereContributed();
        LibraryNode ll = contributed.getLibrary(); // ln2
        assertTrue( "Contributor must be set.", contributed.getContributor() == cf );
        assertTrue( "Must return TlContextualFacet", contributed.getTLModelObject() == tlc );
        assertTrue( "Business Object must find contributed facet.", bo.getContributedFacet( tlc ) == contributed );
        assertTrue( "Contributed must have an owning component", contributed.getOwningComponent() == bo );
        assertTrue( "Owner must be parent.", contributed.getParent() == contributed.getOwningComponent() );

        // When - a contextual facet is contributed to a contextual facet
        //
        TLContextualFacet tlc2 = new TLContextualFacet();
        tlc2.setFacetType( TLFacetType.CUSTOM );
        tlc2.setName( "Custom2" );
        CustomFacetNode cf2 = new CustomFacetNode( tlc2 );
        ln2.addMember( cf2 );
        cf2.setOwner( cf );
        // Then - base custom has new custom as child
        assertTrue( "TLC2 must be child of tlc.", cf.getChildrenHandler().getChildren_TL().contains( tlc2 ) );
        // Then - custom facet is correct
        assertTrue( ln2.getDescendants_ContextualFacets().contains( cf2 ) );
        assertTrue( cf2.getParent() instanceof NavNode );
        assertTrue( cf2.canBeLibraryMember() );
        assertTrue( cf2.getLibrary() == ln2 );
        assertTrue( "Must have contributed location.", cf.getWhereContributed() != null );
        // Then - contributed node is correct
        ContributedFacetNode contrib2 = cf2.getWhereContributed();
        assertTrue( "Cf2 must have where contributed.", contrib2 != null );
        assertTrue( "Contrib2 must be child of cf.", cf.getChildren().contains( contrib2 ) );
        assertTrue( "Custom Facet must find contributed facet.", cf.getContributedFacet( tlc2 ) == contrib2 );
        assertTrue( "Contributed must have an owning component", contrib2.getOwningComponent() == cf );
        assertTrue( "Owner must be parent.", contrib2.getParent() == contrib2.getOwningComponent() );

        //
        // Final check - make sure contributed is reported as a child of the bo
        //
        List<Node> kids = bo.getChildren();
        assertTrue( "Business object must have contributed child.", kids.contains( contributed ) );
        LOGGER.debug( "Done" );
    }

    @Test
    public void Facets_BuildContextual_v16() {
        // Given - two libraries
        ln = ml.createNewLibrary( pc, "bc" );
        assertTrue( ln.isEditable() );
        LibraryNode ln2 = ml.createNewLibrary_Empty( ln.getNamespace(), "bc2", pc.getDefaultProject() );
        assertTrue( ln2.isEditable() );

        // Given - a business object in the first library
        BusinessObjectNode bo = null;
        for (LibraryMemberInterface n : ln.get_LibraryMembers())
            if (n instanceof BusinessObjectNode)
                bo = (BusinessObjectNode) n;
        bo.getChildrenHandler().clear();

        // Given - a tlContextualFacet contributing to the bo in lib2
        TLContextualFacet tlc = new TLContextualFacet();
        tlc.setFacetType( TLFacetType.CUSTOM );
        tlc.setName( "Custom1" );
        CustomFacetNode cf = new CustomFacetNode( tlc );
        ln2.addMember( cf );
        cf.setOwner( bo );
        bo.addFacet( "Q1", TLFacetType.QUERY );
        bo.getTLModelObject().addCustomFacet( tlc );
        // ln2.getTLModelObject().addNamedMember(tlc);
        assertTrue( tlc.getName().contains( "Custom1" ) );

        // When - modeled by getting the children of BO
        //
        List<Node> kids = bo.getChildren();
        // Then - contributed node is correct
        ContributedFacetNode contributed = null;
        for (Node n : bo.getChildren())
            if (n instanceof ContributedFacetNode)
                if (n.getName().contains( "Custom1" ))
                    contributed = (ContributedFacetNode) n;
        assertTrue( contributed != null );
        assertTrue( contributed.getLibrary() == ln2 );
        // Then ???
        TLModelElement tl1 = contributed.getTLModelObject();
        Node tln = Node.GetNode( contributed.getTLModelObject() );

        // Then - contributor is in different library
        assertTrue( contributed.getContributor() != null );
        CustomFacetNode custom = (CustomFacetNode) contributed.getContributor();
        assertTrue( custom.getLibrary() == ln2 );
        // Then ???
        TLModelElement tl2 = custom.getTLModelObject();
        Node tlcu = Node.GetNode( custom.getTLModelObject() );

        LOGGER.debug( "TODO" );
        // When - contributor is contributed to
        //
    }

    @Test
    public void Facets_Tests() {
        // Given libraries loaded from file that contain contextual facets
        // NOTE: load order is important if the compiler is to resolve contributors
        lf.loadFile_Facets1( defaultProject );
        lf.loadFile_Facets2( defaultProject );
        LibraryNode baseLib = lf.loadFile_FacetBase( defaultProject );

        // Then libraries must have contextual facets and those facets must have owners.
        for (LibraryNode ln : defaultProject.getLibraries()) {
            List<ContextualFacetNode> facets = getContextualFacets( ln );
            assertTrue( "Must have some contextual facets.", !facets.isEmpty() );
            for (ContextualFacetNode cf : facets) {
                assertTrue( "Must have owner.", cf.getOwningComponent() != null );
            }
        }

        // Then - check contributions to objects in the base library
        //
        checkFacetContents( baseLib, "FacetTestBO", 6 );
        checkFacetContents( baseLib, "ExtFacetTestBO", 3 );
        checkFacetContents( baseLib, "FacetTestChoice", 2 );
        checkFacetContents( baseLib, "ExtFacetTestChoice", 1 );

        // Then extensions must have inherited children
        for (Node n : baseLib.getDescendants_LibraryMembersAsNodes()) {
            if (n instanceof ExtensionOwner)
                if (((ExtensionOwner) n).getExtensionBase() != null) {
                    // Contextual facets should all have inherited properties
                    for (Node cf : n.getChildren())
                        if (cf instanceof ContextualFacetNode) {
                            // List<?> moKids = ((FacetMO) cf.getModelObject()).getInheritedChildren();
                            List<Node> iKids = cf.getInheritedChildren();
                            // LOGGER.debug(cf + " has " + moKids.size() + " inherited model kids and " + iKids.size()
                            // + " inherited kids.");
                            assertTrue( !iKids.isEmpty() );
                            // assertTrue("Must have inherited children.", !cf.getInheritedChildren().isEmpty());
                        }
                }
        }
    }

    private List<ContextualFacetNode> getContextualFacets(Node container) {
        ArrayList<ContextualFacetNode> facets = new ArrayList<>();
        for (Node n : container.getDescendants())
            if (n instanceof ContextualFacetNode)
                facets.add( (ContextualFacetNode) n );
        return facets;
    }

    /**
     * Starting with the node, find descendants with the target name. Those descendants must match the passed counts.
     * 
     * @param node - collection of library members
     * @param targetName - object to test
     * @param contextual number of other facets (contributed)
     */
    private void checkFacetContents(Node node, String targetName, int contextual) {
        for (LibraryMemberInterface n : node.getDescendants_LibraryMembers())
            if (n.getName().equals( targetName )) {
                List<AbstractContextualFacet> facets = null;
                if (n instanceof ContextualFacetOwnerInterface)
                    facets = ((ContextualFacetOwnerInterface) n).getContextualFacets( false );
                assertTrue( targetName + " must have " + contextual + " contextual facets.",
                    facets.size() == contextual );
            }
    }

    @Test
    public void Facets_Constructors_v16() {
        // Given a versioned library
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );

        BusinessObjectNode bo = null;
        assertTrue( "Library must exist.", ln != null );
        for (LibraryMemberInterface n : ln.get_LibraryMembers())
            if (n instanceof BusinessObjectNode)
                bo = (BusinessObjectNode) n;
        assertTrue( "Business Object must exist.", bo != null );

        // Given - a TLContextual facet used to create each of the contextual facets
        TLContextualFacet tlObj = new TLContextualFacet();
        ContextualFacetNode cf = null;
        ContextualFacetNode cfFactory = null;

        // When - created from constructor and factory
        tlObj.setFacetType( TLFacetType.CUSTOM );
        cf = new CustomFacetNode( tlObj );
        cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember( tlObj );
        // Then - objects exist.
        assertTrue( "Must not be null.", cf != null );
        assertTrue( Node.GetNode( tlObj ) == cf );
        assertTrue( "Must not be null.", cfFactory != null );

        // Given - Set TLContextualFacet library and owning entity to fully exercise constructor

        // When - created from constructor and factory
        tlObj = buildTL( bo, TLFacetType.CUSTOM );
        cf = new CustomFacetNode( tlObj );
        cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember( tlObj );
        // Then - objects exist.
        assertTrue( "Must not be null.", cf != null );
        assertTrue( Node.GetNode( tlObj ) == cf );
        assertTrue( "Must not be null.", cfFactory != null );
        assertTrue( "Must know where contributed.", cf.getWhereContributed() != null );

        tlObj = buildTL( bo, TLFacetType.QUERY );
        cf = new QueryFacetNode( tlObj );
        cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember( tlObj );
        assertTrue( Node.GetNode( tlObj ) == cf );
        assertTrue( "Must not be null.", cf != null );
        assertTrue( "Must not be null.", cfFactory != null );

        tlObj = new TLContextualFacet();
        tlObj.setFacetType( TLFacetType.CHOICE );
        cf = new ChoiceFacetNode( tlObj );
        cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember( tlObj );
        assertTrue( "Must not be null.", cf != null );
        assertTrue( "Must not be null.", cfFactory != null );

        tlObj = buildTL( bo, TLFacetType.UPDATE );
        cf = new UpdateFacetNode( tlObj );
        cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember( tlObj );
        assertTrue( "Must not be null.", cf != null );
        assertTrue( "Must not be null.", cfFactory != null );

        List<Node> kids = bo.getChildren();
        assertTrue( !kids.isEmpty() );
    }

    private TLContextualFacet buildTL(BusinessObjectNode owner, TLFacetType type) {
        TLContextualFacet tlObj = new TLContextualFacet();
        tlObj.setFacetType( type );
        switch (type) {
            case CUSTOM:
                owner.getTLModelObject().addCustomFacet( tlObj );
                break;
            case QUERY:
                owner.getTLModelObject().addQueryFacet( tlObj );
                break;
            case UPDATE:
                owner.getTLModelObject().addUpdateFacet( tlObj );
                break;
            default:
                break;

        }
        // tlObj.setOwningEntity(owner.getTLModelObject());
        tlObj.setOwningLibrary( ln.getTLModelObject() );
        assertTrue( tlObj.getOwningLibrary() != null );
        assertTrue( tlObj.getOwningEntity() != null );
        return tlObj;
    }

    @Test
    public void Facets_RoleTests() throws Exception {
        String myNS = "http://local/junits";
        // ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
        ln = LibraryNodeBuilder.create( "Example", myNS, "p", new Version( 1, 1, 0 ) ).build( defaultProject, pc );
        ln.setEditable( true );
        assertTrue( "Library is minor version.", ln.isMinorVersion() );

        CoreObjectNode core = ml.addCoreObjectToLibrary( ln, "Core1" );
        checkFacet( core.getFacet_Role() );
        List<Node> inheritedKids = core.getFacet_Role().getInheritedChildren();
        // TODO - make sure minor version has inherited children
    }

    public void checkFacet(ListFacetNode lf) {
        // LOGGER.debug("Checking List Facet Node: " + lf);

        if (lf.isSimpleListFacet()) {
            // LOGGER.debug("Simple List Facet Node");
            assertTrue( lf.getName().startsWith( lf.getOwningComponent().getName() ) );
            assertTrue( lf.getName().contains( "Simple" ) );
            assertTrue( lf.getLabel().equals( ComponentNodeType.SIMPLE_LIST.getDescription() ) );
        }
        if (lf.isDetailListFacet()) {
            // LOGGER.debug("Detail List Facet Node");
            assertTrue( lf.getName().startsWith( lf.getOwningComponent().getName() ) );
            assertTrue( lf.getName().contains( "Detail" ) );
            assertTrue( lf.getLabel().equals( ComponentNodeType.DETAIL_LIST.getDescription() ) );
        }
        assertFalse( "Must NOT be delete-able.", lf.isDeleteable() );
        assertFalse( "Must NOT be valid parent.", lf.canOwn( PropertyNodeType.ATTRIBUTE ) );
        assertFalse( "Must NOT be valid parent.", lf.canOwn( PropertyNodeType.ELEMENT ) );
        assertFalse( "Must NOT be valid parent.", lf.canOwn( PropertyNodeType.INDICATOR ) );
    }

    public void checkFacet(VWA_SimpleFacetFacadeNode sf) {
        // LOGGER.debug("Checking Simple Facet Facade Node: " + sf);
        assertFalse( "Must NOT be delete-able.", sf.isDeleteable() );

        assertTrue( sf.getTLModelObject() != null );
        assertTrue( sf.getLibrary() == sf.getParent().getLibrary() );

        assertTrue( sf.getSimpleAttribute() instanceof SimpleAttributeFacadeNode );
        if (sf.getSimpleAttribute().getAssignedType() == null)
            LOGGER.debug( "ERROR " + sf.getSimpleAttribute().getAssignedType() );
        assertTrue( sf.getSimpleAttribute().getAssignedType() != null ); // could be empty
    }

    public void checkFacet(CoreSimpleFacetNode sf) {
        // LOGGER.debug("Checking Simple Facet Facade Node: " + sf);
        assertFalse( "Must NOT be delete-able.", sf.isDeleteable() );

        assertTrue( sf.getTLModelObject() != null );
        assertTrue( sf.getOwningComponent() == sf.getParent() );
        assertTrue( sf.getLibrary() == sf.getParent().getLibrary() );

        assertTrue( sf.getSimpleAttribute() instanceof SimpleAttributeFacadeNode );
        if (sf.getSimpleAttribute().getAssignedType() == null)
            LOGGER.debug( "ERROR " + sf.getSimpleAttribute().getAssignedType() );
        assertTrue( sf.getSimpleAttribute().getAssignedType() != null ); // could be empty
    }

    public void checkFacet(AttributeFacetNode afn) {
        // LOGGER.debug("Checking VWA Attribute Facet Node: " + vf);

        assertTrue( "Must be valid parent of attribute.", afn.canOwn( PropertyNodeType.ATTRIBUTE ) );
        assertTrue( "Must be valid parent of indicator.", afn.canOwn( PropertyNodeType.INDICATOR ) );
        assertTrue( "Must be valid parent of id.", afn.canOwn( PropertyNodeType.ID ) );

        assertFalse( "Must NOT be delete-able.", afn.isDeleteable() );
        assertFalse( "Must NOT be type provider.", afn.isNamedEntity() );
        assertFalse( "Must NOT be assignable.", afn.isAssignable() );
        assertFalse( "Must NOT be valid parent of element.", afn.canOwn( PropertyNodeType.ELEMENT ) );
        assertFalse( "Must NOT be valid parent of ID Reference.", afn.canOwn( PropertyNodeType.ID_REFERENCE ) );
        assertFalse( "Must NOT be valid parent of indicator element.",
            afn.canOwn( PropertyNodeType.INDICATOR_ELEMENT ) );

        // Behaviors
        if (afn.getOwningComponent().isEditable()) {
            // Given - a list with a new property
            List<PropertyNode> properties = new ArrayList<>();
            PropertyNode p = new AttributeNode( new TLAttribute(), null );
            p.setName( "attr1" );
            properties.add( p );
            // When - list is added to attribute facet
            afn.add( properties, false );
            // Then
            assertTrue( "Must have new property as child.", afn.getChildren().contains( p ) );
        }
    }

    /**
     * WARNING - ADDS Properties!!!
     * 
     * @param node
     */
    public void check(FacetInterface node) {
        check( node, true );
    }

    public void check(FacetInterface node, boolean validate) {
        if (node instanceof AbstractContextualFacet)
            new ContextualFacetTests().check( (AbstractContextualFacet) node, validate );
        else if (node instanceof ListFacetNode)
            checkFacet( (ListFacetNode) node );
        else if (node instanceof OperationNode)
            checkFacet( (OperationNode) node );
        else if (node instanceof OperationFacetNode)
            checkFacet( (OperationFacetNode) node );
        else if (node instanceof CoreSimpleFacetNode)
            checkFacet( (CoreSimpleFacetNode) node );
        else if (node instanceof AttributeFacetNode)
            checkFacet( (AttributeFacetNode) node );
        else if (node instanceof EnumerationOpenNode)
            new Enumeration_Tests().check( (EnumerationOpenNode) node );
        else if (node instanceof EnumerationClosedNode)
            new Enumeration_Tests().check( (EnumerationClosedNode) node );
        else if (node instanceof RoleFacetNode)
            checkFacet( (RoleFacetNode) node );
        else
            checkBaseFacet( node );
    }

    public void checkFacet(OperationNode sf) {
        // LOGGER.debug("Checking Operation Node: " + sf);
        // assertTrue("Must be delete-able.", sf.isDeleteable());
    }

    public void checkFacet(OperationFacetNode of) {
        // LOGGER.debug("Checking Operation Facet Node: " + of);

        assertTrue( "Must be delete-able.", of.isDeleteable() );
        assertFalse( "Must NOT be type provider.", of.isNamedEntity() );
        assertFalse( "Must NOT be assignable.", of.isAssignable() );
    }

    public void checkBaseFacet(FacetInterface facetNode) {
        assertTrue( "Must have owning component.", facetNode.getOwningComponent() != null );
        assertTrue( "Must have owning library.", ((Node) facetNode).getLibrary() != null );

        if (!facetNode.isInherited()) {
            // Check children
            for (Node property : facetNode.getChildren()) {
                PropertyNodeTest pnTests = new PropertyNodeTest();
                if (facetNode instanceof ContributedFacetNode) {
                    assertTrue( property.getParent() == ((ContributedFacetNode) facetNode).getContributor() );
                    continue;
                }
                if (property instanceof AliasNode)
                    continue;
                if (property instanceof ContributedFacetNode) {
                    assertTrue( property.getParent() == facetNode );
                    continue;
                }
                if (property instanceof ContextualFacetNode) {
                    continue;
                }
                if (property instanceof PropertyNode) {
                    assertTrue( property.getParent() == facetNode );
                    pnTests.check( (PropertyNode) property );
                } else {
                    LOGGER.debug(
                        "ERROR - invalid property type: " + property + " " + property.getClass().getSimpleName() );
                    assertTrue( false );
                }
            }
        }

        assertTrue( "Must be valid parent to attributes.", facetNode.canOwn( PropertyNodeType.ATTRIBUTE ) );
        assertTrue( "Must be valid parent to elements.", facetNode.canOwn( PropertyNodeType.ELEMENT ) );

        if (facetNode.isFacet( TLFacetType.ID ) || facetNode.isFacet( TLFacetType.DETAIL )
            || facetNode.isFacet( TLFacetType.SUMMARY ))
            assertTrue( "Must NOT be delete-able.", !((Node) facetNode).isDeleteable() );

        // Behaviors
        if (((Node) facetNode).isEditable()) {
            TypeProvider simpleType = ml.getXsdString(); // properties must have a type to accept name
            // Cached children are removed often. Use string at end of name to identify
            final String locatingSuffix = "ZZZXCXZ1234554321VCXZZXCV";
            final int StartingCount = facetNode.getChildren().size();

            new AttributeNode( facetNode, "att1x" + locatingSuffix, simpleType );
            assertTrue( "Must be plus 1 size.", facetNode.getChildren().size() == StartingCount + 1 );
            assertTrue( "Must find property by name.", facetNode.get( "att1x" + locatingSuffix ) != null );

            new ElementNode( facetNode, "ele1x" + locatingSuffix, simpleType );
            assertTrue( "Must be plus 2 size.", facetNode.getChildren().size() == StartingCount + 2 );
            assertTrue( "Must find property by name.", facetNode.get( "Ele1x" + locatingSuffix ) != null );

            // Create array of attribute and element properties to add
            List<PropertyNode> properties = new ArrayList<>();
            AttributeNode attr2 = new AttributeNode( new TLAttribute(), null );
            ElementNode ele2 = new ElementNode( new TLProperty(), null );
            attr2.setAssignedType( simpleType );
            attr2.setName( "att2x" + locatingSuffix );
            ele2.setAssignedType( simpleType );
            ele2.setName( "ele2x" + locatingSuffix );
            properties.add( attr2 );
            properties.add( ele2 );
            // When - add using facet method with array param
            facetNode.add( properties, false );
            assertTrue( "Must be plus 4 size.", facetNode.getChildren().size() == StartingCount + 4 );
            assertTrue( "Must find property by name.", facetNode.get( "att2x" + locatingSuffix ) != null );
            assertTrue( "Must find property by name.", facetNode.get( "Ele2x" + locatingSuffix ) != null );

            // Then - get all children and make sure they exist
            // Then remove them - Note - delete will clear fn's children
            int found = 0;
            for (Node child : ((Node) facetNode).getChildren_New()) {
                if (child.getName().endsWith( locatingSuffix )) {
                    found++;
                    Node un = ModelNode.getUnassignedNode();
                    child.delete();
                    assertTrue( "Must be able to delete added child.", !facetNode.contains( child ) );

                    TypeProvider type = ((TypeUser) child).getAssignedType();
                    assertTrue( "Must no longer be in type assigned where used list.",
                        !((TypeUser) child).getAssignedType().getWhereAssigned().contains( child ) );
                    assertTrue( "Must not be in unassigned where used list.",
                        !ModelNode.getUnassignedNode().getWhereAssigned().contains( child ) );
                }
            }
            assert found == 4;

            facetNode.getChildren();
            assertTrue( "Must be same size as when starting.", facetNode.getChildren().size() == StartingCount );
        }
    }

    public void checkFacet(ContextualFacetNode cf) {
        // LOGGER.debug("Checking Contextual Facet: " + rf);
        checkBaseFacet( cf );
        new ContextualFacetTests().check( cf );
    }

    public void checkFacet(RoleFacetNode roleFacetNode) {
        // LOGGER.debug("Checking Facet: " + roleFacetNode);

        assertTrue( "Must be role facet.", roleFacetNode instanceof RoleFacetNode );
        // assertTrue("Must be delete-able.", roleFacet.isDeleteable());
        assertTrue( "Must be assignable.", roleFacetNode.isAssignable() );
        // assertTrue("Must be assignable to complex.", roleFacetNode.isComplexAssignable());
        assertTrue( "Must be type provider.", roleFacetNode.isNamedEntity() );
        assertTrue( "Must be valid parent to roles.", roleFacetNode.canOwn( PropertyNodeType.ROLE ) );
        // FIXME - assertTrue("Must be assignable to VWA.", roleFacet.isAssignableToVWA());

        assertFalse( "Must NOT be assignable to simple.", roleFacetNode.isAssignableToSimple() );
        assertFalse( "Must NOT be assignable to simple.", roleFacetNode.isSimpleAssignable() );
        assertFalse( "Must NOT be valid parent to attributes.", roleFacetNode.canOwn( PropertyNodeType.ATTRIBUTE ) );
        assertFalse( "Must NOT be valid parent to elements.", roleFacetNode.canOwn( PropertyNodeType.ELEMENT ) );
        assertFalse( "Must NOT be renamable.", roleFacetNode.isRenameable() );

        assertFalse( "Must NOT be assignable to element ref", roleFacetNode.isAssignableToElementRef() );
        // assertFalse("Must NOT be default facet.", roleFacetNode.isDefaultFacet());
        assertFalse( "Must NOT be named type.", !roleFacetNode.isNamedEntity() );

        // Behaviors
        RoleNode role = new RoleNode( roleFacetNode, "newRole1" );
        assertTrue( "Must be able to add roles.", role.getParent() == roleFacetNode );
        assertTrue( "Must be able to add child.", roleFacetNode.getChildren().contains( role ) );
        role.delete();
    }
}

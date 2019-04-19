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

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.FacetNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
// TODO - test deleting the source and the clone with full visit node afterwards
public class Clone_Tests extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger( Clone_Tests.class );

    NodeTesters tt = new NodeTesters();
    SimpleTypeNode builtin = null;

    @Before
    public void beforeEachOfTheseTests() {
        ln = ml.createNewLibrary( "http://example.com/test", "test", defaultProject );
        ln.setEditable( true );
        builtin = (SimpleTypeNode) NodeFinders.findNodeByName( "date", ModelNode.XSD_NAMESPACE );
    }

    @Test
    public void CL_cloneContextualFacetOwners_v16_Tests() {
        ContextualFacetOwnerInterface cfo = null;
        ContextualFacetOwnerInterface newCfo = null;
        LibraryElement newTL = null;
        ContextualFacetOwnerInterface clone = null;

        // Given a destination library
        LibraryNode destLib = ml.createNewLibrary( "http;//example.com/test2", "dest", defaultProject );
        assert destLib.isEditable();

        // Given - a Business Object
        cfo = ml.addBusinessObjectToLibrary( ln, "B1" );
        List<TLContextualFacet> srcTLFacets = ((TLBusinessObject) cfo.getTLModelObject()).getCustomFacets();
        List<TLContextualFacet> libFacets = new ArrayList<>();
        for (LibraryMember lm : ln.getTLModelObject().getNamedMembers())
            if (lm instanceof TLContextualFacet)
                libFacets.add( (TLContextualFacet) lm );

        // When - cloned
        newTL = ((Node) cfo).cloneTLObj(); // no owning library, different contextual facets
        // Then clone is not modeled and facets are correct
        assertTrue( "Must not have owning library.", newTL.getOwningLibrary() == null );
        for (TLContextualFacet tlcf : ((TLBusinessObject) newTL).getCustomFacets()) {
            assertTrue( "Must be a new facet.", !srcTLFacets.contains( tlcf ) );
            assertTrue( "Must not have identity listener.", tlcf.getListeners().isEmpty() );
            assertTrue( "Must have correct owner.", tlcf.getOwningEntity() == newTL );
            assertTrue( "Must be in same library as parent.", tlcf.getOwningLibrary() == newTL.getOwningLibrary() );
            assertTrue( "Must be unique.", !libFacets.contains( tlcf ) );
        }
        // When - modeled in the factory
        clone = (ContextualFacetOwnerInterface) NodeFactory.newLibraryMember( (LibraryMember) newTL );
        // Then - the contextual facets are created correctly
        for (AbstractContextualFacet cf : clone.getContextualFacets()) {
            // assert !(cf instanceof ContributedFacetNode);
            // Not modeled until added to a library
            // assertTrue("Identity listener must be correct.", cf == Node.GetNode(cf.getTLModelObject()));
            // assertTrue("Must have correct parent.", cf.getParent() == clone);
            // assertTrue("Must have same library as parent.", cf.getLibrary() == clone.getLibrary());
        }
        // When - added to destination library
        destLib.addMember( (LibraryMemberInterface) clone );
        // Then - the contextual facets are still correct
        for (AbstractContextualFacet cf : clone.getContextualFacets()) {
            // assert !(cf instanceof ContributedFacetNode);
            assertTrue( "Identity listener must be correct.", cf == Node.GetNode( cf.getTLModelObject() ) );
            assertTrue( "Must have correct parent.", cf.getParent() == ((Node) clone).getParent() );
            assertTrue( "Must have same library as parent.", cf.getLibrary() == clone.getLibrary() );
            assertTrue( "Facet must be in same library as tl facet",
                cf.getTLModelObject().getOwningLibrary() == cf.getLibrary().getTLModelObject() );
        }
        ml.check( (Node) clone, true );

        //
        // Given - Choice Object
        cfo = ml.addChoice( ln, "C1" );
        srcTLFacets = ((TLChoiceObject) cfo.getTLModelObject()).getChoiceFacets();
        libFacets = new ArrayList<>();
        for (LibraryMember lm : ln.getTLModelObject().getNamedMembers())
            if (lm instanceof TLContextualFacet)
                libFacets.add( (TLContextualFacet) lm );

        // When - cloned
        newTL = ((Node) cfo).cloneTLObj();
        // Then clone is not modeled and facets are correct
        assertTrue( "Must not have owning library.", newTL.getOwningLibrary() == null );
        for (TLContextualFacet tlcf : ((TLChoiceObject) newTL).getChoiceFacets()) {
            assertTrue( "Must be a new facet.", !srcTLFacets.contains( tlcf ) );
            assertTrue( "Must not have identity listener.", tlcf.getListeners().isEmpty() );
            assertTrue( "Must have correct owner.", tlcf.getOwningEntity() == newTL );
            assertTrue( "Must be in same library as parent.", tlcf.getOwningLibrary() == newTL.getOwningLibrary() );
            assertTrue( "Must be unique.", !libFacets.contains( tlcf ) );
        }

        // When - modeled in the factory
        clone = (ContextualFacetOwnerInterface) NodeFactory.newLibraryMember( (LibraryMember) newTL );
        // Then - the contextual facets are created correctly
        for (AbstractContextualFacet cf : ((ChoiceObjectNode) clone).getContextualFacets()) {
            // assert !(cf instanceof ContributedFacetNode); // this is v 1.5
            // assertTrue("Identity listener must be correct.", cf == Node.GetNode(cf.getTLModelObject()));
            // assertTrue("Must have correct parent.", cf.getParent() == clone);
            // assertTrue("Must have same library as parent.", cf.getLibrary() == clone.getLibrary());
        }

        // When - added to destination library
        destLib.addMember( (LibraryMemberInterface) clone );
        // Then - the contextual facets are still correct
        for (AbstractContextualFacet cf : ((ChoiceObjectNode) clone).getContextualFacets()) {
            // assert !(cf instanceof ContributedFacetNode);
            assertTrue( "Identity listener must be correct.", cf == Node.GetNode( cf.getTLModelObject() ) );
            assertTrue( "Must have correct parent.", cf.getParent() == ((Node) clone).getParent() );
            assertTrue( "Must have same library as parent.", cf.getLibrary() == clone.getLibrary() );
            assertTrue( "Facet must be in same library as tl facet",
                cf.getTLModelObject().getOwningLibrary() == cf.getLibrary().getTLModelObject() );
        }

        ml.check( (Node) clone, true );
        // Contextual facet
    }

    @Test
    public void shouldCloneTLObjects() {
        // the first step in cloning is cloning the TL Object. This is a facade for the TL model cloneElement()
        FacetProviderNode facet =
            FacetNodeBuilder.create( ln ).addElements( "E1" ).addAttributes( "A1" ).addIndicators( "I1" ).build();
        // TODO - Add these to FacetNodeBuilder
        new IdNode( facet, "Id" );
        new ElementReferenceNode( facet );
        new IndicatorElementNode( facet, "indicatorElement" );
        assert facet.getChildren().size() == 6;

        // Check each property as they are cloned. Clones have no owner.
        List<PropertyNode> kids = new ArrayList<>( facet.getProperties() ); // list get added to by clone
        for (PropertyNode n : kids) {
            if (n instanceof TypeUser)
                ((TypeUser) n).setAssignedType( builtin );
            LibraryElement clone = n.cloneTLObj();
            assert clone != null;
            if (clone instanceof TLProperty) {
                // elements are of type TLPropery
                if (((TLProperty) clone).getName() != null)
                    assert ((TLProperty) clone).getName().equals( n.getName() );
                if (((TLProperty) clone).getType() != null)
                    assert ((TLProperty) clone).getType().equals( ((TypeUser) n).getAssignedTLObject() );
                assert ((TLProperty) clone).getOwner() == null;
            }
        }

        // TODO - test cloning non-properties, GUI only uses for properties
        // Assert clones exist, has correct type and builtin count is larger
        LOGGER.debug( "Done" );
    }

    @Test
    public void shouldCloneElements() {
        FacetProviderNode facet = FacetNodeBuilder.create( ln ).addElements( "E1", "E2", "E3" ).build();
        int assignedCount = builtin.getWhereAssignedCount();

        // Given 3 elements were cloned
        List<Node> kids = new ArrayList<>( facet.getChildren() ); // list get added to by clone
        for (Node n : kids) {
            assert n instanceof TypeUser;
            ((TypeUser) n).setAssignedType( builtin );
            // TypeUser clone = (TypeUser) n.clone();
            TypeUser clone = (TypeUser) n.clone( "_Clone" );
            // TypeUser clone = (TypeUser) n.clone(facet, "Clone");
            // CHECK - listener on the clone tl is NOT original element
            assert Node.GetNode( clone.getTLModelObject() ) == clone;
            assert Node.GetNode( clone.getTLModelObject() ) != n;
        }

        // Assert clones exist, has correct type and builtin count is larger
        assert facet.getChildren().size() == 6;
        assert builtin.getWhereAssignedCount() == assignedCount + 6;
        for (Node n : facet.getChildren())
            assert ((TypeUser) n).getAssignedType() == builtin;
        LOGGER.debug( "Done" );
    }

    @Test
    public void shouldCloneAttributes() {
        FacetProviderNode facet = FacetNodeBuilder.create( ln ).addAttributes( "A1", "A2", "A3" ).build();
        int assignedCount = builtin.getWhereAssignedCount();

        // Given 3 elements were cloned
        List<Node> kids = new ArrayList<>( facet.getChildren() ); // list get added to by clone
        for (Node n : kids) {
            assert n instanceof TypeUser;
            ((TypeUser) n).setAssignedType( builtin );
            // TypeUser clone = (TypeUser) n.clone();
            TypeUser clone = (TypeUser) n.clone( "_Clone" );
            // TypeUser clone = (TypeUser) n.clone(facet, "Clone");
            // CHECK - listener on the clone tl is NOT original element
            assert Node.GetNode( clone.getTLModelObject() ) == clone;
            assert Node.GetNode( clone.getTLModelObject() ) != n;
        }

        // Assert clones exist, has correct type and builtin count is larger
        assert facet.getChildren().size() == 6;
        assert builtin.getWhereAssignedCount() == assignedCount + 6;
        for (Node n : facet.getChildren())
            assert ((TypeUser) n).getAssignedType() == builtin;
        LOGGER.debug( "Done" );
    }

    @Test
    public void shouldCloneAllPropertyTypes() {
        FacetInterface facet = ml.addBusinessObjectToLibrary( ln, "bo" ).getFacet_Summary();

        ml.addAllProperties( facet );
        int size = facet.getChildren().size();

        List<Node> kids = new ArrayList<>( facet.getChildren() ); // list get added to by clone
        for (Node n : kids)
            n.clone( "_Clone" );

        assert facet.getChildren().size() == size * 2;
    }

    // No longer true - clone is safe with null parent
    // @Test
    // public void shouldFailPreTests() {
    // FacetProviderNode facet = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").build();
    // // Node kid = facet.getChildren().get(0);
    // PropertyNode kid = facet.getProperties().get(0);
    // // ln.remove(kid); // leaves library and parent set
    // // kid.setLibrary(null);
    // kid.setParent(null);
    // Node clone = kid.clone(null, null);
    // assert clone == null;
    // }

    // 1/8/2018 - runs green when run alone.
    @Test
    public void cloneTest() throws Exception {
        MainController mc = OtmRegistry.getMainController();
        LoadFiles lf = new LoadFiles();
        // model = mc.getModelNode();

        // Given - Test File 5 (clean) in a chain
        // LibraryNode srcLib = lf.loadFile5Clean(mc); // not valid
        LibraryNode srcLib = lf.loadFile1( mc );
        new LibraryChainNode( srcLib );
        srcLib.setEditable( true );
        assertTrue( srcLib.isEditable() );
        ml.check( srcLib, false );

        // When - all library members are cloned into this library
        for (Node ne : srcLib.getDescendants_LibraryMembersAsNodes()) {
            Node clone = ne.clone( "c" );
            // Objects that can't be cloned return null.
            if (clone != null)
                ml.check( clone );
            else {
                LOGGER.debug( "Could not clone " + ne );
                ne.clone( "CC" );
            }
        }

        // Then - library is still valid
        ml.check( srcLib, false );

        // Given - a different library also in a chain
        LibraryNode destLib = ml.createNewLibrary( pc, "DestLib" );
        new LibraryChainNode( destLib );
        destLib.setEditable( true );
        assertTrue( destLib.isEditable() );

        // When - all library members are cloned into dest lib
        for (LibraryMemberInterface ne : srcLib.getDescendants_LibraryMembers()) {
            LOGGER.debug( "Cloning " + ne );
            ml.check( (Node) ne, false );
            LibraryMemberInterface clone = ne.clone( destLib, "d" );
            if (clone != null) {
                // Not all library members are clone-able.
                assertTrue( destLib.contains( (Node) clone ) );
                ml.check( (Node) clone, false );
                ml.check( (Node) ne, false );
            }
        }

        // Then - both libraries are valid
        ml.check( destLib, false );
        ml.check( srcLib, false );

        // //------------------------------------------
        // // Test cloning to the same library.
        // //
        // LibraryNode source = lf.loadFile5Clean(mc);
        // new LibraryChainNode(source); // Test in a chain
        // // test cloning within library.
        // source.setEditable(true);
        // Node.getModelNode().visitAllNodes(tt.new TestNode());
        // cloneMembers(source, source);
        //
        // LOGGER.debug("Testing cloning properties.");
        // for (Node ne : source.getDescendants_LibraryMembers())
        // cloneProperties(ne);
        // tt.visitAllNodes(source);
        // Node.getModelNode().visitAllNodes(tt.new TestNode());
        //
        // // Test cloning to a different library
        // //
        // // commented some libs out to keep the total time down
        // LibraryNode target = lf.loadFile1(mc);
        // new LibraryChainNode(target); // Test in a chain
        // Node.getModelNode().visitAllNodes(tt.new TestNode());
        // lf.loadTestGroupA(mc);
        //
        // lf.cleanModel();
        // Node.getModelNode().visitAllNodes(tt.new TestNode());
        //
        // LOGGER.debug("\n");
        // LOGGER.debug("Testing cloning to new library.");
        // for (LibraryNode ln : Node.getAllLibraries()) {
        // if (ln.getNamespace().equals(target.getNamespace()))
        // continue;
        // if (ln.isBuiltIn())
        // continue; // these have errors
        // ln.setEditable(true);
        // cloneMembers(ln, target);
        // LOGGER.debug("Cloned members of " + ln);
        // }
        // LOGGER.debug("Done cloning - starting final check.");
        // Node.getModelNode().visitAllNodes(tt.new TestNode());
    }

    private int cloneMembers(LibraryNode ln, LibraryNode target) {
        int mbrCount = 0, equCount = 0;
        Node clone;

        for (Node n : ln.getDescendants_LibraryMembersAsNodes()) {
            // Assert.assertNotNull(n.cloneNew(null)); // no library, so it will fail node tests
            equCount = countEquivelents( n );
            if (n instanceof ServiceNode)
                continue;
            if (ln == target)
                clone = n.clone( "_COPY" );
            else
                clone = n.clone( target, null );
            if (clone != null) {
                tt.visitAllNodes( clone );
                if (countEquivelents( clone ) != equCount)
                    LOGGER.debug( "Equ error on " + clone );
            }
            mbrCount++;
        }
        return mbrCount;
    }

    private int countEquivelents(Node n) {
        for (Node p : n.getDescendants()) {
            if (p instanceof ElementNode) {
                return ((TLProperty) p.getTLModelObject()).getEquivalents().size();
            }
        }
        return 0;
    }

}

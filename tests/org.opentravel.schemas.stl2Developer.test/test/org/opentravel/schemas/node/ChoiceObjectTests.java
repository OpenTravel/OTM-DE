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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.testUtils.LoadFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class ChoiceObjectTests extends BaseTest {

    public TLChoiceObject buildTL(String name) {
        TLChoiceObject tlc = new TLChoiceObject();
        if (name == null || name.isEmpty())
            name = "TestChoice";
        tlc.setName( name );

        TLContextualFacet tlf = new TLContextualFacet();
        tlf.setFacetType( TLFacetType.CHOICE );
        tlf.setName( "CF" + name + "1" ); // Name that will not be ignored when inherited
        tlc.addChoiceFacet( tlf );

        tlf = new TLContextualFacet();
        tlf.setFacetType( TLFacetType.CHOICE );
        tlf.setName( "CF" + name + "2" );
        tlc.addChoiceFacet( tlf );

        return tlc;
    }

    @Test
    public void CH_ConstructorTests() {
        LibraryNode ln = ml.createNewLibrary( "http://example.com/choice", "CT", pc.getDefaultProject() );

        ChoiceObjectNode cn = ml.addChoice( ln, "ChoiceTest1" );

        check( cn, true );
    }

    @Test
    public void CH_GetFacetsTests() {
        LibraryNode ln2 = ml.createNewLibrary( "http://example.com/choice", "CT", pc.getDefaultProject() );
        ChoiceObjectNode c1 = ml.addChoice( ln2, "Choice" );

        assertTrue( "Must have shared facet.", c1.getFacet_Shared() != null );

        int cfCnt = c1.getChoiceFacets().size();
        c1.addFacet( "cf1" );
        c1.addFacet( "cf2" );
        int cfCnt2 = c1.getChoiceFacets().size();
        assertTrue( "Must have two more choice facets.", c1.getChoiceFacets().size() == cfCnt + 2 );
    }

    @Test
    public void CH_FileReadTest() throws Exception {
        LibraryNode testLib = new LoadFiles().loadFile6( mc );
        LibraryNode ln2 = ml.createNewLibrary( "http://example.com/choice", "CT", pc.getDefaultProject() );
        ChoiceObjectNode extendedChoice = ml.addChoice( ln2, "ExtendedChoice" );

        new LibraryChainNode( testLib ); // Test in a chain

        for (LibraryMemberInterface choice : testLib.getDescendants_LibraryMembers()) {
            if (choice instanceof ChoiceObjectNode) {
                check( (ChoiceObjectNode) choice, true );

                extendedChoice.setExtension( (Node) choice );
                check( (ChoiceObjectNode) choice, true );
                check( extendedChoice, true );
            }
        }
    }

    @Test
    public void CH_ExtensionTests() {
        // Given the choice test file with 2 choice objects
        LibraryNode ln = new LoadFiles().loadFile_Choice( defaultProject );
        // new LibraryChainNode(ln); // Test in a chain
        ml.check( ln );

        // Given - find each choice object, one extends the other
        ChoiceObjectNode choice = null;
        ChoiceObjectNode extChoice = null;
        for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
            if (n instanceof ChoiceObjectNode) {
                if (((ChoiceObjectNode) n).getExtensionBase() == null)
                    choice = (ChoiceObjectNode) n;
                else
                    extChoice = (ChoiceObjectNode) n;
            }
        List<AbstractContextualFacet> exFacets = extChoice.getChoiceFacets( true );
        List<AbstractContextualFacet> exFacets2 = extChoice.getContextualFacets( false ); // 2
        List<Node> exFacets3 = extChoice.getInheritedChildren(); // 2
        assertTrue( "Must have base choice object.", choice != null );
        assertTrue( "Choice must have 2 contextual facets.", choice.getContextualFacets( false ).size() == 2 );
        assertTrue( "Must have extended choice object.", extChoice != null );
        assertTrue( "Extended choice must have 2 contextual facets.",
            extChoice.getContextualFacets( false ).size() == 2 );
        assertTrue( "Extended choice must have 2 inherited facets.", extChoice.getInheritedChildren().size() == 2 );

        // Given - the choice extension should work exactly like business object.
        BusinessObjectNode bo = null;
        BusinessObjectNode exBo = null;
        for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
            if (n instanceof BusinessObjectNode) {
                if (((BusinessObjectNode) n).getExtensionBase() == null)
                    bo = (BusinessObjectNode) n;
                else
                    exBo = (BusinessObjectNode) n;
            }
        assertTrue( "Must have base business object.", bo != null );
        assertTrue( "BO must have 2 contextual facets.", bo.getContextualFacets( false ).size() == 2 );
        assertTrue( "Must have extended business object.", exBo != null );
        assertTrue( "Extended BO must have 2 contextual facets.", exBo.getContextualFacets( false ).size() == 2 );
        assertTrue( "Extended BO must have 2 inherited facets.", exBo.getInheritedChildren().size() == 2 );
    }

    @Test
    public void ChoiceFacetsTests() {
        ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );
        // new LibraryChainNode(ln); // Test in a chain

        // Given 3 choice groups
        ChoiceObjectNode ch1 = ml.addChoice( ln, "Ch1" );
        int baseCount = ch1.getChoiceFacets().size();
        ChoiceObjectNode ch2 = new ChoiceObjectNode( new TLChoiceObject() );
        ch2.setName( "Ch2" );
        ln.addMember( ch2 );
        ch2.addFacet( "Ch2CF1" );
        ChoiceObjectNode ch3 = new ChoiceObjectNode( new TLChoiceObject() );
        ch3.setName( "Ch3" );
        ln.addMember( ch3 );

        // When extended
        ch2.setExtension( ch1 );
        ch3.setExtension( ch2 );

        // Then
        assertTrue( "Ch1 must be extended by ch2.", ch1.getWhereExtendedHandler().getWhereExtended().contains( ch2 ) );
        assertTrue( "Ch2 must extend ch1.", ch2.getExtensionBase() == ch1 );
        assertTrue( "Ch2 must have 2 children.", ch2.getChildren().size() == 2 );
        assertTrue( "Ch2 shared facet must NOT have any children.", ch2.getFacet_Shared().getChildren().isEmpty() );
        assertTrue( "Ch3 must extend ch2.", ch3.getExtensionBase() == ch2 );
        assertTrue( "Ch3 must have 1 child.", ch3.getChildren().size() == 1 );
        assertTrue( "Ch3 shared facet must NOT have any children.", ch3.getFacet_Shared().getChildren().isEmpty() );

        // Then - look for ghost facets from TL Model
        List<TLContextualFacet> inf2 = FacetCodegenUtils.findGhostFacets( ch2.getTLModelObject(), TLFacetType.CHOICE );
        List<TLContextualFacet> inf3 = FacetCodegenUtils.findGhostFacets( ch3.getTLModelObject(), TLFacetType.CHOICE );
        assertTrue( "Ch2 must have 2 ghosts.", inf2.size() == 2 );
        assertTrue( "Ch3 must have 3 ghosts.", inf3.size() == 3 );

        // FIXME
        // When - the inherited children are initialized
        // ch2.initInheritedChildren(); // not needed - inherited children are initialized on get()
        // ch3.initInheritedChildren();
        // Then - children remain unchanged.
        assertTrue( "Ch2 must have 2 children.", ch2.getChildren().size() == 2 );
        assertTrue( "Ch3 must have 1 child.", ch3.getChildren().size() == 1 );
        // Then - inherited children are present.
        assertTrue( "Ch2 must inherit base choice facets.", ch2.getInheritedChildren().size() == baseCount );
        assertTrue( "Ch3 must inherit base and c2 choice facets.",
            ch3.getInheritedChildren().size() == baseCount + ch2.getChoiceFacets( false ).size() );
        // Then - the inherited tree filter depends on isInherited.
        for (Node n : ch3.getInheritedChildren())
            assertTrue( "Must be inherited.", n.isInherited() );

        //
        // When - adding and deleting facets to base types
        //
        // Given starting inherited count.
        int ch3Count = ch3.getInheritedChildren().size();
        // When
        // FIXME - ch2 does not have inheritance listener and did not clear inherited children from ch3
        FacetProviderNode ch2cf2 = ch2.addFacet( "Ch2CF2" );
        ch3Count++;
        // Then
        List<Node> ch3Inherited = ch3.getInheritedChildren();
        assertTrue( "Ch3 must have 1 more inherited child.", ch3Inherited.size() == ch3Count );
        // NO - inherited children are new nodes.
        // assertTrue("Ch3 must have ch2cf2 as inherited child.", ch3Inherited.contains(ch2cf2));

        // When
        FacetProviderNode ch1cf3 = ch1.addFacet( "Ch1CF3" );
        ch3Count++;
        // Then
        assertTrue( "Ch3 must have 1 more inherited child.", ch3.getInheritedChildren().size() == ch3Count );

        // When deleted
        ch2cf2.delete();
        ch3Count--;
        assertTrue( "Ch2 must not have deleted facet.", !ch2.getChoiceFacets().contains( ch2cf2 ) );
        assertTrue( "Ch3 must have 1 less inherited child.", ch3.getInheritedChildren().size() == ch3Count );

        ch1cf3.delete();
        ch3Count--;
        assertTrue( "Ch3 must have 1 less inherited child.", ch3.getInheritedChildren().size() == ch3Count );

    }

    @Test
    public void CH_ImportAndCopyTests() {
        // Given - an editable, versioned source library
        LibraryChainNode srcLCN =
            ml.createNewManagedLibrary_Empty( defaultProject.getNSRoot() + "src", "SrcLib", defaultProject );
        LibraryNode srcLib = srcLCN.getHead();
        assertTrue( srcLib.isEditable() );
        // Given - an editable, versioned destination library in different namespace
        LibraryChainNode destLCN = ml.createNewManagedLibrary_Empty( defaultProject.getNSRoot() + "dest" + "/Dest",
            "DestLib", defaultProject );
        LibraryNode destLib = destLCN.getHead();
        assertTrue( destLib != srcLib );
        assertTrue( destLib.isEditable() );
        assertTrue( !srcLib.getNamespace().equals( destLib.getNamespace() ) );

        // Given choice objects
        ChoiceObjectNode ch0 = ml.addChoice( srcLib, "Ch0" );
        ml.check( ch0 );
        ChoiceObjectNode ch1 = ml.addChoice( srcLib, "Ch1" );
        ChoiceObjectNode ch2 = ml.addChoice( srcLib, "Ch2" );
        ch2.addFacet( "Ch2CF3" );
        ChoiceObjectNode ch3 = ml.addChoice( srcLib, "Ch3" );
        ChoiceObjectNode ch4 = ml.addChoice( srcLib, "Ch4" );
        ChoiceObjectNode ch5 = ml.addChoice( srcLib, "Ch5" );
        ml.check( srcLib ); // checks all members

        // TODO - test case for just importing contextual facets

        //
        // When - cloned as used within LibraryNode.importNode()
        LibraryElement tlResult = ch0.cloneTLObj();
        int srcLibSize = srcLib.get_LibraryMembers().size();
        int destLibSize = destLib.get_LibraryMembers().size();
        ChoiceObjectNode newNode = (ChoiceObjectNode) NodeFactory.newLibraryMember( (LibraryMember) tlResult );
        // Then - The contextual facets are only linked to by the tlListeners until added to a library.
        assertTrue( "New node and facets are not in library.", srcLibSize == srcLib.get_LibraryMembers().size() );
        assertTrue( "New node and facets are not in library.", destLibSize == destLib.get_LibraryMembers().size() );
        // Note - new node's contributed's contributor is not set until added to library
        // When - added to destLib
        destLib.addMember( newNode );
        assertTrue( srcLibSize == srcLib.get_LibraryMembers().size() );
        assertTrue( "adding to library also adds the contextual facets",
            destLibSize + 3 == destLib.get_LibraryMembers().size() );
        // Then - there must be same number of contributed facets
        assertTrue( ch0.getChoiceFacets().size() == newNode.getChoiceFacets().size() );

        ml.check( newNode, true );

        // Then - the contextual facets must be in the source library - importNode will move them
        for (AbstractContextualFacet cf : newNode.getContextualFacets( false )) {
            assertTrue( "Facet must be in destLib.", destLib.contains( cf ) );
            assertTrue( "Facet must NOT be in srcLib.", !srcLib.contains( cf ) );
            assertTrue( "Facet must NOT be in source choice.", !ch0.getContextualFacets( false ).contains( cf ) );
        }

        // Then - result will validate
        ml.check( destLib, true );

        //
        // When - LibraryNode.importNode() is run
        newNode = (ChoiceObjectNode) destLib.importNode( ch4 );

        // Then - there must be same number of contributed facets
        assertTrue( ch4.getChoiceFacets().size() == newNode.getChoiceFacets().size() );

        // Then - the contextual facets must be in the source library - importNode will move them
        for (AbstractContextualFacet cf : newNode.getContextualFacets( true )) {
            assertTrue( "Facet must be in destLib.", destLib.contains( cf ) );
            assertTrue( "Facet must be in destination choice.", newNode.getContextualFacets( true ).contains( cf ) );
            assertTrue( "Facet must NOT be in srcLib.", !srcLib.contains( cf ) );
            assertTrue( "Facet must NOT be in source choice.", !ch4.getContextualFacets( true ).contains( cf ) );
        }
        // Then - result will validate
        ml.check( newNode, true );

        // When - imported but the source lib is not editable
        srcLib.setEditable( false );
        newNode = (ChoiceObjectNode) destLib.importNode( ch5 );
        // Then - imported choice valid
        check( newNode, true );
        ml.check( srcLib );

        List<Node> nodeList = new ArrayList<>();

        // When - ImportObjectToLibraryAction - case 1
        nodeList.add( ch1 );
        destLib.importNodes( nodeList );
        // When - ImportObjectToLibraryAction - case 2
        nodeList.clear();
        nodeList.add( ch2 );
        destLib.importNodes( nodeList, true );
        // When - ImportObjectToLibraryAction - case 3
        nodeList.clear();
        nodeList.add( ch3 );
        destLib.importNodes( nodeList, false );

        ml.check( destLib );
        ml.check( srcLib );
    }

    // private List<ContextualFacetNode> getContextualFacets(Node container) {
    // ArrayList<ContextualFacetNode> facets = new ArrayList<ContextualFacetNode>();
    // for (Node n : container.getDescendants())
    // if (n instanceof ContextualFacetNode)
    // facets.add((ContextualFacetNode) n);
    // return facets;
    // }

    public void check(ChoiceObjectNode choice, boolean validate) {
        assertTrue( choice instanceof ChoiceObjectNode );

        // Validate model and tl object
        assertTrue( choice.getTLModelObject() instanceof TLChoiceObject );
        assertNotNull( choice.getTLModelObject().getListeners() );
        TLChoiceObject tlChoice = choice.getTLModelObject();

        if (tlChoice.getOwningLibrary() != null)
            Assert.assertNotNull( choice.getLibrary() );
        String s = tlChoice.getName();

        // must have shared facet
        assertNotNull( choice.getFacet_Shared() );
        s = choice.getFacet_Shared().getName();
        s = choice.getFacet_Shared().getLabel();

        // make sure this does not NPE
        List<AbstractContextualFacet> choices = choice.getChoiceFacets();
        // can be empty - assertTrue(!choices.isEmpty());

        // For choice facets the Name and label should be not empty
        for (AbstractContextualFacet poi : choice.getChoiceFacets()) {
            assertTrue( poi instanceof FacetProviderNode );
            FacetProviderNode f = poi;
            String name = f.getName();
            assertFalse( name.isEmpty() );
            String label = f.getLabel();
            assertFalse( f.getLabel().isEmpty() );
            // if (poi instanceof ContributedFacetNode)
            // assertTrue(((Node) poi).getParent() == choice);
        }

        // Does this extend another choice? If so, examine inherited children
        boolean hasBaseClass = choice.getExtensionBase() != null;
        if (hasBaseClass) {
            Node baseClass = choice.getExtensionBase();
            // Test File 6 has an extended choice - make sure it inherits correctly.
            if (choice.getName().equals( "ExtendedChoice" )) {
                for (Node n : choice.getChildren())
                    if (n instanceof FacetOMNode) {
                        assertTrue( n.getParent() != null );

                        List<TLAttribute> tlAttrs =
                            PropertyCodegenUtils.getInheritedFacetAttributes( (TLFacet) n.getTLModelObject() );
                        List<Node> inheritedList = n.getInheritedChildren();
                        if (inheritedList.isEmpty()) {
                            List<Node> x = n.getInheritedChildren();
                        }

                        // assert !inheritedList.isEmpty();
                        // assert inheritedList.size() == 3;
                    }
            }
            assertNotNull( baseClass );
        }

        // Check all the children
        for (Node n : choice.getChildren()) {
            ml.check( n, validate );
            assertTrue( !(n instanceof VersionNode) );
        }

        //
        // TODO - add test case where a new facet is added to the extension
        //

        // Get Equivalent
        // Get Aliases
        assertNotNull( choice.getAliases() );

    }
}

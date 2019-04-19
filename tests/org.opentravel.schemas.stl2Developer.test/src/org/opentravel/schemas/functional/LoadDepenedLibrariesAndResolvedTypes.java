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

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.CustomFacetNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.BaseRepositoryTest;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Pawel Jedruch / Dave Hollander
 * 
 */
public class LoadDepenedLibrariesAndResolvedTypes extends BaseRepositoryTest {

    private ProjectNode uploadProject;
    private LibraryNode baseLib;
    private LibraryNode extLib;
    private MockLibrary ml = new MockLibrary();

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
     * Create 2 libraries in the <i>uploadProject</i>. Simple type in base library used as type in the ext Library
     * 
     * @throws RepositoryException
     * @throws LibrarySaveException
     */
    @Before
    public void beforeEachTest2() throws RepositoryException, LibrarySaveException {
        uploadProject = createProject( "RepositoryProject", getRepositoryForTest(), "dependencies" );

        baseLib = ml.createNewLibrary_Empty( uploadProject.getNamespace(), "Base", uploadProject );
        SimpleTypeNode simpleInBase = ml.addSimpleTypeToLibrary( baseLib, "MyString" );
        simpleInBase.setAssignedType( ml.getXsdString() );

        // Create a valid Business Object for contextual facet and resource tests
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( baseLib, "BaseBO", true );

        extLib = ml.createNewLibrary_Empty( uploadProject.getNamespace(), "Ext", uploadProject );
        VWA_Node vwa = ml.addVWA_ToLibrary( extLib, "VWA" );
        TypedPropertyNode withAssignedType = new AttributeNode( vwa.getFacet_Attributes(), "attribute1", simpleInBase );

        assert uploadProject != null;
        assert baseLib != null;
        assert extLib != null;
        assert baseLib.isEditable();
        assert extLib.isEditable();
        assert baseLib.getProject() == uploadProject;
        assert extLib.getProject() == uploadProject;
        assert simpleInBase != null;
        assert bo != null;
        assert withAssignedType != null;
        assert withAssignedType.getAssignedType() == simpleInBase;
        assert vwa != null;
        ml.check( uploadProject );
    }

    @Test
    public void manageBothLibraries() throws RepositoryException, LibrarySaveException {
        // When - both are managed in one call
        rc.manage( getRepositoryForTest(), Arrays.asList( extLib, baseLib ) );
        // Then
        assertAllLibrariesLoadedCorrectly( baseLib.getChain(), extLib.getChain() );
    }

    @Test
    public void manageOneByOneStartingFromBaseLibrary() throws RepositoryException {
        // When - given libraries are managed in repository
        LibraryChainNode baseChain = rc.manage( getRepositoryForTest(), Collections.singletonList( baseLib ) ).get( 0 );
        LibraryChainNode extChain = rc.manage( getRepositoryForTest(), Collections.singletonList( extLib ) ).get( 0 );
        // Then
        assertAllLibrariesLoadedCorrectly( baseChain, extChain );
    }

    @Test
    public void manageOnlyLibWithIncludes() throws RepositoryException, LibrarySaveException {
        List<LibraryChainNode> chains = rc.manage( getRepositoryForTest(), Collections.singletonList( extLib ) );
        LibraryChainNode extChain = findLibrary( extLib.getName(), chains );
        LibraryChainNode baseChain = findLibrary( baseLib.getName(), chains );

        assertAllLibrariesLoadedCorrectly( baseChain, extChain );

    }

    @Test
    public void DT_contextualFacets() throws RepositoryException, LibrarySaveException {
        // When - both are managed in one call
        rc.manage( getRepositoryForTest(), Arrays.asList( extLib, baseLib ) );

        // When - both libraries are editable
        rc.lock( baseLib );
        rc.lock( extLib );
        assert baseLib.isEditable();
        assert extLib.isEditable();

        // When - business object and contextual facets added
        // BusinessObjectNode bo = ml.addBusinessObjectToLibrary(baseLib, "CFBase", true);
        // assert bo != null;
        BusinessObjectNode bo = findBusinessObject( baseLib.getChain() );
        ContextualFacetNode cf = new CustomFacetNode();
        extLib.addMember( cf );
        cf.setName( "Custom1" );
        new IndicatorNode( cf, "One" );
        cf.setOwner( bo );

        // When - libraries are saved
        mc.getLibraryController().saveLibrary( baseLib, true );
        mc.getLibraryController().saveLibrary( extLib, true );

        // Then
        assertAllLibrariesLoadedCorrectly( baseLib.getChain(), extLib.getChain() );
    }

    private void assertAllLibrariesLoadedCorrectly(LibraryChainNode baseChain, LibraryChainNode extChain) {
        // find repository item before delete.
        ml.check( uploadProject );
        RepositoryItemNode nodeToRetrive = findRepositoryItem( extChain, getRepositoryForTest() );

        // Remove libraries from TL and GUI models
        mc.getProjectController().remove( (LibraryNavNode) baseChain.getParent() );
        mc.getProjectController().remove( (LibraryNavNode) extChain.getParent() );
        assertTrue( "Project must be empty.", 0 == uploadProject.getChildren().size() );

        // When - load only 1 library which should also load the other to resolve dependencies
        pc.add( uploadProject, nodeToRetrive.getItem() );

        // Then - make sure that Ext library is loaded
        assertTrue( "Must have 2 library chains.", 2 == uploadProject.getChildren().size() );
        LibraryChainNode eLcn = findLibrary( extLib.getName(), uploadProject.getChildren() );
        assertTrue( "Ext Library must be in project.", eLcn != null );
        LibraryChainNode bLcn = findLibrary( baseLib.getName(), uploadProject.getChildren() );
        assertTrue( "Base Library must be in project.", bLcn != null );

        // Then - make sure the BO is loaded
        BusinessObjectNode bo = findBusinessObject( bLcn );
        assertTrue( "Must find business object.", bo != null );

        // Then - make sure the VWA is loaded
        VWA_Node vwaNode = null;
        for (LibraryMemberInterface lm : eLcn.getDescendants_LibraryMembers())
            if (lm instanceof VWA_Node)
                vwaNode = (VWA_Node) lm;
        assertTrue( "VWA must be found.", vwaNode instanceof VWA_Node );

        // Then - make sure the attribute type is assigned
        AttributeNode attr = (AttributeNode) vwaNode.getFacet_Attributes().getChildren().get( 0 );
        assertTrue( "Attribute must be found.", attr != null );
        assertTrue( "Attribute must have assigned type.", !attr.isUnAssigned() );

        // May be empty depending on which test
        CustomFacetNode cf = findCustomFacet( eLcn );
        if (cf != null) {
            assertTrue( "Facet must have TL Owner", cf.getTLModelObject().getOwningEntity() != null );
            assertTrue( "Contextual facet must have an contributor.", cf.getWhereContributed() != null );
        }

        // Finally - check the entire project
        ml.check( uploadProject );
    }

    private BusinessObjectNode findBusinessObject(LibraryChainNode lcn) {
        for (LibraryMemberInterface lm : lcn.getDescendants_LibraryMembers())
            if (lm instanceof BusinessObjectNode)
                return (BusinessObjectNode) lm;
        return null;
    }

    private CustomFacetNode findCustomFacet(LibraryChainNode lcn) {
        for (LibraryMemberInterface lm : lcn.getDescendants_LibraryMembers())
            if (lm instanceof CustomFacetNode)
                return (CustomFacetNode) lm;
        return null;
    }

    private LibraryChainNode findLibrary(String name, Collection<? extends Node> libs) {
        for (Node n : libs) {
            if (n instanceof LibraryNavNode)
                n = (Node) ((LibraryNavNode) n).getThisLib();
            if (n instanceof LibraryChainNode) {
                LibraryChainNode lch = (LibraryChainNode) n;
                if (name.equals( lch.getHead().getName() ))
                    return lch;
            }
        }
        return null;
    }
}

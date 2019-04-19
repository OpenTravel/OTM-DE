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
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
// Does not change files.
public class ExtensionPointNode_Tests extends BaseProjectTest {
    private static final String MY_CORE = "MyCore";

    private static final String ELE1 = "Ele1";

    private final static Logger LOGGER = LoggerFactory.getLogger( ExtensionPointNode_Tests.class );

    TypeProvider emptyNode = null;
    TypeProvider sType = null;

    ProjectNode defaultProject;
    LoadFiles lf = new LoadFiles();
    MockLibrary ml = new MockLibrary();
    LibraryChainNode lcn = null;
    LibraryNode ln = null;

    @Override
    @Before
    public void beforeEachTest() throws Exception {
        LOGGER.debug( "***Before Extension Point Tests ----------------------" );
        super.beforeEachTest();
        defaultProject = testProject;
        ln = ml.createNewLibrary( "http://test.com", "CoreTest", defaultProject );
        ln.setEditable( true );

        emptyNode = (TypeProvider) ModelNode.getEmptyNode();
        sType = (TypeProvider) NodeFinders.findNodeByName( "date", ModelNode.XSD_NAMESPACE );
        assertTrue( sType != null );
    }

    public TLExtensionPointFacet createTL(boolean empty, FacetProviderNode fn) {
        TLExtensionPointFacet tlep = createTL( empty );
        TLExtension tlex = new TLExtension();
        tlex.setExtendsEntity( fn.getTLModelObject() );
        tlep.setExtension( tlex );
        return tlep;
    }

    public TLExtensionPointFacet createTL(boolean empty) {
        TLExtensionPointFacet tlep = new TLExtensionPointFacet();
        if (!empty) {
            tlep.addAttribute( new TLAttribute() );
            tlep.addElement( new TLProperty() );
            tlep.addIndicator( new TLIndicator() );
        }
        return tlep;
    }

    @Test
    public void EP_ConstructorsTests() {
        // Given - an empty TL Extension Point Facet
        TLExtensionPointFacet tlep = createTL( true );
        ExtensionPointNode ep = new ExtensionPointNode( tlep );

        check( ep, false );
        assertTrue( ep.getChildren().isEmpty() );
        assertTrue( ep.getTLModelObject() == tlep );
        assertTrue( ep.getExtensionBase() == null );

        // Given - a TL Extension Point Facet with properties
        TLExtensionPointFacet tlep2 = createTL( false );
        ExtensionPointNode ep2 = new ExtensionPointNode( tlep2 );

        check( ep2, false );
        assertTrue( !ep2.getChildren().isEmpty() );
        assertTrue( ep2.getTLModelObject() == tlep2 );
        assertTrue( ep2.getExtensionBase() == null );

        // Given - a TL Extension Point Facet extending a facet with properties
        // FacetOMNode fn = new FacetOMNode(new TLFacet());
        FacetProviderNode fn = new FacetProviderNode( new TLFacet() );
        TLExtensionPointFacet tlep3 = createTL( false, fn );
        ExtensionPointNode ep3 = new ExtensionPointNode( tlep3 );

        check( ep2, false );
        assertTrue( !ep3.getChildren().isEmpty() );
        assertTrue( ep3.getTLModelObject() == tlep3 );
        assertTrue( ep3.getExtensionBase() == fn );
    }

    @Test
    public void EP_FactoryTest() {
        // Given - an editable library
        ln = ml.createNewLibrary( "http://opentravel.org/test", "test", defaultProject );
        ln.setEditable( true );
        // Given - a bo to extend
        BusinessObjectNode bo = null;
        for (LibraryMemberInterface n : ln.get_LibraryMembers())
            if (n instanceof BusinessObjectNode)
                bo = (BusinessObjectNode) n;

        // Given - a second library for the EP
        LibraryNode ln2 = ml.createNewLibrary_Empty( "http://opentravel.org/test2", "test2", defaultProject );

        LibraryMemberInterface ep = NodeFactory.newLibraryMember( createTL( false, bo.getFacet_ID() ) );
        ln2.addMember( ep );
    }

    @Test
    public void EP_creatingProperties() {
        // Given - an empty TL Extension Point Facet
        TLExtensionPointFacet tlep = createTL( true );
        ExtensionPointNode ep = new ExtensionPointNode( tlep );

        check( ep, false );
        assertTrue( ep.getChildren().isEmpty() );

        // When - add property using an element
        ElementNode ele = new ElementNode( new TLProperty(), null );
        ele.setName( ELE1 );
        ep.addProperty( ele );
        assertTrue( ep.findChildByName( ELE1 ) != null );

        // When - create property from a core
        CoreObjectNode cn = new CoreObjectNode( new TLCoreObject() );
        cn.setName( MY_CORE );
        ep.createProperty( cn );
        assertTrue( ep.findChildByName( MY_CORE ) != null );
        check( ep, false );

        // When - added properties are removed
        ep.removeProperty( ep.findChildByName( MY_CORE ) );
        ep.removeProperty( ep.findChildByName( ELE1 ) );
        // Then - they can't be found and ep checks OK
        assertTrue( ep.findChildByName( MY_CORE ) == null );
        assertTrue( ep.findChildByName( ELE1 ) == null );
        check( ep, false );
    }

    @Test
    public void EP_LoadLibraryTests() throws Exception {
        // Extension point is not valid
        lf.loadFile5Clean( mc );
        for (LibraryNode ln : mc.getModelNode().getUserLibraries()) {
            List<LibraryMemberInterface> types = ln.getDescendants_LibraryMembers();
            for (LibraryMemberInterface n : types)
                if (n instanceof ExtensionPointNode)
                    check( (ExtensionPointNode) n );
            ml.check( ln, false );
        }
    }

    @Test
    public void EP_LoadLibrary2Tests() throws Exception {
        // Two valid libraries with one having extension points
        lf.loadFile_ExtensionPoint( mc );

        BusinessObjectNode bo = null;
        for (LibraryNode ln : mc.getModelNode().getUserLibraries())
            for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
                if (n instanceof BusinessObjectNode)
                    bo = (BusinessObjectNode) n; // save for later

        for (LibraryNode ln : mc.getModelNode().getUserLibraries()) {
            for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
                if (n instanceof ExtensionPointNode) {
                    ExtensionPointNode ep = (ExtensionPointNode) n;
                    if (ep.getExtensionBase() == null)
                        ep.setExtension( bo.getFacet_Detail() ); // make it valid
                    check( ep );
                }
            ml.check( ln, true );
        }
    }

    @Test
    public void EP_TypeSettingTests() {}

    /**
     * Check the structure of the passed Extension Point
     */
    public void check(ExtensionPointNode ep) {
        check( ep, true );
    }

    public void check(ExtensionPointNode ep, boolean valid) {
        LOGGER.debug( "Checking Extension Point: " + ep );

        assertTrue( ep instanceof FacetInterface );
        assertTrue( ep instanceof ExtensionOwner );
        assertTrue( ep.isRenameable() == false );
        assertTrue( ep.getTLModelObject() instanceof TLExtensionPointFacet );
        assertTrue( Node.GetNode( ep.getTLModelObject() ) == ep );

        // Edit-ability
        if (ep.getLibrary() != null && ep.getLibrary().isEditable()) {
            assertTrue( ep.isEnabled_AddProperties() );
            if (ep.getLibrary().isInHead2())
                assertTrue( ep.isDeleteable() );
        }

        // Extension
        assertTrue( ep.getExtensionHandler() != null );
        if (ep.getTLModelObject().getExtension() != null)
            if (ep.getTLModelObject().getExtension().getExtendsEntity() != null)
                assertTrue( ep.getExtensionBase() != null );
            else {
                valid = false; // will not validate
                assertTrue( ep.getExtensionBase() == null );
            }
        else {
            valid = false; // will not validate
            assertTrue( ep.getExtensionBase() == null );
        }

        // Children
        assertTrue( ep.getChildrenHandler() != null );
        List<TLModelElement> tlKids = ep.getChildrenHandler().getChildren_TL();
        for (Node n : ep.getChildren()) {
            ml.check( n, valid );
            assertTrue( tlKids.contains( n.getTLModelObject() ) );
        }
    }

}

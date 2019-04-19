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

package org.opentravel.schemas.node;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.listeners.TypeUserAssignmentListener;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.AttributeReferenceNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeProviders;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PropertyNodeTest extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger( PropertyNodeTest.class );

    TypeProvider emptyNode = null;
    TypeProvider sType = null;

    @Before
    public void beforeEachOfTheseTests() {
        ln = ml.createNewLibrary_Empty( "http://example.com/test", "test", defaultProject );

        emptyNode = (TypeProvider) ModelNode.getEmptyNode();
        sType = (TypeProvider) NodeFinders.findNodeByName( "date", ModelNode.XSD_NAMESPACE );

        // For some reason, the built-iin library is empty on second test
        TypeProvider idType = (TypeProvider) NodeFinders.findNodeByName( "ID", ModelNode.XSD_NAMESPACE );
        assertTrue( "Test Setup Error - no empty type.", emptyNode != null );
        assertTrue( "Test Setup Error - no date.", sType != null );
        assertTrue( "Test Setup Error - no idType.", idType != null );
    }

    @Test
    public void PN_Constructor_Elements() {

        // Given - a facet parent and a simple type to assign
        CoreObjectNode core = new CoreObjectNode( new TLCoreObject() );
        core.setName( "TC" );
        ln.addMember( core );
        FacetProviderNode facet = core.getFacet_Summary();
        assertTrue( facet != null );
        TypeProvider simple1 = (TypeProvider) NodeFinders.findNodeByName( "string", ModelNode.XSD_NAMESPACE );

        ElementNode e1 = new ElementNode( facet, "E1" );
        ElementNode e2 = new ElementNode( new TLProperty(), facet );
        ElementNode e3 = new ElementNode( facet, "E3", simple1 );
        check( e1 );
        check( e2 );
        check( e3 );
        List<Node> kids = facet.getChildren();
        assertTrue( "Facet must have children.", !facet.getChildren().isEmpty() );

        // When - add type assignments to make the facet valid
        e2.setAssignedType( simple1 );
        e1.setAssignedType( simple1 );
        // Then - facet must be valid
        ml.check( facet );
    }

    private List<PropertyNode> oneOfEach(FacetInterface owner) {
        List<PropertyNode> properties = new ArrayList<>();
        properties.add( new ElementNode( owner, "e1" ) );
        properties.add( new ElementReferenceNode( owner ) );

        properties.add( new IndicatorNode( owner, "i1" ) );
        properties.add( new IndicatorElementNode( owner, "ie1" ) );

        properties.add( new AttributeNode( owner, "a1" ) );
        properties.add( new AttributeReferenceNode( owner ) );
        properties.add( new IdNode( owner, "id1" ) );
        return properties;
    }

    @Test
    public void PN_ConstructorsWithOutTLObj() {
        // When - one of each is created
        FacetInterface owner = null;
        oneOfEach( owner ); // make sure no parent does not NPE

        // Check these that are not owned by PropertyOwner
        RoleNode rn = new RoleNode( null, "r1" );
        // SimpleAttributeFacadeNode must have parent - See VWA and core object tests

        // Given - a facet parent and a simple type to assign
        CoreObjectNode core = new CoreObjectNode( new TLCoreObject() );
        core.setName( "TC" );
        ln.addMember( core );
        FacetProviderNode facet = core.getFacet_Summary();
        assertTrue( facet != null );
        // Then - create and check
        for (PropertyNode pn : oneOfEach( facet ))
            check( pn );
    }

    @Test
    public void PN_ConstructorsInOwner() {

        // Given - a facet parent and a simple type to assign
        CoreObjectNode core = new CoreObjectNode( new TLCoreObject() );
        core.setName( "TC" );
        ln.addMember( core );
        FacetProviderNode facet = core.getFacet_Summary();
        assertTrue( facet != null );
        TypeProvider simple1 = (TypeProvider) NodeFinders.findNodeByName( "string", ModelNode.XSD_NAMESPACE );
        // When - one element added to make core valid - need to create BO
        ElementNode e1 = new ElementNode( facet, "E1", simple1 );

        // Given - a business object with valid id facet to use as reference target
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TBO" );
        assertTrue( bo != null );
        ml.check( ln );

        // When - one of each is created
        ElementNode er1 = new ElementReferenceNode( facet, bo );
        AttributeNode a1 = new AttributeNode( facet, "a1", simple1 );
        AttributeNode ar2 = new AttributeReferenceNode( facet, bo );
        AttributeNode id3 = new IdNode( facet, "id1" );
        IndicatorNode i1 = new IndicatorNode( facet, "i1" );
        IndicatorNode ie1 = new IndicatorElementNode( facet, "ie1" );

        // SimpleAttributeNode tested elsewhere
        // EnumLiteral - tested elsewhere

        // Then - core must be valid
        List<Node> kids = facet.getChildren();
        assertTrue( "Facet must have children.", !facet.getChildren().isEmpty() );
        ml.check( core );
    }

    @Test
    public void PN_AttrAssignmentTests() {
        // Given - types to assign
        SimpleTypeNode simple = ml.addSimpleTypeToLibrary( ln, "Simple" );
        TypeProvider string = ml.getXsdString();

        // Given - a VWA with 3 new attributes
        VWA_Node pVwa = ml.addVWA_ToLibrary( ln, "P_VWA" );
        for (Node n : pVwa.getFacet_Attributes().getChildren())
            n.delete();
        new AttributeNode( pVwa.getFacet_Attributes(), "a1" );
        new AttributeNode( pVwa.getFacet_Attributes(), "a2" );
        new AttributeNode( pVwa.getFacet_Attributes(), "a3" );
        assertTrue( !pVwa.getAttributes().isEmpty() );

        // Then - check assignment to unassigned node
        TypeProvider unassigned = ModelNode.getUnassignedNode();
        TypeProvider vType = pVwa.getAssignedType();
        Collection<TypeUser> unList = unassigned.getWhereAssigned();
        assertTrue( !unList.isEmpty() );
        for (Node n : pVwa.getFacet_Attributes().getChildren()) {
            assertTrue( unList.contains( n ) );
            // Make sure user has correct TypeProviderListener
            for (ModelElementListener l : n.getTLModelObject().getListeners())
                if (l instanceof TypeUserAssignmentListener)
                    assertTrue( ((TypeUserAssignmentListener) l).getNode() == unassigned );
        }

        for (Node n : pVwa.getFacet_Attributes().getChildren()) {
            if (n instanceof TypeUser) {
                TypeUser attr = (AttributeNode) n;

                // When - cleared
                attr.setAssignedType();
                // Then - verify the assignment
                assertTrue( attr.getAssignedType() == ModelNode.getUnassignedNode() );

                // When - assigned type
                attr.setAssignedType( string );
                // Then - verify the assignment
                assertTrue( attr.getAssignedType() == string );
                assertTrue( string.getWhereAssigned().contains( attr ) );

                // When - assigned type
                attr.setAssignedType( simple );
                // Then - verify the assignment
                assertTrue( attr.getAssignedType() == simple );
                assertTrue( simple.getWhereAssigned().contains( attr ) );

                // Verify listener
                for (ModelElementListener l : n.getTLModelObject().getListeners())
                    if (l instanceof TypeUserAssignmentListener)
                        assertTrue( ((TypeUserAssignmentListener) l).getNode() == simple );
            }
        }

        // Then - check will test assignments and where assigned
        ml.check( ln );
    }

    @Test
    public void PN_AssignmentTests() {
        // Given - types to assign
        SimpleTypeNode simple = ml.addSimpleTypeToLibrary( ln, "Simple" );
        TypeProvider string = (TypeProvider) NodeFinders.findNodeByName( "string", ModelNode.XSD_NAMESPACE );
        assertTrue( string != null );

        // Given - a Core with one of each type of new property
        CoreObjectNode pCore = ml.addCoreObjectToLibrary( ln, "PCore" );
        new AttributeNode( pCore.getFacet_Summary(), "aa1" );
        oneOfEach( pCore.getFacet_Summary() );

        for (Node n : pCore.getFacet_Summary().getChildren()) {
            if (n instanceof TypeUser) {
                TypeUser user = (TypeUser) n;

                // When - successfully cleared
                if (user.setAssignedType())
                    // Then - verify the assignment
                    assertTrue( user.getAssignedType() == ModelNode.getUnassignedNode() );

                // When - successfully assigned
                if (user.setAssignedType( string ) == string)
                    // Then - verify the assignment
                    assertTrue( user.getAssignedType() == string );

                // When - successfully assigned a type
                if (user.setAssignedType( simple ) == simple) {
                    // Then - verify the assignment
                    assertTrue( user.getAssignedType() == simple );
                    assertTrue( simple.getWhereAssigned().contains( user ) );

                    // Verify listener
                    for (ModelElementListener l : user.getTLModelObject().getListeners())
                        if (l instanceof TypeUserAssignmentListener)
                            assertTrue( ((TypeUserAssignmentListener) l).getNode() == simple );
                }
            }
        }

        // Then - check will test assignments and where assigned
        ml.check( ln, false ); // Will not be valid
    }

    @Test
    public void isRenameableTests() {
        // Given - library with one of each object type
        ml.addOneOfEach( ln, "Rn" );
        BusinessObjectNode bo = null;
        VWA_Node vwa = null;
        CoreObjectNode core = null;
        EnumerationOpenNode eo = null;
        for (LibraryMemberInterface n : ln.get_LibraryMembers())
            if (n instanceof BusinessObjectNode)
                bo = (BusinessObjectNode) n;
            else if (n instanceof VWA_Node)
                vwa = (VWA_Node) n;
            else if (n instanceof CoreObjectNode)
                core = (CoreObjectNode) n;
            else if (n instanceof EnumerationOpenNode)
                eo = (EnumerationOpenNode) n;
        // Given - the business object extends another one
        BusinessObjectNode boBase = ml.addBusinessObjectToLibrary( ln, "Rn2" );
        bo.setExtension( boBase );
        assertTrue( "BO extends BO Base", bo.getExtensionBase() == boBase );

        // Then - each property type should report renameable correct.
        assertTrue( "Enum Literals must be renameable.", eo.getChildren().get( 0 ).isRenameable() );
        assertTrue( "Role nodes must be renameable.", core.getFacet_Role().getChildren().get( 0 ).isRenameable() );

        // Then - properties that must not be reassigned
        assertTrue( "Simple attributes must NOT be renameable.", !core.getSimpleAttribute().isRenameable() );

        // Then - Business object will have one of each property type assigned simple type
        for (Node n : bo.getDescendants())
            if (n instanceof PropertyNode) {
                // Then - check with different type assignments
                propertyRenameableCheck( (PropertyNode) n );
                if (n instanceof TypeUser) {
                    if (vwa.canAssign( n ))
                        ((TypeUser) n).setAssignedType( vwa );
                    propertyRenameableCheck( (PropertyNode) n );
                    if (core.canAssign( n ))
                        ((TypeUser) n).setAssignedType( core );
                    propertyRenameableCheck( (PropertyNode) n );
                }
            }
    }

    public void propertyRenameableCheck(PropertyNode pn) {
        // if editable and not inherited then it depends on the assigned type.
        if (!pn.isEditable())
            assertTrue( "Uneditable property must not be renameable.", !pn.isRenameable() );
        else if (pn.isInherited())
            assertTrue( "Inherited property must not be renameable.", !pn.isRenameable() );
        else if (pn instanceof TypeUser)
            if (!((TypeUser) pn).getAssignedType().isRenameableWhereUsed()
                && !(((TypeUser) pn).getAssignedType() instanceof SimpleTypeProviders))
                assertTrue( "Property's assigned type requires it to not be renameable.", !pn.isRenameable() );
            else
                assertTrue( "Property must be renameable.", pn.isRenameable() );
    }

    public void check(PropertyNode pn) {
        if (pn instanceof TypeUser) {
            TypeProvider at = ((TypeUser) pn).getAssignedType();
            if (at != null) {
                if (((TypeUser) pn).getAssignedTLObject() == null)
                    // Null may or may not be assigned to missing but will return missing implied node
                    LOGGER.debug( "Null assigned type found on tl object. " + pn );
                else if (!at.getWhereAssigned().contains( pn )) {
                    Collection<TypeUser> u = at.getWhereAssigned();
                    LOGGER.debug( "Property must be in where assigned list. " + pn + " " + at );
                }
            } // else
              // LOGGER.debug("OK - Property assigned list. " + pn + " " + nt);
        }

        // assertTrue("Property must be in where assigned list.", nt.getWhereAssigned().contains(pn));
        assertTrue( "Property must have tlObj.", pn.getTLModelObject() != null );
        // If it is not a facade, it must have correct listener.
        if (!(pn instanceof FacadeInterface))
            assertTrue( "Property listener must point to proeprty.", Node.GetNode( pn.getTLModelObject() ) == pn );
        assertTrue( "Property must have parent.", pn.getParent() != null );
        assertTrue( "Property must have library.", pn.getLibrary() == pn.getParent().getLibrary() );
        assertTrue( "Property must have name.", pn.getName() != null );
        assertTrue( "Property must have label.", pn.getLabel() != null );
    }
}

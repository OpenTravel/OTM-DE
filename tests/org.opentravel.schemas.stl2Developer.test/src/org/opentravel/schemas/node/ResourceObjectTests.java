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

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ParamGroup;
import org.opentravel.schemas.node.resources.ParentRef;
import org.opentravel.schemas.node.resources.ResourceBuilder;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.resources.ResourceParameter;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class ResourceObjectTests extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger( ResourceObjectTests.class );

    @Test
    public void constructorTests() {
        // Given - a library and the objects used in constructors
        LibraryNode ln = ml.createNewLibrary( "http://example.com/resource", "RT", pc.getDefaultProject() );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "MyBo" );
        Node node = bo;
        // Given - an empty TLResource
        TLResource emptyTL = new TLResource();
        emptyTL.setName( "MyTlResource" );
        emptyTL.setBusinessObjectRef( bo.getTLModelObject() );

        // Given - a built-out tl object a
        TLResource builtTL = new ResourceBuilder().buildTL(); // get a populated tl resource
        builtTL.setBusinessObjectRef( bo.getTLModelObject() );
        // When - as used in LibraryNode.generateLibrary()
        ResourceNode rn1 = new ResourceNode( builtTL, ln );

        // Then - resource must have children
        List<Node> kids = rn1.getChildren();
        List<Node> tKids = rn1.getTreeChildren();
        assertTrue( "Must have children.", !kids.isEmpty() );
        assertTrue( "Must have tree children.", !tKids.isEmpty() );

        // When - used in tests
        ResourceNode rn2 = ml.addResource( bo );

        // When - used in NodeFactory
        ResourceNode rn3 = new ResourceNode( emptyTL );
        ln.addMember( rn3 );

        // When - used in ResourceCommandHandler to launch wizard
        ResourceNode rn4 = new ResourceNode( node.getLibrary(), bo );
        // When - builder used as in ResourceCommandHandler
        new ResourceBuilder().build( rn4, bo );

        // Then - must be complete
        check( rn1 );
        check( rn2 );
        check( rn3 );
        check( rn4 );
    }

    @Test
    public void fileReadTest() throws Exception {
        LibraryNode testLib = new LoadFiles().loadFile6( mc );
        new LibraryChainNode( testLib ); // Test in a chain

        for (LibraryMemberInterface n : testLib.getDescendants_LibraryMembers()) {
            if (n instanceof ResourceNode)
                check( (ResourceNode) n );
        }
    }

    @Test
    public void deleteResourceTest() {
        // Given - a library and resource
        LibraryNode ln = ml.createNewLibrary( "http://example.com/resource", "RT", pc.getDefaultProject() );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "MyBo" );
        ResourceNode rn = ml.addResource( bo );
        check( rn );

        // Given
        Collection<TypeUser> l1 = bo.getWhereAssigned();
        Collection<TypeUser> l2 = bo.getWhereUsedAndDescendants();
        assertTrue( "Resource must be in subject's where assigned list.", bo.getWhereAssigned().contains( rn ) );
        assertTrue( "Resource must have a subject.", rn.getSubject() == bo );
        assertTrue( "Resource must be in subject's where-used list.", bo.getWhereUsedAndDescendants().contains( rn ) );

        // When - the resource is deleted
        rn.delete();

        // Then
        assertTrue( "Resource must be deleted.", rn.isDeleted() );
        assertTrue( "Resource must NOT be in subject's where-used list.",
            !bo.getWhereUsedAndDescendants().contains( rn ) );
    }

    @Test
    public void resource_CopyTests() {

        // Given - a destination library in a different namespace from the source
        LibraryNode destLib =
            ml.createNewLibrary_Empty( "http://opentravel.org/Test/tx", "TL2", pc.getDefaultProject() );
        // Given - a valid resource using mock library provided business object
        LibraryNode srcLib = ml.createNewLibrary( pc, "ResourceTestLib" );
        BusinessObjectNode bo = null;
        for (LibraryMemberInterface n : srcLib.getDescendants_LibraryMembers())
            if (n instanceof BusinessObjectNode) {
                bo = (BusinessObjectNode) n;
                break;
            }
        ResourceNode resource = ml.addResource( bo );
        assertTrue( "Resource created must not be null.", resource != null );
        ml.check( resource );
        ml.check( srcLib );

        // When - copied to destination library
        destLib.copyMember( resource );
        // Then - source lib is not changed
        assertTrue( srcLib.contains( resource ) );
        ml.check( srcLib );
        ml.check( resource );
        // Then - it is copied and is valid
        ResourceNode newResource = null;
        for (LibraryMemberInterface r : destLib.getDescendants_LibraryMembers())
            if (r.getName().equals( resource.getName() ))
                newResource = (ResourceNode) r;
        assertTrue( destLib.contains( newResource ) );
        BusinessObjectNode subject = newResource.getSubject();
        ml.check( newResource );
        ml.check( destLib );
    }

    @Test
    public void resource_MoveTests() {

        // Given - a valid resource using mock library provided business object
        LibraryNode srcLib = ml.createNewLibrary( pc, "ResourceTestLib" );
        LibraryNode destLib = ml.createNewLibrary( pc, "ResourceTestLib2" );
        BusinessObjectNode bo = null;
        for (LibraryMemberInterface n : srcLib.getDescendants_LibraryMembers())
            if (n instanceof BusinessObjectNode) {
                bo = (BusinessObjectNode) n;
                break;
            }
        ResourceNode resource = ml.addResource( bo );
        assertTrue( "Resource created must not be null.", resource != null );
        ml.check( resource, false );
        ml.check( srcLib, false );

        ml.check( resource );
        ml.check( srcLib );
        ml.check( destLib );

        // When - moved to destination library
        // srcLib.moveMember(resource, destLib);
        destLib.addMember( resource );

        // Then - it is moved and is valid
        assertTrue( !srcLib.contains( resource ) );
        assertTrue( destLib.contains( resource ) );
        ml.check( resource );
        ml.check( srcLib );
        ml.check( destLib );
    }

    @Test
    public void actionExample_Tests() {

        // Given - a valid resource using mock library provided business object
        LibraryNode ln = ml.createNewLibrary( pc, "ResourceTestLib" );
        BusinessObjectNode bo = null;
        for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
            if (n instanceof BusinessObjectNode) {
                bo = (BusinessObjectNode) n;
                break;
            }
        ResourceNode resource = ml.addResource( bo );
        assertTrue( "Resource created must not be null.", resource != null );

        // When
        // Then - examples are created
        for (ActionNode action : resource.getActions()) {
            String url = action.getRequest().getURL();
            LOGGER.debug( "Example: " + url + "." );
            assertTrue( "Action has example.", !url.isEmpty() );
            // some Get actions created for custom facets have longer names
            if (action.getName().equals( "Get" ))
                assertTrue( "Get example must be correct.", url.startsWith(
                    "GET http://example.com/ResourceTestLibInitialBOs/{testIdResourceTestLibInitialBO}/{ResourceTestLibInitialBOID}" ) );
        }
    }

    /**
     * Emulate Resource model to implement <br>
     * /Reservations/{ResID}/Orders/{OrderID}/Products/{ProductID}
     */
    @Test
    public void actionExampleWithBaseResource_Tests() {

        // Given - a valid resource using mock library provided business object
        LibraryNode ln = ml.createNewLibrary( pc, "ResourceTestLib" );
        BusinessObjectNode resBO = ml.addBusinessObject_ResourceSubject( ln, "Reservations" );
        BusinessObjectNode orderBO = ml.addBusinessObject_ResourceSubject( ln, "Orders" );
        BusinessObjectNode productBO = ml.addBusinessObject_ResourceSubject( ln, "Products" );
        BusinessObjectNode descBO = ml.addBusinessObject_ResourceSubject( ln, "Descriptions" );

        ResourceNode resR = ml.addResource( resBO );
        ResourceNode orderR = ml.addResource( orderBO );
        ResourceNode productR = ml.addResource( productBO );
        ResourceNode descR = ml.addResource( descBO );
        // Given tests
        assertTrue( "Resource must have been created.", productR != null );
        assertTrue( "Resource must have been created.", orderR != null );
        assertTrue( "Resource must have been created.", resR != null );
        assertTrue( "Resource must have been created.", descR != null );
        ml.check( ln, false );

        checkActionURLs( resR, "Reservation" );

        // NOTE - library will be invalid because the parent params are not correct.
        // When - reservation is set as parent on Order as done in the GUI
        orderR.toggleParent( resR.getNameWithPrefix() );
        ParentRef parentRef = orderR.getParentRef();
        parentRef.setParamGroup( "ID" );
        parentRef.setPathTemplate( "/Reservations/{reservationId}" );

        // Then - orders has reservation as parent
        // List<TLResourceParentRef> tlRefs = orderR.getTLModelObject().getParentRefs();
        // ParentRef pref = orderR.getParentRef();
        // ResourceNode presource = orderR.getParentRef().getParentResource();
        assertTrue( "Parent reference must be to resR.", orderR.getParentRef().getParentResource() == resR );
        String resContribution = orderR.getParentRef().getUrlContribution();
        assertTrue( "Parent has URL path contribution.", !resContribution.isEmpty() );
        checkActionURLs( orderR, "Reservation" );

        // When - order is set as parent to product
        productR.setParentRef( orderR.getNameWithPrefix(), "ID" );
        productR.getParentRef().setPathTemplate( "/Orders/{orderId}" );

        // Then - product has orders as parent
        assertTrue( "Parent reference is OK.", productR.getParentRef().getParentResource() == orderR );
        String orderContribution = productR.getParentRef().getUrlContribution();
        assertTrue( "Parent has URL path contribution.", !orderContribution.isEmpty() );
        checkActionURLs( productR, "Reservation" );

        // When - product is set as parent to description
        descR.setParentRef( productR.getNameWithPrefix(), "ID" );
        descR.getParentRef().setPathTemplate( "/Products/{productId}" );

        // Then - description has product as parent
        assertTrue( "Parent reference is OK.", descR.getParentRef().getParentResource() == productR );
        checkActionURLs( descR, "Reservation" );
    }

    /**
     * Emulate Resource model to implement <br>
     * /Reservations/{ResID}/Orders/{OrderID}/ /Archive/{ArchiveID}/Reservations/{ResID}/Orders/{OrderID}/
     * /Interactions/(InteractionID}/Reservations/{ResID}/Orders/{OrderID}/
     */
    @Test
    public void actionExampleWithMultipleParents_Tests() {

        // Given - a valid resource using mock library provided business object
        LibraryNode ln = ml.createNewLibrary( pc, "ResourceTestLib" );
        BusinessObjectNode resBO = ml.addBusinessObject_ResourceSubject( ln, "Reservations" );
        BusinessObjectNode orderBO = ml.addBusinessObject_ResourceSubject( ln, "Orders" );
        BusinessObjectNode interactionBO = ml.addBusinessObject_ResourceSubject( ln, "Interactions" );
        BusinessObjectNode archiveBO = ml.addBusinessObject_ResourceSubject( ln, "Archives" );

        ResourceNode resR = ml.addResource( resBO );
        ResourceNode orderR = ml.addResource( orderBO );
        ResourceNode interactionR = ml.addResource( interactionBO );
        ResourceNode archiveR = ml.addResource( archiveBO );

        orderR.setParentRef( resR.getNameWithPrefix(), "ID" );
        orderR.getParentRef().setPathTemplate( "/Reservations/{resId}" );
        resR.setParentRef( interactionR.getNameWithPrefix(), "ID" );
        resR.getParentRef().setPathTemplate( "/Interactions/{interactionId}" );
        resR.setParentRef( archiveR.getNamespaceWithPrefix(), "ID" );
        resR.getParentRef().setPathTemplate( "/Archives/{archiveId}" );

        // FIXME
        checkActionURLs( orderR, "Reservation" );

    }

    /**
     * Assure each action has an URL. If the resource has a parent, assure it contributes to the URL.
     * 
     * @param rn
     */
    private void checkActionURLs(ResourceNode rn, String stringToFind) {
        // LOGGER.debug("");
        LOGGER.debug( "Printing action URLs for " + rn );
        for (ActionNode action : rn.getActions()) {
            // Action request uses private pathTemplate for URLs
            assert (action.getRequest() != null);
            assert (action.getPathTemplate() != null); // pass thorugh to request
            if (rn.getParentRef() != null)
                assert (!action.getParentContribution().isEmpty());

            // URL combines parent contribution with template params and payload
            String url = action.getRequest().getURL();
            // LOGGER.debug("Parent contribution: " + action.getParentContribution());
            LOGGER.debug( "Action URL: " + url );
            assertTrue( "Action has an URL.", !url.isEmpty() );

            if (stringToFind != null)
                assertTrue( "Example must contain " + stringToFind, url.contains( stringToFind ) );

        }

    }

    @Test
    public void RN_inheritedResource_Tests() {

        // Given - a valid resource using mock library provided business object
        LibraryNode ln = ml.createNewLibrary_Empty( defaultProject.getNamespace(), "ResourceTestLib", defaultProject );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "InnerObject" );
        ResourceNode resource = ml.addResource( bo );
        assertTrue( "Resource must have been created.", resource != null );
        ml.check( ln, false ); // Will not be valid
        assert resource.getInheritedChildren().isEmpty();

        // Given - a second BO created before library made invalid by resources
        BusinessObjectNode parentBO = ml.addBusinessObjectToLibrary( ln, "ParentBO" );
        // Given - a second resource
        ResourceNode parentResource = ml.addResource( parentBO );
        assert !parentResource.getChildren().isEmpty();

        // When -
        resource.setExtension( parentResource );
        // // When - parent resource is set on resource with paramGroup
        // ParentRef parentRef = resource.setParentRef(parentResource.getName(), "ID");

        // Then
        List<Node> iKids = resource.getInheritedChildren();
        assertTrue( "Resource must have inherited children.", !iKids.isEmpty() );
    }

    @Test
    public void deleteParentResource_Tests() {

        // Given - a valid resource using mock library provided business object
        LibraryNode ln = ml.createNewLibrary( pc, "ResourceTestLib" );
        BusinessObjectNode bo = null;
        for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
            if (n instanceof BusinessObjectNode) {
                bo = (BusinessObjectNode) n;
                break;
            }
        bo.setName( "InnerObject" );
        // Given - a second BO created before library made invalid by resources
        BusinessObjectNode parentBO = ml.addBusinessObjectToLibrary( ln, "ParentBO" );

        ResourceNode resource = ml.addResource( bo );
        assertTrue( "Resource must have been created.", resource != null );
        ml.check( ln, false ); // Will not be valid

        // Given a second resource
        ResourceNode parentResource = ml.addResource( parentBO );

        // When - parent resource is set on resource with paramGroup
        ParentRef parentRef = resource.setParentRef( parentResource.getNameWithPrefix(), "ID" );

        // Then - there is a parent contribution
        assertTrue( "Parent makes URL contribution.", !parentRef.getUrlContribution().isEmpty() );

        // When - parent resource is deleted
        parentRef.delete();

        // Then - the node, tlRef and contribution are gone
        assertTrue( "Parent has empty URL contribution.", parentRef.getUrlContribution().isEmpty() );
        assertTrue( "TLResource does not have parentRefs", resource.getTLModelObject().getParentRefs().isEmpty() );
        assertTrue( "Resource does not have ParentRef child.", !resource.getChildren().contains( parentRef ) );
    }

    /**
     * Tests to help assure that Parameters from parameter groups are not removed by the system.
     */
    @Test
    public void RN_paramGroupTests() {
        // Given - a valid library with resource and objects
        LibraryNode testLib = new LoadFiles().loadFile7( defaultProject );
        LibraryChainNode lcn = new LibraryChainNode( testLib ); // Test in a chain
        assert lcn.getParent() instanceof LibraryNavNode;

        BusinessObjectNode bo = null;
        for (LibraryMemberInterface n : testLib.getDescendants_LibraryMembers())
            if (n instanceof BusinessObjectNode) {
                bo = (BusinessObjectNode) n;
                break;
            }
        assert bo != null;
        assert bo.getTLModelObject().getOwningModel() != null;
        ml.check();

        // Given - a second library
        LibraryNode ln = ml.createNewLibrary( pc, "ResourceTestLib" );

        // Given - a second resource created to expose the BO from other library
        ResourceNode resource = new ResourceNode( ln, bo );
        new ResourceBuilder().build( resource, bo );
        assertTrue( "Resource must have been created.", resource != null );
        ml.check( ln, true );

        // Then - theGroup parameter group with reference and 2 parameters
        List<ParamGroup> allPGs = resource.getParameterGroups( false );
        assert allPGs.size() == 1; // There should only be one created above
        ParamGroup theGroup = allPGs.get( 0 );

        Node refFacet = theGroup.getFacetRef();
        assertTrue( "Must have a non-null reference facet.", refFacet != null );
        LOGGER.debug( "PG " + theGroup + " has " + theGroup.getChildren().size() + " parameters" );
        assert theGroup.getChildren().size() == 2; // Must be built that way
        for (Node p : theGroup.getChildren())
            assert p instanceof ResourceParameter;
        // assert pg and params have ref obj and names
        ml.check();

        // Then - the tl parameter group must have 2 children
        TLParamGroup tlPG0 = resource.getTLModelObject().getParamGroup( "ID" );
        List<TLParameter> tlGrp0 = tlPG0.getParameters();
        assertTrue( "TL Parameter group must have 2 children.", tlGrp0.size() == 2 );

        //
        // When - testlib is closed
        //
        LOGGER.debug( "Ready to close library containing resource subject." );
        pc.remove( (LibraryNavNode) lcn.getParent() );

        // Then -
        assertTrue( "Test is invalid if the reference facet is not null.", theGroup.getFacetRef() == null );
        assert theGroup.getChildren().size() == 2; // Must remain that way
        BusinessObjectNode boRef = resource.getSubject();
        assertTrue( "Resource must not have a business object ref.", boRef == null );

        // FIXME
        //
        // Then - the tl parameter group must have 2 children
        TLParamGroup tlPG1 = resource.getTLModelObject().getParamGroup( "ID" );
        List<TLParameter> tlGrp1 = tlPG1.getParameters();
        assertTrue( "TL Parameter group must have 2 children.", tlGrp1.size() == 2 );

        // theGroup is NOT valid but check is OK
        ml.check( theGroup, false ); // TODO - make this fail
        // the resource is NOT valid - this will fail
        // ml.check(resource, false);

        for (Node p : theGroup.getChildren()) {
            assert p instanceof ResourceParameter;
            TLParameter tlParam = ((ResourceParameter) p).getTLModelObject();
            assertTrue( "Must have tl parameter.", tlParam != null );
            // TLModelElement tlRef = (TLModelElement) tlParam.getFieldRef();
            // assertTrue("Must have a field refernece.", tlRef != null);
            // assertTrue("TL Owning model of field reference must be null.", tlRef.getOwningModel() == null);
            assertTrue( "TL parameter must have field ref name.", !tlParam.getFieldRefName().isEmpty() );
        }

        // Finally - reload library with business object
        LibraryNode testLib2 = new LoadFiles().loadFile7( defaultProject );
        ml.check( testLib2, true );

        // Then - business object reference must be OK
        BusinessObjectNode boRef2 = resource.getSubject();
        assertTrue( "Resource must have a business object ref.", boRef2 != null );
        TLBusinessObject boTlRef2 = resource.getTLModelObject().getBusinessObjectRef();
        assertTrue( "Resource must have a business object ref.", boTlRef2 != null );
        assertTrue( "Resource must have a business object ref.", resource.getSubject() != null );

        // Then - the parameter group node model has not changed
        ParamGroup theGroup2 = resource.getParameterGroups( true ).get( 0 );
        assertTrue( "The resource must have the same parameter group.", theGroup2 == theGroup );
        assertTrue( "The parameter group must have 2 parameters.", theGroup.getChildren().size() == 2 );

        // FIXME
        //
        // Then - the tl parameter group must have 2 children
        TLParamGroup tlPG = resource.getTLModelObject().getParamGroup( "ID" );
        List<TLParameter> tlGrp = tlPG.getParameters();
        assertTrue( "TL Parameter group must have 2 children.", tlGrp.size() == 2 );

        // Then - each parameter has a field reference to an object in the tl model
        for (Node p : theGroup.getChildren()) {
            assert p instanceof ResourceParameter;
            assertTrue( "Must have tl object.", ((ResourceParameter) p).getTLModelObject() != null );
            TLModelElement tlRef2 = (TLModelElement) ((ResourceParameter) p).getTLModelObject().getFieldRef();
            assertTrue( "Must have a field refernece.", tlRef2 != null );
            assertTrue( "TL Owning model of field reference must NOT be null.", tlRef2.getOwningModel() != null );
        }

        ml.check();
    }

    public void check(ResourceNode resource) {
        LOGGER.debug( "Checking resource: " + resource );

        Assert.assertTrue( resource instanceof ResourceNode );
        assert (Node.GetNode( resource.getTLModelObject() ) == resource);

        // Validate model and tl object
        assertTrue( resource.getTLModelObject() instanceof TLResource );
        assertNotNull( resource.getTLModelObject().getListeners() );
        TLResource tlr = resource.getTLModelObject();

        // Validate that the resource is in the where used list for its subject
        if (!resource.isAbstract()) {
            assertTrue( "Must have a subject.", resource.getSubject() != null );
            assertTrue( "Subject must have resource in its where assigned list.",
                resource.getSubject().getWhereAssigned().contains( resource ) );
            // LOGGER.debug("Subject must have resource in its where assigned list: "
            // + resource.getSubject().getWhereAssigned().contains(resource));
        }

        // Make sure it is in the library
        assertTrue( "Must have library set.", resource.getLibrary() != null );
        List<TypeUser> users = resource.getLibrary().getDescendants_TypeUsers();
        assertTrue( "Must be in library.", users.contains( resource ) );
        if (tlr.getOwningLibrary() != null)
            Assert.assertNotNull( resource.getLibrary() );

        Object o;
        for (ResourceMemberInterface rmi : resource.getActionFacets())
            check( rmi );
        for (ResourceMemberInterface rmi : resource.getActions()) {
            check( rmi );
            for (Node child : rmi.getChildren())
                check( (ResourceMemberInterface) child );
        }
        for (ResourceMemberInterface rmi : resource.getParameterGroups( false )) {
            check( rmi );
            for (Node child : rmi.getChildren())
                check( (ResourceMemberInterface) child );
        }

        o = tlr.getBusinessObjectRef();
        o = tlr.getBusinessObjectRefName();
        o = tlr.getBaseNamespace();
        o = tlr.getBasePath();
        o = tlr.getExtension();
        o = tlr.getListeners();
        o = tlr.getLocalName();
        o = tlr.getName();
        o = tlr.getNamespace();
        o = tlr.getParentRefs();
        o = tlr.getVersion();

    }

    public void check(ResourceMemberInterface resource) {
        // LOGGER.debug("Checking " + resource + " " + resource.getClass().getSimpleName());
        assert resource.getParent() != null;
        assert resource.getName() != null;
        assert resource.getLabel() != null;
        assert resource.getTLModelObject() != null;
        assert resource.getTLModelObject().getListeners() != null;
        assert !resource.getTLModelObject().getListeners().isEmpty();
        assert Node.GetNode( resource.getTLModelObject() ) == resource;
        resource.getFields(); // don't crash
    }
}

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
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.whereused.TypeProviderWhereUsedNode;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class WhereUsedNodeTests extends BaseTest {
    private static final String USER_NAME_TE2 = "TE2";

    static final Logger LOGGER = LoggerFactory.getLogger( WhereUsedNodeTests.class );

    TypeProvider emptyNode = null;
    TypeProvider sType = null;

    @Before
    public void beforeEachOfTheseTests() {
        emptyNode = (TypeProvider) ModelNode.getEmptyNode();
        sType = (TypeProvider) NodeFinders.findNodeByName( "date", ModelNode.XSD_NAMESPACE );
    }

    @Test
    public void WU_ConstructorsTests() {

    }

    @Test
    public void WU_MockLibraryTest() {
        // Given - a library
        LibraryNode ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );

        // Given a VWA to assign to various elements.
        VWA_Node vwa = ml.addVWA_ToLibrary( ln, "Vwa1" );
        TypeProviderWhereUsedNode wu = vwa.getWhereUsedNode();
        check( wu );

        // Given a business, choice and core object with one of each contextual facet
        BusinessObjectNode bo1 = ml.addBusinessObjectToLibrary( ln, "bo" );
        ChoiceObjectNode ch1 = ml.addChoice( ln, "Ch1" );
        CoreObjectNode co1 = ml.addCoreObjectToLibrary( ln, "Co1" );

        // Assure the mock library created is valid
        ml.check( bo1 );

        ElementNode e1 = new ElementNode( bo1.getFacet_Summary(), "E1" );
        ElementNode e2 = new ElementNode( ch1.getFacet_Shared(), "Ch1" );
        ElementNode e3 = new ElementNode( co1.getFacet_Summary(), "Co1" );
        e1.setAssignedType( vwa );
        e2.setAssignedType( vwa );
        e3.setAssignedType( vwa );

        TypeProviderWhereUsedNode wu1 = vwa.getWhereUsedNode();
        assertNotNull( wu1 );
        check( wu1 );
    }

    // load from library tests
    @Test
    public void WU_LibraryLoadTests() throws Exception {
        lf.loadTestGroupA( mc );
    }

    // Simulate process in addMOChildren
    // load from library tests
    @Test
    public void WU_LibraryLoadTests_v16() throws Exception {
        lf.loadFile_FacetBase( defaultProject );
    }

    /**
     * all tests to be used in these tests and by other junits
     */
    public void check(WhereUsedNode<?> wu) {
        check( wu, true );
    }

    public void check(WhereUsedNode<?> wu, boolean validate) {

        Collection<TypeUser> assignedKids = null;

        // Check owner
        if (wu instanceof TypeProviderWhereUsedNode) {
            assert wu.getOwner() instanceof TypeProvider;
            assignedKids = ((TypeProvider) wu.getOwner()).getWhereAssigned();
        }

        // Is assertions - no NPE
        wu.isLibraryMemberContainer();
        wu.isEditable();

        // check name and label
        String s = wu.getName();
        s = wu.getLabel();

        // Parent Links

        // Children
        List<Node> kids = wu.getChildren();
        assertTrue( kids != null );
        assertTrue( wu.hasChildren() != kids.isEmpty() );
        // Assure tree children's presence is reported as needed in tree content provider.
        List<Node> tKids = wu.getTreeChildren( true );
        assertTrue( tKids != null );
        assertTrue( wu.hasTreeChildren( true ) != tKids.isEmpty() );

        // where used should include extensions and assignments
        assertTrue( "Must have atleast as many tree kids as where assigned.", tKids.size() >= assignedKids.size() );

        // Check all the children
    }

}

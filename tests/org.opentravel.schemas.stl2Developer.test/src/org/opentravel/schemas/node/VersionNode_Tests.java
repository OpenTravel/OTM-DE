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
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class VersionNode_Tests extends BaseProjectTest {
    private static final Logger LOGGER = LoggerFactory.getLogger( VersionNode_Tests.class );

    LibraryNode ln_inChain;
    LibraryChainNode lcn;

    public LoadFiles lf = new LoadFiles();
    public MockLibrary ml = new MockLibrary();
    public LibraryNode ln = null;

    @Before
    public void beforeEachVersionNodeTests() {
        // runs super beforeEachTest() first
        LOGGER.debug( "Before Each VersionNode Test" );
        ln = ml.createNewLibrary( "http://www.test.com/test1", "test1", defaultProject );
        ln_inChain = ml.createNewLibrary( "http://www.test.com/test1c", "test1c", defaultProject );
        lcn = new LibraryChainNode( ln_inChain );
    }

    @Test
    public void constructor() {
        SimpleTypeNode simple = new SimpleTypeNode( new TLSimple() );
        simple.setName( "Barney" );

        VersionNode ve = new VersionNode( null );
        VersionNode vc = new VersionNode( simple );

        assertTrue( "Version node must not be empty.", vc.get() != null );
        // Then - simple has no parent or library so neither should version node
        assertTrue( "Version node must not have library.", vc.getLibrary() == null );
        assertTrue( "Version node must not have parent.", vc.getParent() == null );

        // When - simple is in a library
        ln = ml.createNewLibrary_Empty( defaultProject.getNamespace(), "test", defaultProject );
        simple = new SimpleTypeNode( new TLSimple() );
        simple.setName( "Fred" );
        ln.addMember( simple );

        vc = new VersionNode( simple );
        // Then - simple has no parent or library so neither should version node
        assertTrue( "Version node must not be empty.", vc.get() != null );
        assertTrue( "Version node must have library.", vc.getLibrary() != null );
        assertTrue( "Version node must have parent.", vc.getParent() != null );

    }

    @Test
    public void versionNode_AddTests() {
        // Given - 3 simple objects
        ComponentNode s1 = ml.addSimpleTypeToLibrary( ln, "s_1" );
        ComponentNode s2 = ml.addSimpleTypeToLibrary( ln, "s_2" );
        ComponentNode s3 = ml.addSimpleTypeToLibrary( ln, "s_3" );
        assertTrue( "S1 must  have library.", s1.getLibrary() != null );

        // When - added to empty version node
        VersionNode v = new VersionNode( s1 );
        // v.add(s1);
        // Then
        assertTrue( s1.getVersionNode() == v );
        assertTrue( v.getAllVersions().contains( s1 ) );

        // When - added to aggregate
        ln_inChain.getChain().getSimpleAggregate().add( s2 );
        // Then - version node created containing s2
        assertTrue( s2.getVersionNode() != null );
        assertTrue( s2.getVersionNode().getAllVersions().contains( s2 ) );

        // When - added to library chain node
        ln_inChain.getChain().add( s3 );
        // Then - version node created containing s3
        assertTrue( s3.getVersionNode() != null );
        assertTrue( s3.getVersionNode().getAllVersions().contains( s3 ) );
    }

    @Test
    public void projectLoadTest() {
        ProjectNode pn = lf.loadVersionTestProject( pc ); // hits the opentravel repo
        assertTrue( pn != null );

        // Pre-check assertions
        List<LibraryNode> libs = pn.getLibraries();
        if (libs.size() < 3) {
            LOGGER.error( "Error is in project setup or tear down. Runs green when run alone." );
            return;
        }
        assertTrue( "Must load more than 3 libraries. ", libs.size() >= 3 );

        LibraryNavNode lnn = (LibraryNavNode) pn.getChildren().get( 0 );
        assertTrue( lnn != null );
        LibraryChainNode lcn = (LibraryChainNode) lnn.getChildren().get( 0 );
        assertTrue( lcn != null );
        VersionAggregateNode van = lcn.getVersions();
        assertTrue( "Version aggregate has 3 libraries.", van.getChildren().size() == 3 );
        AggregateNode ca = lcn.getComplexAggregate();
        assertTrue( !ca.getChildren().isEmpty() );

        // Find the business object
        BusinessObjectNode bo = null;
        for (Node n : ca.getChildren()) {
            assertTrue( "Aggregate children must be version nodes.", n instanceof VersionNode );
            if (((VersionNode) n).get() instanceof BusinessObjectNode)
                bo = (BusinessObjectNode) ((VersionNode) n).get();
        }
        assertTrue( bo != null );

        // Check version node
        VersionNode vn = bo.getVersionNode();
        assertTrue( "BO must have a version node.", vn != null );
        assertTrue( "BO parent must be a nav node.", bo.getParent() instanceof NavNode );
        assertTrue( "BO must be version list.", vn.getAllVersions().contains( bo ) );
        assertTrue( "BO must be at head of version chain.", vn.get() == bo );
        assertTrue( "There must be a previous version.", vn.getPreviousVersion() != null );
        assertTrue( "Version node previous must NOT be bo.", vn.getPreviousVersion() != bo );
        assertTrue( "Version node previous must be a child of vn.",
            vn.getAllVersions().contains( vn.getPreviousVersion() ) );
        // Single VN for all versions of same object.
        for (Node c : vn.getAllVersions()) {
            assertTrue( "VN Child head must be bo.", c.getVersionNode().get() == bo );
            assertTrue( "VN Child must share version node.", c.getVersionNode() == vn );
        }

    }
}

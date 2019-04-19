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

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TestTypes;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave
 * 
 */
public class NodeFactoryTest extends BaseProjectTest {
    private static final Logger LOGGER = LoggerFactory.getLogger( TestTypes.class );

    LoadFiles lf = new LoadFiles();
    MockLibrary ml = new MockLibrary();
    String NS = "http://example.com/test";
    LibraryNode ln = null;
    ProjectNode defaultProject;
    int nodeCount;

    // From baseProjecTest
    // rc, mc, pc, testProject
    // MainController mc;
    // DefaultProjectController pc;

    @Before
    public void beforeAllTests() throws Exception {
        LOGGER.debug( "Initializing Test Setup." );
        defaultProject = pc.getDefaultProject();
    }

    @Test
    public void NF_CreateLibraryMemberTests() {
        ln = ml.createNewLibrary_Empty( NS, "T", defaultProject );

        // When - each member type added to the library
        for (LibraryMember mbr : createAllTLLibraryMembers()) {
            ln.addMember( NodeFactory.newLibraryMember( mbr ) );
        }
        // Then - the library is well formed but not valid
        ml.check( ln, false );
    }

    public List<LibraryMember> createAllTLLibraryMembers() {
        ArrayList<LibraryMember> mbrs = new ArrayList<LibraryMember>();
        mbrs.add( new TLValueWithAttributes() );
        TLBusinessObject tlb = new TLBusinessObject();
        tlb.setName( "BOName" );
        mbrs.add( tlb );
        TLCoreObject tlcr = new TLCoreObject();
        tlcr.setName( "CoreName" );
        mbrs.add( tlcr );
        TLChoiceObject tlch = new TLChoiceObject();
        tlch.setName( "ChoiceName" );
        mbrs.add( tlch );
        TLOpenEnumeration tleo = new TLOpenEnumeration();
        tleo.setName( "OpenName" );
        mbrs.add( tleo );
        TLClosedEnumeration tlec = new TLClosedEnumeration();
        tlec.setName( "ClosedName" );
        mbrs.add( tlec );
        mbrs.add( new TLExtensionPointFacet() );
        TLResource tlr = new TLResource();
        tlr.setBusinessObjectRef( tlb );
        tlr.setName( "ResourceName" );
        mbrs.add( tlr );
        TLService tlsv = new TLService();
        tlsv.setName( "ServiceName" );
        mbrs.add( tlsv );
        TLSimple tls = new TLSimple();
        tls.setName( "SimpleName" );
        mbrs.add( tls );

        TLContextualFacet tlcf = new TLContextualFacet();
        tlcf.setFacetType( TLFacetType.CUSTOM );
        tlcf.setName( "CustomName" );
        mbrs.add( tlcf );
        tlcf = new TLContextualFacet();
        tlcf.setFacetType( TLFacetType.QUERY );
        tlcf.setName( "QueryName" );
        mbrs.add( tlcf );
        tlcf = new TLContextualFacet();
        tlcf.setFacetType( TLFacetType.CHOICE );
        tlcf.setName( "ChoiceFacetName" );
        mbrs.add( tlcf );
        tlcf = new TLContextualFacet();
        tlcf.setFacetType( TLFacetType.UPDATE );
        tlcf.setName( "UpdateName" );
        mbrs.add( tlcf );

        return mbrs;
    }

    // TODO - add tests for other methods
    @Test
    public void guiTypeAccess() {}
}

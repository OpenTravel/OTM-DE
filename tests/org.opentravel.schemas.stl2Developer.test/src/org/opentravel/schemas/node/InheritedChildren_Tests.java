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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.handlers.children.FacetProviderChildrenHandler;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.InheritedElementNode;
import org.opentravel.schemas.node.properties.InheritedEnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 * @see ChoiceObjectTests#ChoiceFacetsTests()
 * 
 */
public class InheritedChildren_Tests {
    private final static Logger LOGGER = LoggerFactory.getLogger( ComponentNode.class );

    ModelNode model = null;
    TestNode tn = new NodeTesters().new TestNode();
    LoadFiles lf = new LoadFiles();
    Library_FunctionTests lt = new Library_FunctionTests();
    MockLibrary ml = null;
    LibraryNode ln = null;
    MainController mc;
    DefaultProjectController pc;
    ProjectNode defaultProject;
    BusinessObjectNode baseBO, extensionBO;

    @Before
    public void beforeAllTests() {
        mc = OtmRegistry.getMainController();
        ml = new MockLibrary();
        pc = (DefaultProjectController) mc.getProjectController();
        defaultProject = pc.getDefaultProject();
    }

    @After
    public void afterEachTest() {
        defaultProject.closeAll();
        pc.closeAll();
        LOGGER.debug( "After ran." );
        assert defaultProject.getLibraries().isEmpty();
    }

    public void check(InheritedElementNode ie) {

        PropertyNode baseElement = ie.getInheritedFrom();
        assertTrue( "Must be inherited", ie.isInherited() );
        assertTrue( "Must inherit from base", ie.getInheritedFrom() == baseElement );
        assertTrue( "Must not have same parent.", ie.getParent() != baseElement.getParent() );
        assertTrue( "Must have same name.", ie.getName().equals( baseElement.getName() ) );
        if (baseElement instanceof TypeUser)
            assertTrue( "Must have same type.", ie.getAssignedType() == ((TypeUser) baseElement).getAssignedType() );
        assertTrue( "Must have same TLModelObject.", ie.getTLModelObject() == baseElement.getTLModelObject() );

        new PropertyNodeTest().check( ie );
    }

    public void check(InheritedEnumLiteralNode ie) {

        PropertyNode baseElement = ie.getInheritedFrom();
        assertTrue( "Must be inherited", ie.isInherited() );
        assertTrue( "Must inherit from base", ie.getInheritedFrom() == baseElement );
        assertTrue( "Must not have same parent.", ie.getParent() != baseElement.getParent() );
        assertTrue( "Must have same name.", ie.getName().equals( baseElement.getName() ) );
        assertTrue( "Must have same TLModelObject.", ie.getTLModelObject() == baseElement.getTLModelObject() );

        new PropertyNodeTest().check( ie );
    }

    @Test
    public void IC_inheritedEnum() {
        ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );

        // Given - enums in a library, each has 1 literal
        EnumerationOpenNode baseEO = ml.addOpenEnumToLibrary( ln, "BaseEO" );
        EnumerationOpenNode exEO = ml.addOpenEnumToLibrary( ln, "ExEO" );
        EnumerationClosedNode baseEC = ml.addClosedEnumToLibrary( ln, "BaseEC" );
        EnumerationClosedNode exEC = ml.addClosedEnumToLibrary( ln, "ExEC" );

        // When extended
        exEO.setExtension( baseEO );
        exEC.setExtension( baseEC );

        // Then - extended enumerations must have inherited children
        assertTrue( "Must have inherited literal.", !exEO.getInheritedChildren().isEmpty() );
        assertTrue( "Must have inherited literal.", !exEC.getInheritedChildren().isEmpty() );
        for (Node ic : exEO.getInheritedChildren())
            check( (InheritedEnumLiteralNode) ic );
        for (Node ic : exEC.getInheritedChildren())
            check( (InheritedEnumLiteralNode) ic );

    }

    @Test
    public void IC_inheritedEnum_ManagedLibrary() {
        ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );

        Enumeration_Tests eTests = new Enumeration_Tests();
        TLOpenEnumeration openTL = (TLOpenEnumeration) eTests.createTL( true, "BaseOTL" );
        TLOpenEnumeration exOTL = (TLOpenEnumeration) eTests.createTL( true, "ExOTL" );

        // Given - a managed library
        LibraryChainNode lcn = new LibraryChainNode( ln );
        ln.setEditable( true );
        assert ln.isEditable();
        // Given - 2 enums each with 1 literal in managed library,
        EnumerationOpenNode baseEO = new EnumerationOpenNode( openTL );
        EnumerationOpenNode exEO = new EnumerationOpenNode( exOTL );
        ln.addMember( baseEO );
        ln.addMember( exEO );
        EnumerationClosedNode baseEC = ml.addClosedEnumToLibrary( ln, "BaseEC" );
        EnumerationClosedNode exEC = ml.addClosedEnumToLibrary( ln, "ExEC" );
        assert baseEC.getTLModelObject().getOwningLibrary() != null;
        // The only difference between these two construction methods is when the children are added.
        // When extended
        exEO.setExtension( baseEO );
        exEC.setExtension( baseEC );

        // Then - extended enumerations must have inherited children
        assertTrue( "Must have inherited literal.", !exEC.getInheritedChildren().isEmpty() );
        assertTrue( "Must have inherited literal.", !exEO.getInheritedChildren().isEmpty() );
        for (Node ic : exEO.getInheritedChildren())
            check( (InheritedEnumLiteralNode) ic );
        for (Node ic : exEC.getInheritedChildren())
            check( (InheritedEnumLiteralNode) ic );

    }

    public void setUpExtendedBO() {
        ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );

        // Given -- two business objects. extensionBO extends baseBO
        //
        baseBO = ml.addBusinessObjectToLibrary( ln, "BaseBO" );
        // If a fully populated BO is used it will be invalid because of too many id attributes
        extensionBO = ml.addBusinessObjectToLibrary_Empty( ln, "ExtensionBO" );
        extensionBO.setExtension( baseBO );
        assertTrue( "Must extension must extend base.", !extensionBO.getExtendsTypeName().isEmpty() );
        assertTrue( "Base where extended must contain extension.",
            baseBO.getWhereExtendedHandler().getWhereExtended().contains( extensionBO ) );
        assertTrue( "Extension base type must be baseBO.", extensionBO.getExtensionBase() == baseBO );
    }

    @Test
    public void IC_addCustomToBase_V15_Test() {
        // Given extensionBO extends baseBO
        setUpExtendedBO();

        // Then - inherited property children
        List<Node> inherited = extensionBO.getFacet_Summary().getInheritedChildren();
        assertTrue( "Must have inherited property.", !inherited.isEmpty() );
        assertTrue( "All base properties must be inherited.",
            baseBO.getFacet_Summary().getChildren().size() == inherited.size() );
        for (Node i : inherited) {
            assertTrue( i.isInherited() );
            assertTrue( "Must inherit from another node.", ((InheritedInterface) i).getInheritedFrom() != null );
            assertTrue( "Parent must be ex facet.", i.getParent() == extensionBO.getFacet_Summary() );
        }

        // Then - inherited facets
        inherited = extensionBO.getInheritedChildren();
        assertTrue( "Must have inherited facets.", !inherited.isEmpty() );
        assertTrue( "Only and all contextual facets must be inherited.",
            baseBO.getContextualFacets( true ).size() == inherited.size() );
        for (Node i : inherited) {
            assertTrue( i.isInherited() );
            // Version 1.5 does not use contributed which implements inherited interface
            assertTrue( "Parent must be ex facet (v15).", i.getParent() == extensionBO );
        }

        // When - add a facet to the base BO
        baseBO.addFacet( "C2", TLFacetType.CUSTOM );
        // Then
        List<TLContextualFacet> inf =
            FacetCodegenUtils.findGhostFacets( extensionBO.getTLModelObject(), TLFacetType.CUSTOM );
        assertTrue( "TL_Ghosts must be found.", !inf.isEmpty() );
        List<Node> iKids = extensionBO.getInheritedChildren();
        assertTrue( "extended object has inherited children.", !extensionBO.getInheritedChildren().isEmpty() );
        assertTrue( "Must find new inherited facet.", extensionBO.findChildByName( "C2" ) != null );
        // LOGGER.debug("Done");
    }

    @Test
    public void IC_addCustomToBase_Test() {
        // Given extensionBO extends baseBO
        setUpExtendedBO();

        // Then - inherited property children
        List<Node> inherited = extensionBO.getFacet_Summary().getInheritedChildren();
        assertTrue( "Must have inherited property.", !inherited.isEmpty() );
        assertTrue( "All base properties must be inherited.",
            baseBO.getFacet_Summary().getChildren().size() == inherited.size() );
        for (Node i : inherited) {
            assertTrue( i.isInherited() );
            assertTrue( "Must inherit from another node.", ((InheritedInterface) i).getInheritedFrom() != null );
            assertTrue( "Parent must be ex facet.", i.getParent() == extensionBO.getFacet_Summary() );
        }

        // Then - inherited facets
        inherited = extensionBO.getInheritedChildren();
        assertTrue( "Must have inherited facets.", !inherited.isEmpty() );
        // Then - read again to verify caching is correct
        inherited = extensionBO.getInheritedChildren();
        assertTrue( "Must have inherited facets.", !inherited.isEmpty() );
        assertTrue( "Only and all contextual facets must be inherited.",
            baseBO.getContextualFacets( true ).size() == inherited.size() );
        for (Node i : inherited) {
            assertTrue( i instanceof ContributedFacetNode );
            assertTrue( i.isInherited() );
            assertTrue( "Must inherit from another node.", ((InheritedInterface) i).getInheritedFrom() != null );
            assertTrue( ((ContributedFacetNode) i).getContributor() != null );
            assertTrue( "Parent must be ex facet.",
                ((ContributedFacetNode) i).getContributor().getParent() == extensionBO.getParent() );
            assertTrue( "Parent must contain contibuted facet.",
                extensionBO.getContextualFacets( true ).contains( ((ContributedFacetNode) i).getContributor() ) );
        }

        // When - add a facet to the base BO
        baseBO.addFacet( "C2", TLFacetType.CUSTOM );
        // Then
        List<TLContextualFacet> inf =
            FacetCodegenUtils.findGhostFacets( extensionBO.getTLModelObject(), TLFacetType.CUSTOM );
        assertTrue( "TL_Ghosts must be found.", !inf.isEmpty() );
        List<Node> iKids = extensionBO.getInheritedChildren();
        assertTrue( "extended object has inherited children.", !extensionBO.getInheritedChildren().isEmpty() );
        assertTrue( "Must find new inherited facet.", extensionBO.findChildByName( "C2" ) != null );
        // LOGGER.debug("Done");
    }

    @Test
    public void IC_settingBase_Test() {
        setUpExtendedBO();
        assertTrue( "Library must be valid.", ln.isValid() ); // validates TL library
        BusinessObjectNode bo2 = ml.addBusinessObjectToLibrary_Empty( ln, "Bo2" );
        FacetProviderNode sf = bo2.getFacet_Summary();
        List<?> children = sf.getChildren();
        bo2.setExtension( baseBO );
        Assert.assertEquals( sf, bo2.getFacet_Summary() );
        List<?> inherited = sf.getInheritedChildren();
        LOGGER.debug( "Done" );
    }

    @Test
    public void IC_inheritedElementNodeTests() {
        // Given - two BO, baseBO and extensionBO
        setUpExtendedBO();
        ElementNode baseElement = new ElementNode( new TLProperty(), baseBO.getFacet_Summary() );
        TypeProvider simpleType = ml.getSimpleTypeProvider();
        baseElement.setAssignedType( simpleType );
        baseElement.setName( "Ele1" );
        String elementName = baseElement.getName();

        // Constructor
        TypedPropertyNode ie = new InheritedElementNode( baseElement, extensionBO.getFacet_Summary() );
        assertTrue( "Must inherit from base", ((FacadeInterface) ie).get() == baseElement );
        assertTrue( "Must have correct parent.", ie.getParent() == extensionBO.getFacet_Summary() );
        assertTrue( "Must have same name.", ie.getName().equals( elementName ) );
        assertTrue( "Must have same type.", ie.getAssignedType() == simpleType );
        ml.check( ie );

        // Factory
        ie = (TypedPropertyNode) NodeFactory.newInheritedProperty( baseElement, extensionBO.getFacet_Summary() );
        ml.check( ie );
    }

    @Test
    public void IC_inheritedPropertiesTests() {
        // Given - two BO, baseBO and extensionBO
        setUpExtendedBO();

        // Children handler
        List<Node> fromKids = baseBO.getFacet_Summary().getChildren();
        List<Node> kids = extensionBO.getFacet_Summary().getInheritedChildren();
        assertTrue( "Must inherit all properties.", fromKids.size() == kids.size() );
        Collection<ModelElementListener> listeners = baseBO.getFacet_Summary().getTLModelObject().getListeners();
        assertTrue( listeners.size() > 0 );

        for (Node n : kids)
            if (n instanceof InheritedElementNode)
                check( (InheritedElementNode) n );

        // When more properties are added
        ml.addAllProperties( baseBO.getFacet_Summary() );
        // extensionBO.getFacet_Summary().getChildrenHandler().clear(); // should not be needed
        List<Node> nKids = extensionBO.getFacet_Summary().getInheritedChildren();
        assert !nKids.isEmpty();
        // Then kids all check OK
        for (Node n : nKids)
            if (n instanceof InheritedElementNode)
                check( (InheritedElementNode) n );
        assertTrue( "Must have more inherited properties.", nKids.size() > kids.size() );
    }

    // Test changing children in contextual facet and assure all properties are OK
    @Test
    public void IC_inheritedChange_Tests() {
        // Given - two BO, baseBO and extensionBO
        setUpExtendedBO();
        // Given - a template to simulate adding properties by DND
        TypedPropertyNode template = new ElementNode( baseBO.getFacet_Summary(), "Template" );
        template.setAssignedType( ml.getXsdDecimal() );

        // Then - no child may be deleted
        assertNotDeleted( baseBO.getContextualFacets( true ) );
        assertNotDeleted( extensionBO.getContextualFacets( true ) );

        PropertyNode np = null;
        AbstractContextualFacet baseCF = baseBO.getContextualFacets( false ).get( 0 );
        AbstractContextualFacet exCF = extensionBO.getContextualFacets( true ).get( 0 );

        // When - adding a property
        np = baseCF.createProperty( template );

        // Then - no child may be deleted
        assertNotDeleted( baseBO.getContextualFacets( true ) );
        assertNotDeleted( extensionBO.getContextualFacets( true ) );
        // Then - assure reading is stable and has same result
        assertNotDeleted( baseBO.getContextualFacets( true ) );
        assertNotDeleted( extensionBO.getContextualFacets( true ) );

        // When - deleting the property
        np.delete();

        // Then - no child may be deleted
        assertNotDeleted( baseBO.getContextualFacets( true ) );
        assertNotDeleted( extensionBO.getContextualFacets( true ) );

        // When - adding a lot of properties
        for (AbstractContextualFacet cf : baseBO.getContextualFacets( false ))
            ml.addAllProperties( cf );

        // Then - no child may be deleted
        assertNotDeleted( baseBO.getContextualFacets( true ) );
        assertNotDeleted( extensionBO.getContextualFacets( true ) );
    }

    private void assertNotDeleted(List<AbstractContextualFacet> cfList) {
        for (AbstractContextualFacet cf : cfList) {
            List<Node> kids = cf.getChildren();
            for (Node kid : kids)
                assert !kid.isDeleted();
            kids = cf.getInheritedChildren();
            for (Node kid : kids) {
                assert kid instanceof InheritedInterface;
                assert !kid.isDeleted();
            }
        }
    }

    // Test adding/deleting children in contextual facet and assure all properties are OK
    @Test
    public void IC_inheritedCFChange_Tests() {
        // Given - two BO, baseBO and extensionBO
        setUpExtendedBO();

        List<AbstractContextualFacet> excfs = extensionBO.getContextualFacets( true );
        assert !excfs.isEmpty();

        // When - add a custom facet to base bo
        AbstractContextualFacet nf = baseBO.addFacet( "BaseCustom1", TLFacetType.CUSTOM );
        List<AbstractContextualFacet> excfs2 = extensionBO.getContextualFacets( true );
        assert excfs2.size() > excfs.size();

        // When - deleted
        nf.delete();

        excfs2 = extensionBO.getContextualFacets( true );
        assert excfs2.size() == excfs.size();
    }

    /**
     * getExtendsType is used in children handler to find inherited children.
     */
    @Test
    public void IC_getExtendsType_Tests() {
        // Given - two BO, baseBO and extensionBO
        setUpExtendedBO();

        Node ext;
        for (AbstractContextualFacet cf : baseBO.getContextualFacets( true )) {
            ext = cf.getExtendsType();
            LOGGER.debug( "ext = " + ext ); // assert ext == null;
            if (cf instanceof InheritedInterface)
                assert ext != null;
            else
                assert ext == null;
        }
        for (Node cf : baseBO.getChildren()) {
            if (!(cf instanceof FacetProviderNode))
                continue;
            ext = cf.getExtendsType();
            LOGGER.debug( "ext = " + ext );
            assert ext == null;
        }
        for (AbstractContextualFacet cf : extensionBO.getContextualFacets( true )) {
            ext = cf.getExtendsType();
            LOGGER.debug( "ext = " + ext );
            if (cf instanceof InheritedInterface)
                assert ext != null;
            else
                assert ext == null;
        }
        for (Node cf : extensionBO.getChildren()) {
            if (!(cf instanceof FacetProviderNode))
                continue;
            ext = cf.getExtendsType();
            LOGGER.debug( "ext = " + ext );
            assert ext != null;
        }
    }

    @Test
    public void IC_inheritedTreeChildren_Tests() {
        // Given - two BO, baseBO and extensionBO
        setUpExtendedBO();
        // Given - all base contextual facets have children
        for (AbstractContextualFacet cf : baseBO.getContextualFacets( false ))
            ml.addAllProperties( cf );

        // Then - make sure contextual facets are in the tree kids.
        List<?> exTreeKids = extensionBO.getChildrenHandler().getTreeChildren( false );
        List<ContributedFacetNode> exCFs = new ArrayList<>();
        assert !exTreeKids.isEmpty();
        for (Object n : exTreeKids)
            if (n instanceof ContributedFacetNode)
                exCFs.add( (ContributedFacetNode) n );
        assert !exCFs.isEmpty();

        // Then - make sure the contextual facets have children
        List<?> cfProps;
        for (ContributedFacetNode c : exCFs) {
            FacetProviderChildrenHandler ch = c.getChildrenHandler();
            List<Node> l1 = ch.get();
            List<Node> l2 = ch.getInheritedChildren();
            AbstractContextualFacet c3 = ((ContributedFacetNode) c.getInheritedFrom()).getContributor();
            cfProps = c.getChildrenHandler().getTreeChildren( true );
            assert !cfProps.isEmpty();
        }
        // When - add a custom facet to base bo
        AbstractContextualFacet nf = baseBO.addFacet( "BaseCustom1", TLFacetType.CUSTOM );
        // Then
        List<?> exTreeKids2 = extensionBO.getChildrenHandler().getTreeChildren( false );
        assert exTreeKids2.size() > exTreeKids.size();

        // When - deleted
        nf.delete();

        // Then
        exTreeKids2 = extensionBO.getChildrenHandler().getTreeChildren( false );
        assert exTreeKids2.size() == exTreeKids.size();
    }

    @Test
    public void IC_inheritedContextualFacet_v16_Tests() {
        // Given - two BO, baseBO and extensionBO
        setUpExtendedBO();
        List<AbstractContextualFacet> baseCFs = baseBO.getContextualFacets( true );
        List<ContributedFacetNode> baseContribs = baseBO.getContributedFacets( true );
        List<Node> bKids = baseBO.getChildren();
        assertTrue( "Version 1.6 must use contributed facets.", !baseContribs.isEmpty() );

        List<AbstractContextualFacet> eCFs = extensionBO.getContextualFacets( true );
        List<ContributedFacetNode> eContribs = extensionBO.getContributedFacets( true );
        List<Node> eKids = extensionBO.getInheritedChildren();

        assertTrue( "Library must now have these inherited facets.", true );

        assertTrue( !eKids.isEmpty() );
    }

    @Test
    public void IC_facetsFiles_v16_Tests() {
        assert defaultProject.getLibraries().isEmpty();

        LibraryNode baseLN = lf.loadFile_FacetBase( defaultProject );
        LibraryNode f1LN = lf.loadFile_Facets1( defaultProject );
        LibraryNode f2LN = lf.loadFile_Facets2( defaultProject );
        ml.check( baseLN );
        ml.check( f1LN );
        ml.check( f2LN );
        baseLN.setEditable( true );
        f1LN.setEditable( true );
        f2LN.setEditable( true );
        assert baseLN.isEditable();
        assert f1LN.isEditable();

        final String EXCHOICE = "ExtFacetTestChoice";
        final String EXBO = "ExtFacetTestBO";
        ChoiceObjectNode ch = (ChoiceObjectNode) baseLN.findLibraryMemberByName( EXCHOICE );
        BusinessObjectNode bo = (BusinessObjectNode) baseLN.findLibraryMemberByName( EXBO );
        ml.check( ch );
        ml.check( bo );
        ChoiceObjectNode chBase = (ChoiceObjectNode) ch.getExtensionBase();
        BusinessObjectNode boBase = (BusinessObjectNode) bo.getExtensionBase();
        assertTrue( "Choice must extend base choice.", chBase != null );
        assertTrue( "Business object must extend base object.", boBase != null );

        List<ContextualFacetNode> cfs = baseLN.getDescendants_ContextualFacets();
        for (Node cf : ch.getInheritedChildren())
            assertTrue( cf.isInherited() );
        for (Node cf : bo.getInheritedChildren())
            assertTrue( cf.isInherited() );
        cfs = baseLN.getDescendants_ContextualFacets();
    }
}

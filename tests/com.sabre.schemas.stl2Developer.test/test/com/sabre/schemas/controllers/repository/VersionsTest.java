package com.sabre.schemas.controllers.repository;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacetType;
import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryItemState;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemas.node.AggregateNode;
import com.sabre.schemas.node.BusinessObjectNode;
import com.sabre.schemas.node.ComplexComponentInterface;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.CoreObjectNode;
import com.sabre.schemas.node.EnumerationClosedNode;
import com.sabre.schemas.node.ExtensionPointNode;
import com.sabre.schemas.node.FacetNode;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.NavNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeEditStatus;
import com.sabre.schemas.node.NodeFinders;
import com.sabre.schemas.node.OperationNode;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.node.SimpleComponentInterface;
import com.sabre.schemas.node.VWA_Node;
import com.sabre.schemas.node.VersionAggregateNode;
import com.sabre.schemas.node.VersionNode;
import com.sabre.schemas.node.properties.AttributeNode;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.IndicatorNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.testUtils.MockLibrary;
import com.sabre.schemas.testers.GlobalSelectionTester;
import com.sabre.schemas.testers.NodeTester;
import com.sabre.schemas.trees.repository.RepositoryNode;
import com.sabre.schemas.utils.LibraryNodeBuilder;

public class VersionsTest extends RepositoryIntegrationTestBase {
    MockLibrary ml = new MockLibrary();
    private BusinessObjectNode sbo = null;
    private BusinessObjectNode bo = null;
    private BusinessObjectNode nbo = null;
    private VWA_Node vwa = null;
    private ExtensionPointNode ep = null;
    private CoreObjectNode co = null;
    private CoreObjectNode core2 = null;
    private LibraryNode majorLibrary = null;
    private LibraryNode minorLibrary = null;
    private LibraryNode patchLibrary = null;
    private LibraryNode secondLib = null;
    private LibraryChainNode chain = null;
    private CoreObjectNode mCo = null;

    int TotalDescendents, ActiveSimple, ActiveComplex, TotalLibraries, MinorComplex;
    private Node xsdStringNode;

    @Override
    public RepositoryNode getRepositoryForTest() {
        for (RepositoryNode rn : rc.getAll()) {
            if (rn.isRemote()) {
                return rn;
            }
        }
        throw new IllegalStateException("Missing remote repository. Check your configuration.");
    }

    @Before
    public void runBeforeEachTest() throws LibrarySaveException, RepositoryException {
        xsdStringNode = NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE);
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");
        majorLibrary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test/T2", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        secondLib = LibraryNodeBuilder.create("TestLibrary2",
                getRepositoryForTest().getNamespace() + "/Test", "prefix2", new Version(1, 0, 0))
                .build(uploadProject, pc);
        chain = rc.manage(getRepositoryForTest(), Collections.singletonList(majorLibrary)).get(0);
        boolean locked = rc.lock(chain.getHead());
        Assert.assertTrue(locked);
        Assert.assertTrue(majorLibrary.isEditable());
        Assert.assertEquals(RepositoryItemState.MANAGED_WIP, chain.getHead().getProjectItem()
                .getState());

        // Create a valid example of each component type
        sbo = ml.addBusinessObjectToLibrary(secondLib, "sbo");
        bo = ml.addBusinessObjectToLibrary(majorLibrary, "testBO");
        co = ml.addCoreObjectToLibrary(majorLibrary, "testCO");
        bo.setExtensible(true);
        vwa = ml.addVWA_ToLibrary(majorLibrary, "testVWA");
        ml.addSimpleTypeToLibrary(majorLibrary, "testSimple");
        ml.addClosedEnumToLibrary(majorLibrary, "testCEnum");
        ml.addOpenEnumToLibrary(majorLibrary, "testOEnum");
        ml.addNestedTypes(majorLibrary);
        core2 = (CoreObjectNode) majorLibrary.findNodeByName("n2");

        ServiceNode svc = new ServiceNode(bo);
        svc.setName(bo.getName() + "_Service");

        ExtensionPointNode ep = new ExtensionPointNode(new TLExtensionPointFacet());
        ep.setExtendsType(sbo.getSummaryFacet());
        majorLibrary.addMember(ep);
        Assert.assertTrue(majorLibrary.isValid()); // you can't version an invalid library.

        TotalDescendents = 11; // Number in whole chain
        TotalLibraries = 3;
        ActiveComplex = 8; // Number in the aggregates
        ActiveSimple = 2;
        MinorComplex = 0;

        // Create locked patch version
        patchLibrary = rc.createPatchVersion(chain.getHead());

        // Adding the simple to the patch causes it to be duplicated then create error finding.
        ml.addSimpleTypeToLibrary(patchLibrary, "simpleInPatch");
        TotalDescendents += 1; // Number in whole chain
        ActiveSimple += 1;
        Assert.assertTrue(chain.isValid()); // you can't version an invalid library.
        checkCounts(chain);
        // Fixed -- OTA-811 -- Tests OK.

        ExtensionPointNode ePatch = new ExtensionPointNode(new TLExtensionPointFacet());
        patchLibrary.addMember(ePatch);
        ePatch.setExtendsType(core2.getSummaryFacet());
        ePatch.addProperty(new IndicatorNode(ePatch, "patchInd"));
        TotalDescendents += 1; // Number in whole chain
        ActiveComplex += 1; // Number in the aggregates
        Assert.assertTrue(chain.isValid()); // you can't version an invalid library.
        checkCounts(chain);
        // Fixed -- OTA-811 -- Tests OK.

        //
        // Create locked minor version. Will contain bo with property from ePatch.
        minorLibrary = rc.createMinorVersion(chain.getHead());
        MinorComplex++; // the new CO from patch EPF
        TotalDescendents++;
        // Make sure the patch library still has the extension point wrapped in a version node.
        Assert.assertTrue(patchLibrary.getComplexRoot().getChildren().contains(ePatch.getParent()));

        // FIXME - OTA-811
        // Find and add to the chain the CoreObject created by roll-up
        // the roll up creates the core but does not add it to the chain correctly.
        // should be done when object is created w/ minor library.
        mCo = null;
        for (Node n : minorLibrary.getDescendants_NamedTypes()) {
            if (n.getName().equals(core2.getName())) {
                mCo = (CoreObjectNode) n;
                break;
            }
        }
        Assert.assertSame(core2, mCo.getExtendsType());
        // Assert.assertTrue(mCo.getParent() instanceof VersionNode);
        if (!(mCo.getParent() instanceof VersionNode)) {
            // Patch until 811 fixed
            VersionNode vn = new VersionNode(mCo);
            ((AggregateNode) chain.getComplexAggregate()).add(mCo);
        }

        checkCounts(chain);
        Assert.assertTrue(chain.isValid()); // you can't version an invalid library.
    }

    @Test
    public void checkMinorVersion() {
        Assert.assertTrue(minorLibrary.isEditable());
        Assert.assertTrue(chain.isEditable());
        Assert.assertFalse(majorLibrary.isEditable());
        Assert.assertEquals(TotalLibraries, chain.getLibraries().size());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
        Assert.assertFalse(minorLibrary.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);

        // verify the core object, co, created in the minor library contains properties from the
        // extension point in the patch.
        Assert.assertNotNull(mCo);
        Assert.assertEquals(1, mCo.getSummaryFacet().getChildren().size());

        // FIXME - https://jira.sabre.com/browse/OTA-811
        // Assert.assertNotNull(mCo.getExtendsType());
    }

    //
    // Test the library level testers.
    //
    @Test
    public void testLibraryTesters() {
        CoreObjectNode nco = createCoreInMinor(); // extends co

        //
        // Chain assertions
        Assert.assertTrue(chain.isMinor());
        Assert.assertFalse(chain.isMajor());
        Assert.assertFalse(chain.isPatch());

        //
        // Major Library
        Assert.assertTrue(majorLibrary.isInChain());
        Assert.assertTrue(majorLibrary.isManaged());
        Assert.assertTrue(majorLibrary.isReadyToVersion());
        Assert.assertTrue(majorLibrary.isMajorVersion());
        Assert.assertTrue(majorLibrary.isMinorOrMajorVersion());
        //
        Assert.assertFalse(majorLibrary.isEditable());
        Assert.assertFalse(majorLibrary.isLocked());
        Assert.assertFalse(majorLibrary.isPatchVersion());
        Assert.assertFalse(majorLibrary.isMinorVersion());
        Assert.assertFalse(chain.isLaterVersion(majorLibrary, minorLibrary));
        Assert.assertFalse(chain.isLaterVersion(majorLibrary, patchLibrary));

        //
        // Minor Library
        Assert.assertTrue(minorLibrary.isInChain());
        Assert.assertTrue(minorLibrary.isManaged());
        Assert.assertTrue(minorLibrary.isReadyToVersion());
        Assert.assertTrue(minorLibrary.isEditable());
        Assert.assertTrue(minorLibrary.isLocked());
        Assert.assertTrue(minorLibrary.isMinorOrMajorVersion());
        Assert.assertTrue(minorLibrary.isMinorVersion());
        Assert.assertTrue(chain.isLaterVersion(minorLibrary, majorLibrary));
        Assert.assertTrue(chain.isLaterVersion(minorLibrary, patchLibrary));
        //
        Assert.assertFalse(minorLibrary.isMajorVersion());
        Assert.assertFalse(minorLibrary.isPatchVersion());

        //
        // patch Library
        Assert.assertTrue(patchLibrary.isInChain());
        Assert.assertTrue(patchLibrary.isManaged());
        Assert.assertTrue(patchLibrary.isReadyToVersion());
        Assert.assertTrue(patchLibrary.isPatchVersion());
        Assert.assertTrue(chain.isLaterVersion(patchLibrary, majorLibrary));
        //
        Assert.assertFalse(patchLibrary.isMajorVersion());
        Assert.assertFalse(patchLibrary.isEditable());
        Assert.assertFalse(patchLibrary.isLocked());
        Assert.assertFalse(patchLibrary.isMinorVersion());
        Assert.assertFalse(patchLibrary.isMinorOrMajorVersion());
        Assert.assertFalse(chain.isLaterVersion(patchLibrary, minorLibrary));

        // LibraryNode based status
        Assert.assertEquals(NodeEditStatus.MANAGED_READONLY, majorLibrary.getEditStatus());
        Assert.assertEquals(NodeEditStatus.MINOR, minorLibrary.getEditStatus());
        Assert.assertEquals(NodeEditStatus.MANAGED_READONLY, patchLibrary.getEditStatus());
        Assert.assertEquals(NodeEditStatus.FULL, secondLib.getEditStatus());

        // Node based NodeEditStatus is based on chain head
        Assert.assertEquals(NodeEditStatus.MINOR, nco.getEditStatus());
        Assert.assertEquals(NodeEditStatus.MINOR, co.getEditStatus());
        Assert.assertEquals(NodeEditStatus.MINOR, bo.getEditStatus());

        //
        // Node object based status
        Assert.assertTrue(nco.isInHead());
        Assert.assertFalse(co.isInHead());
        Assert.assertFalse(bo.isInHead());
        Assert.assertFalse(vwa.isInHead());

        Assert.assertFalse(nco.isNewToChain());
        Assert.assertFalse(co.isNewToChain());
        Assert.assertFalse(bo.isNewToChain());
        Assert.assertFalse(vwa.isNewToChain());

        //
        // Tests used to enable user actions.

        // Editable - should all be true to drive the navView display.
        Assert.assertTrue(nco.isEditable());
        Assert.assertTrue(co.isEditable());
        Assert.assertTrue(bo.isEditable());

        // Delete-able - NodeTester.canDelete() -> Node.isDeletable()
        Assert.assertTrue(nco.isDeleteable());
        Assert.assertFalse(co.isDeleteable());
        Assert.assertFalse(bo.isDeleteable());
        Assert.assertTrue(sbo.isDeleteable());

        // CanAdd - Control for AddNodeHandler. GlobalSelectionTester.canAdd()
        GlobalSelectionTester gst = new GlobalSelectionTester();
        Assert.assertTrue(gst.test(nco, GlobalSelectionTester.CANADD, null, null));
        Assert.assertTrue(gst.test(co, GlobalSelectionTester.CANADD, null, null));
        Assert.assertTrue(gst.test(bo, GlobalSelectionTester.CANADD, null, null));
        Assert.assertFalse(gst.test(vwa, GlobalSelectionTester.CANADD, null, null));
        Assert.assertFalse(gst.test(ep, GlobalSelectionTester.CANADD, null, null));

        // New Component - NodeTester
        NodeTester tester = new NodeTester();
        Assert.assertTrue(tester.test(nco, NodeTester.IS_IN_TLLIBRARY, null, null));
        Assert.assertTrue(tester.test(nco, NodeTester.IS_OWNER_LIBRARY_EDITABLE, null, null));
        Assert.assertTrue(tester.test(co, NodeTester.IS_IN_TLLIBRARY, null, null));
        Assert.assertTrue(tester.test(co, NodeTester.IS_OWNER_LIBRARY_EDITABLE, null, null));
        Assert.assertTrue(tester.test(bo, NodeTester.IS_IN_TLLIBRARY, null, null));
        Assert.assertTrue(tester.test(bo, NodeTester.IS_OWNER_LIBRARY_EDITABLE, null, null));

        // Move - NavigatorMenus.createMoveActionsForLibraries()
        Assert.assertTrue(nco.getLibrary().isMoveable());
        Assert.assertFalse(co.getLibrary().isMoveable());
        Assert.assertFalse(bo.getLibrary().isMoveable());

        // 3 states:
        // in previous version
        // in head w/ base type (extends older version)
        // in head w/o base type (newToHead)
        //
        // if (co.isInHead() && co.getExtendsType() == null || !chain.contains(co.getExtendsType()))

        // AddAliasAction, AssignTypeAction, ChangeAction all use chain.isMajor()
    }

    //
    // Test the children/parent relationships
    //
    @Test
    public void testHeirarchy() {
        CoreObjectNode nco = createCoreInMinor();
        Assert.assertTrue(nco.getParent() instanceof VersionNode);
        Assert.assertTrue(nco.getLibrary() == minorLibrary);
        // TODO - why? if they are always the same, why have version node pointer? Just to save a
        // cast? If so, create a method w/ cast and remove data
        Assert.assertTrue(nco.getParent() == nco.getVersionNode());

        // check head and prev
        Assert.assertTrue(nco.getVersionNode().getNewestVersion() == nco);
        Assert.assertTrue(co.getVersionNode().getNewestVersion() == nco);
        Assert.assertTrue(nco.getVersionNode().getPreviousVersion() == co);
        Assert.assertTrue(co.getVersionNode().getPreviousVersion() == null);

        Node head = chain.getHead();
        Assert.assertTrue(head instanceof LibraryNode);
        Assert.assertTrue(head.getParent().getParent() == chain);
        Assert.assertTrue(head.getChain() == chain);

        // Make sure all versions are present.
        Node versionsAgg = chain.getVersions();
        Assert.assertTrue(versionsAgg instanceof VersionAggregateNode);
        Assert.assertEquals(TotalLibraries, versionsAgg.getChildren().size());
        Assert.assertEquals(TotalLibraries, versionsAgg.getNavChildren().size());
        Assert.assertTrue(versionsAgg.getParent() == chain);
        Assert.assertTrue(versionsAgg.getChain() == chain);

        for (Node lib : versionsAgg.getChildren()) {
            Assert.assertTrue(lib.getParent() == versionsAgg);
            Assert.assertTrue(lib instanceof LibraryNode);
            if (lib == chain.getHead())
                Assert.assertTrue(lib.isEditable());
            else
                Assert.assertFalse(lib.isEditable());
            // Either nav or service nodes.
            checkChildrenClassType(lib, NavNode.class, ServiceNode.class);

            // Check the children of the Nav Nodes and Service Node
            for (Node nn : lib.getChildren()) {
                if (nn instanceof NavNode) {
                    // Nav node children must be version nodes.
                    Assert.assertTrue(nn.getParent() == lib);
                    checkChildrenClassType(nn, VersionNode.class, null);
                    for (Node vn : nn.getChildren()) {
                        // Version nodes wrap their one child
                        Assert.assertTrue(vn.getParent() == nn);
                        Assert.assertEquals(1, vn.getChildren().size());
                        checkChildrenClassType(vn, ComponentNode.class, null);
                        for (Node cc : vn.getChildren())
                            // Check the actual component nodes.
                            Assert.assertTrue(cc.getParent() == vn);
                    }
                } else {
                    // FIXME - operations in the library should be wrapped.
                    checkChildrenClassType(nn, OperationNode.class, null);
                }
            }
        }

        // Check the aggregates
        Node complexAgg = (Node) chain.getComplexAggregate();
        Assert.assertTrue(complexAgg.getParent() == chain);
        checkChildrenClassType(complexAgg, ComplexComponentInterface.class, null);
        for (Node n : complexAgg.getChildren())
            Assert.assertTrue(n.getParent() != complexAgg);

        Node simpleAgg = (Node) chain.getSimpleAggregate();
        Assert.assertTrue(simpleAgg.getParent() == chain);
        checkChildrenClassType(simpleAgg, SimpleComponentInterface.class, null);
        for (Node n : simpleAgg.getChildren())
            Assert.assertTrue(n.getParent() != simpleAgg);

        Node svcAgg = (Node) chain.getServiceAggregate();
        Assert.assertTrue(svcAgg.getParent() == chain);

    }

    private void checkChildrenClassType(Node parent, Class<?> c, Class<?> c2) {
        for (Node n : parent.getChildren()) {
            // n instanceof c.class
            if (c2 != null)
                Assert.assertTrue(c.isAssignableFrom(n.getClass())
                        || c2.isAssignableFrom(n.getClass()));
            else
                Assert.assertTrue(c.isAssignableFrom(n.getClass()));
        }
    }

    //
    // test adding custom facets
    //
    @Test
    public void testFacets() {
        int facetCount = bo.getChildren().size();
        bo.addFacet("custom1", "", TLFacetType.CUSTOM);
        // Adding to bo should fail...in the future it might create a new bo and add it to that.
        Assert.assertEquals(facetCount, bo.getChildren().size());

        // test adding to a new minor version component
        nbo = (BusinessObjectNode) bo.createMinorVersionComponent();
        MinorComplex += 1;
        nbo.isInHead();
        // We should get at least the default context.
        List<String> contextIDs = minorLibrary.getContextIds();
        Assert.assertFalse(contextIDs.isEmpty());
        // Add a custom facet
        nbo.addFacet("c2", contextIDs.get(0), TLFacetType.CUSTOM);
        Assert.assertEquals(4, nbo.getChildren().size());
        Assert.assertEquals(MinorComplex, minorLibrary.getDescendants_NamedTypes().size());
        Assert.assertTrue(chain.isValid());
        nbo.delete();
        MinorComplex -= 1;
        checkCounts(chain);
    }

    @Test
    public void checkNavChildren() {
        // Chain should have 4
        Assert.assertEquals(4, chain.getNavChildren().size());
        checkChildrenClassType(chain, AggregateNode.class, null);

        // VersionAggregate should have 3, one for each library
        Assert.assertEquals(3, chain.getVersions().getNavChildren().size());
        // Libraries should have 2 or 3, simple, complex and service
        Assert.assertEquals(2, patchLibrary.getNavChildren().size());
        Assert.assertEquals(2, minorLibrary.getNavChildren().size());
        Assert.assertEquals(3, majorLibrary.getNavChildren().size());

        // Nav Nodes should ONLY have version
        for (Node nn : patchLibrary.getNavChildren()) {
            Assert.assertTrue(nn instanceof NavNode);
            checkChildrenClassType(nn, VersionNode.class, null);
            // Version nodes should have NO nav children.
            for (Node vn : nn.getNavChildren())
                Assert.assertEquals(0, vn.getNavChildren().size());
        }

        // Aggregates should have Active Simple, Active Complex and Service.
        // This checks both children and then navChildren.
        checkChildrenClassType(((Node) chain.getComplexAggregate()),
                ComplexComponentInterface.class, null);
        for (Node nc : ((Node) chain.getComplexAggregate()).getNavChildren()) {
            Assert.assertTrue(nc instanceof ComplexComponentInterface);
        }
        checkChildrenClassType(((Node) chain.getSimpleAggregate()), SimpleComponentInterface.class,
                null);
        for (Node nc : ((Node) chain.getSimpleAggregate()).getNavChildren()) {
            Assert.assertTrue(nc instanceof SimpleComponentInterface);
        }

    }

    //
    // Test handling of adding and deleting of new objects
    //
    @Test
    public void testAddingAndDeleting() {
        nbo = ml.addBusinessObjectToLibrary(minorLibrary, "nbo");

        // The new bo should be in the minor library, not the base library.
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));

        // Add some other object types
        EnumerationClosedNode nec = ml.addClosedEnumToLibrary(chain.getHead(), "ce2");
        VWA_Node nvwa = ml.addVWA_ToLibrary(chain.getHead(), "vwa2");
        Assert.assertTrue(chain.isValid());

        Assert.assertTrue(nec.isNewToChain());
        Assert.assertTrue(nvwa.isNewToChain());
        Assert.assertTrue(nbo.isNewToChain()); // this object is in previous version.

        // Remove and delete them
        nbo.delete();
        nec.delete();
        nvwa.delete();
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);
    }

    // TODO - test adding more minor chains and adding objects to all of them to verify the prev
    // link.

    @Test
    public void testAddingProperties() {
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());

        CoreObjectNode nco = createCoreInMinor();
        testAddingPropertiesToFacet(nco.getSummaryFacet());

        BusinessObjectNode nbo = createBO_InMinor();
        testAddingPropertiesToFacet(nbo.getDetailFacet());

        // not supported. see jira OTA-789
        // VWA_Node nVwa = createVWA_InMinor();
        // testAddingPropertiesToFacet(nVwa.getAttributeFacet());

        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        Assert.assertTrue(chain.isValid());

        nco.delete();
        nbo.delete();
        TotalDescendents -= 2;
        MinorComplex -= 0; // keep counts accurate
        // Active complex should remain unchanged.
        checkCounts(chain);
    }

    // Adds the removed properties from the facet.
    // Emulate AddNodeHandler and newPropertiesWizard
    private void testAddingPropertiesToFacet(ComponentNode propOwner) {
        int cnt = propOwner.getChildren().size();
        PropertyNode newProp = null;
        if (!propOwner.isVWA_AttributeFacet()) {
            newProp = new ElementNode(new FacetNode(), "np" + cnt++);
            propOwner.addProperty(newProp);
            newProp.setAssignedType(xsdStringNode);
        }
        PropertyNode newAttr = new AttributeNode(new FacetNode(), "np" + cnt++);
        propOwner.addProperty(newAttr);
        newAttr.setAssignedType(xsdStringNode);

        Assert.assertTrue(newProp.getLibrary() != null);
        Assert.assertEquals(cnt, propOwner.getChildren().size());
        Assert.assertTrue(newProp.isDeleteable());
        newProp.delete();
        Assert.assertEquals(--cnt, propOwner.getChildren().size());
    }

    /**
     * Create a new minor version of core: co. Add a property "te2".
     * 
     * Emulates behavior in AddNodeHandler. AddNodeHandler notifies the user then
     * createMinorVersionComponent(). Constructors can not do this because they are needed for
     * initial rendering of the objects and can't do user dialogs/notifications.
     */
    private CoreObjectNode createCoreInMinor() {
        CoreObjectNode nco = (CoreObjectNode) co.createMinorVersionComponent();
        PropertyNode newProp = new ElementNode(nco.getSummaryFacet(), "te2");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        TotalDescendents += 1;
        MinorComplex += 1;

        // Make sure a new CO was created in the newMinor library.
        Assert.assertNotNull(nco);
        Assert.assertFalse(nco.isNewToChain());
        Assert.assertFalse(co.isNewToChain());
        Assert.assertNotNull(nco.getVersionNode().getPreviousVersion());
        Assert.assertEquals(nco.getVersionNode().getPreviousVersion(), co);
        Assert.assertEquals(1, nco.getSummaryFacet().getChildren().size());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nco));
        Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nco));
        Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nco));

        return nco;
    }

    private BusinessObjectNode createBO_InMinor() {
        BusinessObjectNode nbo = (BusinessObjectNode) bo.createMinorVersionComponent();
        PropertyNode newProp = new ElementNode(nbo.getSummaryFacet(), "te2");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        Assert.assertEquals(1, bo.getSummaryFacet().getChildren().size());
        TotalDescendents += 1;
        MinorComplex += 1;

        // Make sure a new CO was created in the newMinor library.
        Assert.assertNotNull(nbo);
        Assert.assertNotNull(nbo.getVersionNode().getPreviousVersion());
        Assert.assertEquals(1, nbo.getSummaryFacet().getChildren().size());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));

        return nbo;
    }

    private VWA_Node createVWA_InMinor() {
        // Not currently supported. See Jira OTA-789
        return null;
        // VWA_Node nVwa = (VWA_Node) vwa.createMinorVersionComponent();
        // Assert.assertNotNull(nVwa);
        // PropertyNode newProp = new AttributeNode(nVwa.getAttributeFacet(), "te2");
        // newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        // Assert.assertEquals(1, bo.getSummaryFacet().getChildren().size());
        // TotalDescendents += 1;
        // MinorComplex += 1;
        //
        // // Make sure a new was created in the newMinor library.
        // Assert.assertNotNull(nVwa);
        // Assert.assertEquals(1, nVwa.getAttributeFacet().getChildren().size());
        // Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nVwa));
        // Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nVwa));
        // Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nVwa));
        //
        // return nVwa;
    }

    @Test
    public void testCopying() {
        nbo = (BusinessObjectNode) bo.clone("_copy");
        // copy should be in the new library
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(chain.isValid());
        nbo.delete();
        checkCounts(chain);
    }

    @Test
    public void testMove() {
        // This will work because moveMember is at the model level. It is used by the controller
        // which applies the business logic if it is valid to move.
        majorLibrary.moveMember(bo, minorLibrary);
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(bo));
        minorLibrary.moveMember(bo, majorLibrary); // put it back
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);

        // Test moving from another library
        nbo = ml.addBusinessObjectToLibrary(secondLib, "secondLibBO");
        secondLib.moveMember(nbo, minorLibrary);
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(secondLib.getDescendants_NamedTypes().contains(nbo));
        nbo.delete();

        // Test moving to another library
        nbo = ml.addBusinessObjectToLibrary(minorLibrary, "newBO");
        minorLibrary.moveMember(nbo, secondLib);
        Assert.assertTrue(secondLib.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(minorLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(chain.getDescendants_NamedTypes().contains(nbo));
        checkCounts(chain);

        Assert.assertTrue(chain.isValid());
    }

    @Test
    public void testDelete() {
        bo.delete(); // Should and does fail.
        Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
        List<?> kids = chain.getComplexAggregate().getChildren();
        Assert.assertTrue(kids.contains(bo));
        checkCounts(chain);
        Assert.assertTrue(chain.isValid());

        //
        // Test deleting properties
        co.getSummaryFacet().getChildren().get(0).delete();
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());

        //
        // Test handling of new object with same name as existing object.
        nbo = ml.addBusinessObjectToLibrary(chain.getHead(), "testBO");
        Assert.assertFalse(minorLibrary.isValid());

        kids = chain.getComplexAggregate().getChildren();
        Assert.assertTrue(kids.contains(nbo));
        Assert.assertFalse(kids.contains(bo)); // was replaced with nbo

        // The new bo should be in the minor library, not the base library.
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));

        // Deleting via GUI should make it valid and replace the old one back into the aggregate
        // model level delete should just do it.
        nbo.delete();
        Assert.assertTrue(minorLibrary.isValid());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
        checkCounts(chain);
        // counts will be wrong.

        // Renaming it should make chain valid
        nbo = ml.addBusinessObjectToLibrary(minorLibrary, "testBO");
        nbo.setName("testBO2");
        Assert.assertTrue(minorLibrary.isValid());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
    }

    @Test
    public void testMajor() {
        // Create major version which makes the minor final.
        Assert.assertTrue(chain.isEditable());
        LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
        TotalDescendents = TotalDescendents - 2; // Rolled up EP and CO
        ActiveComplex--; // Rolled up CO

        Assert.assertFalse(chain.isEditable());
        LibraryChainNode newChain = newMajor.getChain();
        // The extension point will not be in the major. Add a complex to keep counts right.
        BusinessObjectNode majorBO = ml.addBusinessObjectToLibrary(newMajor, "MajorBO");
        checkCounts(newChain);

        for (Node n : newMajor.getDescendants_NamedTypes()) {
            if (n.getName().equals(bo.getName()))
                bo = (BusinessObjectNode) n;
            if (n.getName().equals(co.getName()))
                co = (CoreObjectNode) n;
        }
        Assert.assertTrue(majorBO.isDeleteable());
        Assert.assertTrue(bo.isDeleteable());
        Assert.assertTrue(co.isDeleteable());

    }

    // Remember, getDescendents uses HashMap - only unique nodes.
    private void checkCounts(LibraryChainNode chain) {

        // Make sure all the base objects are accessible.
        List<Node> namedTypes = chain.getDescendants_NamedTypes();
        int namedTypeCnt = chain.getDescendants_NamedTypes().size();
        Assert.assertEquals(TotalDescendents, namedTypeCnt);
        Node x = namedTypes.get(0);

        // Make sure all the types are in the versions aggregate
        List<Node> nt = chain.getComplexAggregate().getDescendants_NamedTypes();
        namedTypeCnt = chain.getComplexAggregate().getDescendants_NamedTypes().size();
        Assert.assertEquals(ActiveComplex, namedTypeCnt);

        // FIXME - should be 8. The patch extension point should not be included because it is
        // wrapped up into the minor
        namedTypeCnt = chain.getSimpleAggregate().getDescendants_NamedTypes().size();
        Assert.assertEquals(ActiveSimple, namedTypeCnt);

        // Check the service
        namedTypeCnt = chain.getServiceAggregate().getDescendants_NamedTypes().size();
        Assert.assertEquals(1, namedTypeCnt);

        // Check counts against the underlying TL library
        for (LibraryNode lib : chain.getLibraries()) {
            int libCnt = lib.getDescendants_NamedTypes().size();
            int tlCnt = lib.getTLaLib().getNamedMembers().size();
            Assert.assertEquals(libCnt, tlCnt);
        }
    }
}

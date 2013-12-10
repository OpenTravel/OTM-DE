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
import com.sabre.schemas.node.BusinessObjectNode;
import com.sabre.schemas.node.CoreObjectNode;
import com.sabre.schemas.node.EnumerationClosedNode;
import com.sabre.schemas.node.ExtensionPointNode;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFinders;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.node.VWA_Node;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.testUtils.MockLibrary;
import com.sabre.schemas.trees.repository.RepositoryNode;
import com.sabre.schemas.utils.LibraryNodeBuilder;

public class VersionsTest extends RepositoryIntegrationTestBase {
    MockLibrary ml = new MockLibrary();
    private BusinessObjectNode sbo = null;
    private BusinessObjectNode bo = null;
    private BusinessObjectNode nbo = null;
    private CoreObjectNode co = null;
    private LibraryNode baseMajorLibrary = null;
    private LibraryNode secondLib = null;
    private LibraryChainNode chain = null;
    private LibraryNode newMinor = null;

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
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        baseMajorLibrary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test/T2", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        secondLib = LibraryNodeBuilder.create("TestLibrary2",
                getRepositoryForTest().getNamespace() + "/Test", "prefix2", new Version(1, 0, 0))
                .build(uploadProject, pc);
        chain = rc.manage(getRepositoryForTest(), Collections.singletonList(baseMajorLibrary)).get(
                0);
        boolean locked = rc.lock(chain.getHead());
        Assert.assertTrue(locked);
        Assert.assertTrue(baseMajorLibrary.isEditable());
        Assert.assertEquals(RepositoryItemState.MANAGED_WIP, chain.getHead().getProjectItem()
                .getState());

        // Create a valid example of each component type
        sbo = ml.addBusinessObjectToLibrary(secondLib, "sbo");
        bo = ml.addBusinessObjectToLibrary(baseMajorLibrary, "testBO");
        co = ml.addCoreObjectToLibrary(baseMajorLibrary, "testCO");
        ml.addVWA_ToLibrary(baseMajorLibrary, "testVWA");
        ml.addSimpleTypeToLibrary(baseMajorLibrary, "testSimple");
        ml.addClosedEnumToLibrary(baseMajorLibrary, "testCEnum");
        ml.addOpenEnumToLibrary(baseMajorLibrary, "testOEnum");
        ml.addNestedTypes(baseMajorLibrary);
        ServiceNode svc = new ServiceNode(bo);
        svc.setName(bo.getName() + "_Service");
        bo.setExtensible(true);
        ExtensionPointNode ep = new ExtensionPointNode(new TLExtensionPointFacet());
        ep.setExtendsType(sbo.getSummaryFacet());
        baseMajorLibrary.addMember(ep);
        // Assert.assertTrue(testLibrary.isValid()); // you can't version an invalid library.

        // Create locked minor version
        newMinor = rc.createMinorVersion(chain.getHead());

        // The next two lines will break the check that tests TL and node counts. Un-comment to
        // check if that is working.
        // BusinessObjectNode fakeBO = new BusinessObjectNode(new TLBusinessObject());
        // Assert.assertTrue(chain.getComplexAggregate().getChildren().add(fakeBO));
    }

    @Test
    public void checkMinorVersion() {
        Assert.assertTrue(newMinor.isEditable());
        Assert.assertTrue(chain.isEditable());
        Assert.assertFalse(baseMajorLibrary.isEditable());
        Assert.assertEquals(2, chain.getLibraries().size());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(baseMajorLibrary.getDescendants_NamedTypes().contains(bo));
        Assert.assertFalse(newMinor.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);
    }

    //
    // test adding custom facets
    //
    @Test
    public void testFacets() {
        int facetCount = bo.getChildren().size();
        bo.isInHead();
        bo.addFacet("custom1", "", TLFacetType.CUSTOM);
        // Adding to bo should fail...in the future it might create a new bo and add it to that.
        Assert.assertEquals(facetCount, bo.getChildren().size());
        Assert.assertEquals(0, newMinor.getDescendants_NamedTypes().size());

        // test adding to a new minor version component
        nbo = (BusinessObjectNode) bo.createMinorVersionComponent();
        nbo.isInHead();
        nbo.addFacet("c2", "", TLFacetType.CUSTOM);
        Assert.assertEquals(4, nbo.getChildren().size());
        Assert.assertEquals(1, newMinor.getDescendants_NamedTypes().size());
        Assert.assertTrue(chain.isValid());
        nbo.delete();
        checkCounts(chain);
    }

    //
    // Test handling of adding and deleting of new objects
    //
    @Test
    public void testAddingAndDeleting() {
        nbo = ml.addBusinessObjectToLibrary(newMinor, "nbo");

        // The new bo should be in the minor library, not the base library.
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(baseMajorLibrary.getDescendants_NamedTypes().contains(nbo));

        // Add some other object types
        EnumerationClosedNode nec = ml.addClosedEnumToLibrary(chain.getHead(), "ce2");
        VWA_Node nvwa = ml.addVWA_ToLibrary(chain.getHead(), "vwa2");
        Assert.assertTrue(chain.isValid());

        // Remove and delete them
        nbo.delete();
        nec.delete();
        nvwa.delete();
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);
    }

    @Test
    public void testAddingProperties() {
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        // Emulate behavior in AddNodeHandler. AddNodeHandler notifies the user then
        // createMinorVersionComponent()
        // Constructors can not do this because they are needed for initial rendering of the objects
        // and can't do user dialogs/notifications.
        CoreObjectNode nco = (CoreObjectNode) co.createMinorVersionComponent();
        PropertyNode newProp = new ElementNode(nco.getSummaryFacet(), "te2");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());

        // Make sure a new CO was created in the newMinor library.
        Assert.assertNotNull(nco);
        Assert.assertEquals(1, nco.getSummaryFacet().getChildren().size());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nco));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(nco));
        Assert.assertFalse(baseMajorLibrary.getDescendants_NamedTypes().contains(nco));

        newProp = new ElementNode(nco.getSummaryFacet(), "te2");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        Assert.assertEquals(2, nco.getSummaryFacet().getChildren().size());
        newProp.delete();
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        Assert.assertEquals(1, nco.getSummaryFacet().getChildren().size());
        Assert.assertTrue(chain.isValid());
        nco.delete(); // keep counts accurate
        checkCounts(chain);
    }

    @Test
    public void testCopying() {
        nbo = (BusinessObjectNode) bo.clone("_copy");
        // copy should be in the new library
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(baseMajorLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(chain.isValid());
        nbo.delete();
        checkCounts(chain);
    }

    @Test
    public void testMove() {
        // This will work because moveMember is at the model level. It is used by the controller
        // which applies the business logic if it is valid to move.
        baseMajorLibrary.moveMember(bo, newMinor);
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertFalse(baseMajorLibrary.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(bo));
        newMinor.moveMember(bo, baseMajorLibrary); // put it back
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);

        // Test moving from another library
        nbo = ml.addBusinessObjectToLibrary(secondLib, "secondLibBO");
        secondLib.moveMember(nbo, newMinor);
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(secondLib.getDescendants_NamedTypes().contains(nbo));
        nbo.delete();

        // Test moving to another library
        nbo = ml.addBusinessObjectToLibrary(newMinor, "newBO");
        newMinor.moveMember(nbo, secondLib);
        Assert.assertTrue(secondLib.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(newMinor.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(baseMajorLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(chain.getDescendants_NamedTypes().contains(nbo));
        checkCounts(chain);

        Assert.assertTrue(chain.isValid());
    }

    @Test
    public void testDelete() {
        bo.delete(); // Should and Does fail.
        Assert.assertTrue(baseMajorLibrary.getDescendants_NamedTypes().contains(bo));
        List<?> kids = chain.getComplexAggregate().getChildren();
        Assert.assertTrue(kids.contains(bo));
        checkCounts(chain);

        //
        // Test deleting properties
        co.getSummaryFacet().getChildren().get(0).delete();
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        //
        // Test handling of new object with same name as existing object.
        //
        nbo = ml.addBusinessObjectToLibrary(chain.getHead(), "testBO");
        Assert.assertFalse(newMinor.isValid());

        kids = chain.getComplexAggregate().getChildren();
        Assert.assertTrue(kids.contains(nbo));
        Assert.assertFalse(kids.contains(bo)); // was replaced with nbo

        // The new bo should be in the minor library, not the base library.
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(baseMajorLibrary.getDescendants_NamedTypes().contains(nbo));

        // Deleting via GUI should make it valid and replace the old one back into the aggregate
        // model level delete should just do it.
        nbo.delete();
        Assert.assertTrue(newMinor.isValid());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(baseMajorLibrary.getDescendants_NamedTypes().contains(bo));
        checkCounts(chain);
        // counts will be wrong.

        // Renaming it should make chain valid
        nbo = ml.addBusinessObjectToLibrary(newMinor, "testBO");
        nbo.setName("testBO2");
        Assert.assertTrue(newMinor.isValid());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(baseMajorLibrary.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(nbo));
    }

    @Test
    public void testMajor() {
        // Create major version which makes the minor final.
        Assert.assertTrue(chain.isEditable());
        LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
        Assert.assertFalse(chain.isEditable());
        LibraryChainNode newChain = newMajor.getChain();
        // The extension point will not be in the major. Add a complex to keep counts right.
        ml.addBusinessObjectToLibrary(newMajor, "MajorBO");
        checkCounts(newChain);
    }

    // Remember, getDescendents uses HashMap - only unique nodes.
    private void checkCounts(LibraryChainNode chain) {
        // Make sure all the base objects are accessible.
        int namedTypeCnt = chain.getDescendants_NamedTypes().size();
        Assert.assertEquals(11, namedTypeCnt);
        // Make sure all the types are in the versions aggregate
        namedTypeCnt = chain.getComplexAggregate().getDescendants_NamedTypes().size();
        Assert.assertEquals(8, namedTypeCnt);
        namedTypeCnt = chain.getSimpleAggregate().getDescendants_NamedTypes().size();
        Assert.assertEquals(2, namedTypeCnt);
        namedTypeCnt = chain.getServiceAggregate().getDescendants_NamedTypes().size();
        // FIXME - should have a service!
        // Assert.assertEquals(1, namedTypeCnt);

        // Check counts against the underlying TL library
        for (LibraryNode lib : chain.getLibraries()) {
            int libCnt = lib.getDescendants_NamedTypes().size();
            int tlCnt = lib.getTLaLib().getNamedMembers().size();
            Assert.assertEquals(libCnt, tlCnt);
        }
    }
}

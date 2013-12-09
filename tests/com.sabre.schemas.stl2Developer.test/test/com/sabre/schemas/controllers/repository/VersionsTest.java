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

public class VersionsTest extends RepositoryControllerTest {
    MockLibrary ml = new MockLibrary();
    private BusinessObjectNode sbo = null;
    private BusinessObjectNode bo = null;
    private BusinessObjectNode nbo = null;
    private CoreObjectNode co = null;
    private LibraryNode testLibrary = null;
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

    // FIXME - test set up so that it does not repeat tests in repositoryControllerTest
    //
    @Before
    public void runBeforeEachTest() throws LibrarySaveException, RepositoryException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        testLibrary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test/T2", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        secondLib = LibraryNodeBuilder.create("TestLibrary2",
                getRepositoryForTest().getNamespace() + "/Test", "prefix2", new Version(1, 0, 0))
                .build(uploadProject, pc);
        chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibrary)).get(0);
        boolean locked = rc.lock(chain.getHead());
        Assert.assertTrue(locked);
        Assert.assertTrue(testLibrary.isEditable());
        Assert.assertEquals(RepositoryItemState.MANAGED_WIP, chain.getHead().getProjectItem()
                .getState());

        // Create a valid example of each component type
        sbo = ml.addBusinessObjectToLibrary(secondLib, "sbo");
        bo = ml.addBusinessObjectToLibrary(testLibrary, "testBO");
        co = ml.addCoreObjectToLibrary(testLibrary, "testCO");
        ml.addVWA_ToLibrary(testLibrary, "testVWA");
        ml.addSimpleTypeToLibrary(testLibrary, "testSimple");
        ml.addClosedEnumToLibrary(testLibrary, "testCEnum");
        ml.addOpenEnumToLibrary(testLibrary, "testOEnum");
        ml.addNestedTypes(testLibrary);
        ServiceNode svc = new ServiceNode(bo);
        svc.setName(bo.getName() + "_Service");
        bo.setExtensible(true);
        ExtensionPointNode ep = new ExtensionPointNode(new TLExtensionPointFacet());
        ep.setExtendsType(sbo.getSummaryFacet());
        testLibrary.addMember(ep);
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
        Assert.assertFalse(testLibrary.isEditable());
        Assert.assertEquals(2, chain.getLibraries().size());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(testLibrary.getDescendants_NamedTypes().contains(bo));
        Assert.assertFalse(newMinor.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);
    }

    //
    // test adding custom facets
    //
    @Test
    public void testFacets() {
        // TODO - prevent enabling in GUI go.isEnabled() and AddQueryFacet
        //
        int facetCount = bo.getChildren().size();
        boolean head = bo.isInHead(); // this works -- use it in addFacet()
        bo.addFacet("custom1", "", TLFacetType.CUSTOM);
        //
        // Adding to bo should fail...in the future it might create a new bo and add it to that.
        Assert.assertEquals(facetCount, bo.getChildren().size());
        Assert.assertEquals(0, newMinor.getDescendants_NamedTypes().size());

        // test adding to a new minor version component
        nbo = (BusinessObjectNode) bo.createMinorVersionComponent();
        head = nbo.isInHead();
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
        Assert.assertFalse(testLibrary.getDescendants_NamedTypes().contains(nbo));

        // Add some other object types
        EnumerationClosedNode nec = ml.addClosedEnumToLibrary(chain.getHead(), "ce2");
        VWA_Node nvwa = ml.addVWA_ToLibrary(chain.getHead(), "vwa2");
        Assert.assertTrue(chain.isValid());

        // Remove and delete them
        // FIXME - removeFromLibrary() does not remove it, fails counts
        // nbo.removeFromLibrary();
        nbo.delete();
        nec.delete();
        nvwa.delete();
        Assert.assertTrue(chain.isValid());
        // TODO - enable counts, wrong counts are due to add facet tests
        checkCounts(chain);
    }

    @Test
    public void testAddingProperties() {
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        // FIXME - the new property should not be on CO but on the new CO created in the versioned
        // library.
        // - Handler for new property is in AddNodeHandler in a private method.
        // - AddNodeHandler notifies the user then createMinorVersionComponent()
        // *** constructors should be aware and either handle it or throw error.
        // PropertyNode newProp = new ElementNode(co.getSummaryFacet(), "te2");
        // newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        CoreObjectNode nco = (CoreObjectNode) co.createMinorVersionComponent();
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        Assert.assertEquals(0, nco.getSummaryFacet().getChildren().size());

        // Make sure a new CO was created in the newMinor library.
        // Node nco = newMinor.findNode(co.getName(), newMinor.getNamespace());
        Assert.assertNotNull(nco);
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nco));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(nco));
        Assert.assertFalse(testLibrary.getDescendants_NamedTypes().contains(nco));

        PropertyNode newProp = new ElementNode(nco.getSummaryFacet(), "te2");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        Assert.assertEquals(1, nco.getSummaryFacet().getChildren().size());
        newProp.delete();
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        Assert.assertEquals(0, nco.getSummaryFacet().getChildren().size());
        Assert.assertTrue(chain.isValid());
        nco.delete(); // keep counts accurate
        checkCounts(chain);
    }

    @Test
    public void testCopying() {
        // FIXME - causes npe in BusinessObjectNode fixAssignments() (293)
        nbo = (BusinessObjectNode) bo.clone("_copy");
        // copy should be in the new library
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(testLibrary.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(chain.isValid());
        nbo.delete();
        checkCounts(chain);
    }

    @Test
    public void testMove() {
        //
        // Test Move
        //
        testLibrary.moveMember(bo, newMinor); // should fail
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        // FIXME - these two tests should be OK. When fixed, the second move is not needed.
        // Assert.assertTrue(testLibrary.getDescendants_NamedTypes().contains(bo));
        // Assert.assertFalse(newMinor.getDescendants_NamedTypes().contains(bo));
        newMinor.moveMember(bo, testLibrary); // put it back until fixed
        Assert.assertTrue(chain.isValid());
        checkCounts(chain);

        // Test moving from another library
        nbo = ml.addBusinessObjectToLibrary(secondLib, "secondLibBO");
        secondLib.moveMember(nbo, newMinor);
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(secondLib.getDescendants_NamedTypes().contains(nbo));
        nbo.delete();

        // Test moving to another library
        // nbo = ml.addBusinessObjectToLibrary(newMinor, "newBO");
        // FIXME - NPE - libraryNode line 785 - if which lib is in chain?
        // newMinor.moveMember(nbo, secondLib);
        // Assert.assertTrue(secondLib.getDescendants_NamedTypes().contains(nbo));
        // Assert.assertFalse(newMinor.getDescendants_NamedTypes().contains(nbo));

        checkCounts(chain);
        Assert.assertTrue(chain.isValid());
    }

    @Test
    public void testDelete() {
        // Try deleting content from the base library -- should fail
        bo.delete();
        Assert.assertTrue(testLibrary.getDescendants_NamedTypes().contains(bo));
        co.getSummaryFacet().getChildren().get(0).delete();
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        // FIXME - one complex type missing
        // checkCounts(chain);

        //
        // Test handling of new object with same name as existing object.
        //
        nbo = ml.addBusinessObjectToLibrary(chain.getHead(), "testBO");
        Assert.assertFalse(newMinor.isValid());

        // the new testBO should replace old one in the aggregate node.
        List<?> kids = chain.getComplexAggregate().getChildren();
        Assert.assertTrue(kids.contains(nbo));
        Assert.assertFalse(kids.contains(bo));

        // The new bo should be in the minor library, not the base library.
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(nbo));
        Assert.assertFalse(testLibrary.getDescendants_NamedTypes().contains(nbo));

        // FIXME - these tests are all OK, but
        // gui displays duplicate node in base library.
        // gui displays original node in aggregate.

        // Deleting it should make it valid and replace the old one back into the aggregate
        nbo.delete();
        Assert.assertTrue(newMinor.isValid());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(testLibrary.getDescendants_NamedTypes().contains(bo));
        // FIXME - old bo is not being moved back into Aggregate
        // Assert.assertTrue(chain.getComplexAggregate().getChildren().contains(bo));
        // FIXME - has 10 named objects, but only 6 complex
        // checkCounts(chain);

        // Renaming it should make chain valid
        nbo = ml.addBusinessObjectToLibrary(newMinor, "testBO");
        nbo.setName("testBO2");
        Assert.assertTrue(newMinor.isValid());
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(testLibrary.getDescendants_NamedTypes().contains(bo));
        Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
        Assert.assertTrue(newMinor.getDescendants_NamedTypes().contains(nbo));
        nbo.delete();
        // checkCounts(chain);

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

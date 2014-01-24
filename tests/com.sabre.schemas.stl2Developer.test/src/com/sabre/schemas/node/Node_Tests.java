/**
 * 
 */
package com.sabre.schemas.node;

import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemas.controllers.DefaultProjectController;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.modelObject.EmptyMO;
import com.sabre.schemas.modelObject.SimpleFacetMO;
import com.sabre.schemas.modelObject.TLEmpty;
import com.sabre.schemas.modelObject.TLnSimpleAttribute;
import com.sabre.schemas.node.Node.NodeVisitor;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;
import com.sabre.schemas.types.TestTypes;

/**
 * @author Dave Hollander
 * 
 */
public class Node_Tests {
    private static final Logger LOGGER = LoggerFactory.getLogger(Node_Tests.class);

    private int nodeCount = 0;
    private String string = "";

    /**
     * Primary node test for use in other junit tests. Usage: n.visitAllNodes(new Node_Tests().new
     * TestNode()); or TestNode tn = new Node_Tests().new TestNode(); tn.visit(node);
     * ln.visitAllNodes(tn);
     * 
     * @author Dave Hollander
     * 
     */
    public class TestNode implements NodeVisitor {
        @Override
        public void visit(INode n) {
            visitNode((Node) n);
        }
    }

    public class PrintNode implements NodeVisitor {
        @Override
        public void visit(INode n) {
            LOGGER.debug("Visited " + n + "\tof class \t" + n.getClass().getCanonicalName());
        }
    }

    @Test
    public void builtInTests() {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        // for (LibraryNode lib : ModelNode.getAllLibraries())
        // lib.visitAllTypeUsers(new PrintNode());

        mc.getModelNode().visitAllNodes(new TestNode());
    }

    @Test
    public void FacetAsType() {
        // Facets as types throw the resolver off because they have type names not types.
        MockLibrary ml = new MockLibrary();
        MainController mc = new MainController();
        DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
        ProjectNode defaultProject = pc.getDefaultProject();
        LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "tbo");
        Node user = bo.getDescendants_TypeUsers().get(1);

        NamedEntity userType = user.getTLTypeObject();
        Assert.assertNotNull(user.getTLTypeObject());

        user.setAssignedType(bo.getDetailFacet());
        Assert.assertNotNull(user.getTLTypeObject());

    }

    @Test
    public void facetAsTypeOnLoad() {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        LibraryNode ln = lf.loadFile1(mc);
        Node user = null;
        for (Node n : ln.getDescendants_TypeUsers())
            if (n.getName().equals("Profile_Detail")) {
                user = n;
                break;
            }
        Assert.assertNotNull(user);
        TLModelElement userTL = user.getTLModelObject();
        // Comment out body of workaround method PropertyNode.getTLTypeNameField() to see error.
        NamedEntity userTLtype = user.getTLTypeObject(); // Should be not-null: See JIRA 510.
        Assert.assertNotNull(user.getTLTypeObject()); // Should be not-null: See JIRA 510.
        QName qName = user.getTLTypeQName();

        // Type resolver needs a valid qName from this assigned type.
        Assert.assertFalse(qName.getNamespaceURI().isEmpty());
        Assert.assertFalse(qName.getLocalPart().isEmpty());

        // This will only work if the resolver found the qName for the elements assigned facets.
        ln.visitAllNodes(new TestNode());
    }

    @Test
    public void nodeTests() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        lf.loadFile1(mc);

        lf.loadFile5(mc);
        lf.loadFile3(mc);
        lf.loadFile4(mc);
        lf.loadFile2(mc);
        lf.loadXfile2(mc);

        mc.getModelNode().visitAllNodes(new TestNode()); // set visit count

        for (INode n : mc.getModelNode().getChildren()) {
            nodeCount++;
            actOnNode(n);
        }

    }

    @Test
    public void descendantTypeUsersTest() {
        MockLibrary ml = new MockLibrary();
        MainController mc = new MainController();
        DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
        ProjectNode defaultProject = pc.getDefaultProject();
        LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        Node bo = ml.addNestedTypes(ln);

        List<Node> types = bo.getDescendants_AssignedTypes(true);

        Assert.assertNotNull(types);
        Assert.assertEquals(2, types.size());
    }

    /**
     * Test the type providers and assure where used and owner. Test type users and assure getType
     * returns valid node.
     * 
     * @param n
     *            Use Node.visitAllNodes (new visitor());
     */
    public void visitAllNodes(Node n) {
        n.visitAllNodes(new TestNode());
    }

    private void actOnNode(INode n) {
        n.setAssignedType((Node) n);
        n.setName("TEST", true);
        switch (nodeCount % 3) {
            case 0:
                n.removeFromLibrary();
                n.close();
                break;
            case 1:
                n.delete();
                break;
            case 2:
                n.delete();
        }
    }

    /**
     * Test a node to assure it is valid. Checks assignments, access and type node. Also tests
     * compiler validation and example generation.
     * 
     * @param n
     */
    public void visitNode(Node n) {
        // LOGGER.debug("Node_Tests-testing: " + n);
        if (n instanceof ModelNode)
            return;
        Assert.assertNotNull(n);
        if (n.isDeleted()) {
            LOGGER.debug("Test node " + n + " is deleted. Skipping.");
            return;
        }

        // Test the source object
        if (n instanceof LibraryNode) {
            try {
                new TestTypes().visitTypeNode(n); // test type node and example generation.
            } catch (IllegalStateException e) {
                LOGGER.debug("Error with " + n + " " + e);
                Assert.assertEquals("", e.getLocalizedMessage());
                return;
            }
        } else
            Assert.assertTrue(n.getTypeClass().verifyAssignment());

        try {
            new validateTLObject().visit(n);
        } catch (IllegalStateException e) {
            LOGGER.debug("TLObject Error with " + n + ". " + e.getLocalizedMessage());
            Assert.assertEquals("", e.getLocalizedMessage().toString());
            return;
        }

        // Check links.
        Assert.assertFalse(n.getNodeID().isEmpty());
        Assert.assertNotNull(n.getParent());

        if (!n.isLibraryContainer())
            Assert.assertNotNull(n.getLibrary());
        Assert.assertNotNull(n.getLibraries());
        Assert.assertNotNull(n.getUserLibraries());

        Assert.assertNotNull(n.getChildren());
        Assert.assertNotNull(n.getChildren_TypeProviders());

        Assert.assertNotNull(n.getModelObject());

        // Check children
        if (n instanceof NavNode)
            Assert.assertTrue(n.getModelObject() instanceof EmptyMO);
        else if (!(n.getModelObject() instanceof EmptyMO) && !(n instanceof LibraryNode)
                && !(n instanceof OperationNode) && !(n.getChildren().isEmpty())) {
            if (n.getChildren().size() != n.getModelObject().getChildren().size())
                LOGGER.debug("Children sizes are not equal.");
            Assert.assertEquals(n.getChildren().size(), n.getModelObject().getChildren().size());
        }

        // Check names
        String name = "";
        if (n.getName().isEmpty()) {
            name = n.getName();
        }
        String foo = name;
        Assert.assertFalse(n.getName().isEmpty());
        Assert.assertFalse(n.getLabel().isEmpty());
        if (n instanceof ComponentNode) {
            if (n.getNamePrefix().isEmpty())
                string = n.getNamePrefix();
            Assert.assertFalse(n.getNamePrefix() == null);
            Assert.assertFalse(n.getNamespace().isEmpty());
            Assert.assertFalse(n.getNameWithPrefix().isEmpty());
        }

        // Check component type and state
        Assert.assertFalse(n.isDeleted());
        Assert.assertNotNull(n.getComponentType());
        Assert.assertFalse(n.getComponentType().isEmpty());
        if (n.getModelObject() instanceof SimpleFacetMO)
            Assert.assertTrue(n instanceof SimpleFacetNode);

        // Check type information
        Assert.assertNotNull(n.getTypeUsers());

        if (n.isTypeProvider()) {
            ComponentNode cn = (ComponentNode) n;
            Assert.assertNotNull(cn.getWhereUsed());
            Assert.assertNotNull(cn.getTypeOwner());
            Assert.assertFalse(cn.getTypeOwner().getName().isEmpty());
            Assert.assertFalse(cn.getTypeOwner().getNamespace().isEmpty());
        }

        // is tests - make sure they do not throw exception
        n.isEditable();
        n.isLibraryContainer();
        n.isVWA_AttributeFacet();
        n.isTypeProvider();
        n.isTypeUser();
        n.isAssignedByReference();
        n.isLibraryContainer();

    }

    public class validateTLObject implements NodeVisitor {
        @Override
        public void visit(INode in) {
            Node n = (Node) in;
            if (n instanceof ImpliedNode)
                return;
            // XSD types will fail because they are not in the model until
            // imported.
            if (n.isXsdType()) {
                return;
            }
            validateTL(n.getTLModelObject(), in);
        }

        /**
         * Validate the TL model object owning and library relationships.
         * 
         * @param tlObj
         * @param in
         * @throws IllegalStateException
         */
        public void validateTL(TLModelElement tlObj, INode in) throws IllegalStateException {
            String msg = "";
            // LOGGER.debug("Validating tlObj " +
            // tlObj.getValidationIdentity());
            if (tlObj instanceof TLEmpty)
                return;
            if (tlObj instanceof TLnSimpleAttribute)
                return;

            if (tlObj.getValidationIdentity() == null || tlObj.getValidationIdentity().isEmpty())
                msg = "Missing validation identity on ";

            if (tlObj instanceof LibraryMember) {
                // LOGGER.debug("Validating member " + tlObj.getValidationIdentity());

                // if (tlObj.getOwningModel() == null)
                // msg += "Missing owning model on ";
                if (((LibraryMember) tlObj).getOwningLibrary() == null)
                    msg += "Missing owning library on ";
                if (((LibraryMember) tlObj).getNamespace() == null
                        || ((LibraryMember) tlObj).getNamespace().isEmpty())
                    msg += "Missing namespace on ";
                if ((((LibraryMember) tlObj).getLocalName() == null || ((LibraryMember) tlObj)
                        .getLocalName().isEmpty()) && !(tlObj instanceof TLExtensionPointFacet))
                    msg += "Missing local name on ";
            } else if (tlObj instanceof TLFacet) {
                if (((TLFacet) tlObj).getOwningEntity() == null)
                    msg += "Missing facet owner on ";
            } else if (tlObj instanceof TLAttribute) {
                if (((TLAttribute) tlObj).getAttributeOwner() == null)
                    msg += "Missing attribute owner on ";
            } else if (tlObj instanceof TLProperty) {
                if (((TLProperty) tlObj).getPropertyOwner() == null)
                    msg += "Missing property owner on ";
                if (((TLProperty) tlObj).getOwningLibrary() == null)
                    msg += "Missing property library on ";
            } else if (tlObj instanceof TLIndicator) {
                if (((TLIndicator) tlObj).getOwner() == null)
                    msg = "Missing indicator owner on ";
            }

            if (!msg.isEmpty()) {
                if (in != null)
                    msg = msg + in.getNameWithPrefix();
                msg = msg + "\t of class " + tlObj.getClass().getSimpleName();
                throw new IllegalStateException(msg);
            }
        }
    }

}

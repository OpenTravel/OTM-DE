/**
 * 
 */
package org.opentravel.schemas.node;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EditNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.OperationNode;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.RoleFacetNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.Node_Tests.TestNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the creation of new components.
 * 
 * @author Dave Hollander
 * 
 */
public class NewComponent_Tests {
    private final static Logger LOGGER = LoggerFactory.getLogger(NewComponent_Tests.class);

    TestNode nt = new Node_Tests().new TestNode();
    ModelNode model = null;
    TestNode tn = new Node_Tests().new TestNode();
    LoadFiles lf = new LoadFiles();
    LibraryTests lt = new LibraryTests();
    MockLibrary ml = null;
    LibraryNode ln = null;
    MainController mc;
    DefaultProjectController pc;
    ProjectNode defaultProject;
    EditNode en;

    @Before
    public void beforeAllTests() {
        mc = new MainController();
        ml = new MockLibrary();
        pc = (DefaultProjectController) mc.getProjectController();
        defaultProject = pc.getDefaultProject();

        en = new EditNode(ln);
        en.setName("SOME_Component");
        en.setDescription("THIS IS A DESCRIPTION");

    }

    public NewComponent_Tests() {
        mc = new MainController();
        lf = new LoadFiles();
    }

    @Test
    public void newComponentTests() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        LibraryNode noService = lf.loadFile2(mc);
        LibraryNode hasService = lf.loadFile1(mc);

        for (Node n : Node.getAllUserLibraries()) {
            n.visitAllNodes(nt);
        }

        createNewComponents(noService);

        for (Node n : Node.getAllUserLibraries()) {
            n.visitAllNodes(nt);
        }
    }

    @Test
    public void createAllTypes() {
        ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "testBO");
        ServiceNode svc;
        if (ln.getServiceRoot() == null)
            svc = new ServiceNode(ln);
        else
            svc = (ServiceNode) ln.getServiceRoot();
        OperationNode operation = new OperationNode(svc, "Op1");
        EditNode editNode = new EditNode(ln);
        String name = "testEditNode";
        String description = "Testing creating new objects.";
        editNode.setName(name);
        editNode.setDescription(description);
        Node n2;

        for (ComponentNodeType type : ComponentNodeType.values()) {
            String tName = type.getDescription();
            Assert.assertNotNull(ComponentNodeType.fromString(tName));

            if (type.equals(ComponentNodeType.ALIAS))
                n2 = bo.newComponent(type);
            else {
                // LOGGER.debug("Ready to create a " + type.getDescription());
                n2 = editNode.newComponent(type);
                // null returned when type is not supported
                if (n2 != null) {
                    if (type != ComponentNodeType.EXTENSION_POINT) {
                        // if (n2.getName().isEmpty())
                        // LOGGER.debug("Name is empty. Expecting: " + name);
                        Assert.assertEquals(name, n2.getName());
                    }
                    Assert.assertEquals(description, n2.getDescription());
                    Assert.assertNotNull(n2.getParent());
                    Assert.assertEquals(ln, n2.getLibrary());
                }
            }
        }
    }

    public void createNewComponents(LibraryNode ln) {
        Node newOne, newBO, newCO;
        en = new EditNode(ln);
        ln.setEditable(true);
        en.setName("TestObject");

        // Create Business Object
        newBO = en.newComponent(ComponentNodeType.BUSINESS);
        nt.visit(newBO);
        addProperties((ComponentNode) newBO); // properties first so alias is not counted as child
        newBO.visitAllNodes(nt);
        newOne = ((ComponentNode) newBO).newComponent(ComponentNodeType.ALIAS);
        nt.visit(newBO);
        Assert.assertTrue(newBO.isBusinessObject());

        // Create new core object.
        newCO = en.newComponent(ComponentNodeType.CORE);
        addProperties((ComponentNode) newCO);
        addRoles((CoreObjectNode) newCO);
        newCO.visitAllNodes(nt);
        newOne = ((ComponentNode) newCO).newComponent(ComponentNodeType.ALIAS);
        nt.visit(newCO);
        Assert.assertTrue(newCO.isCoreObject());

        newOne = en.newComponent(ComponentNodeType.VWA);
        nt.visit(newOne);
        Assert.assertTrue(newOne.isValueWithAttributes());

        newOne = en.newComponent(ComponentNodeType.EXTENSION_POINT);
        nt.visit(newOne);

        newOne = en.newComponent(ComponentNodeType.CLOSED_ENUM);
        addLiterals(newOne);
        nt.visit(newOne);

        newOne = en.newComponent(ComponentNodeType.OPEN_ENUM);
        nt.visit(newOne);

        newOne = en.newComponent(ComponentNodeType.SIMPLE);
        nt.visit(newOne);

        en.setTLType(newBO); // used as subject of CRUD operations
        newOne = en.newComponent(ComponentNodeType.SERVICE);
        nt.visit(newOne);
    }

    private void addProperties(ComponentNode n) {
        Assert.assertNotNull(n.getSummaryFacet());
        PropertyNode pne = new ElementNode(n.getSummaryFacet(), "Property");
        PropertyNode pna = new AttributeNode(n.getSummaryFacet(), "Attribute");
        PropertyNode pni = new IndicatorNode(n.getSummaryFacet(), "Indicator");
        PropertyNode pner = new ElementReferenceNode(n.getSummaryFacet(), "EleRef");
        Assert.assertEquals(4, n.getSummaryFacet().getChildren().size());
    }

    private void addRoles(CoreObjectNode n) {
        RoleFacetNode rf = n.getRoleFacet();
        Assert.assertNotNull(rf);
        PropertyNode pnr1 = new RoleNode(rf, "Role1");
        PropertyNode pnr2 = new RoleNode(rf, "Role2");
        PropertyNode pnr3 = new RoleNode(rf, "Role3");
        Assert.assertEquals(3, n.getRoleFacet().getChildren().size());
    }

    private void addLiterals(Node n) {
        // ComponentNode en = n.getRoleFacet();
        Assert.assertNotNull(n);
        PropertyNode pnr1 = new EnumLiteralNode((ComponentNode) n, "lit1");
        PropertyNode pnr2 = new EnumLiteralNode((ComponentNode) n, "lit2");
        PropertyNode pnr3 = new EnumLiteralNode((ComponentNode) n, "lit3");
        Assert.assertEquals(4, n.getChildren().size());
    }

}

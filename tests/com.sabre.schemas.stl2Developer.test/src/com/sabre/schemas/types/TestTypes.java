/**
 * 
 */
package com.sabre.schemas.types;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.codegen.example.ExampleDocumentBuilder;
import com.sabre.schemacompiler.codegen.example.ExampleGeneratorOptions;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.BusinessObjectNode;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.ImpliedNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFinders;
import com.sabre.schemas.node.Node_Tests;
import com.sabre.schemas.node.properties.SimpleAttributeNode;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class TestTypes {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTypes.class);

    @Test
    public void checkTypes() throws Exception {
        MainController mc = new MainController();
        LibraryNode ln;
        LoadFiles lf = new LoadFiles();
        lf.loadFile1(mc);
        ln = lf.loadFile5(mc);

        for (Node n : Node.getAllUserLibraries()) {
            visitAllNodes(n);
        }
        testSettingType();

        // 9 is if you do not get the family owned types, 17 if you do.
        Assert.assertEquals(9, testSimples(ln));

        lf.loadFile3(mc);
        lf.loadFile4(mc);
        lf.loadFile2(mc);
        lf.loadXfile1(mc);

        for (Node n : Node.getAllLibraries()) {
            visitAllNodes(n);
        }

    }

    @Test
    public void testsetAssignedTypeForThisNode() {
        MockLibrary ml = new MockLibrary();
        String ns = "http://sabre.com/test";
        boolean ret = false;
        Node prop;
        LOGGER.debug("TEST being run -- testsetAssignedTypeForThisNode");

        LibraryNode ln = ml.createNewLibrary(ns, "LIB", null);
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "BO");

        ret = bo.getTypeClass().setAssignedTypeForThisNode(bo);
        Assert.assertFalse(ret); // should fail since BO is not type user.

        // Test using the finders.
        prop = bo.getIDFacet().getChildren().get(0);
        ret = prop.getTypeClass().setAssignedTypeForThisNode();
        Assert.assertTrue(ret);

        // Test using TypeResolver map.
        TypeResolver tr = new TypeResolver();
        prop = bo.getSummaryFacet().getChildren().get(0);
        Assert.assertNotNull(prop);
        ret = prop.getTypeClass().setAssignedTypeForThisNode(prop, tr.getProviderMap());
        Assert.assertTrue(ret);

        String simpleName = "simple";
        prop = ml.addSimpleTypeToLibrary(ln, simpleName);
        ret = prop.getTypeClass().setAssignedTypeForThisNode(prop, tr.getProviderMap());
        Assert.assertTrue(ret);

        // Make sure GUI can access types
        Assert.assertFalse(prop.getTypeName().equals(simpleName)); // properties view
        Assert.assertTrue(prop.getTypeName().equals("int")); // properties view
        Assert.assertFalse(prop.getTypeNameWithPrefix().isEmpty()); // facet view
        Assert.assertFalse(prop.getAssignedType() instanceof ImpliedNode);

    }

    public int testSimples(LibraryNode ln) {
        int simpleCnt = 0;
        for (Node sn : ln.getSimpleRoot().getChildren()) {
            if (sn.isSimpleType()) {
                simpleCnt++;
                Assert.assertNotNull(sn.getAssignedType());
                if (sn.getAssignedType() instanceof ImpliedNode) {
                    boolean x = sn.getAssignedType() instanceof ImpliedNode;
                }
                Assert.assertFalse(sn.getAssignedType() instanceof ImpliedNode);
            }
        }
        return simpleCnt;
    }

    private void testSettingType() {
        final String testNS = "http://www.sabre.com/ns/OTA2/Demo/Profile/v01";
        Node typeToAssign = NodeFinders.findNodeByName("String_Long", testNS);
        Assert.assertNotNull(typeToAssign);
        Node tn = NodeFinders.findNodeByName("EmploymentZZZ", testNS);
        tn.getLibrary().setEditable(true);
        Assert.assertNotNull(tn);
        int usrCnt = ((ComponentNode) typeToAssign).getTypeUsersCount();
        // 8 summary properties

        for (INode facet : tn.getChildren()) {
            for (Node property : facet.getChildren()) {
                if (property.isTypeUser()) {
                    if (property instanceof SimpleAttributeNode)
                        property.setAssignedType(typeToAssign);
                    Assert.assertTrue(property.setAssignedType(typeToAssign));
                    if (typeToAssign != property.getAssignedType())
                        LOGGER.debug("Assignment Error on " + property);
                    Assert.assertEquals(typeToAssign, property.getAssignedType());

                    if (typeToAssign.getTLModelObject() != property.getTLTypeObject()) {
                        NamedEntity x = property.getTLTypeObject();
                        LOGGER.debug("Assigned TL type does not match typeNode assignment.");
                    }
                    Assert.assertEquals(typeToAssign.getTLModelObject(), property.getTLTypeObject());

                    Assert.assertEquals(((ComponentNode) typeToAssign).getTypeUsersCount(),
                            ++usrCnt);
                }
            }
        }
    }

    /**
     * Test the type providers and assure where used and owner. Test type users and assure getType
     * returns valid node.
     * 
     * @param n
     */
    public void visitAllNodes(Node n) {
        visitTypeNode(n);
        for (Node c : n.getChildren())
            visitAllNodes(c);
    }

    public void GenExampleAndValidate(Node cn) {
        ValidationFindings findings = null;
        final ExampleDocumentBuilder exampleBuilder = new ExampleDocumentBuilder();
        ExampleGeneratorOptions options = new ExampleGeneratorOptions();
        exampleBuilder.setOptions(options);
        String xml = "";
        int errorCount = 0;

        // Make sure we can create findings.
        findings = cn.validate();
        errorCount = findings.getFindingsAsList(FindingType.ERROR).size();
        // LOGGER.debug("Validation Error count for " + n + " = " +
        // errorCount);

        // Make sure we can create examples for types without errors.
        xml = cn.compileExampleXML(errorCount > 0);
        if (errorCount < 1)
            Assert.assertFalse(xml.endsWith("ERROR"));
        else
            Assert.assertTrue(xml.endsWith("ERROR"));

    }

    /**
     * JUnit test to validate the contents of the Type assignments within the node. TypeProvider and
     * TypeUser specific <b>assertions</b> are made. Generates Examples and runs compiler
     * validation. <b>Very cpu intensive!</b>
     * 
     * {@link Node_Tests#visit(Node)} node based test.
     * 
     * @param n
     */
    public void visitTypeNode(Node n) {
        ValidationFindings findings = null;
        final ExampleDocumentBuilder exampleBuilder = new ExampleDocumentBuilder();
        ExampleGeneratorOptions options = new ExampleGeneratorOptions();
        exampleBuilder.setOptions(options);
        String xml = "";
        int errorCount = 0;
        if (n == null)
            return;

        if (n.getParent() == null)
            n.getParent();
        Assert.assertNotNull(n.getParent());
        if (!n.isLibraryContainer()) {
            if (n.getLibrary() == null)
                LOGGER.debug("Null library in " + n);
            Assert.assertNotNull(n.getLibrary());
        }
        Assert.assertNotNull(n.getComponentType());
        Assert.assertFalse(n.getComponentType().isEmpty());

        if (n.isTypeProvider()) {
            ComponentNode cn = (ComponentNode) n;
            Assert.assertNotNull(cn.getWhereUsed());
            Assert.assertNotNull(cn.getTypeOwner());
            Assert.assertFalse(cn.getTypeOwner().getName().isEmpty());
            Assert.assertFalse(cn.getTypeOwner().getNamespace().isEmpty());
        }

        if (n.isTypeUser()) {
            ComponentNode cn = (ComponentNode) n;
            if (cn.getType() == null)
                LOGGER.debug("FIXME - Null type: " + cn);
            else {
                // Why do you get null types? Maybe the library is not editable.
                // LOGGER.debug("Testing " + cn.getLibrary() + "-" + cn + " of type \t"
                // + cn.getType().getClass().getSimpleName() + ":" + cn.getType());
                Assert.assertNotNull(cn.getType());
                Assert.assertFalse(cn.getType().getName().isEmpty());
                if (cn.getType().getNamespace().isEmpty())
                    LOGGER.warn("Namespace is empty for " + cn);
                Assert.assertFalse(cn.getType().getNamespace().isEmpty());
            }
        }
    }

}

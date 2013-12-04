package com.sabre.schemas.testUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.saver.LibraryModelSaver;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemas.node.BusinessObjectNode;
import com.sabre.schemas.node.CoreObjectNode;
import com.sabre.schemas.node.EnumerationClosedNode;
import com.sabre.schemas.node.EnumerationOpenNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFactory;
import com.sabre.schemas.node.NodeFinders;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.node.SimpleTypeNode;
import com.sabre.schemas.node.VWA_Node;
import com.sabre.schemas.node.properties.AttributeNode;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.EnumLiteralNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.stl2Developer.reposvc.RepositoryTestUtils;

/**
 * Creates a mock library in the runtime-OT2Editor.product directory. Is added to the passed
 * project.
 * 
 * @author Dave Hollander
 * 
 */
public class MockLibrary {
    static final Logger LOGGER = LoggerFactory.getLogger(MockLibrary.class);

    public LibraryNode createNewLibrary(String ns, String name, ProjectNode parent) {
        TLLibrary tllib = new TLLibrary();
        tllib.setName(name);
        tllib.setStatus(TLLibraryStatus.DRAFT);
        // causes compiler errors - tllib.setNamespaceAndVersion(ns, "0.0.0");
        tllib.setNamespaceAndVersion(ns, "1.0.0");
        tllib.setPrefix("ttt");
        // LOGGER.debug("Set new library namespace to: " + tllib.getPrefix() + ":"
        // + tllib.getNamespace());

        String testPath;
        try {
            testPath = createTempFile(name + "-Test", ".otm");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        URL testURL = URLUtils.toURL(new File(testPath));
        tllib.setLibraryUrl(testURL);
        LibraryNode ln = new LibraryNode(tllib, parent);

        // Has to be saved to be used in a project. Is not editable yet, so
        // can't use lib controller
        try {
            new LibraryModelSaver().saveLibrary(tllib);
        } catch (LibrarySaveException e) {
            LOGGER.debug("Error Saving: ", e);
        }
        addBusinessObjectToLibrary(ln, "InitialBO");
        Assert.assertTrue(ln.isValid());
        return ln;
    }

    public static String createTempFile(String name, String suffix) throws IOException {
        final File tempDir = File.createTempFile("temp-otm-" + name,
                Long.toString(System.nanoTime()));

        if (!(tempDir.delete())) {
            throw new IOException("Could not delete temp file: " + tempDir.getAbsolutePath());
        }

        if (!(tempDir.mkdir())) {
            throw new IOException("Could not create temp directory: " + tempDir.getAbsolutePath());
        }
        // Because the tempDir contains the *.bak file the deleteOnExit will not work.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (tempDir.exists()) {
                    RepositoryTestUtils.deleteContents(tempDir);
                }
            }
        });
        tempDir.deleteOnExit();
        File f = File.createTempFile(name, suffix, tempDir);
        f.deleteOnExit();
        return f.getPath();
    }

    public BusinessObjectNode addBusinessObjectToLibrary(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestBO";
        BusinessObjectNode newNode = (BusinessObjectNode) NodeFactory
                .newComponent(new TLBusinessObject());
        newNode.setName(name);
        ln.addMember(newNode);

        PropertyNode newProp = new ElementNode(newNode.getIDFacet(), "TestID");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        // newNode.getIDFacet().getChildren().add(newProp);

        newProp = new ElementNode(newNode.getSummaryFacet(), "TestSum");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        // newNode.getSummaryFacet().getChildren().add(newProp);
        return newNode;
    }

    /**
     * Create several nodes that use each other as types
     * 
     * @param ln
     * @return
     */
    public Node addNestedTypes(LibraryNode ln) {
        BusinessObjectNode n1 = (BusinessObjectNode) NodeFactory
                .newComponent(new TLBusinessObject());
        n1.setName("n1");
        ln.addMember(n1);

        CoreObjectNode n2 = (CoreObjectNode) NodeFactory.newComponent(new TLCoreObject());
        n2.setName("n2");
        ln.addMember(n2);
        n2.setSimpleType(NodeFinders.findNodeByName("int", Node.XSD_NAMESPACE));
        PropertyNode n2Prop = new ElementNode(n2.getSummaryFacet(), "TestElement2");
        n2Prop.setAssignedType(n1);

        CoreObjectNode n3 = (CoreObjectNode) NodeFactory.newComponent(new TLCoreObject());
        n3.setName("n3");
        ln.addMember(n3);
        n3.setSimpleType(NodeFinders.findNodeByName("int", Node.XSD_NAMESPACE));
        PropertyNode n3PropA = new ElementNode(n3.getSummaryFacet(), "TestElement3a");
        n3PropA.setAssignedType(n1);
        PropertyNode n3PropB = new ElementNode(n3.getSummaryFacet(), "TestElement3b");
        n3PropB.setAssignedType(n2.getSummaryFacet());

        PropertyNode newProp = new ElementNode(n1.getIDFacet(), "TestID");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        newProp = new ElementNode(n1.getSummaryFacet(), "TestSumA");
        newProp.setAssignedType(n2);
        newProp = new ElementNode(n1.getSummaryFacet(), "TestSumB");
        newProp.setAssignedType(n3.getSimpleFacet());
        return n1;
    }

    public CoreObjectNode addCoreObjectToLibrary(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestCore";
        CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newComponent(new TLCoreObject());
        newNode.setName(name);
        newNode.setSimpleType(NodeFinders.findNodeByName("int", Node.XSD_NAMESPACE));
        PropertyNode newProp = new ElementNode(newNode.getSummaryFacet(), "TestElement");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        ln.addMember(newNode);
        return newNode;
    }

    public SimpleTypeNode addSimpleTypeToLibrary(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "SimpleType";
        SimpleTypeNode sn = new SimpleTypeNode(new TLSimple());
        sn.setName(name);
        sn.setAssignedType(NodeFinders.findNodeByName("int", Node.XSD_NAMESPACE));
        ln.addMember(sn);
        return sn;
    }

    public VWA_Node addVWA_ToLibrary(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestVWA";
        VWA_Node newNode = (VWA_Node) NodeFactory.newComponent(new TLValueWithAttributes());
        newNode.setName(name);
        newNode.setSimpleType(NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE));
        PropertyNode newProp = new AttributeNode(newNode.getAttributeFacet(), "TestAttribute");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        ln.addMember(newNode);
        return newNode;
    }

    public EnumerationOpenNode addOpenEnumToLibrary(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestOpen";
        EnumerationOpenNode newNode = (EnumerationOpenNode) NodeFactory
                .newComponent(new TLOpenEnumeration());
        newNode.setName(name);
        PropertyNode newProp = new EnumLiteralNode(newNode, "Lit-O1");
        ln.addMember(newNode);
        return newNode;
    }

    public EnumerationClosedNode addClosedEnumToLibrary(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestClosed";
        EnumerationClosedNode newNode = (EnumerationClosedNode) NodeFactory
                .newComponent(new TLClosedEnumeration());
        newNode.setName(name);
        PropertyNode newProp = new EnumLiteralNode(newNode, "Lit-C1");
        ln.addMember(newNode);
        return newNode;
    }
}

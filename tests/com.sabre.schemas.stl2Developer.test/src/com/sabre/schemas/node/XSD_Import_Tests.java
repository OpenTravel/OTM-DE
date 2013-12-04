/**
 * 
 */
package com.sabre.schemas.node;

import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.junit.Test;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.testUtils.LoadFiles;

/**
 * @author Dave Hollander
 * 
 */
public class XSD_Import_Tests {

    @Test
    public void xsdImportTest() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        Node_Tests nt = new Node_Tests();
        List<Node> imported;

        LibraryNode sourceLib = lf.loadXfile1(mc); // SabreTypeLibrary_v.1.1.13.xsd
        LibraryNode destLib = lf.loadFile1(mc);
        sourceLib.setEditable(true);
        destLib.setEditable(true);
        sourceLib.visitAllNodes(nt.new TestNode());

        imported = destLib.importNodes(sourceLib.getDescendants_NamedTypes(), true);
        Assert.assertFalse(imported.isEmpty());

        checkContents(destLib);
        destLib.visitAllNodes(nt.new TestNode());

        checkContents(sourceLib);
        sourceLib.visitAllNodes(nt.new TestNode());
    }

    /**
     * Specific tests for the SabreTypeLibrary_v.1.1.13.xsd schema.
     * 
     * @param lib
     */
    private void checkContents(LibraryNode lib) {
        String ns = "http://services.sabre.com/STL/v01";
        Node n = null;
        // Name of a top level object
        n = NodeFinders.findNodeByName("Address.Agency", ns);
        Assert.assertNotNull(n);
        checkGateStand(lib);

        // Name of a property
        n = NodeFinders.findNodeByName("StateProv", ns);
        Assert.assertNotNull(n);
        // Type assigned to that property : StringLength1to64
        Assert.assertEquals("StringLength1to64", n.getType().getName());

        // Name of an attribute
        n = NodeFinders.findNodeByName("shareMarketing", ns);
        Assert.assertNotNull(n);
        Assert.assertEquals("ShareIndicator", n.getType().getName());

        checkGateStand(lib);
    }

    private void checkGateStand(LibraryNode lib) {
        String ns = lib.getNamespace();
        Node n = null;

        // Find Gate Information and make sure two properties are OK
        // The GateStand type for these properties is an example of when aliases are
        // needed on import.
        n = NodeFinders.findTypeProviderByQName(new QName(ns, "GateInformation"), lib);
        Assert.assertNotNull(n);
        Assert.assertTrue(n instanceof CoreObjectNode);
        CoreObjectNode core = (CoreObjectNode) n;
        Node departure = null, arrival = null;
        for (Node child : core.getSummaryFacet().getChildren()) {
            if (child.getName().startsWith("Departure"))
                departure = child;
            if (child.getName().startsWith("Arrival"))
                arrival = child;
        }
        // Will fail if source library was not editable.
        Assert.assertNotNull(departure);
        Assert.assertTrue(departure.getType().getOwningComponent().getName().equals("GateStand"));
        Assert.assertNotNull(arrival);
        Assert.assertTrue(arrival.getType().getOwningComponent().getName().equals("GateStand"));
    }

}

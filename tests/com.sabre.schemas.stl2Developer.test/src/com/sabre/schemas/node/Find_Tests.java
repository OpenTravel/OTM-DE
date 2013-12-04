/**
 * 
 */
package com.sabre.schemas.node;

import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.testUtils.LoadFiles;

/**
 * @author Dave Hollander
 * 
 */
public class Find_Tests {
    ModelNode model = null;
    Node_Tests tt = new Node_Tests();

    @Test
    public void FinderTest() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        model = mc.getModelNode();

        lf.loadTestGroupA(mc);
        for (LibraryNode ln : model.getUserLibraries()) {
            tt.visitAllNodes(ln);
        }

        QName qn = new QName("NameSpace", "Name");
        Assert.assertNull(NodeFinders.findTypeProviderByQName(qn));

        qn = new QName("http://www.sabre.com/ns/OTA2/Demo/Profile/v01", "OutboundFlight");
        Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));

        Node alias = NodeFinders.findTypeProviderByQName(qn);
        qn = new QName("http://www.sabre.com/ns/OTA2/Demo/Profile/v01", "Card");
        Assert.assertNotNull(alias);

        qn = new QName("http://www.sabre.com/ns/OTA2/Demo/Profile/v01", "Card");
        Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));

        qn = new QName("http://www.sabre.com/ns/OTA2/Demo/Profile/v01", "TravelerProfile");
        Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));

        qn = new QName("http://services.sabre.com/STL/Examples/v02", "SimpleVWA");
        Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));

        qn = new QName("http://services.sabre.com/STL/Test4/v02", "BasicCore");
        Assert.assertNotNull(NodeFinders.findTypeProviderByQName(qn));
        Assert.assertNotNull(NodeFinders.findNodeByName("BasicCore",
                "http://services.sabre.com/STL/Test4/v02"));

        // library not loaded yet...should return null.
        Assert.assertNull(NodeFinders.findNodeByName("StandardCore_Simple",
                "http://services.sabre.com/STL/PNR/v02"));
        lf.loadFilePNRB(mc);
        // a library member
        Assert.assertNotNull(NodeFinders.findNodeByName("AirSegment.PNRB",
                "http://services.sabre.com/STL/PNR/v02"));
        // a property
        Assert.assertNotNull(NodeFinders.findNodeByName("NumberOfSeats",
                "http://services.sabre.com/STL/PNR/v02"));

    }

}

/**
 * 
 */
package org.opentravel.schemas.node;

import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.testUtils.LoadFiles;

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
    }

}

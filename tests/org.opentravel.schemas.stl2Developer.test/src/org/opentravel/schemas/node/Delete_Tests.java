/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeModelTestUtils;
import org.opentravel.schemas.testUtils.LoadFiles;

/**
 * @author Dave Hollander
 * 
 */
public class Delete_Tests {
    ModelNode model = null;

    @Test
    public void deleteTest() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();
        model = mc.getModelNode();
        Node_Tests tt = new Node_Tests();

        lf.loadTestGroupA(mc);
        int i = 0;
        for (LibraryNode ln : model.getUserLibraries()) {
            ln.setEditable(true);
            if (i++ == 1)
                deleteAllMembers(ln);
            else
                deleteEachMember(ln);
        }
        tt.visitAllNodes(model);
    }

    private void deleteEachMember(LibraryNode ln) {
        int x;
        for (Node n : ln.getDescendants()) {
            if (!n.isNavigation()) {
                if (n.isDeleted())
                    continue;
                INode user = null;
                if (n.getModelObject() == null)
                    x = 1;
                // System.out.println("Deleting "+n);
                // Make sure the users of this type are informed of deletion.
                if (n.getTypeUsersCount() > 0) {
                    user = n.getTypeUsers().get(0);
                }
                n.delete();
                if (user != null && n.isDeleteable()) {
                    Assert.assertNotSame(n, user.getType());
                }
            }
        }
        NodeModelTestUtils.testNode(ln);
        Assert.assertEquals(2, ln.getDescendants().size());
    }

    private void deleteAllMembers(LibraryNode ln) {
        ArrayList<Node> members = new ArrayList<Node>(ln.getDescendants_NamedTypes());
        NodeModelTestUtils.testNode(ln);
        Node.deleteNodeList(members);
        NodeModelTestUtils.testNode(ln);
        NodeModelTestUtils.testNodeModel();
        Assert.assertEquals(2, ln.getDescendants().size());
    }

}

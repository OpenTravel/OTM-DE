/**
 * 
 */
package com.sabre.schemas.node;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.Node_Tests.TestNode;
import com.sabre.schemas.testUtils.LoadFiles;

/**
 * @author Dave Hollander
 */

public class Remove_Tests {
    private static final Logger LOGGER = LoggerFactory.getLogger(Remove_Tests.class);

    TestNode nt = new Node_Tests().new TestNode();
    LibraryTests lt = new LibraryTests();
    LibraryNode target = null;

    @Test
    public void removeTests() throws Exception {
        MainController mc = new MainController();
        LoadFiles lf = new LoadFiles();

        lf.loadTestGroupA(mc);
        // lf.loadFile4(mc); // no unassigned types
        target = lf.loadFilePNRB(mc);
        // target = lf.loadFile3(mc);
        lt.visitLibrary(target);

        // Remove all members of each library and add them to the target.
        for (LibraryNode ln : Node.getAllUserLibraries()) {
            if (ln == target)
                continue;
            lt.visitLibrary(ln);
            removeAllMembers(ln);
            // library will be empty so you an not do: lt.visitLibrary(ln);
        }
        lt.visitLibrary(target);

        // Now remove all the moved members.
        for (Node n : target.getDescendants_NamedTypes()) {
            target.removeMember(n); // May change type assignments!
        }
        Assert.assertEquals(0, target.getDescendants_NamedTypes().size());
    }

    private void removeAllMembers(LibraryNode ln) {
        int x = 0;
        if (!ln.isTLLibrary())
            return; // can not delete from xsd or built-in

        for (Node n : ln.getDescendants_NamedTypes()) {
            if (n.isService()) {
                x++;
                continue;
            }
            // LOGGER.debug("Removing " + n + " from library " + ln);
            nt.visit(n);
            ln.removeMember(n); // Visit Node should fail due to missing library
            target.addMember(n);
            nt.visit(n);
        }
        // if (ln.getDescendants_NamedTypes().size() > 0)
        // x = ln.getDescendants_NamedTypes().size();
        Assert.assertEquals(x, ln.getDescendants_NamedTypes().size());
    }

}

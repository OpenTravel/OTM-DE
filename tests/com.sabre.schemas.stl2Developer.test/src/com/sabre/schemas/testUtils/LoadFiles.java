/**
 * 
 */
package com.sabre.schemas.testUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.node.ImpliedNode;
import com.sabre.schemas.node.ImpliedNodeType;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.Node.NodeVisitor;
import com.sabre.schemas.node.Node_Tests;
import com.sabre.schemas.node.ProjectNode;

/**
 * @author Dave Hollander
 * 
 */
public class LoadFiles {
    private String filePath1 = "Resources" + File.separator + "testFile1.otm";
    private String filePath2 = "Resources" + File.separator + "testFile2.otm";
    private String filePath3 = "Resources" + File.separator + "testFile3.otm";
    private String filePath4 = "Resources" + File.separator + "testFile4.otm";
    private String path5 = "Resources" + File.separator + "testFile5.otm";
    private String path5c = "Resources" + File.separator + "testFile5-Clean.otm";

    private MainController mc;

    public LoadFiles() {
    }

    @Test
    public void loadFiles() throws Exception {
        this.mc = new MainController();

        int libCnt = 3; //built-ins
        try {
            loadFile1(mc);
            libCnt++;
            loadFile2(mc);
            libCnt++;
            loadFile3(mc);
            libCnt++;
            loadFile4(mc);
            libCnt++;
            loadFile5(mc);
            libCnt++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertEquals(libCnt, Node.getAllLibraries().size());

        loadTestGroupA(mc);
    }

    /**
     * Load files 1 through 5. No tests.
     */
    public int loadTestGroupA(MainController mc) throws Exception {
        ProjectController pc = mc.getProjectController();

        List<File> files = new ArrayList<File>();
        files.add(new File(filePath1));
        files.add(new File(filePath2));
        files.add(new File(filePath3));
        files.add(new File(filePath4));
        files.add(new File(path5));
        pc.getDefaultProject().add(files);
        int libCnt = pc.getDefaultProject().getChildren().size();
        return libCnt;
    }

    /**
     * Remove any nodes with bad assignments. The removed nodes will not pass
     * Node_Tests/visitNode().
     */
    public void cleanModel() {
        NodeVisitor srcVisitor = new Node_Tests().new validateTLObject();
        for (LibraryNode ln : Node.getAllUserLibraries()) {
            // LOGGER.debug("Cleaning Library " + ln + " with " +
            // ln.getDescendants_TypeUsers().size()
            // + " type users.");
            for (Node n : ln.getDescendants_TypeUsers()) {
                if (n.getType() instanceof ImpliedNode) {
                    if (((ImpliedNode) n.getType()).getImpliedType().equals(
                            ImpliedNodeType.UnassignedType)) {
                        // LOGGER.debug("Removing " + n + " due to unassigned type.");
                        n.getOwningComponent().delete();
                        continue;
                    }
                }
                if (n.getTLTypeObject() == null) {
                    // LOGGER.debug("Removing " + n + " due to null TL_TypeObject.");
                    n.getOwningComponent().delete();
                    continue;
                }
                if (!n.getTypeClass().verifyAssignment()) {
                    // LOGGER.debug("Removing " + n + " due to type node mismatch.");
                    n.getOwningComponent().delete();
                    continue;
                }
                try {
                    srcVisitor.visit(n);
                } catch (IllegalStateException e) {
                    // LOGGER.debug("Removing " + n + " due to: " + e.getLocalizedMessage());
                    n.getOwningComponent().delete();
                }

            }
        }
    }

    /**
     * Load a file into the default project. NOTE - if the file is already open an assertion error
     * will be thrown. NOTE - the returned library might not be the one opened, it might be one
     * imported.
     * 
     * @param main
     *            controller
     * @param file
     *            path
     * @return library node containing model created from the OTM file.
     */
    public LibraryNode loadFile(MainController thisModel, String path) {
        ProjectNode project = thisModel.getProjectController().getDefaultProject();
        List<File> files = new ArrayList<File>();
        files.add(new File(path));
        project.add(files);
        Assert.assertNotNull(project);
        Assert.assertTrue(project.getChildren().size() > 0);

        URL u = URLUtils.toURL(new File(System.getProperty("user.dir") + File.separator + path));
        LibraryNode ln = null;
        for (LibraryNode lib : project.getLibraries()) {
            URL url = lib.getTLaLib().getLibraryUrl();
            if (u.equals(url)) {
                ln = lib;
                break;
            }

        }
        Assert.assertNotNull(ln);
        Assert.assertTrue(ln.getChildren().size() > 1);
        return ln;
    }

    // Has 1 unassigned types.
    public LibraryNode loadFile1(MainController mc) {
        ModelNode model = mc.getModelNode();
        LibraryNode ln = loadFile(mc, filePath1);
        Assert.assertNotNull(ln);
        Assert.assertTrue(ln.getChildren().size() > 2);
        Assert.assertNotNull(model);
        Assert.assertNotNull(model.getTLModel());
        Assert.assertTrue(Node.getAllLibraries().size() > 2);
        Assert.assertTrue(Node.getNodeCount() > 100);
        Assert.assertTrue(model.getUnassignedTypeCount() >= 0);
        return ln;
    }

    // Has 14 unassigned types - references to STL2 library
    public LibraryNode loadFile2(MainController thisModel) {
        ModelNode model = thisModel.getModelNode();
        LibraryNode ln = loadFile(thisModel, filePath2);
        Assert.assertNotNull(ln);
        Assert.assertNotNull(model);
        Assert.assertNotNull(model.getTLModel());
        Assert.assertTrue(Node.getNodeCount() > 100);
        Assert.assertTrue(model.getUnassignedTypeCount() >= 14);
        return ln;
    }

    public LibraryNode loadFile3(MainController thisModel) {
        LibraryNode ln = loadFile(thisModel, filePath3);

        Assert.assertNotNull(ln);
        Assert.assertTrue(ln.getChildren().size() > 1);
        Assert.assertTrue(ln.getDescendants_NamedTypes().size() >= 3);

        return ln;
    }

    public LibraryNode loadFile4(MainController thisModel) {
        LibraryNode ln = loadFile(thisModel, filePath4);
        Assert.assertNotNull(ln);
        Assert.assertTrue(ln.getChildren().size() > 1);
        List<Node> d = ln.getDescendants_NamedTypes();
        Assert.assertEquals(7, d.size());
        return ln;
    }

    public LibraryNode loadFile5(MainController thisModel) {
        LibraryNode ln = loadFile(thisModel, path5);
        return ln;
    }

    public LibraryNode loadFile5Clean(MainController thisModel) {
        LibraryNode ln = loadFile(thisModel, path5c);
        return ln;
    }
}

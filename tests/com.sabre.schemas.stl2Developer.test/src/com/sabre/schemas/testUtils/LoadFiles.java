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
    String filePath1 = "Resources" + File.separator + "testFile1.otm";
    String filePath2 = "Resources" + File.separator + "testFile2.otm";
    String filePath3 = "Resources" + File.separator + "testFile3.otm";
    String filePath4 = "Resources" + File.separator + "testFile4.otm";
    String path5 = "Resources" + File.separator + "testFile5.otm";
    String path5c = "Resources" + File.separator + "testFile5-Clean.otm";
    String pathEmpty = "Resources" + File.separator + "EmptyOTM.otm";
    String filePNRB = "Resources" + File.separator + "PNR-Builder.otm";

    String xpath1 = "Resources" + File.separator + "SabreTypeLibrary_v.1.1.13.xsd";
    String xpath2 = "Resources" + File.separator + "ACS_PassengerService_v0.0.1.xsd";
    String xpath3 = "Resources" + File.separator + "ACS_BSO_v.0.0.3.xsd";
    String xpathDsse = "Resources" + File.separator + "DsseResponse.xsd";

    private MainController mc;

    public LoadFiles() {
    }

    @Test
    public void loadFiles() throws Exception {
        this.mc = new MainController();

        int libCnt = 0;
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
            loadXfile1(mc);
            libCnt++;
            loadXfile2(mc);
            libCnt += 2; // has 1 import (STL_For_ACS_BSO)
            loadXfile3(mc);
            libCnt += 4; // lots of includes/imports
            loadFilePNRB(mc);
            libCnt += 4; // 3 includes/imports (2 xsd, 1 otm)
        } catch (Exception e) {
            e.printStackTrace();
        }
        libCnt += 1; //add deprecated OTA2_BuiltIns
        Assert.assertEquals(libCnt, Node.getAllLibraries().size());

        // THIS DOES NOT WORK - ln is null! loadFile5Clean(mc);
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

    public LibraryNode loadFilePNRB(MainController thisModel) {
        LibraryNode ln = loadFile(thisModel, filePNRB);
        Assert.assertNotNull(ln);
        Assert.assertTrue(ln.getChildren().size() > 1);
        return ln;
    }

    public LibraryNode loadFile5(MainController thisModel) {
        LibraryNode ln = loadFile(thisModel, path5);
        return ln;
    }

    /**
     * BROKEN!
     * 
     * @param thisModel
     * @return
     */
    public LibraryNode loadFile5Clean(MainController thisModel) {
        LibraryNode ln = loadFile(thisModel, path5c);
        return ln;
    }

    public LibraryNode loadXfile1(MainController tm) {
        LibraryNode ln = loadFile(tm, xpath1);
        return ln;
    }

    public LibraryNode loadXfile2(MainController tm) {
        LibraryNode ln = loadFile(tm, xpath2);
        return ln;
    }

    public LibraryNode loadXfile3(MainController tm) {
        LibraryNode ln = loadFile(tm, xpath3);
        return ln;
    }

    public LibraryNode loadXfileDsse(MainController tm) {
        LibraryNode ln = loadFile(tm, xpathDsse);
        return ln;
    }

    public LibraryNode loadEmpty(MainController tm) {
        LibraryNode ln = loadFile(tm, pathEmpty);
        return ln;
    }
}

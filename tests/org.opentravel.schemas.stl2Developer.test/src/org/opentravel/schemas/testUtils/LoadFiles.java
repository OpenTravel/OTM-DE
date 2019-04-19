/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */

package org.opentravel.schemas.testUtils;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemas.controllers.DefaultProjectController.OpenedProject;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class LoadFiles {
    private static final Logger LOGGER = LoggerFactory.getLogger( LoadFiles.class );

    private String filePath1 = "Resources" + File.separator + "testFile1.otm";
    private String filePath2 = "Resources" + File.separator + "testFile2.otm";
    private String filePath3 = "Resources" + File.separator + "testFile3.otm";
    private String filePath4 = "Resources" + File.separator + "testFile4.otm";
    private String filePath5 = "Resources" + File.separator + "testFile5.otm";
    private String path5c = "Resources" + File.separator + "testFile5-Clean.otm";
    private String path6 = "Resources" + File.separator + "testFile6.otm";
    private String path7 = "Resources" + File.separator + "testFile7.otm";
    private String pathEP = "Resources" + File.separator + "testFile-ExtensionPoints2.otm";
    private String contextFile1 = "Resources" + File.separator + "base_library.otm";
    private String contextFile2 = "Resources" + File.separator + "facets1_library.otm";
    private String contextFile3 = "Resources" + File.separator + "facets2_library.otm";
    private String choiceFile1 = "Resources" + File.separator + "testFile_Choice1.otm";
    private String Project1 = "Resources" + File.separator + "testProject1.otp";
    private String Project2 = "Resources" + File.separator + "testProject2.otp";
    private String VersionTestProject =
        "Resources" + File.separator + "VersionTests" + File.separator + "VersionTests.otp";
    private String xsd1 = "Resources" + File.separator + "CommonTypes.xsd";
    private String xsd2 = "Resources" + File.separator + "CreateVWAFromExtened.xsd";
    private String xsd3 = "Resources" + File.separator + "OTA2_LibraryModel_v1.4.5.xsd";

    private MainController mc;
    private int nodeCount = 0;

    public LoadFiles() {}

    public void setMainController(MainController mc) {
        this.mc = mc;
    }

    /**
     * Assure Test files can be read.
     * 
     * @throws Exception
     */
    @Test
    public void loadFiles() throws Exception {
        this.mc = OtmRegistry.getMainController();
        List<LibraryNode> loaded = new ArrayList<>();

        // check special files
        ProjectController pc = mc.getProjectController();
        ProjectNode proj = pc.getDefaultProject();
        loaded.add( loadFile_Choice( proj ) );
        loaded.add( loadFile6( proj ) );
        // duplicate ns/name - loadFile7(proj);

        try {
            loaded.add( loadFile1( mc ) );
            loaded.add( loadFile2( mc ) );
            loaded.add( loadFile3( mc ) );
            loaded.add( loadFile4( mc ) );
            loaded.add( loadFile5( mc ) );
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<LibraryNode> libs = Node.getAllLibraries();
        List<LibraryNode> projLibs = proj.getLibraries();
        for (LibraryNode ln : loaded) {
            assertTrue( ln != null );
            assertTrue( "Model must contain library.", libs.contains( ln ) );
            assertTrue( "Project must contain library.", projLibs.contains( ln ) );
        }

        loadTestGroupA( mc );
    }

    @Test
    public void builtInTests() {
        MainController mc = OtmRegistry.getMainController();
        new LoadFiles();
        mc.getModelNode().visitAllNodes( new NodeTesters().new TestNode() );
    }

    /**
     * Load files 1 through 5 into default project. No tests. NOTE - file5 is not valid
     */
    public int loadTestGroupA(MainController mc) throws Exception {
        ProjectController pc = mc.getProjectController();
        return loadTestGroupA( pc.getDefaultProject() );
    }

    /**
     * Load files 1 through 5 into passed project. No tests.
     * <p>
     * <b>NOTE - file5 is not valid</b>
     */
    public int loadTestGroupA(ProjectNode proj) throws Exception {
        List<File> files = new ArrayList<>();
        files.add( new File( filePath1 ) );
        files.add( new File( filePath2 ) );
        files.add( new File( filePath3 ) );
        files.add( new File( filePath4 ) );
        files.add( new File( filePath5 ) );

        proj.add( files );
        new TypeResolver().resolveTypes();
        // Only true if model is already valid
        // new MockLibrary().check();

        int libCnt = proj.getChildren().size();
        return libCnt;
    }

    /**
     * Load files 1 through 5-clean into default project. No tests.
     */
    public int loadTestGroupAc(MainController mc) throws Exception {
        return loadTestGroupAc( mc.getProjectController().getDefaultProject() );
    }

    public int loadTestGroupAc(ProjectNode pn) throws Exception {
        // ProjectController pc = mc.getProjectController();

        List<File> files = new ArrayList<>();
        files.add( new File( filePath1 ) );
        files.add( new File( filePath2 ) );
        files.add( new File( filePath3 ) );
        files.add( new File( filePath4 ) );
        files.add( new File( path5c ) );
        pn.add( files );
        int libCnt = pn.getChildren().size();
        return libCnt;
    }

    /**
     * Load a file into the default project. NOTE - if the file is already open an assertion error will be thrown. NOTE
     * - the returned library might not be the one opened, it might be one imported.
     * 
     * @param main controller
     * @param file path
     * @return library node containing model created from the OTM file.
     */
    public LibraryNode loadFile(MainController thisModel, String path) {
        assertTrue( thisModel.getProjectController().getDefaultProject() != null );
        ProjectNode project = thisModel.getProjectController().getDefaultProject();
        LibraryNode ln = loadFile( project, path );
        if (ln == null) {
            LOGGER.error( "Failed to load file (" + new File( path ).canRead() + "): " + path );
        }
        assertTrue( ln != null );
        ln.setEditable( true );
        assertTrue( ln.isEditable() );
        return ln;
    }

    public LibraryNode loadFile(ProjectNode project, String path) {
        assertTrue( "Must have a non-null project.", project != null );
        List<File> files = new ArrayList<>();
        files.add( new File( path ) );
        for (File f : files) {
            assert f.exists();
            assert f.canRead();
        }
        assertTrue( "File must exist.", files.get( 0 ).exists() );

        // System.out.println("Project " + project + " namespace = " + project.getNamespace() + " file = " + path);
        project.add( files );

        if (project.getChildren().size() <= 0)
            LOGGER.error( "Failed to load file (" + new File( path ).canRead() + "): " + path );
        else {
            // Then - project must have the new library
            assertTrue( "Project must have children.", project.getChildren().size() > 0 );
            URL u = URLUtils.toURL( new File( System.getProperty( "user.dir" ) + File.separator + path ) );
            LibraryNode ln = null;
            List<URL> projURLs = new ArrayList<>();
            for (LibraryNode lib : project.getLibraries()) {
                URL url = lib.getTLModelObject().getLibraryUrl();
                projURLs.add( url );
                if (u.equals( url )) {
                    ln = lib;
                    break;
                }
            }
            assertTrue( "Library must be found that has the correct url.", ln != null );
            assertTrue( "Library must have children.", ln.getChildren().size() > 0 );
            new TypeResolver().resolveTypes();
            return ln;
        }
        return null;
    }

    // Has 1 unassigned types.
    public LibraryNode loadFile1(MainController mc) {
        LibraryNode ln = loadFile( mc, filePath1 );
        assertTrue( ln.getChildren().size() > 2 );
        assertTrue( Node.getAllLibraries().size() > 2 );
        return ln;
    }

    /**
     * Standalone test file. Does not import/include other libraries.
     * 
     * @param project
     * @return
     */
    public LibraryNode loadFile2(ProjectNode project) {
        return loadFile( project, filePath2 );
    }

    // Has 14 unassigned types - references to STL2 library
    public LibraryNode loadFile2(MainController thisMC) {
        return loadFile( thisMC, filePath2 );
    }

    public LibraryNode loadFile3(MainController thisModel) {
        LibraryNode ln = loadFile( thisModel, filePath3 );

        Assert.assertNotNull( ln );
        Assert.assertTrue( ln.getChildren().size() > 1 );
        Assert.assertTrue( ln.getDescendants_LibraryMembers().size() >= 3 );

        return ln;
    }

    public LibraryNode loadFile4(MainController thisModel) {
        LibraryNode ln = loadFile( thisModel, filePath4 );
        Assert.assertNotNull( ln );
        Assert.assertTrue( ln.getChildren().size() > 1 );
        List<LibraryMemberInterface> d = ln.getDescendants_LibraryMembers();
        return ln;
    }

    public LibraryNode loadFile5() {
        return loadFile5( mc );
    }

    /**
     * WARNING - this library has validation errors
     * 
     * @param thisModel
     * @return
     */
    public LibraryNode loadFile5(MainController thisModel) {
        LibraryNode ln = loadFile( thisModel, filePath5 );
        return ln;
    }

    public LibraryNode loadFile5Clean(MainController thisModel) {
        LibraryNode ln = loadFile( thisModel, path5c );
        return ln;
    }

    public LibraryNode loadFile_ExtensionPoint(MainController mc) {
        return loadFile( mc, pathEP );
    }

    /**
     * Load a version 1.5 library. NOT valid.
     * 
     * @param thisModel
     * @return
     */
    public LibraryNode loadFile6(MainController thisModel) {
        LibraryNode ln = loadFile( thisModel, path6 );
        return ln;
    }

    /**
     * Load a version 1.5 library. NOT valid.
     * 
     * @param thisModel
     * @return
     */
    public LibraryNode loadFile6(ProjectNode project) {
        return loadFile( project, path6 );
    }

    public LibraryNode loadFileXsd1(ProjectNode projectNode) {
        LibraryNode ln = loadFile( projectNode, xsd1 );
        return ln;
    }

    public LibraryNode loadFileXsd2(ProjectNode projectNode) {
        LibraryNode ln = loadFile( projectNode, xsd2 );
        return ln;
    }

    public LibraryNode loadFileXsd3(ProjectNode projectNode) {
        LibraryNode ln = loadFile( projectNode, xsd3 );
        return ln;
    }

    /**
     * Version 1.6 library with no Errors with resource and choice objects.
     */
    public LibraryNode loadFile7(ProjectNode project) {
        return loadFile( project, path7 );
    }

    /**
     * No Errors with contextual facets.
     */
    public LibraryNode loadFile_FacetBase(ProjectNode project) {
        return loadFile( project, contextFile1 );
    }

    /**
     * No Errors with contextual facets.
     */
    public LibraryNode loadFile_Choice(ProjectNode project) {
        return loadFile( project, choiceFile1 );
    }

    /**
     * No Errors with contextual facets.
     */
    public LibraryNode loadFile_Facets1(ProjectNode project) {
        return loadFile( project, contextFile2 );
    }

    /**
     * No Errors with contextual facets.
     */
    public LibraryNode loadFile_Facets2(ProjectNode project) {
        return loadFile( project, contextFile3 );
    }

    /**
     * Load project with test files 1,2,3. Simulates paths used by open project
     */
    public ProjectNode loadProject(ProjectController pc) {
        String fn = Project1; // files
        ProjectNode pn = pc.open( fn, null ).project;
        new TypeResolver().resolveTypes();
        return pn;
    }

    /**
     * Load project with test files 1,2,3. Simulates paths used by open project
     */
    public ProjectNode loadProject2(ProjectController pc) {
        String fn = Project2; // files
        ProjectNode pn = pc.open( fn, null ).project;
        new TypeResolver().resolveTypes();
        return pn;
    }

    /**
     * Load project with versioned test files from OTA repository. Project contains managed project item
     * VersionTest_Unmanaged_0_2_0.otm.
     */
    public ProjectNode loadVersionTestProject(ProjectController pc) {
        String fn = VersionTestProject; // files
        OpenedProject op = pc.open( fn, null );
        ProjectNode pn = null;
        if (op == null)
            LOGGER.error( "Null OpenProject object from open method." );
        else {
            pn = op.project;
            if (pn.getTLProject().getProjectItems().isEmpty()) {
                LOGGER.error( "Could not read version test project." );
                LOGGER.error( "Message: " + op.resultMsg );
            }
            new TypeResolver().resolveTypes();
        }
        return pn;
    }

    /**
     * Load the test files 1 though 5 and visit all nodes. Then either remove or delete each node.
     * 
     * @throws Exception
     */
    @Test
    public void testSuiteTests() throws Exception {
        mc = OtmRegistry.getMainController();
        LoadFiles lf = this;

        lf.loadFile4( mc );
        lf.loadFile2( mc );
        lf.loadFile1( mc );
        lf.loadFile5( mc );
        lf.loadFile3( mc );

        mc.getModelNode().visitAllNodes( new NodeTesters().new TestNode() );

        for (INode n : new ArrayList<>( mc.getModelNode().getChildren() ))
            actOnNode( (Node) n );
    }

    private void actOnNode(Node n) {
        if (n == null || n.getLibrary() == null)
            return;
        if (n instanceof TypeUser && n instanceof TypeProvider)
            ((TypeUser) n).setAssignedType( (TypeProvider) n );
        n.setName( "TEST" );
        switch (nodeCount % 3) {
            case 0:
                if (n instanceof LibraryMemberInterface)
                    n.getLibrary().removeMember( (LibraryMemberInterface) n );
                n.close();
                break;
            case 1:
            case 2:
                if (n.isDeleteable())
                    n.delete();
        }
    }

}

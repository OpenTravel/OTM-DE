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
package org.opentravel.schemas.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.stl2Developer.reposvc.RepositoryTestUtils;
import org.opentravel.schemas.trees.repository.RepositoryNode;

/**
 * Abstract base class that defines common methods used during live repository testing.
 * 
 * @author Pawel Jedruch
 */
public abstract class BaseProjectTest {

    protected static File tmpWorkspace;
    protected static DefaultRepositoryController rc;
    protected static MainController mc;
    protected static ProjectController pc;
    protected static ProjectNode defaultProject;
    private static List<ProjectNode> projectsToClean = new ArrayList<ProjectNode>();

    @BeforeClass
    public final static void beforeTests() throws Exception {
        tmpWorkspace = new File(System.getProperty("user.dir"), "/target/test-workspace/");
        RepositoryTestUtils.deleteContents(tmpWorkspace);
        tmpWorkspace.deleteOnExit();
        mc = new MainController();
        rc = (DefaultRepositoryController) mc.getRepositoryController();
        pc = mc.getProjectController();
    }

    @Before
    public void beforeEachTest() throws Exception {
        defaultProject = createProject("Otm-Test-DefaultProject", rc.getLocalRepository(), "IT");
        callBeforeEachTest();
    }

    protected void callBeforeEachTest() throws Exception {
        // Do nothing
    }

    @After
    public void afterEachTest() throws RepositoryException, IOException {
        pc.closeAll();
        for (ProjectNode pn : projectsToClean) {
            RepositoryTestUtils.deleteContents(pn.getProject().getProjectFile().getParentFile());
        }
        projectsToClean.clear();
    }

    public static ProjectNode createProject(String name, RepositoryNode nodeForNamespace,
            String nameSpaceSuffix) {
        File projectDir = new File(tmpWorkspace, name);
        File projectFile = new File(projectDir, name + ".otp");
        ProjectNode project = pc.create(projectFile, nodeForNamespace.getNamespace() + "/"
                + nameSpaceSuffix, name, "");
        projectsToClean.add(project);
        return project;
    }

    @AfterClass
    public final static void afterTests() throws Exception {
        RepositoryTestUtils.deleteContents(tmpWorkspace);
    }

    public static File createTempDirectory(String name) throws IOException {
        final File temp;

        temp = File.createTempFile(name, Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    public static File createFolder(File parent, String folder) {
        File file = new File(parent, folder);
        file.mkdir();
        return file;
    }

}

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
package org.opentravel.schemas.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemas.node.Library_FunctionTests;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.views.TypeView;

/**
 * @author Dave Hollander
 *
 */
public class MainController_Tests {
	private static MainController mc;
	private static MockLibrary ml;
	private static DefaultProjectController pc;
	private static ProjectNode defaultProject;

	ModelNode model = null;
	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	Library_FunctionTests lt = new Library_FunctionTests();

	@BeforeClass
	public static void initTests() {
		mc = OtmRegistry.getMainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Before
	public void beforeAllTests() {
		pc.closeAll();
	}

	@Test
	public void MainControllerTest() throws Exception {

		// Given - a set of files
		lf.loadTestGroupA(mc);
		// Given - 4th node in 3rd file
		Node testNode = null;
		int i = 0;
		for (LibraryNode ln : Node.getAllUserLibraries()) {
			if (i++ == 3) {
				int x = 0;
				for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
					if (x++ == 4)
						testNode = (Node) n;
			}
		}
		assert testNode != null;

		// Make sure all controllers have loaded.
		LibraryController libraryController = mc.getLibraryController();
		ModelController modelController = mc.getModelController();
		ContextController contextController = mc.getContextController();
		ProjectController projectController = mc.getProjectController();
		assertNotNull(libraryController);
		assertNotNull(modelController);
		assertNotNull(contextController);
		assertNotNull(projectController);
		assertNotNull(mc.getSections());

		// Creating Main Controller initializes a navigator view as default.
		// That is why it has content.
		OtmView navView = mc.getDefaultView();
		assertNotNull(navView);

		// Make sure all selected item access methods are safe to use without Display
		mc.getSelectedLibraries();
		mc.getSelectedUserLibraries();
		mc.getSelectedComponents_NavigatorView();
		mc.getSelectedNodes_NavigatorView();
		mc.getSelectedNode_NavigatorView();
		mc.getSelectedNodes_TypeView();
		mc.getSelectedNode_TypeView();

		// Assure current node controls
		OtmRegistry.registerTypeView(new TypeView());
		mc.setCurrentNode_NavigatorView(testNode);
		mc.setCurrentNode_PropertiesView(testNode);
		mc.setCurrentNode_TypeView(testNode);
		assertEquals(testNode, mc.getCurrentNode_NavigatorView());
		assertEquals(testNode, mc.getCurrentNode_PropertiesView());
		assertEquals(testNode, mc.getCurrentNode_TypeView());

		// Uses current node to find something to return.
		// Tries to return library members under the BO
		// assertFalse(mc.getSelectedNodes_NavigatorView().isEmpty());

		// TODO - test running commands

		// Make sure post status is safe
		mc.postStatus("TESTING via MainController_Test.");

	}

}

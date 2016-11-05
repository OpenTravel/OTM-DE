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


import junit.framework.Assert;

import org.junit.Test;
import org.opentravel.schemas.controllers.ContextController;
import org.opentravel.schemas.controllers.LibraryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ModelController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.LibraryTests;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeModelController;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.views.TypeView;

/**
 * @author Dave Hollander
 *
 */
public class MainController_Tests {
	ModelNode model = null;
	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();

	@Test
	public void MainControllerTest() throws Exception {
		MainController mc = new MainController();
		
		lf.loadTestGroupA(mc);
		Node testNode = null;
		int i = 0;
		for (LibraryNode ln : Node.getAllUserLibraries()) {
			ln.visitAllNodes(nt.new TestNode());
			if (i++ == 3) {
				int x = 0;
				for (Node n : ln.getDescendants_LibraryMembers())
					if (x++ == 4) testNode = n;
			}
		}
		Assert.assertNotNull(testNode);
		
		// Make sure all controllers have loaded.
		LibraryController libraryController = mc.getLibraryController();
		ModelController modelController = mc.getModelController();
		NodeModelController nodeController = mc.getNodeModelController();
		ContextController contextController = mc.getContextController();
		ProjectController projectController = mc.getProjectController();
		Assert.assertNotNull(libraryController);
		Assert.assertNotNull(modelController);
		Assert.assertNotNull(nodeController);
		Assert.assertNotNull(contextController);
		Assert.assertNotNull(projectController);
		Assert.assertNotNull(mc.getSections());
		
		// Creating Main Controller initializes a navigator view as default.
		// That is why it has content.
		OtmView navView = mc.getDefaultView();
		Assert.assertNotNull(navView);
		
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
		Assert.assertEquals(testNode, mc.getCurrentNode_NavigatorView());
		Assert.assertEquals(testNode, mc.getCurrentNode_PropertiesView());
		Assert.assertEquals(testNode, mc.getCurrentNode_TypeView());
		
		// Uses current node to find something to return.
		Assert.assertFalse(mc.getSelectedNodes_NavigatorView().isEmpty());

		// TODO - test running commands
		
		// Make sure post status is safe
		mc.postStatus("TESTING via MainController_Test.");
		
}
	
	


	
}

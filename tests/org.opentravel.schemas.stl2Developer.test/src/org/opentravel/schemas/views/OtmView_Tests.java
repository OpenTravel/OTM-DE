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
package org.opentravel.schemas.views;

import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.Library_FunctionTests;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeModelTestUtils;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.views.example.ExampleView;

import junit.framework.Assert;

/**
 * Test the OTM View interface. Note: you can not test the individual views because they require the workbench execution
 * environment.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmView_Tests {
	ModelNode model = null;
	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	Library_FunctionTests lt = new Library_FunctionTests();

	protected NavigatorView nv;

	public OtmView_Tests() {
	}

	@Test
	public void viewTests() throws Exception {
		MainController mc = OtmRegistry.getMainController();
		model = mc.getModelNode();

		lf.loadTestGroupA(mc);
		for (LibraryNode ln : Node.getAllLibraries()) {
			nt.visitAllNodes(ln);
		}
		NodeModelTestUtils.testNodeModel();

		OtmView view = mc.getDefaultView();
		Assert.assertNotNull(view);

		view = OtmRegistry.getNavigatorView();
		Assert.assertNotNull(view);
		checkViewMethods(view);

		TypeView tv = new TypeView();
		view = OtmRegistry.getTypeView();
		checkViewMethods(view);

		// ExampleView ev = new ExampleView();
		// view = OtmRegistry.getExampleView();
		// checkViewMethods(view);
	}

	protected void checkViewMethods(OtmView view) {
		// just make sure these are implemented safely.
		view.activate();
		Assert.assertFalse(view.getViewID().isEmpty());
		view.clearFilter();
		view.clearSelection();
		view.collapse();
		view.expand();
		view.refresh();
		view.refreshAllViews();
		view.setDeepPropertyView(true);
		view.setDeepPropertyView(false);
		view.setExactMatchFiltering(true);
		view.setExactMatchFiltering(false);
		view.setInheritedPropertiesDisplayed(true);
		view.setInheritedPropertiesDisplayed(false);
		Assert.assertFalse(view.isShowInheritedProperties());
		view.setListening(false);
		view.setListening(true);

		// Make sure these are safe for all node types.
		for (INode node : model.getDescendants()) {
			view.refresh(node);
			if (!(view instanceof ExampleView)) {
				// do not generate examples this cause loading 3 additional built-in libraries
				view.refresh(node, true);
			}
			view.refresh(node, false);
			view.refreshAllViews(node);
			view.select(node);
			view.setCurrentNode(node);
			view.setInput(node);
		}

		// should not be empty after the input and current node is set above.
		Assert.assertNotNull(view.getCurrentNode()); // must be "listening"
		view.getPreviousNode();
		Assert.assertNotNull(view.getSelectedNodes());
	}

}

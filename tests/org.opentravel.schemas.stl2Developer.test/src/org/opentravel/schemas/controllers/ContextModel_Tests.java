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


import org.junit.Test;
import org.opentravel.schemas.controllers.ContextController;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.testUtils.NodeTesters;

/**
 * @author Dave Hollander
 *
 */
public class ContextModel_Tests {
	ModelNode model = null;
	NodeTesters nt = new NodeTesters();
	ContextController controller;
	
	@Test
	public void contextModelTest() throws Exception {
//		MainController mc = new MainController();
//		LoadFiles lf = new LoadFiles();
//		model = mc.getModelNode();
//		controller = mc.getContextController();
//
//		controller.getDefaultContextId();
//		Assert.assertEquals(0, controller.getAvailableContextIds().size());
//		Assert.assertNotNull(controller.getRoot());
		
//		lf.loadTestGroupA(mc);
//		for (LibraryNode ln : model.getUserLibraries()) {
//			nt.visitAllNodes(ln);			
//			controller.addContexts(ln);
//			controller.getDefaultContextId(ln);
//			Assert.assertEquals(controller.getDefaultContextId(ln), 
//					controller.getSelectedId(ContextViewType.TYPE_VIEW, ln));
//			if (ln.getTLaLib() instanceof TLLibrary)
//				Assert.assertFalse(controller.getAvailableContextIds(ln.getTLaLib()).isEmpty());
//			if (ln.getTLaLib() instanceof TLLibrary)
//				Assert.assertNotNull(controller.getDefaultContext((TLLibrary) ln.getTLaLib()));
//			
//			for (String id : controller.getAvailableContextIds(ln)) {
//				// System.out.println("id = "+id+" app context = "+controller.getApplicationContext(ln, id));
//				Assert.assertNotNull(id);
//				Assert.assertFalse(id.isEmpty());
//				Assert.assertFalse(controller.getApplicationContext(ln, id).isEmpty());
//				controller.setSelectedId(ContextViewType.TYPE_VIEW, ln, id);
//				Assert.assertTrue(controller.setSelectedId(ContextViewType.CONTEXT_VIEW, ln, id));
//				Assert.assertFalse(controller.setSelectedId(ContextViewType.TYPE_VIEW, ln, "FOXX"));
//				Assert.assertEquals(id, controller.getSelectedId(ContextViewType.TYPE_VIEW, ln));
//				Assert.assertEquals(id, controller.getSelectedId(ContextViewType.CONTEXT_VIEW, ln));
//			}
//			// Test Merging contexts.			
//			ContextNode firstNode = null;
//			if (controller.getAvailableContextIds(ln).size() > 0) {
//				List<String> pre = controller.getAvailableContextIds(ln);
//				for (String id : controller.getAvailableContextIds(ln)) {
//					if (firstNode == null) {
//						firstNode = controller.getContextNode(ln, id);
//					}
//					else
//						controller.getContextModelManager().merge(controller.getContextNode(ln, id), firstNode);
//				}
//				List<String> post = controller.getAvailableContextIds(ln);
//				Assert.assertEquals(1, controller.getAvailableContextIds(ln).size());
//			}
//			// Test Clear
//			controller.clearContexts(ln);
//			Assert.assertEquals("", controller.getSelectedId(ContextViewType.TYPE_VIEW, ln));
//		}
//		// Should be true because there is no default library to set context.
//		Assert.assertTrue(controller.getDefaultContextId().isEmpty());
//		controller.clearContexts();
//		
	}
	


	
}

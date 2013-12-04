/**
 * 
 */
package com.sabre.schemas.controllers;


import org.junit.Test;

import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node_Tests;

/**
 * @author Dave Hollander
 *
 */
public class ContextModel_Tests {
	ModelNode model = null;
	Node_Tests nt = new Node_Tests();
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

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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemas.node.DocumentationNode;
import org.opentravel.schemas.node.DocumentationNode.DocumentationNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeModelTestUtils;
import org.opentravel.schemas.node.controllers.DocumentationNodeModelManager;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.NodeTesters;

/**
 * @author Dave Hollander
 *
 */
public class DocumentationNodeModelManager_Tests {
	ModelNode model = null;
	NodeTesters nt = new NodeTesters();
	DocumentationNodeModelManager dnmm = new DocumentationNodeModelManager();
	TLDocumentationItem di = new TLDocumentationItem();

	@Test
	public void dnmmTest() throws Exception {
		MainController mc = OtmRegistry.getMainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();

		lf.loadTestGroupA(mc);
		for (LibraryNode ln : Node.getAllLibraries()) {
			nt.visitAllNodes(ln);
		}
		NodeModelTestUtils.testNodeModel();
		di.setText("DDD For adding to other documentation.");

		for (LibraryNode ln : model.getUserLibraries()) {
			visitLibraryDoc(ln);
		}
	}

	private void visitLibraryDoc(LibraryNode ln) {
		DocumentationNode dn = null;
		for (Node n : ln.getDescendants()) {
			if (n.getDocumentation() != null) {
				dn = dnmm.createDocumentationTreeRoot(n.getDocumentation());
				TLDocumentationItem di = dn.getDocItem();
				Assert.assertNotNull(dn.getChildren());

				for (DocumentationNode d : dn.getChildren()) {
					visitDocItem(d);
				}
				Assert.assertNotNull(dn.getLabel());
				Assert.assertNotNull(dn.getValue());
			}
		}
	}

	private void visitDocItem(DocumentationNode dn) {
		List<DocumentationNode> kids = new ArrayList<DocumentationNode>(dn.getChildren());
		Assert.assertNotNull(kids);
		Assert.assertNotNull(dn.getLabel());
		Assert.assertNotNull(dn.getValue());
		Assert.assertNotNull(dn.getType());
		// labels = truncated value

		// Read a value, set it, then make sure it is set.
		if (dn.getType().equals(DocumentationNodeType.DOCUMENTATION_ITEM) && !dn.getValue().isEmpty()) {
			// System.out.println("Label: "+dn.getLabel());

			String doc = "AAA Now is the time for all good dogs to chase a cat.";
			dn.setValue(doc);
			Assert.assertEquals(doc, dn.getValue());

			// FIXME
			//
			// if (!(dn.getParent().getModelController() instanceof DescriptionDocItemNodeModelController)) {
			// int siblingCnt = dn.getParent().getChildren().size();
			// DocumentationNode newDN = dnmm.createDocItemNodeForTypeRoot(dn.getParent());
			// // NO - createDoc... adds it to parent. dn.getParent().addChild(newDN);
			// newDN.setValue(doc);
			// Assert.assertEquals(doc, newDN.getValue());
			// Assert.assertEquals(siblingCnt+1, dn.getParent().getChildren().size());
			//
			// newDN.getParent().removeChild(newDN);
			// Assert.assertEquals(siblingCnt, dn.getParent().getChildren().size());
			// }
		}

		for (DocumentationNode d : kids) {
			visitDocItem(d);
		}

	}

}

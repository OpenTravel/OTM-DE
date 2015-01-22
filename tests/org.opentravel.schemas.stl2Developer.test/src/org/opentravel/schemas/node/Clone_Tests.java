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
package org.opentravel.schemas.node;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
// TODO - test deleting the source and the clone with full visit node afterwards
public class Clone_Tests {
	private static final Logger LOGGER = LoggerFactory.getLogger(Clone_Tests.class);

	ModelNode model = null;
	Node_Tests tt = new Node_Tests();

	@Test
	public void cloneTest() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();

		LibraryNode source = lf.loadFile5Clean(mc);
		new LibraryChainNode(source); // Test in a chain
		// test cloning within library.
		source.setEditable(true);
		cloneMembers(source, source);

		LOGGER.debug("Testing cloning properties.");
		for (Node ne : source.getDescendants_NamedTypes())
			cloneProperties(ne);
		tt.visitAllNodes(source);

		// commented some libs out to keep the total time down
		LibraryNode target = lf.loadFile1(mc);
		new LibraryChainNode(target); // Test in a chain
		lf.loadTestGroupA(mc);

		lf.cleanModel();
		Node.getModelNode().visitAllNodes(tt.new TestNode());

		LOGGER.debug("\n");
		LOGGER.debug("Testing cloning to new library.");
		for (LibraryNode ln : Node.getAllLibraries()) {
			if (ln.getNamespace().equals(target.getNamespace()))
				continue;
			if (ln.isBuiltIn())
				continue; // these have errors
			ln.setEditable(true);
			cloneMembers(ln, target);
			LOGGER.debug("Cloned members of " + ln);
		}
		LOGGER.debug("Done cloning - starting final check.");
		Node.getModelNode().visitAllNodes(tt.new TestNode());
	}

	private int cloneMembers(LibraryNode ln, LibraryNode target) {
		int mbrCount = 0, equCount = 0;
		Node clone;

		for (Node n : ln.getDescendants_NamedTypes()) {
			// Assert.assertNotNull(n.cloneNew(null)); // no library, so it will fail node tests
			equCount = countEquivelents(n);
			if (n.isService())
				continue;
			if (ln == target)
				clone = n.clone("_COPY");
			else
				clone = n.clone(target, null);
			if (clone != null) {
				tt.visitAllNodes(clone);
				if (countEquivelents(clone) != equCount)
					LOGGER.debug("Equ error on " + clone);
			}
			mbrCount++;
		}
		return mbrCount;
	}

	private int countEquivelents(Node n) {
		for (Node p : n.getDescendants()) {
			if (p instanceof ElementNode) {
				return ((TLProperty) p.getTLModelObject()).getEquivalents().size();
			}
		}
		return 0;
	}

	private void cloneProperties(Node n) {
		if (n.isNamedType())
			for (Node p : n.getDescendants()) {
				if (p instanceof PropertyNode) {
					if (p.getParent() instanceof ComponentNode)
						((ComponentNode) p.getParent()).addProperty(p.clone());
					else
						LOGGER.debug(p + "has invalid class of parent.");
				}
			}
		tt.visitAllNodes(n);
	}

}

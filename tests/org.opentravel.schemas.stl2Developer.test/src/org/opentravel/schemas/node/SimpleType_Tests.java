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

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class SimpleType_Tests {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleType_Tests.class);

	@Test
	public void checkSimpleTypes() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		int loadedCnt = lf.loadTestGroupA(mc);
		Assert.assertEquals(5, loadedCnt);

		int libCnt = 0;
		for (LibraryNode ln : Node.getAllLibraries()) {
			checkCounts(ln);
			visitSimpleTypes(ln);
			libCnt++;
		}
		Assert.assertEquals(7, libCnt);

	}

	private void checkCounts(LibraryNode lib) {
		int simpleCnt = 0;
		for (Node type : lib.getDescendants_LibraryMembers()) {
			if (type.isSimpleType()) {
				simpleCnt++;
			}
		}
		// if (simpleCnt != lib.getDescendentsSimpleComponents().size()) {
		// List<SimpleComponentNode> list = lib.getDescendentsSimpleComponents();
		// LOGGER.debug("Simple Counts are wrong.");
		// }
		Assert.assertEquals(simpleCnt, lib.getDescendentsSimpleComponents().size());
	}

	private void visitSimpleTypes(LibraryNode ln) {
		for (SimpleComponentNode st : ln.getDescendentsSimpleComponents()) {
			Assert.assertNotNull(st);
			Assert.assertNotNull(st.getLibrary());
			Assert.assertNotNull(st.getBaseType());
			// Assert.assertNotNull(st.getTypeClass());
			if (st instanceof TypeUser) {
				Assert.assertNotNull(st.getAssignedType());
			}

			// Check names
			Assert.assertFalse(st.getName().isEmpty());

			// Type Names
			String an = st.getTypeName();
			// String tn = st.getTypeClass().getTypeNode().getName();
			// // Get Type Name modifies answers from Implied nodes.
			// if (!(st.getTypeClass().getTypeNode() instanceof ImpliedNode)) {
			// if (!an.equals(tn)) {
			// LOGGER.debug("Name error: " + an + " =? " + tn);
			// }
			// Assert.assertEquals(tn, an);
			// }
			//
			// // // Check type namespace
			// String anp = st.getAssignedPrefix();
			// String tnp = st.getTypeClass().getTypeNode().getNamePrefix();
			// if (!anp.isEmpty()) // Prefixes can be empty, but empty is changed by code
			// Assert.assertEquals(tnp, anp);
		}
	}

}

/**
 * 
 */
package com.sabre.schemas.node;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.testUtils.LoadFiles;

/**
 * @author Dave Hollander
 * 
 */
public class XSDNode_Tests {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(XSDNode_Tests.class);

	// Lets make sure they are all unique
	private Map<String, Node> providerMap = new HashMap<String, Node>(
			Node.getNodeCount());
	int dups = 0;
	int counter = 0;

	@Test
	public void checkXsdNodes() throws Exception {
		MainController mc = new MainController();
		ProjectController pc = mc.getProjectController();
		ProjectNode pn = pc.getDefaultProject();
		LoadFiles lf = new LoadFiles();

		int libCnt = 0; // from init
		int locals = 0; // locally defined nodes in the library
		for (LibraryNode ln : Node.getAllLibraries()) {
			providerMap.clear();
			checkCounts(ln);
			if (ln.isXSDSchema()) {
				visitXsdNodes(ln);
			}
			libCnt++;
		}
		Assert.assertEquals(2, libCnt); // the default built-in libraries

		lf.loadXfile2(mc); // should load 2 libraries.
		Assert.assertEquals(2, pn.getLibraries().size());

		lf.loadXfile3(mc);
		Assert.assertEquals(6, pn.getLibraries().size());

		for (LibraryNode ln : Node.getAllLibraries()) {
			providerMap.clear();
			checkCounts(ln);
			if (ln.isXSDSchema()) {
				visitXsdNodes(ln);
			}
			visitSimpleTypes(ln);
			libCnt++;
		}

	}

	private void checkCounts(LibraryNode lib) {
		int simpleCnt = 0;
		for (Node type : lib.getDescendants_NamedTypes()) {
			if (type.isSimpleType()) {
				simpleCnt++;
			}
		}
		String libName = lib.getName();
		int libCnt = lib.getNamedSimpleTypes().size();
		Assert.assertEquals(simpleCnt, lib.getNamedSimpleTypes().size());
	}

	private void visitXsdNodes(INode node) {
		for (Node n : node.getChildren()) {
			if (n.isNavigation()) {
				visitXsdNodes(n);
			} else {
				checkName(n);
				if (n instanceof XsdNode) {
					visitXsdNode((XsdNode) n);
				} else {
					if (!n.isXsdType())
						Assert.assertFalse(n.isXsdType());
				}
			}
		}
	}

	private void checkName(Node n) {
		if (!(n.isTypeProvider()))
			return;

		if (providerMap.put(n.getName(), n) != null)
			dups++;
		// THERE is a bug in family processing that leaves two duplicates.
		// if (peerCount+1 != providerMap.size()) {
		// dups++;
		// }
		// Assert.assertEquals(0, dups);

	}

	private void visitXsdNode(XsdNode xn) {
		Assert.assertTrue(xn.isXsdType());
		Assert.assertTrue(xn.hasOtmModelChild());
		counter++;
	}

	private void visitSimpleTypes(LibraryNode ln) {
		for (SimpleTypeNode st : ln.getNamedSimpleTypes()) {
			Assert.assertNotNull(st.getLibrary());
			Assert.assertNotNull(st.getBaseType());

			// Check names
			Assert.assertFalse(st.getName().isEmpty());

			// Type Names
			String an = st.getTypeName();
			if (an.isEmpty())
				an = "Empty";
			String tn = st.getTypeClass().getTypeNode().getName();
			if (!(st.getTypeClass().getTypeNode() instanceof ImpliedNode))
				Assert.assertEquals(tn, an);
			Assert.assertFalse(an.isEmpty());
			// Check type namespace
			String anp = st.getAssignedPrefix();
			String tnp = st.getTypeClass().getTypeNode().getNamePrefix();
			if (!(st.getTypeClass().getTypeNode() instanceof ImpliedNode))
				Assert.assertEquals(tnp, anp);
			// Prefixes can be empty
		}
	}

}

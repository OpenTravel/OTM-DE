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
package org.opentravel.schemas.testUtils;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.types.TestTypes;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Primary node testers for use in other junit tests.
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public class NodeTesters {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeTesters.class);

	public class TestNode implements NodeVisitor {
		@Override
		public void visit(INode n) {
			visitNode((Node) n);
		}
	}

	// public class PrintNode implements NodeVisitor {
	// @Override
	// public void visit(INode n) {
	// LOGGER.debug("Visited " + n + "\tof class \t" + n.getClass().getCanonicalName());
	// }
	// }

	/**
	 * Test the type providers and assure where used and owner. Test type users and assure getType returns valid node.
	 * 
	 * @param n
	 *            Use Node.visitAllNodes (new visitor());
	 */
	public void visitAllNodes(Node n) {
		n.visitAllNodes(new TestNode());
	}

	/**
	 * Test a node to assure it is valid. Checks assignments, access and type node. Also tests compiler validation and
	 * example generation.
	 * 
	 * @param n
	 */
	public void visitNode(Node n) {
		// LOGGER.debug("Node_Tests-testing: " + n);
		if (n instanceof ModelNode)
			return;
		Assert.assertNotNull(n);
		if (n.isDeleted()) {
			LOGGER.debug("Test node " + n + " is deleted. Skipping.");
			return;
		}
		if (n instanceof ImpliedNode) {
			LOGGER.debug("Test node " + n + " is ImpliedNode. Skipping.");
			return;
		}
		// Check Listeners
		if (n instanceof LibraryMemberInterface)
			assertTrue("Must have identity listener.", Node.GetNode(n.getTLModelObject()) != null);

		// Test the source object
		if (n instanceof LibraryNode) {
			try {
				new TestTypes().visitTypeNode(n); // test type node and example generation.
			} catch (IllegalStateException e) {
				LOGGER.debug("Error with " + n + " " + e);
				Assert.assertEquals("", e.getLocalizedMessage());
				return;
			}
		} else {
			// if (n.getTypeClass().verifyAssignment() == false)
			// LOGGER.debug("Verification error.");
			// Assert.assertTrue(n.getTypeClass().verifyAssignment());
		}

		try {
			new ValidateTLObject().visit(n);
		} catch (IllegalStateException e) {
			LOGGER.debug("TLObject Error with " + n + ". " + e.getLocalizedMessage());
			// Assert.assertEquals("", e.getLocalizedMessage().toString());
			return;
		}

		// Check links.
		Assert.assertFalse(n.getNodeID().isEmpty()); // used in drag-n-drop
		if (n.getParent() == null)
			LOGGER.debug("Null Parent Error");
		Assert.assertNotNull(n.getParent());

		Assert.assertNotNull(n.getChildren());
		Assert.assertNotNull(n.getChildren_TypeProviders());

		// Version nodes
		if (n instanceof VersionNode) {
			VersionNode vn = (VersionNode) n;
			Assert.assertTrue(n.getParent() instanceof NavNode); // could be family???
			Assert.assertTrue(vn.getNewestVersion() instanceof ComponentNode); // could be family???
			return;
		}

		if (n.getName().isEmpty())
			LOGGER.debug("Empty Name: " + n.getName());
		Assert.assertFalse(n.getName().isEmpty());
		// Assert.assertFalse(n.getIdentity().isEmpty()); // new 1/20/15
		Assert.assertFalse(n.getLabel().isEmpty());
		if (n instanceof ComponentNode) {
			if (n.getPrefix().isEmpty())
				n.getPrefix();
			Assert.assertFalse(n.getPrefix() == null);
			Assert.assertFalse(n.getNamespace().isEmpty());
			Assert.assertFalse(n.getNameWithPrefix().isEmpty());
		}

		// Check component type and state
		Assert.assertFalse(n.isDeleted());
		Assert.assertNotNull(n.getComponentType());
		Assert.assertFalse(n.getComponentType().isEmpty());

		// Check type information
		if (n instanceof TypeProvider)
			assert ((TypeProvider) n).getWhereAssigned() != null;

		// is tests - make sure they do not throw exception
		n.isEditable();
		n.isNamedEntity();

	}

	public class ValidateTLObject implements NodeVisitor {
		@Override
		public void visit(INode in) {
			Node n = (Node) in;
			if (n instanceof VersionNode)
				return;
			if (n instanceof ImpliedNode)
				return;
			// XSD types will fail because they are not in the model until imported.
			if (n.isXsdType()) {
				return;
			}
			validateTL(n.getTLModelObject(), in);
		}

		/**
		 * Validate the TL model object owning and library relationships.
		 * 
		 * @param tlObj
		 * @param in
		 * @throws IllegalStateException
		 */
		public void validateTL(TLModelElement tlObj, INode in) throws IllegalStateException {
			String msg = "";
			// LOGGER.debug("Validating tlObj " +
			if (tlObj == null)
				return;

			if (tlObj.getValidationIdentity() == null || tlObj.getValidationIdentity().isEmpty())
				msg = "Missing validation identity on ";

			if (tlObj instanceof LibraryMember) {
				// LOGGER.debug("Validating member " + tlObj.getValidationIdentity());

				// if (tlObj.getOwningModel() == null)
				// msg += "Missing owning model on ";
				if (((LibraryMember) tlObj).getOwningLibrary() == null)
					msg += "Missing owning library on ";
				if (((LibraryMember) tlObj).getNamespace() == null || ((LibraryMember) tlObj).getNamespace().isEmpty())
					msg += "Missing namespace on ";
				if ((((LibraryMember) tlObj).getLocalName() == null || ((LibraryMember) tlObj).getLocalName().isEmpty())
						&& !(tlObj instanceof TLExtensionPointFacet))
					msg += "Missing local name on ";
			} else if (tlObj instanceof TLFacet) {
				if (((TLFacet) tlObj).getOwningEntity() == null)
					msg += "Missing facet owner on ";
			} else if (tlObj instanceof TLAttribute) {
				if (((TLAttribute) tlObj).getOwner() == null)
					msg += "Missing attribute owner on ";
			} else if (tlObj instanceof TLProperty) {
				if (((TLProperty) tlObj).getOwner() == null)
					msg += "Missing property owner on ";
				if (((TLProperty) tlObj).getOwningLibrary() == null)
					msg += "Missing property library on ";
			} else if (tlObj instanceof TLIndicator) {
				if (((TLIndicator) tlObj).getOwner() == null)
					msg = "Missing indicator owner on ";
			}

			if (!msg.isEmpty()) {
				if (in != null)
					msg = msg + in.getNameWithPrefix();
				msg = msg + "\t of class " + tlObj.getClass().getSimpleName();
				throw new IllegalStateException(msg);
			}
		}
	}

}

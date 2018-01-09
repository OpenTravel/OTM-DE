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
package org.opentravel.schemas.node;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Only used by XsdObjectHandler to create nodes to represent the xsd object. Is discarded after the xsd object is
 * modeled.
 * 
 * // * The XsdNode class extends componentNode with the addition of lazy evaluated modeled nodes (otmModel), mo and tl
 * // * objects with access methods. // *
 * 
 * @author Dave Hollander
 * 
 */
public class XsdNode extends ComponentNode {

	private static final Logger LOGGER = LoggerFactory.getLogger(XsdNode.class);
	public ComponentNode otmModel = null; // a pointer to a node/model object and tlObj
											// representing the xsd type
	LibraryNode lib = null;

	/**
	 * Temporary Patch just until handler working
	 */
	public XsdNode(LibraryNode lib) {
		this.lib = lib;
	}

	// /**
	// * Create an XsdNode to represent and XSD Simple or Complex type. Creates an XsdNode with model object, sets name
	// * and description, links TL library member to MO
	// *
	// * @param obj
	// * - the TL XSDComplexType or XSDSimpleType
	// */
	// @Deprecated
	// public XsdNode(final TLLibraryMember obj, LibraryNode lib) {
	// super(obj); //
	// this.setLibrary(lib);
	// // Build all of the tl models now so they and their local types get rendered in the tree
	// this.createTLModelChild();
	//
	// // fix listener to point to otmModel instead the this
	// Collection<ModelElementListener> toRemove = new ArrayList<ModelElementListener>();
	// for (ModelElementListener l : getTLModelObject().getListeners())
	// if (l instanceof NodeIdentityListener)
	// toRemove.add(l);
	// for (ModelElementListener l : toRemove)
	// getTLModelObject().removeListener(l);
	// NamedTypeListener listener = new NamedTypeListener(getOtmModel());
	// getTLModelObject().addListener(listener);
	// }

	// /**
	// * Utility function - use getOtmModelChild() which will create one if it did not exist.
	// *
	// * @return
	// */
	// @Deprecated
	// protected boolean hasOtmModelChild() {
	// return otmModel == null ? false : true;
	// }

	// /**
	// *
	// * @return the otmModel node that represents this xsd node
	// */
	// @Deprecated
	// public ComponentNode getOtmModel() {
	// return otmModel == null ? createTLModelChild() : otmModel;
	// }

	// /**
	// * Return the TL model Rendered child of this xsd node. If one does not exist, it tries to create one. The new
	// node
	// * is a member of the generated library and <b>not</b> part of the TLModel. They can not be or else there will be
	// * name collisions.
	// *
	// * @return
	// */
	// @Deprecated
	// private ComponentNode createTLModelChild() {
	// // LOGGER.debug("Creating TLModel Child for node " +
	// // this.getNameWithPrefix()+" in namespace "+ this.getNamespace());
	// if (this.getLibrary() == null)
	// LOGGER.error("Can not create a TL Model child without a library!. " + this.getName());
	//
	// // Use this model object to build a TL_Object and use that to create a node.
	// ComponentNode cn = (ComponentNode) NodeFactory.newLibraryMember(modelObject.buildTLModel(this));
	// if (cn != null) {
	// cn.xsdNode = this;
	// otmModel = cn;
	// cn.setLibrary(getLibrary());
	// xsdType = true;
	// setXsdTypeOnChildren(cn);
	// }
	// return cn;
	// }

	// @Deprecated
	// private void setXsdTypeOnChildren(Node n) {
	// n.xsdType = true;
	// for (Node c : n.getChildren())
	// setXsdTypeOnChildren(c);
	// }

	@Override
	public LibraryNode getLibrary() {
		return lib;
	}

	@Deprecated
	@Override
	public String getName() {
		// used when modeling xsd objects
		// assert false; // xsd node should never be maintained
		return "";
		// return modelObject.getName();
	}

	@Deprecated
	@Override
	public boolean isImportable() {
		// if ((isInXSDSchema() || isInBuiltIn()) && getOtmModel() != null
		// && !(getOtmModel().getTLModelObject() instanceof TLEmpty)) {
		// return true;
		// }
		assert false; // xsd node should never be maintained
		return false;
	}

	@Deprecated
	@Override
	public boolean isDeleteable() {
		assert false; // xsd node should never be maintained
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#getTLModelObject()
	 */
	@Override
	public TLModelElement getTLModelObject() {
		return null;
	}

	// @Override
	// public boolean isMissingAssignedType() {
	// // LOGGER.debug("check xsdNode "+getName()+" for missing type");
	// return getOtmModel().isMissingAssignedType();
	// }

	// public boolean isXsdElement() {
	// return (modelObject instanceof XSDElementMO) ? true : false;
	// }

	// // @Override
	// public boolean isCoreObject() {
	// return otmModel instanceof CoreObjectNode;
	// }

	// @Override
	// public boolean isSimpleType() {
	// assert (false);
	// return otmModel.isSimpleType();
	// // If this is never reached then this Node method can become instanceof test
	// }

	// // @Override
	// public boolean isBusinessObject() {
	// return otmModel instanceof BusinessObjectNode;
	// }
	//
	// // @Override
	// public boolean isValueWithAttributes() {
	// return otmModel instanceof VWA_Node;
	// }

	// @Override
	// public boolean isSimpleTypeProvider() {
	// return otmModel instanceof SimpleComponentInterface;
	// }

	// @Override
	// public INode getBaseType() {
	// throw new IllegalAccessError("xsd node getBaseType() is not implemented.");
	// }

	// @Override
	// public NamedEntity getTLOjbect() {
	// return otmModel.getTLModelObject() instanceof NamedEntity ? (NamedEntity) otmModel.getTLModelObject() : null;
	// }

}

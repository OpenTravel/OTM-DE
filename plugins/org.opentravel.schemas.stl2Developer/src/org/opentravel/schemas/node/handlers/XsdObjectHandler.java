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
package org.opentravel.schemas.node.handlers;

import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.XsdNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.NodeIdentityListener;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.utils.XsdModelingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelElement;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * This handler works with TLLibraryMembers: XSDSimpleType, XSDComplexType, and XSDElement
 * 
 * <p>
 * Owner <-> SimpleTypeNode <br>
 * src -> xsdSimple/xsdComplex/xsdElement from compiler with listener pointing to SimpleTypeNode <br>
 * Built -> TLSimple created by the handler, listener set to SimpleTypeNode
 * 
 * @author Dave Hollander
 * 
 */
public class XsdObjectHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(XsdObjectHandler.class);

	public LibraryMemberInterface owner = null;
	private XsdNode xsdNode = null;
	private TLLibraryMember srcTL = null;
	private TLLibraryMember builtTL = null;

	public XsdObjectHandler(final XSDSimpleType obj, LibraryNode lib) {
		xsdNode = new XsdNode(lib); // just needed for modeling utils and creating owner
		srcTL = obj;
		final SimpleType jTLS = obj.getJaxbType();
		// If the simple has no jaxB type, it is probably a built-in type
		String name = obj.getJaxbType() != null ? obj.getJaxbType().getName() : obj.getName();
		builtTL = XsdModelingUtils.buildSimpleObject(jTLS, name, xsdNode);
		getOwner();
	}

	public XsdObjectHandler(final XSDComplexType obj, LibraryNode lib) {
		xsdNode = new XsdNode(lib); // just needed for modeling utils and creating owner
		srcTL = obj;
		final TopLevelComplexType jaxbTLC = obj.getJaxbType();
		builtTL = XsdModelingUtils.buildCoreObject(jaxbTLC, jaxbTLC.getName(), xsdNode);
		builtTL.setOwningLibrary(lib.getTLModelObject());
		getOwner();
	}

	public XsdObjectHandler(final XSDElement obj, LibraryNode lib) {
		xsdNode = new XsdNode(lib); // just needed for modeling utils and creating owner
		srcTL = obj;
		final TopLevelElement jaxbEle = obj.getJaxbElement();
		if (jaxbEle.getComplexType() != null)
			builtTL = XsdModelingUtils.buildCoreObject(jaxbEle.getComplexType(), jaxbEle.getName(), xsdNode);
		else if (jaxbEle.getSimpleType() != null)
			builtTL = XsdModelingUtils.buildSimpleObject(jaxbEle.getSimpleType(), jaxbEle.getName(), xsdNode);
		else
			builtTL = buildElementObject(jaxbEle);
		if (builtTL != null) {
			builtTL.setOwningLibrary(lib.getTLModelObject());
			getOwner();
		}
		// else
		// LOGGER.debug("Unknown xsd element type: " + jaxbEle.getName());
	}

	private TLLibraryMember buildElementObject(TopLevelElement xEle) {
		TLSimple simple = new TLSimple();
		simple.setName(xEle.getName());
		if (xEle.getType() != null) {
			TLDocumentation tlDoc = new TLDocumentation();
			tlDoc.setDescription(xEle.getType().toString());
			simple.setDocumentation(tlDoc);
		}
		return simple;
	}

	public LibraryMemberInterface getOwner() {
		if (builtTL == null)
			return null;
		if (owner != null)
			return owner;

		owner = NodeFactory.newLibraryMember(builtTL);
		if (owner != null) {
			owner.setLibrary(xsdNode.getLibrary());
			// setXsdTypeOnChildren((Node) owner);
			((TypeProvider) owner).setXsdHandler(this);
			srcTL.addListener(new NodeIdentityListener((Node) owner));
			// ((Node) owner).setTlModelObject(srcTL);
			// ListenerFactory.setListner((Node) owner);
		}
		return owner;
	}

	/**
	 * @return the original source TLLibraryMember used to create the model
	 */
	public TLLibraryMember getTLLibraryMember() {
		return srcTL;
	}

	/**
	 * @return the original source TLLibraryMember used to create the model
	 */
	public TLLibraryMember getBuiltTL() {
		return builtTL;
	}

	/**
	 * Get the prefix of the restriction base that this xsd simple type is created from.
	 * 
	 * @return
	 */
	public String getAssignedPrefix() {
		// String prefix = owner.getLibrary().getPrefix();
		String jPrefix = "";
		if (srcTL instanceof XSDSimpleType) {
			TopLevelSimpleType jaxb = ((XSDSimpleType) srcTL).getJaxbType();
			if (jaxb != null && jaxb.getRestriction() != null)
				if (jaxb.getRestriction().getBase() != null)
					if (!jaxb.getRestriction().getBase().getPrefix().isEmpty())
						jPrefix = jaxb.getRestriction().getBase().getPrefix();
		}
		// assert jPrefix.equals(prefix);
		return jPrefix;
	}

	public TypeProvider getRequiredType() {
		return ModelNode.getUndefinedNode();
	}

	// This may be needed for xsd library objects to be imported.
	//
	// @Override
	// public boolean isImportable() {
	// if ((isInXSDSchema() || isInBuiltIn()) && getOtmModel() != null
	// && !(getOtmModel().getModelObject().getTLModelObj() instanceof TLEmpty)) {
	// return true;
	// }
	// return false;
	// }

	/** ------------------------------------------- Old Code ------------------------- */

	/**
	 * Create an XsdNode to represent and XSD Simple or Complex type. Creates an XsdNode with model object, sets name
	 * and description, links TL library member to MO
	 * 
	 * @param obj
	 *            - the TL XSDComplexType or XSDSimpleType
	 */
	// public XsdObjectHandler(final TLLibraryMember obj, LibraryNode lib) {
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
	// protected boolean hasOtmModelChild() {
	// return owner == null ? false : true;
	// }

	// /**
	// *
	// * @return the otmModel node that represents this xsd node
	// */
	// public ComponentNode getOtmModel() {
	// return owner == null ? createTLModelChild() : owner;
	// }

	/**
	 * Return the TL model Rendered child of this xsd node. If one does not exist, it tries to create one. The new node
	 * is a member of the generated library and <b>not</b> part of the TLModel. They can not be or else there will be
	 * name collisions.
	 * 
	 * @return
	 */
	// private ComponentNode createTLModelChild() {
	// // LOGGER.debug("Creating TLModel Child for node " +
	// // this.getNameWithPrefix()+" in namespace "+ this.getNamespace());
	// if (this.getLibrary() == null)
	// LOGGER.error("Can not create a TL Model child without a library!. " + this.getName());
	//
	// // Use this model object to build a TL_Object and use that to create a node.
	// ComponentNode cn = NodeFactory.newComponent_UnTyped((TLLibraryMember) modelObject.buildTLModel(xsdNode));
	// if (cn != null) {
	// // cn.xsdNode = this;
	// owner = cn;
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

	// @Override
	// public String getName() {
	// return "";
	// // return modelObject.getName();
	// }
	//
	//
	// @Override
	// public boolean isDeleteable() {
	// return false;
	// }

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

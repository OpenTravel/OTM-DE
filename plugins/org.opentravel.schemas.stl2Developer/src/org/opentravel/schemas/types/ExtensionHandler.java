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
package org.opentravel.schemas.types;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.types.WhereExtendedHandler.WhereExtendedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles extension assignment. Extensions modify the structure of the base object. Note: ExtensionPointFacet extends
 * facets not ExtensionPointFacets.
 * 
 * @author Dave Hollander
 * 
 */
public class ExtensionHandler extends AbstractAssignmentHandler<ExtensionOwner> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionHandler.class);

	private ExtensionOwner owner = null;

	public ExtensionHandler(ExtensionOwner n) {
		super();
		owner = n;
	}

	@Override
	public Node getOwner() {
		return (Node) owner;
	}

	public Node get() {

		// Get the extension base from the TL Model Element
		NamedEntity tlObj = owner.getModelObject().getTLBase();
		return Node.GetNode((TLModelElement) tlObj);

		// TODO - modelObject only does Business, Choice and Core objects.

		// From VWA
		// TLAttributeType tlParent = ((TLValueWithAttributes) getTLModelObject()).getParentType();
		// Node parent = GetNode(((TLModelElement) tlParent).getListeners());
		// return parent;
		// From closed enum:
		// Lazy evaluation: get it from TL object after all nodes generated with listeners.
		// Node nType = ModelNode.getDefaultStringNode(); // Set base type.
		// if (((TLAbstractEnumeration) getTLModelObject()).getExtension() != null) {
		// NamedEntity eType = ((TLAbstractEnumeration) getTLModelObject()).getExtension().getExtendsEntity();
		// nType = GetNode(((TLModelElement) eType).getListeners());
		// }
		// if (getTypeClass().getTypeNode() != nType)
		// getTypeClass().setTypeNode(nType);
		// return (Node) getTypeClass().getTypeNode();

		// From enum:
		// Lazy evaluation: get it from TL object after all nodes generated with listeners.
		// Node nType = ModelNode.getDefaultStringNode(); // Set base type.
		// if (((TLAbstractEnumeration) getTLModelObject()).getExtension() != null) {
		// NamedEntity eType = ((TLAbstractEnumeration) getTLModelObject()).getExtension().getExtendsEntity();
		// nType = GetNode(((TLModelElement) eType).getListeners());
		// }
		// if (getTypeClass().getTypeNode() != nType)
		// getTypeClass().setTypeNode(nType);
		// return (Node) getTypeClass().getTypeNode();
		// // base type might not have been loaded when constructor was called. check the tl model not the type node.
		// return getExtensionNode();

		// From choice
		// if (getTLModelObject() instanceof TLExtensionOwner
		// && ((TLExtensionOwner) getTLModelObject()).getExtension() != null) {
		// NamedEntity tlBase = ((TLExtensionOwner) getTLModelObject()).getExtension().getExtendsEntity();
		// if (tlBase instanceof TLModelElement)
		// baseClass = GetNode(((TLModelElement) tlBase).getListeners());
		// // for (ModelElementListener listener : ((TLModelElement) tlBase).getListeners())
		// // if (listener instanceof BaseNodeListener)
		// // baseClass = ((BaseNodeListener) listener).getNode();
		// }
		// return baseClass;
	}

	/**
	 * Set the base type for owner node to the passed base object such that <i>this extends base</i>.
	 * 
	 * A WhereExtended listener will be added to the base before being set. The listener will add this node to the
	 * base's where extended list.
	 * 
	 * If null, remove assignment.
	 * 
	 * Note: extension point facets extend facets not themselves.
	 * 
	 * @param base
	 */
	public boolean set(Node base) {
		if (base == null)
			return remove();

		LOGGER.debug("Start - set extension base of " + owner.getNameWithPrefix() + " to " + base.getNameWithPrefix());

		// Save the old base object for after the assignment
		Node oldBase = owner.getExtensionBase();
		// If owner was extending a different base, remove the link and listener.
		if (oldBase != null) {
			oldBase.getWhereExtendedHandler().removeListener(owner);
			oldBase.getWhereExtendedHandler().remove((Node) owner);
			owner.getModelObject().setExtendsType(null); // allow ownership event to happen
		}
		// Add a where extended listener to the new base before making the assignment
		base.getWhereExtendedHandler().setListener(owner);

		// Do the assignment
		owner.getModelObject().setExtendsType(base.getModelObject());

		// update library where used

		if (oldBase == null)
			LOGGER.debug("END -" + owner + " changed assigment from null to " + base);
		else
			LOGGER.debug("END -" + owner + " changed assigment from " + oldBase.getNameWithPrefix() + " to "
					+ base.getNameWithPrefix());

		// /////////////////////////////////////////////////////
		// Add handler listener
		// Node oldValue = null;
		// if (base instanceof ExtensionOwner)
		// oldValue = ((ExtensionOwner) base).getExtendsType();
		//
		// whereExtendedHandler.setListener(base);
		//
		//
		// // remove where used listener from old assigned type
		// if (oldValue != target)
		// ((TypeProvider) oldValue).removeListener(owner);

		// update library where used
		// if (oldValue.getLibrary() != base.getLibrary()) {
		// if (base.getLibrary() != null)
		// base.getLibrary().getWhereUsedHandler().add(owner);
		// if (oldValue.getLibrary() != null)
		// oldValue.getLibrary().getWhereUsedHandler().remove(owner);
		// }

		// if (!(owner instanceof ExtensionOwner))
		// sourceNode = ModelNode.getUnassignedNode();
		// if ((sourceNode == null) || (!sourceNode.isTypeProvider()))
		// sourceNode = ModelNode.getUnassignedNode();

		// From VWA:
		// update TLModel
		// super.setExtendsType(sourceNode);
		// // make changes to node model
		// setSimpleType((Node) sourceNode);

		// FIXME
		// LOGGER.error("Set on extension handler not implemented yet.");
		// setBaseType((Node) sourceNode);
		// Unlink if base type is already set.
		// if ((typeNode.getTypeClass().baseUsers != null))
		// typeNode.getTypeClass().baseUsers.remove(typeOwner);
		//
		// // Add this owner to the sources base users list
		// if (!sourceNode.getTypeClass().baseUsers.contains(typeOwner))
		// sourceNode.getTypeClass().baseUsers.add(typeOwner);
		// // TESTME - this used to also add to the typeUsers array
		//
		// typeNode = sourceNode;

		// Set the TL model if TLExtension owner or else set to null (clear)
		// Note: VWAs are not members of TLExtensionOnwer and must have the parent type set instead.
		// if (!(sourceNode instanceof ImpliedNode))
		// if (owner.getTLModelObject() instanceof TLExtensionOwner)
		// owner.getModelObject().setExtendsType(sourceNode.getModelObject());
		// else if (owner instanceof VWA_Node)
		// ((VWA_Node) owner).setAssignedType((TypeProvider) sourceNode);
		// else
		// owner.getModelObject().setExtendsType(null);

		// LOGGER.debug("Set base type of " + owner + " to " + base);
		return false;
	}

	/**
	 * Remove the extension
	 * 
	 * @return
	 */
	private boolean remove() {
		// Let the model object clear it out.
		owner.getModelObject().setExtendsType(null);

		removeListener();
		// // Remove the listener from the old base type (if any)
		// if (owner.getExtensionBase() != null)
		// owner.getExtensionBase().getWhereExtendedHandler().removeListener(owner);

		return true;
	}

	public void removeListener() {
		// Remove the listener identifying the base type
		for (ModelElementListener l : ((Node) owner).getTLModelObject().getListeners())
			if (l instanceof WhereExtendedListener) {
				owner.getTLModelObject().removeListener(l);
				return;
			}

		// if (owner.getExtensionBase() != null)
		// owner.getExtensionBase().getWhereExtendedHandler().removeListener(owner);

	}

	@Override
	public TLModelElement getTLModelElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NamedEntity getTLNamedEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	// Not needed - just use set
	// public void replace(Node replacement, LibraryNode scope) {
	// // Get where this owne is used as the extension and replace with replacement
	// Node owner = get();
	// if (owner instanceof ExtensionOwner)
	// ((ExtensionOwner) owner).setExtension(replacement);
	// }
}

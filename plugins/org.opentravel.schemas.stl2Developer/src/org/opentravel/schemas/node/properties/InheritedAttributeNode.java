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
package org.opentravel.schemas.node.properties;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUserHandler;

/**
 * A property node that represents an inherited attribute. It is simply a facade for the underlying attribute from which
 * it was created.
 * <p>
 * The node is typically created from the node associated with a TL Attribute retrieved using
 * {@link PropertyCodegenUtils#getInheritedAttributes(org.opentravel.schemacompiler.model.TLFacet)}. It is a separate
 * node to allow it to be displayed uniquely in navigator trees.
 * 
 * @author Dave Hollander
 * 
 */
public class InheritedAttributeNode extends AttributeNode implements InheritedInterface, FacadeInterface {

	private AttributeNode inheritedFrom = null;

	public InheritedAttributeNode(AttributeNode from, FacetInterface parent) {
		super();
		inheritedFrom = from;
		this.parent = (Node) parent;
	}

	@Override
	public boolean canAssign(Node type) {
		return false;
	}

	@Override
	public TypeProvider getAssignedType() {
		return getInheritedFrom().getAssignedType();
	}

	// @Override
	// public ComponentNodeType getComponentNodeType() {
	// return ComponentNodeType.ATTRIBUTE;
	// }

	// @Override
	// public IValueWithContextHandler getEquivalentHandler() {
	// if (equivalentHandler == null)
	// equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
	// return equivalentHandler;
	// }
	//
	// @Override
	// public IValueWithContextHandler getExampleHandler() {
	// if (exampleHandler == null)
	// exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
	// return exampleHandler;
	// }

	// @Override
	// public Image getImage() {
	// return Images.getImageRegistry().get(Images.XSDAttribute);
	// }

	@Override
	public String getLabel() {
		// String label = getName();
		// if (getAssignedType() != null)
		// label += " [" + getTypeNameWithPrefix() + "]";
		return getInheritedFrom().getLabel();
	}

	@Override
	public String getName() {
		return getInheritedFrom().getName();
	}

	// @Override
	// public Node getParent() {
	// return parent;
	// }

	@Override
	public TLAttribute getTLModelObject() {
		return getInheritedFrom().getTLModelObject();
	}

	/**
	 * @return the typeHandler
	 */
	@Override
	public TypeUserHandler getTypeHandler() {
		return getInheritedFrom().getTypeHandler();
	}

	// @Override
	// public int indexOfTLProperty() {
	// return getTLModelObject() != null ? getTLModelObject().getOwner().getAttributes().indexOf(getTLModelObject())
	// : 0;
	// }

	// @Override
	// public boolean isMandatory() {
	// return getTLModelObject() != null ? getTLModelObject().isMandatory() : false;
	// }

	// @Override
	// public boolean isOnlySimpleTypeUser() {
	// // allow VWAs to be assigned to VWA Attributes.
	// return parent != null && parent instanceof AttributeFacetNode ? false : true;
	// }

	// @Override
	// public boolean isRenameable() {
	// return isEditable() && !isInherited();
	// }

	/**
	 * Override to provide GUI assist: Since attributes can be renamed, there is no need to use the alias. Aliases are
	 * not TLAttributeType members so the GUI assist must convert before assignment.
	 */
	@Override
	public boolean setAssignedType(TypeProvider provider) {
		// if (provider instanceof AliasNode)
		// provider = (TypeProvider) ((Node) provider).getOwningComponent();
		// return typeHandler.set(provider);
		return false;
	}

	@Override
	public IValueWithContextHandler setEquivalent(String example) {
		getEquivalentHandler().set(example, null);
		return getInheritedFrom().getEquivalentHandler();
	}

	@Override
	public IValueWithContextHandler setExample(String example) {
		getExampleHandler().set(example, null);
		return getInheritedFrom().getExampleHandler();
	}

	// /**
	// * Allowed in major versions and on objects new in a minor.
	// */
	// @Override
	// public void setMandatory(final boolean selection) {
	// if (getTLModelObject() != null)
	// if (isEditable_newToChain())
	// if (getOwningComponent().isNewToChain() || !getLibrary().isInChain())
	// getTLModelObject().setMandatory(selection);
	// }
	//
	// @Override
	// public void setName(String name) {
	// if (getTLModelObject() != null)
	// getTLModelObject().setName(NodeNameUtils.fixAttributeName(name));
	// }

	// @Override
	// protected void moveDownTL() {
	// if (getTLModelObject() != null)
	// getTLModelObject().moveDown();
	// }
	//
	// @Override
	// protected void moveUpTL() {
	// if (getTLModelObject() != null)
	// getTLModelObject().moveUp();
	// }

	// @Override
	// protected void removeFromTL() {
	// if (getParent() != null && getParent().getTLModelObject() instanceof TLAttributeOwner)
	// ((TLAttributeOwner) getParent().getTLModelObject()).removeAttribute(getTLModelObject());
	// }
	//
	@Override
	public boolean setAssignedTLType(TLModelElement tla) {
		// if (tla == getTLModelObject().getType())
		// return false;
		// if (tla instanceof TLAttributeType)
		// getTLModelObject().setType((TLAttributeType) tla);
		// return getTLModelObject().getType() == tla;
		return false;
	}

	@Override
	public AttributeNode getInheritedFrom() {
		return inheritedFrom;
	}

	@Override
	public AttributeNode get() {
		return getInheritedFrom();
	}
}

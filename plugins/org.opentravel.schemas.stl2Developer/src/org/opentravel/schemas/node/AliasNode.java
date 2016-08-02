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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aliases are displayed as properties but are assignable as type references. They provide an alternate name for their
 * parentNode facet or business object
 * 
 * @author Dave Hollander
 * 
 */
public class AliasNode extends TypeProviderBase implements TypeProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(AliasNode.class);

	// public AliasNode() {
	// super();
	// }

	public AliasNode(final TLModelElement obj) {
		super(obj);
	}

	/**
	 * Add a new alias to a core or business object parent.
	 * 
	 * @param parent
	 * @param tlObj
	 */
	public AliasNode(final Node parent, final TLAlias tlObj) {
		this(tlObj);
		if (parent != null) {
			parent.linkChild(this);
			setLibrary(parent.getLibrary());
			if (parent instanceof BusinessObjectNode || parent instanceof CoreObjectNode) {
				parent.getModelObject().addAlias(tlObj);
				createChildrenAliases(parent, tlObj);
			}
		}
	}

	/**
	 * Create a new alias complete with new TL model and link to parentNode
	 * 
	 * @param parentNode
	 * @param en
	 */
	public AliasNode(final Node parent, final String name) {
		// Do not use the parent form of this constructor. The alias must be named before children are created.
		this(new TLAlias());
		setName(name);
		((TLAlias) getTLModelObject()).setName(name);
		parent.linkChild(this); // link without doing family tests.
		setLibrary(parent.getLibrary());
		if (parent instanceof BusinessObjectNode || parent instanceof CoreObjectNode) {
			parent.getModelObject().addAlias((TLAlias) getTLModelObject());
			createChildrenAliases(parent, (TLAlias) getTLModelObject());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#delete()
	 */
	@Override
	public void delete() {
		deleteAliasList visitor = new deleteAliasList();
		touchSiblingAliases(visitor);
		for (AliasNode n : visitor.getToBeDeleted())
			n.superDelete();
	}

	private void superDelete() {
		super.delete();
	}

	private void createChildrenAliases(Node owner, TLAlias tla) {
		if (owner == null)
			return;

		for (Node n : owner.getChildren()) {
			if (n instanceof FacetNode)
				((FacetNode) n).updateAliasNodes();
		}
	}

	@Override
	public Node getOwningComponent() {
		return getParent() != null ? getParent().getOwningComponent() : this;
	}

	@Override
	public String getPropertyRole() {
		return "Alias";
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Alias);
	}

	@Override
	public boolean isFacetAlias() {
		final Object model = modelObject.getTLModelObj();
		if (model instanceof TLAlias) {
			final TLAliasOwner owner = ((TLAlias) model).getOwningEntity();
			if (owner == null || owner instanceof TLAbstractFacet) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAssignable() {
		return getParent() != null ? getParent().isAssignable() : false;
	}

	@Override
	public boolean isAssignedByReference() {
		return getParent() != null ? getParent().isAssignedByReference() : false;
	}

	@Override
	public boolean isSimpleAssignable() {
		return getParent() != null ? getParent().isSimpleAssignable() : false;
	}

	@Override
	public String getComponentType() {
		return "Alias: " + getName();
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ALIAS;
	}

	@Override
	public List<Node> getNavChildren() {
		return null;
	}

	@Override
	public boolean hasNavChildren() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#setName(java.lang.String)
	 */
	@Override
	public void setName(String n) {
		if (getParent() == null)
			return; // happens during initial node creation
		if (!(getParent() instanceof ComplexComponentInterface)) {
			// LOGGER.warn("Can't set name unless the parent is the component, not a facet. " + getName());
			return;
		}
		n = NodeNameUtils.fixComplexTypeName(n);
		touchSiblingAliases(new renameAlias(this, n));
	}

	// Why is this here? I think it will always give same result as getOwningComponent(). Only used
	// for delete and setName.
	private ComponentNode findOwningComponent() {
		ComponentNode component = (ComponentNode) getParent();
		if (component instanceof ComplexComponentInterface)
			return component;

		component = (ComponentNode) getParent().getParent();
		if (component instanceof ComplexComponentInterface)
			return component;
		else
			return null;
	}

	/**
	 * Execute visitor on this node and all others with the same name root under the parent component.
	 * 
	 * @param visitor
	 * @param a
	 */
	private void touchSiblingAliases(Visitor visitor) {
		// find parent component.
		String thisAlias = getName();
		Node component = findOwningComponent();
		List<Node> peers = findOwningComponent().getDescendants();
		String rootAlias = "";
		for (Node node : peers) {
			if (node instanceof AliasNode && node.getParent() == findOwningComponent()) {
				if (thisAlias.startsWith(node.getName())) {
					rootAlias = node.getName();
				}
			}
		}
		visitor.visit(this);
		for (Node peer : peers) {
			if (peer instanceof AliasNode && peer != this) {
				if (peer.getName().startsWith(rootAlias)) {
					visitor.visit(peer);
				}
			}
		}
	}

	private interface Visitor {
		public void visit(Node n);
	}

	/**
	 * Visitor class that creates list of aliases.
	 * 
	 * @author Dave Hollander
	 * 
	 */
	private class deleteAliasList implements Visitor {
		List<AliasNode> toBeDeleted = new ArrayList<AliasNode>();

		/**
		 * @return the toBeDeleted
		 */
		public List<AliasNode> getToBeDeleted() {
			return toBeDeleted;
		}

		@Override
		public void visit(Node n) {
			if (n instanceof AliasNode)
				toBeDeleted.add((AliasNode) n);
		}

	}

	private class renameAlias implements Visitor {
		String newName = "";
		String aliasName = "";

		public renameAlias(AliasNode leadAlias, String newName) {
			this.newName = newName;
			this.aliasName = leadAlias.getName();
		}

		@Override
		public void visit(Node n) {
			if (n.getModelObject() == null)
				throw new IllegalStateException("Model Object on " + getName() + " is null.");

			String remainder = n.getName().substring(aliasName.length());
			String fullNewName = newName;
			if (remainder.length() > 0)
				fullNewName = newName + remainder;
			n.getModelObject().setName(fullNewName);
			if (n instanceof TypeProvider)
				for (TypeUser user : ((TypeProvider) n).getWhereAssigned())
					user.setName(fullNewName);
		}
	}

	@Override
	public boolean isAssignableToSimple() {
		return false;
	}

	@Override
	public boolean isAssignableToVWA() {
		return false;
	}

	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

}

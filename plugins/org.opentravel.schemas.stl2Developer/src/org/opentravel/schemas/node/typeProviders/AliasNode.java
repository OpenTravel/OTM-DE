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
package org.opentravel.schemas.node.typeProviders;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
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
public class AliasNode extends TypeProviders {
	private static final Logger LOGGER = LoggerFactory.getLogger(AliasNode.class);

	private AliasOwner owner = null;

	/**
	 * Create a new alias complete with new TL model and AliasOwner parent.
	 * 
	 * @param parentNode
	 * @param en
	 */
	public AliasNode(final AliasOwner parent, String name) {
		this((Node) parent, new TLAlias());
		setName(name);
	}

	/**
	 * Create an alias node to represent a TLAlias. If parent is an AliasOwner it will be added to the parent.
	 * <p>
	 * If parent is a non-AliasOwner the owner will be set allowing the children handler to create nodes for facets and
	 * other non-owning nodes.
	 * 
	 * @param parentNode
	 * @param en
	 */
	public AliasNode(final Node parent, TLAlias tlObj) {
		super(tlObj);
		// assure the name is not null to prevent NPE in code gen utils
		if (tlObj.getName() == null)
			tlObj.setName("");
		this.parent = parent;

		if (parent instanceof AliasOwner) {
			owner = (AliasOwner) parent;
			owner.addAlias(this);
		} else if (parent.getOwningComponent() instanceof AliasOwner)
			owner = (AliasOwner) parent.getOwningComponent();

		assert (getTLModelObject() != null);
		assert (Node.GetNode(getTLModelObject()) == this);
	}

	@Override
	public void delete() {
		if (parent instanceof AliasOwner)
			((AliasOwner) parent).remove(this);
		deleted = true;
		parent = null;
	}

	@Override
	public void deleteTL() {
		// final TLAliasOwner owningEntity = getTLModelObject().getOwningEntity();
		// if (owningEntity != null && !(owningEntity instanceof TLFacet) &&
		// !(owningEntity instanceof TLListFacet)) {
		// owningEntity.removeAlias(getTLModelObject());
		// }
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ALIAS;
	}

	@Override
	public String getComponentType() {
		return "Alias: " + getName();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Alias);
	}

	@Override
	public LibraryNode getLibrary() {
		if (getOwningComponent() == null)
			return null;
		return getOwningComponent().getLibrary();
	}

	@Override
	public String getName() {
		if (getTLModelObject() == null)
			LOGGER.warn("Missing TLModelObject.");
		return getTLModelObject().getName();
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return Collections.emptyList();
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		return getParent() != null ? getParent().getOwningComponent() : null;
	}

	@Override
	public String getPropertyRole() {
		return "Alias";
	}

	@Override
	public TLAlias getTLModelObject() {
		return (TLAlias) tlObj;
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
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

	// @Override
	public boolean isFacetAlias() {
		return getTLModelObject().getOwningEntity() instanceof TLAbstractFacet;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public boolean isRenameable() {
		return isEditable() && parent == owner;
		// return isEditable() && !(parent instanceof PropertyOwnerInterface);
	}

	@Override
	public boolean isSimpleAssignable() {
		return getParent() != null ? getParent().isSimpleAssignable() : false;
	}

	// listeners propagate change to all tl children
	@Override
	public void setName(String name) {
		if (getTLModelObject() != null)
			getTLModelObject().setName(name == null ? "" : name);
	}

	/**
	 * Used by TypeProviderListener to set name to type users where this alias is assigned. Only if the parent is type
	 * that requires assigned users to use the owner's name.
	 */
	@Override
	public void setNameOnWhereAssigned(String n) {
		if (owner instanceof TypeProvider && !((TypeProvider) owner).isRenameableWhereUsed())
			for (TypeUser u : getWhereAssigned())
				u.setName(n);
	}

	private void clearChildrenCaches() {
		// Clear the children cache
		for (Node c : getParent().getChildren())
			if (c.getChildrenHandler() != null)
				c.getChildrenHandler().clear();
		if (getParent() != null)
			getParent().getChildrenHandler().clear();
	}

}

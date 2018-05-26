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
package org.opentravel.schemas.node.resources;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for inherited resource members. Provides a unique class for the tree views. Delegates methods to wrapped
 * node. Wrapped node is lazy evaluated from the TL Object used in the constructor to allow wrapping of members that
 * have not been modeled yet when opening a project.
 * 
 * @author Dave
 *
 */
public class InheritedResourceMember extends Node implements ResourceMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(InheritedResourceMember.class);

	private ResourceMemberInterface member; // actual node that is inherited
	private TLModelElement tlMember; // used to identify the member

	/**
	 * On construction of the library, the base resource may not have node identity listeners.
	 * 
	 * @param tlObj
	 */
	public InheritedResourceMember(TLModelElement tlObj) {
		tlMember = tlObj;
		member = (ResourceMemberInterface) Node.GetNode(tlObj);
	}

	@Override
	public void addChild(ResourceMemberInterface child) {
		get().addChild(child);
	}

	public void addListeners() {
	}

	/**
	 * Resource members should call this <b>after</b> doing member specific deletes.
	 */
	@Override
	public void delete() {
		get().delete();
		// clearListeners();
		if (parent != null && parent.getChildren() != null)
			parent.getChildren().remove(this);
		deleted = true;
	}

	/**
	 * Get the wrapped member. If it has not been resolved yet, use the identity listener on the member to resolve it.
	 * 
	 * @return
	 */
	public ResourceMemberInterface get() {
		if (member == null)
			member = (ResourceMemberInterface) Node.GetNode(tlMember);
		assert (member != null);
		return member;
	}

	@Override
	public String getDecoration() {
		String decoration = "  Inherited from " + get().getOwningComponent().getName();
		return decoration;
	}

	@Override
	public String getDescription() {
		return get().getDescription();
	}

	@Override
	public List<ResourceField> getFields() {
		return get().getFields();
	}

	@Override
	public Image getImage() {
		return get().getImage();
	}

	@Override
	public String getLabel() {
		return get().getLabel();
	}

	@Override
	public LibraryNode getLibrary() {
		return ((Node) get()).getLibrary();
	}

	@Override
	public String getName() {
		return get().getName();
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return get().getNavChildren(deep);
	}

	@Override
	public ResourceNode getOwningResource() {
		return (ResourceNode) get().getOwningComponent();
	}

	@Override
	public ResourceNode getOwningComponent() {
		return (ResourceNode) get().getOwningComponent();
	}

	@Override
	public TLModelElement getTLModelObject() {
		return get().getTLModelObject();
	}

	@Override
	public TLModelElement getTLOwner() {
		return get().getTLOwner();
	}

	@Override
	public String getTooltip() {
		return get().getTooltip();
	}

	@Override
	public ValidationFindings getValidationFindings() {
		return get().getValidationFindings();
	}

	@Override
	public Collection<String> getValidationMessages() {
		return get().getValidationMessages();
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return ((Node) get()).hasNavChildren(deep);
	}

	@Override
	public boolean isDeleteable() {
		return ((Node) get()).isDeleteable();
	}

	/**
	 * Inherited members are never editable
	 */
	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isNameEditable() {
		return get().isNameEditable();
	}

	@Override
	public boolean isValid() {
		return get().isValid();
	}

	@Override
	public boolean isValid_NoWarnings() {
		return get().isValid_NoWarnings();
	}

	@Override
	public void removeDependency(ResourceMemberInterface dependent) {
		get().removeDependency(dependent);
	}

	@Override
	public void setDescription(final String description) {
		get().setDescription(description);
	}
}

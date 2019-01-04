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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.ConstraintHandler;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.TypeUserListener;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.TypeUserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends property Node to include type assignment capabilities.
 * <p>
 * Property nodes control element (property), attribute and indicator property model objects Simple Attributes and
 * others. (See PropertyNodeType: Role, Literal, Alias, Simple)
 * 
 * Properties extends components by giving them the ability change nature (Indicator, attribute, element) without losing
 * data unique to each one. To do this, it has extra model objects. Also used for RoleProperty nodes.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class TypedPropertyNode extends PropertyNode implements TypeUser {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypedPropertyNode.class);

	/**
	 * A property within a facet can change roles. This class keeps track of the various implementations that may be
	 * used for any property. All properties that can be alternated between should share this class.
	 * 
	 * @author Dave Hollander
	 * 
	 */

	/**
	 * Only used for facade properties that have no TL Model Object.
	 */
	public TypedPropertyNode() {
	}

	/**
	 * Create a property node to represent the passed TL Model object.
	 * 
	 * @param obj
	 */
	protected TypedPropertyNode(final TLModelElement tlObj, FacetInterface parent) {
		super(tlObj, parent);
		typeHandler = new TypeUserHandler(this);
	}

	/**
	 * Create a new property and add to the facet.
	 * 
	 * @param parent
	 *            facet node to attach to
	 * @param name
	 *            to give the property
	 */
	protected TypedPropertyNode(final TLModelElement tlObj, final FacetInterface parent, final String name) {
		this(tlObj, parent);
		setName(name);

		// Properties added to minor versions must be optional
		if (getLibrary() != null && getLibrary().isMinorVersion() && !isInherited())
			setMandatory(false);
	}

	@Override
	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}

	/**
	 * Clone the TLModelObject and create clone node using factory. Assign clone to parent. Set whereUsed on assigned
	 * type.
	 * 
	 * @param parent
	 *            FacetInterface to add clone to
	 * @param nameSuffix
	 *            added at end of name if not null
	 */
	@Override
	public TypedPropertyNode clone(Node parent, String nameSuffix) {
		TypedPropertyNode clone = (TypedPropertyNode) super.clone(parent, nameSuffix);

		if (clone.getAssignedType() != null)
			clone.setAssignedType(this.getAssignedType());
		else
			clone.setAssignedType();

		return clone;
	}

	/**
	 * Remove from TL owner and clear where assigned and parent's children handler.
	 */
	@Override
	public void delete() {
		if (isDeleted() || getParent() == null)
			return;
		deleted = true;
		setAssignedType();
		deleteTL();
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return null; // overriden in element and attribute nodes
	}

	@Override
	public TLModelElement getAssignedTLObject() {
		return getTypeHandler().getAssignedTLModelElement();
	}

	/**
	 * Override to handle inherited properties by getting the inheritsFrom property.
	 * 
	 * @return
	 */
	@Override
	public TypeProvider getAssignedType() {
		assert !isInherited();
		return getTypeHandler() != null ? getTypeHandler().get() : null;
	}

	/**
	 * Return new array containing assigned type if an alias its parent
	 */
	@Override
	public List<Node> getNavChildren(boolean deep) {
		List<Node> kids = new ArrayList<>();
		if (deep) {
			Node typeNode = (Node) getAssignedType();
			kids.add(typeNode);

			// If it is an alias, add its object as well.
			if (typeNode instanceof AliasNode)
				kids.add(typeNode.getParent());
		}
		return kids;
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new TypeUserListener(this);
	}

	@Override
	public TypeProvider getRequiredType() {
		return null; // override for properties with fixed types
	}

	/**
	 * Overridden to provide assigned type and aliases if deep is set.
	 */
	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getNavChildren(deep);
	}

	/**
	 * Use {@link #getAssignedType}
	 */
	@Override
	@Deprecated
	public Node getType() {
		return (Node) getAssignedType();
	}

	@Override
	public String getAssignedTypeName() {
		return getTypeHandler() != null ? getTypeHandler().getAssignedTypeName() : "";
	}

	@Override
	public String getAssignedTLTypeName() {
		return ""; // Override if property has a TL Type Name field.
	}

	@Override
	public String getTypeNameWithPrefix() {
		return getTypeHandler() != null ? getTypeHandler().getAssignedTypeNameWithPrefix() : "";
	}

	@Override
	public TypeSelectionFilter getTypeSelectionFilter() {
		return null;
	}

	/**
	 * True if it has assigned type
	 */
	@Override
	public boolean hasNavChildren(boolean deep) {
		return deep && getAssignedType() != null;
	}

	@Override
	public boolean isAssignedComplexType() {
		// Need to test because inherited properties do not have assigned types.
		return getAssignedType() != null ? getAssignedType().isAssignedByReference() : false;
	}

	/**
	 * Typed Properties are only be a navigation child in deep mode.
	 */
	@Override
	public boolean isNavChild(boolean deep) {
		return deep;
	}

	// Override if not re-nameable.
	@Override
	public boolean isRenameable() {
		return isEditable() && !isInherited() && getAssignedType().isRenameableWhereUsed();
	}

	@Override
	public abstract void removeAssignedTLType();

	@Override
	public boolean setAssignedType() {
		return getTypeHandler().set();
	}

	@Override
	public abstract boolean setAssignedTLType(TLModelElement tlProvider);

	@Override
	public TypeProvider setAssignedType(TypeProvider provider) {
		return getTypeHandler().set(provider) ? provider : null;
	}

	/**
	 * @return the typeHandler
	 */
	@Override
	public TypeUserHandler getTypeHandler() {
		return typeHandler;
	}

}

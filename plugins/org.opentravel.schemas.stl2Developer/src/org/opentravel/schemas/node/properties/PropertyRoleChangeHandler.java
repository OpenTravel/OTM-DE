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

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for changing property roles from Element to Attribute to Indicator, etc. Maintains copy of previous role and
 * uses the copy when possible.
 * 
 * @author dmh
 *
 */
public class PropertyRoleChangeHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyRoleChangeHandler.class);

	private PropertyNode owner = null;
	public PropertyNode currentPN;
	public ElementNode oldEleN = null;
	public IndicatorNode oldIndN = null;
	public AttributeNode oldAttrN = null;
	public IndicatorElementNode oldIndEleN = null;
	public ElementReferenceNode oldEleRefN = null;
	public AttributeReferenceNode oldAttrRefN = null;
	public IdNode oldIdN = null;

	public PropertyRoleChangeHandler(PropertyNode pn) {
		this(pn, pn.getParent());
	}

	public PropertyRoleChangeHandler(PropertyNode pn, Node parent) {
		owner = pn;
		assert owner != null;
		assert parent instanceof FacetInterface;

		// Have to use role assigned not class because it is used in chained constructors
		// so the class type may be wrong.
		switch (pn.getPropertyType()) {
		case ELEMENT:
			oldEleN = (ElementNode) pn;
			break;
		case ATTRIBUTE:
			oldAttrN = (AttributeNode) pn;
			break;
		case ID:
			oldIdN = (IdNode) pn;
			break;
		case ID_REFERENCE:
			oldEleRefN = (ElementReferenceNode) pn;
			break;
		case ID_ATTR_REF:
			oldAttrRefN = (AttributeReferenceNode) pn;
			break;
		case INDICATOR:
			oldIndN = (IndicatorNode) pn;
			break;
		case INDICATOR_ELEMENT:
			oldIndEleN = (IndicatorElementNode) pn;
			break;
		default:
			break;
		}
		currentPN = pn;
	}

	/**
	 * If an old copy of this property has been saved, return it; otherwise create a new property. New properties are
	 * saved.
	 * 
	 * @param type
	 * @return property or null if not supported for the property type or this was already that type.
	 */
	public PropertyNode oldOrNew(PropertyNodeType type) {
		assert currentPN.getParent() instanceof FacetInterface;
		if (currentPN.getPropertyType().equals(type))
			return null;

		FacetInterface parent = (FacetInterface) currentPN.getParent();

		PropertyNode pn = null;
		switch (type) {
		case ELEMENT:
			// set pn to saved element if there is one or else create an element
			pn = oldEleN != null ? oldEleN : new ElementNode(parent, owner.getName());
			oldEleN = (ElementNode) pn;
			break;
		case ID_REFERENCE:
			pn = oldEleRefN != null ? oldEleRefN : new ElementReferenceNode(parent);
			oldEleRefN = (ElementReferenceNode) pn;
			break;
		case ID_ATTR_REF:
			pn = oldAttrRefN != null ? oldAttrRefN : new AttributeReferenceNode(parent);
			oldAttrRefN = (AttributeReferenceNode) pn;
			break;
		case ATTRIBUTE:
			pn = oldAttrN != null ? oldAttrN : new AttributeNode(parent, owner.getName());
			oldAttrN = (AttributeNode) pn;
			break;
		case INDICATOR:
			pn = oldIndN != null ? oldIndN : new IndicatorNode(parent, owner.getName());
			oldIndN = (IndicatorNode) pn;
			break;
		case INDICATOR_ELEMENT:
			pn = oldIndEleN != null ? oldIndEleN : new IndicatorElementNode(parent, owner.getName());
			oldIndEleN = (IndicatorElementNode) pn;
			break;
		case ID:
			pn = oldIdN != null ? oldIdN : new IdNode(parent, owner.getName());
			oldIdN = (IdNode) pn;
			break;
		default:

		}
		if (pn != null) {
			pn.setChangeHandler(this); // in case there was a new node created.
			currentPN = pn;
		}
		return pn;
	}

	/**
	 * Replace this property with one of the specified type. Uses the saved alternateRole or creates a new property. All
	 * values that can be copied will be. The old property is saved. All copies share the same alternateRoles instance.
	 * <p>
	 * The parent is checked to assure it can own the requested type.If not, ATTRIBUTE is used as toType.
	 * 
	 * @param toType
	 * @param parent
	 *            - optional property checked to assure it can own the toType. getParent() used if omitted.
	 * @return the new property, or this property if change could not be made.
	 */

	public PropertyNode changePropertyRole(PropertyNodeType toType) {
		if (currentPN.getParent() instanceof FacetInterface)
			return changePropertyRole(toType, (FacetInterface) currentPN.getParent());
		return changePropertyRole(toType, (FacetInterface) null);
	}

	public PropertyNode changePropertyRole(PropertyNodeType toType, FacetInterface parent) {

		if (parent != null && !parent.canOwn(toType))
			toType = PropertyNodeType.ATTRIBUTE;

		PropertyNode oldPN = currentPN; // OldOrNew will change it
		PropertyNode newPN = null; // new property
		TypeProvider newType = null; // type to assign to new property

		newPN = oldOrNew(toType);

		if (newPN != null) {
			newPN.copyDetails(oldPN);
			oldPN.swap(newPN);

			// Now do type assignments
			if (oldPN instanceof TypeUser) {
				newType = ((TypeUser) oldPN).getAssignedType();
				((TypeUser) oldPN).getAssignedType().getWhereAssignedHandler().removeUser((TypeUser) oldPN);
			}
			// If there was an assigned type, use it otherwise assign old type to this.
			if (newPN instanceof TypeUser) {
				if (((TypeUser) newPN).getAssignedType() != null
						&& !(((TypeUser) newPN).getAssignedType() instanceof ImpliedNode))
					newType = ((TypeUser) newPN).getAssignedType();// If there was an assigned type, use it
				((TypeUser) newPN).setAssignedType(newType); // set type and where used
			}

		}

		return newPN == null ? currentPN : newPN;
	}

}

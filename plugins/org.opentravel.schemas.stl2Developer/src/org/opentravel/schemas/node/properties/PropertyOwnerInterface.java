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
package org.opentravel.schemas.node.properties;


/**
 * Implementers can be the parent of a property.
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public interface PropertyOwnerInterface {

	// /**
	// * Add property to TL model object then clear children handler cache.
	// *
	// * @param property
	// */
	// public void addProperty(PropertyNode property);
	//
	// /**
	// * Add list of properties to a facet.
	// *
	// * @param properties
	// * @param clone
	// * - if true, the properties are cloned before adding.
	// */
	// public void addProperties(List<Node> properties, boolean clone);
	//
	// public void add(PropertyNode property, int i);
	//
	// /**
	// * Drag-n-drop behavior. Assign type if unassigned or clone property and assign type
	// *
	// * @param type
	// * - type to assign
	// * @return
	// */
	// public INode createProperty(final Node type);
	//
	// public List<Node> getChildren();
	//
	// public List<Node> getChildren_TypeUsers();
	//
	// public LibraryMemberInterface getOwningComponent();
	//
	// public TLModelElement getTLModelObject();
	//
	// public boolean isDefaultFacet();
	//
	// public boolean isEditable();
	//
	// /**
	// * Remove the property this node and underlying tl model object. Use to move the property to a different facet.
	// *
	// * @param property
	// */
	// public void removeProperty(final Node property);
	//
	// /**
	// * @param attrName
	// * @return
	// */
	// public PropertyNode findChildByName(String attrName);
	//
	// /**
	// * @return
	// */
	// public ChildrenHandlerI<?> getChildrenHandler();

}

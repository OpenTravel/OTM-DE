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
package org.opentravel.schemas.node.interfaces;

import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.types.ExtensionHandler;

/**
 * Implementors can extend a base type. Extensions change the structure of the object. Extensions may be assigned by the
 * owner as a base type (tlExtension, parent) or by version logic for minor versions.
 * 
 * As of 2/2016 the mapping to TL model is: ExtensionPoints, Business, Core, Choice, Open and Closed Enum objects and
 * operations and resources use TLExtensions. VWA uses TL.parent.
 * 
 * The base type may be any of these owners <b>or</b> a facet (as used by ExtensionPoint).
 * 
 * @author Dave
 *
 */
// FIXME - the name is confusing, it makes me think of base objects not sub-type
public interface ExtensionOwner {

	// Node level impls - return "" if not overridden.
	public String getExtendsTypeName();

	public String getExtendsTypeNS();

	public boolean isInstanceOf(Node base);

	/**
	 * Set the base type for this node to the passed base object such that <i>this extends base</i>.
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
	public void setExtension(Node base);

	/**
	 * Return the base type if any. Note that the base type may be a true extension or may be used for versions.
	 * 
	 * @return the base type - the node displayed in select Extends field. Null if no base type.
	 * @see Node#getExtendsType()
	 */
	public Node getExtensionBase();

	// ?? Does this belong here? Only extended objects have inheritance
	public List<Node> getInheritedChildren();

	/**
	 * @return
	 */
	public TLModelElement getTLModelObject();

	/**
	 * @return
	 */
	public Object getOwningComponent();

	/**
	 * Get the handler for this extension owner if any.
	 *
	 * @return the handler if this owner extends a base, otherwise null.
	 */
	public ExtensionHandler getExtensionHandler();

	// // NOT Part of extension
	// public LibraryNode getLibrary();
	//
	// // /**
	// // * @return the ModelObject to use for assignment and tests
	// // */
	// // public ModelObject<?> getModelObject();
	//
	// public String getNameWithPrefix();
	//
	// public TLModelElement getTLModelObject();
	//
	// public LibraryMemberInterface getOwningComponent();

}

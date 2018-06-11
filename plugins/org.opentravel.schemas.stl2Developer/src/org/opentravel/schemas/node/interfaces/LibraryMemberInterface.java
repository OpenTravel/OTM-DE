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
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.handlers.children.ChildrenHandlerI;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;

/**
 * Implementors are objects that are first class, named members of the library.
 * 
 * @author Dave
 *
 */

public interface LibraryMemberInterface extends INode {

	public LibraryMemberInterface clone(LibraryNode targetLib, String nameSuffix);

	/**
	 * Create a copy of this node in destination library
	 * 
	 * @param destLib
	 *            library to add copy to. This library used if null.
	 * @return the copied node
	 */
	public LibraryMemberInterface copy(LibraryNode destLib) throws IllegalArgumentException;

	public List<AliasNode> getAliases();

	/**
	 * @return children handler specific to this library member or null if no children
	 */
	public ChildrenHandlerI<?> getChildrenHandler();

	@Override
	public String getLabel();

	@Override
	public LibraryNode getLibrary();

	@Override
	public String getName();

	@Override
	public String getNamespace();

	@Override
	public String getNameWithPrefix();

	@Override
	public String getPrefix();

	/**
	 * @return
	 */
	public TLModelElement getTLModelObject();

	/**
	 * @return
	 */
	public VersionNode getVersionNode();

	/**
	 * @return
	 */
	public boolean isEditable_isNewOrAsMinor();

	/**
	 * @return
	 */
	public boolean isEditable_newToChain();

	/**
	 * @return
	 */
	public boolean isEnabled_AddProperties();

	/**
	 * @return
	 */
	public boolean isInHead();

	/**
	 * @return
	 */
	public boolean isLatestVersion();

	/**
	 * @return
	 */
	public boolean isNewToChain();

	public boolean isValid();

	/**
	 * @return true if this object is a later version of another object. True if has same base ns and name as the object
	 *         it extends.
	 */
	public boolean isVersioned();

	public void setLibrary(LibraryNode library);

}

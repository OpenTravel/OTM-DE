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

import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * Implementors are objects that are first class, named members of the library.
 * 
 * @author Dave
 *
 */

public interface LibraryMemberInterface extends INode {

	/**
	 * Create a copy of this node in destination library
	 * 
	 * @param destLib
	 *            library to add copy to. This library used if null.
	 * @return the copied node
	 */
	public LibraryMemberInterface copy(LibraryNode destLib) throws IllegalArgumentException;

	public List<AliasNode> getAliases();

	public String getLabel();

	public LibraryNode getLibrary();

	public String getName();

	public String getNamespace();

	public String getNameWithPrefix();

	public String getPrefix();

	public boolean isValid();

	public void setLibrary(LibraryNode library);

}

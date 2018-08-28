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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * Implementors are libraries or library chains. They contain members which define the model objects. Library Nav Node
 * which links libraries to projects is also an implementer.
 * 
 * @author Dave
 *
 */
public interface LibraryInterface {

	/**
	 * Close the library or chain. Do NOT remove members from TL model.
	 */
	public void close();

	/**
	 * Use the parent, if known, to close this library from the parent's context. If the parent is null, close the
	 * contents of this library.
	 * <p>
	 * <b>Warning</b> the caller must assure the parent is the intended LibraryOwner or null.
	 * <p>
	 * The parent uses the library model manager because this LibraryNode may be linked to multiple LibraryNavNodes or
	 * VersionAggregateNodes. If this library is not used elsewhere the library model manager will call close with
	 * parent set to null.
	 */
	public void closeLibraryInterface();

	/**
	 * @return this if is a chain, chain if library is in a chain or null
	 */
	public LibraryChainNode getChain();

	public Image getImage();

	public String getLabel();

	/**
	 * @return the library or chain head library
	 */
	public LibraryNode getLibrary();

	public LibraryNavNode getLibraryNavNode();

	public String getName();

	public List<Node> getNavChildren(boolean deep);

	/**
	 * Could be a Project or a LibraryChainNode. NOTE: if library or chain there may be other projects that are also
	 * related.
	 * 
	 * @return one of possibly many parents related to this library.
	 */
	public Node getParent();

	/**
	 * @return one of possibly many projects related to this library. NOTE: if library or chain there may be other
	 *         projects that are also related.
	 */
	public ProjectNode getProject();

	public List<Node> getTreeChildren(boolean deep);

	public boolean hasNavChildren(boolean deep);

	public boolean hasTreeChildren(boolean deep);

	public boolean isNavChild(boolean deep);

	public void setParent(Node project);

}

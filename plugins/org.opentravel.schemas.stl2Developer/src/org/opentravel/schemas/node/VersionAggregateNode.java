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
package org.opentravel.schemas.node;

import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * The version aggregate node collects libraries that are in a chain. The library chain displays it children which are
 * Aggregate Node and a Version Aggregate Node.
 * 
 * Children this node are only allowed to be libraries.
 * 
 * @author Dave Hollander
 * 
 */
public class VersionAggregateNode extends AggregateNode {

	public VersionAggregateNode(AggregateType type, LibraryChainNode parent) {
		super(type, parent);
	}

	/**
	 * Add this library to the aggregate's children.
	 * <p>
	 * Set libraryNavNode's library to the chain.
	 * <p>
	 * Set the library parent to this aggregate.
	 */
	public void add(LibraryNode ln) {
		// getChildren().add(ln); // fixme
		// ln.getParent().getChildren().remove(ln); // fixme
		// ln.setParent(this);
		// assert ln.getParent() instanceof LibraryNavNode;

		getChildrenHandler().add(ln);
		if (ln.getParent() instanceof LibraryNavNode)
			((LibraryNavNode) ln.getParent()).setThisLib(getParent());
		ln.setParent(this);
	}

	// public void add(Node n) {
	// throw (new IllegalStateException("Version aggregates can not contain " + n.getClass().getSimpleName()));
	// }

	@Override
	public void close() {
		// if (getParent() != null)
		// getParent().getChildren().remove(this);
		for (Node n : getChildren_New())
			n.close();
		getChildren().clear();
		setLibrary(null);
		deleted = true;
	}

	/**
	 * @return the library matching this project item or null
	 */
	public LibraryNode get(ProjectItem pi) {
		for (Node n : getChildren())
			if (pi.equals(((LibraryNode) n).getProjectItem()))
				return (LibraryNode) n;
		return null;
	}

	/**
	 * Return the project this chain has as its parent. <b>Note</b> that libraries can belong to multiple projects.
	 * 
	 * @see {@link LibraryModelManager#isUsedElsewhere(LibraryInterface, ProjectNode)}
	 * @return parent project or null if no project is found.
	 */
	public ProjectNode getProject() {
		return getParent().getProject();
	}

	@Override
	public LibraryChainNode getParent() {
		return (LibraryChainNode) parent;
	}
}

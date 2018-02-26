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
import org.opentravel.schemas.node.interfaces.LibraryOwner;
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
public class VersionAggregateNode extends AggregateNode implements LibraryOwner {

	public VersionAggregateNode(AggregateType type, LibraryChainNode parent) {
		super(type, parent);
	}

	/**
	 * Add this library to the aggregate's children handler.
	 * <p>
	 * Set libraryNavNode's library to the chain.
	 * <p>
	 * Set the library parent to this aggregate.
	 */
	public void add(LibraryNode ln) {
		getChildrenHandler().add(ln);
		if (ln.getParent() instanceof LibraryNavNode)
			((LibraryNavNode) ln.getParent()).setThisLib(getParent());
		ln.setParent(this);
	}

	/**
	 * Close the library and remove from this chain
	 * <p>
	 * Use the libraryManager to assure the libraries are not used elsewhere.
	 * 
	 * @param ln
	 */
	public void close(LibraryNode ln) {
		// If ln is not used elsewhere, it will closed a parent set to null
		Node.getLibraryModelManager().close(ln, getProject());
		getChildrenHandler().clear(ln);
		// Junit Delete_Tests#deleteLibraries_Tests() - if the versions are empty then???
		if (getChildrenHandler().get().isEmpty()) {
			if (getProject() != null)
				getProject().close(getParent());
			if (getParent() != null)
				getParent().close();
		}
	}

	/**
	 * Remove all libraries from this chain. Attempts to close all the libraries using the library manager.
	 */
	@Override
	public void close() {
		for (Node ln : getChildrenHandler().getChildren_New())
			if (ln instanceof LibraryNode)
				close((LibraryNode) ln);
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
	@Override
	public ProjectNode getProject() {
		return getParent().getProject();
	}

	@Override
	public LibraryChainNode getParent() {
		return (LibraryChainNode) parent;
	}

	@Override
	public boolean contains(LibraryInterface lib) {
		return getChildrenHandler().contains((Node) lib);
	}

	@Override
	public String getDecoration() {
		return "Contains: ";
	}

	// /**
	// * Get a new list of library members in this Nav Node.
	// *
	// * @return
	// */
	// @Override
	// public List<LibraryMemberInterface> get_LibraryMembers() {
	// List<LibraryMemberInterface> members = new ArrayList<LibraryMemberInterface>();
	// for (Node ln : getChildren()) {
	// members.addAll(ln.getDescendants_LibraryMembers());
	// // if (n instanceof VersionNode && ((VersionNode) n).get() != null)
	// // n = ((VersionNode) n).get();
	// // if (n instanceof LibraryMemberInterface)
	// // members.add((LibraryMemberInterface) n);
	// }
	// return members;
	// }

}

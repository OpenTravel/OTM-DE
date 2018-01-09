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
package org.opentravel.schemas.node.handlers.children;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.AggregateNode;
import org.opentravel.schemas.node.AggregateNode.AggregateType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.types.TypeProviderAndOwners;

/**
 * Model node children handler using a static children handler for chain aggregate nav nodes.
 * <p>
 * 
 * @author Dave
 *
 */
public class LibraryChainChildrenHandler extends StaticChildrenHandler<Node, LibraryChainNode> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ModelNodeChildrenHandler.class);

	// Library Chains collect content from all chain libraries organized by the nav-node.
	protected AggregateNode complexRoot;

	/**
	 * @return the complexRoot
	 */
	public AggregateNode getComplexRoot() {
		return complexRoot;
	}

	/**
	 * @return the simpleRoot
	 */
	public AggregateNode getSimpleRoot() {
		return simpleRoot;
	}

	/**
	 * @return the serviceRoot
	 */
	public AggregateNode getServiceRoot() {
		return serviceRoot;
	}

	/**
	 * @return the resourceRoot
	 */
	public AggregateNode getResourceRoot() {
		return resourceRoot;
	}

	/**
	 * @return the versions
	 */
	public VersionAggregateNode getVersions() {
		return versions;
	}

	protected AggregateNode simpleRoot;
	protected AggregateNode serviceRoot;
	protected AggregateNode resourceRoot;
	protected VersionAggregateNode versions;

	public LibraryChainChildrenHandler(LibraryChainNode owner) {
		super(owner);
		createAggregates();
	}

	private void createAggregates() {
		versions = new VersionAggregateNode(AggregateType.Versions, owner);
		complexRoot = new AggregateNode(AggregateType.ComplexTypes, owner);
		simpleRoot = new AggregateNode(AggregateType.SimpleTypes, owner);
		serviceRoot = new AggregateNode(AggregateType.Service, owner);
		resourceRoot = new AggregateNode(AggregateType.RESOURCES, owner);
		children.add(complexRoot);
		children.add(resourceRoot);
		children.add(serviceRoot);
		children.add(simpleRoot);
		children.add(versions);
	}

	public LibraryInterface getThisLibI() {
		if (!children.isEmpty() && children.get(0) instanceof LibraryInterface)
			return (LibraryInterface) children.get(0);
		return null;
	}

	// LibraryNavNode only ever has 1 child.
	@Override
	public void add(Node c) {
		// children.clear();
		// children.add(c);
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		return Collections.emptyList();
	}

	// @Override
	// public List<TLModelElement> getInheritedChildren_TL() {
	// return Collections.emptyList();
	// }

	@Override
	public boolean hasNavChildren(boolean deep) {
		return true;
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return children;
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return true;
		// return getThisLibI() != null ? ((Node) getThisLibI()).hasChildren_TypeProviders() : false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TypeProviderAndOwners> getChildren_TypeProviders() {
		List<TypeProviderAndOwners> tpos = new ArrayList<TypeProviderAndOwners>();
		for (Node child : children)
			tpos.add((TypeProviderAndOwners) child);
		return tpos;
		// return getThisLibI() != null ? ((Node) getThisLibI()).getChildrenHandler().getChildren_TypeProviders()
		// : Collections.EMPTY_LIST;
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		return true;
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		List<Node> treeKids = new ArrayList<Node>(children);
		LibraryNode head = owner.getHead();
		if (head != null && head.getWhereUsedHandler() != null) {
			if (!treeKids.contains(head.getWhereUsedHandler().getWhereUsedNode()))
				treeKids.add(head.getWhereUsedHandler().getWhereUsedNode());
			if (!treeKids.contains(head.getWhereUsedHandler().getUsedByNode()))
				treeKids.add(head.getWhereUsedHandler().getUsedByNode());
		}
		return treeKids;
	}

}

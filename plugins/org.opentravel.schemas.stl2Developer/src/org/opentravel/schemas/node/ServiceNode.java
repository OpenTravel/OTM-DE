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
package org.opentravel.schemas.node;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemas.node.handlers.children.ServiceChildrenHandler;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.objectMembers.OperationNode.ServiceOperationTypes;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.trees.type.BusinessObjectOnlyTypeFilter;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ServiceNode extends ComponentNode implements LibraryMemberInterface {
	private final static Logger LOGGER = LoggerFactory.getLogger(ServiceNode.class);

	protected LibraryNode owningLibrary = null;

	public ServiceNode(final BusinessObjectNode n) {
		this(n.getLibrary());
		addCRUDQ_Operations(n);
		setName(n.getName());
	}

	/**
	 * Create a new service (node and underlying TL model). Note: If the passed node's library does not have a service,
	 * the service is linked into the library.
	 * 
	 * @param n
	 *            node to get the library from. Can be the library. If it is a business object, CRUD operations will be
	 *            create with it as the subject.
	 */
	public ServiceNode(final LibraryNode ln) {
		this(new TLService(), ln);
		setDescription(ln.getDescription());

		assert (getTLModelObject() instanceof TLService);
	}

	/**
	 * Service created from TL Service already in a library.
	 * 
	 * @param tlSvc
	 * @param ln
	 */
	public ServiceNode(final TLService tlSvc) {
		super(tlSvc);

		childrenHandler = new ServiceChildrenHandler(this);
	}

	/**
	 * Service constructor used in tests created from TL Service already in a library.
	 * 
	 * @param tlSvc
	 * @param ln
	 */
	public ServiceNode(final TLService tlSvc, LibraryNode ln) {
		this(tlSvc);

		owningLibrary = ln;
		if (ln != null)
			ln.addServiceNode(this);
	}

	public void add(OperationNode op) {
		getChildrenHandler().add(op);
	}

	/**
	 * Add CRUDQ operations to service. Set message elements to subject. Query operations are made for each query facet.
	 * Does nothing if the node is not a business object.
	 * 
	 * @param nodeInterface
	 */
	public void addCRUDQ_Operations(Node subject) {
		if (!(subject instanceof BusinessObjectNode))
			return;
		BusinessObjectNode bo = (BusinessObjectNode) subject;
		for (ServiceOperationTypes op : ServiceOperationTypes.values())
			if (!op.equals(ServiceOperationTypes.QUERY))
				new OperationNode(this, op.displayName, op, bo);
		for (Node n : bo.getQueryFacets())
			new OperationNode(this, n.getLabel(), ServiceOperationTypes.QUERY, bo);
	}

	@Override
	public LibraryMemberInterface clone(LibraryNode targetLib, String nameSuffix) {
		return null; // NO-OP
	}

	// @Override
	// public Node clone(Node parent, String nameSuffix) {
	// return null; // NO-OP
	// }

	@Override
	public LibraryElement cloneTLObj() {
		return null;
	}

	/**
	 * Delete Service. There can only be one service in a library. The service parent is the library where declared. It
	 * may also be in the service aggregate for the chain. Services are not wrapped in a version node.
	 */
	@Override
	public void delete() {
		// LOGGER.debug("Deleting Service node.");
		if (getLibrary().isInChain())
			getLibrary().getChain().removeFromAggregate(this);
		getLibrary().getServiceRoot().removeLM(this);
		deleteTL();
		deleted = true;
	}

	@Override
	public void deleteTL() {
		if (getTLModelObject().getOwningLibrary() != null)
			getTLModelObject().getOwningLibrary().removeNamedMember(getTLModelObject());
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.OPERATION;
	}

	@Override
	public ServiceChildrenHandler getChildrenHandler() {
		return (ServiceChildrenHandler) childrenHandler;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.SERVICE;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Service);
	}

	@Override
	public String getLabel() {
		return getLibrary() != null ? getName() + " (Version:  " + getLibrary().getVersion() + ")" : "";
	}

	@Override
	public LibraryNode getLibrary() {
		return owningLibrary;
	}

	@Override
	public String getName() {
		return getTLModelObject() == null || getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public TLService getTLModelObject() {
		return (TLService) tlObj;
	}

	@Override
	public TypeSelectionFilter getTypeSelectionFilter() {
		return new BusinessObjectOnlyTypeFilter(null);
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		// enable if we want to have messages assignable as types.
		return false;
	}

	@Override
	public boolean isAssignable() {
		return false;
	}

	public boolean isEmpty() {
		return getChildren() != null ? getChildren().isEmpty() : true;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		if (getLibrary() == null || parent == null || !isEditable() || isDeleted())
			return false;

		// Adding to service will automatically create correct service operation to add to.
		if (!getLibrary().isInChain())
			return true;
		return !getLibrary().getChain().getHead().getEditStatus().equals(NodeEditStatus.PATCH);
	}

	@Override
	public boolean isNamedEntity() {
		return false;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	public void remove(OperationNode op) {
		getChildrenHandler().remove(op);
	}

	/**
	 * Simple Setter of owning library
	 */
	@Override
	public void setLibrary(LibraryNode lib) {
		owningLibrary = lib;
	}

	@Override
	public void setName(final String name) {
		getTLModelObject().setName(NodeNameUtils.fixServiceName(name));
	}

	@Override
	public LibraryMemberInterface copy(LibraryNode destLib) throws IllegalArgumentException {
		return null;
	}

	@Override
	public List<AliasNode> getAliases() {
		return null;
	}
}

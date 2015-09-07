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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemas.node.OperationNode.ResourceOperationTypes;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ServiceNode extends ComponentNode {
	private final static Logger LOGGER = LoggerFactory.getLogger(ServiceNode.class);

	public ServiceNode(final TLService tlSvc, LibraryNode ln) {
		super(tlSvc);
		addMOChildren();

		if (ln == null)
			throw new IllegalArgumentException("Null library for the service.");

		// Make sure the library only has one service.
		final TLLibrary tlLib = (TLLibrary) ln.getTLaLib();
		if (ln.getServiceRoot() != null) {
			ln.getServiceRoot().delete();
		}
		if (tlLib.getService() != tlSvc) {
			tlLib.setService(tlSvc);
		}

		if (tlSvc.getName() == null || tlSvc.getName().isEmpty())
			setName(ln.getName() + "_Service");
		ln.getChildren().add(this);
		setParent(ln);
		setLibrary(ln);
		ln.setServiceRoot(this);

		if (!(tlSvc instanceof TLService))
			throw new IllegalArgumentException("Invalid object to create service from.");
	}

	/**
	 * Create a new service (node and underlying TL model). Note: If the passed node's library does not have a service,
	 * the service is linked into the library.
	 * 
	 * @param n
	 *            node to get the library from. If it is a business object, CRUD operations will be create with it as
	 *            the subject.
	 */
	public ServiceNode(final Node n) {
		this(new TLService(), n.getLibrary());
		setDescription(n.getDescription());
		setName(n.getName(), false);
		addCRUDQ_Operations(n);
		// If a chain, the wrap the service in a version and add to chain aggregate.
		if (n.getLibrary().isInChain())
			n.getLibrary().getChain().add(this);
	}

	/**
	 * Add CRUDQ operations to service. Set message elements to subject. Query operations are made for each query facet.
	 * 
	 * @param nodeInterface
	 */
	public void addCRUDQ_Operations(Node subject) {
		if (!subject.isBusinessObject())
			return;
		BusinessObjectNode bo = (BusinessObjectNode) subject;
		for (ResourceOperationTypes op : ResourceOperationTypes.values())
			if (!op.equals(ResourceOperationTypes.QUERY))
				new OperationNode(this, op.displayName, op, subject);
		for (Node n : bo.getQueryFacets())
			new OperationNode(this, n.getLabel(), ResourceOperationTypes.QUERY, subject);
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.OPERATION;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Service);
	}

	@Override
	public String getLabel() {
		return "Service: " + getName();
	}

	@Override
	public boolean isService() {
		return true;
	}

	@Override
	public boolean isAssignable() {
		return false;
	}

	@Override
	public boolean isTypeProvider() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#hasChildren_TypeProviders()
	 */
	@Override
	public boolean hasChildren_TypeProviders() {
		// enable if we want to have messages assignable as types.
		return false;
	}

}

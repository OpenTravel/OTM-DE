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
package org.opentravel.schemas.node.facets;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemas.modelObject.OperationMO;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.NodeVisitors;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.types.TypeProvider;

/**
 * Service Operations.
 * 
 * @author Dave Hollander
 * 
 */
public class OperationNode extends PropertyOwnerNode implements VersionedObjectInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(OperationNode.class);

	/**
	 * List of operations used in CRUD resource services.
	 */
	public enum ResourceOperationTypes {
		CREATE("Create"), DELETE("Delete"), QUERY("Query"), READ("Read"), UPDATE("Update");

		public String displayName;

		private ResourceOperationTypes(String displayName) {
			this.displayName = displayName;
		}
	}

	public OperationNode(TLOperation tlObj) {
		super(tlObj);
		addMOChildren();

		assert (modelObject instanceof OperationMO);
	}

	/**
	 * Create a Service Operation for the passed service. Creates RQ, RS and Notif. Adds new operation to the TL Model.
	 * Messages complete with unassigned Subject element.
	 * 
	 * @param name
	 *            of the operation
	 */
	public OperationNode(ServiceNode svc, String name) {
		super(new TLOperation()); // creates model object

		LibraryNode head = null;
		String svcName = svc.getName();
		if (svc.getLibrary() != null && svc.getLibrary().getChain() != null)
			head = svc.getLibrary().getChain().getHead();

		// If there is no service in the head library then create one.
		if (head != null && svc.getLibrary().isManaged()) {
			// LOGGER.debug("Managed service is not in version head library! " + svc.isInHead());
			if (head.getServiceRoot() == null) {
				svc = new ServiceNode(head);
				svc.setName(svcName); // return;
			} else
				svc = (ServiceNode) head.getServiceRoot();
		}

		svc.getTLModelObject().addOperation(getTLModelObject());
		this.setName(name);
		setExtensible(true);
		svc.linkChild(this);

		// Create Messages from those in the new TLOperation
		for (final Object msg : modelObject.getChildren()) {
			if (msg instanceof TLFacet) {
				final FacetNode fn = new FacetNode((TLFacet) msg);
				this.linkChild(fn);
				new ElementNode(fn, "");
			}
		}

		assert (modelObject instanceof OperationMO);
		assert (getTLModelObject() instanceof TLOperation);
	}

	/**
	 * Create a Service Operation as a facet node of the passed service. Creates RQ, RS and Notif Messages complete with
	 * Subject element.
	 * 
	 * @param name
	 *            of the operation
	 * @param subject
	 *            is the business object to assign to the messages
	 */
	public OperationNode(final ServiceNode service, String name, ResourceOperationTypes type,
			BusinessObjectNode businessObject) {
		this(new TLOperation());
		if (businessObject == null)
			return;

		final TLOperation tlo = getTLModelObject();
		service.getTLModelObject().addOperation(tlo);
		tlo.setOwningService(service.getTLModelObject());
		this.setName(name);
		setExtensible(true);
		service.linkChild(this);

		for (final Object msg : modelObject.getChildren()) {
			if (!(msg instanceof TLFacet))
				continue;

			final FacetNode fn = new FacetNode((TLFacet) msg);
			this.linkChild(fn);
			final ElementNode np = new ElementNode(fn, businessObject.getName());
			TLFacet tlf = (TLFacet) np.getTLModelObject().getOwner();
			switch (type) {
			case CREATE:
				set(np, tlf.getFacetType(), businessObject, businessObject, businessObject.getIDFacet());
				break;
			case DELETE:
				set(np, tlf.getFacetType(), businessObject, businessObject.getIDFacet(), businessObject);
				break;
			case QUERY:
				PropertyOwnerNode query = null;
				if (businessObject.getQueryFacets().size() > 0) {
					// Find the query facet that matches the name parameter
					for (Node qf : businessObject.getQueryFacets())
						if (qf.getLabel().equals(name))
							query = (PropertyOwnerNode) qf;
				}
				set(np, tlf.getFacetType(), query, businessObject, businessObject.getIDFacet());
				break;
			case READ:
				set(np, tlf.getFacetType(), businessObject.getIDFacet(), businessObject, businessObject);
				break;
			case UPDATE:
				set(np, tlf.getFacetType(), businessObject, businessObject.getIDFacet(), businessObject);
				break;
			}
			((Node) np).visitAllNodes(new NodeVisitors().new FixNames());
		}

		assert (modelObject instanceof OperationMO);
		assert (getTLModelObject() instanceof TLOperation);
	}

	/**
	 * Set the node type based on what type of facet is passed.
	 */
	private void set(ElementNode np, TLFacetType type, TypeProvider rq, TypeProvider rs, TypeProvider notif) {
		if (type.equals(TLFacetType.REQUEST))
			np.setAssignedType(rq);
		else if (type.equals(TLFacetType.RESPONSE))
			np.setAssignedType(rs);
		else if (type.equals(TLFacetType.NOTIFICATION))
			np.setAssignedType(notif);

	}

	@Override
	public TLFacetType getFacetType() {
		return null;
	}

	@Override
	public String getComponentType() {
		return ComponentNodeType.OPERATION.toString();
		// return "Operation";
	}

	@Override
	public String getName() {
		return getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public TLOperation getTLModelObject() {
		return (TLOperation) modelObject.getTLModelObj();
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.OPERATION;
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new OperationNode(new TLOperation()));
	}

	@Override
	public boolean isDeleteable() {
		return getParent().isDeleteable();
	}

	@Override
	public boolean isExtensible() {
		return getTLModelObject() != null ? !getTLModelObject().isNotExtendable() : false;
	}

	@Override
	public boolean isRenameable() {
		return true;
	}

	// @Override
	// public boolean isExtensibleObject() {
	// return true;
	// }

	@Override
	public boolean isNamedEntity() {
		return false;
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			getTLModelObject().setNotExtendable(!extensible);
		return this;
	}

	@Override
	public String getLabel() {
		return NodeNameUtils.OPERATION_PREFIX + getName();
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixOperationName(name));
	}

	@Override
	public List<Node> getChildren_TypeUsers() {
		ArrayList<Node> users = new ArrayList<Node>();
		for (Node facet : getChildren()) {
			users.addAll(facet.getChildren_TypeUsers());
		}
		return users;
	}

}

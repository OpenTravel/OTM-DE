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
package org.opentravel.schemas.node.objectMembers;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.handlers.children.OperationChildrenHandler;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.types.TypeProvider;

/**
 * Service Operations. Operations own 3 facets: Request, Response and Notification
 * 
 * @author Dave Hollander
 * 
 */
public class OperationNode extends NonTypeProviders implements VersionedObjectInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(OperationNode.class);

	/**
	 * List of operations used in CRUD resource services.
	 */
	public enum ServiceOperationTypes {
		CREATE("Create"), DELETE("Delete"), QUERY("Query"), READ("Read"), UPDATE("Update");

		public String displayName;

		private ServiceOperationTypes(String displayName) {
			this.displayName = displayName;
		}
	}

	/**
	 * Create a Service Operation for the passed service. Creates RQ, RS and Notif. Adds new operation to the TL Model.
	 * Messages complete with unassigned Subject element.
	 * 
	 * @param name
	 *            of the operation
	 */
	public OperationNode(ServiceNode svc, String name) {
		this(new TLOperation());

		this.setName(name);
		setExtensible(true);
		svc.getTLModelObject().addOperation(getTLModelObject());
		svc.add(this); // Add to children handler
		parent = svc;

		// FIXME - Something to do with new minor libraries adding operations.
		if (!svc.getLibrary().isEditable())
			assert false;
		// TODO - why is this creating services?
		// LibraryNode head = null;
		// if (svc.getLibrary() != null && svc.getLibrary().getChain() != null)
		// head = svc.getLibrary().getChain().getHead();
		//
		// // If there is no service in the head library then create one.
		// if (head != null && svc.getLibrary().isManaged()) {
		// // LOGGER.debug("Managed service is not in version head library! " + svc.isInHead());
		// if (head.getServiceRoot() == null) {
		// svc = new ServiceNode(head);
		// svc.setName(svcName); // return;
		// } else
		// svc = (ServiceNode) head.getServiceRoot();
		// }

		assert (getTLModelObject() instanceof TLOperation);
	}

	/**
	 * Create a Service Operation as a facet node of the passed service. Creates RQ, RS and Notif Messages complete with
	 * Subject element.
	 * 
	 * @param service
	 * @param name
	 *            of the operation
	 * @param type
	 * @param businessObject
	 *            the business object to assign to the messages
	 */
	public OperationNode(final ServiceNode service, String name, ServiceOperationTypes type,
			BusinessObjectNode businessObject) {
		this(service, name);
		if (businessObject == null)
			return;

		// For each of operations, the RQ, RS and Notif children, create elements for each CRUD+Q message.
		for (Node n : getChildren()) {
			assert n instanceof FacetOMNode;
			FacetOMNode fn = (FacetOMNode) n;
			ElementNode newEle = new ElementNode(fn, businessObject.getName());
			assert fn.contains(newEle);
			switch (type) {
			case CREATE:
				set(newEle, fn.getTLModelObject().getFacetType(), businessObject, businessObject,
						businessObject.getFacet_ID());
				break;
			case DELETE:
				set(newEle, fn.getTLModelObject().getFacetType(), businessObject, businessObject.getFacet_ID(),
						businessObject);
				break;
			case READ:
				set(newEle, fn.getTLModelObject().getFacetType(), businessObject.getFacet_ID(), businessObject,
						businessObject);
				break;
			case UPDATE:
				set(newEle, fn.getTLModelObject().getFacetType(), businessObject, businessObject.getFacet_ID(),
						businessObject);
				break;
			case QUERY:
				AbstractContextualFacet queryRq = null;
				if (businessObject.getQueryFacets().size() > 0) {
					// Find the query facet that matches the name parameter
					for (Node qf : businessObject.getQueryFacets())
						if (qf.getLabel().equals(name))
							queryRq = (AbstractContextualFacet) qf;
					set(newEle, fn.getTLModelObject().getFacetType(), queryRq, businessObject,
							businessObject.getFacet_ID());
				}
				break;
			}
		}
	}

	/**
	 * Model the TLOperation and all of its children. Does <b>not</b> set parent or library. Use node factory to model
	 * with parent and library.
	 * 
	 * @param tlObj
	 */
	public OperationNode(TLOperation tlObj) {
		super(tlObj);
		childrenHandler = new OperationChildrenHandler(this);
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	public void add(OperationFacetNode fn) {
		getChildrenHandler().add(fn);
	}

	public void remove(OperationFacetNode fn) {
		getChildrenHandler().remove(fn);
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new OperationNode(new TLOperation()));
	}

	@Override
	public void deleteTL() {
		getTLModelObject().getOwningService().removeOperation(getTLModelObject());
	}

	@Override
	public OperationChildrenHandler getChildrenHandler() {
		return (OperationChildrenHandler) childrenHandler;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.OPERATION;
	}

	@Override
	public String getComponentType() {
		return ComponentNodeType.OPERATION.toString();
		// return "Operation";
	}

	@Override
	public TLFacetType getFacetType() {
		return null;
	}

	@Override
	public String getLabel() {
		return NodeNameUtils.OPERATION_PREFIX + getName();
	}

	@Override
	public String getName() {
		return getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public TLOperation getTLModelObject() {
		return (TLOperation) tlObj;
	}

	@Override
	public boolean isDeleteable() {
		return getParent() != null ? getParent().isDeleteable() : false;
	}

	@Override
	public boolean isExtensible() {
		return getTLModelObject() != null ? !getTLModelObject().isNotExtendable() : false;
	}

	@Override
	public boolean isNamedEntity() {
		return false;
	}

	@Override
	public boolean isRenameable() {
		return true;
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			getTLModelObject().setNotExtendable(!extensible);
		return this;
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixOperationName(name));
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

}

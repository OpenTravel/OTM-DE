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
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemas.modelObject.ModelObjectFactory;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeVisitors;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

/**
 * Service Operations.
 * 
 * @author Dave Hollander
 * 
 */
public class OperationNode extends FacetNode implements VersionedObjectInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(OperationNode.class);
	public static final String OPERATION_PREFIX = "Operation: ";

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
		// setIdentity("Operation:" + getName());
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new OperationNode(new TLOperation()));
	}

	@Override
	public boolean isDetailListFacet() {
		return false;
	}

	@Override
	public boolean isDeleteable() {
		return getParent().isDeleteable();
	}

	@Override
	protected boolean isNavChild() {
		return true;
	}

	@Override
	public boolean isDefaultFacet() {
		return false;
	}

	@Override
	public boolean isExtensible() {
		return getTLModelObject() != null ? !((TLOperation) getTLModelObject()).isNotExtendable() : false;
	}

	/**
	 * @return true if this facet is renameable.
	 */
	@Override
	public boolean isRenameable() {
		return true;
	}

	@Override
	public boolean isExtensibleObject() {
		return true;
	}

	@Override
	public boolean isTypeProvider() {
		return false;
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			((TLOperation) getTLModelObject()).setNotExtendable(!extensible);
		return this;
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

		((TLService) svc.modelObject.getTLModelObj()).addOperation((TLOperation) this.getTLModelObject());
		this.setName(name);
		// setIdentity("Operation:" + getName());
		setExtensible(true);
		svc.linkChild(this);

		// Create Messages from those in the new TLOperation
		for (final Object msg : modelObject.getChildren()) {
			final FacetNode fn = new FacetNode((TLFacet) msg);
			this.linkChild(fn);
			new ElementNode(fn, "");
		}
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
	public OperationNode(final ServiceNode service, String name, ResourceOperationTypes type, Node subject) {
		if (subject == null)
			return;
		if (!(subject instanceof BusinessObjectNode))
			return;
		BusinessObjectNode businessObject = (BusinessObjectNode) subject;

		final TLOperation tlo = new TLOperation();
		((TLService) service.getTLModelObject()).addOperation(tlo);
		tlo.setOwningService(((TLService) service.getTLModelObject()));
		modelObject = ModelObjectFactory.newModelObject(tlo, this);
		this.setName(name);
		// setIdentity("Operation:" + getName());
		setExtensible(true);
		service.linkChild(this);

		for (final Object msg : modelObject.getChildren()) {
			final FacetNode fn = new FacetNode((TLFacet) msg);
			this.linkChild(fn);
			final TypeUser np = new ElementNode(fn, subject.getName());
			TLFacet tlf = (TLFacet) ((TLProperty) ((Node) np).getTLModelObject()).getOwner();
			switch (type) {
			case CREATE:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType((TypeProvider) businessObject.getIDFacet());
				break;
			case DELETE:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType((TypeProvider) businessObject.getIDFacet());
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType(businessObject);
				break;
			case QUERY:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST)) {
					np.setAssignedType((TypeProvider) ModelNode.getEmptyNode());
					if (businessObject.getQueryFacets().size() > 0) {
						// Find the query facet that matches the name parameter
						for (Node qf : businessObject.getQueryFacets())
							if (qf.getLabel().equals(name))
								np.setAssignedType((TypeProvider) qf);
					}
				} else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType((TypeProvider) businessObject.getIDFacet());
				break;
			case READ:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST))
					np.setAssignedType((TypeProvider) businessObject.getIDFacet());
				else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType(businessObject);
				break;
			case UPDATE:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType((TypeProvider) businessObject.getIDFacet());
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType(businessObject);
				break;
			}
			((Node) np).visitAllNodes(new NodeVisitors().new FixNames());
		}
	}

	@Override
	public String getLabel() {
		return OPERATION_PREFIX + getName();
	}

	@Override
	public void setName(String name) {
		// super.setName(n, false);

		// Strip the object name and "Operation: " string if present.
		if (name.startsWith(OPERATION_PREFIX))
			name = name.substring(OPERATION_PREFIX.length());
		if (getModelObject() != null) {
			((TLOperation) getTLModelObject()).setName(name);
		}
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.OPERATION;
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

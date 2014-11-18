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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemas.modelObject.ModelObjectFactory;
import org.opentravel.schemas.node.properties.ElementNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service Operations.
 * 
 * @author Dave Hollander
 * 
 */
public class OperationNode extends FacetNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(OperationNode.class);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.FacetNode#isListFacet()
	 */
	@Override
	public boolean isListFacet() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.FacetNode#isMessage()
	 */
	@Override
	public boolean isMessage() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.FacetNode#isDetailListFacet()
	 */
	@Override
	public boolean isDetailListFacet() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.FacetNode#isCustomFacet()
	 */
	@Override
	public boolean isCustomFacet() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.FacetNode#isQueryFacet()
	 */
	@Override
	public boolean isQueryFacet() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.FacetNode#isDefaultFacet()
	 */
	@Override
	public boolean isDefaultFacet() {
		return false;
	}

	@Override
	public boolean isExtensible() {
		return getTLModelObject() != null ? !((TLOperation) getTLModelObject()).isNotExtendable() : false;
	}

	@Override
	public boolean isExtensibleObject() {
		return true;
	}

	@Override
	public Node setExtensible(boolean extensible) {
		((TLOperation) getTLModelObject()).setNotExtendable(!extensible);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.FacetNode#isRoleFacet()
	 */
	@Override
	public boolean isRoleFacet() {
		return false;
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

		// This won't work -- svc node is always in head()
		// This code creates an operation on the TLService...but that operation is not saved.
		if (svc.getLibrary().isManaged() && !svc.isInHead()) {
			LOGGER.debug("Managed service is not in version head library!");
			return;
		}
		((TLService) svc.modelObject.getTLModelObj()).addOperation((TLOperation) this.getTLModelObject());
		this.setName(name, false);
		setExtensible(true);
		svc.linkChild(this, false);

		// Create Messages from those in the new TLOperation
		for (final Object msg : modelObject.getChildren()) {
			final FacetNode fn = new FacetNode((TLFacet) msg);
			this.linkChild(fn, false);
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
		this.setName(name, false);
		setExtensible(true);
		service.linkChild(this, false);

		for (final Object msg : modelObject.getChildren()) {
			final FacetNode fn = new FacetNode((TLFacet) msg);
			this.linkChild(fn, false);
			final Node np = new ElementNode(fn, subject.getName());
			TLFacet tlf = (TLFacet) ((TLProperty) np.getTLModelObject()).getPropertyOwner();
			switch (type) {
			case CREATE:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType(businessObject.getIDFacet());
				break;
			case DELETE:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType(businessObject.getIDFacet());
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType(businessObject);
				break;
			case QUERY:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST)) {
					np.setAssignedType((Node) ModelNode.getEmptyNode());
					if (businessObject.getQueryFacets().size() > 0) {
						// Find the query facet that matches the name parameter
						for (Node qf : businessObject.getQueryFacets())
							if (qf.getLabel().equals(name))
								np.setAssignedType(qf);
					}
				} else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType(businessObject.getIDFacet());
				break;
			case READ:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST))
					np.setAssignedType(businessObject.getIDFacet());
				else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType(businessObject);
				break;
			case UPDATE:
				if (tlf.getFacetType().equals(TLFacetType.REQUEST))
					np.setAssignedType(businessObject);
				else if (tlf.getFacetType().equals(TLFacetType.RESPONSE))
					np.setAssignedType(businessObject.getIDFacet());
				else if (tlf.getFacetType().equals(TLFacetType.NOTIFICATION))
					np.setAssignedType(businessObject);
				break;
			}
			np.visitAllNodes(new NodeVisitors().new FixNames());
		}
	}

	// @Override
	// public boolean isOperation() {
	// if (!(getTLModelObject() instanceof TLOperation))
	// throw new IllegalStateException("Operation " + this
	// + " does not have TLOperation model object.");
	// return true;
	// }

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
	public List<Node> getChildren_TypeUsers() {
		ArrayList<Node> users = new ArrayList<Node>();
		for (Node facet : getChildren()) {
			users.addAll(facet.getChildren_TypeUsers());
		}
		return users;
	}

}

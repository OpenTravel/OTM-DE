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
package org.opentravel.schemas.modelObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.controllers.DefaultModelController;
import org.opentravel.schemas.modelObject.events.OwnershipEventListener;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessObjMO extends ModelObject<TLBusinessObject> {

	public enum Events {
		FACET_ADDED, FACET_REMOVED, FACET_UPDATED
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjMO.class);
	List<PropertyChangeListener> listeners = new LinkedList<PropertyChangeListener>();
	private InheritedFacetListener inheritedFacetListener;

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	private void firePropertyChange(Events propertyName, Object oldValue, Object newValue) {
		for (PropertyChangeListener l : listeners) {
			l.propertyChange(new PropertyChangeEvent(this, propertyName.toString(), oldValue, newValue));
		}
	}

	public BusinessObjMO(final TLBusinessObject obj) {
		super(obj);
		addBaseListener();
		addInheritedFacets();
	}

	private void addBaseListener() {
		if (getTLBase() != null) {
			DefaultModelController modelC = (DefaultModelController) OtmRegistry.getMainController()
					.getModelController();
			inheritedFacetListener = new InheritedFacetListener((TLBusinessObject) getTLBase());
			modelC.addSourceListener(inheritedFacetListener);
		}
	}

	private void removeBaseListener() {
		if (getTLBase() != null) {
			if (inheritedFacetListener == null) {
				throw new IllegalStateException("Extended BO has to listen on changes on base type.");
			}
			DefaultModelController modelC = (DefaultModelController) OtmRegistry.getMainController()
					.getModelController();
			modelC.removeSourceListener(inheritedFacetListener);
			inheritedFacetListener = null;
		}
	}

	class InheritedFacetListener extends OwnershipEventListener<TLBusinessObject, TLFacet> {

		public InheritedFacetListener(TLBusinessObject source) {
			super(source);
		}

		@Override
		public void processModelEvent(OwnershipEvent<TLBusinessObject, TLFacet> event) {
			switch (event.getType()) {
			case CUSTOM_FACET_REMOVED:
			case QUERY_FACET_REMOVED:
				removeInheritedFacet(event.getAffectedItem());
				return;
			case CUSTOM_FACET_ADDED:
			case QUERY_FACET_ADDED:
				updateFacet(event.getAffectedItem());
				return;
			default:
				break;
			}
		}

	};

	@Override
	public void addAlias(final TLAlias tla) {
		srcObj.addAlias(tla);
	}

	public void updateFacet(TLFacet affectedItem) {
		TLFacet exist = findFacet(affectedItem.getLabel(), affectedItem.getContext(), affectedItem.getFacetType());
		if (exist == null) {
			addFacet(affectedItem);
		} else {
			firePropertyChange(Events.FACET_UPDATED, null, exist);
		}

	}

	protected void removeInheritedFacet(TLFacet affectedItem) {
		if (TLFacetType.CUSTOM.equals(affectedItem.getFacetType())) {
			TLFacet removedFacet = getTLModelObj().getCustomFacet(affectedItem.getContext(), affectedItem.getLabel());
			if (removedFacet != null && !isNotEmpty(removedFacet)) {
				getTLModelObj().removeCustomFacet(removedFacet);
				firePropertyChange(Events.FACET_REMOVED, affectedItem, null);
			}
		} else if (TLFacetType.QUERY.equals(affectedItem.getFacetType())) {
			TLFacet removedFacet = getTLModelObj().getQueryFacet(affectedItem.getContext(), affectedItem.getLabel());
			if (removedFacet != null && !isNotEmpty(removedFacet)) {
				getTLModelObj().removeQueryFacet(removedFacet);
				firePropertyChange(Events.FACET_REMOVED, removedFacet, null);
			}
		} else {
			throw new RuntimeException("Type is not supported: + " + affectedItem.getFacetType()
					+ ". Can only add custom or query facet.");
		}
	}

	public TLFacet addFacet(String name, String context, TLFacetType type) {
		TLFacet newFacet = createFacet(name, context, type);
		if (TLFacetType.CUSTOM.equals(newFacet.getFacetType())) {
			addCustomFacet(newFacet);
		} else if (TLFacetType.QUERY.equals(newFacet.getFacetType())) {
			addQueryFacet(newFacet);
		} else {
			throw new RuntimeException("Type is not supported: + " + newFacet.getFacetType()
					+ ". Can only add custom or query facet.");
		}
		return newFacet;
	}

	private TLFacet findFacet(String name, String context, TLFacetType type) {
		if (TLFacetType.CUSTOM.equals(type)) {
			return getTLModelObj().getCustomFacet(context, name);
		} else if (TLFacetType.QUERY.equals(type)) {
			return getTLModelObj().getQueryFacet(context, name);
		}
		return null;
	}

	public TLFacet addFacet(TLFacet facet) {
		TLFacet newFacet = addFacet(facet.getLabel(), facet.getContext(), facet.getFacetType());
		firePropertyChange(Events.FACET_ADDED, null, newFacet);
		return newFacet;
	}

	private TLFacet createFacet(String name, String context, TLFacetType type) {
		TLFacet tf = new TLFacet();
		tf.setLabel(name);
		tf.setContext(context);
		tf.setFacetType(type);
		tf.setOwningEntity(getTLModelObj());
		return tf;
	}

	@Override
	public TLFacet addFacet(TLFacetType type) {
		final TLFacet tlf = new TLFacet();
		switch (type) {
		case QUERY:
			srcObj.addQueryFacet(tlf);
			break;
		case CUSTOM:
			srcObj.addCustomFacet(tlf);
			break;
		default:
			throw new IllegalArgumentException("Only the following types are supported for new facets: "
					+ TLFacetType.QUERY + ", " + TLFacetType.CUSTOM);
		}
		return tlf;
	}

	public void addCustomFacet(final TLFacet tlf) {
		srcObj.addCustomFacet(tlf);
		LOGGER.info("Added custom facet " + tlf.getLocalName() + " to BusinessObject " + this.getName());
	}

	public void addQueryFacet(final TLFacet tlf) {
		srcObj.addQueryFacet(tlf);
		LOGGER.info("Added query facet " + tlf.getLocalName() + " to BusinessObject " + this.getName());
	}

	// It may already have been taken out of the library, but if not do so.
	@Override
	public void delete() {
		if (getTLModelObj().getOwningLibrary() != null)
			getTLModelObj().getOwningLibrary().removeNamedMember(getTLModelObj());
		listeners.clear();
		removeBaseListener();
	}

	@Override
	public List<?> getChildren() {
		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		kids.add(getTLModelObj().getIdFacet());
		kids.add(getTLModelObj().getSummaryFacet());
		kids.add(getTLModelObj().getDetailFacet());
		kids.addAll(getTLModelObj().getQueryFacets());
		kids.addAll(getTLModelObj().getCustomFacets());
		kids.addAll(getTLModelObj().getAliases());
		return kids;
	}

	private void addFacets(List<TLFacet> inheritedFacet) {
		for (TLFacet f : inheritedFacet) {
			addFacet(f);
		}
	}

	public List<TLFacet> getInheritedFacet(TLFacetType type) {
		return FacetCodegenUtils.findGhostFacets(getTLModelObj(), type);
	}

	@Override
	public String getName() {
		return getTLModelObj().getName();
	}

	@Override
	public String getNamespace() {
		return getTLModelObj().getNamespace();
	}

	@Override
	public String getNamePrefix() {
		final TLLibrary lib = (TLLibrary) getLibrary(getTLModelObj());
		return lib == null ? "" : lib.getPrefix();
	}

	@Override
	public NamedEntity getTLBase() {
		return srcObj.getExtension() != null ? srcObj.getExtension().getExtendsEntity() : null;
	}

	@Override
	public TLBusinessObject getTLModelObj() {
		return srcObj;
	}

	@Override
	protected AbstractLibrary getLibrary(final TLBusinessObject obj) {
		return obj.getOwningLibrary();
	}

	// @Override
	// public boolean isComplexAssignable() {
	// return true;
	// }

	/**
	 * @see org.opentravel.schemas.modelObject.ModelObject#getExtendsType()
	 */
	@Override
	public String getExtendsType() {
		TLExtension tlExtension = getTLModelObj().getExtension();
		String extendsTypeName = "";

		if (tlExtension != null) {
			if (tlExtension.getExtendsEntity() != null)
				extendsTypeName = tlExtension.getExtendsEntity().getLocalName();
			else
				extendsTypeName = "--base type can not be found--";
		}
		return extendsTypeName;
	}

	@Override
	public String getExtendsTypeNS() {
		TLExtension tlExtension = getTLModelObj().getExtension();
		String extendsNS;

		if ((tlExtension != null) && (tlExtension.getExtendsEntity() != null)) {
			extendsNS = tlExtension.getExtendsEntity().getNamespace();
		} else {
			extendsNS = "";
		}
		return extendsNS;
	}

	@Override
	public boolean isExtendedBy(NamedEntity extension) {
		if (extension == null || !(extension instanceof TLBusinessObject))
			return false;
		if (extension.getValidationIdentity() == null)
			return false;

		if (getTLModelObj() != null)
			if (getTLModelObj().getExtension() != null)
				if (getTLModelObj().getExtension().getValidationIdentity() != null)
					return getTLModelObj().getExtension().getExtendsEntity() == extension;
		return false;
	}

	/**
	 * @see org.opentravel.schemas.modelObject.ModelObject#setExtendsType(org.opentravel.schemas.modelObject.ModelObject)
	 */
	@Override
	public void setExtendsType(ModelObject<?> mo) {
		removeBaseListener();
		removeInheritedFacets();
		// Throw an extension ownership event for the removal before setting
		if (mo == null)
			getTLModelObj().setExtension(null);
		else {
			TLExtension tlExtension = getTLModelObj().getExtension();
			if (tlExtension == null) {
				tlExtension = new TLExtension();
				getTLModelObj().setExtension(tlExtension);
			}
			tlExtension.setExtendsEntity((NamedEntity) mo.getTLModelObj());
			addBaseListener();
			addInheritedFacets();
		}
	}

	private void addInheritedFacets() {
		addFacets(getInheritedFacet(TLFacetType.QUERY));
		addFacets(getInheritedFacet(TLFacetType.CUSTOM));
	}

	private void removeInheritedFacets() {
		List<TLFacet> customs = findInheritedFacets(getTLModelObj(), TLFacetType.CUSTOM);
		for (TLFacet c : customs) {
			TLFacet cc = getTLModelObj().getCustomFacet(c.getContext(), c.getLabel());
			if (cc != null)
				removeInheritedFacet(cc);
		}
		List<TLFacet> queres = findInheritedFacets(getTLModelObj(), TLFacetType.QUERY);
		for (TLFacet q : queres) {
			TLFacet qq = getTLModelObj().getCustomFacet(q.getContext(), q.getLabel());
			if (qq != null)
				removeInheritedFacet(qq);
		}

	}

	private boolean isNotEmpty(TLFacet facet) {
		boolean ret = !facet.getAliases().isEmpty();
		ret = ret || !facet.getAttributes().isEmpty();
		ret = ret || !facet.getElements().isEmpty();
		ret = ret || (facet.getDocumentation() != null ? !facet.getDocumentation().isEmpty() : false);
		ret = ret || !facet.getIndicators().isEmpty();
		return ret;
	}

	public List<TLFacet> findInheritedFacets(TLFacetOwner facetOwner, TLFacetType facetType) {
		Set<String> inheritedFacetNames = new HashSet<String>();
		List<TLFacet> inheritedFacets = new ArrayList<TLFacet>();
		TLFacetOwner extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
		Set<TLFacetOwner> visitedOwners = new HashSet<TLFacetOwner>();

		// Find all of the inherited facets of the specified facet type
		while (extendedOwner != null) {
			List<TLFacet> facetList = FacetCodegenUtils.getAllFacetsOfType(extendedOwner, facetType);

			for (TLFacet facet : facetList) {
				String facetKey = facetType.getIdentityName(facet.getContext(), facet.getLabel());

				if (!inheritedFacetNames.contains(facetKey)) {
					inheritedFacetNames.add(facetKey);
					inheritedFacets.add(facet);
				}
			}
			visitedOwners.add(extendedOwner);
			extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(extendedOwner);

			if (visitedOwners.contains(extendedOwner)) {
				break; // exit if we encounter a circular reference
			}
		}

		List<TLFacet> ghostFacets = new ArrayList<TLFacet>();

		for (TLFacet inheritedFacet : inheritedFacets) {
			TLFacet ghostFacet = new TLFacet();
			ghostFacet.setFacetType(facetType);
			ghostFacet.setContext(inheritedFacet.getContext());
			ghostFacet.setLabel(inheritedFacet.getLabel());
			ghostFacet.setOwningEntity(facetOwner);
			ghostFacets.add(ghostFacet);
		}
		return ghostFacets;
	}

	@Override
	public String getComponentType() {
		return "Business Object";
	}

	@Override
	public boolean setName(final String name) {
		getTLModelObj().setName(name);
		return true;
	}
}

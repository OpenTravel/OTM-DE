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

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemas.node.interfaces.INode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelObjectFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelObjectFactory.class);

	/**
	 * Class factory for the Source Object class with generic TL type Editable, node and assigned type are set by the
	 * object constructors.
	 * 
	 * @param obj
	 *            - the object to model.
	 * @param node
	 *            - the node that represents this model object
	 * @return - returns a object in the MO class
	 */
	@SuppressWarnings({ "unchecked" })
	public static <TL> ModelObject<TL> newModelObject(final TL obj, final INode node) {

		ModelObject<TL> ret = null;
		// Top level classes
		if (obj instanceof AbstractLibrary) {
			ret = (ModelObject<TL>) new LibraryMO((AbstractLibrary) obj);
		} else if (obj instanceof TLModel) {
			ret = (ModelObject<TL>) new TLModelMO((TLModel) obj);
		} else if (obj instanceof TLProject) {
			ret = (ModelObject<TL>) new ProjectMO((TLProject) obj);
		} else if (obj instanceof XSDComplexType) {
			ret = (ModelObject<TL>) new XSDComplexMO((XSDComplexType) obj);
		} else if (obj instanceof XSDSimpleType) {
			ret = (ModelObject<TL>) new XSDSimpleMO((XSDSimpleType) obj);
		} else if (obj instanceof XSDElement) {
			ret = (ModelObject<TL>) new XSDElementMO((XSDElement) obj);
		} else if (obj instanceof TLClosedEnumeration) {
			ret = (ModelObject<TL>) new ClosedEnumMO((TLClosedEnumeration) obj);
		} else if (obj instanceof TLOpenEnumeration) {
			ret = (ModelObject<TL>) new OpenEnumMO((TLOpenEnumeration) obj);
		} else if (obj instanceof TLValueWithAttributes) {
			ret = (ModelObject<TL>) new ValueWithAttributesMO((TLValueWithAttributes) obj);
		} else if (obj instanceof TLSimple) {
			ret = (ModelObject<TL>) new SimpleMO((TLSimple) obj);
		} else if (obj instanceof TLCoreObject) {
			ret = (ModelObject<TL>) new CoreObjectMO((TLCoreObject) obj);
		} else if (obj instanceof TLBusinessObject) {
			ret = (ModelObject<TL>) new BusinessObjMO((TLBusinessObject) obj);
		} else if (obj instanceof TLChoiceObject) {
			ret = (ModelObject<TL>) new ChoiceObjMO((TLChoiceObject) obj);
		} else if (obj instanceof TLService) {
			ret = (ModelObject<TL>) new ServiceMO((TLService) obj);
		} else if (obj instanceof TLResource) {
			ret = (ModelObject<TL>) new ResourceMO((TLResource) obj);
		} else if (obj instanceof TLExtensionPointFacet) {
			ret = (ModelObject<TL>) new ExtensionPointFacetMO((TLExtensionPointFacet) obj);
		} else if (obj instanceof TLEnumValue) {
			ret = (ModelObject<TL>) new EnumLiteralMO((TLEnumValue) obj);
		} else if (obj instanceof TLAttribute) {
			ret = (ModelObject<TL>) new AttributeMO((TLAttribute) obj);
		} else if (obj instanceof TLnSimpleAttribute) {
			ret = (ModelObject<TL>) new SimpleAttributeMO((TLnSimpleAttribute) obj);
		} else if (obj instanceof TLIndicator) {
			ret = (ModelObject<TL>) new IndicatorMO((TLIndicator) obj);
		} else if (obj instanceof TLEmpty) {
			// Empty extends property so must be tested first.
			ret = (ModelObject<TL>) new EmptyMO((TLEmpty) obj);
		} else if (obj instanceof TLProperty) {
			ret = (ModelObject<TL>) new ElementPropertyMO((TLProperty) obj);
		} else if (obj instanceof TLListFacet) {
			ret = (ModelObject<TL>) new ListFacetMO((TLListFacet) obj);
		} else if (obj instanceof TLnValueWithAttributesFacet) {
			ret = (ModelObject<TL>) new ValueWithAttributesAttributeFacetMO((TLnValueWithAttributesFacet) obj);
		} else if (obj instanceof TLSimpleFacet) {
			ret = (ModelObject<TL>) new SimpleFacetMO((TLSimpleFacet) obj);
		} else if (obj instanceof TLFacet) {
			ret = (ModelObject<TL>) new FacetMO((TLFacet) obj);
		} else if (obj instanceof TLOperation) {
			ret = (ModelObject<TL>) new OperationMO((TLOperation) obj);
		} else if (obj instanceof TLAlias) {
			ret = (ModelObject<TL>) new AliasMO((TLAlias) obj);
		} else if (obj instanceof TLRoleEnumeration) {
			ret = (ModelObject<TL>) new RoleEnumerationMO((TLRoleEnumeration) obj);
		} else if (obj instanceof TLRole) {
			ret = (ModelObject<TL>) new RolePropertyMO((TLRole) obj);
		} else if (obj == null) {
			ret = (ModelObject<TL>) new EmptyMO(new TLEmpty());
		} else {
			ret = (ModelObject<TL>) new EmptyMO(new TLEmpty());
			LOGGER.error("ModelObjectFactory - EmptyMO assigned to unknown TL type: " + obj.getClass().getSimpleName());
		}
		ret.node = node;

		return ret;
	}
}

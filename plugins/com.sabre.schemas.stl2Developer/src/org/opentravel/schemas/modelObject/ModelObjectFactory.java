/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import org.opentravel.schemas.node.INode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLRoleEnumeration;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDComplexType;
import com.sabre.schemacompiler.model.XSDElement;
import com.sabre.schemacompiler.model.XSDSimpleType;

public class ModelObjectFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelObjectFactory.class);

    /**
     * Class factory for the Source Object class with generic TL type Editable, node and assigned
     * type are set by the object constructors.
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
        } else if (obj instanceof TLService) {
            ret = (ModelObject<TL>) new ServiceMO((TLService) obj);
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
        } else if (obj instanceof TLValueWithAttributesFacet) {
            ret = (ModelObject<TL>) new ValueWithAttributesAttributeFacetMO(
                    (TLValueWithAttributesFacet) obj);
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
            LOGGER.error("ModelObjectFactory - EmptyMO assigned to unknown TL type: "
                    + obj.getClass().getSimpleName());
        }
        ret.node = node;

        return ret;
    }

}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.modelObject.BusinessObjMO;
import org.opentravel.schemas.modelObject.ModelObjectFactory;
import org.opentravel.schemas.modelObject.RoleEnumerationMO;
import org.opentravel.schemas.modelObject.TLValueWithAttributesFacet;
import org.opentravel.schemas.modelObject.ValueWithAttributesAttributeFacetMO;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.utils.StringComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimpleFacet;

/**
 * Facets are containers for properties (elements and attributes) as well as simple facets for core
 * and VWA objects. Operation RQ, RS and Notif messages are also modeled with facets. (See library
 * sorter for all the variations in facet types.)
 * 
 * @author Dave Hollander
 * 
 */
public class FacetNode extends ComponentNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

    public FacetNode() {
    }

    public FacetNode(final TLFacet obj) {
        super(obj);
    }

    public FacetNode(final TLOperation obj) {
        super(obj);
    }

    public FacetNode(final TLSimpleFacet obj) {
        super(obj);
    }

    public FacetNode(final TLRoleEnumeration obj) {
        super(obj);
    }

    public FacetNode(final TLValueWithAttributesFacet obj) {
        super(obj);
    }

    public FacetNode(final TLListFacet obj) {
        super(obj);
        // setName(obj.getLocalName());
    }

    /**
     * Make a copy of all the properties of the source facet and add to this facet. If the property
     * is of the wrong type, it is changed into an attribute first.
     * 
     * @param sourceFacet
     */
    public void copyFacet(FacetNode sourceFacet) {
        PropertyNode newProperty = null;
        for (Node p : sourceFacet.getChildren()) {
            if (p instanceof PropertyNode) {
                PropertyNode property = (PropertyNode) p;
                newProperty = (PropertyNode) property.clone(null, null);
                this.linkChild(newProperty, false); // must have parent for test and change
                                                    // to work
                if (!this.isValidParentOf(newProperty.getPropertyType()))
                    newProperty = newProperty.changePropertyRole(PropertyNodeType.ATTRIBUTE);
                modelObject.addChild(newProperty.getTLModelObject());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.ComponentNode#addProperties(java.util.List)
     */
    @Override
    public void addProperties(List<Node> properties, boolean clone) {
        boolean attrsOnly = false;
        Node np;
        if (getParent().isValueWithAttributes())
            attrsOnly = true;
        for (Node p : properties) {
            np = p;
            if (clone) {
                // don't want to add to parent
                np = p.clone(null, null);
            }
            if (attrsOnly && p.isSimpleAssignable())
                addProperty(np);
            else
                addProperty(np);
        }
    }

    /**
     * Create a custom facet. Custom facets are the only facets named by the user.
     * 
     * @param facetType
     *            currently either QUERY or CUSTOM
     */
    public FacetNode(final ComponentNode parent, final String name, final String context,
            final TLFacetType facetType) {
        if (parent.isBusinessObject()) {
            final TLFacet tlf = new TLFacet();
            tlf.setContext(context);
            updateModel(tlf, parent, facetType);
            modelObject = ModelObjectFactory.newModelObject(tlf, parent);
            if (name == null || name.isEmpty())
                modelObject.setName(context);
            else
                modelObject.setName(name);

            // description = modelObject.getDescriptionDoc();
            parent.linkChild(this, false);
            this.addMOChildren();
        }
    }

    private void updateModel(final TLFacet tlf, final ComponentNode parent,
            final TLFacetType facetType) {
        switch (facetType) {
            case QUERY:
                ((BusinessObjMO) parent.getModelObject()).addQueryFacet(tlf);
                break;
            case CUSTOM:
                ((BusinessObjMO) parent.getModelObject()).addCustomFacet(tlf);
                break;
            default:
                throw new IllegalArgumentException(
                        "Only the following types are supported for new FacetNode: "
                                + TLFacetType.QUERY + ", " + TLFacetType.CUSTOM);
        }
    }

    /**
     * Remove the properties in the list from this node and underlying tl model object. Use to move
     * the property to a different facet.
     * 
     * @param property
     */
    @Override
    public void removeProperty(final Node property) {
        ((PropertyNode) property).removeProperty();
    }

    public void setContext(final String context) {
        final Object ne = modelObject.getTLModelObj();
        if (ne instanceof TLFacet) {
            ((TLFacet) ne).setContext(context);
        }
    }

    public String getContext() {
        final Object ne = modelObject.getTLModelObj();
        if (ne instanceof TLFacet) {
            return ((TLFacet) ne).getContext();
        }
        return null;
    }

    @Override
    public String getComponentType() {
        return modelObject.getComponentType();
        // return modelObject.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#getOwningComponent()
     */
    @Override
    public Node getOwningComponent() {
        return isExtensionPointFacet() ? this : getParent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isAssignedByReference()
     */
    @Override
    public boolean isAssignedByReference() {
        if (getOwningComponent() == null) {
            LOGGER.equals("No owning component for this facet: " + this);
            return false;
        }
        if (isSimpleListFacet())
            return false;
        return getOwningComponent().isValueWithAttributes() ? false : true;
    }

    @Override
    public boolean isNamedType() {
        return isExtensionPointFacet() ? true : false;
    }

    @Override
    public String getLabel() {
        String label = getModelObject().getLabel();
        if (label.indexOf("-Facet") > 0)
            label = label.substring(0, label.indexOf("-Facet"));
        return label.isEmpty() ? "" : label;
    }

    /**
     * Facets can have aliases which are nav nodes.
     */
    @Override
    public List<Node> getNavChildren() {
        final ArrayList<Node> ret = new ArrayList<Node>();
        for (final Node n : getChildren()) {
            if (n.isAlias()) {
                ret.add(n);
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.INode#hasChildren_TypeProviders()
     */
    @Override
    public boolean hasChildren_TypeProviders() {
        return isXsdType() ? false : true;
    }

    @Override
    public boolean hasNavChildren() {
        for (final Node n : getChildren()) {
            if (n.isAlias()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isListFacet() {
        return getModelObject().getTLModelObj() instanceof TLListFacet;
    }

    @Override
    public boolean isMessage() {
        return getParent() != null && getParent().isOperation();
    }

    @Override
    public boolean isSimpleListFacet() {
        if (!(getTLModelObject() instanceof TLListFacet))
            return false;
        return getName().endsWith(NodeNameUtils.SIMPLE_LIST_SUFFIX);
    }

    public boolean isSummary() {
        return TLFacetType.SUMMARY.equals(getFacetType());
    }

    @Override
    public boolean isDetailListFacet() {
        if (!(getTLModelObject() instanceof TLListFacet))
            return false;
        return getName().endsWith(NodeNameUtils.DETAIL_LIST_SUFFIX);
    }

    @Override
    public boolean isCustomFacet() {
        return ((TLAbstractFacet) getTLModelObject()).getFacetType() == TLFacetType.CUSTOM;
    }

    @Override
    public boolean isQueryFacet() {
        return ((TLAbstractFacet) getTLModelObject()).getFacetType() == TLFacetType.QUERY;
    }

    @Override
    public boolean isDefaultFacet() {
        if (getOwningComponent().isValueWithAttributes())
            return true;
        return ((TLAbstractFacet) getTLModelObject()).getFacetType() == TLFacetType.SUMMARY;
    }

    @Override
    public TLFacetType getFacetType() {
        if (!(getTLModelObject() instanceof TLAbstractFacet)) {
            return null;
        }
        return ((TLAbstractFacet) getTLModelObject()).getFacetType();
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.Facet);
    }

    @Override
    public boolean isVWA_AttributeFacet() {
        return (getModelObject() instanceof ValueWithAttributesAttributeFacetMO);
    }

    public boolean isComplexAssignable() {
        return true;
    }

    /**
     * Facets assigned to core object list types have no model objects but may be page1-assignable.
     */
    @Override
    public boolean isSimpleAssignable() {
        if (isSimpleListFacet())
            return true;
        return false;
    }

    @Override
    public boolean isAssignableToSimple() {
        if (!isSimpleListFacet())
            return false;
        if (getOwningComponent() instanceof CoreObjectNode)
            if (((CoreObjectNode) getOwningComponent()).getSimpleType() != ModelNode.getEmptyNode())
                return true;
        return false;
    }

    @Override
    public boolean isAssignableToVWA() {
        return isAssignableToSimple();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isTypeProvider()
     */
    @Override
    public boolean isTypeProvider() {
        if (getParent() == null)
            return false;
        if (getParent().isValueWithAttributes())
            return false;
        if (isOperation())
            return false;
        if (getParent().isOperation())
            return false;
        return true;
    }

    @Override
    public boolean isAssignable() {
        if (getParent() == null) {
            return false;
        }
        if (getParent().isValueWithAttributes()) {
            return false;
        }
        if (getParent().isOperation()) {
            return false;
        }
        return (isComplexAssignable() || isSimpleAssignable()) ? true : false;
    }

    @Override
    public boolean isRoleFacet() {
        return modelObject instanceof RoleEnumerationMO;
    }

    @Override
    public boolean isValidParentOf(PropertyNodeType type) {
        if (isRoleFacet())
            return type.equals(PropertyNodeType.ROLE);
        if (isVWA_AttributeFacet())
            return type.equals(PropertyNodeType.ATTRIBUTE)
                    || type.equals(PropertyNodeType.INDICATOR);
        if (isListFacet())
            return false;

        switch (type) {
            case ATTRIBUTE:
            case ELEMENT:
            case INDICATOR:
            case INDICATOR_ELEMENT:
            case ID_REFERENCE:
            case ID:
                return true;
        }
        return false;
    }

    @Override
    public boolean isValidParentOf(Node type) {
        if (isRoleFacet())
            return type instanceof RoleNode;
        if (isVWA_AttributeFacet())
            return type.isVWASimpleAssignable();
        if (isListFacet())
            return false;
        if (type instanceof PropertyNode)
            return true;
        return false;
    }

    @Override
    public INode createProperty(final Node type) {
        PropertyNode pn = null;
        if (isVWA_AttributeFacet())
            pn = new AttributeNode(new TLAttribute(), this);
        else
            pn = new ElementNode(new TLProperty(), this);
        pn.setDescription(type.getDescription());
        pn.setAssignedType(type);
        pn.setName(type.getName());
        return pn;
    }

    public boolean hasTheSameContext(final String contextId, final TLFacetType facetType) {
        if (getModelObject() == null || contextId == null || facetType == null) {
            return false;
        }
        final Object tlObject = getModelObject().getTLModelObj();
        final TLLibrary tlLib = (TLLibrary) getLibrary().getTLaLib();
        if (tlObject instanceof TLFacet) {
            final TLFacet facet = (TLFacet) tlObject;
            final TLFacetType thisType = facet.getFacetType();
            final TLContext thisContext = tlLib.getContext(this.getContext());
            if (thisContext != null && contextId.equals(thisContext.getContextId())
                    && thisType.equals(facetType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAliasable() {
        // TODO - this seems wrong. We no longer allow aliases on facets, only objects.
        final Object tlModelObj = getModelObject().getTLModelObj();
        if (getParent().isAliasable()) {
            // business object - all the facades are aliasable
            return true;
        } else if (getParent().isCoreObject()) {
            if (tlModelObj instanceof TLAbstractFacet) {
                final TLAbstractFacet facet = (TLAbstractFacet) tlModelObj;
                if (!(facet instanceof TLListFacet)
                        && (TLFacetType.SUMMARY.equals(facet.getFacetType()) || TLFacetType.DETAIL
                                .equals(facet.getFacetType()))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDeleteable() {
        if (!super.isDeleteable())
            return false;
        return TLFacetType.QUERY.equals(this.getFacetType())
                || TLFacetType.REQUEST.equals(this.getFacetType())
                || TLFacetType.RESPONSE.equals(this.getFacetType())
                || TLFacetType.NOTIFICATION.equals(this.getFacetType())
                || TLFacetType.CUSTOM.equals(this.getFacetType());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#setName(java.lang.String, boolean)
     */
    @Override
    public void setName(String n) {
        if (isOperation()) {
            super.setName(n, false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#setName(java.lang.String, boolean)
     */
    @Override
    public void setName(String n, boolean doFamily) {
        super.setName(n, false); // Facets don't have families
    }

    @Override
    public void sort() {
        Collections.sort(getChildren(), new StringComparator<Node>() {

            @Override
            protected String getString(Node object) {
                return object.getName();
            }
        });
        modelObject.sort();
    }

    /**
     * Add nodes for all TLAliases that do not have nodes. This ONLY adds a node for an existing
     * TLFacet. It does not create a TLFacet.
     * 
     * @param name
     */
    public void updateAliasNodes() {
        if (!(getTLModelObject() instanceof TLFacet))
            return;
        List<String> knownAliases = new ArrayList<String>();
        for (Node n : getChildren()) {
            if (n instanceof AliasNode)
                knownAliases.add(n.getName());
        }
        for (TLAlias tla : ((TLFacet) getTLModelObject()).getAliases()) {
            if (!knownAliases.contains(tla.getName())) {
                new AliasNode(this, tla);
                knownAliases.add(tla.getName());
            }
        }
    }

}

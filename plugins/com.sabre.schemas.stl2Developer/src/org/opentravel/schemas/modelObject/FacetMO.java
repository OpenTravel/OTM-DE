/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemas.controllers.DefaultModelController;
import org.opentravel.schemas.modelObject.events.OwnershipEventListener;
import org.opentravel.schemas.modelObject.events.ValueChangeEventListener;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.utils.StringComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;

public class FacetMO extends ModelObject<TLFacet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacetMO.class);
    private ValueChangeEventListener<TLFacet, String> inheritedFacetListener;
    private OwnershipEventListener<TLFacetOwner, TLFacet> ownershipListener;

    public FacetMO(final TLFacet obj) {
        super(obj);
        addBaseListener();
    }

    public void attachInheritanceListener() {
        removeBaseListener();
        addBaseListener();
    }

    private void addBaseListener() {
        List<TLFacetType> inheritedTypes = Arrays.asList(TLFacetType.CUSTOM, TLFacetType.QUERY);
        if (getTLModelObj().getOwningEntity() instanceof TLBusinessObject
                && inheritedTypes.contains(getTLModelObj().getFacetType())) {
            TLBusinessObject owner = (TLBusinessObject) getTLModelObj().getOwningEntity();
            if (owner.getExtension() != null) {
                TLFacet inheritedFacet = findGhostFacets(getTLModelObj().getOwningEntity(),
                        getTLModelObj());
                if (inheritedFacet != null) {
                    inheritedFacetListener = listenTo(inheritedFacet);
                    ownershipListener = listenToOwner(inheritedFacet.getOwningEntity());
                }
            }
        }
    }

    public boolean isInherited() {
        return inheritedFacetListener != null;
    }

    private void removeBaseListener() {
        if (inheritedFacetListener != null) {
            DefaultModelController modelC = (DefaultModelController) OtmRegistry
                    .getMainController().getModelController();
            modelC.removeSourceListener(inheritedFacetListener);
            inheritedFacetListener = null;
            modelC.removeSourceListener(ownershipListener);
            ownershipListener = null;
        }
    }

    public ValueChangeEventListener<TLFacet, String> listenTo(TLFacet affectedItem) {
        DefaultModelController modelC = (DefaultModelController) OtmRegistry.getMainController()
                .getModelController();
        ValueChangeEventListener<TLFacet, String> listener = new ValueChangeEventListener<TLFacet, String>(
                affectedItem) {

            @Override
            public void processModelEvent(ValueChangeEvent<TLFacet, String> event) {
                setName(event.getNewValue());
            }

            @Override
            public boolean supported(ModelEventType type) {
                return ModelEventType.LABEL_MODIFIED.equals(type);
            }

        };
        modelC.addSourceListener(listener);
        return listener;
    }

    public OwnershipEventListener<TLFacetOwner, TLFacet> listenToOwner(TLFacetOwner owner) {
        DefaultModelController modelC = (DefaultModelController) OtmRegistry.getMainController()
                .getModelController();
        OwnershipEventListener<TLFacetOwner, TLFacet> listener = new OwnershipEventListener<TLFacetOwner, TLFacet>(
                owner) {

            @Override
            public void processModelEvent(OwnershipEvent<TLFacetOwner, TLFacet> event) {
                List<ModelEventType> events = Arrays.asList(ModelEventType.CUSTOM_FACET_REMOVED,
                        ModelEventType.QUERY_FACET_REMOVED);
                // it can be deleted from delete() code.
                if (inheritedFacetListener != null) {
                    if (events.contains(event.getType())
                            && event.getAffectedItem() == inheritedFacetListener.getSource()) {
                        removeBaseListener();
                    }
                }
            }

        };
        modelC.addSourceListener(listener);
        return listener;
    }

    @Override
    public boolean addChild(final TLModelElement child) {
        if (child instanceof TLProperty) {
            getTLModelObj().addElement((TLProperty) child);
        } else if (child instanceof TLAttribute) {
            getTLModelObj().addAttribute((TLAttribute) child);
        } else if (child instanceof TLIndicator) {
            getTLModelObj().addIndicator((TLIndicator) child);
        } else
            return false;
        return true;
    }

    /**
     * You can not add aliases to facets.
     */
    @Override
    @Deprecated
    public void addAlias(final TLAlias tla) {
        // Not found, add it.
    }

    @Override
    public void delete() {
        removeBaseListener();
        if (getTLModelObj().getOwningEntity() == null) {
            LOGGER.error("Tried to delete a facet MO with no ownining entity.");
            return;
        }
        if (!((ComponentNode) node).isFacet()) {
            LOGGER.error("Tried to delete a facet MO with whose node is not a facet: " + node);
        }
        if ((getTLModelObj().getFacetType().equals(TLFacetType.REQUEST))
                || (getTLModelObj().getFacetType().equals(TLFacetType.RESPONSE))
                || (getTLModelObj().getFacetType().equals(TLFacetType.NOTIFICATION))) {
            getTLModelObj().clearFacet();
        } else if (getTLModelObj().getFacetType().equals(TLFacetType.CUSTOM)) {
            ((TLBusinessObject) getTLModelObj().getOwningEntity())
                    .removeCustomFacet(getTLModelObj());
        } else if (getTLModelObj().getFacetType().equals(TLFacetType.QUERY)) {
            ((TLBusinessObject) getTLModelObj().getOwningEntity())
                    .removeQueryFacet(getTLModelObj());
        } else {
            getTLModelObj().clearFacet();
        }
    }

    @Override
    public List<?> getChildren() {
        final List<TLModelElement> kids = new ArrayList<TLModelElement>();
        kids.addAll(getTLModelObj().getAttributes());
        kids.addAll(getTLModelObj().getIndicators());
        kids.addAll(getTLModelObj().getElements());
        kids.addAll(getTLModelObj().getAliases());
        return kids;
    }

    /**
     * @see org.opentravel.schemas.modelObject.ModelObject#getInheritedChildren()
     */
    @Override
    public List<?> getInheritedChildren() {
        final List<TLModelElement> inheritedKids = new ArrayList<TLModelElement>();
        final List<?> declaredKids = getChildren();

        for (TLAttribute attribute : PropertyCodegenUtils
                .getInheritedFacetAttributes(getTLModelObj())) {
            if (!declaredKids.contains(attribute)) {
                inheritedKids.add(attribute);
            }
        }
        for (TLIndicator indicator : PropertyCodegenUtils
                .getInheritedFacetIndicators(getTLModelObj())) {
            if (!declaredKids.contains(indicator)) {
                inheritedKids.add(indicator);
            }
        }
        for (TLProperty element : PropertyCodegenUtils.getInheritedFacetProperties(getTLModelObj())) {
            if (!declaredKids.contains(element)) {
                inheritedKids.add(element);
            }
        }
        return inheritedKids;
    }

    @Override
    public String getComponentType() {
        return getDisplayName(getTLModelObj().getFacetType());
    }

    public static String getDisplayName(TLFacetType facetType) {
        switch (facetType) {
            case ID:
                return "ID-Facet";
            case CUSTOM:
                return "Custom-Facet";
            case DETAIL:
                return "Detail-Facet";
            case NOTIFICATION:
                return "Notification-Facet";
            case QUERY:
                return "Query-Facet";
            case REQUEST:
                return "Request-Facet";
            case RESPONSE:
                return "Response-Facet";
            case SIMPLE:
                return "Simple-Facet";
            case SUMMARY:
                return "Summary-Facet";
        }
        // should never happend. Make sure that switch cover all cases.
        return "";
    }

    @Override
    protected AbstractLibrary getLibrary(final TLFacet obj) {
        return null;
    }

    @Override
    public String getLabel() {
        String label = getDisplayName(srcObj.getFacetType());
        if (srcObj.getFacetType().equals(TLFacetType.CUSTOM)
                || srcObj.getFacetType().equals(TLFacetType.QUERY)) {
            label = XsdCodegenUtils.getGlobalTypeName(getTLModelObj());
            String parent = srcObj.getOwningEntity().getLocalName();
            if (label.startsWith(parent)) {
                label = label.substring((srcObj.getOwningEntity().getLocalName()).length());
            }
            if (label.startsWith("_"))
                label = label.substring(1);
        } else if (srcObj.getOwningEntity() instanceof TLOperation)
            getDisplayName(srcObj.getFacetType());
        return label;
    }

    @Override
    public String getName() {
        String name = XsdCodegenUtils.getGlobalTypeName(getTLModelObj());
        // Summary facets XSD names do not use the Summary suffix, but we do in the modeling.
        // Custom and query facets report the wrong name, so use their local name (Jan 11, 2013)
        if (getTLModelObj().getFacetType().equals(TLFacetType.SUMMARY))
            name = name + "_" + TLFacetType.SUMMARY.getIdentityName();

        else if (getTLModelObj().getFacetType().equals(TLFacetType.QUERY))
            name = getTLModelObj().getLocalName();
        else if (getTLModelObj().getFacetType().equals(TLFacetType.CUSTOM))
            name = getTLModelObj().getLocalName();

        return name == null ? getTLModelObj().getContext() : name;
    }

    @Override
    public String getNamePrefix() {
        return null;
    }

    @Override
    public String getNamespace() {
        return getTLModelObj().getNamespace();
    }

    @Override
    public boolean isComplexAssignable() {
        return true;
    }

    @Override
    public boolean isComplexFacet() {
        return true;
    }

    @Override
    public boolean setName(final String name) {
        // Only custom and query facets can be named.
        if (getTLModelObj().getFacetType() == TLFacetType.CUSTOM
                || getTLModelObj().getFacetType() == TLFacetType.QUERY) {
            getTLModelObj().setLabel(name);
            return true;
        }
        return false;
    }

    @Override
    public void sort() {
        TLFacet f = getTLModelObj();
        f.sortElements(new StringComparator<TLProperty>() {

            @Override
            protected String getString(TLProperty object) {
                return object.getName();
            }
        });
        f.sortAttributes(new StringComparator<TLAttribute>() {

            @Override
            protected String getString(TLAttribute object) {
                return object.getName();
            }
        });
        f.sortIndicators(new StringComparator<TLIndicator>() {

            @Override
            protected String getString(TLIndicator object) {
                return object.getName();
            }
        });
    }

    private static TLFacet findGhostFacets(TLFacetOwner facetOwner, TLFacet obj) {
        TLFacetOwner extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
        Set<TLFacetOwner> visitedOwners = new HashSet<TLFacetOwner>();
        TLFacet inherited = null;

        // Find all of the inherited facets of the specified facet type
        String indentityName = obj.getFacetType().getIdentityName(obj.getContext(), obj.getLabel());
        while (extendedOwner != null) {
            List<TLFacet> facetList = FacetCodegenUtils.getAllFacetsOfType(extendedOwner,
                    obj.getFacetType());

            for (TLFacet facet : facetList) {
                if (indentityName.equals(facet.getFacetType().getIdentityName(facet.getContext(),
                        facet.getLabel()))) {
                    return facet;
                }

            }
            visitedOwners.add(extendedOwner);
            extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(extendedOwner);

            if (visitedOwners.contains(extendedOwner)) {
                break; // exit if we encounter a circular reference
            }
        }
        return inherited;
    }

}

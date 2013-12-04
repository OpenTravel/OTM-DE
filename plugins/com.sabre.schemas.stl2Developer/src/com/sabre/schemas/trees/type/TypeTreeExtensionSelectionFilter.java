/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.trees.type;

import org.eclipse.jface.viewers.Viewer;

import com.sabre.schemas.modelObject.ExtensionPointFacetMO;
import com.sabre.schemas.modelObject.FacetMO;
import com.sabre.schemas.modelObject.ModelObject;
import com.sabre.schemas.node.Node;

/**
 * Type selection filter that only allow the selection of a particular type of model object. This is
 * used during the selection of extensions since business objects can only extend business objects,
 * cores can only extend cores, etc. 10/18/2012 - this behavior is being removed: Extendible only
 * impacts compiler; it does not imply "final". In addition to the type of object being filtered on,
 * the entities themselves must be extendible in order for this filter to return an affirmative
 * condition.
 * 
 * @author S. Livezey
 */
public class TypeTreeExtensionSelectionFilter extends TypeSelectionFilter {

    private ModelObject<?> modelObject;
    private Class<? extends ModelObject<?>> extensionType;

    /**
     * Constructor that specifies the type of model object to be visible when the filter is applied.
     * 
     * @param modelObjectType
     *            the type of model object that should be
     */
    @SuppressWarnings("unchecked")
    public TypeTreeExtensionSelectionFilter(ModelObject<?> modelObject) {
        this.modelObject = modelObject;

        if (modelObject instanceof ExtensionPointFacetMO) {
            extensionType = FacetMO.class;
        } else {
            extensionType = (Class<? extends ModelObject<?>>) modelObject.getClass();
        }
    }

    /**
     * @see com.sabre.schemas.trees.type.TypeSelectionFilter#isValidSelection(com.sabre.schemas.node.Node)
     */
    @Override
    public boolean isValidSelection(Node n) {
        boolean isValid = false;

        if (n != null) {
            ModelObject<?> modelObject = n.getModelObject();

            if ((extensionType == null) || extensionType.equals(modelObject.getClass())) {
                if (this.modelObject instanceof ExtensionPointFacetMO) {
                    // XP Facets must select extensions in a different namespace
                    // if (n.getParent().getModelObject().isExtendable()) {
                    isValid = (n.getNamespace() != null)
                            && !n.getNamespace().equals(this.modelObject.getNamespace());
                    // }
                } else {
                    // commented out to allow extensions even if base is not extend-able.
                    // isValid = modelObject.isExtendable();
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element == null || !(element instanceof Node)) {
            return false;
        }
        Node n = (Node) element;
        boolean result;

        if (n.getModelObject() == modelObject) {
            result = false;
        } else {
            result = isValidSelection(n) || hasValidChildren(n);
        }
        return result;
    }

}

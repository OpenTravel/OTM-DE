/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.trees.library;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.NavNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.types.TypeNode;

public class LibrarySorter extends ViewerSorter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibrarySorter.class);

    private static final LibrarySorter instance = new LibrarySorter();

    /**
     * @return {@link Comparator} adapter for this ViewerSorter applicable for {@link Node}s.
     */
    public static Comparator<Node> createComparator() {
        return new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                return instance.compare(null, o1, o2);
            }
        };
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int cat1 = category(e1);
        int cat2 = category(e2);

        if (cat1 != cat2) {
            return super.compare(viewer, e1, e2);
        }

        int val = 0, index1 = 0, index2 = 0;
        final Node n1 = (Node) e1;
        final Node n2 = (Node) e2;

        if ((n1 instanceof PropertyNode) && (n2 instanceof PropertyNode)) {
            if (n1.getParent() != null)
                index1 = n1.getParent().getChildren().indexOf(n1);
            if (n2.getParent() != null)
                index2 = n2.getParent().getChildren().indexOf(n2);
            if (index1 > index2)
                val = 1;
            else if (index1 < index2)
                val = -1;
        } else
            return super.compare(viewer, e1, e2);
        return val;
    }

    @Override
    public int category(final Object element) {
        final Node n = (Node) element;

        if (n.getModelObject() == null)
            return 0;

        if (n instanceof TypeNode)
            return 100;

        if (n instanceof ProjectNode) {
            if (n.isEditable())
                return 4;
            else
                return 10;
        }

        if (n instanceof NavNode) {
            final String name = n.getName();
            if (LibraryNode.ELEMENTS.equals(name)) {
                return 3;
            } else if (LibraryNode.SIMPLE_OBJECTS.equals(name)) {
                return 2;
            } else if (LibraryNode.COMPLEX_OBJECTS.equals(name)) {
                return 1;
            } else if (name.equals("Services")) {
                return 4;
            }
        }

        if (n instanceof PropertyNode) {
            int inheritAdjust = n.isInheritedProperty() ? 0 : 10;
            // All inherited first, then actual
            switch (((PropertyNode) n).getPropertyType()) {
                case INDICATOR:
                    return inheritAdjust + 100;
                case INDICATOR_ELEMENT:
                    return inheritAdjust + 100;
                case ATTRIBUTE:
                    return inheritAdjust + 200;
                case ID_REFERENCE:
                    return inheritAdjust + 300;
                case ELEMENT:
                    return inheritAdjust + 300;
            }
        }

        if (n.isAlias())
            return 0;
        if (n.isExtensionPointFacet())
            return 1;

        if (n.isFacet()) {
            if (n.isSimpleFacet())
                return 1;
            if (n.isVWA_AttributeFacet())
                return 2;
            if (n.isRoleFacet())
                return 61;
            if (n.isSimpleListFacet())
                return 65;
            if (n.isListFacet())
                return 66;
            if (n.isOperation())
                return 67;

            if (n.getTLModelObject() instanceof TLFacet) {
                switch (((TLFacet) n.getTLModelObject()).getFacetType()) {
                    case ID:
                        return 10;
                    case SIMPLE:
                        return 20;
                    case SUMMARY:
                        return 30;
                    case DETAIL:
                        return 40;
                    case CUSTOM:
                        return 50;
                    case QUERY:
                        return 60;
                    case REQUEST:
                        return 70;
                    case RESPONSE:
                        return 80;
                    case NOTIFICATION:
                        return 90;
                }
            } else
                LOGGER.error("Don't know what " + n + " is!");
            return 100;
        }

        if (n.isBuiltIn())
            return 3;
        if (n.isXSDSchema())
            return 2;
        if (n.isLibrary())
            return 1;

        return 10;
    }
}

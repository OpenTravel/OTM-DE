/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.internal;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.stl2Developer.editor.internal.layouts.INodeLayout;
import org.opentravel.schemas.stl2Developer.editor.internal.layouts.VerticalNodeLayout;

/**
 * @author Pawel Jedruch
 * 
 */
public class Features {

    private static boolean isLayoutEnabled = false;
    private static boolean isViewLinkedWithNavigator;
    private static boolean isShowingSimpleObjectsAsUsedType;

    public static boolean customLinesForNotVisbile() {
        return true;
    }

    /**
     * The target connection anchor will be fixed on top of figure
     */
    public static boolean fixedTargetAnchor() {
        return true;
    }

    /**
     * @return
     */
    public static INodeLayout getLayoutAlgorithm() {
        if (isLayoutEnabled())
            return new VerticalNodeLayout();
        else {
            return new INodeLayout.Stub();
        }
    }

    public static boolean isLayoutEnabled() {
        return isLayoutEnabled;
    }

    public static void setLayoutEnabled(boolean isLayoutEnabled) {
        Features.isLayoutEnabled = isLayoutEnabled;
    }

    /**
     * @return
     */
    public static boolean isLayoutAnimationEnabled() {
        return false;
    }

    /**
     * @return border color used for selected parts
     */
    public static Color getSelectionColor() {
        return ColorConstants.blue;
    }

    /**
     * @param node
     * @return true if given node should be displayed from "Show used type" action
     */
    public static boolean showAsUsedType(Node node) {
        boolean ret = node instanceof VWA_Node;
        ret = ret || node instanceof BusinessObjectNode;
        ret = ret || node instanceof CoreObjectNode;
        if (node instanceof SimpleTypeNode) {
            return isShowingSimpleObjectsAsUsedType();
        }
        return ret;
    }

    /**
     * @return if true then all simple types will be displayed on "Show used type" action.
     */
    public static boolean isShowingSimpleObjectsAsUsedType() {
        return isShowingSimpleObjectsAsUsedType;
    }

    public static void setShowSimpleObjectsAsUsedType(boolean showSimpleObjects) {
        isShowingSimpleObjectsAsUsedType = showSimpleObjects;
    }

    /**
     * @return true if newly added node (by link pressing) will be created in fully expanded state.
     */
    public static boolean addNewPropertyFullyExpanded() {
        return true;
    }

    public static boolean isViewLinkedWithNavigator() {
        return isViewLinkedWithNavigator;
    }

    public static void setViewLinkedWithNavigator(boolean linked) {
        isViewLinkedWithNavigator = linked;
    }
}

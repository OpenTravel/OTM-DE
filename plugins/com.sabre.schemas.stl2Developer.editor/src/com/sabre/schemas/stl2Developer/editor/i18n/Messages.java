/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author Pawel Jedruch
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.sabre.schemas.stl2Developer.editor.i18n.messages"; //$NON-NLS-1$
    public static String DependeciesView_ActionClearAll;
    public static String DependeciesView_ActionClearUnlinked;
    public static String DependeciesView_ActionSaveToClipboard;
    public static String DependeciesView_ActionUsedTypes;
    public static String DependeciesView_EmptyFacetFilter;
    public static String DependeciesView_FiltersMenu;
    public static String DependeciesView_MenuAlignment;
    public static String DependenciesView_ActionRemove;
    public static String DependenciesView_ActionWhereUsed;
    public static String GenericEditPart_UnlinkedTooltip;
    public static String ToggleLayout_DisableLayout;
    public static String ToggleLayout_EnableLayout;
    public static String ToggleShowSimpleObjects_Disable;
    public static String ToggleShowSimpleObjects_Enable;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}

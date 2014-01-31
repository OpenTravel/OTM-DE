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
package org.opentravel.schemas.stl2Developer.editor.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author Pawel Jedruch
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.opentravel.schemas.stl2Developer.editor.i18n.messages"; //$NON-NLS-1$
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

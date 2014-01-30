/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.properties;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;

/**
 * Registry for common fonts used throughout the application.
 * 
 * @author S. Livezey
 */
public class Fonts {

    public static final String inheritedItem = "inherited";
    public static final String readOnlyItem = "readOnly";
    public static final String defaultContext = "default_context";

    private static FontRegistry font_registry;
    private static volatile boolean initialized = false;

    public static FontRegistry getFontRegistry() {
        if (!initialized) {
            initializeRegistry();
        }
        return font_registry;
    }

    private static void initializeRegistry() {
        try {
            font_registry = new FontRegistry(PlatformUI.getWorkbench().getDisplay());
            FontData[] defaultFont = PlatformUI.getWorkbench().getDisplay().getActiveShell()
                    .getFont().getFontData();

            font_registry.put(inheritedItem, newFontData(defaultFont, null, -1, SWT.ITALIC));
            font_registry.put(readOnlyItem, newFontData(defaultFont, null, -1, SWT.ITALIC));
            font_registry.put(defaultContext, newFontData(defaultFont, null, -1, SWT.BOLD));
            initialized = true;
        } catch (Exception e) {
            // Sometimes it would throw an Exception (NPE) when invoked too early. No Active Shell?
            // Catching and trying again if not initialized properly
        }
    }

    /**
     * Returns a copy of the given font data so that it can be slightly modified before adding to
     * the registry.
     */
    private static FontData[] newFontData(FontData[] baseFont, String name, int height, int style) {
        FontData[] fontData = new FontData[baseFont.length];

        for (int i = 0; i < baseFont.length; i++) {
            String fontName = (name == null) ? baseFont[i].getName() : name;
            int fontHeight = (height < 0) ? baseFont[i].getHeight() : height;
            int fontStyle = (style < 0) ? baseFont[i].getStyle() : style;

            fontData[i] = new FontData(fontName, fontHeight, fontStyle);
        }
        return fontData;
    }

}

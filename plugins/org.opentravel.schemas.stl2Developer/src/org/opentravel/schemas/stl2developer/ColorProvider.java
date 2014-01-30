
package org.opentravel.schemas.stl2developer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ColorProvider {

    private final Display display;

    public ColorProvider(final Display display) {
        this.display = display;

    }

    /**
     * Retrieves system {@link Color} object given {@link SWT} color id
     * 
     * @param swtColorId
     *            {@link SWT} colorId, e.g. {@code SWT.COLOR_DARK_RED}
     * @return {@link Color} object
     */
    public Color getColor(final int swtColorId) {
        return display.getSystemColor(swtColorId);
    }

}

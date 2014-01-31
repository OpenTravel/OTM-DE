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

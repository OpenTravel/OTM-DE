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
package org.opentravel.schemas.stl2Developer.editor.internal.layouts;

import java.util.Collections;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public interface INodeLayout {

    /**
     * @param model
     *            - model to layout
     * @param context
     *            - UINodes size
     * @param size
     *            - the bounds in which the layout can place the nodes.
     * @return
     */
    Map<UINode, Point> getConstraints(Diagram model, Map<UINode, Dimension> context, Rectangle size);

    public class Stub implements INodeLayout {

        @Override
        public Map<UINode, Point> getConstraints(Diagram model, Map<UINode, Dimension> context,
                Rectangle size) {
            return Collections.emptyMap();
        }

    }

}

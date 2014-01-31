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
package org.opentravel.schemas.stl2Developer.editor.internal;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINodeFactory;

/**
 * @author Pawel Jedruch
 * 
 */
public class DropListener extends AbstractTransferDropTargetListener {
    private final UINodeFactory factory;

    public DropListener(EditPartViewer viewer, Diagram diagram) {
        super(viewer, TextTransfer.getInstance());
        factory = new UINodeFactory(diagram);
    }

    @Override
    protected void updateTargetRequest() {

    }

    @Override
    protected Request createTargetRequest() {
        CreateRequest request = new CreateRequest();
        request.setFactory(factory);
        request.setLocation(getDropLocation());
        return request;
    }

    @Override
    protected void handleDragOver() {
        getCurrentEvent().detail = DND.DROP_COPY;
        super.handleDragOver();
    }

    @Override
    protected void handleDrop() {
        Object data = getCurrentEvent().data;
        Node sourceNode = NodeFinders.findNodeByID((String) data);
        factory.setNode(sourceNode);
        super.handleDrop();
    }
}

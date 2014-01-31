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
package org.opentravel.schemas.stl2Developer.editor.model;

import org.eclipse.gef.requests.CreationFactory;
import org.opentravel.schemas.node.Node;

public class UINodeFactory implements CreationFactory {

    private Node node;
    private Diagram diagram;

    public UINodeFactory(Diagram diagram) {
        this.diagram = diagram;
    }

    @Override
    public Class<? extends Node> getObjectType() {
        return node.getClass();
    }

    @Override
    public Node getNewObject() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

}
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
package org.opentravel.schemas.node.controllers;

import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class DescriptionDocItemNodeModelController implements DocItemNodeModelController {

    private TLDocumentation parentDoc;

    public DescriptionDocItemNodeModelController(TLDocumentation parentDoc) {
        this.parentDoc = parentDoc;
    }

    @Override
    public TLDocumentationItem createChild() {
        if (parentDoc.getDescription() == null) {
            parentDoc.setDescription("");
        }
        throw new IllegalStateException("Cannot add more descriptions");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#removeChild()
     */
    @Override
    public void removeChild(TLDocumentationItem child) {
        throw new UnsupportedOperationException(
                "Cannot delete description, clear the description field instead");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#getChildren()
     */
    @Override
    public List<TLDocumentationItem> getChildren() {
        return Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#getChild(int)
     */
    @Override
    public TLDocumentationItem getChild(int index) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#moveChildUp(java.lang.Object)
     */
    @Override
    public void moveChildUp(TLDocumentationItem child) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#moveChildDown(java.lang.Object)
     */
    @Override
    public void moveChildDown(TLDocumentationItem child) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.controllers.NodeModelController#getChild(java.lang.Object)
     */
    @Override
    public TLDocumentationItem getChild(Object key) {
        throw new UnsupportedOperationException("Cannot retrieve documentation item by key");
    }

}

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
/**
 * 
 */
package org.opentravel.schemas.node;

import org.opentravel.schemas.modelObject.ModelObject;

import org.opentravel.schemacompiler.model.NamedEntity;

/**
 * @author Dave Hollander
 * 
 */
public interface SimpleComponentInterface {

    public boolean isSimpleTypeProvider();

    /**
     * @return the node representing the base type.
     */
    public INode getBaseType();

    /**
     * @return the model object underlying this node.
     */
    public ModelObject<?> getModelObject(); // The Model Object

    /**
     * @return the TL Object underlying this node's model object
     */
    public NamedEntity getTLOjbect();
}

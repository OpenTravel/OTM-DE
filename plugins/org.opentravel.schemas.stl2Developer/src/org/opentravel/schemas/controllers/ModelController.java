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
package org.opentravel.schemas.controllers;

import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;

/**
 * Centralizes all the model related actions. When created, creates and saves a model node and
 * TLModel. It operates on the whole model in contrast to {@link LibraryNode} which is responsible
 * for single libraries actions.
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface ModelController {

    /**
     * Creates new empty model and initializes it with built in libraries
     * 
     * @return newly created {@link ModelNode}
     */
    public ModelNode createNewModel();

    /**
     * Saves all the libraries within the given model
     * 
     * @param model
     *            {@link ModelNode} to be saved
     */
    public void saveModel(INode model);

    /**
     * Saves and closes all the libraries (also built in) within the given model, clears the model
     * 
     * @param model
     *            {@link ModelNode} to be closed
     */
    public void close();

    /**
     * Compiles the libraries into output files (defined by the underlying schema compiler)
     * 
     * @param model
     *            {@link ModelNode} to be compiled
     */
    public void compileModel(ModelNode model);

    /**
     * Compiles the libraries in the project into output files (defined by the underlying schema
     * compiler)
     * 
     */
    public void compileModel(ProjectNode cur);

    public ModelNode getModel();

    public TLModel getTLModel();

    /**
     * Change given property to simple. This action only make sense for owning component with
     * SimpleFacet. Check {@link ComplexComponentInterface}.
     * 
     * @param property
     * @return false if property is not {@link SimpleAttributeNode} or owning component is not
     *         {@link ComplexComponentInterface}
     */
    public boolean changeToSimple(PropertyNode property);

    /**
     * Move given simple attribute to target facet. As a result this method should create new simple
     * attribute with type == OTA:Empty to prevent {@link ComplexComponentInterface} of don't having
     * simple attribute.
     * 
     * @param simpleAttribute
     * @param summaryFacet
     * @return create new property in targetFacet or null if simpleAttribute is not
     *         {@link SimpleAttributeNode}
     */
    ComponentNode moveSimpleToFacet(Node simpleAttribute, ComponentNode targetFacet);

}

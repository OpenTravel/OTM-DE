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
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * Centralizes all the model related actions. When created, creates and saves a model node and TLModel. It operates on
 * the whole model in contrast to {@link LibraryNode} which is responsible for single libraries actions.
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface ModelController {

	/**
	 * Saves and closes all the libraries (also built in) within the given model, clears the model
	 * 
	 * @param model
	 *            {@link ModelNode} to be closed
	 */
	public void close();

	/**
	 * Compiles the libraries in the project into output files (defined by the underlying schema compiler)
	 * 
	 */
	public void compileInBackground(ProjectNode project);

	/**
	 * @return the directory of the last compile or empty string
	 */
	public String getLastCompileDirectory();

	public ModelNode getModel();

	public TLModel getTLModel();

	/**
	 * Saves all the libraries within the given model
	 * 
	 * @param model
	 *            {@link ModelNode} to be saved
	 */
	public void saveModel(INode model);

}

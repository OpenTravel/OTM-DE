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

import java.util.List;

import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * Central place for all the library related actions. Note that the global model actions are controlled by
 * {@link ModelController}
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface LibraryController {

	// /**
	// * Change namespace. If the namespace is shared, the user is asked if they want to change all, one or none.
	// */
	// void changeNamespace(LibraryNode library, String namespace);

	// void changeNamespaceExtension(LibraryNode library, String namespace);

	/**
	 * Convert given library to OTM. This method will convert given library, and if needed all his dependencies.
	 * 
	 * @param xsdLibrary
	 *            - XSD schema, non-otm file.
	 * @param withDependecies
	 *            TODO
	 * @return newly created otm library and all dependencies from given xsd schema.
	 */
	List<LibraryNode> convertXSD2OTM(LibraryNode xsdLibrary, boolean withDependecies);

	/**
	 * Creates new library in the project currently selected in the navigator view.
	 * 
	 * @return
	 * 
	 */
	LibraryNavNode createLibrary();

	// /**
	// * Get all libraries assigned to a namespace.
	// *
	// * @param namespace
	// * @return list of libraries assigned to the namespace.
	// */
	// List<LibraryNode> getLibrariesWithNamespace(String namespace);

	/**
	 * @param libary
	 *            with status and {@link ProjectItem} with {@link RepositoryItemState}
	 * @return status base on {@link LibraryNode#getStatus()} and {@link ProjectItem#getState()}.
	 */
	String getLibraryStatus(LibraryNode libary);

	/**
	 * OpenLibraryAction - Find the nearest parent that can contain a library then open the existing library and add it
	 * to that parent. Opens already existing library using a file selection dialog and adds it to the model.
	 */
	void openLibrary(INode model);

	// /**
	// * Remove the library from the parent project.
	// *
	// * @param libraries
	// */
	// void remove(Collection<? extends Node> libraries);

	/**
	 * Saves all the user defined libraries in the given model
	 * 
	 * @param quiet
	 *            be quite, do not notify user of happy path
	 * @return false in case one if libraries was not saved successfully
	 */
	boolean saveAllLibraries(boolean quiet);

	/**
	 * Saves the given libraries to the physical files
	 * 
	 * @param libraries
	 *            list of {@link LibraryNode}s to be saved
	 * @param quiet
	 *            be quite, do not notify user of happy path
	 * @return false in case one if libraries was not saved successfully
	 */
	boolean saveLibraries(List<LibraryNode> libraries, boolean quiet);

	/**
	 * Saves the given library to the physical file
	 * 
	 * @param library
	 *            {@link LibraryNode} to be saved
	 * @param quiet
	 *            be quite, do not notify user of happy path
	 * @return false if library was not saved successfully
	 */
	boolean saveLibrary(LibraryNode library, boolean quiet);

	/**
	 * Update the editable status for all libraries.
	 */
	void updateLibraryStatus();

}

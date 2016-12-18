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
package org.opentravel.schemas.actions;

import java.util.Collection;
import java.util.Collections;

import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.trees.repository.RepositoryNode;

/**
 * Manage a library in a repository.
 * 
 * @author Dave Hollander
 * 
 */
public class ManageInRepositoryAction extends OtmAbstractAction {
	private static StringProperties propDefault = new ExternalizedStringProperties("action.library.manage");
	RepositoryNode repository;
	RepositoryController rc;
	private LibraryNode selectedLibrary;

	public ManageInRepositoryAction() {
		super(propDefault);
		rc = mc.getRepositoryController();
	}

	public ManageInRepositoryAction(final StringProperties props, RepositoryNode repository) {
		super(props);
		this.repository = repository;
		rc = mc.getRepositoryController();
	}

	public void setLibrary(LibraryNode n) {
		// TODO: support for multi selection of libraries
		selectedLibrary = n;
	}

	@Override
	public void run() {
		if (repository == null)
			return; // TODO - launch repository selection wizard

		rc.manage(repository, Collections.singletonList(selectedLibrary));
	}

	private Collection<LibraryNode> selectedLibraries() {
		if (selectedLibrary != null) {
			return Collections.singletonList(selectedLibrary);
		} else {
			return mc.getSelectedLibraries();
		}
	}

	private boolean canManaged(LibraryNode n) {
		if (n == null || !rc.isInManagedNS(n.getNamespace(), repository))
			return false;
		return n.getProjectItem().getState().equals(RepositoryItemState.UNMANAGED);
	}

	@Override
	public boolean isEnabled() {
		for (LibraryNode n : selectedLibraries()) {
			if (!canManaged(n))
				return false;
		}
		return true;
	}
}

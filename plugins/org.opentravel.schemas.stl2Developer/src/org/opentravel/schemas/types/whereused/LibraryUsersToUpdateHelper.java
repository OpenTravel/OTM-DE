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
package org.opentravel.schemas.types.whereused;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates lists of TypeUsers, ExtensionUsers and ContextualFacetUsers for use in updating versions.
 * <p>
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryUsersToUpdateHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryUsersToUpdateHelper.class);

	private List<TypeUser> usersToUpdate = new ArrayList<>();
	private List<ExtensionOwner> extensionsToUpdate = new ArrayList<>();
	private List<ContextualFacetNode> facetsToUpdate = new ArrayList<>();

	public LibraryUsersToUpdateHelper(LibraryProviderNode lpn) {
		for (Node user : lpn.getChildren(false))
			if (user instanceof TypeUserNode) {
				if (!usersToUpdate.contains(((TypeUserNode) user).getOwner()))
					usersToUpdate.add(((TypeUserNode) user).getOwner());
			} else if (user instanceof ExtensionUserNode) {
				if (!extensionsToUpdate.contains(((ExtensionUserNode) user).getOwner()))
					extensionsToUpdate.add(((ExtensionUserNode) user).getOwner());
			} else if (user instanceof ContextualFacetUserNode) {
				if (!facetsToUpdate.contains(((ContextualFacetUserNode) user).getOwner()))
					facetsToUpdate.add(((ContextualFacetUserNode) user).getOwner());
			}
	}

	public List<TypeUser> getUsersToUpdate() {
		return usersToUpdate;
	}

	public List<ExtensionOwner> getExtensionsToUpdate() {
		return extensionsToUpdate;
	}

	public List<ContextualFacetNode> getFacetsToUpdate() {
		return facetsToUpdate;
	}

	public boolean isEmpty() {
		return (usersToUpdate.isEmpty() && extensionsToUpdate.isEmpty() && facetsToUpdate.isEmpty());
	}

	public void replace(LibraryNode replacement) {
		replacement.replaceAllUsers(getUsersToUpdate());
		replacement.replaceAllExtensions(getExtensionsToUpdate());
		replacement.replaceAllContributors(getFacetsToUpdate());

		// These are no longer correct so clear them.
		getUsersToUpdate().clear();
		getExtensionsToUpdate().clear();
		getFacetsToUpdate().clear();
	}
}

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
package org.opentravel.schemas.views.propertyview;

import org.eclipse.ui.views.properties.IPropertySource;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;

/**
 * @author Pawel Jedruch
 * 
 */
public class NodePropertySourceFactory {

	public IPropertySource createPropertySource(Node node) {
		if (node instanceof LibraryNavNode)
			node = (Node) ((LibraryNavNode) node).getThisLib();

		if (node instanceof RepositoryNode) {
			return new RepositoryPropertySource((RepositoryNode) node);
		} else if (node instanceof LibraryChainNode) {
			return new LibraryPropertySource(((LibraryChainNode) node).getHead());
		} else if (node instanceof LibraryNode) {
			return new LibraryPropertySource((LibraryNode) node);
		} else if (node instanceof ProjectNode) {
			return new ProjectPropertySource((ProjectNode) node);
		}
		return null;
	}

}

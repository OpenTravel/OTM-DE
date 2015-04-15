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

import static org.opentravel.schemas.node.controllers.NodeUtils.isBuildInProject;
import static org.opentravel.schemas.node.controllers.NodeUtils.isProject;

import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;

/**
 * @author Dave Hollander
 * 
 */
@Deprecated
public class RemoveAllLibrariesAction extends RemoveLibrariesAction {

	public RemoveAllLibrariesAction() {
		super("action.library.removeAll");
	}

	@Override
	protected boolean selectionSupported(List<? extends Node> newSelection) {
		for (Node n : newSelection) {
			if (!isProject(n) || isBuildInProject((ProjectNode) n))
				return false;
		}
		return true;
	}

}

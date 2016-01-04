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
import static org.opentravel.schemas.node.controllers.NodeUtils.isDefaultProject;
import static org.opentravel.schemas.node.controllers.NodeUtils.isProject;

import java.util.List;

import org.eclipse.ui.PlatformUI;
import org.opentravel.schemas.navigation.GlobalSelectionProvider;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.stl2developer.OtmRegistry;

/**
 * @author Dave Hollander
 * 
 */
@Deprecated
public class CloseProjectAction extends AbstractGlobalSelectionAction {

	private ProjectNode toClose;

	public CloseProjectAction() {
		super("action.closeProject", PlatformUI.getWorkbench(), GlobalSelectionProvider.NAVIGATION_VIEW);
		new ExternalizedStringProperties(getId()).initializeAction(this);
	}

	@Override
	public void run() {
		if (toClose != null) {
			OtmRegistry.getMainController().getProjectController().close(toClose);
			toClose = null;
		}
	}

	@Override
	protected boolean isEnabled(Object object) {
		@SuppressWarnings("unchecked")
		List<Node> newSelection = (List<Node>) object;
		if (newSelection.size() != 1) {
			return false;
		}

		// save ref to make sure run will execute on the same instance
		toClose = getProjectToClose(newSelection.get(0));
		return toClose != null;
	}

	private ProjectNode getProjectToClose(Node n) {
		if (isProject(n) && !(isBuildInProject((ProjectNode) n) || isDefaultProject(n))) {
			return ((ProjectNode) n);
		}
		return null;
	}

}

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
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;

/**
 * 
 * @author Dave Hollander
 * 
 */

public class CloseProjectHandler extends OtmAbstractHandler {

	public static String COMMAND_ID = "org.opentravel.schemas.commands.CloseProject";
	ProjectNode project = null;

	/**
	 * Use project controller to close a project
	 */
	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		if (isEnabled())
			OtmRegistry.getMainController().getProjectController().close(project);
		return null;
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	@Override
	public boolean isEnabled() {
		project = getSelectedProject();
		return project != null ? !project.isBuiltIn() : false;
	}
}

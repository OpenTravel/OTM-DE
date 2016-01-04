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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ui.PlatformUI;
import org.opentravel.schemas.navigation.INavigationService;

public class NavigationCommand extends AbstractHandler implements IExecutableExtension {

	private static final String MODE_FORWARD = "FORWARD";
	private static final String MODE_BACKWARD = "BACKWARD";
	private String mode = "";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (MODE_BACKWARD.equalsIgnoreCase(mode))
			getNavigationController().goBackward();
		else if (MODE_FORWARD.equalsIgnoreCase(mode))
			getNavigationController().goForward();
		return null;
	}

	private INavigationService getNavigationController() {
		return (INavigationService) PlatformUI.getWorkbench().getService(INavigationService.class);
	}

	// Would not execute breakpoint except on startup
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		mode = (String) data;
	}
}

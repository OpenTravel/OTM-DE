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
import org.eclipse.jface.resource.ImageDescriptor;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveLibraryHandler extends OtmAbstractHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SaveLibrariesHandler.class);

	public static String COMMAND_ID = "org.opentravel.schemas.commands.SaveLibrary";

	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		mc.getLibraryController().saveLibraries(getSelectedLibraries(true), false);
		return null;
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	public static ImageDescriptor getIcon() {
		return Images.getImageRegistry().getDescriptor(Images.Save);
	}

	// Note - this is executed OFTEN - make sure it is quick
	@Override
	public boolean isEnabled() {
		// Active part will be null on startup, return true to not cause error
		if (mc.getActivePart() == null)
			return true;

		// LOGGER.debug("Is enabled? " + mc.getSelectedNode_NavigatorView());
		Node n = getFirstSelected();
		if (n == null || n.getLibrary() == null)
			return false;
		return n.getLibrary().isEditable();
	}

}

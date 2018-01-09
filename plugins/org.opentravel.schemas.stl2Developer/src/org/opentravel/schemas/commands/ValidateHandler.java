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
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
import org.opentravel.schemas.views.ValidationResultsView;

/**
 * 
 * @author Dave Hollander
 * 
 */

public class ValidateHandler extends OtmAbstractHandler {

	public static String COMMAND_ID = "org.opentravel.schemas.commands.Validate";
	Node node = null;

	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		ValidationResultsView view = OtmRegistry.getValidationResultsView();
		if (view == null || !view.activate()) {
			DialogUserNotifier.openWarning("Warning", "Please open the validation view before validating.");
			return null;
		}

		if (isEnabled())
			view.validateNode(node);

		return null;
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	public static ImageDescriptor getIcon() {
		return Images.getImageRegistry().getDescriptor(Images.Validate);
		// return Activator.getImageDescriptor(Images.Validate);
	}

	@Override
	public boolean isEnabled() {
		node = mc.getSelectedNode_NavigatorView();
		if (node instanceof ImpliedNode || node instanceof WhereUsedNode)
			return false;
		return node != null ? !node.isBuiltIn() && !node.isXSDSchema() : false;
	}

}

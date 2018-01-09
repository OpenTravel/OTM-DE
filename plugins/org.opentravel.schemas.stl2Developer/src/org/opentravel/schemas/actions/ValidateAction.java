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

import java.util.List;

import org.opentravel.schemas.commands.ValidateHandler;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.navigation.GlobalSelectionProvider;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
import org.opentravel.schemas.views.ValidationResultsView;

/**
 * NOT USED - {@link ValidateHandler}
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public class ValidateAction extends AbstractGlobalSelectionAction {

	public static final String ID = "action.validate";

	public ValidateAction() {
		super(ID, GlobalSelectionProvider.NAVIGATION_VIEW);
		new ExternalizedStringProperties(getId()).initializeAction(this);
		setImageDescriptor(Images.getImageRegistry().getDescriptor(Images.Validate));
	}

	@Override
	public void run() {
		MainController mc = OtmRegistry.getMainController();

		ValidationResultsView view = OtmRegistry.getValidationResultsView();
		if (view == null || !view.activate()) {
			DialogUserNotifier.openWarning("Warning", "Please open the validation view before validating.");
			return;
		}

		Node node = getSourceValue().get(0);
		// If in a version chain, validate all members of the chain.
		if (node.getChain() != null) {
			view.validateNode(node.getChain());
		} else {
			view.validateNode(node);
		}
		mc.refresh();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Node> getSourceValue() {
		return (List<Node>) super.getSourceValue();
	}

	@Override
	public boolean isEnabled(Object object) {
		List<Node> newSelection = getSourceValue();
		if (newSelection.size() != 1) {
			return false;
		}
		Node n = newSelection.get(0);
		if (n instanceof ImpliedNode || n instanceof WhereUsedNode)
			return false;
		return n != null ? !n.isBuiltIn() && !n.isXSDSchema() : false;
	}
}

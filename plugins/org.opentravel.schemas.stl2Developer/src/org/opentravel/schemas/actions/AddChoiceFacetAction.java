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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.commands.OtmAbstractHandler;
import org.opentravel.schemas.node.ChoiceObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.SimpleNameWizard;
import org.opentravel.schemas.wizards.validators.NewNodeNameValidator;

/**
 * @author Dave Hollander
 * 
 */
public class AddChoiceFacetAction extends OtmAbstractAction {
	private static StringProperties propsDefault = new ExternalizedStringProperties("action.addChoice");

	OtmAbstractHandler handler = new OtmAbstractHandler() {
		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			return null;
		}
	};

	/**
	 *
	 */
	public AddChoiceFacetAction() {
		super(propsDefault);
	}

	public AddChoiceFacetAction(final StringProperties props) {
		super(props);
	}

	@Override
	public void run() {
		addChoiceFacet();
	}

	@Override
	public boolean isEnabled() {
		// Unmanaged or in the most current (head) library in version chain.
		Node n = mc.getCurrentNode_NavigatorView().getOwningComponent();
		return n instanceof ChoiceObjectNode ? n.isEditable_newToChain() : false;
		// use if we allow custom facets to be added as minor version change
		// return n instanceof BusinessObjectNode ? n.isEnabled_AddProperties() : false;

	}

	private void addChoiceFacet() {
		final TLFacetType facetType = TLFacetType.CHOICE;
		ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		if (current == null || !(current instanceof ChoiceObjectNode) || !current.isEditable_newToChain()) {
			DialogUserNotifier.openWarning("Add Choice Facet",
					"Choice Facets can only be added to non-versioned Choice objects.");
			return;
		}

		// use if we allow custom facets to be added as minor version change
		// // Use the version utils in handler to create a minor or patch version if needed
		// if (current.isEnabled_AddProperties() && !current.isInHead())
		// current = handler.createVersionExtension(current);
		// if (current == null)
		// return;

		final ChoiceObjectNode co = (ChoiceObjectNode) current;

		SimpleNameWizard wizard = new SimpleNameWizard("wizard.newOperation");
		wizard.setValidator(new NewNodeNameValidator(co, wizard, Messages.getString("wizard.newOperation.error.name")));
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			co.addFacet(wizard.getText());
			// new FacetNode(co, wizard.getText(), mc.getContextController().getDefaultContextId(), TLFacetType.CHOICE);
			mc.refresh(co);
		}

		mc.refresh(co);
	}

}

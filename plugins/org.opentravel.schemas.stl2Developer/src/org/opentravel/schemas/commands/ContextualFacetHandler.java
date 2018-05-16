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
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.SimpleNameWizard;
import org.opentravel.schemas.wizards.validators.NewNodeNameValidator;

/**
 * 
 * @author Dave Hollander
 * 
 */
// TODO - change activation to not use Add*Actions
//
public class ContextualFacetHandler extends OtmAbstractHandler {
	public static String COMMAND_ID = "org.opentravel.schemas.commands.addContextualFacet";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		return null;
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	public void addContextualFacet(ChoiceObjectNode ch) {
		if (ch == null || !ch.isEditable_newToChain()) {
			DialogUserNotifier.openWarning("Add Choice Facet",
					"Choice Facets can only be added to choice object that are not versioned or are new to the version chain.");
			return;
		}
		AbstractContextualFacet af = null;
		af = ch.addFacet("new");

		if (af != null)
			af.setName(getName(af));
		mc.refresh(ch);
	}

	public void addContextualFacet(BusinessObjectNode bo, TLFacetType type) {
		// Verify the current node is editable business object
		if (bo == null || !bo.isEditable_newToChain()) {
			DialogUserNotifier.openWarning("Add Facet", "Facets can only be added to new Business Objects");
			return;
		}

		AbstractContextualFacet af = null;
		af = bo.addFacet("new", type);

		if (af != null)
			af.setName(getName(af));
		mc.refresh(bo);
	}

	private String getName(Node parent) {
		// Test allows junit tests and provides safety from unexpected eclipse issues
		if (getMainWindow().hasDisplay()) {
			SimpleNameWizard wizard = new SimpleNameWizard("wizard.newOperation");
			wizard.setValidator(
					new NewNodeNameValidator(parent, wizard, Messages.getString("wizard.newOperation.error.name")));
			wizard.run(OtmRegistry.getActiveShell());
			if (!wizard.wasCanceled()) {
				return wizard.getText();
			}
		}
		return "new";
	}

}

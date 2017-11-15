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
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ChoiceObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.facets.ChoiceFacetNode;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.CustomFacetNode;
import org.opentravel.schemas.node.facets.QueryFacetNode;
import org.opentravel.schemas.node.facets.UpdateFacetNode;
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

	public void addContextualFacet(BusinessObjectNode bo, TLFacetType type) {
		// Verify the current node is editable business object
		if (bo == null || !bo.isEditable_newToChain()) {
			DialogUserNotifier.openWarning("Add Facet", "Facets can only be added to new Business Objects");
			return;
		}

		// Create the contextual facet
		ContextualFacetNode cf = null;
		switch (type) {
		case QUERY:
			cf = new QueryFacetNode();
			bo.getTLModelObject().addQueryFacet(cf.getTLModelObject());
			break;
		case CUSTOM:
			cf = new CustomFacetNode();
			bo.getTLModelObject().addCustomFacet(cf.getTLModelObject());
			break;
		case UPDATE:
			cf = new UpdateFacetNode();
			bo.getTLModelObject().addUpdateFacet(cf.getTLModelObject());
		default:
			return;
		}
		cf.setName(getName(bo));
		bo.getLibrary().addMember(cf);

		// Create contributed facet
		NodeFactory.newChild(bo, cf.getTLModelObject());
		mc.refresh(bo);
	}

	// Only called from Add Choice Facet Action for version 1.5
	public void addContextualFacet(ChoiceObjectNode co) {
		// Verify the current node is editable business object
		if (co == null || !co.isEditable_newToChain()) {
			DialogUserNotifier.openWarning("Add Choice Facet",
					"Choice Facets can only be added to non-versioned Choice objects.");
			return;
		}

		// Create the contextual facet
		ChoiceFacetNode cf = new ChoiceFacetNode();
		cf.setName(getName(co));
		if (OTM16Upgrade.otm16Enabled)
			co.getLibrary().addMember(cf);
		co.getTLModelObject().addChoiceFacet(cf.getTLModelObject());

		// Create contributed facet
		NodeFactory.newChild(co, cf.getTLModelObject());
		mc.refresh(co);
	}

	private String getName(Node parent) {
		SimpleNameWizard wizard = new SimpleNameWizard("wizard.newOperation");
		wizard.setValidator(new NewNodeNameValidator(parent, wizard, Messages
				.getString("wizard.newOperation.error.name")));
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			return wizard.getText();
		}
		return "new";
	}

}

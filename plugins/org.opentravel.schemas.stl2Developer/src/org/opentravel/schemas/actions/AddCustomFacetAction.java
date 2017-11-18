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
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.commands.ContextualFacetHandler;
import org.opentravel.schemas.commands.OtmAbstractHandler;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewFacetWizard;
import org.opentravel.schemas.wizards.validators.NewFacetValidator;

/**
 * @author Dave Hollander
 * 
 */
public class AddCustomFacetAction extends OtmAbstractAction {
	private static StringProperties propsDefault = new ExternalizedStringProperties("action.addCustom");

	OtmAbstractHandler handler = new OtmAbstractHandler() {
		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			return null;
		}
	};

	/**
	 *
	 */
	public AddCustomFacetAction() {
		super(propsDefault);
	}

	public AddCustomFacetAction(final StringProperties props) {
		super(props);
	}

	@Override
	public void run() {
		if (OTM16Upgrade.otm16Enabled)
			addContextualFacet(TLFacetType.CUSTOM);
		else
			addCustomFacet();
	}

	@Override
	public boolean isEnabled() {
		// Unmanaged or in the most current (head) library in version chain.
		LibraryMemberInterface n = mc.getCurrentNode_NavigatorView().getOwningComponent();
		return n instanceof BusinessObjectNode ? n.isEditable_newToChain() : false;
		// use if we allow custom facets to be added as minor version change
		// return n instanceof BusinessObjectNode ? n.isEnabled_AddProperties() : false;

	}

	private void addContextualFacet(TLFacetType type) {
		ContextualFacetHandler cfh = new ContextualFacetHandler();
		ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		if (current != null && current instanceof BusinessObjectNode)
			cfh.addContextualFacet((BusinessObjectNode) current, TLFacetType.CUSTOM);

		// // Verify the current node is editable business object
		// ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		// if (current == null || !(current instanceof BusinessObjectNode) || !current.isEditable_newToChain()) {
		// DialogUserNotifier.openWarning("Add Custom Facet",
		// "Custom Facets can only be added to new Business Objects");
		// return;
		// }
		// BusinessObjectNode bo = (BusinessObjectNode) current;
		//
		// // Create the contextual facet
		// CustomFacetNode cf = new CustomFacetNode();
		// cf.setName("new");
		// bo.getLibrary().addMember(cf);
		// bo.getTLModelObject().addCustomFacet(cf.getTLModelObject());
		//
		// // Create contributed facet
		// NodeFactory.newComponentMember(bo, cf.getTLModelObject());
		// mc.refresh(bo);
	}

	private void addCustomFacet() {
		final TLFacetType facetType = TLFacetType.CUSTOM;
		ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		if (current == null || !(current instanceof BusinessObjectNode) || !current.isEditable_newToChain()) {
			DialogUserNotifier.openWarning("Add Custom Facet",
					"Custom Facets can only be added to new Business Objects");
			return;
		}

		// use if we allow custom facets to be added as minor version change
		// // Use the version utils in handler to create a minor or patch version if needed
		// if (current.isEnabled_AddProperties() && !current.isInHead())
		// current = handler.createVersionExtension(current);
		// if (current == null)
		// return;

		final BusinessObjectNode bo = (BusinessObjectNode) current;
		final ComponentNode propertyOwner = current.getFacet_Detail();

		// Set up and run the wizard
		String defaultContext = current.getLibrary().getDefaultContextId();
		String defaultName = defaultContext;
		final NewFacetWizard wizard = new NewFacetWizard(propertyOwner, defaultName);
		wizard.setValidator(new NewFacetValidator(current, facetType, wizard));
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			FacetNode newFacet = bo.addFacet(wizard.getName(), facetType);
			for (final PropertyNode n : wizard.getSelectedProperties()) {
				NodeFactory.newChild(newFacet, (TLModelElement) n.cloneTLObj());
			}
		}
		mc.refresh(bo);
	}

}

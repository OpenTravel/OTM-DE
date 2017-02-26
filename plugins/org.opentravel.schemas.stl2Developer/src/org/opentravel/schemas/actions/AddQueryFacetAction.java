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

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.commands.ContextualFacetHandler;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewFacetWizard;
import org.opentravel.schemas.wizards.validators.NewFacetValidator;

/**
 * @author Dave Hollander
 * 
 */
public class AddQueryFacetAction extends OtmAbstractAction {
	private static StringProperties propsDefault = new ExternalizedStringProperties("action.addQuery");

	/**
	 *
	 */
	public AddQueryFacetAction(final MainWindow mainWindow) {
		super(mainWindow, propsDefault);
	}

	public AddQueryFacetAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	@Override
	public void run() {
		if (OTM16Upgrade.otm16Enabled)
			addContextualFacet(TLFacetType.QUERY);
		else
			addQueryFacet();
	}

	// GlobalSelectionTester gst = new GlobalSelectionTester();
	// Assert.assertTrue(gst.test(co, GlobalSelectionTester.CANADD, null, null));

	@Override
	public boolean isEnabled() {
		// Unmanaged or in the most current (head) library in version chain.
		Node n = mc.getCurrentNode_NavigatorView().getOwningComponent();
		return n instanceof BusinessObjectNode ? n.isEditable_newToChain() : false;
	}

	private void addContextualFacet(TLFacetType type) {
		ContextualFacetHandler cfh = new ContextualFacetHandler();
		ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		if (current != null && current instanceof BusinessObjectNode)
			cfh.addContextualFacet((BusinessObjectNode) current, TLFacetType.QUERY);

		// // Verify the current node is editable business object
		// ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		// if (current == null || !(current instanceof BusinessObjectNode) || !current.isEditable_newToChain()) {
		// DialogUserNotifier.openWarning("Add Query Facet", "Query Facets can only be added to new Business Objects");
		// return;
		// }
		// BusinessObjectNode bo = (BusinessObjectNode) current;
		//
		// // Create the contextual facet
		// QueryFacetNode qf = new QueryFacetNode();
		// qf.setName("new");
		// bo.getLibrary().addMember(qf);
		// bo.getTLModelObject().addQueryFacet(qf.getTLModelObject());
		//
		// // Create contributed facet
		// NodeFactory.newComponentMember(bo, qf.getTLModelObject());
		// mc.refresh(bo);
	}

	private void addQueryFacet() {
		final TLFacetType facetType = TLFacetType.QUERY;
		final ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		if (current == null || !(current instanceof BusinessObjectNode) || !current.isEditable_newToChain()) {
			DialogUserNotifier.openWarning("Add Query Facet",
					"Query Facets can only be added to non-versioned Business Objects");
			// LOGGER.warn("New custom facet can be added only to Business Objects, tried to add to: " + current);
			return;
		}

		final BusinessObjectNode bo = (BusinessObjectNode) current;
		final ComponentNode propertyOwner = current;

		// Set up and run the wizard
		String defaultName = "";
		final NewFacetWizard wizard = new NewFacetWizard(propertyOwner, defaultName);
		wizard.setValidator(new NewFacetValidator(current, facetType, wizard));
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			FacetNode newFacet = bo.addFacet(wizard.getName(), facetType);
			for (final PropertyNode n : wizard.getSelectedProperties()) {
				NodeFactory.newMember(newFacet, n.cloneTLObj());
			}
		}
		mc.refresh(bo);
	}

}

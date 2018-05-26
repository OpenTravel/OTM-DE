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
/**
 * 
 */
package org.opentravel.schemas.commands;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ActionFacet;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ActionResponse;
import org.opentravel.schemas.node.resources.ParamGroup;
import org.opentravel.schemas.node.resources.ParentRef;
import org.opentravel.schemas.node.resources.ResourceBuilder;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.wizards.TypeSelectionWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command Handler for resource related commands. Commands are enabled/disabled in plugin.xml handler extensions.
 * 
 * @author Dave Hollander
 *
 */
public class ResourceCommandHandler extends OtmAbstractHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCommandHandler.class);
	public static String COMMAND_ID = "org.opentravel.schemas.commands.newResource";

	// Enumeration of the types of command actions nodes can handle.
	// Used in plugin.xml for commandId
	public static enum CommandType {
		DELETE, PARENTREF, ACTION, ACTIONFACET, PARAMGROUP, RESOURCE, NONE, ACTIONRESPONSE, ACTIONREQUEST, WIZARD
	}

	private Node selectedNode; // The user selected node.
	private BusinessObjectNode predicate;
	private OtmView view;

	protected Node getSelectedNode(ExecutionEvent exEvent) {
		return mc.getGloballySelectNode();
	}

	@Override
	public boolean isEnabled() {
		// IWorkbenchWindow ww;
		// ww = UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		// IWorkbenchPage wp = ww.getActivePage();
		// Node n = getFirstSelected();
		Node rn = mc.getCurrentNode_ResourceView();
		if (!rn.isEditable())
			mc.postStatus("Resource must be in latest version of a library chain to be edited.");
		return rn.isEditable();
	}

	// Used by Actions
	public void execute(Event event) {
		if (event.data instanceof CommandType) {
			setSelected();
			runCommand((CommandType) event.data);
		}
		return;
	}

	// Entry point from command execution
	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		// String filePathParam = exEvent.getParameter("org.opentravel.schemas.stl2Developer.newAction");
		// LOGGER.debug(filePathParam);

		view = OtmRegistry.getResourceView();
		setSelected();
		if (selectedNode == null)
			return null; // Nothing to act on

		runCommand(getCmdType(exEvent.getCommand().getId()));
		if (view != null)
			view.activate();
		return null;
	}

	private ResourceCommandHandler.CommandType getCmdType(String cmdId) {
		CommandType cmdType = CommandType.RESOURCE;
		if (selectedNode.getOwningComponent() instanceof ResourceNode)
			if (cmdId.endsWith(CommandType.ACTION.toString()))
				cmdType = CommandType.ACTION;
			else if (cmdId.endsWith(CommandType.ACTIONFACET.toString()))
				cmdType = CommandType.ACTIONFACET;
			else if (cmdId.endsWith(CommandType.PARAMGROUP.toString()))
				cmdType = CommandType.PARAMGROUP;
			// else if (cmdId.endsWith(CommandType.ACTIONREQUEST.toString()))
			// cmdType = CommandType.ACTIONREQUEST;
			else if (cmdId.endsWith(CommandType.ACTIONRESPONSE.toString()))
				cmdType = CommandType.ACTIONRESPONSE;
			else if (cmdId.endsWith(CommandType.PARENTREF.toString()))
				cmdType = CommandType.PARENTREF;
			else if (cmdId.endsWith(CommandType.DELETE.toString()))
				cmdType = CommandType.DELETE;
			else if (cmdId.endsWith(CommandType.WIZARD.toString()))
				cmdType = CommandType.WIZARD;
		return cmdType;
	}

	/**
	 * If view is set return the current node from the view. Use navigator node otherwise.
	 */
	private void setSelected() {
		if (mc.getCurrentNode_NavigatorView() instanceof BusinessObjectNode)
			predicate = (BusinessObjectNode) mc.getCurrentNode_NavigatorView();

		selectedNode = getFirstSelected();
		if (selectedNode == null)
			if (view != null)
				selectedNode = (Node) view.getCurrentNode();
			else
				selectedNode = mc.getCurrentNode_NavigatorView();
	}

	/**
	 * @return the library if unmanaged, library at the head of the chain or null if not editable
	 */
	private LibraryNode getEffectiveLibrary(Node node) {
		LibraryNode effectiveLib = node.getLibrary();
		if (effectiveLib != null) {
			// If it is in a chain, get the head of the chain
			if (effectiveLib.getChain() != null && !effectiveLib.isInHead())
				effectiveLib = effectiveLib.getChain().getHead();
			return effectiveLib.isEditable() ? effectiveLib : null;
		}
		return null;
	}

	private void runCommand(CommandType type) {
		ResourceNode rn = null;
		if (selectedNode == null)
			return;

		if (selectedNode.getOwningComponent() instanceof ResourceNode)
			rn = (ResourceNode) selectedNode.getOwningComponent();
		// Make sure we have the latest version of the resource
		if (rn != null && rn.getVersionNode() != null)
			rn = (ResourceNode) rn.getVersionNode().get();

		switch (type) {
		case DELETE:
			Node owner = (Node) selectedNode.getOwningComponent();
			List<Node> nodes = view.getSelectedNodes();
			new DeleteNodesHandler().deleteNodes(nodes);

			// for (Node n : view.getSelectedNodes())
			// if (owner.isEditable() && n.isDeleteable())
			// n.delete();
			// else
			// postWarning(type, n);
			view.refresh(owner);
			if (owner instanceof ResourceNode)
				OtmRegistry.getNavigatorView().refresh(); // refresh entire navigator view tree because content changed
			break;
		case RESOURCE:
			// Try to get an editable library
			LibraryNode effectiveLib = getEffectiveLibrary(selectedNode);
			if (effectiveLib != null) {
				// run wizard
				predicate = getBusinessObject();
				if (predicate == null)
					if (!DialogUserNotifier.openQuestion("New Resource",
							"Do you want to create an abstract resource? If yes, you will be asked to select a base response object for the error response action facet."))
						return;
				ResourceNode newR = new ResourceNode(effectiveLib, predicate); // create named empty resource
				if (predicate != null)
					new ResourceBuilder().build(newR, predicate);
				else {
					// Build an abstract resource
					ActionFacet af = new ResourceBuilder().buildAbstract(newR, true);
					final TypeSelectionWizard wizard = new TypeSelectionWizard(af);
					if (wizard.run(OtmRegistry.getActiveShell()))
						af.setBasePayload(wizard.getSelection());
				}

				if (view != null) {
					view.select(newR);
					view.refresh(newR);
				}
				mc.refresh(); // update the navigator view
			} else
				postWarning(type);
			break;
		case ACTION:
			if (rn != null) {
				// If in a chain and not in head, create a new version
				if (!rn.isEditable_newToChain())
					rn = (ResourceNode) createVersionExtension(rn);
				if (rn == null)
					return;
				view.refresh(new ActionNode(rn));
			} else
				postWarning(type);
			break;
		case ACTIONFACET:
			if (rn != null) {
				// If in a chain and not in head, create a new version
				if (!rn.isEditable_newToChain())
					rn = (ResourceNode) createVersionExtension(rn);
				if (rn == null)
					return;
				view.refresh(new ActionFacet(rn));
			} else
				postWarning(type);
			break;
		case PARAMGROUP:
			if (rn != null) {
				// If in a chain and not in head, create a new version
				if (!rn.isEditable_newToChain())
					rn = (ResourceNode) createVersionExtension(rn);
				if (rn == null)
					return;
				view.refresh(new ParamGroup(rn));
			} else
				postWarning(type);
			break;
		case ACTIONRESPONSE:
			if (selectedNode instanceof ActionNode) {
				// If in a chain and not in head, create a new version
				if (!rn.isEditable_newToChain())
					rn = (ResourceNode) createVersionExtension(rn);
				if (rn == null)
					return;
				view.refresh(new ActionResponse((ActionNode) selectedNode));
			} else
				postWarning(type);
			break;
		case PARENTREF:
			if (selectedNode instanceof ResourceNode) {
				// If in a chain and not in head, create a new version
				if (!rn.isEditable_newToChain())
					rn = (ResourceNode) createVersionExtension(rn);
				if (rn == null)
					return;
				view.refresh(new ParentRef((ResourceNode) selectedNode));
			} else
				postWarning(type);
			break;
		case WIZARD:
			baseResponseWizard(selectedNode);
			view.refresh();
			break;
		case NONE:
		default:
			DialogUserNotifier.openWarning("Not Supported", "Not supported for this object type.");
		}
	}

	private void baseResponseWizard(Node n) {
		ResourceNode rn = null;
		ActionFacet af = null;
		if (n instanceof ResourceNode)
			rn = (ResourceNode) n;
		else if (n instanceof ResourceMemberInterface)
			rn = ((ResourceMemberInterface) n).getOwningResource();

		if (rn == null) {
			DialogUserNotifier.openWarning("Not Supported",
					"Wizard does not support objects of type " + n.getClass().getSimpleName());
			return;
		}

		// Get the first action facet for the wizard
		if (!rn.getActionFacets().isEmpty())
			af = rn.getActionFacets().get(0);

		// Run selection wizard to get base response object
		if (af != null && OtmRegistry.getActiveShell() != null) {
			final TypeSelectionWizard wizard = new TypeSelectionWizard(af);
			if (wizard.run(OtmRegistry.getActiveShell()))
				rn.setAllActionFacets(wizard.getSelection());
		} else
			DialogUserNotifier.openWarning("Not Supported", "There are no action facets for the wizard to work with.");
	}

	private void postWarning(CommandType type) {
		switch (type) {
		case DELETE:
			DialogUserNotifier.openWarning("Can Not Delete", "The state of this library does not allow deletion.");
			break;
		case RESOURCE:
			DialogUserNotifier.openWarning("Can Not Create Resource",
					"The state of this library does not allow adding resources.");
			break;
		default:
			DialogUserNotifier.openWarning("Missing Subject", "Can not find the parent for the new item.");
		}
	}

	/**
	 * If the command was run from a business object, return that object otherwise run type selection wizard to get a
	 * business object.
	 * 
	 * @param rn
	 * @return business object or null.
	 */
	private BusinessObjectNode getBusinessObject() {
		if (mc.getCurrentNode_NavigatorView() instanceof BusinessObjectNode)
			return (BusinessObjectNode) mc.getCurrentNode_NavigatorView();

		// post a business object only Type Selection then pass the selected node.
		Node subject = null;
		ResourceNode rn = new ResourceNode(new TLResource());
		final TypeSelectionWizard wizard = new TypeSelectionWizard(rn);
		if (wizard.run(OtmRegistry.getActiveShell())) {
			subject = wizard.getSelection();
		}
		rn.delete();
		return (BusinessObjectNode) (subject instanceof BusinessObjectNode ? subject : null);
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	public static ImageDescriptor getIcon() {
		return Images.getImageRegistry().getDescriptor(Images.AddNode);
	}

	public Image getImage() {
		return Images.getImageRegistry().get(Images.AddNode);
	}

	public static ImageDescriptor getIcon(CommandType type) {
		if (type.equals(CommandType.DELETE))
			return Images.getImageRegistry().getDescriptor(Images.Delete);
		else
			return Images.getImageRegistry().getDescriptor(Images.AddNode);
	}

}

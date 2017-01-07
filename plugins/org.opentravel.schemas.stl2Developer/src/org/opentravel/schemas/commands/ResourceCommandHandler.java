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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ActionFacet;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ActionResponse;
import org.opentravel.schemas.node.resources.ParamGroup;
import org.opentravel.schemas.node.resources.ParentRef;
import org.opentravel.schemas.node.resources.ResourceBuilder;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.wizards.TypeSelectionWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command Handler for resource related commands.
 * 
 * @author Dave Hollander
 *
 */
public class ResourceCommandHandler extends OtmAbstractHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCommandHandler.class);
	public static String COMMAND_ID = "org.opentravel.schemas.commands.newResource";

	// Enumeration of the types of command actions nodes can handle.
	public static enum CommandType {
		DELETE, PARENTREF, ACTION, ACTIONFACET, PARAMGROUP, RESOURCE, NONE, ACTIONRESPONSE, ACTIONREQUEST
	}

	private Node selectedNode; // The user selected node.
	private BusinessObjectNode predicate;
	private OtmView view;

	protected Node getSelectedNode(ExecutionEvent exEvent) {
		return mc.getGloballySelectNode();
	}

	// Used by Actions
	public void execute(Event event) {
		if (event.data instanceof CommandType) {
			setSelected();
			runCommand((CommandType) event.data);
		}
		return;
		// mc.postStatus("Resource Handler.");
	}

	// Used by buttons
	// public void execute(SelectionEvent event) {
	// view = OtmRegistry.getResourceView();
	// selectedNode = (Node) view.getCurrentNode();
	// runCommand(ResourceCommandHandler.CommandType.RESOURCE);
	// // mc.postStatus("Resource Handler.");
	// }

	// Entry point from command execution
	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		String filePathParam = exEvent.getParameter("org.opentravel.schemas.stl2Developer.newAction");
		LOGGER.debug(filePathParam);

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
		return cmdType;
	}

	/**
	 * If view is set return the current node from the view. Use navigator node otherwise.
	 */
	private void setSelected() {
		if (mc.getCurrentNode_NavigatorView() instanceof BusinessObjectNode)
			predicate = (BusinessObjectNode) mc.getCurrentNode_NavigatorView();

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
		}
		return effectiveLib.isEditable() ? effectiveLib : null;
	}

	private void runCommand(CommandType type) {
		ResourceNode rn = null;
		if (selectedNode.getOwningComponent() instanceof ResourceNode)
			rn = (ResourceNode) selectedNode.getOwningComponent();

		if (selectedNode == null) {
			return;
		}

		switch (type) {
		case DELETE:
			Node owner = selectedNode.getOwningComponent();
			for (Node n : view.getSelectedNodes())
				if (n.isDeleteable())
					n.delete();
				else
					postWarning(type, n);
			view.refresh(owner);
			if (owner instanceof ResourceNode)
				OtmRegistry.getNavigatorView().refresh(); // refresh entire navigator view tree because content changed
			break;
		case RESOURCE:
			// Try to get an editable library
			LibraryNode effectiveLib = getEffectiveLibrary(selectedNode);
			if (effectiveLib != null) {
				ResourceNode newR = new ResourceNode(effectiveLib, predicate); // create named empty resource

				// Use the preselected BO or run wizard.
				if (predicate == null)
					predicate = getBusinessObject(newR);

				if (predicate == null)
					newR.setAbstract(true);
				else
					new ResourceBuilder().build(newR, predicate);
				view.select(newR);
				view.refresh(newR);
				mc.refresh(); // update the navigator view
			} else
				postWarning(type);
			break;
		case ACTION:
			if (rn != null)
				view.refresh(new ActionNode(rn));
			else
				postWarning(type);
			break;
		case ACTIONFACET:
			if (rn != null)
				view.refresh(new ActionFacet(rn));
			else
				postWarning(type);
			break;
		case PARAMGROUP:
			if (rn != null)
				view.refresh(new ParamGroup(rn));
			else
				postWarning(type);
			break;
		case ACTIONRESPONSE:
			if (selectedNode instanceof ActionNode)
				view.refresh(new ActionResponse((ActionNode) selectedNode));
			else
				postWarning(type);
			break;
		case PARENTREF:
			if (selectedNode instanceof ResourceNode)
				view.refresh(new ParentRef((ResourceNode) selectedNode));
			else
				postWarning(type);
			break;
		case NONE:
		default:
			DialogUserNotifier.openWarning("Not Supported", "Not supported for this object type.");
		}
	}

	private void postWarning(CommandType type, Node n) {
		if (n.getLibrary() == null)
			LOGGER.debug(n + " has no library.");
		postWarning(type);
	}

	private void postWarning(CommandType type) {
		switch (type) {
		case DELETE:
			DialogUserNotifier.openWarning("Can Not Delete", "The state of this library does not allow deletion.");
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
	private BusinessObjectNode getBusinessObject(ResourceNode rn) {
		// post a business object only Type Selection then pass the selected node.
		if (mc.getCurrentNode_NavigatorView() instanceof BusinessObjectNode)
			return (BusinessObjectNode) mc.getCurrentNode_NavigatorView();

		Node subject = null;
		final TypeSelectionWizard wizard = new TypeSelectionWizard(rn);
		if (wizard.run(OtmRegistry.getActiveShell())) {
			subject = wizard.getSelection();
		}
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

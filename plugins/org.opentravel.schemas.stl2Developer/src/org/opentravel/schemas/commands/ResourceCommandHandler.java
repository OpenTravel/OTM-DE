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

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.resources.ActionFacet;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ActionResponse;
import org.opentravel.schemas.node.resources.ParamGroup;
import org.opentravel.schemas.node.resources.ResourceBuilder;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.wizards.TypeSelectionWizard;

/**
 * Command Handler for resource related commands.
 * 
 * @author Dave Hollander
 *
 */
public class ResourceCommandHandler extends OtmAbstractHandler {
	// private static final Logger LOGGER = LoggerFactory.getLogger(AddNodeHandler2.class);
	public static String COMMAND_ID = "org.opentravel.schemas.commands.newResource";

	// Enumeration of the types of command actions nodes can handle.
	public static enum CommandType {
		DELETE, ACTION, ACTIONFACET, PARAMGROUP, RESOURCE, NONE, ACTIONRESPONSE, ACTIONREQUEST
	}

	private Node selectedNode; // The user selected node.
	private OtmView view;

	public void execute(Event event) {
		if (mc == null)
			mc = OtmRegistry.getMainController();
		selectedNode = mc.getGloballySelectNode();
		if (!(selectedNode instanceof ResourceMemberInterface))
			return;
		// mc.postStatus("Resource Handler.");
	}

	public void execute(SelectionEvent event) {
		view = OtmRegistry.getResourceView();
		selectedNode = (Node) view.getCurrentNode();
		runCommand(ResourceCommandHandler.CommandType.RESOURCE);
		// mc.postStatus("Resource Handler.");
	}

	// Entry point from command execution
	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		view = OtmRegistry.getResourceView();
		selectedNode = (Node) view.getCurrentNode();
		Command cmd = exEvent.getCommand();
		runCommand(getCmdType(cmd.getId()));
		// mc.postStatus("Resource Handler.");
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
			else if (cmdId.endsWith(CommandType.DELETE.toString()))
				cmdType = CommandType.DELETE;
		return cmdType;
	}

	private void runCommand(CommandType type) {
		ResourceNode rn = null;
		if (selectedNode.getOwningComponent() instanceof ResourceMemberInterface)
			rn = (ResourceNode) selectedNode.getOwningComponent();
		if (rn == null) {
			return;
		}

		switch (type) {
		case DELETE:
			if (selectedNode.isDeleteable())
				selectedNode.delete();
			view.refresh();
			break;
		case RESOURCE:
			ResourceNode newR = new ResourceNode(selectedNode); // create named empty resource
			BusinessObjectNode bo = getBusinessObject(newR);
			new ResourceBuilder().build(newR, bo);
			view.refresh(newR);
			// view.refresh(new ResourceNode(selectedNode));
			break;
		case ACTION:
			view.refresh(new ActionNode(rn));
			break;
		case ACTIONFACET:
			view.refresh(new ActionFacet(rn));
			break;
		case PARAMGROUP:
			view.refresh(new ParamGroup(rn));
			break;
		case ACTIONRESPONSE:
			view.refresh(new ActionResponse((ActionNode) selectedNode));
			break;
		case NONE:
		default:
			DialogUserNotifier.openWarning("Not Supported", "Not supported for this object type.");
		}
	}

	private BusinessObjectNode getBusinessObject(ResourceNode rn) {
		// post a business object only Type Selection then pass the selected node.
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

}

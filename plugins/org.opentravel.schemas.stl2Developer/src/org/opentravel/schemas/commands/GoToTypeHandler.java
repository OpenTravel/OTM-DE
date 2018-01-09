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

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
import org.opentravel.schemas.views.NavigatorView;
import org.opentravel.schemas.views.OtmView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If selection on active view contains node with type, then it will select this node type in Navigator View. It should
 * be only enabled when {@link ComponentNode} is selected and has assigned type.
 * 
 * @author Pawel Jedruch
 * 
 */
public class GoToTypeHandler extends AbstractHandler {

	public final static String COMMAND_ID = "org.opentravel.schemas.commands.goto.type";

	private final static Logger LOGGER = LoggerFactory.getLogger(GoToTypeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart view = HandlerUtil.getActivePart(event);
		if (view instanceof OtmView) {
			selectType((OtmView) view);
			return null;
		}
		return null;
	}

	private void selectType(OtmView view) {
		Node typeNode = getTypeNode(view);
		select(typeNode);
	}

	private void select(Node type) {
		// LOGGER.debug("Selecting: " + type.toString());
		NavigatorView view = (NavigatorView) OtmRegistry.getNavigatorView();
		if (view == null)
			return;
		// if (view.isReachable(type)) { // 8/18/2015 dmh - commented out because it doesn't work and uses test hook.
		view.expand(type);
		OtmRegistry.getMainController().selectNavigatorNodeAndRefresh(type);
		if (view.isFilterActive()) {
			DialogUserNotifier.openInformation("WARNING", Messages.getString("action.goto.unreachable.filter"));
		}
	}

	/**
	 * we can ignore checks because of enableWhen declarations for this handler in plugin.xml
	 * 
	 * @param typeView
	 * @return type node for selected node in view.
	 */
	private Node getTypeNode(OtmView typeView) {
		List<Node> nodes = typeView.getSelectedNodes();
		Node n = nodes.get(0);
		return getTypeNode(n);
	}

	private Node getTypeNode(Node node) {
		if (node instanceof WhereUsedNode)
			return node.getParent();

		if (node instanceof TypeUser)
			return (Node) ((TypeUser) node).getAssignedType();

		if (node instanceof ContributedFacetNode)
			return ((ContributedFacetNode) node).getContributor();

		return node;

		// return (Node) node.getTypeClass().getTypeNode();
	}

}

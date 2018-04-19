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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.views.TypeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for sorting properties of a node.
 * 
 * @author Dave Hollander
 * 
 */
public class SortNodeHandler extends OtmAbstractHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SortNodeHandler.class);
	public static final String COMMAND_ID = "org.opentravel.schemas.commands.Sort";

	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		IWorkbenchPart activeView = HandlerUtil.getActivePart(exEvent);
		List<Node> selectedNodes = getSelectedNodes(activeView);
		Set<Sortable> toSort = new HashSet<>();

		// Get a set of nodes to sort, properties will be sorted by their parent
		for (Node node : selectedNodes) {
			if (node instanceof PropertyNode)
				node = node.getParent();
			if (node instanceof Sortable)
				toSort.add((Sortable) node);
		}
		for (Sortable node : toSort)
			node.sort();

		mc.refresh();
		return null;
	}

	@Override
	public boolean isEnabled() {
		Node selected = getFirstSelected();
		return selected != null && selected.isEditable() && selected instanceof FacetInterface;
	}

	private List<Node> getSelectedNodes(IWorkbenchPart activeView) {
		List<Node> selectedNodes = Collections.emptyList();
		;
		if (activeView instanceof OtmView) {
			OtmView view = (OtmView) activeView;
			if (view instanceof TypeView) {
				selectedNodes = getTypeViewSelection((TypeView) view);
			}
		}
		if (selectedNodes.isEmpty()) {
			selectedNodes = mc.getGloballySelectNodes();
		}
		return selectedNodes;
	}

	private List<Node> getTypeViewSelection(TypeView view) {
		List<Node> selectedNodes = view.getSelectedNodes();
		if (selectedNodes == null || selectedNodes.isEmpty())
			return Collections.singletonList(view.getCurrentNode());
		return selectedNodes;
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

}

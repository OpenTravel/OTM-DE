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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Images;

/**
 * 
 * @author Dave Hollander
 * 
 */

public class MergeHandler extends OtmAbstractHandler {
	// private static final Logger LOGGER = LoggerFactory.getLogger(MergeNodesAction.class);

	public static String COMMAND_ID = "org.opentravel.schemas.commands.MergeNodes";

	private List<Node> toMerge = new ArrayList<>();

	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		if (isEnabled()) {
			Node selectedNode = toMerge.get(0);
			Node newNode = ((Node) selectedNode.getOwningComponent()).clone("_Merged");

			int cnt = 0;
			for (Node node : mc.getSelectedNodes_NavigatorView()) {
				// LOGGER.debug("Merge this node: " + node + " into " + newNode);
				if (node != null && node != selectedNode) {
					newNode.merge(node);
					cnt++;
				}
			}
			mc.refresh(selectedNode.getParent());

			mc.postStatus("Merged " + cnt + " components.");
			// LOGGER.debug("Merge Nodes Command Handler " + selectedNode);
		}
		return null;
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	@Override
	public boolean isEnabled() {
		toMerge.clear();
		List<Node> newSelection = mc.getSelectedNodes_NavigatorView();
		if (newSelection.size() > 1 && newSelection.get(0).isMergeSupported() && isSameObjects(newSelection)) {
			toMerge = newSelection;
			return newSelection.get(0).isEditable_newToChain();
		}
		return false;
	}

	private boolean isSameObjects(List<Node> newSelection) {
		Class<? extends Node> first = null;
		for (Node node : newSelection) {
			if (first == null) {
				first = node.getClass();
			} else if (!first.equals(node.getClass())) {
				return false;
			}
		}
		return true;
	}

	public static ImageDescriptor getIcon() {
		return Images.getImageRegistry().getDescriptor(Images.MergeNodes);
	}
}

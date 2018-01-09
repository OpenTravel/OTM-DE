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
package org.opentravel.schemas.types.whereused;

import java.util.Collections;
import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;

/**
 * Leaf node describing an ExtensionOwner. Exposes which specific versioned object in a chain provides the extension
 * base to the user.
 * 
 * @author Dave Hollander
 * 
 */
public class ExtensionUserNode extends WhereUsedNode<ExtensionOwner> implements WhereUsedNodeInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionUserNode.class);

	public ExtensionUserNode(ExtensionOwner user) {
		super(user); // sets owner

		String label = "--no owner--";
		if (owner.getOwningComponent() != null)
			label = ((Node) owner.getOwningComponent()).getNameWithPrefix();
		labelProvider = simpleLabelProvider(label);
		imageProvider = nodeImageProvider((Node) owner.getOwningComponent());
	}

	@Override
	public String getDecoration() {
		String decoration = "  ";
		decoration += "extends ";
		decoration += " " + owner.getExtensionBase().getName();
		// decoration += " " + owner.getExtensionBase().getNameWithPrefix();
		return decoration;
	}

	@Override
	public List<Node> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		return false;
	}

}

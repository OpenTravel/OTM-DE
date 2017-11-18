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
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.types.TypeUser;

/**
 * Leaf node describing a TypeUser. Used as leaf node in both where used and uses trees.
 * 
 * Exposes which specific versioned objects in a chain provides the assigned type.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeUserNode extends WhereUsedNode<TypeUser> implements WhereUsedNodeInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(TypeProviderUserNode.class);

	public TypeUserNode(TypeUser user) {
		super(user); // sets owner

		String label = "--no owner--";
		if (user.getOwningComponent() != null)
			label = user.getOwningComponent().getNameWithPrefix();
		labelProvider = simpleLabelProvider(label);
		imageProvider = nodeImageProvider((Node) user.getOwningComponent());
	}

	@Override
	public boolean isEditable() {
		if (owner == null || owner.getLibrary() == null)
			return false;
		if (owner.getLibrary().isFinal())
			return false;
		if (!owner.getLibrary().isEditable())
			return false;

		// Not editable if owner is Minor and not new to chain and assigned the latest version
		if (((Node) owner).getLibrary().isMinorVersion() && !((Node) owner).isNewToChain()
				&& ((Node) owner.getAssignedType()).isInHead())
			return false;
		return ((Node) owner).getLibrary().isInHead2();
	}

	@Override
	public String getDecoration() {
		String decoration = "  ";
		decoration += "uses ";
		decoration += " " + ((Node) owner.getAssignedType()).getNameWithPrefix();
		// decoration += this.getClass().getSimpleName();
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

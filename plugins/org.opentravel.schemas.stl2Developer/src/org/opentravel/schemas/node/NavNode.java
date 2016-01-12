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
package org.opentravel.schemas.node;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Navigation Nodes describe GUI model objects that are not part of the TL Model. They ease navigating the GUI and
 * <b>not</b> representing the OTM model.
 * 
 * @author Dave Hollander
 * 
 */
public class NavNode extends Node {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(NavNode.class);

	private String name = "";

	/**
	 * Create a navigation node, get ns and prefix from parentNode node. link to parentNode node.
	 * 
	 * @param name
	 * @param parent
	 */
	public NavNode(final String name, final Node parent) {
		super();
		assert (parent != null) : "Parent is null.";
		setName(name);
		setIdentity("NavNode:" + getName());
		setLibrary(parent.getLibrary());
		// Don't break version node-component node bond.
		if (parent instanceof VersionNode)
			parent.getParent().linkChild(this, false);
		else
			parent.linkChild(this, false); // link without doing family tests.
	}

	public boolean isComplexRoot() {
		return this == getLibrary().getComplexRoot() ? true : false;
	}

	public boolean isResourceRoot() {
		return getLibrary() != null ? this == getLibrary().getResourceRoot() : false;
	}

	@Override
	public void linkLibrary(LibraryNode lib) {
		if (lib != null && !getChildren().contains(lib))
			getChildren().add(lib);
	}

	@Override
	public Image getImage() {
		if (isResourceRoot())
			return Images.getImageRegistry().get(Images.Resources);
		return Images.getImageRegistry().get(Images.Folder);
	}

	@Override
	public boolean isLibraryContainer() {
		return false;
	}

	public boolean isSimpleRoot() {
		return this == getLibrary().getSimpleRoot() ? true : false;
	}

	public boolean isServiceRoot() {
		return this == getLibrary().getServiceRoot() ? true : false;
	}

	// public boolean isElementRoot() {
	// return this == getLibrary().getElementRoot() ? true : false;
	// }

	@Override
	public boolean isNavigation() {
		return true;
	}

	@Override
	public String getComponentType() {
		return "Navigation Node";
	}

	/**
	 * For navigation nodes, return the default component type for the type of navigation node.
	 */
	@Override
	public ComponentNodeType getComponentNodeType() {
		if (isSimpleRoot())
			return ComponentNodeType.SIMPLE;
		else
			return ComponentNodeType.CORE;
	}

	// @Override
	// public List<Node> getNavChildren() {
	// return getChildren();
	// }

	@Override
	public boolean hasNavChildren() {
		return !getChildren().isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#hasChildren_TypeProviders()
	 */
	@Override
	public boolean hasChildren_TypeProviders() {
		return getChildren().size() > 0;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getLabel() {
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

}

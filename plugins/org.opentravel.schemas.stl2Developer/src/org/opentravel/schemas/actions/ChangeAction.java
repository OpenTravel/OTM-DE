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

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * Attached to the navigator menus.
 * 
 * 
 */
public class ChangeAction extends OtmAbstractAction {
	private final static StringProperties propsDefault = new ExternalizedStringProperties("action.changeObject");

	/**
	 *
	 */
	public ChangeAction(final MainWindow mainWindow) {
		super(mainWindow, propsDefault);
	}

	public ChangeAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		getMainController().changeTreeSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		Node n = (Node) getMainController().getCurrentNode_NavigatorView().getOwningComponent();
		if (n instanceof BusinessObjectNode || n instanceof CoreObjectNode || n instanceof VWA_Node) {
			return n.getChain() == null ? n.isEditable() : n.getChain().isMajor();
		}
		return false;
	}

}

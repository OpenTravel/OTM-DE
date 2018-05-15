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

import java.util.List;

import org.opentravel.schemas.commands.OtmAbstractHandler;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;

/**
 * Establish an action with window context and string properties.
 * 
 * @param mainWindow
 * @param props
 *            - user implementations such as ExternalizedStringProperties() to establish string values for
 *            PropertyType.TEXT, PropertyType.TOOLTIP, PropertyType.IMAGE as saved in messages.properties and in the
 *            image registry.
 * 
 *            Example: new AddEnumValueAction(mainWindow, new ExternalizedStringProperties("action.addEnumValue"));
 * 
 * @author Dave
 * 
 */
public class OtmAbstractAction extends OtmAbstractBaseAction {

	private final MainWindow mainWindow;
	protected final MainController mc;

	public OtmAbstractAction(final MainWindow mainWindow, final StringProperties props) {
		super(props);
		this.mainWindow = mainWindow;
		mc = OtmRegistry.getMainController();
	}

	public OtmAbstractAction(final StringProperties props) {
		super(props);
		this.mainWindow = OtmRegistry.getMainWindow();
		mc = OtmRegistry.getMainController();
	}

	public OtmAbstractAction(final MainWindow mainWindow, final StringProperties props, final int style) {
		super(props, style);
		this.mainWindow = mainWindow;
		mc = OtmRegistry.getMainController();
	}

	public OtmAbstractAction(MainWindow mainWindow) {
		super();
		this.mainWindow = mainWindow;
		mc = OtmRegistry.getMainController();
	}

	/**
	 * @return the first globally selected node or null.
	 * @see org.opentravel.schemas.controllers.MainController#getGloballySelectNodes()
	 * @see OtmAbstractHandler#getFirstSelected()
	 */
	public Node getFirstSelected() {
		if (mc == null)
			return null;
		List<Node> selectedNodes = mc.getGloballySelectNodes();
		Node selection = null;
		if (selectedNodes != null && !selectedNodes.isEmpty())
			selection = selectedNodes.get(0);
		return selection;
	}

	// TODO - actions seem to use navigator view not globally selected.
	// TODO - examine selection controls then reconcile behavior
	public Node getNavigatorSelection() {
		return mc != null ? mc.getSelectedNode_NavigatorView() : null;
	}

	/**
	 * @return owning component of the navigator selection or null
	 */
	public LibraryMemberInterface getOwnerOfNavigatorSelection() {
		if (mc == null)
			return null;
		Node n = mc.getSelectedNode_NavigatorView();
		return n != null ? n.getOwningComponent() : null;
	}

	public MainWindow getMainWindow() {
		return mainWindow;
	}

	public MainController getMainController() {
		return mc;
	}

}

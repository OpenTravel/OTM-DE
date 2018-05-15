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
package org.opentravel.schemas.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.whereused.ExtensionUserNode;
import org.opentravel.schemas.types.whereused.TypeUserNode;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all classes that extend ViewPart to be a workbench view.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmAbstractView extends ViewPart implements OtmView {
	private static final Logger LOGGER = LoggerFactory.getLogger(OtmAbstractView.class);

	protected MainController mc;
	private MainWindow mainWindow;

	protected OtmAbstractView() {
		this(OtmRegistry.getMainController());
	}

	protected OtmAbstractView(MainController mc) {
		this.mc = mc;
		if (mc == null) {
			throw new IllegalArgumentException("Tried to construct view without a main controller.");
		}
		mainWindow = mc.getMainWindow();
	}

	protected MainWindow getMainWindow() {
		return mainWindow;
	}

	protected MainController getMainController() {
		return mc;
	}

	@Override
	public boolean activate() {
		if (mainWindow == null)
			return false;
		IWorkbenchPage page = null;

		try {
			PlatformUI.getWorkbench();
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null)
				page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		} catch (IllegalStateException e) {
			return false; // No workbench or display.
		}

		if (page != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(this);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(this.getViewID());
			} catch (PartInitException e) {
				LOGGER.debug("Error showing view: " + getViewID());
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public void refreshAllViews() {
		for (OtmView view : OtmRegistry.getAllActiveViews()) {
			view.refresh();
		}
	}

	@Override
	public void refreshAllViews(INode node) {
		for (OtmView view : OtmRegistry.getAllActiveViews()) {
			view.refresh(node);
		}
	}

	@Override
	public void refresh(INode node, boolean force) {
		refresh(node);
	}

	// Override this method if the view can recreate its contents.
	@Override
	public void refresh(boolean regenerate) {
		refresh();
	}

	/** ************** Override if needed for a view ***************** **/
	@Override
	public void clearFilter() {
	}

	@Override
	public void clearSelection() {
	}

	@Override
	public INode getPreviousNode() {
		return null;
	}

	@Override
	public void collapse() {
	}

	@Override
	public void expand() {
	}

	@Override
	public boolean isShowInheritedProperties() {
		return OtmRegistry.getNavigatorView() != null ? OtmRegistry.getNavigatorView().isShowInheritedProperties()
				: false;
	}

	@Override
	public boolean isListening() {
		return true;
	}

	@Override
	public void select(INode node) {
	}

	@Override
	public void setDeepPropertyView(boolean state) {
	}

	@Override
	public void setExactMatchFiltering(boolean state) {
	}

	@Override
	public void setInput(INode node) {
	}

	@Override
	public void setInheritedPropertiesDisplayed(boolean state) {
	}

	@Override
	public void setListening(boolean state) {
	}

	@Override
	public void restorePreviousNode() {
		setCurrentNode(getPreviousNode());
	}

	@Override
	public void remove(INode node) {
	}

	/**
	 * @param selection
	 * @return Return first node from selection. For the {@link WhereUsedNode} will return his parent (since TypeNodes
	 *         are not in the real tree). If selection is not Structured or the firstElement in selection is not
	 *         {@link INode} then return null;
	 */
	public INode extractFirstNode(ISelection selection) {
		if (selection instanceof StructuredSelection) {
			Object firstElement = ((StructuredSelection) selection).getFirstElement();
			if (firstElement instanceof INode) {
				INode node = (INode) firstElement;
				if (node instanceof TypeUserNode)
					return (INode) ((TypeUserNode) node).getOwner();
				else if (node instanceof ExtensionUserNode)
					return (INode) ((ExtensionUserNode) node).getOwner();
				else if (node instanceof WhereUsedNode) {
					return node.getParent();
				}
				return node;
			}
		}
		return null;
	}
}

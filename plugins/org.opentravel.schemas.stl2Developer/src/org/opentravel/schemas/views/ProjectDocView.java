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

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a browser as a view that can be called on to display URLs.
 */
public class ProjectDocView extends OtmAbstractView
		implements ISelectionListener, ISelectionChangedListener, ITreeViewerListener, ModifyListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDocView.class);

	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.ProjectDocView";
	public static final String ID = ProjectDocView.class.getName();
	private Browser browser = null;
	private String url;

	/**
	 * Create the browser component in frame.
	 */
	@Override
	public void createPartControl(Composite frame) {
		OtmRegistry.registerProjectDocView(this);
		try {
			// Do NOT use SWT.MOZILLA or SWT.WEBKIT as these add system requirements
			browser = new Browser(frame, SWT.NONE);
		} catch (SWTError e) {
			LOGGER.error("Error creating browser: " + e.getLocalizedMessage());
			return;
		}

		url = OtmRegistry.getMainController().getModelController().getLastCompileDirectory();
	}

	private boolean viewerIsOk() {
		return (browser != null && !browser.isDisposed());
	}

	/**
	 * Disposes the internal browser widget.
	 */
	@Override
	public void dispose() {
		if (viewerIsOk())
			browser.dispose();
		browser = null;
		super.dispose();
	}

	/**
	 * Directs the browser to the given URL.
	 * 
	 * @param url
	 *            the URL to navigate to
	 */
	public void gotoURL(String url) {
		if (viewerIsOk() && !url.isEmpty())
			browser.setUrl(url);
	}

	/**
	 * Delegates focusing this view to the browser, so that it can handle mouse-clicks etc.
	 */
	@Override
	public void setFocus() {
		if (viewerIsOk())
			browser.setFocus();
		url = OtmRegistry.getMainController().getModelController().getLastCompileDirectory();
		if (!url.isEmpty())
			url += "/documentation/index.html";
		gotoURL(url);
	}

	@Override
	public INode getCurrentNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> getSelectedNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getViewID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(INode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentNode(INode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyText(ModifyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		gotoURL(url);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub

	}
}
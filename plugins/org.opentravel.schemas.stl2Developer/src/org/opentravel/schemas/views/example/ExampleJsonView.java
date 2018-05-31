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
package org.opentravel.schemas.views.example;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmAbstractView;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Legacy view to be removed in the future. It exists to allow the user to find out that this view has been removed and
 * to force it to be removed from the saved layouts (May 30, 2018)
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public class ExampleJsonView extends OtmAbstractView implements ISelectionListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleJsonView.class);

	private Text jsonBox;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(final Composite parent) {
		// LOGGER.info("Initializing part control of " + this.getClass());

		final Text text = WidgetFactory.createText(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setEditable(false);
		text.setText("Example views have been removed from OTM-DE. \nPLEASE CLOSE THIS WINDOW.");

		OtmRegistry.registerExampleJsonView(this);

		// jsonBox = initializeJsonBox(parent);
		// getSite().getPage().addSelectionListener("org.opentravel.schemas.stl2Developer.ExampleView", this);
		// LOGGER.info("Done initializing part control of " + this.getClass());
	}

	// /**
	// * @param mainSashForm
	// * @return
	// */
	// private Text initializeJsonBox(final Composite parent) {
	// final Text text = WidgetFactory.createText(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	// text.setEditable(false);
	// return text;
	// }
	//
	@Override
	public void setFocus() {
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		// if (selection instanceof IStructuredSelection) {
		// final Object o = ((IStructuredSelection) selection).getFirstElement();
		// if (o instanceof ExampleModel) {
		// String s = ((ExampleModel) o).getJsonString();
		// if (s == null) {
		// s = "";
		// }
		// if (!jsonBox.isDisposed())
		// jsonBox.setText(s);
		// }
		// }
	}

	@Override
	public INode getCurrentNode() {
		return null;
	}

	@Override
	public List<Node> getSelectedNodes() {
		return null;
	}

	@Override
	public String getViewID() {
		return null;
	}

	@Override
	public void refresh() {
	}

	@Override
	public void refresh(INode node) {
	}

	@Override
	public void setCurrentNode(INode node) {
	}

}

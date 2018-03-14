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
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.stl2Developer.editor.commands.HideAllNodesCommand;
import org.opentravel.schemas.stl2Developer.editor.internal.Features;
import org.opentravel.schemas.stl2Developer.editor.parts.ComponentNodeEditPart;
import org.opentravel.schemas.stl2Developer.editor.view.DependeciesView;
import org.opentravel.schemas.utils.RCPUtils;
import org.opentravel.schemas.views.NavigatorView;

/**
 * @author Pawel Jedruch
 * 
 */
public class LinkWithNavigator extends Action {
	private DependeciesView view;
	private NavigatorSelectionListener listener;

	public LinkWithNavigator(DependeciesView view) {
		super(getLabel(), IAction.AS_CHECK_BOX);
		this.view = view;
		init();
	}

	private void init() {
		if (Features.isViewLinkedWithNavigator()) {
			view.addSelectionListener(NavigatorView.VIEW_ID, getListener());
			ISelectionService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
			ISelection selection = service.getSelection(NavigatorView.VIEW_ID);
			getListener().selectNodes(selection);
		} else {
			view.removeSelectionListener(getListener());
		}
	}

	@Override
	public void run() {
		setText(getLabel());
		init();
	}

	class NavigatorSelectionListener implements ISelectionListener {

		public void selectNodes(ISelection selection) {
			List<Node> nodes = RCPUtils.extractObjects(selection, Node.class);

			CompoundCommand cmds = new CompoundCommand(getText());
			cmds.add(new HideAllNodesCommand(view.getDiagram()));
			for (Node n : nodes) {
				cmds.add(getCreateCommand(n));
			}
			if (cmds.canExecute() && !nodes.isEmpty()) {
				view.getGraphicalViewer().getEditDomain().getCommandStack().execute(cmds);
				for (Node n : nodes) {
					addDepedencyNodes(n);
				}
			}
		}

		private void addDepedencyNodes(Node n) {
			if (n instanceof VersionNode)
				n = ((VersionNode) n).get();
			view.selectNode(n);
			for (Object ep : view.getGraphicalViewer().getSelectedEditParts()) {
				if (ep instanceof ComponentNodeEditPart) {
					((ComponentNodeEditPart) ep).expand();
				}
			}
			new AddUsedTypesAction(view.getGraphicalViewer(), "").run();
			view.selectNode(n);
			new WhereUsedActionGef(view.getGraphicalViewer(), "").run();
			view.selectNode(n);
		}

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			selectNodes(selection);
		}

		private Command getCreateCommand(final Node newNode) {
			CreateRequest create = new CreateRequest();
			create.setFactory(new CreationFactory() {

				@Override
				public Object getObjectType() {
					return null;
				}

				@Override
				public Object getNewObject() {
					return newNode;
				}
			});
			Point location = new Point(0, 10);
			if (view.getGraphicalViewer().getControl() != null) {
				org.eclipse.swt.graphics.Point size = view.getGraphicalViewer().getControl().getSize();
				location = new Point(size.x / 2 - 100, 10);
			}
			create.setLocation(location);
			EditPart target = view.getGraphicalViewer().getRootEditPart().getContents().getTargetEditPart(create);
			return target.getCommand(create);
		}

	}

	private NavigatorSelectionListener getListener() {
		if (listener == null) {
			listener = new NavigatorSelectionListener();
		}
		return listener;
	}

	private static String getLabel() {
		if (Features.isViewLinkedWithNavigator()) {
			return "Unlink";
		} else {
			return "Link";
		}
	}

	@Override
	public boolean isChecked() {
		return Features.isViewLinkedWithNavigator();
	}

	@Override
	public void setChecked(boolean checked) {
		Features.setViewLinkedWithNavigator(checked);
		super.setChecked(checked);
	}

}

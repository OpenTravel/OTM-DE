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
package org.opentravel.schemas.stl2Developer.editor.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.stl2Developer.editor.model.Connection;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;
import org.opentravel.schemas.stl2Developer.editor.policies.ParentSelectionPolicy;
import org.opentravel.schemas.stl2Developer.editor.ui.figures.ExpandGraphLabel;
import org.opentravel.schemas.stl2Developer.editor.ui.figures.ExpandGraphLabel.IExpandListener;
import org.opentravel.schemas.trees.library.LibraryTreeLabelProvider;

/**
 * @author Pawel Jedruch
 * 
 */
public class ComponentNodeEditPart extends GenericEditPart<ComponentNode> {

	public ComponentNodeEditPart(UINode model) {
		super(model);
	}

	public final static LibraryTreeLabelProvider LABEL_PROVIDER = new LibraryTreeLabelProvider();

	@Override
	protected IFigure createFigure() {
		ExpandGraphLabel e = new ExpandGraphLabel();
		e.addExpandListener(new IExpandListener() {

			@Override
			public void expanded() {
				getModel().getOwner().publish(getModel(), "expand", false, true);
			}

			@Override
			public void collapsed() {
				getModel().getOwner().publish(getModel(), "expand", true, false);
			}
		});
		return e;

	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ChildrenDecorationPolicy());
	}

	class ChildrenDecorationPolicy extends LayoutEditPolicy {

		@Override
		protected EditPolicy createChildEditPolicy(EditPart child) {
			return new ParentSelectionPolicy();
		}

		@Override
		protected Command getCreateCommand(CreateRequest request) {
			return null;
		}

		@Override
		protected Command getMoveChildrenCommand(Request request) {
			return null;
		}

	}

	@Override
	public void performRequest(Request req) {
		if (RequestConstants.REQ_OPEN.equals(req.getType())) {
			getFigure().toogleAll();
		}
	}

	@Override
	protected void refreshVisuals() {
		// TODO - fix/create provider to supply prefix
		if (getNodeModel() instanceof LibraryMemberInterface)
			getFigure().setText(getNodeModel().getNameWithPrefix());
		else
			getFigure().setText(LABEL_PROVIDER.getText(getNodeModel()));
		getFigure().setImage(LABEL_PROVIDER.getImage(getNodeModel()));
		if (getModelChildren().isEmpty()) {
			getFigure().getTitle().setBackgroundColor(null);
			getFigure().hideExpanedSymbol();
		} else {
			if (!(getParent() instanceof DiagramEditPart))
				getFigure().getTitle().setBackgroundColor(ColorConstants.lightGray);
			getFigure().showExpanedSymbol();
		}
		if (getNodeModel() instanceof ContributedFacetNode)
			getFigure().setForegroundColor(ColorConstants.red);

		super.refreshVisuals();
	}

	@Override
	public ExpandGraphLabel getFigure() {
		return (ExpandGraphLabel) super.getFigure();
	}

	public static Label newLabel(Node node) {
		return new Label(LABEL_PROVIDER.getText(node), LABEL_PROVIDER.getImage(node));
	}

	@Override
	public IFigure getContentPane() {
		return getFigure().getContainer();
	}

	@Override
	protected void addChildVisual(EditPart childEditPart, int index) {
		IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
		if (child instanceof ExpandGraphLabel) {
			((ExpandGraphLabel) child).getTitle().setBackgroundColor(ColorConstants.lightGray);
		}
		child.setBorder(null);
		super.addChildVisual(childEditPart, index);
	}

	@Override
	protected void refreshSourceConnections() {
		super.refreshSourceConnections();
		if (children != null) {
			for (Object o : children) {
				((EditPart) o).refresh();
			}
		}
	}

	@Override
	protected void refreshTargetConnections() {
		super.refreshTargetConnections();
	}

	@Override
	protected List<Connection> getModelSourceConnections() {
		List<Connection> connections = new ArrayList<Connection>();
		connections.addAll(super.getModelSourceConnections());
		return connections;
	}

	public void expand() {
		getModel().getOwner().setListenersEnabled(false);
		getFigure().expandAllChildren();
		getModel().getOwner().setListenersEnabled(true);
		// fire up expand event
		getFigure().expand();
	}

	public void collapse() {
		getModel().getOwner().setListenersEnabled(false);
		getFigure().collapseAllChildren();
		getModel().getOwner().setListenersEnabled(true);
		// fire up collapse event
		getFigure().collapse();
	}

}

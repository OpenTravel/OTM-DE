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

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.properties.Fonts;
import org.opentravel.schemas.stl2Developer.editor.commands.HideNodeCommand;
import org.opentravel.schemas.stl2Developer.editor.internal.Features;
import org.opentravel.schemas.stl2Developer.editor.internal.GEFUtils;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram.Position;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;
import org.opentravel.schemas.stl2Developer.editor.ui.figures.HyperlinkLabel;
import org.opentravel.schemas.stl2Developer.editor.ui.figures.NodeWithFacetsFigure;

/**
 * @author Pawel Jedruch
 * 
 */
public class PropertyNodeEditPart extends GenericEditPart<Node> implements MouseListener {

	public PropertyNodeEditPart(UINode node) {
		super(node);
	}

	@Override
	protected IFigure createFigure() {
		PropertyFigure ff = new PropertyFigure();
		ff.setBackgroundColor(NodeWithFacetsFigure.classColor);
		ff.addMouseListenerForLink(this);
		return ff;
	}

	@Override
	protected void refreshVisuals() {
		getFigure().setImage(getNodeModel().getImage());
		getFigure().setName(getNodeModel().getName());

		if (!(getNodeModel().getType() instanceof ImpliedNode)) {
			// For properties, show the type and cardinality
			String typeText = getNodeModel().getTypeNameWithPrefix();
			if (getNodeModel() instanceof PropertyNode) {
				typeText += " - ";
				if (((PropertyNode) getNodeModel()).isMandatory())
					typeText += 1;
				else
					typeText += 0;
				if (getNodeModel() instanceof ElementNode) {
					typeText += " .. ";
					int cnt = ((ElementNode) getNodeModel()).getRepeat();
					if (cnt == 0)
						typeText += "1";
					else
						typeText += cnt;
				}
			}
			getFigure().setType(typeText);
		}
		getFigure().setTypeImage(getModel().getTypeImage());
		getFigure().setForegroundColor(ColorConstants.black);
		if (getModel().getNode().isInherited()) {
			// TODO: move this to Features class
			getFigure().setFont(Fonts.getFontRegistry().get(Fonts.inheritedItem));
			getFigure().setForegroundColor(ColorConstants.darkBlue);
		}
		if (getNodeModel().isUnAssigned()) {
			getFigure().setForegroundColor(ColorConstants.red);
			getFigure().setType(getNodeModel().getTypeNameWithPrefix());
		}
		super.refreshVisuals();
	}

	@Override
	public PropertyFigure getFigure() {
		return (PropertyFigure) super.getFigure();
	}

	@Override
	public void mousePressed(MouseEvent me) {
		Node newNode = getNodeModel().getType();
		Command command = getCreateCommand(newNode);

		// if New Node is already displayed ( uiType is not null), hide command
		UINode uiType = getModel().getOwner().findUINode(newNode);
		if (uiType != null)
			command = new HideNodeCommand(newNode, getModel().getOwner());

		command.setDebugLabel("From PropertyNodeEditPart");
		if (command != null && command.canExecute()) {
			getViewer().getEditDomain().getCommandStack().execute(command);
			selectNode(newNode);
		}
		me.consume();
	}

	private void selectNode(Node newNode) {
		Diagram diagram = getModel().getOwner();
		UINode uiNode = diagram.findUINode(newNode);

		if (getViewer() == null || getViewer().getEditPartRegistry() == null)
			return;

		EditPart nodeEP = (EditPart) getViewer().getEditPartRegistry().get(uiNode);
		final EditPart toSelecteEP = GEFUtils.getEditPartToSelect(nodeEP);
		getViewer().getEditPartRegistry().get(uiNode);

		// wait until layout will finish
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (Features.addNewPropertyFullyExpanded()) {
					if (toSelecteEP instanceof ComponentNodeEditPart) {
						((ComponentNodeEditPart) toSelecteEP).expand();
					}
				}
				if (toSelecteEP != null) {
					getViewer().reveal(toSelecteEP);
					getViewer().select(toSelecteEP);
				}
			}
		});

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
		Diagram diagram = getModel().getOwner();
		Point location = diagram.findBestLocation(diagram.findUINode(getNodeModel()).getTopLevelParent(),
				(Node) create.getNewObject(), Position.RIGHT);
		create.setLocation(location);
		EditPart target = getTargetEditPart(create);
		return target.getParent().getCommand(create);
	}

	class PropertyFigure extends Figure {
		private Label image = new Label();
		private Label name = new Label();
		private HyperlinkLabel type = new HyperlinkLabel();
		private Label typeImage = new Label();

		public PropertyFigure() {
			this.setFont(Display.getDefault().getSystemFont());
			// name.setFont(Display.getDefault().getSystemFont());
			setLayoutManager(new BorderLayout());
			setOpaque(true);
			setBorder(new LineBorder(ColorConstants.black, 1));
			Figure leftC = new Figure();
			leftC.setLayoutManager(new ToolbarLayout(true));
			leftC.add(image);
			leftC.add(name);
			leftC.add(type);
			add(leftC, BorderLayout.LEFT);
			add(typeImage, BorderLayout.RIGHT);
		}

		public void setName(String name) {
			this.name.setText(name);
		}

		public void setType(String type) {
			if (type != null && !type.isEmpty())
				this.type.setText(" [" + type + "]");
		}

		public void setTypeImage(Image img) {
			this.typeImage.setIcon(img);
		}

		public void setImage(Image img) {
			this.image.setIcon(img);
		}

		public void addMouseListenerForLink(MouseListener listener) {
			type.addMouseListener(listener);
			typeImage.addMouseListener(listener);
			typeImage.setCursor(Cursors.HAND);
		}

	}

	@Override
	public void mouseReleased(MouseEvent me) {

	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {

	}

}

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
package org.opentravel.schemas.stl2Developer.editor.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2Developer.editor.parts.DiagramEditPart;

public class Diagram implements LayoutListener {
	/**
	 * register of all nodes in editor (visible and not visible) including top levels
	 */
	private Map<Node, UINode> registeredNodes = new HashMap<Node, UINode>();
	/**
	 * Node that should be displayed as top level nodes in diagram
	 */
	private Set<UINode> topLevels = new LinkedHashSet<UINode>();

	private ListenerList listeners = new ListenerList();
	private boolean listenersEnabled = true;
	private EditPart ownerEP;

	public Collection<UINode> getTopLevels() {
		return topLevels;
	}

	public enum Position {
		TOP, RIGHT, BOTTOM, LEFT;
	}

	/**
	 * Calculate best possible position newChild and calculate new position based on existing location and
	 * {@link Position}.
	 * 
	 * @param existing
	 *            - reference to already placed node. TODO: replace with UINode
	 * @param newChild
	 *            - new object to be added to diagram
	 * @param posistion
	 *            - position related to existing node.
	 * @return return UINode for provided child.
	 */
	public Point findBestLocation(UINode existing, Node newChild, Position posistion) {
		UINode closeOne = findClosest(existing, posistion);
		Point newLocation = null;
		if (closeOne != null) {
			newLocation = closeOne.getBoundry().getBottomLeft().translate(0, 20);
		} else {
			if (posistion == Position.RIGHT) {
				newLocation = existing.getBoundry().getTopRight().translate(70, 0);
			} else if (posistion == Position.BOTTOM) {
				newLocation = existing.getBoundry().getBottomLeft().translate(0, 70);
			} else if (posistion == Position.TOP) {
				newLocation = existing.getBoundry().getTopLeft().translate(0, -70);
			} else {
				// TODO: instead of calculating size return the Top-Right for figure.
				newLocation = existing.getBoundry().getTopLeft().translate(-getFigureSize(newChild).width - 70, 0);
			}
		}
		newLocation = doNotOverlap(newLocation);
		return newLocation;
	}

	/**
	 * Add temporary GEP in order to get the figure size. This method is cleaning up every EditParts and
	 * ConnectionEditPArt after it finish.
	 * 
	 * @param newChild
	 * @return
	 */
	private Dimension getFigureSize(Node newChild) {
		UINode uiModel = new UINode(newChild, this, null);
		GraphicalEditPart gep = (GraphicalEditPart) getViewer().getEditPartFactory().createEditPart(ownerEP, uiModel);
		gep.setParent(ownerEP);
		gep.refresh();
		IFigure tmpF = gep.getFigure();
		deactivate(gep);
		unregister(uiModel);
		return tmpF.getPreferredSize();
	}

	private void deactivate(GraphicalEditPart gep) {
		gep.deactivate();
		gep.removeNotify();
	}

	private Point doNotOverlap(Point newLocation) {
		for (UINode shape : topLevels) {
			if (shape.getBoundry().contains(newLocation)) {
				newLocation.x = shape.getBoundry().getBottomLeft().x;
				newLocation.y = shape.getBoundry().getBottomLeft().y + 20;
				return doNotOverlap(newLocation);
			}
		}
		return newLocation;
	}

	private UINode findClosest(UINode existing, Position pos) {
		Point referancePoints = null;
		if (pos == Position.RIGHT) {
			referancePoints = existing.getBoundry().getTopRight();
		} else {
			referancePoints = existing.getBoundry().getTopLeft();
		}
		double min = Double.MAX_VALUE;
		UINode minNode = null;
		for (UINode shape : topLevels) {
			if (shape == existing)
				continue;

			if (pos == Position.RIGHT) {
				if (referancePoints.x - shape.getLocation().x < 0) {
					double newMin = Math.min(referancePoints.getDistance(shape.getBoundry().getBottomLeft()), min);
					if (min > newMin) {
						min = newMin;
						minNode = shape;
					}
				}
			} else {
				if (referancePoints.x - shape.getLocation().x > 0) {
					double newMin = Math.min(referancePoints.getDistance(shape.getBoundry().getBottomLeft()), min);
					if (min > newMin) {
						min = newMin;
						minNode = shape;
					}
				}

			}
		}
		return minNode;
	}

	/**
	 * If node already exist in diagram, this method will do nothing.
	 * 
	 * @param child
	 *            - new node to be added to diagram
	 * @param location
	 *            - initial location for node or null
	 * @return return UINode for provided child.
	 */
	public UINode addChild(final Node child, Point location) {
		UINode uiNode = findUINode(child);
		if (uiNode == null) {
			uiNode = createModel(child, this, null);
			addChild(uiNode);
			if (location != null)
				uiNode.setLocation(location);
		}
		return uiNode;
	}

	private void addChild(UINode newNode) {
		if (!topLevels.contains(newNode)) {
			topLevels.add(newNode);
			register(newNode);
			publish("child", null, newNode);
		}
	}

	public void addAllChildren(Collection<? extends Node> childen) {
		setListenersEnabled(false);
		for (Node n : childen) {
			addChild(n, (Point) null);
		}
		setListenersEnabled(true);
		publish("children", null, childen);
	}

	public UINode findUINode(Node node) {
		return registeredNodes.get(node);
	}

	public Collection<UINode> getAllNodes() {
		return new ArrayList<UINode>(registeredNodes.values());
	}

	public void addListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	private void publish(String name, Object oldValue, Object newValue) {
		publish(this, name, oldValue, newValue);
	}

	public void publish(Object source, String name, Object oldValue, Object newValue) {
		if (!listenersEnabled)
			return;

		PropertyChangeEvent evnet = new PropertyChangeEvent(source, name, oldValue, newValue);
		for (Object l : listeners.getListeners()) {
			PropertyChangeListener pcl = (PropertyChangeListener) l;
			pcl.propertyChange(evnet);
		}
	}

	public void removeAll() {
		Set<UINode> oldNodes = new HashSet<UINode>(topLevels);
		setListenersEnabled(false);
		for (UINode nodes : oldNodes) {
			remove(nodes);
		}
		setListenersEnabled(true);
		publish("children_all_remove", oldNodes, null);
	}

	public void remove(UINode model) {
		if (topLevels.contains(model)) {
			topLevels.remove(model);
			unregister(model);
			publish("child_remove", model, null);
		}
	}

	/**
	 * Add node to the registry.
	 * 
	 * @param uiNode
	 */
	public void register(UINode uiNode) {
		if (!registeredNodes.containsKey(uiNode.getNode()))
			registeredNodes.put(uiNode.getNode(), uiNode);
	}

	private void unregister(UINode model) {
		for (UINode child : model.getChildren()) {
			unregister(child);
		}
		registeredNodes.remove(model.getNode());
	}

	public void remove(Node node) {
		UINode uiNode = findUINode(node);
		remove(uiNode);
	}

	public boolean isListenersEnabled() {
		return listenersEnabled;
	}

	public void setListenersEnabled(boolean listenersEnabled) {
		this.listenersEnabled = listenersEnabled;
	}

	@Override
	public void invalidate(IFigure container) {
	}

	@Override
	public boolean layout(IFigure container) {
		return false;
	}

	@Override
	public void postLayout(IFigure container) {
	}

	@Override
	public void remove(IFigure child) {
	}

	@Override
	/**
	 * listen on layout changes to update UINode size.
	 */
	public void setConstraint(IFigure child, Object constraint) {
		EditPart figureEP = (EditPart) getViewer().getVisualPartMap().get(child);

		// Can be null if editpart is not registered.
		if (figureEP == null)
			return;
		if (figureEP.getModel() instanceof UINode) {
			UINode node = (UINode) figureEP.getModel();
			if (node.isTopLevel()) {
				node.setSize(child.getPreferredSize());
			}
		}
	}

	public void setOwnerEP(DiagramEditPart diagramEditPart) {
		this.ownerEP = diagramEditPart;
	}

	public EditPartViewer getViewer() {
		return ownerEP.getViewer();
	}

	public static UINode createModel(Node node, Diagram diagram, UINode parent) {
		UINode uiNode = diagram.findUINode(node);
		if (uiNode == null) {
			uiNode = new UINode(node, diagram, parent);
			diagram.register(uiNode);
		}
		return uiNode;
	}

	/**
	 * 
	 * @return list of top level unlinked nodes
	 */
	public Collection<UINode> getUnlinkedNodes() {
		ArrayList<UINode> ret = new ArrayList<UINode>();
		for (UINode ui : getTopLevels()) {
			if (ui.isUnlinked()) {
				ret.add(ui);
			}
		}
		return ret;
	}

	public void clearUnlinkedNodes() {
		setListenersEnabled(false);
		Collection<UINode> toRemove = getUnlinkedNodes();
		for (UINode ui : toRemove) {
			remove(ui);
		}
		setListenersEnabled(true);
		publish("clear_unlinked", toRemove, null);
	}

}
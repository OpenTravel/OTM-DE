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
package org.opentravel.schemas.controllers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.opentravel.schemas.actions.AssignTypeAction;
import org.opentravel.schemas.actions.SetDescriptionEqExAction;
import org.opentravel.schemas.actions.SetObjectNameAction;
import org.opentravel.schemas.commands.AddNodeHandler2;
import org.opentravel.schemas.controllers.DefaultContextController.ContextViewType;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.handlers.ConstraintHandler;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.widgets.OtmEventData;
import org.opentravel.schemas.widgets.OtmHandlers;
import org.opentravel.schemas.wizards.TypeSelectionWizard;

/**
 * @author Dave Hollander
 * 
 */
public class OtmActions {
	// private static final Logger LOGGER = LoggerFactory.getLogger(OtmActions.class);

	private MainController mc = null; // hold onto for refreshes

	private static final int noOp = 0; // do nothing
	private static final int toggleMandatory = 12;
	private static final int toggleList = 13;

	private static final int changeMaxLength = 14;
	private static final int changeMinLength = 15;
	private static final int changeFractionDigits = 55;
	private static final int changeTotalDigits = 56;
	private static final int changeRepeatCnt = 16;
	private static final int changePropertyRole = 17;
	private static final int changeContext = 18;

	private static final int setName = 30;
	private static final int setComponentName = 31;
	private static final int setDescription = 32;
	private static final int setExample = 33;
	private static final int setEquivalence = 34;
	private static final int setPattern = 35;
	private static final int setPropertyType = 36;
	private static final int setOrNewPropertyType = 37;
	private static final int setNameSpace = 38;

	private static final int typeSelector = 46;
	private static final int propertyTypeSelector = 47;
	private static final int exampleContextSelector = 48; // deprecated
	private static final int equivalentContextSelector = 49; // deprecated
	private static final int otherDocContextSelector = 54;
	private static final int extendsSelector = 57;
	private static final int clearExtends = 58;

	private static final int importToTree = 50;

	private static final int setMinInclusive = 59;
	private static final int setMaxInclusive = 60;
	private static final int setMinExclusive = 61;
	private static final int setMaxExclusive = 62;

	public static int getNoOp() {
		return noOp;
	}

	public OtmActions(final MainController mainController) {
		this.mc = mainController; // hold onto for refreshes
	}

	public void doEvent(final OtmEventData wd) {
		// LOGGER.debug("Triggered event " + wd.getBusinessEvent() + ".");
		OtmHandlers.suspendHandlers();
		switch (wd.getBusinessEvent()) {
		case noOp:
			break;
		case changeMaxLength:
			changeMaxLength(wd);
			break;
		case changeMinLength:
			changeMinLength(wd);
			break;
		case changeFractionDigits:
			changeFractionDigits(wd);
			break;
		case changeTotalDigits:
			changeTotalDigits(wd);
			break;
		case setMinInclusive:
			setMinInclusive(wd);
			break;
		case setMaxInclusive:
			setMaxInclusive(wd);
			break;
		case setMinExclusive:
			setMinExclusive(wd);
			break;
		case setMaxExclusive:
			setMaxExclusive(wd);
			break;
		case changeRepeatCnt:
			changeRepeatCount(wd);
			break;
		case changePropertyRole:
			changePropertyRole(wd);
			break;
		case changeContext:
			changeContext(wd);
			break;
		case toggleMandatory:
			toggleMandatory(wd);
			break;
		case toggleList:
			toggleList(wd);
			break;
		case setName:
			setName(wd);
			break;
		case setNameSpace:
			setNameSpace(wd);
			break;
		case setComponentName:
			setComponentName(wd);
			break;
		case setDescription:
			setDescription(wd);
			break;
		case setExample:
			setExample(wd);
			break;
		case setEquivalence:
			setEquivalence(wd);
			break;
		case setPattern:
			setPattern(wd);
			break;
		case setPropertyType:
			setPropertyType(wd);
			break;
		case setOrNewPropertyType:
			setOrNewPropertyType(wd);
			break;
		case typeSelector:
			typeSelector(wd);
			break;
		case exampleContextSelector:
			exampleContextSelector();
			break;
		case equivalentContextSelector:
			equivalentContextSelector();
			break;
		case otherDocContextSelector:
			otherDocContextSelector();
			break;
		case propertyTypeSelector:
			propertyTypeSelector(wd);
			break;
		case importToTree:
			importToTree(wd);
			break;
		}
		OtmHandlers.enableHandlers();
	}

	/**
	 * Get the current node or first node selected from facetView. Inform the user if no nodes were selected.
	 * 
	 * @return
	 */
	private Node getFacetSelection() {
		final Node n = mc.getSelectedNode_TypeView();

		if (n == null) {
			DialogUserNotifier.openInformation("No Selection", Messages.getString("OtmW.100")); //$NON-NLS-1$
		}
		return n;
	}

	private Node getPropertySelection() {
		final Node n = (Node) mc.getCurrentNode_PropertiesView();

		if (n == null) {
			DialogUserNotifier.openInformation("No Selection", Messages.getString("OtmW.101"));
		}
		return n;
	}

	public static int propertyTypeSelector() {
		return propertyTypeSelector;
	}

	public static int extendsSelector() {
		return extendsSelector;
	}

	public static int clearExtends() {
		return clearExtends;
	}

	// Called when property view type selector is used
	private void propertyTypeSelector(final OtmEventData wd) {
		Node n = wd.getNode();
		// If no node was saved with the event data, see if there is a current node selected.
		if (n == null && ((n = getPropertySelection()) == null))
			return;

		List<Node> list = new ArrayList<>();
		list.add(n);
		final TypeSelectionWizard wizard = new TypeSelectionWizard(n);
		if (wizard.run(OtmRegistry.getActiveShell())) {
			AssignTypeAction.execute(list, wizard.getSelection());
		}
	}

	public static int exampleContextSelectorEventId() {
		return exampleContextSelector;
	}

	/**
	 * Launch context change on existing TLExample.
	 * 
	 * @deprecated - these controls were removed from the properties panel in type view
	 */
	@Deprecated
	private void exampleContextSelector() {
	}

	public static int equivalentContextSelectorEventId() {
		return equivalentContextSelector;
	}

	/**
	 * @deprecated - these controls were removed from the properties panel in type view
	 */
	@Deprecated
	private void equivalentContextSelector() {
	}

	public static int otherDocContextSelectorEventId() {
		return otherDocContextSelector;
	}

	private void otherDocContextSelector() {
		// LOGGER.error("OtmActions tried to execute otherDocContextSelector");
		Assert.isTrue(1 == 1); // We should NEVER get here
	}

	/**
	 * Run type selection wizard on simple type or property.
	 * 
	 * @return
	 */
	public static int typeSelector() {
		return typeSelector;
	}

	private void typeSelector(final OtmEventData wd) {
		ArrayList<Node> list = new ArrayList<>();
		Node n = wd.getNode();
		if (n != null)
			list.add(n);
		else
			list.addAll(wd.getNodeList());
		if (list.size() <= 0)
			list.add(getFacetSelection());

		if (list.get(0) instanceof TypeUser)
			AssignTypeAction.execute(((TypeUser) list.get(0)));
		// else
		// LOGGER.debug("Invalid node to select type for: " + list.get(0));
	}

	public static int changeMaxLength() {
		return changeMaxLength;
	}

	public void changeMaxLength(final OtmEventData wd) {
		if (!(mc.getCurrentNode_PropertiesView() instanceof ComponentNode)) {
			return;
		}
		ConstraintHandler ch = ((ComponentNode) mc.getCurrentNode_PropertiesView()).getConstraintHandler();
		if (ch != null)
			ch.setMaxLength(wd.getInt());
	}

	public static int changeMinLength() {
		return changeMinLength;
	}

	public void changeMinLength(final OtmEventData wd) {
		if (!(mc.getCurrentNode_PropertiesView() instanceof ComponentNode)) {
			return;
		}
		ConstraintHandler ch = ((ComponentNode) mc.getCurrentNode_PropertiesView()).getConstraintHandler();
		if (ch != null)
			ch.setMinLength(wd.getInt());
	}

	public static int changeFractionDigits() {
		return changeFractionDigits;
	}

	public void changeFractionDigits(final OtmEventData wd) {
		if (!(mc.getCurrentNode_PropertiesView() instanceof ComponentNode)) {
			return;
		}
		ConstraintHandler ch = ((ComponentNode) mc.getCurrentNode_PropertiesView()).getConstraintHandler();
		if (ch != null)
			ch.setFractionDigits(wd.getInt());
	}

	public static int changeTotalDigits() {
		return changeTotalDigits;
	}

	public void changeTotalDigits(final OtmEventData wd) {
		if (!(mc.getCurrentNode_PropertiesView() instanceof ComponentNode)) {
			return;
		}
		ConstraintHandler ch = ((ComponentNode) mc.getCurrentNode_PropertiesView()).getConstraintHandler();
		if (ch != null)
			ch.setTotalDigits(wd.getInt());
	}

	public void setMinInclusive(final OtmEventData wd) {
		if (!(mc.getCurrentNode_PropertiesView() instanceof ComponentNode)) {
			return;
		}
		ConstraintHandler ch = ((ComponentNode) mc.getCurrentNode_PropertiesView()).getConstraintHandler();
		if (ch != null)
			ch.setMinInclusive(wd.getText());
	}

	public void setMaxInclusive(final OtmEventData wd) {
		if (!(mc.getCurrentNode_PropertiesView() instanceof ComponentNode)) {
			return;
		}
		ConstraintHandler ch = ((ComponentNode) mc.getCurrentNode_PropertiesView()).getConstraintHandler();
		if (ch != null)
			ch.setMaxInclusive(wd.getText());
	}

	public void setMinExclusive(final OtmEventData wd) {
		if (!(mc.getCurrentNode_PropertiesView() instanceof ComponentNode)) {
			return;
		}
		ConstraintHandler ch = ((ComponentNode) mc.getCurrentNode_PropertiesView()).getConstraintHandler();
		if (ch != null)
			ch.setMinExclusive(wd.getText());
	}

	public void setMaxExclusive(final OtmEventData wd) {
		if (!(mc.getCurrentNode_PropertiesView() instanceof ComponentNode)) {
			return;
		}
		ConstraintHandler ch = ((ComponentNode) mc.getCurrentNode_PropertiesView()).getConstraintHandler();
		if (ch != null)
			ch.setMaxExclusive(wd.getText());
	}

	public static int changePropertyRoleEventID() {
		return changePropertyRole;
	}

	public void changePropertyRole(final OtmEventData wd) {
		if (!(mc.getCurrentNode_PropertiesView() instanceof ComponentNode)) {
			return;
		}
		PropertyNode pn = ((PropertyNode) mc.getCurrentNode_PropertiesView());
		PropertyNode npn = null;
		npn = pn.changePropertyRole(PropertyNodeType.fromString(wd.getText()));
		if (npn != pn) {
			mc.refresh(npn);
			// show correct order in navigationView.
			OtmRegistry.getNavigatorView().refresh(npn.getOwningComponent());
		} else
			DialogUserNotifier.openWarning("No Change", "The property could not be changed as requested.");
	}

	public static int changeContextEventId() {
		return changeContext;
	}

	/**
	 * Set the current default context in the library node.
	 * 
	 * @param wd
	 *            - event data containing the combo text containing the contextID to select
	 */
	public void changeContext(OtmEventData wd) {
		Node n = (Node) mc.getCurrentNode_PropertiesView();
		if (n == null) {
			// LOGGER.error("No current properties Node. Early Exit.");
			return;
		}
		ContextController cc = mc.getContextController();
		cc.setSelectedId(ContextViewType.TYPE_VIEW, n.getLibrary(), wd.getText());
		OtmRegistry.getPropertiesView().refresh();
	}

	public static int changeRepeatCount() {
		return changeRepeatCnt;
	}

	public void changeRepeatCount(final OtmEventData wd) {
		if (mc.getCurrentNode_PropertiesView() instanceof ElementNode)
			((ElementNode) mc.getCurrentNode_PropertiesView()).setRepeat(wd.getInt());
		// ((Node) mc.getCurrentNode_PropertiesView()).setRepeat(wd.getInt());
	}

	public static int setName() {
		return setName;
	}

	// Property view name field.
	private void setName(final OtmEventData wd) {
		Event e = new Event();
		e.widget = wd.getWidget();
		e.data = mc.getCurrentNode_PropertiesView();
		new SetObjectNameAction(mc.getMainWindow()).runWithEvent(e);
	}

	public static int setComponentName() {
		return setComponentName;
	}

	// Facet view name field.
	private void setComponentName(final OtmEventData wd) {
		// FIXME: workaround to pass current widget to SetObjectNameAction. We should reuse the
		// properties name field.
		Event e = new Event();
		e.widget = wd.getWidget();
		INode n = mc.getCurrentNode_FacetView();
		e.data = n;
		if (!(n instanceof LibraryMemberInterface))
			e.data = n.getOwningComponent();
		new SetObjectNameAction(mc.getMainWindow()).runWithEvent(e);
	}

	public static int setNameSpace() {
		return setNameSpace;
	}

	private void setNameSpace(final OtmEventData wd) {
		final INode curNode = mc.getCurrentNode_PropertiesView();
		if (curNode != null && curNode instanceof LibraryNode) {
			((LibraryNode) curNode).getNsHandler().rename(curNode.getNamespace(), wd.getText());
		}
	}

	public static int setDescription() {
		return setDescription;
	}

	private void setDescription(final OtmEventData wd) {
		Node n = (Node) mc.getCurrentNode_PropertiesView();
		Event e = new Event();
		SetDescriptionEqExAction action = new SetDescriptionEqExAction(mc.getMainWindow());
		e.data = action.new SetDescriptionEvent(n, wd.getText(), SetDescriptionEqExAction.DescriptionType.DESCRIPTION);
		action.runWithEvent(e);
		mc.getDefaultView().refreshAllViews();
	}

	public static int setExample() {
		return setExample;
	}

	private void setExample(final OtmEventData wd) {
		final Node n = (Node) mc.getCurrentNode_PropertiesView();
		// final String text = wd.getText();
		Event e = new Event();
		SetDescriptionEqExAction action = new SetDescriptionEqExAction(mc.getMainWindow());
		e.data = action.new SetDescriptionEvent(n, wd.getText(), SetDescriptionEqExAction.DescriptionType.EXAMPLE);
		action.runWithEvent(e);
	}

	public static int setEquivalence() {
		return setEquivalence;
	}

	private void setEquivalence(final OtmEventData wd) {
		final Node n = (Node) mc.getCurrentNode_PropertiesView();
		// final String text = wd.getText();
		Event e = new Event();
		SetDescriptionEqExAction action = new SetDescriptionEqExAction(mc.getMainWindow());
		e.data = action.new SetDescriptionEvent(n, wd.getText(), SetDescriptionEqExAction.DescriptionType.EQUIVALENT);
		action.runWithEvent(e);
	}

	public static int setPattern() {
		return setPattern;
	}

	public static int setMinInclusive() {
		return setMinInclusive;
	}

	public static int setMaxInclusive() {
		return setMaxInclusive;
	}

	public static int setMinExclusive() {
		return setMinExclusive;
	}

	public static int setMaxExclusive() {
		return setMaxExclusive;
	}

	private void setPattern(final OtmEventData wd) {
		ConstraintHandler ch = ((ComponentNode) mc.getCurrentNode_PropertiesView()).getConstraintHandler();
		if (ch != null)
			ch.setPattern(wd.getText());
	}

	public static int importToTree() {
		return importToTree;
	}

	private void importToTree(final OtmEventData ed) {
		// LOGGER.debug("importToTree. ed = " + ed);
		mc.importSelectedToDragTarget(ed.isDragCopy());
	}

	/**
	 * Set type of property if not set, or else create a new property.
	 * 
	 * @return
	 */
	public static int setOrNewPropertyType() {
		return setOrNewPropertyType;
	}

	private void setOrNewPropertyType(final OtmEventData ed) {
		// Find the node representing the table row dropped upon.
		final TableItem ti = (TableItem) ed.getWidget();
		if ((ti == null) || (ti.getData() == null) || (!(ti.getData() instanceof ComponentNode))) {
			// LOGGER.warn("set or new property - early exit due to invalid entry conditions.");
			showInvalidTargetWarning();
			return;
		}
		ComponentNode tableNode = (ComponentNode) ti.getData();

		// Get the drag source node from the event data.
		final Node sourceNode = NodeFinders.findNodeByID(ed.getText());
		if (sourceNode == null) {
			// LOGGER.warn("set or new property - early exit due to invalid drop target.");
			showInvalidTargetWarning();
			return;
		}
		// LOGGER.debug("tableNode = " + tableNode + "\tsourceNode = " + sourceNode);

		// 10/2016 dmh - restructured logic and fixed bug where DND was different than add for minor versions
		if (ed.isDragCopy() || tableNode.isUnAssigned()) {
			// Just set the type
			if (tableNode instanceof TypeUser && sourceNode instanceof TypeProvider) {
				((TypeUser) tableNode).setAssignedType((TypeProvider) sourceNode);
				mc.refresh(tableNode);
				OtmRegistry.getNavigatorView().refresh();
			}
		} else {
			// add property, set mandatory if needed
			// if needed, create minor version of object
			// GloballySelectNode is the navigator node (sourceNode)
			Event event = new Event();
			event.data = tableNode; // signals handler to add to this node
			new AddNodeHandler2().execute(event);
		}
	}

	private void showInvalidTargetWarning() {
		DialogUserNotifier.openInformation("WARNING", Messages.getString("dnd.drop.invalid.object"));
	}

	/**
	 * Set the assigned type of the current property posted in the properties view. Event data (in ed.text) is used as
	 * the NodeID of the type source node.
	 */
	public static int setPropertyType() {
		return setPropertyType;
	}

	private void setPropertyType(final OtmEventData ed) {
		final INode pNode = mc.getCurrentNode_PropertiesView();
		if (pNode != null && pNode instanceof TypedPropertyNode)
			((TypedPropertyNode) pNode).setAssignedType((TypeProvider) NodeFinders.findNodeByID(ed.getText()));
		mc.refresh(pNode);
	}

	public static int toggleMandatory() {
		return toggleMandatory;
	}

	public void toggleMandatory(final OtmEventData wd) {
		if (mc.getCurrentNode_PropertiesView() instanceof PropertyNode)
			((PropertyNode) mc.getCurrentNode_PropertiesView()).setMandatory(wd.isSelected());
		// mc.getCurrentNode_PropertiesView().getModelObject().setMandatory(wd.isSelected());
	}

	public static int toggleList() {
		return toggleList;
	}

	public void toggleList(final OtmEventData wd) {
		if (mc.getCurrentNode_PropertiesView() instanceof SimpleTypeNode)
			((SimpleTypeNode) mc.getCurrentNode_PropertiesView()).setList(wd.isSelected());
		// mc.getCurrentNode_PropertiesView().getModelObject().setList(wd.isSelected());
		mc.refresh();
	}

}

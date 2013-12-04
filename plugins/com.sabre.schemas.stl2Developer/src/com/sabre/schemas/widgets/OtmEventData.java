/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.widgets;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

import com.sabre.schemas.controllers.OtmActions;
import com.sabre.schemas.node.Node;

public class OtmEventData {

    protected String label = ""; //$NON-NLS-1$
    protected Widget widget = null;
    protected boolean textDirty = false; // if true, the text may have been
                                         // modified but not saved
    protected boolean hasFocus = false; // if true, the widget has screen focus
    protected Node node = null;
    protected ArrayList<Node> nodeList = null; // List of nodes to act upon
    private boolean isDragCopy = true; // If true, the drag operation is copy, move otherwise

    public ArrayList<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<Node> nodeList) {
        this.nodeList = nodeList;
    }

    protected int businessEvent; // id of the business event this widget initiates
    protected OtmActions actionHandler; // class that handles the widget associated actions

    private Event event;
    private String textValue; // value in the widget
    private int intValue; // value in the widget
    private boolean selectionValue = false; //

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public boolean isDirty() {
        return textDirty;
    }

    public void setDirty(final boolean state) {
        textDirty = state;
    }

    public boolean hasFocus() {
        return hasFocus;
    }

    public void setFocus(final boolean state) {
        hasFocus = state;
    }

    public OtmActions getActionHandler() {
        return actionHandler;
    }

    public void setActionHandler(final OtmActions actions) {
        actionHandler = actions;
    }

    public int getBusinessEvent() {
        return businessEvent;
    }

    public void setSelection(final boolean selection) {
        selectionValue = selection;
    }

    public boolean isSelected() {
        return selectionValue;
    }

    public void setText(final String text) {
        textValue = text;
    }

    public String getText() {
        return textValue;
    }

    public void setInt(final int value) {
        intValue = value;
    }

    public int getInt() {
        return intValue;
    }

    public void setBusinessEvent(final int businessEvent) {
        this.businessEvent = businessEvent;
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(final Widget widget) {
        this.widget = widget;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(final Node node) {
        this.node = node;
    }

    /**
     * @return the isDragCopy
     */
    public boolean isDragCopy() {
        return isDragCopy;
    }

    /**
     * @param isDragCopy
     *            the isDragCopy to set
     */
    public void setDragCopy(boolean isDragCopy) {
        this.isDragCopy = isDragCopy;
    }

    /**
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * @param event
     *            the event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }

}

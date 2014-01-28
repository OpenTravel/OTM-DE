/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Action used to populate toolbar with pull-down actions.
 * 
 * @author Pawel Jedruch
 * 
 */
public class DropDownAction extends Action implements IMenuCreator {

    private Menu lastMenu;
    private List<IAction> subActions = new ArrayList<IAction>();

    public DropDownAction(String text) {
        super(text, IAction.AS_DROP_DOWN_MENU);
    }

    public void addAction(IAction emptyFacet) {
        subActions.add(emptyFacet);
    }

    public void removeAction(IAction emptyFacet) {
        subActions.remove(emptyFacet);
    }

    protected void addActionToMenu(Menu parent, IAction action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    @Override
    public void dispose() {
        if (lastMenu != null) {
            lastMenu.dispose();
            lastMenu = null;
        }
    }

    @Override
    public Menu getMenu(Control parent) {
        if (lastMenu != null) {
            lastMenu.dispose();
        }
        lastMenu = new Menu(parent);
        for (IAction a : subActions) {
            addActionToMenu(lastMenu, a);
        }
        return lastMenu;

    }

    @Override
    public Menu getMenu(Menu parent) {
        return null;
    }

    @Override
    public IMenuCreator getMenuCreator() {
        return this;
    }

}
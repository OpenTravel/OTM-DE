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
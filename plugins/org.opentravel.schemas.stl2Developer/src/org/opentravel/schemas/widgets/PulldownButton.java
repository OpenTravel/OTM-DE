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
package org.opentravel.schemas.widgets;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @author Pawel Jedruch
 * 
 */
public class PulldownButton extends Button {

    private MenuManager mm;

    public PulldownButton(Composite parent, int style) {
        super(parent, SWT.PUSH);
        setText("");
        super.addListener(SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent(Event event) {
                Button button = (Button) event.widget;
                Rectangle rect = button.getBounds();
                Point p = button.toDisplay(rect.x, rect.y + rect.height);
                getMenu().setLocation(p.x - rect.x, p.y - rect.y);
                getMenu().setVisible(true);
            }
        });
        mm = new MenuManager();
        mm.createContextMenu(getShell());
    }

    @Override
    public Menu getMenu() {
        return mm.getMenu();
    }

    public MenuManager getMenuManager() {
        return mm;
    }

    @Override
    public void setMenu(Menu menu) {
    }

    @Override
    protected void checkSubclass() {
    }

    public void addOption(String text, SelectionListener listener) {
        MenuItem item = new MenuItem(getMenu(), SWT.PUSH);
        item.setText(text);
        item.addSelectionListener(listener);
    }

}

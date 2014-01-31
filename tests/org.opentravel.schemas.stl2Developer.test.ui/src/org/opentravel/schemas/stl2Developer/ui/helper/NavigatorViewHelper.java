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
package org.opentravel.schemas.stl2Developer.ui.helper;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.opentravel.schemas.trees.library.LibraryTreeLabelProvider;
import org.opentravel.schemas.views.NavigatorView;

/**
 * @author Pawel Jedruch
 * 
 */
public class NavigatorViewHelper {

    private static final SWTWorkbenchBot bot = new SWTWorkbenchBot();

    public void seletLibraryInDefaultProject(String name) {
        tree().getTreeItem("Default Project").expand().getNode(name).select();
    }

    public ILabelProvider getLabelProvider() {
        return new LibraryTreeLabelProvider();
    }

    public void newService(final String library, String... boPath) {
        tree().getTreeItem("Default Project").expand().getNode(library)
                .contextMenu("New object...").click();
        bot.comboBox().setSelection("Service");
        bot.text().typeText("SName");
        bot.button("Next >").click();
        SWTBotTreeItem item = bot.tree().getTreeItem(boPath[0]);
        for (int i = 1; i < boPath.length; i++) {
            item = item.getNode(boPath[i]);
        }
        item.select();
        bot.button("Finish").click();
    }

    private SWTBotTree tree() throws WidgetNotFoundException {
        return view().bot().tree();
    }

    private SWTBotView view() throws WidgetNotFoundException {
        return bot.viewById(NavigatorView.VIEW_ID);
    }

    public SWTBotTreeItem getLibrary(String libraryName) {
        return tree().getTreeItem("Default Project").expand().getNode(libraryName);
    }
}

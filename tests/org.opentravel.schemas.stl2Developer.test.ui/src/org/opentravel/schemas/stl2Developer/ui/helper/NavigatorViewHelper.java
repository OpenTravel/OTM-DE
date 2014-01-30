
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

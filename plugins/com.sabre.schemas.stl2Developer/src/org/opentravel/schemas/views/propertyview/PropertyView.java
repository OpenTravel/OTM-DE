package org.opentravel.schemas.views.propertyview;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.opentravel.schemas.controllers.MainController.IRefreshListener;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.NavigatorView;
import org.opentravel.schemas.views.OtmAbstractView;
import org.opentravel.schemas.views.RepositoryView;

public class PropertyView extends PageBookView implements ISelectionListener, IRefreshListener {

    public static final String VIEW_ID = "org.opentravel.schemas.stl2Developer.PropertyView";

    public PropertyView() {
        super();
        OtmRegistry.getMainController().addRefreshListener(this);
    }

    private IWorkbenchPart currentPart;

    @Override
    public void init(IViewSite site) throws PartInitException {
        site.getPage().addPostSelectionListener(this);
        super.init(site);
    }

    @Override
    public void partActivated(IWorkbenchPart part) {
        super.partActivated(part);
        PageRec pr = getPageRec(getCurrentPage());
        if (pr != null && pr.part.equals(part)) {
            if (isImportant(part)) {
                currentPart = part;
            }
        }
    }

    @Override
    protected IPage createDefaultPage(PageBook book) {
        Page page = null;
        page = new PropertySheetPage(null);
        page.createControl(getPageBook());
        initPage(page);
        return page;
    }

    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        Page page = null;
        if (part instanceof OtmAbstractView) {
            INode node = ((OtmAbstractView) part).getCurrentNode();
            page = new PropertySheetPage((Node) node);
            page.createControl(getPageBook());
            initPage(page);
            return new PageRec(part, page);
        }
        return null;
    }

    @Override
    protected IWorkbenchPart getBootstrapPart() {
        return null;
    }

    @Override
    protected boolean isImportant(IWorkbenchPart part) {
        Set<String> allowedViews = new HashSet<String>();
        allowedViews.add(NavigatorView.VIEW_ID);
        allowedViews.add(RepositoryView.VIEW_ID);
        String partId = part.getSite().getId();
        return allowedViews.contains(partId);
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        // we ignore null selection, or if we are pinned, or our own selection or same selection
        if (selection == null || !isImportant(part)) {
            return;
        }

        // we ignore selection if we are hidden OR selection is coming from another source as the
        // last one
        if (part == null || !part.equals(currentPart)) {
            return;
        }

        currentPart = part;

        // pass the selection to the page
        PropertySheetPage page = (PropertySheetPage) getCurrentPage();
        if (page != null) {
            page.selectionChanged(currentPart, selection);
        }
        setPartName("Properties [" + page.getName() + "]");
    }

    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
        IPage page = pageRecord.page;
        page.dispose();
        pageRecord.dispose();
    }

    @Override
    public void refresh() {
        PropertySheetPage page = (PropertySheetPage) getCurrentPage();
        if (page != null) {
            page.refresh();
        }
    }

    @Override
    public void refresh(INode node) {
        refresh();
    }

}

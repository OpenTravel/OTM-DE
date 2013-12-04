/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.views.example;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.OtmAbstractView;
import com.sabre.schemas.widgets.WidgetFactory;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ExampleXmlView extends OtmAbstractView implements ISelectionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleXmlView.class);

    private Text xmlBox;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(final Composite parent) {
        LOGGER.info("Initializing part control of " + this.getClass());
        OtmRegistry.registerExampleXmlView(this);
        xmlBox = initializeXmlBox(parent);
        getSite().getPage().addSelectionListener("com.sabre.schemas.stl2Developer.ExampleView",
                this);
        LOGGER.info("Done initializing part control of " + this.getClass());
    }

    /**
     * @param mainSashForm
     * @return
     */
    private Text initializeXmlBox(final Composite parent) {
        final Text text = WidgetFactory.createText(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP
                | SWT.V_SCROLL);
        text.setEditable(false);
        return text;
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            final Object o = ((IStructuredSelection) selection).getFirstElement();
            if (o instanceof ExampleModel) {
                String s = ((ExampleModel) o).getXmlString();
                if (s == null) {
                    s = "";
                }
                xmlBox.setText(s);
            }
        }
    }

    @Override
    public INode getCurrentNode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Node> getSelectedNodes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getViewID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh(INode node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCurrentNode(INode node) {
        // TODO Auto-generated method stub

    }

}

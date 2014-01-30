
package org.opentravel.schemas.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.controllers.NodeUtils;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.trees.type.TypeTree;
import org.opentravel.schemas.trees.type.TypeTreeNameFilter;
import org.opentravel.schemas.trees.type.TypeTreeNamespaceFilter;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;

/**
 * Wizard to allow user to select a type for the passed node objects.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeSelectionPage extends WizardPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeSelectionPage.class);

    private INode curNode = null;
    private ArrayList<Node> curNodeList = null;
    private boolean listMode = false;
    ArrayList<Node> firstNodeOnly = new ArrayList<Node>();
    private Node selectedNode;
    private Composite container;
    private TypeTree typeTree;
    private TreeViewer treeViewer;
    private TypeTreeNamespaceFilter nsFilter;
    private TypeTreeNameFilter nameFilter;
    private TypeSelectionFilter typeSelectionFilter;
    private IContentProvider typeTreeContentProvider;
    private Text name;
    private Text nameSpace;
    private Text typeText;
    private Text descriptionText;

    private IDoubleClickListener doubleClickListener = null;
    private TypeSelectionListener typeSelectionListener;
    private boolean canNavigateToNextPage = true;

    private final Map<Button, Class<? extends AbstractLibrary>> radioButtons = new HashMap<Button, Class<? extends AbstractLibrary>>();

    protected TypeSelectionPage(final String pageName, final String title, String description,
            final ImageDescriptor titleImage, final INode n) {
        super(pageName, title, titleImage);
        setTitle(title);
        setDescription(description);
        curNode = n;
        LOGGER.debug("Created Initial Type Selection Page for node: " + n);
        checkCore((Node) n);
    }

    protected void checkCore(Node curNode) {
        if (curNode instanceof CoreObjectNode) {
            FacetNode sum = (FacetNode) ((CoreObjectNode) curNode).getSummaryFacet();
            TLFacet tlSum = (TLFacet) sum.getTLModelObject();
            LOGGER.debug(curNode + " now has " + sum.getInheritedChildren().size()
                    + " inherited children and " + sum.getChildren().size() + " children.");
            LOGGER.debug(curNode + " now has "
                    + PropertyCodegenUtils.getInheritedFacetProperties(tlSum).size()
                    + " inherited elements and " + tlSum.getElements().size()
                    + " element children.");
        }
    }

    /**
     * Set up the page to ask if to make changes to the first node in the list or all of them.
     */
    protected TypeSelectionPage(final String pageName, final String title, String description,
            final ImageDescriptor titleImage, ArrayList<Node> nodeList) {

        this(pageName, title, description, titleImage, nodeList.get(0));

        if (nodeList == null || nodeList.size() <= 0)
            return;

        curNodeList = new ArrayList<Node>(nodeList);
        curNode = curNodeList.get(0); // give tree something to work with.
        firstNodeOnly.add(curNodeList.get(0)); // used for buttons
        if (curNodeList.size() > 1)
            listMode = true;

        LOGGER.debug("Page initialized.");
        for (INode n : curNodeList)
            LOGGER.debug(" Node: " + n);
    }

    public void setTypeSelectionListener(TypeSelectionListener listener) {
        this.typeSelectionListener = listener;
    }

    /**
     * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
     */
    @Override
    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage() && canNavigateToNextPage;
    }

    @Override
    public void createControl(final Composite parent) {
        container = new Composite(parent, SWT.BORDER);
        container.setLayout(new GridLayout(2, false));

        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;

        final Label l1 = new Label(container, SWT.NONE);
        l1.setText("Assign Type to:");

        name = WidgetFactory.createText(container, SWT.READ_ONLY | SWT.BORDER);
        name.setLayoutData(gridData);
        // (if a node list is presented, show the names of all of them
        if (listMode) {
            String nlNames = "";
            int i = 0;
            for (INode n : curNodeList) {
                if (i++ > 0)
                    nlNames = nlNames.concat(", ");
                nlNames = nlNames.concat(n.getName());
            }
            name.setText(nlNames);
            final Label nlChoiceLabel = new Label(container, SWT.NONE);
            nlChoiceLabel.setText("Assign to: ");
            choiceButtons(container);
        } else
            name.setText(curNode.getName());

        final Label label = new Label(container, SWT.NULL);
        label.setText("Type:");

        typeText = WidgetFactory.createText(container, SWT.BORDER);
        typeText.setLayoutData(gridData);
        typeText.addKeyListener(new TypeKeyListener());
        typeText.setFocus();

        // Namespace field
        final Label nsLabel = new Label(container, SWT.NULL);
        nsLabel.setText("Type Namespace:");
        nameSpace = WidgetFactory.createText(container, SWT.BORDER);
        nameSpace.setLayoutData(gridData);
        nameSpace.addKeyListener(new TypeKeyListener());

        // Description Field
        final Label desLabel = new Label(container, SWT.NULL);
        desLabel.setText("Description:");
        descriptionText = WidgetFactory.createText(container, SWT.BORDER | SWT.READ_ONLY);
        descriptionText.setLayoutData(gridData);

        // Tree filter by namespace
        final Label l2 = new Label(container, SWT.NULL);
        l2.setText("Show namespaces:");
        radioButtons(container);

        // Type Tree
        // typeTree = new TypeTree(Node.getModelNode(), typeTreeContentProvider);
        typeTree = new TypeTree(Node.getModelNode(), null);
        treeViewer = typeTree.setUpViewer(container);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalSpan = 2;
        treeViewer.getTree().setLayoutData(gridData);

        nameFilter = new TypeTreeNameFilter();
        treeViewer.addFilter(nameFilter);
        nsFilter = new TypeTreeNamespaceFilter();
        treeViewer.addFilter(nsFilter);
        treeViewer.refresh();

        treeViewer.addSelectionChangedListener(new TTSelectionListener());
        if (doubleClickListener != null) {
            treeViewer.addDoubleClickListener(doubleClickListener);
        }
        List<Node> excluded = getExcludeNodes(curNode);
        if (!excluded.isEmpty()) {
            treeViewer.addFilter(new CurrentNodeFilter(excluded));
        }
        if (typeSelectionFilter != null) {
            treeViewer.addFilter(typeSelectionFilter);
        }
        // treeViewer.refresh(); // shouldn't be needed, but i was missing a changed BO to core once
        // as base of a core

        setControl(container); // Required to avoid an error in the system
        setPageComplete(false);
    }

    public void setTypeSelectionFilter(TypeSelectionFilter typeSelectionFilter) {
        this.typeSelectionFilter = typeSelectionFilter;
    }

    public void setTypeTreeContentProvider(IContentProvider typeTreeContentProvider) {
        this.typeTreeContentProvider = typeTreeContentProvider;
    }

    public void addDoubleClickListener(final IDoubleClickListener listener) {
        doubleClickListener = listener;
    }

    protected Node getSelectedNode() {
        return selectedNode;
    }

    protected ArrayList<Node> getCurNodeList() {
        return curNodeList;
    }

    private class CurrentNodeFilter extends ViewerFilter {

        private List<Node> excluded;

        public CurrentNodeFilter(List<Node> excluded) {
            this.excluded = excluded;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (conatinReference(excluded, element)) {
                return false;
            }
            return true;
        }

        private boolean conatinReference(List<Node> objects, Object element) {
            for (Node o : objects) {
                if (o == element) {
                    return true;
                }
            }
            return false;
        }
    }

    private List<Node> getExcludeNodes(INode node) {
        Node n = (Node) node;
        if (NodeUtils.checker(n).ownerIs(ComponentNodeType.VWA).is(PropertyNodeType.SIMPLE).get()) {
            return Collections.singletonList(n.getOwningComponent());
        } else if (NodeUtils.checker(n).ownerIs(ComponentNodeType.CORE).is(PropertyNodeType.SIMPLE)
                .get()) {
            return Collections.singletonList(n.getOwningComponent());
        } else if (NodeUtils.checker(n).is(ComponentNodeType.CORE).get()) {
            return Collections.singletonList(n);
        }
        return Collections.emptyList();
    }

    private final class TTSelectionListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(final SelectionChangedEvent event) {
            checkCore((Node) curNode);
            final IStructuredSelection iss = (IStructuredSelection) event.getSelection();
            final Object object = iss.getFirstElement();
            if ((object == null) || (!(object instanceof Node))) {
                return;
            }
            final Node n = (Node) object;
            nameSpace.setText(n.getNamespace());
            typeText.setText(n.getName());
            descriptionText.setText(n.getDescription());

            // If no type selection filter is assigned, the selection is valid as long as it is
            // assignable. If a
            // filter is present, delegate the evaluation of the selected node to the filter.
            if ((typeSelectionFilter == null) ? n.isAssignable() : typeSelectionFilter
                    .isValidSelection(n)) {
                selectedNode = n;
                setPageComplete(true); // create logic
            } else {
                selectedNode = null;
                setPageComplete(false);
            }
            if (typeSelectionListener != null) {
                canNavigateToNextPage = typeSelectionListener.notifyTypeSelected(selectedNode);
                getContainer().updateButtons();
            }
            checkCore((Node) curNode);
        }
    }

    private final class TypeKeyListener implements KeyListener {
        @Override
        public void keyPressed(final KeyEvent e) {
        }

        @Override
        // Only Name must be filled out to move to next page
        public void keyReleased(final KeyEvent e) {
            final String txt = typeText.getText();
            nameFilter.setText(txt);
            // treeViewer.expandAll();
            treeViewer.refresh();
        }
    }

    public final class ButtonSelectionHandler extends SelectionAdapter {

        @Override
        public void widgetSelected(final SelectionEvent e) {
            if (e.widget instanceof Button) {
                final Button b = (Button) e.widget;
                if (b.getData() == null) {
                    // Namespaces radio button selected
                    nsFilter.setLibrary(radioButtons.get(b));
                } else {
                    // Property selection radio button
                    curNodeList = (ArrayList<Node>) b.getData();
                }
            }
            treeViewer.expandAll();
            treeViewer.refresh();
        }
    }

    private void radioButtons(final Composite c) {
        final Composite c2 = new Composite(c, SWT.NULL);
        final GridLayout gl2 = new GridLayout(4, false);
        c2.setLayout(gl2);

        Button radio = new Button(c2, SWT.RADIO);
        radio.setText("All");
        radio.setSelection(true);
        radioButtons.put(radio, AbstractLibrary.class);
        radio.addSelectionListener(new ButtonSelectionHandler());

        radio = new Button(c2, SWT.RADIO);
        radio.setText("Libraries");
        radioButtons.put(radio, TLLibrary.class);
        radio.addSelectionListener(new ButtonSelectionHandler());

        radio = new Button(c2, SWT.RADIO);
        radio.setText("Built-ins");
        radioButtons.put(radio, BuiltInLibrary.class);
        radio.addSelectionListener(new ButtonSelectionHandler());

        radio = new Button(c2, SWT.RADIO);
        radio.setText("XSD Schemas");
        radioButtons.put(radio, XSDLibrary.class);
        radio.addSelectionListener(new ButtonSelectionHandler());

    }

    private void choiceButtons(Composite c) {
        final Composite ch2 = new Composite(c, SWT.NULL);
        final GridLayout glh2 = new GridLayout(4, false);
        ch2.setLayout(glh2);

        Button radio = new Button(ch2, SWT.RADIO);
        radio.setText("All");
        radio.setSelection(true);
        radio.setData(curNodeList);
        radio.addSelectionListener(new ButtonSelectionHandler());

        radio = new Button(ch2, SWT.RADIO);
        radio.setText(curNodeList.get(0).getName() + " Only");
        radio.setData(firstNodeOnly);
        radio.addSelectionListener(new ButtonSelectionHandler());
    }

}

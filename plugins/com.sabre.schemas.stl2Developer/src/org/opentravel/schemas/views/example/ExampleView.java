/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.views.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.preferences.CompilerPreferences;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.FindingsDialog;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmAbstractView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions.DetailLevel;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ExampleView extends OtmAbstractView {
    public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.ExampleView";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleView.class);

    private TreeViewer viewer;

    public ExampleView() {
        OtmRegistry.registerExampleView(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(final Composite parent) {
        LOGGER.info("Initializing part control of " + this.getClass());
        viewer = initializeTreeViewer(parent);
        getSite().setSelectionProvider(viewer);
        LOGGER.info("Done initializing part control of " + this.getClass());
    }

    private TreeViewer initializeTreeViewer(final Composite parent) {
        final TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER);

        viewer.setContentProvider(new ITreeContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public boolean hasChildren(Object element) {
                return !((ExampleModel) element).getChildren().isEmpty();
            }

            @Override
            public ExampleModel getParent(Object element) {
                if (element instanceof ExampleModel)
                    return ((ExampleModel) element).getParent();
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public Object[] getElements(Object inputElement) {
                return ((List<ExampleModel>) inputElement).toArray();
            }

            @Override
            public Object[] getChildren(Object parentElement) {
                return ((ExampleModel) parentElement).getChildren().toArray();
            }
        });
        viewer.setLabelProvider(new ExampleTreeProvider());
        viewer.setSorter(new ViewerSorter() {

            @Override
            public int category(Object element) {
                // paint errors as last nodes in tree
                if (element instanceof ErrorExampleModel) {
                    return 1;
                } else {
                    return 0;
                }
            }

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                // disable sorting by name
                return 0;
            }

        });
        return viewer;
    }

    class ExampleTreeProvider extends LabelProvider implements IColorProvider {

        @Override
        public Color getForeground(Object element) {
            if (element instanceof ErrorExampleModel) {
                return OtmRegistry.getMainWindow().getColorProvider().getColor(SWT.COLOR_DARK_GRAY);
            }
            return null;
        }

        @Override
        public Color getBackground(Object element) {
            return null;
        }

        @Override
        public Image getImage(Object element) {
            return ((ExampleModel) element).getLabelProvider().getImage(element);
        }

        @Override
        public String getText(Object element) {
            return ((ExampleModel) element).getLabelProvider().getText(element);
        }

    }

    public void generateExamples() {
        final ModelNode modelNode = mc.getModelNode();
        clearExamples();
        if (modelNode != null)
            generateExamples(modelNode.getUserLibraries());
    }

    public void clearExamples() {
        if (viewer != null) {
            viewer.setInput(null);
            viewer.refresh();
        }
    }

    public void generateExamples(final List<LibraryNode> libraries) {
        List<ExampleModel> examples = new ArrayList<ExampleModel>(libraries.size());
        Map<LibraryChainNode, ExampleModel> chainRoot = new HashMap<LibraryChainNode, ExampleModel>();
        for (final LibraryNode lib : libraries) {
            ExampleModel libModel = null;
            if (lib.isInChain()) {
                ExampleModel root = chainRoot.get(lib.getChain());
                if (root == null) {
                    root = new ExampleModel(lib.getChain());
                    chainRoot.put(lib.getChain(), root);
                    examples.add(root);
                }
                libModel = new ExampleModel(lib);
                root.addChildren(Collections.singletonList(libModel));
            } else {
                libModel = new ExampleModel(lib);
                examples.add(libModel);
            }
            ValidationFindings findings = new ValidationFindings();
            List<ExampleModel> children = generateExamplesForLibrary(lib, findings);
            if (!findings.isEmpty()) {
                showFindingsDialog(lib.getName(), findings);
            }
            libModel.addChildren(children);
        }
        if (viewer != null) {
            viewer.setInput(examples);
            viewer.refresh(true);
        }
    }

    private void showFindingsDialog(String libName, ValidationFindings findings) {
        FindingsDialog.open(Display.getDefault().getActiveShell(), "Validation erros",
                "Could not generate all the examples properly for library " + libName
                        + " - there are validation errors. Correct the errors and try again.",
                findings.getAllFindingsAsList());

    }

    /**
     * @param lib
     * @param findings
     * @return
     */
    private List<ExampleModel> generateExamplesForLibrary(LibraryNode lib,
            ValidationFindings findings) {
        return generateExamplesForNode(lib.getChildren(), findings);
    }

    /**
     * @param children
     * @return
     */
    private List<ExampleModel> generateExamplesForNode(List<Node> children,
            ValidationFindings findingsAggregator) {
        List<ExampleModel> ret = new ArrayList<ExampleModel>();
        for (Node child : children) {
            NamedEntity namedEntity = getNamedEntity(child);
            if (namedEntity != null) {
                ExampleModel childModel = null;
                try {
                    Object[] examples = generateExample(namedEntity);
                    if (child instanceof VersionNode) {
                        child = ((VersionNode) child).getNewestVersion();
                    }
                    if (child.isService()) {
                        childModel = new ExampleModel(child);
                        childModel.addChildren(generateExamplesForNode(child.getChildren(),
                                findingsAggregator));
                    } else if (child.isOperation()) {
                        childModel = new ExampleModel(child);
                        childModel.addChildren(generateExamplesForNode(child.getChildren(),
                                findingsAggregator));
                    } else {
                        if (namedEntity instanceof TLFacet) {
                            TLFacet facet = (TLFacet) namedEntity;
                            if (!facet.declaresContent()) {
                                break;
                            }
                        }
                        if (((Document) examples[0]).getDocumentElement() != null) {
                            childModel = new DOMExampleModel(child,
                                    ((Document) examples[0]).getDocumentElement());
                            childModel.setXmlString((String) examples[1]);
                        }
                    }
                } catch (ValidationException e) {
                    childModel = new ErrorExampleModel(child);
                    findingsAggregator.addAll(e.getFindings());
                    LOGGER.debug("Validation errors: "
                            + Arrays.toString(e.getFindings().getAllValidationMessages(
                                    FindingMessageFormat.IDENTIFIED_FORMAT)));
                } catch (CodeGenerationException e) {
                    DialogUserNotifier.openError("Example View",
                            "Could not generate examples properly - an error occurred: " + e);
                    LOGGER.error("Exception during example generation", e);
                }
                if (childModel != null)
                    ret.add(childModel);
            } else if (!child.getChildren().isEmpty()) {
                ret.addAll(generateExamplesForNode(child.getChildren(), findingsAggregator));
            }
        }
        return ret;
    }

    /**
     * @param namedEntity
     * @return
     * @throws CodeGenerationException
     * @throws ValidationException
     */
    private Object[] generateExample(NamedEntity namedEntity) throws ValidationException,
            CodeGenerationException {
        ExampleDocumentBuilder exampleBuilder = new ExampleDocumentBuilder();
        ExampleGeneratorOptions examOptions = setupExampleGeneratorOptions();
        exampleBuilder.setModelElement(namedEntity).setOptions(examOptions);
        Document dom = exampleBuilder.buildDomTree();
        String xml = exampleBuilder.buildString();
        return new Object[] { dom, xml };
    }

    private ExampleGeneratorOptions setupExampleGeneratorOptions() {
        // Setup the example generation options using the compiler preferences
        final CompilerPreferences compilePreferences = new CompilerPreferences(
                CompilerPreferences.loadPreferenceStore());
        final ExampleGeneratorOptions examOptions = new ExampleGeneratorOptions();
        examOptions
                .setDetailLevel(compilePreferences.isGenerateMaxDetailsForExamples() ? DetailLevel.MAXIMUM
                        : DetailLevel.MINIMUM);
        examOptions.setMaxRecursionDepth(compilePreferences.getExampleMaxDepth());
        examOptions.setMaxRepeat(compilePreferences.getExampleMaxRepeat());
        return examOptions;
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void expand() {
        if (viewer != null)
            viewer.expandAll();
    }

    @Override
    public void collapse() {
        if (viewer != null)
            viewer.collapseAll();
    }

    @Override
    public INode getCurrentNode() {
        return mc.getModelNode();
    }

    @Override
    public List<Node> getSelectedNodes() {
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(mc.getModelNode());
        return nodes;
    }

    @Override
    public String getViewID() {
        return VIEW_ID;
    }

    @Override
    public void refresh() {
        if (viewer != null)
            viewer.refresh();
    }

    @Override
    public void refresh(boolean regenerate) {
        if (regenerate) {
            generateExamples();
        }
        refresh();
        if (regenerate) {
            // force selection changed event in NavigatorView.
            OtmRegistry.getNavigatorView().refresh();
        }
    }

    @Override
    public void refresh(INode node, boolean force) {
        if (force) {
            generateExamples();
        }
        setCurrentNode(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.views.OtmView#refresh(org.opentravel.schemas.node.INode)
     */
    @Override
    public void refresh(INode node) {
        if (viewer != null)
            viewer.refresh();
    }

    @Override
    public void setCurrentNode(INode node) {
        if (viewer == null)
            return;
        if (node instanceof Node) {
            Node n = (Node) node;
            n = getOwningComponent(n);
            if (viewer.testFindItem(n) == null) {
                // Because tree is generate in lazy manner, then we need to expand
                // library
                // to force element children creations.
                expandAndSelect(n);
                viewer.setSelection(new StructuredSelection(n), true);
            } else {
                viewer.setSelection(new StructuredSelection(n), true);
            }
        }
    }

    private void expandAndSelect(Node node) {
        LinkedList<Node> parents = new LinkedList<Node>();
        while (viewer.testFindItem(node) == null) {
            node = node.getParent();
            // the given node is invalid for ExampleView, do nothing
            if (node == null) {
                return;
            }
            parents.addFirst(node);
        }
        for (Node p : parents) {
            viewer.expandToLevel(p, 1);
        }
    }

    /**
     * In case of Service and his children we always need to get Service instance. Otherwise simple
     * getOwningComponent should be enough.
     * 
     * @see ExampleView#generateExample(ExampleDocumentBuilder, ExampleGeneratorOptions,
     *      NamedEntity)
     */
    private Node getOwningComponent(Node n) {
        if (n == null || n.getParent() == null)
            return null; // Node could be removed/deleted.
        if (n.isOperation() || n.getParent().isOperation()) {
            return n;
        }
        n = n.getOwningComponent();
        return n;
    }

    private NamedEntity getNamedEntity(Node node) {
        ModelObject<?> mo = node.getModelObject();
        if (mo != null) {
            Object object = mo.getTLModelObj();
            if (object instanceof NamedEntity)
                return (NamedEntity) object;
        }
        return null;
    }

}

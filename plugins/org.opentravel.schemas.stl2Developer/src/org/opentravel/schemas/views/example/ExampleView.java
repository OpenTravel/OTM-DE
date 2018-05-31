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
package org.opentravel.schemas.views.example;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmAbstractView;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Legacy view to be removed in the future. It exists to allow the user to find out that this view has been removed and
 * to force it to be removed from the saved layouts (May 30, 2018)
 * 
 * @author Agnieszka Janowska, Dave Hollander
 * 
 */
@Deprecated
public class ExampleView extends OtmAbstractView {
	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.ExampleView";

	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleView.class);

	private TreeViewer viewer;

	public ExampleView() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(final Composite parent) {
		// LOGGER.info("Initializing part control of " + this.getClass());

		final Text text = WidgetFactory.createText(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setEditable(false);
		text.setText("Example views have been removed from OTM-DE. \nPLEASE CLOSE THIS WINDOW.");
		OtmRegistry.registerExampleView(this);

		// viewer = initializeTreeViewer(parent);
		// getSite().setSelectionProvider(viewer);
		// LOGGER.info("Done initializing part control of " + this.getClass());
	}

	// private TreeViewer initializeTreeViewer(final Composite parent) {
	// final TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	//
	// viewer.setContentProvider(new ITreeContentProvider() {
	//
	// @Override
	// public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	// }
	//
	// @Override
	// public void dispose() {
	// }
	//
	// @Override
	// public boolean hasChildren(Object element) {
	// return !((ExampleModel) element).getChildren().isEmpty();
	// }
	//
	// @Override
	// public ExampleModel getParent(Object element) {
	// if (element instanceof ExampleModel)
	// return ((ExampleModel) element).getParent();
	// return null;
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// public Object[] getElements(Object inputElement) {
	// return ((List<ExampleModel>) inputElement).toArray();
	// }
	//
	// @Override
	// public Object[] getChildren(Object parentElement) {
	// return ((ExampleModel) parentElement).getChildren().toArray();
	// }
	// });
	// viewer.setLabelProvider(new ExampleTreeProvider());
	// viewer.setSorter(new ViewerSorter() {
	//
	// @Override
	// public int category(Object element) {
	// // paint errors as last nodes in tree
	// if (element instanceof ErrorExampleModel) {
	// return 1;
	// } else {
	// return 0;
	// }
	// }
	//
	// @Override
	// public int compare(Viewer viewer, Object e1, Object e2) {
	// // disable sorting by name
	// return 0;
	// }
	//
	// });
	// return viewer;
	// }

	// class ExampleTreeProvider extends LabelProvider implements IColorProvider, IFontProvider {
	//
	// /**
	// * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	// */
	// @Override
	// public Font getFont(Object element) {
	// Font font = null; // null to use default font
	// if (element instanceof DOMExampleModel) {
	// DOMExampleModel de = (DOMExampleModel) element;
	// PropertyNode prop = null;
	// if (de.getOwningNode() instanceof PropertyNode)
	// prop = (PropertyNode) de.getOwningNode();
	//
	// if (prop != null && !prop.isMandatory())
	// font = Fonts.getFontRegistry().get(Fonts.inheritedItem);
	// }
	// // font = Fonts.getFontRegistry().get(Fonts.readOnlyItem);
	// // font = Fonts.getFontRegistry().get(Fonts.inheritedItem);
	// return font;
	// }
	//
	// @Override
	// public Color getForeground(Object element) {
	// if (element instanceof ErrorExampleModel) {
	// return OtmRegistry.getMainWindow().getColorProvider().getColor(SWT.COLOR_DARK_GRAY);
	// }
	// return null;
	// }
	//
	// @Override
	// public Color getBackground(Object element) {
	// return null;
	// }
	//
	// @Override
	// public Image getImage(Object element) {
	// return ((ExampleModel) element).getLabelProvider().getImage(element);
	// }
	//
	// @Override
	// public String getText(Object element) {
	// return ((ExampleModel) element).getLabelProvider().getText(element);
	// }
	//
	// }

	// public void generateExamples() {
	// // final ModelNode modelNode = mc.getModelNode();
	// // clearExamples();
	// // if (modelNode != null)
	// // generateInBackground(modelNode.getUserLibraries());
	// }

	// public void clearExamples() {
	// // // If not in the UI thread, sync w/ ui thread.
	// // if (Display.getCurrent() == null) {
	// // Display.getDefault().asyncExec(new Runnable() {
	// // @Override
	// // public void run() {
	// // if (viewerIsOk()) {
	// // viewer.setInput(null);
	// // viewer.refresh();
	// // }
	// // }
	// // });
	// // } else if (viewerIsOk()) {
	// // viewer.setInput(null);
	// // viewer.refresh();
	// // }
	// }

	// List<ExampleModel> examples = new ArrayList<>();

	// public void generateInBackground(final List<LibraryNode> libraries) {
	// // examples.clear();
	// // if (Display.getCurrent() == null)
	// // generateExamples(libraries); // not in UI Thread
	// // else {
	// // // run in a background job
	// // mc.postStatus("Generating Examples.");
	// // Job job = new Job("Generating examples") {
	// // @Override
	// // protected IStatus run(IProgressMonitor monitor) {
	// // monitor.beginTask("Generating Examples ", libraries.size());
	// // // Make the generate work on individual libraries and use the worked to track progress
	// // for (LibraryNode lib : libraries) {
	// // monitor.subTask(lib.getName());
	// // generateExamplesForLibrary(lib);
	// // monitor.worked(1);
	// // }
	// // monitor.done();
	// // syncWithUI("Done.");
	// // return Status.OK_STATUS;
	// // }
	// // };
	// // job.setUser(true);
	// // job.schedule();
	// // }
	// // // set up viewer now for refresh when job is done
	// // if (viewerIsOk()) {
	// // viewer.setInput(examples);
	// // viewer.refresh(true);
	// // }
	// }

	// private void syncWithUI(String msg) {
	// DialogUserNotifier.syncWithUi(msg);
	// }

	// Map<LibraryChainNode, ExampleModel> chainRoot = new HashMap<>();

	// private void generateExamples(final List<LibraryNode> libraries) {
	// // for (final LibraryNode lib : libraries) {
	// // generateExamplesForLibrary(lib);
	// // }
	// }

	// private void generateExamplesForLibrary(LibraryNode lib) {
	// // ExampleModel libModel = null;
	// // if (lib.isInChain()) {
	// // ExampleModel root = chainRoot.get(lib.getChain());
	// // if (root == null) {
	// // root = new ExampleModel(lib.getChain());
	// // chainRoot.put(lib.getChain(), root);
	// // examples.add(root);
	// // }
	// // libModel = new ExampleModel(lib);
	// // root.addChildren(Collections.singletonList(libModel));
	// // } else {
	// // libModel = new ExampleModel(lib);
	// // examples.add(libModel);
	// // }
	// // ValidationFindings findings = new ValidationFindings();
	// // List<ExampleModel> children = generateExamplesForLibrary(lib, findings);
	// //
	// // // if (!findings.isEmpty()) {
	// // // showFindingsDialog(lib.getName(), findings);
	// // // }
	// // libModel.addChildren(children);
	// // LOGGER.debug("Generated " + children.size() + " examples for " + lib);
	// }

	// private void showFindingsDialog(String libName, ValidationFindings findings) {
	// FindingsDialog.open(Display.getDefault().getActiveShell(), "Validation erros",
	// "Could not generate all the examples properly for library " + libName
	// + " - there are validation errors. Correct the errors and try again.",
	// findings.getAllFindingsAsList());
	// }

	// /**
	// * @param lib
	// * @param findings
	// * @return
	// */
	// private List<ExampleModel> generateExamplesForLibrary(LibraryNode lib, ValidationFindings findings) {
	//// return generateExamplesForNode(lib.getChildren(), findings);
	// }

	// /**
	// * @param children
	// * @return
	// */
	// private List<ExampleModel> generateExamplesForNode(List<Node> children, ValidationFindings findingsAggregator) {
	// List<ExampleModel> ret = new ArrayList<>();
	// for (Node child : children) {
	// NamedEntity namedEntity = getNamedEntity(child);
	// if (namedEntity != null) {
	// ExampleModel childModel = null;
	// try {
	// Object[] examples = generateExample(namedEntity);
	// if (child instanceof VersionNode) {
	// child = ((VersionNode) child).getNewestVersion();
	// }
	// if (child instanceof ServiceNode) {
	// childModel = new ExampleModel(child);
	// childModel.addChildren(generateExamplesForNode(child.getChildren(), findingsAggregator));
	// } else if (child instanceof OperationNode) {
	// childModel = new ExampleModel(child);
	// childModel.addChildren(generateExamplesForNode(child.getChildren(), findingsAggregator));
	// } else {
	// if (namedEntity instanceof TLFacet) {
	// TLFacet facet = (TLFacet) namedEntity;
	// if (!facet.declaresContent()) {
	// break;
	// }
	// }
	// if (((Document) examples[0]).getDocumentElement() != null) {
	// childModel = new DOMExampleModel(child, ((Document) examples[0]).getDocumentElement());
	// childModel.setXmlString((String) examples[1]);
	// childModel.setJsonString((String) examples[2]);
	//
	// // LOGGER.debug("DOM Doc Element : nodeName = " + ((DOMExampleModel)
	// // childModel).getNodeName());
	// }
	// }
	// } catch (ValidationException e) {
	// childModel = new ErrorExampleModel(child);
	// findingsAggregator.addAll(e.getFindings());
	// // LOGGER.debug("Validation errors: "
	// // + Arrays.toString(e.getFindings().getAllValidationMessages(
	// // FindingMessageFormat.IDENTIFIED_FORMAT)));
	// } catch (CodeGenerationException e) {
	// DialogUserNotifier.openError("Example View",
	// "Could not generate examples properly - an error occurred: " + e.getLocalizedMessage(), e);
	// LOGGER.error("Exception during example generation", e);
	// }
	// if (childModel != null)
	// ret.add(childModel);
	// } else if (!child.getChildren().isEmpty()) {
	// ret.addAll(generateExamplesForNode(child.getChildren(), findingsAggregator));
	// }
	// }
	// return ret;
	// }

	// /**
	// * @param namedEntity
	// * @return
	// * @throws CodeGenerationException
	// * @throws ValidationException
	// */
	// private Object[] generateExample(NamedEntity namedEntity) throws ValidationException, CodeGenerationException {
	// ExampleBuilder<Document> exampleBuilder = new ExampleDocumentBuilder(setupExampleGeneratorOptions())
	// .setModelElement(namedEntity);
	// Document dom = exampleBuilder.buildTree();
	// String xml = exampleBuilder.buildString();
	//
	// ExampleJsonBuilder exampleJsonBuilder = new ExampleJsonBuilder(setupExampleGeneratorOptions());
	// exampleJsonBuilder.setModelElement(namedEntity);
	// String json = exampleJsonBuilder.buildString();
	//
	// return new Object[] { dom, xml, json };
	// }

	// private ExampleGeneratorOptions setupExampleGeneratorOptions() {
	// // Setup the example generation options using the compiler preferences
	// final CompilerPreferences compilePreferences = new CompilerPreferences(
	// CompilerPreferences.loadPreferenceStore());
	// final ExampleGeneratorOptions examOptions = new ExampleGeneratorOptions();
	// examOptions.setDetailLevel(
	// compilePreferences.isGenerateMaxDetailsForExamples() ? DetailLevel.MAXIMUM : DetailLevel.MINIMUM);
	// examOptions.setMaxRecursionDepth(compilePreferences.getExampleMaxDepth());
	// examOptions.setMaxRepeat(compilePreferences.getExampleMaxRepeat());
	// return examOptions;
	// }

	@Override
	public void setFocus() {
		// if (viewerIsOk())
		// viewer.getControl().setFocus();
	}

	@Override
	public void expand() {
		// if (viewerIsOk())
		// viewer.expandAll();
	}

	@Override
	public void collapse() {
		// if (viewerIsOk())
		// viewer.collapseAll();
	}

	@Override
	public INode getCurrentNode() {
		return mc.getModelNode();
	}

	@Override
	public List<Node> getSelectedNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		// nodes.add(mc.getModelNode());
		return nodes;
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	@Override
	public void refresh() {
		// if (viewerIsOk())
		// viewer.refresh();
	}

	@Override
	public void refresh(boolean regenerate) {
		// if (regenerate) {
		// generateExamples();
		// }
		// refresh();
		// if (regenerate) {
		// // force selection changed event in NavigatorView.
		// OtmRegistry.getNavigatorView().refresh();
		// }
	}

	@Override
	public void refresh(INode node, boolean force) {
		// if (force) {
		// generateExamples();
		// }
		// setCurrentNode(node);
	}

	// private boolean viewerIsOk() {
	// return (viewer != null && viewer.getTree() != null && !viewer.getTree().isDisposed());
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.views.OtmView#refresh(org.opentravel.schemas.node.INode)
	 */
	@Override
	public void refresh(INode node) {
		// if (viewerIsOk())
		// viewer.refresh();
	}

	@Override
	public void setCurrentNode(INode node) {
		// if (!viewerIsOk())
		// return;
		// if (node instanceof Node) {
		// Node n = (Node) node;
		// n = getOwningComponent(n);
		// if (n == null)
		// return;
		// if (viewer.testFindItem(n) == null) {
		// // Because tree is generate in lazy manner, then we need to expand library
		// // to force element children creations.
		// expandAndSelect(n);
		// viewer.setSelection(new StructuredSelection(n), true);
		// } else {
		// viewer.setSelection(new StructuredSelection(n), true);
		// }
		// }
	}

	// private void expandAndSelect(Node node) {
	// // if (node != null && viewerIsOk()) {
	// // LinkedList<Node> parents = new LinkedList<>();
	// // while (viewer.testFindItem(node) == null) {
	// // node = node.getParent();
	// // // the given node is invalid for ExampleView, do nothing
	// // if (node == null) {
	// // return;
	// // }
	// // parents.addFirst(node);
	// // }
	// // for (Node p : parents) {
	// // viewer.expandToLevel(p, 1);
	// // }
	// // }
	// }

	// /**
	// * In case of Service and his children we always need to get Service instance. Otherwise simple getOwningComponent
	// * should be enough.
	// *
	// * @see ExampleView#generateExample(ExampleDocumentBuilder, ExampleGeneratorOptions, NamedEntity)
	// */
	// private Node getOwningComponent(Node n) {
	// if (n == null || n.getParent() == null)
	// return null; // Node could be removed/deleted.
	// if (n instanceof OperationNode || n.getParent() instanceof OperationNode) {
	// return n;
	// }
	// n = (Node) n.getOwningComponent();
	// return n;
	// }

	// private NamedEntity getNamedEntity(Node node) {
	// // ModelObject<?> mo = node.getModelObject();
	// // if (mo != null) {
	// Object object = node.getTLModelObject();
	// if (object instanceof NamedEntity)
	// return (NamedEntity) object;
	// // }
	// return null;
	// }

}

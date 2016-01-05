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
package org.opentravel.schemas.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventListener;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.ic.ModelIntegrityChecker;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.modelObject.events.OwnershipEventListener;
import org.opentravel.schemas.modelObject.events.ValueChangeEventListener;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.preferences.CompilerPreferences;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.FileDialogs;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.ValidationResultsView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Agnieszka Janowska
 * 
 */
public class DefaultModelController extends OtmControllerBase implements ModelController {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelController.class);

	private final LibraryController libraryController;
	private ModelNode modelRoot;
	public static String COMPILER_SUFFIX = "CompilerOutput";

	/**
	 * Create a model controller If needed, creates a new model node and TLModel.
	 * 
	 * @param mainController
	 * @param libraryController
	 */
	public DefaultModelController(final MainController mainController, final LibraryController libraryController) {
		super(mainController);

		this.libraryController = libraryController;
		if (modelRoot == null) {
			try {
				modelRoot = new ModelNode(newTLModel());
			} catch (LibraryLoaderException e) {
				LOGGER.error("Could not create TLModel.");
			}
		}
	}

	public LibraryController getLibraryController() {
		return libraryController;
	}

	@Override
	public ModelNode getModel() {
		return modelRoot;
	}

	@Override
	public TLModel getTLModel() {
		return Node.getModelNode().getTLModel();
	}

	@Override
	public ModelNode createNewModel() {
		// LOGGER.debug("Creating new model");
		ModelNode model = null;
		try {
			model = new ModelNode(newTLModel());
			// TODO - TEST - how can this work? Is it needed?
			// model.setMainWindow(mainWindow);
			libraryController.openLibrary(model);
		} catch (final LibraryLoaderException e) {
			LOGGER.error("Error while creating model", e);
			DialogUserNotifier.openError("Model error", "Could not create new model - " + e.getMessage());
		}
		OtmRegistry.getValidationResultsView().validateNode(Node.getModelNode());
		// TODO - prevent the extra validation message?
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.otmActions.ModelController#saveModel(org.opentravel.schemas.node.ModelNode )
	 */
	@Override
	public void saveModel(final INode model) {
		// LOGGER.debug("Saving model " + model);

		assert model != null;

		libraryController.saveAllLibraries(false);
	}

	/**
	 * * 3/28/2015 dmh - NEVER USED.
	 * 
	 * Closes the current model then creates a new empty model.
	 */
	@Override
	public void close() {
		// LOGGER.debug("Closing model.");
		LOGGER.error("CLOSE MODEL is NOT IMPLEMENTED.");
		// OtmView view = OtmRegistry.getNavigatorView();
		//
		// assert modelRoot != null;
		// assert modelRoot == mc.getModelNode();
		//
		// mc.getProjectController().closeAll();
		// modelRoot.close();
		//
		// // Now open up a new one to make it easier to use when opening a library
		// try {
		// modelRoot = new ModelNode(newTLModel());
		// } catch (LibraryLoaderException e) {
		// LOGGER.warn("Error creating new TL Model: " + e.getLocalizedMessage());
		// }
		// // Get a new Project controller to create default and built-in
		// mc.getProjectController().saveState(); // clear out eclipse saved state which is in
		// // constructor
		// mc.setProjectController(new DefaultProjectController(mc, MainController.getDefaultRepositoryManager()));
		// mc.getProjectController().getDefaultProject(); // triggers its creation.
		//
		// view.setCurrentNode(modelRoot);
		// mc.setModelNode(modelRoot);
		// mc.clearSelection();
		// mc.refresh();
	}

	@Override
	public void compileModel(ProjectNode project) {
		if (project == null)
			return;

		// Get a directory to compile into.
		String directoryName = project.getProject().getProjectFile().getAbsolutePath();
		directoryName = directoryName.substring(0, directoryName.length() - 4); // strip .otp
		if (directoryName == null || directoryName.isEmpty()) {
			directoryName = FileDialogs.postDirDialog(Messages.getString("fileDialog.directory.compilePath"));
			directoryName = directoryName + project.getName();
		}
		directoryName += "_" + COMPILER_SUFFIX;
		final File targetFolder = new File(directoryName);
		if (!targetFolder.exists()) {
			if (!targetFolder.mkdirs()) {
				LOGGER.warn("Could not make directory: " + targetFolder);
				DialogUserNotifier.openError("Model Error", "Could not make directory " + targetFolder.getPath()
						+ " for the compiled output.");
				return;
			}
		}

		// Do the compile.
		final ValidationFindings findings = new ValidationFindings();
		try {
			findings.addAll(compileModel(project.getProject(), targetFolder));
			displayUserMessage(findings, targetFolder);
		} catch (final SchemaCompilerException e) {
			DialogUserNotifier.openError("Model Error", "Could not compile the model - " + e.getMessage());
		} catch (final Exception e) {
			DialogUserNotifier.openError("Error",
					"Could not compile the model, unknown error occurred - " + e.getMessage());
		}
		final ValidationResultsView view = OtmRegistry.getValidationResultsView();
		if (view != null) {
			view.setFindings(findings, project);
		}
		mc.postStatus("Model compiled successfully in directory " + targetFolder.getAbsolutePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.otmActions.ModelController#compileModel(org.opentravel.schemas.node .ModelNode)
	 */
	@Override
	public void compileModel(ModelNode model) {
		// LOGGER.debug("Compiling model " + model);
		assert model != null;

		final TLModel tlModel = (model.getTLModel());
		String path = model.getFilePath();

		boolean isPathDirectory = false;
		if (path == null || path.isEmpty()) {
			path = FileDialogs.postDirDialog(Messages.getString("fileDialog.directory.compilePath"));
			isPathDirectory = true;
		}
		if (path != null) {
			if (!isPathDirectory) {
				path = path + "_codegenOutput";
			}
			final File targetFolder = new File(path);
			if (!targetFolder.exists()) {
				if (!targetFolder.mkdirs()) {
					LOGGER.warn("Could not make directory: " + targetFolder);
					DialogUserNotifier.openError("Model Error", "Could not make directory " + targetFolder.getPath()
							+ " for the compiled output.");
					return;
				}
			}

			final ValidationFindings findings = new ValidationFindings();
			try {
				findings.addAll(compileModel(tlModel, targetFolder));
				displayUserMessage(findings, targetFolder);
			} catch (final SchemaCompilerException e) {
				LOGGER.error("Cannot compile model", e);
				DialogUserNotifier.openError("Model Error", "Could not compile the model - " + e.getMessage());
			} catch (final Exception e) {
				LOGGER.error("Unknown compiler error", e);
				DialogUserNotifier.openError("Error",
						"Could not compile the model, unknown error occurred - " + e.getMessage());
			}
			final ValidationResultsView view = OtmRegistry.getValidationResultsView();
			if (view != null) {
				view.setFindings(findings, model);
			}
		}
	}

	public ValidationFindings compileModel(final TLModel tlModel, final File targetFolder)
			throws SchemaCompilerException {
		final CompilerPreferences compilePreferences = new CompilerPreferences(
				CompilerPreferences.loadPreferenceStore());
		final CompileAllCompilerTask codegenTask = new CompileAllCompilerTask();
		ValidationFindings findings = new ValidationFindings();

		// final ContextsView contextsView = OtmRegistry.getContextsView();
		// if (contextsView != null) {
		// codegenTask.setExampleContext(contextsView.getContextController().getDefaultContextId());
		// }
		codegenTask.applyTaskOptions(compilePreferences);
		codegenTask.setOutputFolder(targetFolder.getAbsolutePath());
		findings = codegenTask.compileOutput(tlModel);
		return findings;
	}

	public ValidationFindings compileModel(final Project project, final File targetFolder)
			throws SchemaCompilerException {
		final CompilerPreferences compilePreferences = new CompilerPreferences(
				CompilerPreferences.loadPreferenceStore());
		final CompileAllCompilerTask codegenTask = new CompileAllCompilerTask();

		// final ContextsView contextsView = OtmRegistry.getContextsView();
		// if (contextsView != null) {
		// codegenTask.setExampleContext(contextsView.getContextController().getDefaultContextId());
		// }
		codegenTask.applyTaskOptions(compilePreferences);
		codegenTask.setOutputFolder(targetFolder.getAbsolutePath());
		ValidationFindings findings = new ValidationFindings();
		findings = codegenTask.compileOutput(project);
		return findings;
	}

	private void displayUserMessage(final ValidationFindings findings, final File targetFolder) {
		if (findings.hasFinding(FindingType.ERROR)) {
			LOGGER.warn("Could not compile model, validation errors were found.");
			DialogUserNotifier.openError("Model Error",
					"Could not compile - errors were found. See the Warnings and Errors section.");
		} else if (findings.hasFinding(FindingType.WARNING)) {
			LOGGER.warn("Validation warnings were found during model compilation.");
			DialogUserNotifier.openWarning("Compile with Warnings", "Model compiled with warnings in directory "
					+ targetFolder.getAbsolutePath() + "\n\nSee the Warnings and Errors section.");
		} else { // success
			DialogUserNotifier.openInformation("Compile",
					"Model compiled successfully in directory " + targetFolder.getAbsolutePath());
		}
	}

	private TLModel newTLModel() throws LibraryLoaderException {
		TLModel tlModel = new TLModel();
		tlModel.addListener(new ModelIntegrityChecker());
		tlModel.addListener(objectsListeners);
		return tlModel;
	}

	public <S> void addSourceListener(ValueChangeEventListener<S, ? extends Object> listener) {
		objectsListeners.addSourceListener(listener.getSource(), listener);
	}

	public <S> void removeSourceListener(ValueChangeEventListener<S, ? extends Object> listener) {
		objectsListeners.removeSourceListener(listener.getSource(), listener);
	}

	public <S> void addSourceListener(OwnershipEventListener<S, ? extends Object> listener) {
		objectsListeners.addSourceListener(listener.getSource(), listener);
	}

	public <S> void removeSourceListener(OwnershipEventListener<S, ? extends Object> listener) {
		objectsListeners.removeSourceListener(listener.getSource(), listener);

	}

	private ObjectNotyficationListener objectsListeners = new ObjectNotyficationListener();

	class ObjectNotyficationListener implements ModelEventListener<ModelEvent<Object>, Object> {

		private Map<Object, List<ValueChangeEventListener<Object, Object>>> valueChangeListeners = new HashMap<Object, List<ValueChangeEventListener<Object, Object>>>();
		private Map<Object, List<OwnershipEventListener<Object, Object>>> ownerShipListeners = new HashMap<Object, List<OwnershipEventListener<Object, Object>>>();

		@SuppressWarnings("unchecked")
		public <S> void addSourceListener(S source, ValueChangeEventListener<S, ? extends Object> listener) {
			List<ValueChangeEventListener<Object, Object>> sourceL = valueChangeListeners.get(source);
			if (sourceL == null) {
				sourceL = new LinkedList<ValueChangeEventListener<Object, Object>>();
				valueChangeListeners.put(source, sourceL);
			}
			sourceL.add((ValueChangeEventListener<Object, Object>) listener);
		}

		@SuppressWarnings("unchecked")
		public <S> void addSourceListener(S source, OwnershipEventListener<S, ? extends Object> listener) {
			List<OwnershipEventListener<Object, Object>> sourceL = ownerShipListeners.get(source);
			if (sourceL == null) {
				sourceL = new LinkedList<OwnershipEventListener<Object, Object>>();
				ownerShipListeners.put(source, sourceL);
			}
			sourceL.add((OwnershipEventListener<Object, Object>) listener);
		}

		public <S> void removeSourceListener(S source, ValueChangeEventListener<S, ? extends Object> listener) {
			List<ValueChangeEventListener<Object, Object>> sourceL = valueChangeListeners.get(source);
			if (sourceL != null) {
				sourceL.remove(listener);
			} else {
				for (List<ValueChangeEventListener<Object, Object>> listeners : valueChangeListeners.values()) {
					listeners.remove(listener);
				}
			}
		}

		public <S> void removeSourceListener(S source, OwnershipEventListener<S, ? extends Object> listener) {
			List<OwnershipEventListener<Object, Object>> sourceL = ownerShipListeners.get(source);
			if (sourceL != null) {
				sourceL.remove(listener);
			} else {
				for (List<OwnershipEventListener<Object, Object>> listeners : ownerShipListeners.values()) {
					listeners.remove(listener);
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void processModelEvent(ModelEvent<Object> event) {
			// LOGGER.debug("TL Event: " + event.getType() + " on " + event.getSource());
			ModelEventType type = event.getType();
			Object source = event.getSource();
			if (event instanceof ValueChangeEvent) {
				if (valueChangeListeners.containsKey(source))
					for (ValueChangeEventListener<Object, Object> list : getValueChangeListeners(source)) {
						if (list.supported(type))
							list.processModelEvent((ValueChangeEvent<Object, Object>) event);
					}

			} else if (event instanceof OwnershipEvent) {
				if (ownerShipListeners.containsKey(source))
					for (OwnershipEventListener<Object, Object> list : getOwnershipListeners(source)) {
						if (list.supported(type))
							list.processModelEvent((OwnershipEvent<Object, Object>) event);
					}
			}
		}

		private List<ValueChangeEventListener<Object, Object>> getValueChangeListeners(Object source) {
			return new ArrayList<ValueChangeEventListener<Object, Object>>(valueChangeListeners.get(source));
		}

		private List<OwnershipEventListener<Object, Object>> getOwnershipListeners(Object source) {
			return new ArrayList<OwnershipEventListener<Object, Object>>(ownerShipListeners.get(source));

		}

		@Override
		public Class<ModelEvent<Object>> getEventClass() {
			return null;
		}

		@Override
		public Class<Object> getSourceObjectClass() {
			return null;
		}

	}

	@Override
	public boolean changeToSimple(PropertyNode p) {
		if (p instanceof SimpleAttributeNode)
			return false;
		if (!p.getType().isSimpleAssignable())
			return false;

		ComponentNode owner = (ComponentNode) p.getOwningComponent();
		if (!(owner instanceof ComplexComponentInterface)) {
			return false;
		}

		// Other methods do node and TL level changes. Adding TL model code.
		// Handles both core and VWA object types.
		// 2/3/2015 dmh
		TLModelElement tlPropOwner = p.getParent().getTLModelObject();
		if (tlPropOwner instanceof TLPropertyOwner)
			((TLPropertyOwner) tlPropOwner).removeProperty((TLProperty) p.getTLModelObject());
		else if (tlPropOwner instanceof TLAttributeOwner)
			((TLAttributeOwner) tlPropOwner).removeAttribute((TLAttribute) p.getTLModelObject());
		else
			return false;

		ComplexComponentInterface ci = (ComplexComponentInterface) owner;
		Node simpleProp = ci.getSimpleFacet().getSimpleAttribute();
		simpleProp.setAssignedType(p.getType());
		copyDocumentation(p, simpleProp);
		// TODO also copy the examples and equivalents
		p.unlinkNode();

		return true;
	}

	private void copyDocumentation(Node from, Node to) {
		TLDocumentation fromDoc = from.getDocumentation();
		if (fromDoc != null) {
			to.getModelObject().setDocumentation((TLDocumentation) fromDoc.cloneElement());
		}
		if (from instanceof TLDocumentationOwner && to instanceof TLDocumentationOwner) {
			TLDocumentationOwner toO = (TLDocumentationOwner) to;
			toO.setDocumentation(from.getDocumentation());
		}
	}

	@Override
	public ComponentNode moveSimpleToFacet(Node simpleAttribute, ComponentNode targetFacet) {
		if (!(simpleAttribute instanceof SimpleAttributeNode))
			return null;

		ComponentNode cn = (ComponentNode) simpleAttribute.getOwningComponent();
		if (!(cn instanceof ComplexComponentInterface)) {
			return null;
		}

		ComplexComponentInterface ci = (ComplexComponentInterface) cn;
		TLModelElement tlModel = null;
		if (ci instanceof VWA_Node) {
			String name = NodeNameUtils.stipSimpleSuffix(simpleAttribute.getName());
			tlModel = createTLAttribute(name);
		} else {
			String name = NodeNameUtils.stipSimpleSuffix(simpleAttribute.getName());
			tlModel = createTLProperty(name);
		}
		ComponentNode newProperty = NodeFactory.newComponentMember(targetFacet, tlModel);
		NodeNameUtils.fixName(newProperty);
		newProperty.setAssignedType(simpleAttribute.getType());
		simpleAttribute.setAssignedType((Node) ModelNode.getEmptyNode());
		copyDocumentation(simpleAttribute, newProperty);

		return newProperty;
	}

	private TLModelElement createTLAttribute(String name) {
		TLAttribute atr = new TLAttribute();
		atr.setName(name);
		return atr;
	}

	private TLModelElement createTLProperty(String name) {
		TLProperty atr = new TLProperty();
		atr.setName(name);
		return atr;
	}

}

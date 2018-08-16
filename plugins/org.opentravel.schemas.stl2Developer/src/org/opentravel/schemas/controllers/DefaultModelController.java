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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.opentravel.schemacompiler.ic.ModelIntegrityChecker;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.preferences.CompilerPreferences;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.views.ValidationResultsView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Agnieszka Janowska / Dave Hollander
 * 
 */
public class DefaultModelController extends OtmControllerBase implements ModelController {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelController.class);

	private final LibraryController libraryController;
	private ModelNode modelRoot;
	public static String COMPILER_SUFFIX = "CompilerOutput";

	private String lastCompileDirectory = "";
	private String lastCompileMessage;
	private ValidationFindings lastCompileFindings;

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

	@Override
	public String getLastCompileDirectory() {
		return lastCompileDirectory;
	}

	// public LibraryController getLibraryController() {
	// return libraryController;
	// }

	@Override
	public ModelNode getModel() {
		return modelRoot;
	}

	@Override
	public TLModel getTLModel() {
		return Node.getModelNode().getTLModel();
	}

	// 11/11/2016 - dmh - not used anywhere
	// FIXME - remove from ModelController interface
	@Deprecated
	@Override
	public ModelNode createNewModel() {
		// // LOGGER.debug("Creating new model");
		ModelNode model = null;
		return model;
	}

	/*
	 */
	@Override
	public void saveModel(final INode model) {
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
		LOGGER.error("CLOSE MODEL is NOT IMPLEMENTED.");
	}

	public void syncWithUi(final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				OtmRegistry.getMainController().postStatus(msg);
				// Update the project documentation view
				if (OtmRegistry.getProjectDocView() != null)
					OtmRegistry.getProjectDocView().setFocus();
				DialogUserNotifier.openInformation("Compile Results", msg);
				OtmRegistry.getMainController().refresh();
			}
		});
	}

	@Override
	public void compileInBackground(final ProjectNode project) {
		if (Display.getCurrent() == null)
			compile(project); // not in UI Thread
		else {
			// run in a background job
			mc.postStatus("Compiling " + project);
			Job job = new Job("Compiling " + project + " with " + getUserLibraryCount(project) + " libraries.") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Compiling Project: " + project, 2);
					monitor.worked(1);
					String status = compile(project, monitor);
					monitor.done();
					syncWithUi(status);
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

	private String getUserLibraryCount(ProjectNode project) {
		// Report to user the project item count
		int itemCount = 0;
		for (ProjectItem item : project.getTLProject().getProjectItems())
			if (item.getContent() instanceof TLLibrary)
				itemCount++;
		return Integer.toString(itemCount);
	}

	// TODO - implement
	// I have also added a static method that will allow you to generate documentation in a directory of your choosing.
	// Just use the following lines of code. The generator will perform a validation check, but you can leave the
	// ‘findings’ parameter null if you don’t care about them.
	//
	// ValidationFindings findings = new ValidationFindings();
	// File indexHtml = DocumentationCompileTask.compileDocumentation( model, outputFolder, findings );

	private void compile(ProjectNode project) {
		mc.postStatus(compile(project, null));
		final ValidationResultsView view = OtmRegistry.getValidationResultsView();
		if (view != null)
			view.setFindings(lastCompileFindings, project);
	}

	public String compile(ProjectNode project, IProgressMonitor monitor) {
		if (project == null)
			return "Null project";

		// Get a directory to compile into.
		String directoryName = project.getTLProject().getProjectFile().getAbsolutePath();
		directoryName = directoryName.substring(0, directoryName.length() - 4); // strip .otp
		directoryName += "_" + COMPILER_SUFFIX;
		final File targetFolder = new File(directoryName);
		if (!targetFolder.exists()) {
			if (!targetFolder.mkdirs()) {
				LOGGER.warn("Could not make directory: " + targetFolder);
				return "Error. Could not make directory " + targetFolder.getPath() + " for the compiled output.";
			}
		}

		// Use codegen task to do the compile.
		lastCompileFindings = new ValidationFindings();
		final CompilerPreferences compilePreferences = new CompilerPreferences(
				CompilerPreferences.loadPreferenceStore());
		final CompileAllCompilerTask codegenTask = new CompileAllCompilerTask();
		codegenTask.applyTaskOptions(compilePreferences);
		codegenTask.setOutputFolder(targetFolder.getAbsolutePath());
		try {
			lastCompileFindings = codegenTask.compileOutput(project.getTLProject());
		} catch (final SchemaCompilerException e) {
			return "Error: Could not compile - " + e.getMessage();
		} catch (final Exception e) {
			return "Error: Could not compile , unknown error occurred - " + e.getMessage();
		}

		lastCompileDirectory = targetFolder.getAbsolutePath();
		lastCompileMessage = getUserLibraryCount(project) + " project items compiled into directory "
				+ lastCompileDirectory;
		return lastCompileMessage;
	}

	private TLModel newTLModel() throws LibraryLoaderException {
		TLModel tlModel = null;
		try {
			tlModel = new TLModel();
		} catch (Exception e) {
			LOGGER.debug("Exception creating new model: " + e.getLocalizedMessage());
		}
		tlModel.addListener(new ModelIntegrityChecker());
		// tlModel.addListener(objectsListeners);
		return tlModel;
	}

	@Override
	@Deprecated
	public boolean changeToSimple(PropertyNode p) {
		if (p instanceof SimpleAttributeFacadeNode)
			return false;
		if (p instanceof TypedPropertyNode)
			if (!(((TypedPropertyNode) p).getAssignedType() instanceof TypeProvider)
					|| !p.getType().isSimpleAssignable())
				return false;

		ComponentNode owner = (ComponentNode) p.getOwningComponent();
		SimpleAttributeOwner simpleAttr = null;
		TypeProvider result = null;
		if (owner instanceof CoreObjectNode && p instanceof TypedPropertyNode)
			result = ((CoreObjectNode) owner).setAssignedType(((TypedPropertyNode) p).getAssignedType());
		else if (owner instanceof VWA_Node && p instanceof TypedPropertyNode)
			result = ((VWA_Node) owner).setAssignedType(((TypedPropertyNode) p).getAssignedType());
		if (result == null)
			return false; // failed to make assignements
		// TODO copy the examples and equivalents
		// FIXME copyDocumentation(p, simpleFacet);

		// Remove from current parent
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

		// Add to simple attribute as type
		// ComponentNode ci = owner;
		// Node simpleProp = null;
		// if (ci instanceof VWA_SimpleFacetFacadeNode)
		// simpleProp = ((VWA_SimpleFacetFacadeNode) ci.getFacet_Simple()).getSimpleAttribute();
		// else if (ci instanceof CoreSimpleFacetNode)
		// simpleProp = ((CoreSimpleFacetNode) ci.getFacet_Simple()).getSimpleAttribute();
		// ((TypeUser) simpleFacet).setAssignedType((TypeProvider) p.getType());

		p.getParent().getChildrenHandler().clear();
		// p.unlinkNode();
		// TEST - FIXME
		return true;
	}

	private void copyDocumentation(Node from, Node to) {
		TLDocumentation fromDoc = from.getDocumentation();
		if (fromDoc != null) {
			((TLDocumentationOwner) to.getTLModelObject()).setDocumentation((TLDocumentation) fromDoc.cloneElement());
		}
		if (from instanceof TLDocumentationOwner && to instanceof TLDocumentationOwner) {
			TLDocumentationOwner toO = (TLDocumentationOwner) to;
			toO.setDocumentation(from.getDocumentation());
		}
	}

	// TODO - why is this here and none of the other methods needed in wizard?
	@Override
	public ComponentNode moveSimpleToFacet(Node simpleAttribute, ComponentNode targetFacet) {
		if (!(simpleAttribute instanceof SimpleAttributeFacadeNode))
			return null;

		ComponentNode cn = (ComponentNode) simpleAttribute.getOwningComponent();
		if (!(cn instanceof FacetOwner)) {
			return null;
		}

		FacetOwner ci = (FacetOwner) cn;
		TLModelElement tlModel = null;
		if (ci instanceof VWA_Node) {
			String name = NodeNameUtils.stipSimpleSuffix(simpleAttribute.getName());
			tlModel = createTLAttribute(name);
		} else {
			String name = NodeNameUtils.stipSimpleSuffix(simpleAttribute.getName());
			tlModel = createTLProperty(name);
		}
		ComponentNode newProperty = NodeFactory.newChild(targetFacet, tlModel);
		NodeNameUtils.fixName(newProperty);
		((TypeUser) newProperty).setAssignedType((TypeProvider) simpleAttribute.getType());
		((TypeUser) simpleAttribute).setAssignedType((TypeProvider) ModelNode.getEmptyNode());
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

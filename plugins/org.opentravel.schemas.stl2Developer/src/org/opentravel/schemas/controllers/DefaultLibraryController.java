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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.FileDialog;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.visitor.DependencyNavigator;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.FileDialogs;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.views.ValidationResultsView;
import org.opentravel.schemas.views.decoration.LibraryDecorator;
import org.opentravel.schemas.wizards.GlobalLocalCancelDialog;
import org.opentravel.schemas.wizards.GlobalLocalCancelDialog.GlobalDialogResult;
import org.opentravel.schemas.wizards.NewLibraryValidator;
import org.opentravel.schemas.wizards.NewLibraryWizard;
import org.opentravel.schemas.wizards.NewLibraryWizardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements interactions the user has with the Library View by acting upon the library nodes and
 * model node.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class DefaultLibraryController extends OtmControllerBase implements LibraryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLibraryController.class);

    public DefaultLibraryController(final MainController mainController) {
        super(mainController);
    }

    @Override
    public LibraryNode createLibrary() {
        LibraryNode ln = null;
        ProjectNode pn = null;

        final OtmView view = OtmRegistry.getNavigatorView();
        if (view == null)
            return null;

        // Find the active project
        Node selected = null;
        if (!view.getSelectedNodes().isEmpty())
            selected = view.getSelectedNodes().get(0);
        if (selected == null)
            pn = mc.getProjectController().getDefaultProject();
        else if (selected instanceof ProjectNode)
            pn = (ProjectNode) selected;
        else if (selected.getLibrary() == null)
            pn = mc.getProjectController().getDefaultProject();
        else
            pn = selected.getLibrary().getProject();

        final NewLibraryWizard wizard = new NewLibraryWizard(pn);
        LibraryNode libNode = wizard.getLibraryNode();
        wizard.setValidator(new NewLibraryValidator(libNode, pn.getNamespace()));
        wizard.run(OtmRegistry.getActiveShell());
        if (!wizard.wasCanceled()) {
            // Create an OTM file, then add that file to the project.
            ln = createNewLibraryFromPrototype(libNode);
            // remove prototype library
            libNode.delete();
            // pn.getChildren().remove(libNode);
            // libNode.setParent(null);
            if (ln != null) {
                if (saveLibrary(ln, true)) {
                    getView().refreshAllViews();
                    view.select(ln);
                } else {
                    ln.delete();
                }
            }
        }
        return ln;
    }

    @Override
    public void changeNamespace(final LibraryNode library, final String newNS) {
        if (library == null || newNS == null || newNS.isEmpty())
            throw new IllegalArgumentException("Null or empty namespace change.");
        LOGGER.debug("Changing namespace to " + newNS);

        // Is the library shared?
        final List<LibraryNode> libs = getLibrariesWithNamespace(newNS);
        if (libs.size() < 1) {
            library.getNsHandler().renameInProject(library.getNamespace(), newNS);
        } else {
            switch (postChangeMessage(libs)) {
                case GLOBAL:
                    library.getNsHandler().rename(library.getNamespace(), newNS);
                    break;
                case LOCAL:
                    library.getNsHandler().renameInProject(library.getNamespace(), newNS);
                    break;
            }
        }
    }

    @Override
    public void changeNamespaceExtension(LibraryNode lib, String extension) {
        // TODO Auto-generated method stub
        LOGGER.debug("Changing namespace extension to: " + extension);
    }

    private GlobalDialogResult postChangeMessage(List<LibraryNode> libs) {
        final StringBuilder message = new StringBuilder(
                "The namespace you want to change is shared by more than one library (");
        for (final LibraryNode lib : libs) {
            message.append(lib.getName()).append(", ");
        }
        message.delete(message.length() - 2, message.length());
        message.append("). The namespace can be changed in the following manners:")
                .append("\n Global - namespace is changed globally and all the libraries within the namespace will be affected "
                        + "(prefix will stay the same and will now point to the new namespace)")
                .append("\n Local - namespace will be changed only for the local library which trigerred the change "
                        + "(if the new namespace is not yet registered within the model, new 'Undefined' prefix will be assinged to it; "
                        + "otherwise the namespace is assigned already existing prefix).")
                .append("\n Cancel - leave the namespace unchanged.");
        final GlobalLocalCancelDialog nsChangeDialog = new GlobalLocalCancelDialog(
                OtmRegistry.getActiveShell(), message.toString());
        nsChangeDialog.open();
        return nsChangeDialog.getResult();
    }

    /**
     * Using the name and path, create a .otm file. The new library node is not added to a project
     * (parent is not set) and namespaces are not registered. After is this complete, a project item
     * representing this library can be added to the project node.
     * 
     * @param model
     * @param libNode
     * @return - a new library node with links to the TL Abstract Library.
     */
    public LibraryNode createNewLibraryFromPrototype(final LibraryNode libNode) {
        final ProjectNode pn = (ProjectNode) libNode.getParent();
        final ProjectController pc = mc.getProjectController();

        final TLLibrary tlLib = new TLLibrary();
        tlLib.setStatus(TLLibraryStatus.DRAFT);
        tlLib.setPrefix(libNode.getNamePrefix());
        tlLib.setName(libNode.getName());
        tlLib.setComments(libNode.getComments());
        tlLib.setLibraryUrl(libNode.getTLaLib().getLibraryUrl());
        tlLib.setNamespace(libNode.getNamespace());

        return pc.add(pn, tlLib);
    }

    /**
     * Find the nearest parent that can contain a library then open the existing library and add it
     * to that parent. Parent must be a project node.
     */
    @Override
    public void openLibrary(INode node) {
        ProjectNode project = null;
        if (node == null)
            node = mc.getProjectController().getDefaultProject();
        while (!(node instanceof ProjectNode)) {
            node = node.getParent();
            if (node == null)
                node = mc.getProjectController().getDefaultProject();
        }
        project = (ProjectNode) node;
        openLibrary(project);
        mc.refresh();
        LOGGER.debug("Opening library for project " + project);
    }

    @Override
    public void openLibrary(ProjectNode pn) {
        LOGGER.debug("Opening library");
        if (pn == null)
            return;

        // Prompt the user to select one or more files
        final FileDialog fd = FileDialogs.postFilesDialog();

        List<File> filesToOpen = new ArrayList<File>();
        for (String fileName : fd.getFileNames()) {
            fileName = fd.getFilterPath() + File.separator + fileName;
            if (fileName != null) {
                filesToOpen.add(new File(fileName));
            }
        }

        pn.add(filesToOpen);
        for (LibraryNode ln : pn.getLibraries())
            ((DefaultProjectController) mc.getProjectController()).fixElementNames(ln);

        mc.refresh();
        mc.getProjectController().save(pn);
    }

    @Override
    public void closeLibrary(final LibraryNode library) {
        closeLibraries(Arrays.asList(library));
    }

    @Override
    public void closeLibraries(final List<LibraryNode> libraries) {
        if (libraries == null || libraries.isEmpty()) {
            return;
        }
        List<LibraryNode> libs = new ArrayList<LibraryNode>(libraries);
        for (final LibraryNode lib : libs) {
            lib.close();
        }

        final ValidationResultsView view = OtmRegistry.getValidationResultsView();
        if (view != null) {
            view.clearFindings();
        }
        mc.clearSelection();
        mc.refresh();
    }

    @Override
    public boolean saveLibrary(final LibraryNode library, boolean quiet) {
        if (library != null) {
            LOGGER.debug("Saving library " + library.getName());
            return saveLibraries(Arrays.asList(library), quiet);
        }
        return false;
    }

    private List<TLLibrary> getEditableUsersLibraraies(List<LibraryNode> libraries) {
        final List<TLLibrary> toSave = new ArrayList<TLLibrary>();
        for (final LibraryNode lib : libraries) {
            final AbstractLibrary tlLib = lib.getTLaLib();
            if (lib.isEditable() && tlLib instanceof TLLibrary) {
                toSave.add((TLLibrary) tlLib);
            }
        }
        return toSave;
    }

    @Override
    public boolean saveLibraries(final List<LibraryNode> libraries, boolean quiet) {
        final List<TLLibrary> toSave = getEditableUsersLibraraies(libraries);
        if (toSave.isEmpty()) {
            DialogUserNotifier.openInformation("Warning",
                    Messages.getString("action.saveAll.noUserDefied"));
            LOGGER.debug("No user defined libraries to save");
            return false;
        }
        final LibraryModelSaver lms = new LibraryModelSaver();
        final StringBuilder successfulSaves = new StringBuilder();
        final StringBuilder errorSaves = new StringBuilder();
        final ValidationFindings findings = new ValidationFindings();
        for (final TLLibrary library : toSave) {
            final String libraryName = library.getName();
            final URL libraryUrl = library.getLibraryUrl();
            try {
                LOGGER.debug("Saving library: " + libraryName + " " + libraryUrl);
                findings.addAll(lms.saveLibrary(library));
                if (!quiet)
                    successfulSaves.append("\n").append(libraryName).append(" (")
                            .append(libraryUrl).append(")");
            } catch (final LibrarySaveException e) {
                final Throwable t = e.getCause();
                errorSaves.append("\n").append(libraryName).append(" (").append(libraryUrl)
                        .append(")").append(" - ").append(e.getMessage());
                if (t != null && t.getMessage() != null) {
                    errorSaves.append(" (").append(t.getMessage()).append(")");
                }
            }
        }
        String message = getUserMessage(successfulSaves, errorSaves, findings);
        if (!message.isEmpty()) {
            DialogUserNotifier.openInformation("Save Results", message.toString());
        }
        final ValidationResultsView vView = OtmRegistry.getValidationResultsView();
        if (vView != null) {
            vView.setFindings(findings, Node.getModelNode());
        }
        return errorSaves.length() == 0;
    }

    private String getUserMessage(StringBuilder successfulSaves, StringBuilder errorSaves,
            ValidationFindings findings) {
        final StringBuilder userMessage = new StringBuilder();
        if (successfulSaves.length() > 0) {
            userMessage.append("Successfully saved:").append(successfulSaves).append("\n\n");
        }
        if (errorSaves.length() > 0) {
            userMessage.append("Failed to save:").append(errorSaves).append("\n\n")
                    .append("You may need to use the .bak file to restore your work");
        }
        if (!findings.isEmpty()) {
            userMessage
                    .append("WARNING: Some validation errors or warnings occurred. "
                            + "You may not be able to reopen the library once you close it before fixing those issues. "
                            + "Please refer to Warnings and Errors section to review them.")
                    .append("\n\n");
        }
        return userMessage.toString();
    }

    @Override
    public void remove(final Collection<? extends Node> libraries) {
        Set<ProjectNode> projectsToSave = new HashSet<ProjectNode>();
        for (LibraryNode ln : getLibrariesToClose(libraries)) {
            if (ln == null || ln.getParent() == null) {
                LOGGER.error("ILLEGAL State - library " + ln + " parent is null.");
            } else {
                projectsToSave.add(ln.getProject());
                ln.close(); // Don't use delete because recurses and deletes children from the
                            // library.

            }
        }
        for (ProjectNode project : projectsToSave) {
            mc.getProjectController().save(project);
            mc.refresh(project); // give user feedback
        }
        mc.clearSelection();
    }

    private Collection<LibraryNode> getLibrariesToClose(Collection<? extends Node> newSelection) {
        Set<LibraryNode> ret = new HashSet<LibraryNode>();
        for (Node n : newSelection) {
            if (n instanceof LibraryChainNode) {
                ret.addAll(getLibrariesToClose(n.getLibraries()));
            } else if (n instanceof LibraryNode && !n.isBuiltIn()) {
                ret.add((LibraryNode) n);
            } else if (n instanceof ProjectNode) {
                ret.addAll(getLibrariesToClose(n.getChildren()));
            }
        }
        return ret;
    }

    @Override
    public boolean saveAllLibraries(boolean quiet) {
        return saveLibraries(Node.getAllLibraries(), quiet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.LibraryController#getUserLibraries()
     */
    @Override
    public List<LibraryNode> getUserLibraries() {
        List<LibraryNode> libs = new ArrayList<LibraryNode>();
        for (INode lib : Node.getAllLibraries()) {
            if (lib instanceof LibraryNode) {
                if (((LibraryNode) lib).getTLaLib() instanceof TLLibrary)
                    libs.add((LibraryNode) lib);
            }
        }
        return libs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.LibraryController#getLibrariesWithNamespace()
     */
    @Override
    public List<LibraryNode> getLibrariesWithNamespace(final String namespace) {
        final List<LibraryNode> toReturn = new ArrayList<LibraryNode>();
        if (namespace != null) {
            for (final INode n : Node.getAllLibraries()) {
                if (n instanceof LibraryNode && namespace.equals(n.getNamespace())) {
                    toReturn.add((LibraryNode) n);
                }
            }
        }
        return toReturn;
    }

    // TODO - remove open from the interface.
    // @Override
    // public List<AbstractLibrary> open(final String fileName) {
    // LOGGER.info("Not Implemented - libraryController.open is not implemented. Use project controller.");
    // return null;
    // }

    public void validateLibrary(final LibraryNode library) {
        LOGGER.debug("Validating library " + library);

        assert library != null;

        final ValidationResultsView view = OtmRegistry.getValidationResultsView();
        if (view != null) {
            view.setFindings(
                    TLModelCompileValidator.validateModelElement(library.getTLModelObject()),
                    library);
        }
    }

    @Override
    public void updateLibraryStatus() {
        for (LibraryNode ln : Node.getAllUserLibraries()) {
            ln.updateLibraryStatus();
        }
        mc.refresh();
    }

    @Override
    public String getLibraryStatus(LibraryNode library) {
        // TODO: use i18n for text
        if (library.getTLaLib() instanceof XSDLibrary)
            return "XSD";
        ProjectItem pi = library.getProjectItem();
        // During adding library there is some kind of duplication. Until resolving unnecessary
        // duplication check this
        if (pi == null) {
            return "NULL Status";
        }
        if (library.getTLaLib() instanceof BuiltInLibrary) {
            return "Built-in";
        }
        return LibraryDecorator.translateStatusState(library.getStatus(), pi.getState(),
                pi.getLockedByUser(), library.isEditable());
    }

    private LibraryNode findLibrary(AbstractLibrary tlLib) {
        for (LibraryNode userLib : Node.getAllLibraries()) {
            if (userLib.getTLaLib() == tlLib) {
                return userLib;
            }
        }
        return null;
    }

    @Override
    public List<LibraryNode> convertXSD2OTM(LibraryNode xsdLibrary, boolean withDependecies) {
        if (!xsdLibrary.isXSDSchema())
            throw new IllegalArgumentException("");

        if (withDependecies) {
            List<XSDLibrary> xsdDeps = findDependecies(xsdLibrary.getTLaLib());
            for (XSDLibrary xsdDep : xsdDeps) {
                LibraryNode nodeLib = findLibrary(xsdDep);
                if (nodeLib != null) {
                    convertXSD2OTM(nodeLib, withDependecies);
                }
            }
        }

        URL otmLibraryURL = createLibURL(xsdLibrary);
        String otmLibraryName = "OTM" + xsdLibrary.getName();
        LibraryNode newLib = createLibrary(otmLibraryName, xsdLibrary.getNamePrefix(),
                otmLibraryURL, xsdLibrary.getNamespace(), xsdLibrary.getProject());

        if (newLib != null) {
            // make it temporary editable to import types
            newLib.setEditable(true);
            newLib.importNodes(xsdLibrary.getDescendentsNamedTypes(), false);
            newLib.updateLibraryStatus();
            return Collections.singletonList(newLib);
        }
        return Collections.emptyList();
    }

    private List<XSDLibrary> findDependecies(final AbstractLibrary xsdLibrary) {
        final LinkedList<XSDLibrary> dependecis = new LinkedList<XSDLibrary>();
        DependencyNavigator.navigate(xsdLibrary, new ModelElementVisitorAdapter() {

            @Override
            public boolean visitLegacySchemaLibrary(XSDLibrary library) {
                if (library.getOwningModel().getBuiltInLibraries().contains(library)) {
                    return false;
                }
                if (library != xsdLibrary && !dependecis.contains(library)) {
                    dependecis.addLast(library);
                }
                return true;
            }
        });
        return dependecis;
    }

    private URL createLibURL(LibraryNode xsdLibrary) {
        URL xsd = xsdLibrary.getTLaLib().getLibraryUrl();
        try {
            return new URL(xsd.toString() + "." + NewLibraryWizardPage.DEFAULT_EXTENSION);
        } catch (MalformedURLException e) {
            // TODO: should not happen
        }
        return xsd;
    }

    private LibraryNode createLibrary(String name, String prefix, URL url, String namespace,
            ProjectNode pn) {
        final TLLibrary tlLib = new TLLibrary();
        tlLib.setStatus(TLLibraryStatus.DRAFT);
        tlLib.setPrefix(prefix);
        tlLib.setName(name);
        tlLib.setLibraryUrl(url);
        tlLib.setNamespace(namespace);

        final ProjectController pc = mc.getProjectController();
        return pc.add(pn, tlLib);
    }
}

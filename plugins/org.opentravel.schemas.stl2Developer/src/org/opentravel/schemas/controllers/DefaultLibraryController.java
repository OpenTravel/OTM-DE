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

import org.eclipse.swt.widgets.FileDialog;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.visitor.DependencyNavigator;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemas.actions.NewLibraryAction;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.FileDialogs;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.views.ValidationResultsView;
import org.opentravel.schemas.views.decoration.LibraryDecorator;
import org.opentravel.schemas.wizards.NewLibraryWizard2;
import org.opentravel.schemas.wizards.NewLibraryWizard2.LibraryMetaData;
import org.opentravel.schemas.wizards.NewLibraryWizardPage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Implements interactions the user has with the Library View by acting upon the library nodes and model node.
 *
 * "the controller accepts input and converts it to commands for the model or view."
 * 
 * @author Agnieszka Janowska / Dave Hollander
 * 
 */
public class DefaultLibraryController extends OtmControllerBase implements LibraryController {
    // private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLibraryController.class);

    public DefaultLibraryController(final MainController mainController) {
        super( mainController );
    }

    /**
     * Called from the {@link NewLibraryAction#run()}
     * 
     * Project taken from currently selected node or else the default project.
     * 
     * Prompts user using the New Library Wizard. The wizard creates a library then uses the createFromPrototype()
     * method.
     * 
     * @return newly created library or null if wizard canceled or not running in gui.
     */
    @Override
    public LibraryNavNode createLibrary() {
        LibraryNavNode lnn = null;
        ProjectNode pn = null;

        final OtmView view = OtmRegistry.getNavigatorView();
        if (view == null)
            return null;
        // Make sure this is running from the workbench before starting wizard.
        if (!OtmRegistry.getMainWindow().hasDisplay())
            return null;

        // Find the active project
        Node selected = null;
        if (!view.getSelectedNodes().isEmpty())
            selected = view.getSelectedNodes().get( 0 );
        if (selected == null)
            pn = mc.getProjectController().getDefaultProject();
        else if (selected instanceof ProjectNode)
            pn = (ProjectNode) selected;
        else if (selected.getLibrary() == null)
            pn = mc.getProjectController().getDefaultProject();
        else
            pn = selected.getLibrary().getProject();

        // Run wizard
        final NewLibraryWizard2 wizard = new NewLibraryWizard2( pn );
        wizard.run( OtmRegistry.getActiveShell() );
        if (!wizard.wasCanceled()) {
            // Create an OTM file, then add that file to the project.
            LibraryMetaData metaData = wizard.getLibraryMetaData();
            lnn = createNewLibraryFromMetadata( metaData, pn );

            if (lnn != null) {
                if (save( lnn )) {
                    getView().refreshAllViews();
                    view.select( lnn );
                } else {
                    // LOGGER.debug("Could not save file.");
                    DialogUserNotifier.openWarning( "File Save", "Could not save library." );
                    lnn.delete();
                }
            }
        }

        return lnn;
    }

    /**
     * Create an OTM library file.
     * 
     * @param library metadata - containing the path, ns, prefix, name and comments
     * @param project
     * @return - a new library node with links to the TL Abstract Library.
     */
    public LibraryNavNode createNewLibraryFromMetadata(final LibraryMetaData metaData, final ProjectNode pn) {
        final ProjectController pc = mc.getProjectController();

        final File file = new File( metaData.getPath() );
        final URL fileURL = URLUtils.toURL( file );

        final TLLibrary tlLib = new TLLibrary();
        tlLib.setStatus( TLLibraryStatus.DRAFT );
        tlLib.setPrefix( metaData.getNsPrefix() );
        tlLib.setName( metaData.getName() );
        tlLib.setComments( metaData.getComments() );
        tlLib.setLibraryUrl( fileURL );
        tlLib.setNamespace( metaData.getNamespace() );

        return pc.add( pn, tlLib );
    }

    @Override
    public void openLibrary(INode node) {
        // LOGGER.debug("Opening library");

        // Determine what project to use
        ProjectNode pn = null;
        if (node == null)
            node = mc.getProjectController().getDefaultProject();
        while (!(node instanceof ProjectNode)) {
            node = node.getParent();
            if (node == null)
                node = mc.getProjectController().getDefaultProject();
        }
        pn = (ProjectNode) node;

        // Get the List of files from the user
        List<File> files = openLibraryDialog();
        if (files == null || files.isEmpty())
            return;

        // Open files using the Project Node
        pn.add( files );

        new TypeResolver().resolveTypes();

        mc.getProjectController().save( pn );

        mc.refresh();
        // LOGGER.debug("Opened library for project " + pn);
    }

    private List<File> openLibraryDialog() {
        // Prompt the user to select one or more files
        final FileDialog fd = FileDialogs.postFilesDialog();

        List<File> filesToOpen = new ArrayList<>();
        for (String fileName : fd.getFileNames()) {
            fileName = fd.getFilterPath() + File.separator + fileName;
            if (fileName != null) {
                filesToOpen.add( new File( fileName ) );
            }
        }

        return filesToOpen;
    }

    public boolean save(final LibraryNavNode lnn) {
        if (lnn.getLibrary() instanceof LibraryNode)
            return saveLibrary( lnn.getLibrary(), true );
        return false;
    }

    // SaveSelectedLibraryAsAction
    @Override
    public boolean saveLibrary(final LibraryNode library, boolean quiet) {
        if (library != null) {
            // LOGGER.debug("Saving library " + library.getName());
            return saveLibraries( Arrays.asList( library ), quiet );
        }
        return false;
    }

    // TODO - should use Library manager
    private Set<TLLibrary> getEditableUsersLibraraies(List<LibraryNode> libraries) {
        final Set<TLLibrary> saveSet = new HashSet<>();
        // final List<TLLibrary> toSave = new ArrayList<TLLibrary>();
        for (final LibraryNode lib : libraries) {
            if (lib == null)
                continue;
            final AbstractLibrary tlLib = lib.getTLaLib();
            if (lib.isEditable() && tlLib instanceof TLLibrary) {
                saveSet.add( (TLLibrary) tlLib );
                // toSave.add((TLLibrary) tlLib);
            }
        }
        return saveSet;
    }

    // SaveLibraryHandler
    // SaveSelectedLibrariesAction
    @Override
    public boolean saveLibraries(final List<LibraryNode> libraries, boolean quiet) {
        final Set<TLLibrary> toSave = getEditableUsersLibraraies( libraries );
        if (toSave.isEmpty()) {
            DialogUserNotifier.openInformationMsg( "action.warning", "action.saveAll.noUserDefied" );
            // LOGGER.debug("No editable libraries to save");
            return false;
        }

        // // Post user warning
        // if (!DialogUserNotifier.openConfirm("Warning", Messages.getString("action.saveAll.version16")))
        // return false;
        // assert false; // Don't let junits save

        final LibraryModelSaver lms = new LibraryModelSaver();
        final StringBuilder successfulSaves = new StringBuilder();
        final StringBuilder errorSaves = new StringBuilder();
        final ValidationFindings findings = new ValidationFindings();
        for (final TLLibrary library : toSave) {
            final String libraryName = library.getName();
            final URL libraryUrl = library.getLibraryUrl();
            try {
                // LOGGER.debug("Saving library: " + libraryName + " " + libraryUrl);
                findings.addAll( lms.saveLibrary( library ) );
                if (!quiet)
                    successfulSaves.append( "\n" ).append( libraryName ).append( " (" ).append( libraryUrl )
                        .append( ")" );
            } catch (final LibrarySaveException e) {
                final Throwable t = e.getCause();
                errorSaves.append( "\n" ).append( libraryName ).append( " (" ).append( libraryUrl ).append( ")" )
                    .append( " - " ).append( e.getMessage() );
                if (t != null && t.getMessage() != null) {
                    errorSaves.append( " (" ).append( t.getMessage() ).append( ")" );
                }
            }
        }
        String message = getUserMessage( successfulSaves, errorSaves, findings );
        if (!message.isEmpty()) {
            DialogUserNotifier.openInformation( "Save Results", message.toString() );
        }
        final ValidationResultsView vView = OtmRegistry.getValidationResultsView();
        if (vView != null) {
            vView.setFindings( findings, Node.getModelNode() );
        }
        return errorSaves.length() == 0;
    }

    private String getUserMessage(StringBuilder successfulSaves, StringBuilder errorSaves,
        ValidationFindings findings) {
        final StringBuilder userMessage = new StringBuilder();
        if (successfulSaves.length() > 0) {
            userMessage.append( "Successfully saved:" ).append( successfulSaves ).append( "\n\n" );
        }
        if (errorSaves.length() > 0) {
            userMessage.append( "Failed to save:" ).append( errorSaves ).append( "\n\n" )
                .append( "You may need to use the .bak file to restore your work" );
        }
        if (!findings.isEmpty()) {
            userMessage.append( "WARNING: Some validation errors or warnings occurred. "
                + "You may not be able to reopen the library once you close it before fixing those issues. "
                + "Please refer to Warnings and Errors section to review them." ).append( "\n\n" );
        }
        return userMessage.toString();
    }

    // SaveLibrariesHandler
    @Override
    public boolean saveAllLibraries(boolean quiet) {
        return saveLibraries( Node.getAllLibraries(), quiet );
    }

    // org.opentravel.schemas.preferences.GeneralPreferencePage.performOk()
    @Override
    public void updateLibraryStatus() {
        for (LibraryNode ln : Node.getAllUserLibraries()) {
            ln.updateLibraryStatus();
        }
        mc.refresh();
    }

    // org.opentravel.schemas.views.decoration.LibraryDecorator.getLibraryStatus(LibraryNode)
    @Override
    public String getLibraryStatus(LibraryNode library) {
        // TODO: use i18n for text
        if (library == null || library.getTLModelObject() == null)
            return "NULL Status";

        if (library.getTLModelObject() instanceof XSDLibrary)
            return "XSD";

        ProjectItem pi = library.getProjectItem();
        // During adding library there is some kind of duplication. Until resolving unnecessary
        // duplication check this
        if (pi == null)
            return "NULL Status";

        if (library.getTLModelObject() instanceof BuiltInLibrary)
            return "Built-in";

        return LibraryDecorator.translateStatusState( library.getStatus(), pi.getState(), pi.getLockedByUser(),
            library.isEditable(), library.isMinorVersion() );
    }

    // org.opentravel.schemas.actions.XSD2OTMAction.convertLibraryToOTM()
    @Override
    public List<LibraryNode> convertXSD2OTM(LibraryNode xsdLibrary, boolean withDependecies) {
        HashMap<Node,Node> sourceToNewMap = new HashMap<>();
        List<LibraryNode> newLibs =
            convertXSD2OTM( xsdLibrary, withDependecies, new HashSet<LibraryNode>(), sourceToNewMap );

        // Change type users to use the imported nodes.
        for (final Entry<Node,Node> entry : sourceToNewMap.entrySet()) {
            for (LibraryNode scope : newLibs) {
                final Node sourceNode = entry.getKey();
                if (entry.getValue() instanceof TypeProvider)
                    sourceNode.replaceTypesWith( (TypeProvider) entry.getValue(), scope );
            }
        }
        return newLibs;
    }

    // TODO - make private
    public List<LibraryNode> convertXSD2OTM(LibraryNode xsdLibrary, boolean withDependecies, Set<LibraryNode> visited,
        Map<Node,Node> sourceToNewMap) {
        if (!xsdLibrary.isXSDSchema())
            throw new IllegalArgumentException( "" );
        if (visited.contains( xsdLibrary ))
            return Collections.emptyList();

        // prevent cycling referance
        visited.add( xsdLibrary );

        List<LibraryNode> converted = new ArrayList<>();
        if (withDependecies) {
            List<XSDLibrary> xsdDeps = findDependecies( xsdLibrary.getTLaLib() );
            for (XSDLibrary xsdDep : xsdDeps) {
                LibraryNode nodeLib = findLibrary( xsdDep );
                if (nodeLib != null) {
                    converted.addAll( convertXSD2OTM( nodeLib, withDependecies, visited, sourceToNewMap ) );
                }
            }
        }

        URL otmLibraryURL = createLibURL( xsdLibrary );
        String otmLibraryName = "OTM_" + xsdLibrary.getName();
        LibraryNavNode lnn = createLibrary( otmLibraryName, xsdLibrary.getPrefix(), otmLibraryURL,
            xsdLibrary.getNamespace(), xsdLibrary.getProject() );

        LibraryNode newLib = lnn.getLibrary();
        if (newLib != null) {
            // make it temporary editable to import types
            newLib.setEditable( true );
            sourceToNewMap.putAll( newLib.importNodes( xsdLibrary.getDescendentsNamedTypes() ) );
            newLib.updateLibraryStatus();
            converted.add( newLib );
        }
        return converted;
    }

    private List<XSDLibrary> findDependecies(final AbstractLibrary xsdLibrary) {
        final LinkedList<XSDLibrary> dependecis = new LinkedList<>();
        DependencyNavigator.navigate( xsdLibrary, new ModelElementVisitorAdapter() {

            @Override
            public boolean visitLegacySchemaLibrary(XSDLibrary library) {
                if (library.getOwningModel().getBuiltInLibraries().contains( library )) {
                    return false;
                }
                if (library != xsdLibrary && !dependecis.contains( library )) {
                    dependecis.addLast( library );
                }
                return true;
            }
        } );
        return dependecis;
    }

    private LibraryNode findLibrary(AbstractLibrary tlLib) {
        for (LibraryNode userLib : Node.getAllLibraries()) {
            if (userLib.getTLaLib() == tlLib) {
                return userLib;
            }
        }
        return null;
    }

    private URL createLibURL(LibraryNode xsdLibrary) {
        URL xsd = xsdLibrary.getTLaLib().getLibraryUrl();
        try {
            return new URL( xsd.toString() + "." + NewLibraryWizardPage.DEFAULT_EXTENSION );
        } catch (MalformedURLException e) {
            // should not happen, only adding extension
        }
        return xsd;
    }

    /**
     * {@link org.opentravel.schemas.controllers.DefaultLibraryController#convertXSD2OTM(LibraryNode, boolean,
     * Set<LibraryNode>, Map<Node, Node>)}
     */
    private LibraryNavNode createLibrary(String name, String prefix, URL url, String namespace, ProjectNode pn) {
        final TLLibrary tlLib = new TLLibrary();
        tlLib.setStatus( TLLibraryStatus.DRAFT );
        tlLib.setPrefix( prefix );
        tlLib.setName( name );
        tlLib.setLibraryUrl( url );
        tlLib.setNamespace( namespace );

        final ProjectController pc = mc.getProjectController();
        return pc.add( pn, tlLib );
    }

}

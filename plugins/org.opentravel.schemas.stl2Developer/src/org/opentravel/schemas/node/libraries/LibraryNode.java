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
package org.opentravel.schemas.node.libraries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary.BuiltInType;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemacompiler.repository.RepositoryItemHistory;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemas.controllers.ContextController;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeVisitors;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.XsdNode;
import org.opentravel.schemas.node.handlers.NamespaceHandler;
import org.opentravel.schemas.node.handlers.children.LibraryChildrenHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.LibraryOwner;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.LibraryNodeListener;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.preferences.GeneralPreferencePage;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.type.LibraryOnlyTypeFilter;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeProviderAndOwners;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereUsedLibraryHandler;
import org.opentravel.schemas.types.whereused.LibraryProviderNode;
import org.opentravel.schemas.types.whereused.LibraryUsesNode;
import org.opentravel.schemas.types.whereused.TypeUserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The LibraryNode class manages an internal navigation oriented node a library model class. Libraries are model classes
 * that contain named members representing global types and elements from either schemas (XSD), built-in-types or OTA2
 * model components.
 */

public class LibraryNode extends Node implements LibraryInterface, TypeProviderAndOwners {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryNode.class);

	protected static final String DEFAULT_LIBRARY_TYPE = "Library";
	protected static final String XSD_LIBRARY_TYPE = "XSD Library";
	protected static final String TempLib = "TemporaryLibrary";

	public static final String COMPLEX_OBJECTS = "Complex Objects";
	public static final String SIMPLE_OBJECTS = "Simple Objects";
	public static final String RESOURCES = "Resources";
	public static final String SERVICES = "Services";
	public static final String ELEMENTS = "Elements";
	protected WhereUsedLibraryHandler whereUsedHandler = null;

	// TODO - replace this with tlObj
	protected AbstractLibrary absTLLibrary; // Underlying TL library model object
	// TODO - try to make this only used in generation and not global
	protected TLLibrary genTLLib = null; // TL library for generated components.

	protected ProjectItem projectItem; // The TL Project Item wrapped around this library

	protected NamespaceHandler nsHandler;

	protected String curContext; // The current default context id to use for this library.
	private boolean editable = false;

	/**
	 * Default constructor. Used for user-directed library creation. Names type node "Library" and does <i>not</i>
	 * create navNodes.
	 * 
	 * <p>
	 * <b>NOTE:</b> does not create model object or listeners!
	 * <p>
	 * NOTE: Wizard is only user and it replaces it with one created by project controller
	 * <p>
	 * 
	 * @see {@link org.opentravel.schemas.wizards.NewLibraryWizard#NewLibraryWizard(ProjectNode)}
	 */
	// Done - change wizard to use a libraryNavNode then delete this
	@Deprecated
	public LibraryNode(ProjectNode pn) {
		assert false; // 6/14/2018 dmh
		// TODO - delete when deleting NewLibraryWizard.java

		// super();
		// assert (pn instanceof ProjectNode);
		//
		// // FIXME - this MUST have a library Nav Node
		// // Used during master reset
		// assert false;
		//
		// // setLibrary(this);
		// absTLLibrary = new TLLibrary();
		// ListenerFactory.setIdentityListner(this);
		// setParent(pn);
		// if (pn.getChildren() != null && pn.getChildren().contains(this))
		// pn.getChildren().add(this);
		// // getParent().linkLibrary(this);
		//
		// this.setName("");
		// nsHandler = NamespaceHandler.getNamespaceHandler(pn);
		// this.setNamespace(pn.getNamespace());
		// // LOGGER.debug("Created empty library without underlying model");
		// // TODO - why no listener?
		// childrenHandler = new LibraryChildrenHandler(this);
	}

	public WhereUsedLibraryHandler getWhereUsedHandler() {
		if (whereUsedHandler == null)
			whereUsedHandler = new WhereUsedLibraryHandler(this);
		return whereUsedHandler;
	}

	/**
	 * Create a library node and all of its children based on the TL model library. Create LibraryNavNode linking this
	 * library to the parent project. May be invoked with an new, empty library. Assures library managed by the
	 * appropriate TL Project.
	 * <p>
	 * Note - caller must set namespace and prefix.
	 * <p>
	 * <b>Note: </b> If {@link #getProjectItem()} == null there was an error adding this to the TL project.
	 * 
	 * @see {@link org.opentravel.schemas.controllers.DefaultRepositoryController#createMajorVersion(LibraryNode)}
	 * 
	 * @param alib
	 * @param project
	 *            project to link to with the new LibraryNavNode
	 */
	public LibraryNode(final AbstractLibrary alib, final ProjectNode project) {
		super(alib.getName());
		if (project == null)
			throw new IllegalArgumentException("Parent project must not be null.");
		// LOGGER.debug("Begin creating new library: " + alib.getName() + " in " + parent);

		// setLibrary(this);
		absTLLibrary = alib;

		// Set the Project Item, add to project if not already a member
		setProjectItem(project.addToTL(alib));
		if (projectItem == null)
			LOGGER.warn("There was an error adding library " + this + " to the project.");

		// Parent is a new library nav node
		setParent(new LibraryNavNode(this, project));

		// FIXME - this seems circular in some cases but not when creating major version.
		// Register the library
		getModelNode().getLibraryManager().add(this);

		initLibrary(alib);

		// LOGGER.debug("Library created: " + this.getName());
	}

	private void initLibrary(final AbstractLibrary alib) {
		nsHandler = NamespaceHandler.getNamespaceHandler(this);
		nsHandler.registerLibrary(this);

		// Let the tools edit the library during construction.
		setEditable(true);

		// Create Listener
		ListenerFactory.setIdentityListner(this);

		if ((alib instanceof XSDLibrary) || (alib instanceof BuiltInLibrary))
			if (genTLLib == null)
				makeGeneratedComponentLibrary(alib);

		// Create a children handler which will model all the tl objects in the library
		childrenHandler = new LibraryChildrenHandler(this);

		// Set up the contexts
		addContexts();

		// Save edit state: Test to see if this is an editable library.
		updateLibraryStatus();
	}

	/**
	 * 
	 * @param pi
	 *            - must not be null
	 * @param chain
	 *            - must not be null
	 */
	// org.opentravel.schemas.controllers.DefaultProjectController.add(LibraryNode, AbstractLibrary)
	// org.opentravel.schemas.node.LibraryChainNode.add(ProjectItem)
	public LibraryNode(ProjectItem pi, LibraryChainNode chain) {
		super(pi.getContent().getName());
		assert (chain != null);
		setParent(chain.getVersions());
		// LOGGER.debug("Begin creating new library: " + alib.getPrefix() + ":" + alib.getName() + " in aggregate "
		// + parent.getParent());
		assert (parent != null);

		absTLLibrary = pi.getContent();
		setParent(parent);
		((VersionAggregateNode) getParent()).add(this);

		assert (getProject() != null);
		assert (getProject().getTLProject() != null);

		// Set the Project Item, add to project if not already a member
		setProjectItem(getProject().addToTL(absTLLibrary));

		initLibrary(absTLLibrary);

		for (LibraryMemberInterface members : getDescendants_LibraryMembers()) {
			if (members instanceof ComponentNode)
				chain.add((ComponentNode) members);
			else
				assert false;
			// throw new IllegalStateException("Library member is not a component node!");
		}
		// FIXME - shouldn't service also be done in members?
		chain.add(getService());
		// Do NOT add resource here. It is done in addMember().

		projectItem = pi;

		// No need to register with library manager since the chain will register

		// Save edit state: may be different with Project Item.
		// Make sure library is in the managed namespace of the project.
		updateLibraryStatus();
	}

	public NamespaceHandler getNsHandler() {
		return nsHandler;
	}

	/**
	 * Using the library status and namespace policies set the library editable state. Use when changing the library's
	 * owner or namespace.
	 */
	public void updateLibraryStatus() {
		editable = isAbsLibEditable();
		// Override for managed namespaces
		if (GeneralPreferencePage.areNamespacesManaged()) {
			if (isInChain()) {
				if (getEditStatus().equals(NodeEditStatus.MANAGED_READONLY))
					this.editable = false;
				else if (getEditStatus().equals(NodeEditStatus.NOT_EDITABLE))
					this.editable = false;
			} else if (!isInProjectNS())
				this.editable = false;
		}
		getEditableState();
	}

	@Override
	public boolean isEditable() {
		return this.editable;
	}

	private boolean getEditableState() {
		if (isDeleted()) {
			return false;
		}
		boolean editable = isAbsLibEditable();
		if (GeneralPreferencePage.areNamespacesManaged()) {
			if (isInChain()) {
				if (getEditStatus().equals(NodeEditStatus.MANAGED_READONLY))
					editable = false;
				else if (getEditStatus().equals(NodeEditStatus.NOT_EDITABLE))
					editable = false;
			} else if (!isInProjectNS())
				editable = false;
		}
		return editable;
	}

	/**
	 * Override the namespace policy and TL Library status and set to editable. <b>Caution</b> can cause the GUI and TL
	 * Model to get out of sync.
	 * 
	 * @param true
	 *            to enable edits on this library
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * @return true if complex, simple, resource and service roots are null or empty
	 */
	public boolean isEmpty() {
		if (getComplexRoot() == null)
			return true; // assume others are null also
		if (getComplexRoot().isEmpty())
			if (getSimpleRoot().isEmpty())
				if (getResourceRoot().isEmpty())
					if (getServiceRoot().isEmpty())
						return true;
		return false;
	}

	/**
	 * Import a list of nodes to this library. Imports nodes then replaces type assignments.
	 * 
	 * @param global
	 *            if true all nodes that use the imported node as a type will be changed to use the imported node. If
	 *            false, only those in the current library will be changed.
	 * @return list of imported nodes without those not imported.
	 */
	// TODO - there is something wrong with the changing of type use for read-only libraries.
	public List<Node> importNodes(List<Node> sourceList, boolean global) {
		ArrayList<Node> imported = new ArrayList<>();
		final Map<Node, Node> sourceToNewMap;

		// Do the import. Nodes are typed, but not used.
		sourceToNewMap = importNodes(sourceList);
		// LOGGER.debug("Imported " + sourceToNewMap.size() + " nodes. Ready to fix type assignments.");

		// Change type users to use the imported nodes.
		LibraryNode scopeLib = this;
		if (global)
			scopeLib = null;
		for (final Entry<Node, Node> entry : sourceToNewMap.entrySet()) {
			final Node sourceNode = entry.getKey();
			imported.add(entry.getValue());
			// if (global)
			// sourceNode.replaceTypesWith(entry.getValue());
			// else
			if (entry.getValue() instanceof TypeProvider)
				sourceNode.replaceTypesWith((TypeProvider) entry.getValue(), scopeLib);
		}

		return imported;
	}

	/**
	 * Import a list of nodes to this library. Fixes the names of the imported components.
	 * {@link org.opentravel.schemas.actions.ImportObjectToLibraryAction#importSelectedToLibrary(LibraryNode)}
	 * 
	 * @return list of imported nodes excluding those not imported.
	 */
	public Map<Node, Node> importNodes(List<Node> sourceList) {
		// insertion ordered
		final Map<Node, Node> sourceToNewMap = new LinkedHashMap<>(sourceList.size());

		// Create aliases if the source has multiple properties which use the same complex type.
		// Must be done before imports to assure all referenced types have the aliases added.
		for (final Node source : sourceList) { // FIXME - choice can be alias owner
			if (source instanceof CoreObjectNode || source instanceof BusinessObjectNode)
				((ComponentNode) source).createAliasesForProperties();
		}

		NodeVisitor nameFixer = new NodeVisitors().new FixNames();
		for (final Node source : sourceList) {
			final Node newNode = importNode(source);
			if (newNode != null && newNode != source) {
				nameFixer.visit(newNode);
				sourceToNewMap.put(source, newNode);
			}
			// else
			// LOGGER.warn("Import duplicate excluded from map: " + newNode);
		}

		collapseContexts();

		new TypeResolver().resolveTypes(this);

		// LOGGER.info("ImportNodes() imported " + sourceToNewMap.size() + " nodes. ");
		return sourceToNewMap;
	}

	/**
	 * Import a node to this library. Clone the node and underlying models and add it to the library.
	 * 
	 * NOTE: newly imported node is UN-TYPED.
	 * 
	 * @param source
	 * @return - new node created, source if it already was in library, or null on error.
	 */
	public Node importNode(final Node source) {
		// LOGGER.debug("Importing source node: " + source.getName());
		if (source.getLibrary() == this) {
			// LOGGER.error("Tried to import to same library: " + this.getName());
			return source; // nothing to do.
		}
		if (!importNodeCheck(source))
			return null;

		// Don't use ContextUtils because may create new contexts in the target library
		LibraryMemberInterface newNode = NodeFactory.newLibraryMember((LibraryMember) source.cloneTLObj());
		if (newNode == null) {
			// LOGGER.warn("Could not clone " + source + " a " + source.getClass().getSimpleName());
			return null;
		}

		addMember(newNode);

		collapseContexts(); // slow, but reliable

		assert getTLLibrary().getContexts().size() == 1;

		if (!(newNode instanceof EnumerationClosedNode))
			((Node) newNode).setExtensible(true);

		return (Node) (newNode);
	}

	/**
	 * @return false if not a TL library, source is not a library member or library is not editable
	 */
	private boolean importNodeCheck(Node source) {
		if (this.getTLLibrary() == null) {
			// LOGGER.error("Tried to import source node to non-TL library: " + this.getName());
			return false;
		}
		if ((source.getTLModelObject() == null) || !(source.getTLModelObject() instanceof LibraryMember)) {
			// LOGGER.error("Exit - not a TLLibraryMember: " + source.getName());
			return false;
		}
		if (!this.isEditable()) {
			// LOGGER.error("Tried to import to a read-only library: " + this.getName());
			return false;
		}
		return true;
	}

	/**
	 * True if the compiler reports the library is editable. Also checks the file system to see if it can write to the
	 * file.
	 * 
	 */
	public boolean isAbsLibEditable() {
		boolean isEditable = (absTLLibrary != null) && !absTLLibrary.isReadOnly();
		// override with false depending on repository state.
		if (getProjectItem() != null) {
			if (getProjectItem().getState().equals(RepositoryItemState.MANAGED_UNLOCKED))
				isEditable = false;
			if (getProjectItem().getState().equals(RepositoryItemState.MANAGED_LOCKED))
				isEditable = false;
		}
		return isEditable;
	}

	public TLLibraryStatus getStatus() {
		TLLibraryStatus status = TLLibraryStatus.DRAFT;
		return getProjectItem() == null ? status : getProjectItem().getStatus();
	}

	/**
	 * Add a tlContext to the library if none exists. Then, if this is a TL Library add the context to the context
	 * controller. If the TLLibrary associated with this library does not have a context, make one.
	 */
	public void addContexts() {
		ContextController cc = OtmRegistry.getMainController().getContextController();
		if (cc == null)
			throw new IllegalStateException("Context Controller not registered before use.");

		TLLibrary lib = getTLLibrary();
		if (lib == null)
			return; // builtin will be null until generated
		if (isEditable() && lib.getContexts().size() > 1)
			collapseContexts();

		if (lib.getContexts().isEmpty()) {
			TLContext tlc = new TLContext();
			lib.addContext(tlc);

			tlc.setContextId(defautIfNullOrEmpty(lib.getPrefix(), "default"));
			tlc.setApplicationContext(defautIfNullOrEmpty(lib.getNamespace(), "Default"));
		}

		if (isTLLibrary())
			cc.addContexts(this);
	}

	/**
	 * Collapse all contexts in this library (context model and tlLibrary) down to the default context. All contents of
	 * the library may be changed to merge contexts to the default.
	 */
	// Collapse all contexts down to one. Temporary fix that may be in place for years.
	public void collapseContexts() {
		// LOGGER.debug("Ready to merge contexts for library: " + this);
		if (!(getTLModelObject() instanceof TLLibrary)) {
			// LOGGER.error("Error. Not a valid library for collapseContexts.");
			return;
		}

		// If there are no context then we are done.
		if (getTLLibrary().getContexts().isEmpty())
			return;

		// tlc is the context to keep.
		TLContext tlc = getTLLibrary().getContext(getDefaultContextId());
		if (tlc == null)
			tlc = getTLLibrary().getContexts().get(0); // there must be at least one

		// If there is only one TLContext then make sure context manager matches.
		if (getTLLibrary().getContexts().size() == 1) {
			// assert (cc.getAvailableContextIds(this).size() == 1);
			// assert (cc.getAvailableContextIds(this).size() == getTLLibrary().getContexts().size());
			// assert (cc.getAvailableContextIds(this).get(0).equals(tlc.getContextId()));
			return; // all done. if any child used a different context the TLLibrary would have more than 1.
		}

		// More than one context is being used. Merge context of children the collapse down the unused contexts.
		//
		// Merge contexts in all children of this library.
		List<LibraryMemberInterface> lms = getDescendants_LibraryMembers();
		for (LibraryMemberInterface n : getDescendants_LibraryMembers()) {
			((Node) n).mergeContext(tlc.getContextId());
		}

		// Now remove the unused contexts
		List<TLContext> contexts = new ArrayList<>(getTLLibrary().getContexts());
		for (TLContext tc : contexts) {
			if (tc != tlc) {
				getTLLibrary().removeContext(getTLLibrary().getContext(tc.getContextId()));
				// LOGGER.debug("removed " + tc.getContextId() + " from tlLibrary " + this);
			}
		}

		assert (((TLLibrary) getTLModelObject()).getContexts().size() == 1);

		// TODO - Make the context manager agree with the tllibrary
		// cc.clearContexts(this);
		// cc.newContext(this, tlc.getContextId(), tlc.getApplicationContext());
		// FAILS - cm.addNode(this, tlc);
		// assert (cc.getAvailableContextIds(this).size() == 1);
		// assert (cc.getAvailableContextIds(this).size() == getTLLibrary().getContexts().size());
		// assert (cc.getAvailableContextIds(this).get(0).equals(tlc.getContextId()));

		// LOGGER.debug("merged contexts into context " + tlc.getContextId());
	}

	/**
	 * Create the library for the generated components.
	 * 
	 * @param xLib
	 */
	protected void makeGeneratedComponentLibrary(AbstractLibrary xLib) {
		genTLLib = new TLLibrary();
		if (xLib == null) {
			genTLLib.setNamespace("uri:namespaces:temporaryNS");
			genTLLib.setPrefix("tmp");
			genTLLib.setName(TempLib);
			try {
				genTLLib.setLibraryUrl(new URL("temp"));
			} catch (MalformedURLException e) {
				// LOGGER.error("Invalid URL exception");
			}
		} else {
			genTLLib.setNamespace(xLib.getNamespace());
			genTLLib.setPrefix(xLib.getPrefix());
			genTLLib.setName(xLib.getName());
			genTLLib.setLibraryUrl(xLib.getLibraryUrl());
			// genTLLib.setOwningModel(getTLModel()); // TEST - TEST - TEST
		}
		setEditable(false);
	}

	/**
	 * @return - the LTLibrary created to hold generated otm model elements
	 */
	public TLLibrary getGeneratedLibrary() {
		return genTLLib;
	}

	@Override
	public Image getImage() {
		final ImageRegistry imageRegistry = Images.getImageRegistry();
		if (getTLaLib() instanceof XSDLibrary) {
			return imageRegistry.get(Images.builtInLib);
		}
		if (getTLaLib() instanceof BuiltInLibrary) {
			return imageRegistry.get(Images.builtInLib);
		}
		return imageRegistry.get(Images.library);
	}

	/**
	 * Lock this library in its repository. User repository controller to handle exceptions with user dialogs.
	 * 
	 * @throws RepositoryException
	 * @throws LibraryLoaderException
	 */
	// TODO - why is lock managed here while unlock is managed in project manager?
	// only called by run in repo controller and by tests in repository controller test
	public void lock() throws RepositoryException, LibraryLoaderException {
		ProjectNode pn = getProject();
		if (pn == null)
			throw new RepositoryException("Library was not part of a project");

		// String path = pn.getProject().getProjectFile().getAbsolutePath();
		File dir = pn.getTLProject().getProjectFile().getParentFile();

		// FIXME - don't get the list.
		// try to lock - if throws exception then refresh and try again.

		// 5/2016 - dmh - refresh the project to assure the most current version is being locked.
		List<ProjectItem> updated = pn.getTLProject().getProjectManager().refreshManagedProjectItems();
		// pn.getProject().getProjectManager().refreshManagedProjectItems();

		pn.getTLProject().getProjectManager().lock(getProjectItem());
		// LOGGER.debug("Locked library which created local file: " + path);
		setEditable(isAbsLibEditable());
	}

	public void checkExtension(Node n) {
		if (n instanceof ExtensionOwner) {
			Node base = ((ExtensionOwner) n).getExtensionBase();
			if (base != null && base.getLibrary() != null) {
				if (!base.getLibrary().get_LibraryMembers().contains(base)) {
					// LOGGER.error(base.getNameWithPrefix() + " library is not correct.");
					// List<LibraryMemberInterface> members = base.getLibrary().get_LibraryMembers();
					for (LibraryNode ln : Node.getAllUserLibraries())
						for (LibraryMemberInterface n2 : ln.get_LibraryMembers())
							if (n2 == base) {
								base.setLibrary(ln);
								LOGGER.error("Corrected library " + base.getNameWithPrefix() + " to " + ln);
							}
				}
				// Has base, if a version base then make sure version node is set
				if (base.getName().equals(n.getName()) && !(n instanceof Enumeration)) {
					if (!n.getVersionNode().contains(base))
						LOGGER.error("Version node for " + n + " MUST have base in it's children.");
					if (n.getVersionNode().getPreviousVersion() == null)
						LOGGER.error(n + " MUST have previous version.");
				}
			} else {
				// No base, check tl model
				if (getTLModelObject() instanceof TLExtensionOwner)
					if (((TLExtensionOwner) getTLModelObject()).getExtension() != null)
						LOGGER.error("base was null but tlExtension was not. ");
			}
		}
	}

	// FIXME - only used in tests
	public boolean hasGeneratedChildren() {
		return genTLLib.getNamedMembers().size() > 0 ? true : false;
	}

	/**
	 * Add node to this library. Links to library's complex/simple or element root. Handles adding nodes to chains.
	 * <ul>
	 * <li>Removes from existing library if in a different library. If already in this library, returns with no changes.
	 * <li>Updates assignments (type, base extension and contextual facet injection).
	 * <li>Adds underlying the TL object to this library's TLModel library if this member is not inherited.
	 * </ul>
	 * 
	 * @param lm
	 *            library member node to add to this library
	 */
	public void addMember(final LibraryMemberInterface lm) {
		assert getChildrenHandler() != null;
		assert lm.getTLModelObject() != null;
		assert lm.getTLModelObject() instanceof LibraryMember;
		// Services - there can only be one service in a library
		if (lm instanceof ServiceNode) {
			addServiceNode((ServiceNode) lm);
			return;
		}

		// Safety Check - Subject must have resource in its where assigned list.
		if (lm instanceof ResourceNode && ((ResourceNode) lm).getSubject() != null)
			assert (((ResourceNode) lm).getSubject().getWhereAssigned().contains(lm));

		// Hold onto number of contextual facets for assertion after move
		int cfCount = 0;
		// List<AbstractContextualFacet> cfList = null;
		if (lm instanceof ContextualFacetOwnerInterface) {
			cfCount = ((ContextualFacetOwnerInterface) lm).getContextualFacets(false).size();
			//
			// // Hold onto Contextual facets need to be re-assigned after LM is moved
			// cfList = ((ContextualFacetOwnerInterface) lm).getContextualFacets(false);
		}
		if (!isEditable() && !(lm instanceof InheritedInterface)) {
			LOGGER.warn("Tried to addMember() " + lm + " to non-editable library " + this);
			return;
		}

		// Early Exits
		if (this.contains((Node) lm) && lm.getLibrary() == this) {
			assert getTLLibrary().getNamedMembers().contains(lm.getTLModelObject());
			return; // early exit - already a member
		}
		assert !getTLLibrary().getNamedMembers().contains(lm.getTLModelObject());

		// FIXME - Work around for defect reported 8/15/2018
		// Remove all aliases and add them back later
		List<AliasNode> aliases = null;
		if (lm instanceof AliasOwner) {
			aliases = new ArrayList<>(lm.getAliases());
			for (AliasNode alias : aliases)
				((AliasOwner) lm).remove(alias);
		}

		// Remove from Old library if any
		LibraryNode oldLib = lm.getLibrary();
		if (oldLib != null && oldLib != this) {
			oldLib.removeMember(lm, false);
			assert !oldLib.contains((Node) lm);
			// FIXME - Work around for defect reported 8/15/2018
			// if (lm instanceof ContextualFacetOwnerInterface)
			// for (AbstractContextualFacet cf : ((ContextualFacetOwnerInterface) lm).getContextualFacets(false))
			// if (cf.getTLModelObject().getAliases() != null)
			// for (TLAlias tla : cf.getTLModelObject().getAliases())
			// cf.getTLModelObject().removeAlias(tla); // Operation not supported
		}

		// Assure contextual facets remained
		if (lm instanceof ContextualFacetOwnerInterface)
			assert cfCount == ((ContextualFacetOwnerInterface) lm).getContextualFacets(false).size();

		// Add to the TL Library - if not already in it and not inherited
		if (!(lm instanceof InheritedInterface))
			getTLLibrary().addNamedMember((LibraryMember) lm.getTLModelObject());

		// Add to the node library and/or chain
		lm.setLibrary(this);
		if (getChain() != null)
			getChain().add((ComponentNode) lm);
		getChildrenHandler().add(lm);
		assert this.contains((Node) lm);

		// If the contextual facets were not modeled, model them and add to this library.
		if (lm.getChildrenHandler() != null)
			for (TLModelElement tlcf : lm.getChildrenHandler().getChildren_TL())
				if (tlcf instanceof TLContextualFacet && Node.GetNode(tlcf) == null)
					addMember(NodeFactory.newLibraryMember((LibraryMember) tlcf));

		// Make sure it did not bring additional contexts with it
		collapseContexts(); // TODO - optimize to just object not whole library

		// Subject must have resource in its where assigned list.
		if (lm instanceof ResourceNode && ((ResourceNode) lm).getSubject() != null)
			assert (((ResourceNode) lm).getSubject().getWhereAssigned().contains(lm));

		// Reverify contextual facet count
		if (lm instanceof ContextualFacetOwnerInterface)
			assert cfCount == ((ContextualFacetOwnerInterface) lm).getContextualFacets(false).size();

		// Reassignments - assure extensions, contextual facets and type users are assigned to the moved LM
		// Extensions
		if (lm instanceof ExtensionOwner)
			for (ExtensionOwner subType : ((Node) lm).getWhereExtendedHandler().getWhereExtended())
				subType.setExtension((Node) lm);
		// Contextual facets
		if (lm instanceof ContextualFacetOwnerInterface)
			for (AbstractContextualFacet cf : ((ContextualFacetOwnerInterface) lm).getContextualFacets(false)) {
				cf.getTLModelObject().setOwningEntity((TLFacetOwner) lm.getTLModelObject());
				cf.checkAliasesAreUnique(true);
				// assert cf.checkAliasesAreUnique(false);
			}
		// Type assignments
		if (lm instanceof TypeProvider)
			((TypeProvider) lm).getWhereAssignedHandler().replaceAll((TypeProvider) lm, null);

		// FIXME - Work around for defect reported 8/15/2018
		// add back the aliases and their type assignments
		if (aliases != null) {
			for (AliasNode alias : aliases) {
				((AliasOwner) lm).addAlias(alias);
				for (TypeUser tu : alias.getWhereAssigned())
					tu.setAssignedTLType(alias.getTLModelObject());
			}
		}
		if (lm instanceof ContextualFacetOwnerInterface)
			for (AbstractContextualFacet cf : ((ContextualFacetOwnerInterface) lm).getContextualFacets(false))
				assert cf.checkAliasesAreUnique(false);

	}

	public boolean isInChain() {
		// assert getParent() instanceof VersionAggregateNode;
		return getChain() != null;
	}

	@Override
	public LibraryChainNode getChain() {
		// FIXME - delegate second get parent - versionAggregateNode
		if (getParent() == null || getParent().getParent() == null)
			return null;
		return getParent().getParent() instanceof LibraryChainNode ? (LibraryChainNode) getParent().getParent() : null;
	}

	/**
	 * Use the parent, if known, to close this library from the parent's context. If the parent is null, close the
	 * contents of this library.
	 * <p>
	 * <b>Warning</b> the caller must assure the parent is the intended LibraryOwner or null.
	 * <p>
	 * The parent uses the library model manager because this LibraryNode may be linked to multiple LibraryNavNodes or
	 * VersionAggregateNodes. If this library is not used elsewhere the library model manager will call close with
	 * parent set to null.
	 */
	@Deprecated
	@Override
	public void close() {
		if (getParent() != null)
			assert false;
		closeLibraryInterface();
	}

	/**
	 * Use the parent, if known, to close this library from the parent's context. If the parent is null, close the
	 * contents of this library.
	 * <p>
	 * <b>Warning</b> the caller must assure the parent is the intended LibraryOwner or null.
	 * <p>
	 * The parent uses the library model manager because this LibraryNode may be linked to multiple LibraryNavNodes or
	 * VersionAggregateNodes. If this library is not used elsewhere the library model manager will call close with
	 * parent set to null.
	 */
	@Override
	public void closeLibraryInterface() {
		// LOGGER.debug("Closing library " + getNameWithPrefix());

		// Attempt to use the parent to close this library
		if (getParent() instanceof VersionAggregateNode) {
			((VersionAggregateNode) getParent()).close(this);
		} else if (getParent() instanceof LibraryNavNode) {
			((LibraryNavNode) getParent()).close();
		} else {
			// Close all contents. When called from library manager, the parent is null.
			if (getProjectItem() != null)
				ListenerFactory.clearListners(getProjectItem().getContent());
			// if (getName().equals("test1"))
			// LOGGER.debug("HERE");
			for (Node n : getChildren_New())
				n.close();

			deleted = true;

			if (!isEmpty())
				LOGGER.debug("Closed library " + this + " is not empty.");
			assert (isEmpty());
			assert (deleted);
		}
		// assert (getParent() == null);
	}

	public List<RepositoryItemCommit> getCommitHistory() {
		RepositoryItemHistory h = null;
		if (projectItem != null && projectItem.getRepository() != null) {
			try {
				h = projectItem.getRepository().getHistory(projectItem);
				h.getCommitHistory();// LOGGER.debug("Committed " + this);
			} catch (RepositoryException e) {
				// LOGGER.debug("Exception: " + e.getLocalizedMessage());
			}
		}
		return h != null ? h.getCommitHistory() : null;
	}

	/**
	 * Libraries are not deleted, just closed.
	 * <p>
	 * WARNING - libraries should <b>only</b> be closed or deleted from LibraryNavNode because a library may have many
	 * parents so it doesn't know which one to close.
	 */
	@Deprecated
	@Override
	public void delete() {
		close();
	}

	/**
	 * Delete this library from the repository project and project node. Does <b>not</b> check Library Model Manager to
	 * see if the library is used elsewhere.
	 * 
	 * <b>NOTE:</b> parent must be the project the library is to be removed from. Libraries can have multiple parents!
	 * 
	 * @param doMembers
	 *            when true delete all members of the library
	 */
	@Deprecated
	public void delete(boolean doMembers) {
		if (isBuiltIn())
			return;
		close();

	}

	/**
	 * @return TypeProvider with matching name or null if not found.
	 */
	public TypeProvider findTypeProvider(String name) {
		TypeProvider node = null;
		for (TypeProvider p : getDescendants_TypeProviders())
			if (p.getName().equals(name))
				return p;
		return node;
	}

	/**
	 * Copy a member into this library.
	 * 
	 * @param source
	 * @return cloned source object added to this library
	 * @throws IllegalArgumentException
	 */
	public LibraryMemberInterface copyMember(final LibraryMemberInterface source) throws IllegalArgumentException {
		if (source == null || ((Node) source).getTLModelObject() == null)
			throw new IllegalArgumentException("Null in copy source model.");
		if (!(getTLModelObject() instanceof TLLibrary))
			throw new IllegalArgumentException("Copy destination library is not a TLLibrary.");

		return source.copy(this);
	}

	/**
	 * Move a node from its library to a different library. Moves the node and underlying TL object.
	 * 
	 * @param mbr
	 *            library member to be moved
	 * @param destination
	 */
	// FIXME - how is this any different that destination.addMember(mbr) ???
	// FIXME - used in changeNode in main controller and move object action
	@Deprecated
	public void moveMember(final Node mbr, LibraryNode destination) throws IllegalArgumentException {
		// Pre-tests
		if (!(mbr instanceof LibraryMemberInterface))
			throw new IllegalArgumentException(mbr + " is not a library member.");
		if (mbr.getLibrary() != this)
			throw new IllegalArgumentException("Internal error - source of move is in wrong library.");
		if (destination == null)
			throw new IllegalArgumentException("Move destination library is null.");
		if (!(destination.getTLModelObject() instanceof TLLibrary))
			throw new IllegalArgumentException("Move destination library is not a TLLibrary.");
		// Assure listeners are correct
		if (Node.GetNode(getTLModelObject()) != this)
			throw new IllegalArgumentException("Internal Error - incorrect listener on " + this);
		if (Node.GetNode(destination.getTLModelObject()) != destination)
			throw new IllegalArgumentException("Internal Error - incorrect listener on " + destination);

		destination.addMember((LibraryMemberInterface) mbr);

		destination.collapseContexts(); // reduce down to one context
		assert !this.contains(mbr);
		assert destination.contains(mbr);
		return;
	}

	/**
	 * Delete this library.
	 */

	/**
	 * Remove the node from its library and from the underlying tl library.
	 * <p>
	 * Does <b>not</b> delete the node or its TL Object contents. TL type assignments are assured to match the
	 * assignments in TypeNode.
	 * <p>
	 * NOTE: does not replace this node with an earlier version in a version chain.
	 * 
	 */
	// FIXME - make this param a LibraryMemberInterface
	public void removeMember(final LibraryMemberInterface n) {
		if (!(n.getTLModelObject() instanceof LibraryMember))
			return;
		removeMember(n, true);
	}

	public void removeMember(final LibraryMemberInterface n, boolean alsoDoContextualFacets) {
		if (isBuiltIn())
			return;

		// Nov 18, 2017 - workaround for compiler bug in tlBusinessObject
		if (alsoDoContextualFacets && n instanceof BusinessObjectNode)
			for (AbstractContextualFacet cf : ((BusinessObjectNode) n).getContextualFacets(false))
				if (cf instanceof ContextualFacetNode)
					cf.delete();

		if (getChildrenHandler() != null) {
			if (n.getLibrary() != null && n.getLibrary().getTLModelObject() != null)
				n.getLibrary().getTLModelObject().removeNamedMember((LibraryMember) n.getTLModelObject());
			if (n instanceof ServiceNode && n.getParent() instanceof NavNode)
				((NavNode) n.getParent()).removeLM(n); // all others done in listener
		} else {
			assert false;
		}
	}

	@Override
	public LibraryNode getLibrary() {
		return this;
	}

	/**
	 * @return string of version number from ns handler IFF managed project item, empty string otherwise.
	 */
	public String getLibraryVersion() {
		String version = "";
		if (getProjectItem() != null && getNsHandler() != null
				&& !RepositoryItemState.UNMANAGED.equals(getProjectItem().getState()))
			version = getNsHandler().getNSVersion(getNamespace());
		return version;
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new LibraryNodeListener(this);
	}

	/**
	 * Use {@link #getTLModelObject()}
	 * 
	 * @return
	 */
	@Deprecated
	public AbstractLibrary getTLaLib() {
		return absTLLibrary;
	}

	@Override
	public LibraryChildrenHandler getChildrenHandler() {
		return (LibraryChildrenHandler) childrenHandler;
	}

	/**
	 * @return - Return the library's complex root node.
	 */
	public NavNode getComplexRoot() {
		return getChildrenHandler().getComplexRoot();
	}

	public NavNode getServiceRoot() {
		return getChildrenHandler().getServiceRoot();
	}

	public NavNode getSimpleRoot() {
		return getChildrenHandler().getSimpleRoot();
	}

	public NavNode getResourceRoot() {
		return getChildrenHandler().getResourceRoot();
	}

	@Override
	public String getComponentType() {
		return DEFAULT_LIBRARY_TYPE;
	}

	@Override
	public String getNamespace() {
		return getTLModelObject().getNamespace();
	}

	public String getNSBase() {
		return nsHandler.getNSBase(getNamespace());
	}

	public String getNSExtension() {
		return nsHandler.getNSExtension(getNamespace());
	}

	public String getNSVersion() {
		return nsHandler.getNSVersion(getNamespace());
	}

	@Override
	public String getNamespaceWithPrefix() {
		final String prefix = getPrefix();
		final String namespace = getNamespace();
		return (prefix.isEmpty() ? "() " + namespace : "( " + prefix + " ) " + namespace);
	}

	@Override
	public String getPrefix() {
		return emptyIfNull(getTLaLib().getPrefix());
	}

	/**
	 * Override to false if it is in a chain.
	 */
	@Override
	public boolean isNavChild(boolean deep) {
		return !(parent instanceof VersionAggregateNode);
	}

	/**
	 * @return true if this library or one in its chain has a service.
	 */
	public boolean hasService() {
		return getService() != null;
	}

	public ServiceNode getService() {
		if (isInChain())
			return getChain().getServiceAggregate().getService();
		else
			return getChildrenHandler().getServiceRoot().getService();
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	@Override
	public boolean isXSDSchema() {
		return (absTLLibrary instanceof BuiltInLibrary
				&& ((BuiltInLibrary) absTLLibrary).getBuiltInType().equals(BuiltInType.SCHEMA_FOR_SCHEMAS_BUILTIN));
		// return getTLaLib() instanceof XSDLibrary;
	}

	/**
	 * Is this either of the built in libraries: XSD Schema or OTA_Common_v01_00
	 */
	@Override
	public boolean isBuiltIn() {
		return getTLModelObject() instanceof BuiltInLibrary;
	}

	@Override
	public boolean isTLLibrary() {
		return getTLModelObject() instanceof TLLibrary;
	}

	/**
	 * @throws IllegalArgumentException
	 *             - thrown if the new value creates a name/namespace conflict with another library
	 */
	@Override
	public void setName(final String n) {
		if (!this.isTLLibrary() || getTLaLib() == null)
			return;
		getTLaLib().setName(n);
	}

	@Override
	public String getName() {
		return getTLModelObject() == null || getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public String getLabel() {
		String prefix = "";
		if (!getPrefix().isEmpty())
			prefix = getPrefix() + " : ";
		return prefix + getName();
	}

	/**
	 * Can members be moved? True is some members can be moved. Individual members can not be moved if they are in older
	 * versions OR are not new to the head version.
	 * 
	 * @return - true if members of the library can be moved
	 */
	public boolean isMoveable() {
		return isManaged() ? isEditable() : isTLLibrary();
	}

	public void setNamespace(String ns) {
		if (nsHandler == null)
			throw new IllegalStateException("Null nsHandler");
		if (ns == null || ns.isEmpty())
			throw new IllegalArgumentException("Null or empty namespace argument.");
		String prefix = getPrefix(); // save in case not registered.
		if (nsHandler.setLibraryNamespace(this, ns)) {
			// don't set prefix if library namespace could not be set
			if (getPrefix().isEmpty()) {
				nsHandler.setNamespacePrefix(ns, prefix);
				setNSPrefix(prefix);
			}
		}
		updateLibraryStatus();
	}

	public void setNSPrefix(String n) {
		if (n == null || n.isEmpty()) {
			n = UNDEFINED_PROPERTY_TXT;
		}
		getTLaLib().setPrefix(n);
		nsHandler.setNamespacePrefix(getNamespace(), n);
	}

	public void setVersionScheme(final String scheme) {
		if (absTLLibrary instanceof TLLibrary) {
			((TLLibrary) absTLLibrary).setVersionScheme(scheme);
		}
	}

	public String getVersionScheme() {
		String scheme = "";
		if (absTLLibrary instanceof TLLibrary) {
			scheme = ((TLLibrary) absTLLibrary).getVersionScheme();
		}
		return emptyIfNull(scheme);
	}

	public void setVersion(final String version) {
		if (absTLLibrary instanceof TLLibrary) {
			((TLLibrary) absTLLibrary).setVersion(version);
			// LOGGER.debug("Set version of " + this + " to " + version);
		}
		// TODO - implement the rest of the version logic!
	}

	public String getVersion() {
		String version = "";
		if (absTLLibrary != null && absTLLibrary instanceof TLLibrary)
			version = ((TLLibrary) absTLLibrary).getVersion();
		return emptyIfNull(version);
	}

	/**
	 * Get the major version number from the TLLibrary
	 * 
	 * @return
	 */
	public String getVersion_Major() {
		String version = "";
		if (absTLLibrary != null && absTLLibrary instanceof TLLibrary)
			version = ((TLLibrary) absTLLibrary).getVersion();
		if (version.contains("."))
			version = version.substring(0, version.indexOf("."));
		return emptyIfNull(version);
	}

	@Override
	public void setParent(Node parent) {
		assert parent == null || parent instanceof LibraryOwner;
		this.parent = parent;
	}

	public void setPath(final String text) {
		final File file = new File(text);
		final URL fileURL = URLUtils.toURL(file);
		// LOGGER.debug("File url being set to: "+fileURL);
		absTLLibrary.setLibraryUrl(fileURL);
	}

	/**
	 * NOTE - a library can belong to many projects. The LibraryNavNode parent relates it to a single project. The
	 * parent can also be a library chain.
	 */
	@Override
	public Node getParent() {
		assert parent == null || parent instanceof LibraryOwner;
		return parent;
	}

	/**
	 * @return the file path converted from the library URL
	 */
	public String getPath() {
		String path = "";
		if (absTLLibrary != null && absTLLibrary.getLibraryUrl() != null) {
			try {
				path = URLUtils.toFile(absTLLibrary.getLibraryUrl()).getCanonicalPath();
			} catch (final Exception e) {
				path = absTLLibrary.getLibraryUrl().toString();
			}
		}
		return emptyIfNull(path);
	}

	/**
	 * Get Repository
	 */
	public String getRepositoryDisplayName() {
		if (projectItem == null) {
			return "Error";
		}
		if (projectItem.getRepository() == null)
			return "Local File System";
		if (projectItem.getRepository() instanceof RemoteRepository)
			return ((RemoteRepository) projectItem.getRepository()).getDisplayName();
		else
			return "Local";
	}

	/**
	 * @return the projectItem
	 */
	public ProjectItem getProjectItem() {
		return projectItem;
	}

	/**
	 * Get the library provider node that identifies types in this library that use types from the passed library.
	 * 
	 * @param ln
	 *            library providing types to this library
	 * @return library providing types or null if not found
	 */
	public LibraryProviderNode getLibraryProviderNode(LibraryNode ln) {
		LibraryProviderNode thisLPN = null;
		if (getWhereUsedHandler() == null || getWhereUsedHandler().getUsedByNode() == null
				|| getWhereUsedHandler().getUsedByNode().getChildren() == null)
			return null;

		for (Node p : getWhereUsedHandler().getUsedByNode().getChildren())
			if (p instanceof LibraryProviderNode)
				if (((LibraryProviderNode) p).getOwner() == ln) {
					thisLPN = (LibraryProviderNode) p;
				}
		return thisLPN;
	}

	/**
	 * Return the project this library or its chain have as its parent. <b>Note</b> that libraries can belong to
	 * multiple projects.
	 * 
	 * @see {@link LibraryModelManager#isUsedElsewhere(LibraryInterface, ProjectNode)}
	 * @return parent project or null if no project is found.
	 */
	@Override
	public ProjectNode getProject() {
		ProjectNode pn = null;
		if (getParent() instanceof LibraryOwner)
			pn = ((LibraryOwner) getParent()).getProject();
		// FIXME - these should never be parents
		else if (getParent() instanceof ProjectNode)
			pn = (ProjectNode) getParent();
		else if (getParent() instanceof LibraryChainNode)
			pn = ((LibraryChainNode) getParent()).getProject();
		return pn;
	}

	/**
	 * Simple setter of projectItem field
	 * 
	 * @param projectItem
	 *            the projectItem to set
	 */
	public void setProjectItem(ProjectItem projectItem) {
		this.projectItem = projectItem;
	}

	public void setComments(final String comments) {
		if (absTLLibrary instanceof TLLibrary) {
			((TLLibrary) absTLLibrary).setComments(comments);
		}
	}

	public String getComments() {
		String comments = "";
		if (absTLLibrary instanceof TLLibrary)
			comments = ((TLLibrary) absTLLibrary).getComments();
		return emptyIfNull(comments);
	}

	public String getRemarks() {
		String comments = "";
		if (absTLLibrary instanceof TLLibrary)
			comments = ((TLLibrary) absTLLibrary).getComments();
		return emptyIfNull(comments);
	}

	/**
	 * @return - list of context id strings, empty list of not TLLibrary or no contexts assigned.
	 */
	// FIXME - only used in tests
	public List<String> getContextIds() {
		ArrayList<String> contexts = new ArrayList<>();
		for (TLContext c : getTLLibrary().getContexts())
			contexts.add(c.getContextId());
		return contexts;
	}

	/**
	 * Get the default context. As of 9/2015 there is only one context. This method allows for the obsolescence of the
	 * context controller.
	 */
	public String getDefaultContextId() {
		String id = OtmRegistry.getMainController().getContextController().getDefaultContextId(this);
		if (id.isEmpty() && absTLLibrary instanceof TLLibrary)
			if (getTLLibrary().getContexts().get(0) instanceof TLContext)
				id = getTLLibrary().getContexts().get(0).getContextId();
		return id;
	}

	/**
	 * 
	 * @return - Return either the original TLLibrary or the one used for generated components.
	 */
	// TODO - consider sub-typing library for built-in
	public TLLibrary getTLLibrary() {
		return absTLLibrary instanceof TLLibrary ? (TLLibrary) absTLLibrary : genTLLib;
	}

	@Override
	public AbstractLibrary getTLModelObject() {
		return absTLLibrary instanceof AbstractLibrary ? (AbstractLibrary) absTLLibrary : genTLLib;
	}

	@Override
	public TypeSelectionFilter getTypeSelectionFilter() {
		return new LibraryOnlyTypeFilter();
	}

	/**
	 * Set the current default context
	 * 
	 * @param curContext
	 */
	public void setCurContext(String curContext) {
		this.curContext = curContext;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public boolean isDeleteable() {
		return true;
	}

	/**
	 * @return - returns the string if not null or empty, or else the ifTrue string
	 */
	private static String defautIfNullOrEmpty(String s, String ifTrue) {
		return (s == null) || s.isEmpty() ? ifTrue : s;
	}

	/**
	 * Add a member created from an XSD local anonymous type.
	 * 
	 * @param n
	 */
	// TODO - move the temp/local library management here. genTLLib
	public void addLocalMember(XsdNode xn, ComponentNode cn) {
		// if (xn == null || cn == null)
		// return;
		// If being called from within children handler so do nothing.
		if (cn instanceof LibraryMemberInterface)
			if (getChildrenHandler() != null)
				addMember((LibraryMemberInterface) cn);
	}

	/**
	 * Get a new list of library members in this library. Version nodes return their actual object. Includes objects
	 * inside NavNodes.
	 * 
	 * @return
	 */
	public List<LibraryMemberInterface> get_LibraryMembers() {
		List<LibraryMemberInterface> members = new ArrayList<>();
		for (Node n : getChildren()) {
			if (n instanceof NavNode)
				members.addAll(((NavNode) n).get_LibraryMembers());
			if (n instanceof VersionNode && ((VersionNode) n).get() != null)
				n = ((VersionNode) n).get();
			if (n instanceof LibraryMemberInterface)
				members.add((LibraryMemberInterface) n);
		}
		return members;
	}

	/**
	 * Examine each descendant that is a type user or extension owner, if those nodes use types from other libraries,
	 * add that library to returned list, if the library is in a chain, return the head library.
	 * 
	 * Used by {@link LibraryUsesNode#getChildren()} and {@link TypeUserNode#getChildren()}
	 * <p>
	 * WhereUseHandler provides the inverse relationship. Libraries in the list should have this library in their where
	 * used handler. {@link WhereUsedLibraryHandler#getWhereUsed()}
	 * 
	 * @param deep
	 *            if true collect users from the chain that contains the library.
	 * 
	 * @see {@link org.opentravel.schemas.controllers.repository.LibraryVersionUpdateTest#updateVersionTest_BaseTypes()}
	 *      for tests.
	 * 
	 * @return a new array list of libraries that contain extensions or types assigned to any named object in this
	 *         library.
	 */
	public List<LibraryNode> getAssignedLibraries(boolean deep) {
		Set<LibraryNode> usedLibs = new HashSet<>();
		if (deep && getChain() != null)
			for (LibraryNode lib : getChain().getLibraries())
				usedLibs.addAll(lib.getAssignedLibraries());
		else
			usedLibs.addAll(getAssignedLibraries());
		return new ArrayList<>(usedLibs);
	}

	private List<LibraryNode> getAssignedLibraries() {
		Set<LibraryNode> usedLibs = new HashSet<>();

		// Walk selected library type users and collect all used libraries
		for (TypeUser user : getDescendants_TypeUsers()) {
			TypeProvider provider = user.getAssignedType();
			if (provider != null && provider.getLibrary() != null && !provider.getLibrary().isBuiltIn()) {
				// if (!usedLibs.contains(provider.getLibrary().getHead())) {
				// LOGGER.debug("Added " + provider + " in " + provider.getLibrary().getHead());
				// LibraryNode lib = provider.getLibrary();
				// }
				usedLibs.add(provider.getLibrary().getHead());// returns lib if unmanaged
			}
		}
		// Walk selected library extension owner and collect all used libraries
		for (ExtensionOwner owner : getDescendants_ExtensionOwners()) {
			Node base = owner.getExtensionBase();
			if (base != null && base.getLibrary() != null && !base.getLibrary().isBuiltIn())
				usedLibs.add(base.getLibrary().getHead());// returns lib if unmanaged
		}
		// Walk selected library contextual facets and collect all used libraries
		for (ContextualFacetNode cf : getDescendants_ContextualFacets()) {
			if (cf == null || cf.getWhereContributed() == null)
				continue;
			Node provider = (Node) cf.getWhereContributed().getOwningComponent();
			if (provider != null && provider.getLibrary() != null && !provider.getLibrary().isBuiltIn())
				usedLibs.add(provider.getLibrary().getHead());// returns lib if unmanaged
		}

		// Don't match any library in this chain.
		if (this.getChain() != null)
			usedLibs.removeAll(this.getChain().getLibraries());
		usedLibs.remove(this);
		return new ArrayList<>(usedLibs);
	}

	/**
	 * Get all type providers within library. Includes simple and complex objects only. Does NOT return any
	 * local-anonymous types. // FIXME - this method also is in Node. One in node does not include services.
	 * 
	 * @see Node.getDescendants_LibraryMembers()
	 * @return
	 */
	@Deprecated
	public List<Node> getDescendentsNamedTypes() {
		return getDescendants_LibraryMembersAsNodes();
	}

	/**
	 * Is the library ready to version? True if it is it managed and valid.
	 */
	public boolean isReadyToVersion() {
		// LOGGER.debug("Ready to version? valid: " + isValid() + ", managed: " + isManaged());
		return isManaged() && isValid();
	}

	/** ***************************** Library Status ************************* **/

	/**
	 * Get the editing status of the library.
	 */
	@Override
	public NodeEditStatus getEditStatus() {
		NodeEditStatus status = NodeEditStatus.FULL;
		if (GeneralPreferencePage.areNamespacesManaged() && !isInProjectNS())
			status = NodeEditStatus.NOT_EDITABLE;
		else if (isManaged()) {
			if (!isLocked())
				status = NodeEditStatus.MANAGED_READONLY;
			else if (isMajorVersion())
				status = NodeEditStatus.FULL;
			else if (isMinorOrMajorVersion())
				status = NodeEditStatus.MINOR;
			else
				status = NodeEditStatus.PATCH;
		}
		return status;
	}

	public boolean isFinal() {
		return getTLLibrary() == null ? false : getTLLibrary().getStatus() == TLLibraryStatus.FINAL;
	}

	/**
	 * @return true if this library is managed in a repository.
	 */
	public boolean isManaged() {
		if (isBuiltIn())
			return false;
		return projectItem != null && !projectItem.getState().equals(RepositoryItemState.UNMANAGED);
	}

	/**
	 * @return true if this library is managed and locked in a repository.
	 */
	public boolean isLocked() {
		return projectItem != null && (projectItem.getState().equals(RepositoryItemState.MANAGED_LOCKED)
				|| projectItem.getState().equals(RepositoryItemState.MANAGED_WIP));
	}

	/**
	 * @return true if this library's namespace is within the project's namespace.
	 */
	public boolean isInProjectNS() {
		return getModelNode().isInProjectNS(getNamespace());
	}

	/**
	 * Check the namespace and return true if patch and minor values are 0.
	 * 
	 * @return true if this library is a major version
	 */
	public boolean isMajorVersion() {
		return nsHandler.getNS_Minor(getNamespace()).equals("0") && nsHandler.getNS_Patch(getNamespace()).equals("0");
	}

	/**
	 * NOTE: Major versions will also return true as needed to create a minor from a major version.
	 * 
	 * @return true if this library is a major or minor version
	 */
	public boolean isMinorOrMajorVersion() {
		return nsHandler.getNS_Patch(getNamespace()).equals("0");
	}

	/**
	 * Use library namespace to determine if it is a minor version.
	 * 
	 * @return true only if this library is a minor version
	 */
	// FIXME - broken
	public boolean isMinorVersion() {
		return !nsHandler.getNS_Minor(getNamespace()).equals("0") && nsHandler.getNS_Patch(getNamespace()).equals("0");
	}

	/**
	 * @return true if this library is a patch version
	 */
	public boolean isPatchVersion() {
		return !nsHandler.getNS_Patch(getNamespace()).equals("0");
	}

	@Override
	public String toString() {
		return getLabel();
	}

	/**
	 * Replace assignments to all extension owners in the list with a type from this library with the same name.
	 * <p>
	 * for each extensionToUpdate.setExtension(ownerFromThisLibrary)
	 * 
	 * @param extensionsToUpdate
	 *            are the superTypes extended from base types in this library.
	 */
	public void replaceAllExtensions(List<ExtensionOwner> extensionsToUpdate) {
		if (extensionsToUpdate == null || extensionsToUpdate.isEmpty())
			return;

		// Create a map of all type providers in this library keyed by name
		Map<String, ExtensionOwner> candidates = new HashMap<>();
		for (ExtensionOwner p : this.getDescendants_ExtensionOwners())
			candidates.put(((Node) p).getName(), p);

		for (ExtensionOwner e : extensionsToUpdate) {
			e.setExtension((Node) candidates.get(e.getExtendsTypeName()));
		}
	}

	/**
	 * Replace assignments to all users in the list with a type from this library with the same name.
	 * 
	 * @param users
	 *            - list of type users to replace assigned types with types from this library
	 */
	public void replaceAllUsers(List<TypeUser> users) {
		if (users == null || users.isEmpty())
			return;

		LOGGER.debug("Replacing type users to use types from " + this + " library.");
		// Create a map of all type providers in this library keyed by name
		Map<String, TypeProvider> candidates = new HashMap<>();
		for (TypeProvider p : this.getDescendants_TypeProviders())
			candidates.put(p.getName(), p);

		for (TypeUser user : users) {
			TypeProvider replacement = candidates.get(user.getAssignedType().getName());
			if (replacement != null) {
				user.setAssignedType(replacement);
				LOGGER.debug("assigned type " + replacement + " to " + user);
			}
		}
	}

	/**
	 * Replace contextual facet contribution assignments to all contextual facets in the list with an owner from this
	 * library with the same name.
	 */
	public void replaceAllContributors(List<ContextualFacetNode> cfs) {
		if (cfs == null || cfs.isEmpty())
			return;
		LOGGER.debug("Replacing contextual facet to contribute to owners from " + this + " library.");
		// Create a map of all type providers in this library keyed by name
		Map<String, ContextualFacetOwnerInterface> candidates = new HashMap<>();
		for (ContextualFacetOwnerInterface p : this.getDescendants_ContextualFacetOwners())
			candidates.put(((Node) p).getName(), p);

		for (ContextualFacetNode cf : cfs) {
			cf.setOwner(candidates.get(cf.getWhereContributed().getOwningComponent().getName()));
			LOGGER.debug("Set owner of " + cf + " to " + cf.getWhereContributed().getOwningComponent());
		}
	}

	/**
	 * Replace assignments to all extension owners and type users in this library using the passed map. The map must
	 * contain key/value pairs of library nodes where the currently used library is the key. No action taken on types in
	 * libraries not in the map.
	 */
	@Deprecated
	public void replaceAllUsers(HashMap<LibraryNode, LibraryNode> replacementMap) {
		replaceTypeUsers(replacementMap);
		replaceExtensionUsers(replacementMap);
	}

	/**
	 * Replace all type users in this library using the passed map. The map must contain key/value pairs of library
	 * nodes where the currently used library is the key. No action taken on types in libraries not in the map.
	 */
	@Deprecated
	public void replaceTypeUsers(HashMap<LibraryNode, LibraryNode> replacementMap) {
		TypeProvider provider = null;
		for (TypeUser user : getDescendants_TypeUsers()) {
			// Get the replacement library from the map using the assigned library as the key
			LibraryNode replacementLib = replacementMap.get(user.getAssignedType().getLibrary());
			if (user.isEditable() && replacementLib != null) {
				// Find a type provider in new library with the same name as existing assigned type
				provider = replacementLib.findTypeProvider(user.getAssignedType().getName());
				if (provider != null)
					// If found, set the new provider as the assigned type
					user.setAssignedType(provider); // don't set to null as null clears assignment
			}
		}
	}

	/**
	 * Replace all Extension users in this library using the passed map. The map must contain key/value pairs of library
	 * nodes where the currently used library is the key. No action taken on types in libraries not in the map.
	 */
	@Deprecated
	public void replaceExtensionUsers(HashMap<LibraryNode, LibraryNode> replacementMap) {
		Node provider = null;
		for (ExtensionOwner owner : getDescendants_ExtensionOwners()) {
			// Skip owners that do not extend another object or are not editable.
			if (owner.getExtensionBase() == null || !((INode) owner).isEditable())
				continue;

			// Get the replacement library from the map using the assigned library as the key
			LibraryNode replacementLib = replacementMap.get(owner.getExtensionBase().getLibrary());
			if (replacementLib != null) {
				// Find a type provider in new library with the same name as existing assigned type
				provider = (Node) replacementLib.findTypeProvider(owner.getExtensionBase().getName());
				// If found, set the new provider as the assigned type
				if (provider != null)
					owner.setExtension(provider); // don't set to null as null clears assignment
			}
		}
	}

	/**
	 * Add this service to the library. Assures only one service. Deletes existing service if any. Assure TL library has
	 * this service. Assure service has a name.
	 */
	public void addServiceNode(ServiceNode serviceNode) {

		// Remove service from previous library if any
		removeMember(serviceNode);

		// Make sure the library only has one service.
		if (getService() != null)
			getService().delete();
		assert getService() == null;

		if (getTLModelObject() instanceof TLLibrary)
			if (((TLLibrary) getTLModelObject()).getService() != serviceNode.getTLModelObject())
				((TLLibrary) getTLModelObject()).setService(serviceNode.getTLModelObject());

		if (serviceNode.getName() == null || serviceNode.getName().isEmpty())
			serviceNode.setName(getName() + "_Service");

		serviceNode.setParent(this);
		serviceNode.setLibrary(this);
		getServiceRoot().add(serviceNode);

		// If a chain, add to chain aggregate.
		if (isInChain())
			getChain().add(serviceNode);
	}

	public void setAsDefault() {
		this.getProject().getTLProject().setDefaultItem(getProjectItem());
	}

	@Override
	public LibraryNavNode getLibraryNavNode() {
		if (getChain() != null)
			return getChain().getLibraryNavNode();
		return getParent() instanceof LibraryNavNode ? (LibraryNavNode) getParent() : null;
	}

	/**
	 * Check all members to see if this is a member. Does not examine facets and properties.
	 * 
	 * @param member
	 * @return true if the member is a member of this library.
	 */
	@Override
	public boolean contains(Node member) {
		// FIXME - make this param a library member without making contains ambiguous
		if (!(member instanceof LibraryMemberInterface))
			return false;
		return getChildrenHandler().contains(member);
	}

	/**
	 * @return this library if unmanaged or the head of the chain for managed libraries
	 */
	public LibraryNode getHead() {
		if (isManaged())
			return getChain().getHead();
		return this;
	}

}

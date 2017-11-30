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
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryMember;
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
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
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
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.handlers.NamespaceHandler;
import org.opentravel.schemas.node.handlers.children.LibraryChildrenHandler;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.LibraryNodeListener;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.preferences.GeneralPreferencePage;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereUsedLibraryHandler;
import org.opentravel.schemas.types.whereused.LibraryUsesNode;
import org.opentravel.schemas.types.whereused.TypeUserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The LibraryNode class manages an internal navigation oriented node a library model class. Libraries are model classes
 * that contain named members representing global types and elements from either schemas (XSD), built-in-types or OTA2
 * model components.
 */

public class LibraryNode extends Node implements LibraryInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryNode.class);

	protected static final String DEFAULT_LIBRARY_TYPE = "Library";
	protected static final String XSD_LIBRARY_TYPE = "XSD Library";
	protected static final String TempLib = "TemporaryLibrary";

	public static final String COMPLEX_OBJECTS = "Complex Objects";
	public static final String SIMPLE_OBJECTS = "Simple Objects";
	public static final String RESOURCES = "Resources";
	public static final String ELEMENTS = "Elements";

	protected NavNode complexRoot;
	protected NavNode simpleRoot;
	protected NavNode elementRoot;
	protected ServiceNode serviceRoot;
	protected NavNode resourceRoot;

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
	// FIXME - change wizard to use a libraryNavNode then delete this
	public LibraryNode(ProjectNode pn) {
		super();
		assert (pn instanceof ProjectNode);

		// setLibrary(this);
		absTLLibrary = new TLLibrary();
		ListenerFactory.setIdentityListner(this);
		setParent(pn);
		if (pn.getChildren() != null && pn.getChildren().contains(this))
			pn.getChildren().add(this);
		// getParent().linkLibrary(this);

		this.setName("");
		nsHandler = NamespaceHandler.getNamespaceHandler(pn);
		this.setNamespace(pn.getNamespace());
		// LOGGER.debug("Created empty library without underlying model");
		// TODO - why no listener?
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
	 * 
	 * Note - caller must set namespace and prefix.
	 * 
	 * @see {@link org.opentravel.schemas.controllers.DefaultRepositoryController#createMajorVersion(LibraryNode)}
	 * 
	 * @param alib
	 * @param parent
	 *            project to link to with the new LibraryNavNode
	 */
	public LibraryNode(final AbstractLibrary alib, final ProjectNode parent) {
		super(alib.getName());
		if (parent == null)
			throw new IllegalArgumentException("Parent project must not be null.");
		// LOGGER.debug("Begin creating new library: " + alib.getName() + " in " + parent);

		setLibrary(this);
		absTLLibrary = alib;

		// Parent is a new library nav node
		setParent(new LibraryNavNode(this, parent));

		// Register the library
		getModelNode().getLibraryManager().add(this);

		// Set the Project Item, add to project if not already a member
		setProjectItem(getProject().addToTL(alib));

		initLibrary(alib);

		// LOGGER.debug("Library created: " + this.getName());
	}

	public LibraryNode(final AbstractLibrary alib, final VersionAggregateNode parent) {
		super(alib.getName());
		// LOGGER.debug("Begin creating new library: " + alib.getPrefix() + ":" + alib.getName() + " in aggregate "
		// + parent.getParent());
		assert (parent != null);

		setLibrary(this);
		absTLLibrary = alib;
		setParent(parent);
		((VersionAggregateNode) getParent()).add(this);
		// getParent().linkLibrary(this);

		assert (getProject() != null);
		assert (getProject().getTLProject() != null);

		// Set the Project Item, add to project if not already a member
		setProjectItem(getProject().addToTL(alib));

		initLibrary(alib);

		// LOGGER.debug("Library created: " + this.getName());
	}

	private void initLibrary(final AbstractLibrary alib) {
		nsHandler = NamespaceHandler.getNamespaceHandler(this);
		nsHandler.registerLibrary(this);

		// modelObject = ModelObjectFactory.newModelObject(alib, this);

		// complexRoot = new NavNode(COMPLEX_OBJECTS, this);
		// simpleRoot = new NavNode(SIMPLE_OBJECTS, this);
		// resourceRoot = new NavNode(RESOURCES, this);

		// Let the tools edit the library during construction.
		setEditable(true);
		// Create Listener
		ListenerFactory.setIdentityListner(this);

		// // Process all the children
		// generateModel(alib);

		if ((alib instanceof XSDLibrary) || (alib instanceof BuiltInLibrary))
			if (genTLLib == null)
				makeGeneratedComponentLibrary(alib);

		childrenHandler = new LibraryChildrenHandler(this);
		for (Node item : childrenHandler.get())
			if (item instanceof NavNode)
				if (((NavNode) item).getName().equals(COMPLEX_OBJECTS))
					complexRoot = (NavNode) item;
				else if (((NavNode) item).getName().equals(SIMPLE_OBJECTS))
					simpleRoot = (NavNode) item;
				else if (((NavNode) item).getName().equals(RESOURCES))
					resourceRoot = (NavNode) item;

		// Set up the contexts
		addContexts();

		// Save edit state: Test to see if this is an editable library.
		updateLibraryStatus();
	}

	// private void addListeners() {
	// ListenerFactory.setListner(this);
	// }
	//
	// private void removeListeners() {
	// ListenerFactory.clearListners(this);
	// }

	// org.opentravel.schemas.controllers.DefaultProjectController.add(LibraryNode, AbstractLibrary)
	// org.opentravel.schemas.node.LibraryChainNode.add(ProjectItem)
	public LibraryNode(ProjectItem pi, LibraryChainNode chain) {
		this(pi.getContent(), chain.getVersions());
		for (Node members : getDescendants_LibraryMembers()) {
			if (members instanceof ComponentNode)
				chain.add((ComponentNode) members);
		}
		chain.add(getService());

		// Do NOT add resource here. It is done in addMember().
		// TODO - fix service to match resource (or vice versa)
		// for (ResourceNode r : getResources())
		// chain.add(r);

		projectItem = pi;

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
	 * @param true to enable edits on this library
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
					if (getServiceRoot() == null || serviceRoot.isEmpty())
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
		ArrayList<Node> imported = new ArrayList<Node>();
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
			sourceNode.replaceTypesWith(entry.getValue(), scopeLib);
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
		final Map<Node, Node> sourceToNewMap = new LinkedHashMap<Node, Node>(sourceList.size());

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

		// LibraryNode oldLib = source.getLibrary();
		// New contextual facets are also created
		// Don't use ContextUtils because may create new contexts in the target library
		LibraryMemberInterface newNode = NodeFactory.newLibraryMember((LibraryMember) source.cloneTLObj());
		if (newNode == null) {
			// LOGGER.warn("Could not clone " + source + " a " + source.getClass().getSimpleName());
			return null;
		}

		// 3/23/2017 - simplified context handling to just set to new library default.
		for (Node child : ((Node) newNode).getDescendants())
			if (child.getTLModelObject() instanceof TLContextReferrer)
				((TLContextReferrer) child.getTLModelObject()).setContext(getDefaultContextId());
		assert getTLLibrary().getContexts().size() == 1;

		// Clone will duplicate the contextual facets so also import them.
		// Test coverage: ChoiceObjectTests.CH_ImportAndCopyTests()
		if (newNode instanceof ContextualFacetOwnerInterface) {
			// Move the new facets
			for (ContextualFacetNode cf : ((ContextualFacetOwnerInterface) newNode).getContextualFacets()) {
				LOGGER.debug("Moving " + cf + " from library " + cf.getLibrary() + " to " + this);
				addMember(cf); // don't leave the facets behind in the source lib
				assert cf.getTLModelObject() != null;
				assert Node.GetNode(cf.getTLModelObject()) == cf;
			}
			// If it is a contextual facet, set its where contributed.
			ContextualFacetOwnerInterface owner = null;
			if (newNode instanceof ContextualFacetNode && source instanceof ContextualFacetNode) {
				ContributedFacetNode cf = ((ContextualFacetNode) source).getWhereContributed();
				if (cf != null && cf.getOwningComponent() instanceof ContextualFacetOwnerInterface)
					owner = (ContextualFacetOwnerInterface) cf.getOwningComponent();
				if (owner != null)
					((ContextualFacetNode) newNode).setOwner(owner);
				// Also set the library for all children (bug patch)
				// for (Node child : newNode.getChildren())
				// child.setLibrary(newNode.getLibrary());
			}
		}
		addMember(newNode);

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
		// if ((source.getTLModelObject() == null) || !(source.getTLModelObject() instanceof TLLibraryMember)) {
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
		if (!(getTLaLib() instanceof TLLibrary)) {
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
		for (Node n : getDescendants_LibraryMembers()) {
			n.mergeContext(tlc.getContextId());
		}

		// Now remove the unused contexts
		List<TLContext> contexts = new ArrayList<TLContext>(getTLLibrary().getContexts());
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

	// /**
	// * Use child manager to add Node to the appropriate navigation node in this library.
	// * <p>
	// * Does set library and contexts. Does <b>not</b> impact the TL model. Does <b>not</b> do aggregate processing.
	// *
	// * @return
	// */
	// @Deprecated
	// public boolean linkMember(Node n) {
	// if (n == null)
	// throw new IllegalArgumentException("Null parameter.");
	// if (getChildrenHandler() == null)
	// throw new IllegalStateException("Null children handler.");
	//
	// getChildrenHandler().add(n);
	// return true;

	// if (n.getName().isEmpty())
	// throw new IllegalArgumentException("Node must have a name.");
	// boolean linkOK = true;

	// LOGGER.debug("Linking node: "+n.getName());
	// if (n instanceof ComplexComponentInterface)
	// linkOK = complexRoot.linkChild(n);
	// else if (n instanceof ContextualFacetNode)
	// linkOK = complexRoot.linkChild(n); // unreachable - contextual is complexComponent
	// else if (n instanceof SimpleComponentInterface)
	// linkOK = simpleRoot.linkChild(n);
	// else if (n instanceof ResourceNode) {
	// if (!getResourceRoot().getChildren().contains(n))
	// getResourceRoot().getChildren().add(n);
	// n.setParent(getResourceRoot());

	// if (n instanceof ServiceNode)
	// return linkOK; // Nothing to do because services are already linked to library.
	// else if (n.isXsdElementAssignable())
	// TODO - i don't think this is ever reached. ElementRoot is never accessed.
	// linkOK = elementRoot.linkChild(n.getXsdNode());
	// else
	// LOGGER.error("linkMember is trying to add unknown object type: " + n + ":" + n.getClass().getSimpleName());
	// I don't know why but only service node creates stack overflow.
	// Services can't be moved, so they will never have to change their lib.
	// if (linkOK) {
	// if (!(n instanceof ServiceNode)) {
	// n.setLibrary(this);
	// n.setKidsLibrary();
	// }
	// addContext(n);
	// }

	// if (n instanceof ServiceNode)
	// return linkOK; // Nothing to do because services are already linked to library.
	// else if (n.isXsdElementAssignable())
	// // TODO - i don't think this is ever reached. ElementRoot is never accessed.
	// assert false;
	//
	// NavNode owner = null;
	// if (n instanceof ComplexComponentInterface)
	// owner = getComplexRoot();
	// else if (n instanceof SimpleComponentInterface)
	// owner = getSimpleRoot();
	// else if (n instanceof ResourceNode)
	// owner = getResourceRoot();
	//
	// if (owner != null) {
	// owner.add((LibraryMemberInterface) n);
	// n.setParent(owner);
	// n.setLibrary(this);
	// addContext(n);
	// }
	// return linkOK;
	// }

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

		pn.getTLProject().getProjectManager().lock(getProjectItem(), dir);
		// LOGGER.debug("Locked library which created local file: " + path);
		setEditable(isAbsLibEditable());
	}

	// /**
	// * Use the generator appropriate to the library type. Gets each member of the abstract library and creates the
	// * appropriate nodes and links them to the library.
	// *
	// * NOTE - resolveTypes must be used to set type nodes and owners.
	// *
	// * @param alib
	// * - abstract library from TL model
	// */
	// protected void generateModel(final AbstractLibrary alib) {
	// // LOGGER.debug("Generating library " + alib.getName() + ".");
	// if (alib instanceof XSDLibrary)
	// generateLibrary((XSDLibrary) alib);
	// else if (alib instanceof BuiltInLibrary)
	// generateLibrary((BuiltInLibrary) alib);
	// else if (alib instanceof TLLibrary)
	// generateLibrary((TLLibrary) alib);
	// // LOGGER.debug("Done generating library " + alib.getName() + ".");
	// }

	// private void generateLibrary(final BuiltInLibrary biLib) {
	// Node n;
	// XsdNode xn;
	// // boolean hasXsd = false;
	// // elementRoot = new NavNode(ELEMENTS, this);
	// if (genTLLib == null)
	// makeGeneratedComponentLibrary(biLib);
	// for (final LibraryMember mbr : biLib.getNamedMembers()) {
	// n = GetNode(mbr);
	// // if ((mbr instanceof XSDSimpleType) || (mbr instanceof XSDComplexType) || (mbr instanceof XSDElement)) {
	// // hasXsd = true;
	// // If the mbr has not been modeled, create node for the xsd member
	// if (n == null) {
	// // if (mbr instanceof XSDSimpleType)
	// // linkMember(new XsdObjectHandler((XSDSimpleType) mbr, this).getOwner());
	// // else if (mbr instanceof XSDComplexType)
	// // linkMember(new XsdObjectHandler((XSDComplexType) mbr, this).getOwner());
	// // else if (mbr instanceof XSDElement)
	// // linkMember(new XsdObjectHandler((XSDElement) mbr, this).getOwner());
	// if (mbr instanceof XSDSimpleType)
	// n = (Node) new XsdObjectHandler((XSDSimpleType) mbr, this).getOwner();
	// else if (mbr instanceof XSDComplexType)
	// n = (Node) new XsdObjectHandler((XSDComplexType) mbr, this).getOwner();
	// else if (mbr instanceof XSDElement)
	// n = (Node) new XsdObjectHandler((XSDElement) mbr, this).getOwner();
	// else
	// n = NodeFactory.newLibraryMember((TLLibraryMember) mbr);
	//
	// if (n != null)
	// linkMember(n);
	// else
	// // Happens when extension point and its variants are loaded but the tl element is incomplete
	// LOGGER.debug("Null node from xsd handler.");
	//
	// // n.setLibrary(this);
	//
	// // xn = new XsdNode((TLLibraryMember) mbr, this);
	// // n = xn.getOtmModel();
	// // xn.setXsdType(true);
	// }
	// // if (n == null)
	// // continue;
	// // n.setXsdType(true); // TESTME - may be null
	// // } else if (n == null)
	// // n = NodeFactory.newComponent_UnTyped((TLLibraryMember) mbr);
	// // linkMember(n);
	// // n.setLibrary(this);
	// }
	// // if (!hasXsd)
	// // elementRoot = null;
	// }

	// private void generateLibrary(final XSDLibrary xLib) {
	// // elementRoot = new NavNode(ELEMENTS, this);
	// if (genTLLib == null)
	// makeGeneratedComponentLibrary(xLib);
	// for (final LibraryMember mbr : xLib.getNamedMembers()) {
	// Node n = GetNode(mbr); // use node if member is already modeled.
	// if (n == null) {
	// if (mbr instanceof XSDSimpleType)
	// n = (Node) new XsdObjectHandler((XSDSimpleType) mbr, this).getOwner();
	// else if (mbr instanceof XSDComplexType)
	// n = (Node) new XsdObjectHandler((XSDComplexType) mbr, this).getOwner();
	// else if (mbr instanceof XSDElement)
	// n = (Node) new XsdObjectHandler((XSDElement) mbr, this).getOwner();
	//
	// // final XsdNode xn = new XsdNode((TLLibraryMember) mbr, this);
	// // n = xn.getOtmModel();
	// // xn.setXsdType(true);
	// // if (n != null)
	// // n.setXsdType(true); // FIXME
	// // else
	// // LOGGER.debug("ERROR - null otm node.");
	// }
	// // else
	// // LOGGER.debug("Used listener to get: " + n.getNameWithPrefix());
	// if (n != null) {
	// linkMember(n);
	// // n.setLibrary(this);
	// }
	// }
	// }

	// private void generateLibrary(final TLLibrary tlLib) {
	// // LOGGER.debug("Generating Library: " + tlLib.getName());
	//
	// // When contextual facets can be library members (version 1.6 and later, model them first
	// // Contextual facets will be processed twice. Once here to create library member and once in addMOChildren()
	// // The same TLContextual facet will be in both the contextual facet and contributed facet and the listener will
	// // be used to link them.
	// if (OTM16Upgrade.otm16Enabled)
	// for (final TLContextualFacet cf : tlLib.getContextualFacetTypes()) {
	// // LOGGER.debug("Generating contextual facet: " + cf.getLocalName());
	// Node n = GetNode(cf);
	// if (n == null)
	// n = NodeFactory.newObjectNode(cf, this);
	// assert (getDescendants_LibraryMembers().contains(n));
	// }
	//
	// for (final LibraryMember mbr : tlLib.getNamedMembers()) {
	// // Skip members that are in a different library than their owner
	// if (mbr instanceof TLContextualFacet)
	// // if (!((TLContextualFacet) mbr).isLocalFacet())
	// continue; // Model in its own library.
	//
	// // LOGGER.debug("Generating named member: " + mbr.getLocalName());
	// ComponentNode existingNode = (ComponentNode) GetNode(mbr);
	// if (mbr instanceof TLService) {
	// if (existingNode instanceof ServiceNode)
	// ((ServiceNode) existingNode).link((TLService) mbr, this);
	// else
	// new ServiceNode((TLService) mbr, this);
	// } else if (mbr instanceof TLResource)
	// if (existingNode instanceof ResourceNode) {
	// existingNode.getLibrary().remove(existingNode);
	// this.linkMember(existingNode);
	// } else
	// new ResourceNode((TLResource) mbr, this);
	// else {
	// // If the tlLib already has nodes associated, use those nodes; Otherwise create new ones.
	// if (existingNode == null)
	// existingNode = NodeFactory.newObjectNode((LibraryMember) mbr, this);
	// }
	// }
	// assert checkListeners();
	// }

	// /**
	// * Test method - assert all members have correct identity listener
	// */
	// private boolean checkListeners() {
	// for (Node n : getDescendants_LibraryMembers())
	// assert n == Node.GetNode(n.getTLModelObject()) : "Missing or incorrect identity listener assigned to " + n;
	// return true;
	// }

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

	// @Override
	// public List<Node> getChildren() {
	// if (getChildrenHandler() == null)
	// return Collections.emptyList();
	// return getChildrenHandler().get();
	// }

	/**
	 * Add the context of the node to this TL library if it does not already exist. For XSD Nodes, the namespace and
	 * prefix are used. For all other nodes, any context values they contain are copied.
	 */
	public void addContext(final Node n) {
		if (getTLLibrary() == null)
			return;
		if (n.isXsdType()) {
			if (getTLLibrary().getContextByApplicationContext(n.getNamespace()) == null) {
				final TLContext ctx = new TLContext();
				ctx.setApplicationContext(n.getNamespace());
				ctx.setContextId(n.getPrefix());
				if (ctx.getContextId().isEmpty())
					ctx.setContextId("Imported");
				getTLLibrary().addContext(ctx);
			}
		} else {
			List<TLContext> ctxList = n.getUsedContexts();
			if (ctxList == null)
				return;
			for (TLContext ctx : ctxList) {
				if (getTLLibrary().getContextByApplicationContext(ctx.getApplicationContext()) == null) {
					final TLContext nctx = new TLContext();
					nctx.setApplicationContext(ctx.getApplicationContext());
					nctx.setContextId(ctx.getContextId());
					getTLLibrary().addContext(nctx);
				}
			}
		}
	}

	/**
	 * Add node to this library. Links to library's complex/simple or element root. Adds underlying the TL object to
	 * this library's TLModel library. Removes from existing library if already in a library. Handles adding nodes to
	 * chains. Adds context to the TL Model library if needed. Does not change type assignments.
	 * <p>
	 * Add to tlLibrary.addNamedMember() <br>
	 * linkMember() <br>
	 * getChain.add()
	 * 
	 * @param lm
	 *            node to add to this library
	 */
	// @Deprecated
	// public void addMember(final Node n) {
	// if (n instanceof LibraryMemberInterface)
	// addMember((LibraryMemberInterface) n);
	// }

	public void addMember(final LibraryMemberInterface lm) {
		// If it doesn't have a children handler yet, it is doing the handler constructor.
		// The constructor will add the member.
		if (getChildrenHandler() == null) {
			LOGGER.debug("Missing library children handler.");
			return;
		}
		assert getChildrenHandler() != null;
		assert lm.getTLModelObject() != null;
		assert lm.getTLModelObject() instanceof LibraryMember;

		if (!isEditable() && !(lm instanceof InheritedInterface)) {
			LOGGER.warn("Tried to addMember() " + lm + " to non-editable library " + this);
			return;
		}

		// Remove from Old library if any
		LibraryNode oldLib = lm.getLibrary();
		if (oldLib != null && oldLib != this) {
			oldLib.removeMember((Node) lm);
			assert !oldLib.contains((Node) lm);
		}

		if (this.contains((Node) lm))
			return; // early exit - already a member

		if (!(lm instanceof InheritedInterface))
			getTLLibrary().addNamedMember((LibraryMember) lm.getTLModelObject());

		getChildrenHandler().add(lm);

		assert this.contains((Node) lm);

		// If the TL object has contextual facets, make sure they are modeled and add to this library if not
		if (lm.getChildrenHandler() != null)
			for (TLModelElement tlcf : lm.getChildrenHandler().getChildren_TL())
				if (tlcf instanceof TLContextualFacet && Node.GetNode(tlcf) == null)
					addMember(NodeFactory.newLibraryMember((LibraryMember) tlcf));
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
	 * Walk all members and close them.
	 * 
	 * <b>WARNING:</b> this simply closes the library and <b>not</b> the underlying TL library. The library may be in
	 * multiple projects.
	 * 
	 * @see To remove a library from a project use ProjectController.remove(LibraryNavNode)
	 * 
	 *      {@link #LibraryModelManager}
	 */
	// /**
	// * If this library's parent is a LibraryNavNode the close it. If it is in a chain, close the chain. Otherwise,
	// walk
	// * all members and close them.
	// *
	// * <b>WARNING:</b> this simply closes the library and <b>not</b> the underlying TL library. The library may be in
	// * multiple projects.
	// *
	// * @see To remove a library from a project use ProjectController.remove(LibraryNavNode)
	// *
	// * {@link #LibraryModelManager}
	// */
	@Override
	public void close() {
		LOGGER.debug("Closing " + getNameWithPrefix());

		if (getProjectItem() != null)
			ListenerFactory.clearListners(getProjectItem().getContent());

		for (Node n : getChildren_New())
			n.close();
		setParent(null); // redundent
		deleted = true;
		// }
		// if (!isEmpty())
		// LOGGER.debug("Closed library " + this + " is not empty.");

		// assert (isEmpty());
		assert (deleted);
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
	 */
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
	// FIXME - only used in changeNode in main controller
	@Deprecated
	public void moveMember(final Node mbr, LibraryNode destination) throws IllegalArgumentException {
		// Pre-tests
		if (mbr == null || mbr.getTLModelObject() == null)
			throw new IllegalArgumentException("Null in move member.");
		if (mbr instanceof ServiceNode)
			throw new IllegalArgumentException("Services can not be moved.");
		if (!(mbr instanceof LibraryMemberInterface))
			throw new IllegalArgumentException(mbr + " is not a library member.");
		if (mbr.getLibrary() != this)
			throw new IllegalArgumentException("Internal error - source of move is in wrong library.");
		if (!(mbr.getTLModelObject() instanceof TLLibraryMember))
			throw new IllegalArgumentException("Model object is not a library member.");
		if (getTLModelObject().getNamedMember(((TLLibraryMember) mbr.getTLModelObject()).getLocalName()) == null)
			throw new IllegalArgumentException("Source library can not find object to move.");
		// destination pre-tests
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

		// // Do the move manually. Let listeners handle the nodes.
		// if (mbr instanceof ContextualFacetNode && !(mbr instanceof ContributedFacetNode)) {
		// TLContextualFacet tlSource = ((ContextualFacetNode) mbr).getTLModelObject();
		// tlSource.getOwningLibrary().removeNamedMember(tlSource);
		// destination.getTLModelObject().addNamedMember(tlSource);
		// } else
		// // Move the TL object to destination tl library.
		// try {
		// TLLibrary srcLib = (TLLibrary) getTLModelObject();
		// TLLibrary destLib = (TLLibrary) destination.getTLModelObject();
		// TLLibraryMember tlMbr = (TLLibraryMember) mbr.getTLModelObject();
		// srcLib.moveNamedMember(tlMbr, destLib);
		// } catch (Exception e) {
		// // Failed to move.
		// throw new IllegalArgumentException("Internal Error - " + e.getLocalizedMessage());
		// }

		destination.collapseContexts(); // reduce down to one context
		assert !this.contains(mbr);
		assert destination.contains(mbr);
		return;
	}

	/**
	 * Delete this library.
	 */

	/**
	 * Remove the node from its library. and from the underlying tl library.
	 * <p>
	 * Does <b>not</b> delete the node or its TL Object contents. TL type assignments are assured to match the
	 * assignments in TypeNode.
	 * <p>
	 * NOTE: does not replace this node with an earlier version in a version chain.
	 * 
	 */
	public void removeMember(final Node n) {
		if (!(n.getTLModelObject() instanceof LibraryMember))
			return;
		if (isBuiltIn())
			return;

		// Nov 18, 2017 - workaround for compiler bug in tlBusinessObject
		if (n instanceof BusinessObjectNode)
			for (ContextualFacetNode cf : ((BusinessObjectNode) n).getContextualFacets())
				if (!cf.canBeLibraryMember())
					cf.delete();

		if (getChildrenHandler() != null) {
			n.getLibrary().getTLModelObject().removeNamedMember((LibraryMember) n.getTLModelObject());
			// getChildrenHandler().remove(n); // done in listener
		} else {
			assert false;
			// if (n == null || n.getTLModelObject() == null) {
			// // LOGGER.warn("LibraryNode:removeMember() - error. model object or tl model object is null. " +
			// // n.getName()
			// // + " - " + n.getClass().getSimpleName());
			// return;
			// }
			// if (!(n.getTLModelObject() instanceof TLLibraryMember)) {
			// // LOGGER.warn("Tried to remove non-TLLibraryMember: " + n);
			// return;
			// }
			//
			// if (n.getParent() != null)
			// n.unlinkNode();
			// n.getLibrary().getTLLibrary().removeNamedMember((TLLibraryMember) n.getTLModelObject());
			// n.setLibrary(null);
			// // n.fixAssignments();
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

	public NavNode getSimpleRoot() {
		return simpleRoot;
	}

	@Override
	public LibraryChildrenHandler getChildrenHandler() {
		return (LibraryChildrenHandler) childrenHandler;
	}

	/**
	 * @return - Return the library's complex root node.
	 */
	public NavNode getComplexRoot() {
		return complexRoot;
	}

	public Node getServiceRoot() {
		return serviceRoot;
	}

	public NavNode getResourceRoot() {
		return resourceRoot;
	}

	@Override
	public String getComponentType() {
		return DEFAULT_LIBRARY_TYPE;
	}

	// @Override
	// public List<Node> getNavChildren(boolean deep) {
	// if (parent instanceof VersionAggregateNode)
	// return new ArrayList<Node>();
	// else
	// return new ArrayList<Node>(getChildren());
	// }

	// @Override
	// public List<Node> getTreeChildren(boolean deep) {
	// List<Node> treeKids = getNavChildren(deep);
	// if (!treeKids.isEmpty()) {
	// if (!treeKids.contains(getWhereUsedHandler().getWhereUsedNode()))
	// treeKids.add(getWhereUsedHandler().getWhereUsedNode());
	// if (!treeKids.contains(getWhereUsedHandler().getUsedByNode()))
	// treeKids.add(getWhereUsedHandler().getUsedByNode());
	// }
	// return treeKids;
	// }
	//
	// @Override
	// public boolean hasTreeChildren(boolean deep) {
	// return true; // include where used and uses from
	// }

	// FIXME - this should work but TLEmpty would have to extend abstract library
	// @Override
	// public AbstractLibrary getTLModelObject() {
	// return modelObject.getTLModelObj() instanceof TLEmpty ? null : (AbstractLibrary) modelObject.getTLModelObj();
	// }

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

	// @Override
	// public boolean hasNavChildren(boolean deep) {
	// if (parent instanceof VersionAggregateNode)
	// return false;
	// return !getChildren().isEmpty();
	// }

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
		boolean result = false;
		LibraryChainNode chain = getChain();
		if (chain == null) {
			// TODO - why not just check the service node?
			for (Node n : getChildren())
				if (n instanceof ServiceNode)
					result = true;
		} else
			result = !chain.getServiceAggregate().getChildren().isEmpty();
		return result;
	}

	private ServiceNode getService() {
		for (Node n : getChildren()) {
			if (n instanceof ServiceNode)
				return (ServiceNode) n;
		}
		return null;
	}

	// @Override
	// public boolean hasChildren_TypeProviders() {
	// return getChildren().size() > 0 ? true : false;
	// }
	//
	@Override
	public boolean isNavigation() {
		return true;
	}

	@Override
	public boolean isXSDSchema() {
		return getTLaLib() instanceof XSDLibrary;
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

	public String getVersion_Major() {
		String version = "";
		if (absTLLibrary != null && absTLLibrary instanceof TLLibrary)
			version = ((TLLibrary) absTLLibrary).getVersion();
		if (version.contains("."))
			version = version.substring(0, version.indexOf("."));
		return emptyIfNull(version);
	}

	public void setPath(final String text) {
		final File file = new File(text);
		final URL fileURL = URLUtils.toURL(file);
		// LOGGER.debug("File url being set to: "+fileURL);
		absTLLibrary.setLibraryUrl(fileURL);
	}

	/**
	 * NOTE - a library can have many project parents as it can belong to multiple projects. It can also be a library
	 * chain.
	 */
	@Override
	public Node getParent() {
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
	 * Return the project this library or its chain have as its parent. <b>Note</b> that libraries can belong to
	 * multiple projects.
	 * 
	 * @see {@link LibraryModelManager#isUsedElsewhere(LibraryInterface, ProjectNode)}
	 * @return parent project or null if no project is found.
	 */
	@Override
	public ProjectNode getProject() {
		ProjectNode pn = null;
		if (getParent() instanceof LibraryNavNode)
			pn = ((LibraryNavNode) getParent()).getProject();
		else if (getParent() instanceof ProjectNode)
			pn = (ProjectNode) getParent();
		else if (getParent() instanceof LibraryChainNode)
			pn = ((LibraryChainNode) getParent()).getProject();
		else if (getParent() instanceof VersionAggregateNode)
			pn = ((VersionAggregateNode) getParent()).getProject();
		return pn;
	}

	/**
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
		ArrayList<String> contexts = new ArrayList<String>();
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
		// return (AbstractLibrary) (modelObject != null ? modelObject.getTLModelObj() : null);
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
		// cn.xsdNode = xn;
		// cn.setLibrary(this);
		// cn.xsdType = true;
		// cn.local = true;
		// xn.otmModel = cn;

		// if (xn.getParent() != null) {
		// if (xn.getParent().getChildrenHandler() != null)
		// xn.getParent().getChildrenHandler().clear();
		// xn.unlinkNode();
		// LOGGER.debug("HUMM...why was this linked?");
		// }
		// linkMember(cn);
	}

	// /**
	// * Get all type providers within library. Includes simple and complex objects, aliases and facets. Does NOT return
	// * any local-anonymous types.
	// *
	// * @return
	// */
	// // FIXME - this method also is in Node
	// public List<Node> getDescendentsNamedTypeProviders() {
	// ArrayList<Node> namedTypeProviders = new ArrayList<Node>();
	// for (Node n : getChildren())
	// namedTypeProviders.addAll(gntp(n));
	// return namedTypeProviders;
	// }

	// private Collection<? extends Node> gntp(Node n) {
	// ArrayList<Node> lst = new ArrayList<Node>();
	// if (n.isNamedEntity() && !isLocal())
	// lst.add(n);
	// for (Node gc : n.getChildren())
	// lst.addAll(gntp(gc));
	// return lst;
	// }

	/**
	 * Get a new list of library members in this library. Version nodes return their actual object. Includes objects
	 * inside NavNodes.
	 * 
	 * @return
	 */
	public List<LibraryMemberInterface> get_LibraryMembers() {
		List<LibraryMemberInterface> members = new ArrayList<LibraryMemberInterface>();
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
		Set<LibraryNode> usedLibs = new HashSet<LibraryNode>();
		if (deep && getChain() != null)
			for (LibraryNode lib : getChain().getLibraries())
				usedLibs.addAll(lib.getAssignedLibraries());
		else
			usedLibs.addAll(getAssignedLibraries());
		return new ArrayList<LibraryNode>(usedLibs);
	}

	private List<LibraryNode> getAssignedLibraries() {
		Set<LibraryNode> usedLibs = new HashSet<LibraryNode>();

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
			if (cf == null || cf.getWhereContributed() == null || cf instanceof ContributedFacetNode)
				continue;
			Node provider = (Node) cf.getWhereContributed().getOwningComponent();
			if (provider != null && provider.getLibrary() != null && !provider.getLibrary().isBuiltIn())
				usedLibs.add(provider.getLibrary().getHead());// returns lib if unmanaged
		}

		// Don't match any library in this chain.
		if (this.getChain() != null)
			usedLibs.removeAll(this.getChain().getLibraries());
		usedLibs.remove(this);
		return new ArrayList<LibraryNode>(usedLibs);
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
		return getDescendants_LibraryMembers();
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
		return projectItem != null
				&& (projectItem.getState().equals(RepositoryItemState.MANAGED_LOCKED) || projectItem.getState().equals(
						RepositoryItemState.MANAGED_WIP));
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
	 */
	public void replaceAllExtensions(List<ExtensionOwner> extensionsToUpdate) {
		if (extensionsToUpdate == null || extensionsToUpdate.isEmpty())
			return;

		// Create a map of all type providers in this library keyed by name
		Map<String, ExtensionOwner> candidates = new HashMap<String, ExtensionOwner>();
		for (ExtensionOwner p : this.getDescendants_ExtensionOwners())
			candidates.put(((Node) p).getName(), p);

		for (ExtensionOwner e : extensionsToUpdate) {
			e.setExtension((Node) candidates.get(e.getExtendsTypeName()));
		}
	}

	/**
	 * Replace assignments to all users in the list with a type from this library with the same name.
	 */
	public void replaceAllUsers(List<TypeUser> users) {
		if (users == null || users.isEmpty())
			return;

		LOGGER.debug("Replacing type users to use types from " + this + " library.");
		// Create a map of all type providers in this library keyed by name
		Map<String, TypeProvider> candidates = new HashMap<String, TypeProvider>();
		for (TypeProvider p : this.getDescendants_TypeProviders())
			candidates.put(p.getName(), p);

		for (TypeUser user : users) {
			user.setAssignedType(candidates.get(user.getAssignedType().getName()));
			LOGGER.debug("assigned type " + user.getAssignedType().getName() + " to "
					+ user.getOwningComponent().getName());
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
		Map<String, ContextualFacetOwnerInterface> candidates = new HashMap<String, ContextualFacetOwnerInterface>();
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
	 * Add this service to the library. Assures only one service. Assure TL library has this service. Assure service has
	 * a name.
	 */
	public void setServiceRoot(ServiceNode serviceNode) {

		// Make sure the library only has one service.
		if (getServiceRoot() != null)
			getServiceRoot().delete();

		if (getTLModelObject() instanceof TLLibrary)
			if (((TLLibrary) getTLModelObject()).getService() != serviceNode.getTLModelObject())
				((TLLibrary) getTLModelObject()).setService(serviceNode.getTLModelObject());

		if (serviceNode.getName() == null || serviceNode.getName().isEmpty())
			setName(getName() + "_Service");

		serviceNode.setParent(this);
		serviceNode.setLibrary(this);
		serviceRoot = serviceNode;

	}

	/**
	 * 
	 */
	protected void setResourceRoot(NavNode resourceNode) {
		this.resourceRoot = resourceNode;
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
	 * Check all members to see if this is a member.
	 * 
	 * @param member
	 * @return true if the member is a member of this library.
	 */
	@Override
	public boolean contains(Node member) {
		return getChildrenHandler().contains(member);
		// if (member instanceof LibraryMemberInterface) {
		// if (getComplexRoot().contains(member))
		// return true;
		// if (getSimpleRoot().contains(member))
		// return true;
		// if (getServiceRoot() == member)
		// return true;
		// if (getResourceRoot().contains(member))
		// return true;
		// }
		// return false;
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

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
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.handlers.children.ModelNodeChildrenHandler;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.listeners.NodeModelEventListener;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNodeType;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeProviderAndOwners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model Node is a conceptual node that is never displayed. It is the parent of all nodes at the root of the navigator
 * tree.
 * 
 * It maintains contents of the model.
 * 
 * @author Dave Hollander
 * 
 */
public class ModelNode extends Node implements TypeProviderAndOwners {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelNode.class);

	private final static AtomicInteger counter = new AtomicInteger(0);
	// Constants defined here because it is a singleton
	public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	public static final String Chameleon_NS = "http://chameleon.anonymous/ns";
	private static final QName OTA_EMPTY_QNAME = new QName("http://www.opentravel.org/OTM/Common/v0", "Empty");

	// These nodes are not in the TL model but used within the node model.
	// They allow all nodes to have a type and related properties.
	protected static ImpliedNode undefinedNode = new ImpliedNode(ImpliedNode.Undefined);
	protected static ImpliedNode indicatorNode = new ImpliedNode(ImpliedNodeType.Indicator);
	protected static ImpliedNode unassignedNode = new ImpliedNode(ImpliedNodeType.UnassignedType);
	protected static ImpliedNode defaultStringNode = new ImpliedNode(ImpliedNodeType.String);
	protected static ImpliedNode atomicTypeNode = new ImpliedNode(ImpliedNodeType.XSD_Atomic);
	protected static ImpliedNode unionTypeNode = new ImpliedNode(ImpliedNodeType.Union);
	protected static Node emptyNode = null; // will be set to a built-in type.

	/**
	 * @return the defaultStringNode
	 */
	public static ImpliedNode getDefaultStringNode() {
		return defaultStringNode;
	}

	/**
	 * @return the empty node
	 */
	public static Node getEmptyNode() {
		setEmptyNode();
		return emptyNode;
	}

	public static TLAttributeType getEmptyType() {
		setEmptyNode();
		return emptyNode != null ? (TLAttributeType) emptyNode.getTLModelObject() : null;
	}

	/**
	 * @return the indicatorNode
	 */
	public static ImpliedNode getIndicatorNode() {
		return indicatorNode;
	}

	// protected ModelContentsData mc = new ModelContentsData();

	/**
	 * @return the unassignedNode for use on nodes that should have types assigned but are <i>missing</i>
	 */
	public static ImpliedNode getUnassignedNode() {
		return unassignedNode;
	}

	/**
	 * @return the undefined node for use on nodes that have no type associated with them.
	 */
	public static ImpliedNode getUndefinedNode() {
		return undefinedNode;
	}

	private static void setEmptyNode() {
		// Find the built-in empty node.
		if (emptyNode == null)
			emptyNode = NodeFinders.findNodeByQName(OTA_EMPTY_QNAME);
		// This will happen during startup when built-in libaries are being loaded.
		// if (emptyNode == null)
		// LOGGER.error("Empty Node could not be set. Be sure that library is loaded early.");
	}

	private TLModel tlModel;

	private String name = "";
	// Just have one so we can skip checking for null MO and TLmodelObjects

	private TLModelElement tlModelEle = new TLModelElement() {

		@Override
		public TLModel getOwningModel() {
			return getTLModel();
		}

		@Override
		public String getValidationIdentity() {
			return "The_Model";
		}
	};

	// Statistics
	private int unresolvedTypes = 0;

	private LibraryModelManager libMgr = null;

	/**
	 * Constructor
	 * 
	 * @param model
	 */
	public ModelNode(final TLModel model) {
		super();
		setParent(null);
		// duplicateTypesNode.initialize(this);
		undefinedNode.initialize(this);
		indicatorNode.initialize(this);
		unassignedNode.initialize(this);
		defaultStringNode.initialize(this);
		name = "Model_Root_" + counter.incrementAndGet();
		root = this;
		tlModel = model;
		libMgr = new LibraryModelManager(this);

		ListenerFactory.setIdentityListner(this);
		childrenHandler = new ModelNodeChildrenHandler(this);
		getChildrenHandler().add(unassignedNode);

		// LOGGER.debug("ModelNode(TLModel) done.");
	}

	public void addProject(final ProjectNode project) {
		getChildrenHandler().add(project);
		project.setParent(this);
	}

	/**
	 * Get the default project from the project controller.
	 * 
	 * @return
	 */
	public ProjectNode getDefaultProject() {
		if (OtmRegistry.getMainController() != null)
			if (OtmRegistry.getMainController().getProjectController() != null)
				return OtmRegistry.getMainController().getProjectController().getDefaultProject();
		return null;
	}

	/**
	 * Model should be reset using {@link ProjectController#closeAll()} not this method since project controller has
	 * access to TLProjects.
	 */
	public void close(boolean includeBuiltins) {
		List<ProjectNode> projects = getUserProjects();
		// projects.add(OtmRegistry.getMainController().getProjectController().getDefaultProject());
		if (includeBuiltins)
			projects = getAllProjects();
		for (ProjectNode project : projects) {
			project.close();
			getChildrenHandler().clear(project);
		}
		libMgr.clear(includeBuiltins); // leave built-ins
		undefinedNode.initialize(this);
		indicatorNode.initialize(this);
		unassignedNode.initialize(this);
		defaultStringNode.initialize(this);
	}

	@Override
	public ModelNodeChildrenHandler getChildrenHandler() {
		return (ModelNodeChildrenHandler) childrenHandler;
	}

	@Override
	public String getComponentType() {
		return "Model";
	}

	/**
	 * Return all libraries in the library manager.
	 */
	// @Override
	public List<LibraryNode> getLibraries() {
		return libMgr.getAllLibraries();
	}

	@Override
	public LibraryNode getLibrary() {
		return null;
	}

	public LibraryModelManager getLibraryManager() {
		return libMgr;
	}

	/**
	 * 
	 * @return new list of libraries managed by the library model manager
	 */
	public List<LibraryInterface> getManagedLibraries() {
		return libMgr.getLibraries();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new NodeModelEventListener(this);
	}

	@Override
	public Node getParent() {
		return null; // top of the tree
	}

	public TLModel getTLModel() {
		return tlModel;
	}

	@Override
	public TLModelElement getTLModelObject() {
		// Models do not have model elements, just TLModel.
		// TLModel is not an TLModelElement.
		// But return an empty just have one so we can skip checking for
		// null MO and TLmodelObjects
		return tlModelEle;
	}

	/**
	 * @return the unresolvedTypes
	 */
	public int getUnresolvedTypeCount() {
		return unresolvedTypes;
	}

	public List<ProjectNode> getUserProjects() {
		ArrayList<ProjectNode> libs = new ArrayList<>();
		for (Node n : getChildren()) {
			if (n instanceof ProjectNode && !n.isBuiltIn())
				libs.add((ProjectNode) n);
		}
		return libs;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @return true if namespace is managed by any of the child projects
	 */
	public boolean isInProjectNS(String namespace) {
		if (namespace == null)
			return false;
		for (Node n : getChildren())
			if (n instanceof ProjectNode) {
				if (namespace.startsWith(n.getNamespace())) // order is significant due to versions
					return true;
			}
		return false;
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	public void removeProject(final ProjectNode project) {
		getChildrenHandler().remove(project);
		project.setParent(null);
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

}

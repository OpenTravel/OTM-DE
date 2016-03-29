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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.listeners.NodeModelEventListener;
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
public class ModelNode extends Node {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelNode.class);
	// public static String TempLib = "TemporaryLibrary";
	private final static AtomicInteger counter = new AtomicInteger(0);
	private TLModel tlModel;
	private LibraryNode defaultLibrary; // Library used for paths and
										// compilation
	private String name = "";
	// Just have one so we can skip checking for null MO and TLmodelObjects
	private TLModelElement tlModelEle = new TLModelElement() {

		@Override
		public String getValidationIdentity() {
			return "The_Model";
		}

		@Override
		public TLModel getOwningModel() {
			return getTLModel();
		}
	};

	// Statistics
	private int typeProviders = 0;
	private int typeUsers = 0;
	private int unresolvedTypes = 0;
	private int xsdTypes = 0;
	private int resolvedXsdTypes = 0;

	// These nodes are not in the TL model but used within the node model.
	// They allow all nodes to have a type and related properties.
	private static TLLibrary impliedTLLib = makeImpliedLibrary();
	protected static ImpliedNode undefinedNode = new ImpliedNode(ImpliedNode.Undefined);
	protected static ImpliedNode indicatorNode = new ImpliedNode(ImpliedNodeType.Indicator);
	protected static ImpliedNode unassignedNode = new ImpliedNode(ImpliedNodeType.UnassignedType);
	protected static ImpliedNode defaultStringNode = new ImpliedNode(ImpliedNodeType.String);
	protected static ImpliedNode atomicTypeNode = new ImpliedNode(ImpliedNodeType.XSD_Atomic);
	protected static ImpliedNode unionTypeNode = new ImpliedNode(ImpliedNodeType.Union);
	protected static ImpliedNode duplicateTypesNode = new ImpliedNode(ImpliedNodeType.Duplicate);
	protected List<Node> duplicateTypes = new ArrayList<Node>();

	protected static Node emptyNode = null; // will be set to a built-in type.
	private static final QName OTA_EMPTY_QNAME = new QName("http://www.opentravel.org/OTM/Common/v0", "Empty");

	protected ModelContentsData mc = new ModelContentsData();

	public ModelNode(final TLModel model) {
		super();
		setParent(null);
		duplicateTypesNode.initialize(this);
		undefinedNode.initialize(this);
		indicatorNode.initialize(this);
		unassignedNode.initialize(this);
		defaultStringNode.initialize(this);
		name = "Model_Root_" + counter.incrementAndGet();
		root = this;
		tlModel = model;

		addListeners();

		// // TODO - like ProjectNode - see if the MO adds value
		// modelObject = ModelObjectFactory.newModelObject(tlModel, this);
		//
		// LOGGER.debug("ModelNode(TLModel) done.");
	}

	public void addProject(final ProjectNode project) {
		getChildren().add(project);
		project.setParent(this);
	}

	/**
	 * @return the atomicTypeNode
	 */
	public static ImpliedNode getAtomicTypeNode() {
		return atomicTypeNode;
	}

	@Override
	public List<Node> getChildren() {
		if (!super.getChildren().contains(ModelNode.getUnassignedNode())) {
			super.getChildren().add(ModelNode.getUnassignedNode());
			// Enhancement - enable duplicate display. Needs controls to keep it up to
			// date with changes throughout the model before enabling.
			//
			// navChildren.add(ModelNode.getDuplicateTypesNode());
		}
		return super.getChildren();
	}

	@Override
	public String getComponentType() {
		return "Model";
	}

	public void setDefaultLibrary(LibraryNode dLib) {
		defaultLibrary = dLib;
	}

	/**
	 * Returns output file name without extension.
	 * 
	 * @param fn
	 * @return
	 */
	public String getFilePath() {
		if (defaultLibrary == null)
			return "";

		final String fileName = defaultLibrary.getPath();
		if ((fileName == null) || (fileName.isEmpty())) {
			return "";
		}
		final int i = fileName.lastIndexOf(".");
		return i > 0 ? fileName.substring(0, i) : "";
	}

	public TLModel getTLModel() {
		return tlModel;
	}

	public void addListeners() {
		tlModel.addListener(new NodeModelEventListener());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#getTLModelObject()
	 */
	@Override
	public TLModelElement getTLModelObject() {
		// Models do not have model elements, just TLModel.
		// But return an empty just have one so we can skip checking for
		// null MO and TLmodelObjects
		return tlModelEle;
	}

	// @Override
	// public List<Node> getNavChildren() {
	// return getChildren();
	// }

	@Override
	public Node getParent() {
		return null; // top of the tree
	}

	@Override
	public boolean hasNavChildren() {
		return getChildren().size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#hasChildren_TypeProviders()
	 */
	@Override
	public boolean hasChildren_TypeProviders() {
		return getChildren().size() > 0 ? true : false;
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	@Override
	public boolean isUnique(final INode testNode) {
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public LibraryNode getLibrary() {
		List<LibraryNode> libs = getLibraries();
		if (libs.isEmpty()) {
			return null;
		} else {
			return libs.get(0);
		}
	}

	/**
	 * @return the empty node
	 */
	public static INode getEmptyNode() {
		setEmptyNode();
		return emptyNode;
	}

	public static TLAttributeType getEmptyType() {
		setEmptyNode();
		return emptyNode != null ? (TLAttributeType) emptyNode.getTLModelObject() : null;
	}

	private static void setEmptyNode() {
		// Find the built-in empty node.
		if (emptyNode == null)
			emptyNode = NodeFinders.findNodeByQName(OTA_EMPTY_QNAME);
		if (emptyNode == null)
			LOGGER.error("Empty Node could not be set. Be sure that library is loaded early.");
	}

	/**
	 * duplicates are set by the type resolver
	 */
	public static ImpliedNode getDuplicateTypesNode() {
		return duplicateTypesNode;
	}

	public void addDuplicateType(Node n) {
		duplicateTypesNode.getChildren().add(n);
	}

	public List<Node> getDuplicateTypes() {
		return duplicateTypesNode.getChildren();
	}

	public boolean isDuplicate(Node n) {
		return duplicateTypesNode.getChildren().contains(n);
	}

	/**
	 * @return the indicatorNode
	 */
	public static ImpliedNode getIndicatorNode() {
		return indicatorNode;
	}

	/**
	 * @return the undefined node for use on nodes that have no type associated with them.
	 */
	public static ImpliedNode getUndefinedNode() {
		return undefinedNode;
	}

	/**
	 * @return the unassignedNode for use on nodes that should have types assigned but are <i>missing</i>
	 */
	public static ImpliedNode getUnassignedNode() {
		return unassignedNode;
	}

	/**
	 * @return the union node
	 */
	public static ImpliedNode getUnionNode() {
		return unionTypeNode;
	}

	/**
	 * @return the defaultStringNode
	 */
	public static ImpliedNode getDefaultStringNode() {
		return defaultStringNode;
	}

	public int getTypeProviderCount() {
		return typeProviders;
	}

	@Override
	public int getWhereAssignedCount() {
		return typeUsers;
	}

	public int getUnassignedTypeCount() {
		return unassignedNode.getWhereAssignedCount();
	}

	/**
	 * @return the unresolvedTypes
	 */
	public int getUnresolvedTypeCount() {
		return unresolvedTypes;
	}

	/**
	 * @return the xsdTypes
	 */
	public int getXsdTypeCount() {
		return xsdTypes;
	}

	/**
	 * @return the
	 */
	public int getResolvedXsdTypeCount() {
		return resolvedXsdTypes;
	}

	/**
	 * Create the library for the generated components.
	 * 
	 * @param xLib
	 */
	protected static TLLibrary makeImpliedLibrary() {
		TLLibrary impliedTLLib = new TLLibrary();
		impliedTLLib.setNamespace("uri:namespaces:impliedNS");
		impliedTLLib.setPrefix("implied");
		impliedTLLib.setName("ImpliedTypeLibrary");
		try {
			impliedTLLib.setLibraryUrl(new URL("file://temp"));
		} catch (MalformedURLException e) {
			LOGGER.error("Invalid URL exception");
		}
		return impliedTLLib;
	}

	/**
	 * @return the impliedTLLib
	 */
	public static TLLibrary getImpliedTLLib() {
		return impliedTLLib;
	}

	@Override
	public boolean isLibraryContainer() {
		return true;
	}

}

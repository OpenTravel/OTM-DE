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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;
import org.opentravel.schemas.node.handlers.ConstraintHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeProviders;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * The ComponentNode class handles nodes that represent model objects. It is overridden for most types and properties.
 * 
 * This does not implement type provider -- sub-classes implement TypeProvider
 * 
 * @author Dave Hollander
 * 
 */

public abstract class ComponentNode extends Node {
	private final static Logger LOGGER = LoggerFactory.getLogger(ComponentNode.class);

	/**
	 * ComponentNode constructor for nodes with no tlObj. Node Identity Listener can not be set.
	 */
	public ComponentNode() {
	}

	/**
	 * Top Level component node construction. Create model object, set name and description, set TL library member
	 * 
	 * @param tlModelObject
	 *            - the model object for the node
	 */
	public ComponentNode(final TLLibraryMember tlModelObject) {
		super(tlModelObject);
		ListenerFactory.setIdentityListner(this);
	}

	/**
	 * Create a component node for a <b>non</b>-top-level model element. Only creates node and sets name and
	 * description.
	 * 
	 * @param obj
	 */
	public ComponentNode(final TLModelElement obj) {
		super(obj);
		ListenerFactory.setIdentityListner(this);
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;
	}

	public void addProperty(final Node property) {
		if (!(property instanceof PropertyNode))
			return;
		if (!(this instanceof FacetInterface))
			return;
		((FacetInterface) this).add((PropertyNode) property, -1);
	}

	/**
	 * Create aliases for all complex properties of this object that have the same type. Used in importing from XSD.
	 */
	public void createAliasesForProperties() {

		Map<ComponentNode, List<Node>> typeMap = new HashMap<>(getDescendants().size());
		List<Node> list;
		for (Node d : getDescendants()) {
			if (d.getType() == null)
				continue;
			if (d.getType() instanceof CoreObjectNode || d.getType() instanceof BusinessObjectNode) {
				if (typeMap.containsKey(d.getType()))
					list = typeMap.get(d.getType());
				else {
					list = new ArrayList<>();
					typeMap.put((ComponentNode) d.getType(), list);
				}
				list.add(d);
			}
		}

		for (ComponentNode type : typeMap.keySet()) {
			list = typeMap.get(type);
			if (list == null)
				continue;
			if (list.size() > 1) {
				for (Node property : list) {
					String aliasName = property.getName() + "_" + type.getName();
					((TypeUser) property).setAssignedType(new AliasNode((AliasOwner) type, aliasName));
					property.setName(aliasName);
				}
			}
		}
	}

	/**
	 * Create a minor version of the object using the MinorVersionHelper. New object is added to the head TL Library.
	 * 
	 * @param node
	 * @return new TL LibraryMember or null
	 */
	protected LibraryMember createMinorTLVersion(VersionedObjectInterface node) {
		MinorVersionHelper helper = new MinorVersionHelper();
		Versioned v = null;
		TLLibrary tlLib = getChain().getHead().getTLLibrary();
		try {
			v = helper.createNewMinorVersion((Versioned) getTLModelObject(), tlLib);
		} catch (VersionSchemeException | ValidationException e) {
			LOGGER.debug("Exception creating minor TL version in: " + tlLib.getPrefix() + ":" + tlLib.getName() + " "
					+ e.getLocalizedMessage());
			return null;
		}
		return (LibraryMember) v;
	}

	// newNode is the node constructed from the TL object returned from createMinorTLVersion()
	protected ComponentNode createMinorVersionComponent(ComponentNode newNode) {

		assert !getOwningComponent().isInHead();
		// assert newNode instanceof ExtensionOwner;
		if (newNode instanceof ImpliedNode) {
			LOGGER.debug("Empty minor version created");
			return null;
		}
		Node owner = (Node) this.getOwningComponent();
		owner.getLibrary().checkExtension(owner);
		// exit if it is already in the head of the chain.
		if (owner.isInHead())
			return null;

		if (newNode instanceof ExtensionOwner)
			((ExtensionOwner) newNode).setExtension(owner);

		if (newNode.getName() == null || newNode.getName().isEmpty())
			newNode.setName(owner.getName()); // Some of the version handlers do not set name

		// Add the new node to the library at the head of the chain
		LibraryChainNode chain = owner.getChain();
		if (newNode instanceof LibraryMemberInterface)
			chain.getHead().addMember((LibraryMemberInterface) newNode);

		// Removed - What does this do? They are the same version nodes for new and owner nodes
		// chain.getComplexAggregate().getChildren().remove(owner);

		owner.getLibrary().checkExtension(owner);
		// TODO - should old properties be set to inherited?

		// Final check
		assert newNode.getParent() != null;
		assert chain.getHead().contains(newNode);
		if (newNode instanceof SimpleTypeProviders)
			assert chain.getSimpleAggregate().contains(newNode.getVersionNode());
		else
			assert chain.getComplexAggregate().contains(newNode.getVersionNode());
		// This should fail -- same version node!
		// assert !chain.getComplexAggregate().contains(owner.getVersionNode());
		assert newNode.getVersionNode().get() == newNode; // assure version node updated
		if (this.getExtendsType() == null)
			assert newNode.getExtendsType() == null; // does not report version extension
		assert ((ExtensionOwner) newNode).getExtensionBase() == owner;

		return newNode;
	}

	/*
	 * ComponentNode Utilities
	 */

	/**
	 * Create a new object in a patch version library. Creates an empty extension point facet. Adds the new node to the
	 * owner's chain head library.
	 * 
	 * @return new extension point facet, of this one if it was an extension point
	 */
	public ComponentNode createPatchVersionComponent() {
		if (this.getOwningComponent() instanceof ExtensionPointNode)
			return (ComponentNode) this.getOwningComponent();

		ExtensionPointNode newNode = null;
		newNode = new ExtensionPointNode(new TLExtensionPointFacet());

		if (this instanceof FacetOMNode)
			newNode.setExtension(this);
		else if (this instanceof PropertyNode)
			newNode.setExtension(getParent());
		else if (this instanceof CoreObjectNode)
			newNode.setExtension(((CoreObjectNode) this).getFacet_Summary());
		else if (this instanceof BusinessObjectNode)
			newNode.setExtension(((BusinessObjectNode) this).getFacet_Summary());
		// else
		// LOGGER.error("Can't add a property to this: " + this);
		// If there already is an EP, then return that.
		LibraryChainNode chain = getChain();
		Node existing = ((Node) chain.getComplexAggregate()).findLibraryMemberByName(newNode.getName());
		if (existing != null && (existing instanceof ExtensionPointNode))
			newNode = (ExtensionPointNode) existing;
		else
			chain.getHead().addMember(newNode); // needs extends type to know name for family.

		return newNode;
	}

	public INode createProperty(final Node dropTarget) {
		return null;
	}

	// overridden where a simple type exists.
	// Only used in validation view
	@Override
	@Deprecated
	public Node getAssignable() {
		return getSimpleProperty();
	}

	/**
	 * @return the assigned namespace prefix from the model object.
	 */
	public String getAssignedPrefix() {
		if (this instanceof TypeUser)
			return ((TypeUser) this).getTypeHandler().getAssignedTypePrefix();
		return "";
	}

	public ConstraintHandler getConstraintHandler() {
		return null;
	}

	public TLFacetType getFacetType() {
		return null;
	}

	/**
	 * Simple getter of the actual node where the inherited child is declared.
	 * 
	 * @return type inherited from or null if no inheritance. Note, Open Enumerations have an inherited attribute but
	 *         null for inherits from.
	 */
	@Override
	public Node getInheritedFrom() {
		assert !(this instanceof InheritedInterface);
		return null;
	}

	@Override
	public String getName() {
		return "";
	}

	/**
	 * Property Roles are displayed in the facet table and describe what role the item can play in constructing
	 * vocabularies.
	 * 
	 * @return - string from enumerated list of roles, or empty string if not property.
	 */
	public String getPropertyRole() {
		return "";
	}

	// overridden where a simple property exists.
	public Node getSimpleProperty() {
		return null;
	}

	@Override
	public abstract TLModelElement getTLModelObject();

	/**
	 * Test this node against those in the parentNode to and return true if the name is unique within its parent and
	 * inherited parent. NOTE: does not check other facets!
	 * 
	 * @return
	 */
	// FIXME - why here and in Node? Why different?
	public boolean isUnique() {
		return isValid();
	}

	@Override
	@Deprecated
	public boolean isXsdType() {
		// Local anonymous types may not have xsdType set.
		return xsdType;
	}

	@Deprecated
	public void removeProperty(final Node property) {
		// throw new IllegalStateException("Remove property in component node should never run.");
	}

	/**
	 * @return true if the documentation could be set, false otherwise
	 */
	public boolean setDocumentation(TLDocumentation documentation) {
		if (getTLModelObject() instanceof TLDocumentationOwner)
			((TLDocumentationOwner) getTLModelObject()).setDocumentation(documentation);
		else
			return false;
		return true;
	}

}

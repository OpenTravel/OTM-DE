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
/**
 * 
 */
package org.opentravel.schemas.node.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.handlers.children.ResourceChildrenHandler;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.TypeUserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ResourceNode extends ComponentNode implements TypeUser, ResourceMemberInterface, VersionedObjectInterface,
		LibraryMemberInterface, ExtensionOwner {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceNode.class);

	// private Node subject = null;
	private TLResource tlResource = null;
	private String MSGKEY = "rest.ResourceNode";
	private ExtensionHandler extensionHandler = null; // Lazy construction - created when accessed
	protected LibraryNode owningLibrary = null;
	protected LibraryNode library;

	// Static children handler.
	// private ResourceChildrenHandler childrenHandler = null;

	public class AbstractListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setAbstract(Boolean.valueOf(value));
			return false;
		}
	}

	// @Override
	// public NodeChildrenHandler<?> getChildrenHandler() {
	// return null;
	// }

	public class BasePathListener implements ResourceFieldListener {
		@Override
		public boolean set(String path) {
			setBasePath(path);
			return true; // changes examples
		}
	}

	public class FirstClassListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			tlResource.setFirstClass(Boolean.valueOf(value));
			// LOGGER.debug("Set first class to: " + tlObj.isFirstClass());
			return false;
		}
	}

	public class ParentRefListener implements ResourceFieldListener {
		@Override
		public boolean set(String name) {
			toggleParent(name);
			return false;
		}
	}

	public class SubjectListener implements ResourceFieldListener {
		@Override
		public boolean set(String name) {
			setSubject(name);
			return false;
		}

		public boolean set(Node subject) {
			if (subject != null)
				setSubject(subject);
			return true;
		}
	}

	public class ExtensionListener implements ResourceFieldListener {
		@Override
		public boolean set(String name) {
			setExtension(name);
			return false;
		}
	}

	/**************************************************************
	 * 
	 */
	public ResourceNode(LibraryMember mbr) {
		this((TLLibraryMember) mbr);
	}

	public ResourceNode(TLLibraryMember mbr) {
		super(mbr);
		assert mbr instanceof TLResource;
		tlResource = (TLResource) mbr;
		ListenerFactory.setIdentityListner(this);
		owningLibrary = (LibraryNode) Node.GetNode(mbr.getOwningLibrary());

		childrenHandler = new ResourceChildrenHandler(this);
		getChildrenHandler().initChildren();
		getChildrenHandler().initInherited();
		// addMOChildren(); // NOTE - this will fail if no library

		if (getSubject() == null)
			LOGGER.debug("No subject assigned: " + this);
		else
			getSubject().addWhereUsed(this);

		// LOGGER.debug("NOT IMPLEMENTED - resource node constructor.");
		assert true;
		assert (GetNode(mbr) == this);
	}

	public ResourceNode(TLResource mbr, LibraryNode lib) {
		super(mbr);
		assert mbr instanceof TLResource;
		tlResource = mbr;

		ListenerFactory.setIdentityListner(this);

		// assert (getModelObject() != null);
		if (lib != null)
			lib.addMember(this);

		childrenHandler = new ResourceChildrenHandler(this);
		getChildrenHandler().initChildren();
		getChildrenHandler().initInherited();
		// addMOChildren();

		// NOTE - subject may not have a node assigned yet!
		// FIXME - how to add where used in these cases?
		if (getSubject() == null)
			LOGGER.debug("No subject assigned: " + this);
		else
			getSubject().addWhereUsed(this);

		assert (GetNode(mbr) == this);
	}

	/**
	 * Create a resource in the library of the node. Name the resource using the library member name. If the node has an
	 * editable library add the resource to that library.
	 * 
	 * @param library
	 *            - add to the library if not null and editable
	 * @param business_object
	 *            - use the name to name the resource or if null "NewResource"
	 */
	public ResourceNode(LibraryNode ln, BusinessObjectNode bo) {
		super(new TLResource());
		tlResource = (TLResource) tlObj;
		ListenerFactory.setIdentityListner(this);

		if (bo == null)
			tlResource.setName("NewResource"); // must be named to add to library
		else
			tlResource.setName(bo.getName() + "Resource");

		childrenHandler = new ResourceChildrenHandler(this);
		// children add themselves to the parent, so children handler must exist.
		getChildrenHandler().initChildren();
		getChildrenHandler().initInherited();

		if (ln != null && ln.isEditable()) {
			ln.addMember(this);
			assert getLibrary() != null;
		} else
			LOGGER.warn("Resource not added to library. " + ln + " Is not an editable library.");

		assert (GetNode(tlResource) == this);
	}

	@Override
	public ResourceChildrenHandler getChildrenHandler() {
		return (ResourceChildrenHandler) childrenHandler;
	}

	// /**
	// * Use the passed business object to build a fully populated resource added to the library of the passed BO.
	// */
	// public ResourceNode(BusinessObjectNode businessObject) {
	// super(new ResourceBuilder().buildTL(businessObject));
	// tlObj = getTLModelObject();
	//
	// businessObject.getLibrary().addMember(this);
	// }

	@Override
	public void addChild(ResourceMemberInterface child) {
		getChildrenHandler().add((Node) child);
	}

	@Override
	public LibraryMemberInterface clone(LibraryNode targetLib, String nameSuffix) {
		if (getLibrary() == null || !getLibrary().isEditable()) {
			LOGGER.warn("Could not clone node because library " + getLibrary() + " it is not editable.");
			return null;
		}

		LibraryMemberInterface clone = null;

		// Use the compiler to create a new TL src object.
		TLModelElement newLM = (TLModelElement) cloneTLObj();
		if (newLM != null) {
			clone = NodeFactory.newLibraryMember((LibraryMember) newLM);
			if (nameSuffix != null)
				clone.setName(clone.getName() + nameSuffix);
			targetLib.addMember(clone);
		}
		return clone;
	}

	public TLResource cloneTL() {
		TLResource newTL = (TLResource) super.cloneTLObj();

		// Clone has a bo ref name but not a bo ref
		newTL.setBusinessObjectRef(getTLModelObject().getBusinessObjectRef());

		// Parameter group facet references are not set
		for (TLParamGroup pg : getTLModelObject().getParamGroups())
			// Find matching param group and set facet ref
			for (TLParamGroup npg : newTL.getParamGroups())
				if (npg.getName().equals(pg.getName())) {
					npg.setFacetRef(pg.getFacetRef());
					// Each parameter must have its field set
					for (TLParameter p : pg.getParameters())
						for (TLParameter np : npg.getParameters())
							if (np.getFieldRefName().equals(p.getFieldRefName()))
								np.setFieldRef(p.getFieldRef());
				}

		// This could be simplified by using the names as keys, but it is working so i didn't do that.
		// Create Action Facet mapping for use in response payload type
		Map<TLActionFacet, TLActionFacet> facets = new HashMap<TLActionFacet, TLActionFacet>();
		for (TLActionFacet af : getTLModelObject().getActionFacets())
			for (TLActionFacet naf : newTL.getActionFacets())
				if (naf.getName().equals(af.getName()))
					facets.put(af, naf);
		// Create parameter group map for use in requests
		Map<TLParamGroup, TLParamGroup> groups = new HashMap<TLParamGroup, TLParamGroup>();
		for (TLParamGroup pg : getTLModelObject().getParamGroups())
			for (TLParamGroup npg : newTL.getParamGroups())
				if (npg.getName().equals(pg.getName()))
					groups.put(pg, npg);

		// Action request parameter group
		for (TLAction a : getTLModelObject().getActions())
			for (TLAction na : newTL.getActions())
				if (na.getActionId().equals(a.getActionId())) {
					// Set request parameter group and payload type
					if (na.getRequest() != null) {
						na.getRequest().setParamGroup(groups.get(a.getRequest().getParamGroup()));
						if (na.getRequest().getPayloadTypeName() != null
								&& na.getRequest().getPayloadTypeName().equals(a.getRequest().getPayloadTypeName()))
							na.getRequest().setPayloadType(facets.get(a.getRequest().getPayloadType()));
					}
					// Set responses payload types
					for (TLActionResponse r : a.getResponses())
						for (TLActionResponse nr : na.getResponses())
							if (nr.getPayloadTypeName() != null
									&& nr.getPayloadTypeName().equals(r.getPayloadTypeName()))
								nr.setPayloadType(facets.get(r.getPayloadType()));
				}

		// ValidationFindings findings = TLModelCompileValidator.validateModelElement((TLModelElement) newTL, true);
		// assert findings.isEmpty();

		return newTL;
	}

	@Override
	public ResourceNode copy(LibraryNode destLib) throws IllegalArgumentException {
		if (destLib == null)
			destLib = getLibrary();

		// Clone the TL object
		TLResource tlCopy = cloneTL();

		// Create contextual facet from the copy
		Node copy = (Node) NodeFactory.newLibraryMember(tlCopy);
		if (!(copy instanceof ResourceNode))
			throw new IllegalArgumentException("Unable to copy " + this);
		ResourceNode resource = (ResourceNode) copy;

		// Fix any contexts
		resource.fixContexts();

		destLib.addMember(resource);

		return resource;
	}

	@Override
	public void deleteTL() {
		getTLModelObject().getOwningLibrary().removeNamedMember(getTLModelObject());
	}

	/**
	 * Do Nothing.
	 */
	@Override
	public void removeDependency(ResourceMemberInterface dependent) {
		LOGGER.debug("No dependency on " + dependent);
	}

	@Override
	public boolean setAssignedType(TypeProvider type) {
		LOGGER.debug("Tried to set assigned type: " + getType());
		return false;
	}

	@Override
	public boolean setAssignedType(TLModelElement tlProvier) {
		return false;
		// throw new IllegalAccessError("Not Implemented.");
	}

	@Override
	public boolean setAssignedType() {
		return false;
		// throw new IllegalAccessError("Not Implemented.");
	}

	public String getMsgKey() {
		return MSGKEY;
	}

	// @Override
	// public boolean canExtend() {
	// return true;
	// }

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new ResourceNode(createMinorTLVersion(this)));
		// LOGGER.debug("NOT IMPLEMENTED - createMinorVersionCompnoent for resource node.");
		// return null;
	}

	@Override
	public void delete() {
		// LOGGER.debug("Deleting rest resource: " + this);
		// List<Node> kids = new ArrayList<Node>(getChildren());
		for (Node kid : getChildren_New())
			kid.delete();

		if (getParent() != null && getParent().getChildren() != null)
			getParent().getChildrenHandler().clear(this);

		if (getChain() != null)
			getChain().removeAggregate(this);
		if (getSubject() != null)
			getSubject().removeWhereAssigned(this);
		parent = null;
		setLibrary(null);
		deleted = true;

		// LOGGER.debug("Deleting rest resource: " + this);
		if (tlResource.getOwningLibrary() != null)
			tlResource.getOwningLibrary().removeNamedMember(tlResource);
		// LOGGER.debug("Deleted rest resource: " + this);
	}

	public List<ActionNode> getActions() {
		ArrayList<ActionNode> actions = new ArrayList<ActionNode>();
		for (Node child : getChildren())
			if (child instanceof ActionNode)
				actions.add((ActionNode) child);
		return actions;
	}

	/**
	 * @return owned and inherited action facets
	 */
	public List<ActionFacet> getActionFacets() {
		ArrayList<ActionFacet> facets = new ArrayList<ActionFacet>();
		for (Node child : getChildren()) {
			if (child instanceof ActionFacet)
				facets.add((ActionFacet) child);
			if (child instanceof InheritedResourceMember)
				if (((InheritedResourceMember) child).get() instanceof ActionFacet)
					facets.add((ActionFacet) ((InheritedResourceMember) child).get());
		}
		// if (getExtendsType() != null)
		// facets.addAll(getExtendsType().getActionFacets());
		// TODO - JUNIT - add test for inherited AFs
		return facets;
	}

	/**
	 * @return the named action facet or null
	 */
	public ActionFacet getActionFacet(String name) {
		if (!name.equals(ResourceField.NONE))
			for (ActionFacet f : getActionFacets())
				if (f.getName().equals(name))
					return f;
		return null;
	}

	/**
	 * @return An array of action facet names and NONE. If this resource extends another one get names from the base
	 *         type
	 */
	public String[] getActionFacetNames() {
		List<ActionFacet> facets = getActionFacets();
		String[] facetNames = new String[facets.size() + 1];
		int i = 0;
		facetNames[i++] = ResourceField.NONE;
		for (ActionFacet f : facets)
			facetNames[i++] = f.getName();
		return facetNames;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	/**
	 * Override to return subject.
	 */
	@Override
	public TypeProvider getAssignedType() {
		return getSubject();
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.RESOURCE;
	}

	/**
	 * Node method relies upon extension handler that uses model object but Resource does not have model object.
	 */
	@Override
	public boolean isVersioned() {
		return versionNode != null;
	}

	/**
	 * @return non-empty string
	 */
	@Override
	public String getDecoration() {
		String decoration = "";
		if (isAbstract())
			decoration += " Abstract";
		if (getSubject() != null)
			decoration += "  (Exposes: " + getSubject().getNameWithPrefix() + ") - ";

		String extensionTxt = " ";
		// Is it an extension
		if (this.isVersioned()) {
			ComponentNode exBase = versionNode.getPreviousVersion();
			// ComponentNode exBase = (ComponentNode) getExtendsType();
			if (exBase != null) {
				extensionTxt += "Extends version: " + exBase.getNameWithPrefix() + " ";
				if (getChain() != null)
					extensionTxt += " - ";
			}
		}

		if (isInHead())
			if (getLibrary().isMajorVersion())
				extensionTxt += "Major Version";
			else if (isNewToChain())
				extensionTxt += "New to this version";
			else
				extensionTxt += "Current Version";
		else
			extensionTxt += "Version: " + getTlVersion();
		decoration += surround(extensionTxt);
		return decoration.isEmpty() ? " " : decoration;
	}

	@Override
	public boolean isInHead() {
		if (getChain() == null)
			return false;
		return getChain().getHead() == getLibrary();
	}

	// @Override
	// public PropertyOwnerInterface getFacet_Default() {
	// return null;
	// }

	@Override
	public String getDescription() {
		TLResource tlObj = getTLModelObject();
		return tlObj.getDocumentation() != null ? tlObj.getDocumentation().getDescription() : "";
	}

	// @Override
	@Override
	public ResourceNode getExtendsType() {
		Node base = null;
		NamedEntity tl = null;
		if (tlResource.getExtension() != null)
			tl = tlResource.getExtension().getExtendsEntity();
		if (tl instanceof TLResource)
			base = Node.GetNode(tl);

		// Don't return extensions used for versions
		// TODO - relies on extension handler
		// Node base = getExtensionHandler().get();
		if (base == null || base.isVersioned() || !(base instanceof ResourceNode))
			return null;
		return (ResourceNode) base;

		// return base instanceof ResourceNode ? (ResourceNode) base : null;
		// should this implement Extension Owner?
		// throw new IllegalStateException("Need to add type handler to resource.");
		// return (Node) getTypeClass().getTypeNode();
	}

	/**
	 * @return Returns the name of the extension if any. Will return name of a previous version. If in a different
	 *         library the prefix will be added.
	 * 
	 */
	public String getExtendsEntityName() {
		if (tlResource.getExtension() != null) {
			ResourceNode rn = (ResourceNode) Node.GetNode(tlResource.getExtension().getExtendsEntity());
			return getPeerName(rn);
		}
		return "";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Extensions - User can only extend Major version libraries.
		new ResourceField(fields, getExtendsEntityName(), MSGKEY + ".fields.extension", ResourceFieldType.Enum,
				isEditable_newToChain(), new ExtensionListener(), getPeerNames());

		// Business Object = launch selection wizard
		new ResourceField(fields, getSubjectName(), MSGKEY + ".fields.businessObject", ResourceFieldType.ObjectSelect,
				!isAbstract(), new SubjectListener(), this);

		// Base Path
		new ResourceField(fields, tlResource.getBasePath(), MSGKEY + ".fields.basePath", ResourceFieldType.String,
				!isAbstract(), new BasePathListener());

		// Abstract - yes/no button
		new ResourceField(fields, Boolean.toString(tlResource.isAbstract()), MSGKEY + ".fields.abstract",
				ResourceFieldType.CheckButton, new AbstractListener());

		// First Class - yes/no button
		new ResourceField(fields, Boolean.toString(tlResource.isFirstClass()), MSGKEY + ".fields.firstClass",
				ResourceFieldType.CheckButton, !isAbstract(), new FirstClassListener());

		return fields;

	}

	/**
	 * Return the base path and parameter contribution to the URL. Primary use case is for computing URL contribution of
	 * parent resources for examples.
	 * 
	 * @return the path template for action request or empty string
	 */
	public String getPathContribution(ParamGroup params) {
		String contribution = "";
		for (Node child : getChildren())
			if (child instanceof ParentRef) {
				contribution = ((ParentRef) child).getUrlContribution();
			}
		// LOGGER.debug("Path contribution from " + this + " is " + contribution);
		return contribution;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Resource);
	}

	@Override
	public String getLabel() {
		return super.getLabel();
	}

	@Override
	public LibraryNode getLibrary() {
		return owningLibrary;
	}

	// @Override
	// @Deprecated
	// public ResourceMO getModelObject() {
	// ModelObject<?> obj = super.getModelObject();
	// return (ResourceMO) (obj instanceof ResourceMO ? obj : null);
	// }

	@Override
	public AbstractLibrary getTLOwner() {
		return tlResource.getOwningLibrary();
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return Collections.emptyList();
	}

	/**
	 * Children with any inherited actions removed
	 * 
	 * @return
	 */
	public List<Node> getTreeChildren() {
		List<Node> treeChildren = new ArrayList<Node>();
		// Remove any inherited Actions
		for (Node child : getChildren()) {
			if (child instanceof InheritedResourceMember)
				if (((InheritedResourceMember) child).get() instanceof ActionNode)
					continue;
			treeChildren.add(child);
		}

		return treeChildren;
	}

	public List<ParamGroup> getParameterGroups(boolean idGroupsOnly) {
		ArrayList<ParamGroup> pgroups = new ArrayList<ParamGroup>();
		for (Node child : getChildren()) {
			if (child instanceof ParamGroup)
				if (!idGroupsOnly || ((ParamGroup) child).isIdGroup())
					pgroups.add((ParamGroup) child);
			// Get inherited param groups also
			if (child instanceof InheritedResourceMember) {
				child = (Node) ((InheritedResourceMember) child).get();
				if (child instanceof ParamGroup)
					if (!idGroupsOnly || ((ParamGroup) child).isIdGroup())
						pgroups.add((ParamGroup) child);
			}
		}
		return pgroups;
	}

	/**
	 * @param idGroupsOnly
	 *            if true only parameter groups with ID Group set will be included as needed for parent references
	 * @return list of the parameter groups by name and NONE
	 */
	public String[] getParameterGroupNames(boolean idGroupsOnly) {
		List<ParamGroup> paramGroups = getParameterGroups(idGroupsOnly);
		String[] groupNames = new String[paramGroups.size() + 1];
		int i = 0;
		groupNames[i++] = ResourceField.NONE;
		for (Node n : paramGroups)
			groupNames[i++] = n.getName();
		return groupNames;
	}

	/**
	 * @return parameter group from last parent resource node or null if none
	 */
	public ParamGroup getParentParamGroup() {
		return getParentRef() != null ? getParentRef().getParameterGroup() : null;
	}

	/**
	 * @return first parent reference resource node or null if none
	 */
	public ParentRef getParentRef() {
		for (Node child : getChildren())
			if (child instanceof ParentRef)
				return ((ParentRef) child);
		return null;
	}

	/**
	 * @return a string array of parent resource references by name
	 */
	public String[] getParentRefNames() {
		String[] parents = new String[tlResource.getParentRefs().size()];
		int i = 0;
		for (TLResourceParentRef parent : tlResource.getParentRefs())
			if (parent.getParentResourceName() != null)
				parents[i++] = parent.getParentResourceName();
		return parents;
	}

	/**
	 * @return an array of other resources including NONE
	 *         <p>
	 *         If this is a minor version, returns ONLY the name of the extension object
	 */
	public String[] getPeerNames() {
		ArrayList<String> peerList = new ArrayList<String>();
		if (!isEditable_newToChain())
			peerList.add(getExtendsEntityName());
		else {
			peerList.add(ResourceField.NONE);
			if (getParent() != null)
				for (ResourceNode peer : getPeers())
					peerList.add(getPeerName(peer));
		}
		return peerList.toArray(new String[peerList.size()]);
	}

	// Add prefix if in different library
	private String getPeerName(ResourceNode peer) {
		if (peer == null)
			return "";
		return peer.getLibrary() == getLibrary() ? peer.getName() : peer.getNameWithPrefix("");
	}

	private ArrayList<ResourceNode> getPeers() {
		ArrayList<ResourceNode> peerList = new ArrayList<ResourceNode>();
		for (LibraryNode ln : Node.getAllUserLibraries())
			for (Node n : ln.getResourceRoot().getChildren()) {
				if (n instanceof VersionNode)
					n = n.getVersionNode().get();
				if (n instanceof ResourceNode)
					peerList.add((ResourceNode) n);
				// TODO - eliminate members of this chain
			}
		return peerList;
	}

	public ResourceNode getPeerByName(String name) {
		ResourceNode peer = null;
		// for (Node n : getParent().getChildren())
		for (ResourceNode n : getPeers())
			if (getPeerName(n).equals(name))
				peer = n;
		return peer;
	}

	public BusinessObjectNode getSubject() {
		Node subject = null;
		if (tlResource != null && tlResource.getBusinessObjectRef() != null) {
			subject = this.getNode(tlResource.getBusinessObjectRef().getListeners());
			// assert subject != null : "Missing listener on referenced business object.";
		}
		return (BusinessObjectNode) subject;
	}

	public String getSubjectName() {
		return tlResource.getBusinessObjectRef() != null ? tlResource.getBusinessObjectRef().getLocalName() : "None";
	}

	/**
	 * @return a list of business objects by name including "NONE"
	 */
	public String[] getSubjectCandidates() {
		if (getLibrary() == null)
			return new String[0];
		List<Node> subjects = new ArrayList<Node>();
		for (Node n : getLibrary().getDescendants_LibraryMemberNodes())
			if (n instanceof BusinessObjectNode)
				subjects.add(n);
		String[] names = new String[subjects.size() + 1];
		int i = 0;
		names[i++] = ResourceField.NONE;
		for (Node n : subjects)
			names[i++] = n.getName();
		return names;
	}

	/**
	 * 
	 * @param includeSubGrp
	 *            if true include entry for the substitution group
	 * @return list of facets on the subject business object
	 */
	public String[] getSubjectFacets(boolean includeSubGrp) {
		if (getSubject() == null)
			return new String[0];
		List<FacetOMNode> facets = new ArrayList<FacetOMNode>();
		for (Node facet : getSubject().getChildren())
			if (facet instanceof FacetOMNode)
				facets.add((FacetOMNode) facet);
		int size = facets.size();
		if (includeSubGrp)
			size += 1;

		String[] fs = new String[size];
		int i = 0;
		if (includeSubGrp)
			fs[i++] = ResourceField.SUBGRP;
		for (Node facet : getSubject().getChildren())
			if (facet instanceof FacetOMNode)
				fs[i++] = ResourceCodegenUtils.getActionFacetReferenceName((TLFacet) facet.getTLModelObject());
		return fs;
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public String getName() {
		return getTLModelObject() == null || getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public TLResource getTLModelObject() {
		// if (tlObj == null && getTLModelObject() instanceof TLResource)
		// tlObj = (TLResource) getTLModelObject();
		return tlResource;
	};

	@Override
	public boolean hasNavChildren(boolean deep) {
		return false;
	}

	public boolean isAbstract() {
		return tlResource.isAbstract();
	}

	/**
	 * When set to abstract = true then clear base path, remove subject, remove parameter groups,
	 */
	public void setAbstract(boolean flag) {
		tlResource.setAbstract(flag);

		setBasePath("");
		setSubject(ResourceField.NONE);
		for (Node n : getParameterGroups(false))
			if (n instanceof ParamGroup)
				n.delete();
		// LOGGER.debug("Set abstract to: " + tlObj.isAbstract());
		// Also, remove reference facets from action facets
		for (ActionFacet af : getActionFacets())
			af.setReferenceFacetName(ResourceField.NONE);
	}

	@Override
	public boolean isAssignedByReference() {
		return true;
	}

	/**
	 * Resources are not versioned. Override default node behavior that manages versioning.
	 */
	// FIXME - edit-ability should be based on library state and business object
	@Override
	public boolean isDeleteable() {
		if (getLibrary() == null || !getLibrary().isEditable() || parent == null || deleted)
			return false;
		return true;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		if (library == null || parent == null)
			return false;
		return isEditable_isNewOrAsMinor();
	}

	@Override
	public boolean isEditable_newToChain() {
		if (!isEditable())
			return false;
		if (getChain() == null)
			return true;
		if (getVersionNode().hasOlder())
			return false;
		if (getLibrary() != getChain().getHead())
			return false; // is not in the head library of the chain.
		return true;
	}

	@Override
	public boolean isNameEditable() {
		return isEditable();
		// return true;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	public String getBasePath() {
		return tlResource.getBasePath();
	}

	/**
	 * Resource Base Path added to request paths.
	 * <p>
	 * The base path can not have parameters.
	 * <p>
	 * It Must not be empty unless abstract resource.
	 * 
	 * @param path
	 */
	public void setBasePath(String path) {
		if (!isAbstract() && path.isEmpty())
			path = "/";
		tlResource.setBasePath(path);
		assert (!path.contains("{"));
		// LOGGER.debug("Set base path to " + path + ": " + tlObj.getBasePath());
	}

	@Override
	public void setLibrary(LibraryNode ln) {
		owningLibrary = ln;
	}

	@Override
	public void setName(String n) {
		// this.setName(n, false);
		tlResource.setName(n);
	}

	public boolean setExtension(String name) {
		ResourceNode peer = getPeerByName(name);
		if (peer != null) {
			// TODO - use extension handler
			setExtension(peer);
			assert (tlResource.getExtension().getExtendsEntity() == peer.getTLModelObject());
		} else {
			tlResource.setExtension(null);
			// LOGGER.debug("Set extension to null:" + tlObj.getExtension());
		}
		return false;
	}

	public void setSubject(String name) {
		if (name == null || name.equals(ResourceField.NONE)) {
			tlResource.setBusinessObjectRef(null);
			tlResource.setBusinessObjectRefName("");
			LOGGER.debug("Set subject to null.");
		} else
			for (Node n : getLibrary().getDescendants_LibraryMemberNodes())
				if (n instanceof BusinessObjectNode && n.getName().equals(name)) {
					tlResource.setBusinessObjectRef((TLBusinessObject) n.getTLModelObject());
					LOGGER.debug("Set subect to " + name + ": " + tlResource.getBusinessObjectRefName());
				}
	}

	public void setSubject(Node subject) {
		if (subject != null && subject.getTLModelObject() != null
				&& subject.getTLModelObject() instanceof TLBusinessObject) {
			tlResource.setBusinessObjectRef((TLBusinessObject) subject.getTLModelObject());
			// Set where used on BO
			if (subject instanceof TypeProvider)
				((TypeProvider) subject).addWhereUsed(this);
		}
	}

	/**
	 * If named resource is not a parent, set it. If it is, remove it.
	 * 
	 * @param name
	 */
	public void toggleParent(String name) {
		if (name.equals("NONE")) {
			List<TLResourceParentRef> parents = new ArrayList<TLResourceParentRef>(tlResource.getParentRefs());
			for (TLResourceParentRef ref : parents)
				tlResource.removeParentRef(ref);
			return;
		}
		TLResourceParentRef toRemove = null;
		for (TLResourceParentRef ref : tlResource.getParentRefs()) {
			if (ref.getParentResourceName() != null) {
				String rn = ref.getParentResourceName();
				if (ref.getParentResourceName().equals(name))
					toRemove = ref;
			}
		}
		if (toRemove != null) {
			tlResource.removeParentRef(toRemove);
			// LOGGER.debug("Removed parent : " + toRemove.getParentResourceName());
		} else {
			setParentRef(name);
		}
	}

	/**
	 * Create a ParentRef for the named parent resource. Note that there may be multiple references to the same parent
	 * resource with different parameter groups.
	 * 
	 * @param parentName
	 * @return the created ParentRef
	 */
	public ParentRef setParentRef(String parentName) {
		ParentRef pr = new ParentRef(this);
		pr.setParent(parentName);
		// LOGGER.debug("Added parent " + parentName + ": " + getParentRef().getParentResourceName() + " to " + this);
		return pr;
	}

	/**
	 * Create a ParentRef for named parent resource and set the parameter group. No error checking.
	 * 
	 * @param parentName
	 * @param paramGroup
	 * @return
	 */
	public ParentRef setParentRef(String parentName, String paramGroup) {
		ParentRef pr = setParentRef(parentName);
		pr.setParamGroup(paramGroup);
		return pr;
	}

	// protected void addMOChildren() {
	// if (tlResource != null) {
	// for (TLResourceParentRef parent : tlResource.getParentRefs())
	// new ParentRef(parent);
	// for (TLParamGroup tlp : tlResource.getParamGroups())
	// new ParamGroup(tlp);
	// for (TLAction action : tlResource.getActions())
	// new ActionNode(action);
	// for (TLActionFacet af : tlResource.getActionFacets())
	// new ActionFacet(af);
	// // On construction of the library, the base resource may not have node identity listeners.
	// initInherited();
	// }
	// }

	private void initInherited() {
		if (tlResource.getExtension() != null) {
			NamedEntity base = tlResource.getExtension().getExtendsEntity();
			if (base instanceof TLResource) {
				for (TLParamGroup tlInherited : ((TLResource) base).getParamGroups())
					getChildren().add(new InheritedResourceMember(tlInherited));
				for (TLActionFacet tlInherited : ((TLResource) base).getActionFacets())
					getChildren().add(new InheritedResourceMember(tlInherited));
				for (TLAction tlInherited : ((TLResource) base).getActions())
					getChildren().add(new InheritedResourceMember(tlInherited));
			}
		}
	}

	@Override
	public Collection<String> getValidationMessages() {
		ValidationFindings findings = TLModelCompileValidator.validateModelElement(tlResource);
		ArrayList<String> msgs = new ArrayList<String>();
		for (String f : findings.getValidationMessages(FindingType.ERROR, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
			msgs.add(f);
		for (String f : findings.getValidationMessages(FindingType.WARNING, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
			msgs.add(f);
		return msgs;
	}

	@Override
	public ValidationFindings getValidationFindings() {
		return TLModelCompileValidator.validateModelElement(tlResource);
	}

	@Override
	public boolean isValid() {
		return TLModelCompileValidator.validateModelElement(tlResource).count(FindingType.ERROR) == 0;
	}

	@Override
	public boolean isValid_NoWarnings() {
		return TLModelCompileValidator.validateModelElement(tlResource).count(FindingType.WARNING) == 0;
	}

	@Override
	public List<AliasNode> getAliases() {
		return Collections.emptyList();
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TLModelElement getAssignedTLObject() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * If there is no subject, then set the type to undefined not unused.
	 */
	@Override
	public TypeProvider getRequiredType() {
		return getSubject() != null ? null : ModelNode.getUndefinedNode();
	}

	/**** Extension Owner Methods ****/
	@Override
	public String getExtendsTypeNS() {
		return getExtensionBase().getNamespace();
	}

	@Override
	public Node getExtensionBase() {
		return extensionHandler != null ? extensionHandler.get() : null;
	}

	@Override
	public ExtensionHandler getExtensionHandler() {
		if (extensionHandler == null)
			extensionHandler = new ExtensionHandler(this);
		return extensionHandler;
	}

	@Override
	public void setExtension(Node base) {
		assert (base instanceof ResourceNode);
		TLExtension ext = new TLExtension();
		tlResource.setExtension(ext);
		ext.setExtendsEntity(((ResourceNode) base).getTLModelObject());
		LOGGER.debug("Set extension to " + base + ": " + tlResource.getExtension().getExtendsEntityName());

		// update inherited children
		ArrayList<InheritedResourceMember> inherited = new ArrayList<InheritedResourceMember>();
		for (Node n : getChildren())
			if (n instanceof InheritedResourceMember)
				inherited.add((InheritedResourceMember) n);
		for (Node n : inherited)
			getChildren().remove(n);
		getChildrenHandler().initInherited();
		for (Node child : getChildren())
			if (child instanceof ActionNode)
				((ActionNode) child).initInherited();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeUser#getTypeHandler()
	 */
	@Override
	public TypeUserHandler getTypeHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param child
	 */
	public void removeChild(ResourceMemberInterface child) {
		getChildrenHandler().remove((Node) child);
	}
}

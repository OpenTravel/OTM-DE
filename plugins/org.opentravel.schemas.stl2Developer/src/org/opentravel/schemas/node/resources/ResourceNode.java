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
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.modelObject.ResourceMO;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ResourceNode extends ComponentNode implements TypeUser, ResourceMemberInterface, VersionedObjectInterface,
		LibraryMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceNode.class);

	private Node subject = null;
	private TLResource tlObj = null;
	private String MSGKEY = "rest.ResourceNode";

	public class AbstractListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setAbstract(Boolean.valueOf(value));
			return false;
		}
	}

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
			tlObj.setFirstClass(Boolean.valueOf(value));
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
	public ResourceNode(TLLibraryMember mbr) {
		super(mbr);
		ListenerFactory.setListner(this);

		assert (getModelObject() != null);
		tlObj = getTLModelObject();

		addMOChildren(); // NOTE - this will fail if no library

		if (getSubject() == null)
			LOGGER.debug("No subject assigned: " + this);
		else
			getSubject().addWhereUsed(this);

		// LOGGER.debug("NOT IMPLEMENTED - resource node constructor.");
		assert true;
	}

	public ResourceNode(TLResource mbr, LibraryNode lib) {
		super(mbr);
		if (GetNode(mbr) == null)
			ListenerFactory.setListner(this);

		assert (getModelObject() != null);
		tlObj = getTLModelObject();
		lib.addMember(this);
		addMOChildren();

		if (getSubject() == null)
			LOGGER.debug("No subject assigned: " + this);
		else
			getSubject().addWhereUsed(this);
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
		tlObj = getTLModelObject();
		if (bo == null)
			tlObj.setName("NewResource"); // must be named to add to library
		else
			tlObj.setName(bo.getName() + "Resource");

		if (ln != null && ln.isEditable()) {
			ln.addMember(this);
			assert getLibrary() != null;
		} else
			LOGGER.warn("Resource not added to library. " + ln + " Is not an editable library.");
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

	public void addChild(ResourceMemberInterface child) {
		if (!getChildren().contains(child))
			getChildren().add((Node) child);
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
		throw new IllegalAccessError("Not Implemented.");
	}

	@Override
	public boolean setAssignedType() {
		throw new IllegalAccessError("Not Implemented.");
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
		// return super.createMinorVersionComponent(new ResourceNode(createMinorTLVersion(this)));
		LOGGER.debug("NOT IMPLEMENTED - createMinorVersionCompnoent for resource node.");
		return null;
	}

	@Override
	public void delete() {
		// LOGGER.debug("Deleting rest resource: " + this);
		// List<Node> kids = new ArrayList<Node>(getChildren());
		for (Node kid : getChildren_New())
			kid.delete();

		if (getParent().getChildren() != null)
			getParent().getChildren().remove(this);

		if (getChain() != null)
			getChain().removeAggregate(this);
		if (getSubject() != null)
			getSubject().removeTypeUser(this);
		parent = null;
		setLibrary(null);
		deleted = true;

		// LOGGER.debug("Deleting rest resource: " + this);
		if (tlObj.getOwningLibrary() != null)
			tlObj.getOwningLibrary().removeNamedMember(tlObj);
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
		for (Node child : getChildren())
			if (child instanceof ActionFacet)
				facets.add((ActionFacet) child);
		if (getExtendsType() != null)
			facets.addAll(getExtendsType().getActionFacets());
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

	@Override
	public String getDecoration() {
		String decoration = "";
		if (getSubject() != null)
			decoration += "  (Exposes: " + getSubject().getNameWithPrefix() + ") - ";
		String extensionTxt = " ";
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
		return decoration;
	}

	@Override
	public PropertyOwnerInterface getDefaultFacet() {
		return null;
	}

	@Override
	public String getDescription() {
		TLResource tlObj = (TLResource) getTLModelObject();
		return tlObj.getDocumentation() != null ? tlObj.getDocumentation().getDescription() : "";
	}

	// @Override
	public ResourceNode getExtendsType() {
		Node base = null;
		NamedEntity tl = null;
		if (tlObj.getExtension() != null)
			tl = tlObj.getExtension().getExtendsEntity();
		if (tl instanceof TLResource)
			base = Node.GetNode((TLResource) tl);
		return base instanceof ResourceNode ? (ResourceNode) base : null;
		// should this implement Extension Owner?
		// throw new IllegalStateException("Need to add type handler to resource.");
		// return (Node) getTypeClass().getTypeNode();
	}

	public String getExtendsEntityName() {
		return tlObj.getExtension() != null ? tlObj.getExtension().getExtendsEntityName() : "";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Extensions
		new ResourceField(fields, getExtendsEntityName(), MSGKEY + ".fields.extension", ResourceFieldType.Enum,
				new ExtensionListener(), getPeerNames());

		// Business Object = launch selection wizard
		new ResourceField(fields, getSubjectName(), MSGKEY + ".fields.businessObject", ResourceFieldType.ObjectSelect,
				!isAbstract(), new SubjectListener(), this);

		// Base Path
		new ResourceField(fields, tlObj.getBasePath(), MSGKEY + ".fields.basePath", ResourceFieldType.String,
				!isAbstract(), new BasePathListener());

		// Abstract - yes/no button
		new ResourceField(fields, Boolean.toString(tlObj.isAbstract()), MSGKEY + ".fields.abstract",
				ResourceFieldType.CheckButton, new AbstractListener());

		// First Class - yes/no button
		new ResourceField(fields, Boolean.toString(tlObj.isFirstClass()), MSGKEY + ".fields.firstClass",
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
		// for (ActionNode action : getActions())
		// if (action.getRequest().getHttpMethodAsString().equals(method.toString()))
		// contribution = action.getRequest().getPathTemplate();
		return contribution;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Resource);
	}

	@Override
	public String getLabel() {
		// if (getSubject() == null)
		// if (getExtendsType() == null)
		return super.getLabel();
		// else
		// return super.getLabel() + "(Exposes: " + getSubject() + ")";
		// else if (isVersioned())
		// else if (getExtendsType().getName().equals(getName()))
		// return super.getLabel() + " (Extends version:  " + getExtendsType().getLibrary().getVersion() + ")";
		// else
		// return super.getLabel() + " (Extends: " + getExtendsType().getNameWithPrefix() + ")";
	}

	@Override
	public ResourceMO getModelObject() {
		ModelObject<?> obj = super.getModelObject();
		return (ResourceMO) (obj instanceof ResourceMO ? obj : null);
	}

	@Override
	public AbstractLibrary getTLOwner() {
		return tlObj.getOwningLibrary();
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return Collections.emptyList();
	}

	public List<ParamGroup> getParameterGroups(boolean idGroupsOnly) {
		ArrayList<ParamGroup> pgroups = new ArrayList<ParamGroup>();
		for (Node child : getChildren())
			if (child instanceof ParamGroup)
				if (!idGroupsOnly || ((ParamGroup) child).isIdGroup())
					pgroups.add((ParamGroup) child);
		return pgroups;
	}

	/**
	 * @param idGroupsOnly
	 *            if true only parameter groups with ID Group set will be included as needed for parent references
	 * @return list of the parameter groups by name and NONE
	 */
	public String[] getParameterGroupNames(boolean idGroupsOnly) {
		List<ParamGroup> paramGroups = getParameterGroups(idGroupsOnly);
		String[] groupNames = new String[paramGroups.size()];
		int i = 0;
		groupNames[i] = ResourceField.NONE;
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
		String[] parents = new String[tlObj.getParentRefs().size()];
		int i = 0;
		for (TLResourceParentRef parent : tlObj.getParentRefs())
			if (parent.getParentResourceName() != null)
				parents[i++] = parent.getParentResourceName();
		return parents;
	}

	/**
	 * @return an array of other resources including NONE
	 */
	public String[] getPeerNames() {
		int size = 1;
		if (getParent() != null)
			size = getParent().getChildren().size();
		String[] peers = new String[size];
		int i = 0;
		peers[i++] = ResourceField.NONE;
		if (getParent() != null)
			for (Node n : getParent().getChildren())
				if (n != this)
					peers[i++] = n.getName();
		return peers;
	}

	public ResourceNode getPeerByName(String name) {
		ResourceNode peer = null;
		for (Node n : getParent().getChildren())
			if (n.getName().equals(name))
				peer = (ResourceNode) n;
		return peer;
	}

	public BusinessObjectNode getSubject() {
		if (tlObj != null && tlObj.getBusinessObjectRef() != null)
			subject = this.getNode(tlObj.getBusinessObjectRef().getListeners());
		return (BusinessObjectNode) subject;
	}

	public String getSubjectName() {
		return tlObj.getBusinessObjectRef() != null ? tlObj.getBusinessObjectRef().getLocalName() : "None";
	}

	/**
	 * @return a list of business objects by name including "NONE"
	 */
	public String[] getSubjectCandidates() {
		if (getLibrary() == null)
			return new String[0];
		List<Node> subjects = new ArrayList<Node>();
		for (Node n : getLibrary().getDescendants_LibraryMembers())
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
		List<FacetNode> facets = new ArrayList<FacetNode>();
		for (Node facet : subject.getChildren())
			if (facet instanceof FacetNode)
				facets.add((FacetNode) facet);
		int size = facets.size();
		if (includeSubGrp)
			size += 1;

		String[] fs = new String[size];
		int i = 0;
		if (includeSubGrp)
			fs[i++] = ResourceField.SUBGRP;
		for (Node facet : subject.getChildren())
			if (facet instanceof FacetNode)
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
		if (tlObj == null)
			tlObj = (TLResource) modelObject.getTLModelObj();
		return tlObj;
	};

	@Override
	public boolean hasNavChildren(boolean deep) {
		return false;
	}

	// @Override
	// public boolean hasNavChildrenWithProperties() {
	// return false;
	// }

	public boolean isAbstract() {
		return tlObj.isAbstract();
	}

	public void setAbstract(boolean flag) {
		tlObj.setAbstract(flag);
		// LOGGER.debug("Set abstract to: " + tlObj.isAbstract());
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
	public boolean isEditable() {
		return getLibrary().isEditable();
	}

	@Override
	public boolean isMergeSupported() {
		return false;
	}

	// @Override
	// public boolean isNamedType() {
	// return false;
	// }

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
		return tlObj.getBasePath();
	}

	public void setBasePath(String path) {
		// if (!path.endsWith("/"))
		// path = path + "/";
		tlObj.setBasePath(path);
		// LOGGER.debug("Set base path to " + path + ": " + tlObj.getBasePath());
	}

	@Override
	public void setName(String n) {
		// this.setName(n, false);
		tlObj.setName(n);
	}

	// @Override
	// public void setName(String n, boolean doFamily) {
	// // super.setName(n, doFamily);
	// // tlObj.setName(n);
	// // There are no type users -- resources are not type assignable
	// // for (Node user : getTypeUsers()) {
	// // if (user instanceof PropertyNode)
	// // user.setName(n);
	// // }
	// }

	public boolean setExtension(String name) {
		ResourceNode peer = getPeerByName(name);
		if (peer != null) {
			TLExtension ext = new TLExtension();
			tlObj.setExtension(ext);
			ext.setExtendsEntity(peer.getTLModelObject());
			// LOGGER.debug("Set extension to " + name + ": " + tlObj.getExtension().getExtendsEntityName());
		} else {
			tlObj.setExtension(null);
			// LOGGER.debug("Set extension to null:" + tlObj.getExtension());
		}
		return false;
	}

	public void setSubject(String name) {
		if (name == null || name.equals(ResourceField.NONE)) {
			tlObj.setBusinessObjectRef(null);
			tlObj.setBusinessObjectRefName("");
			// LOGGER.debug("Set subject to null.");
		}
		for (Node n : getLibrary().getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode && n.getName().equals(name)) {
				tlObj.setBusinessObjectRef((TLBusinessObject) n.getTLModelObject());
				// LOGGER.debug("Set subect to " + name + ": " + tlObj.getBusinessObjectRefName());
			}
	}

	public void setSubject(Node subject) {
		if (subject != null && subject.getTLModelObject() != null
				&& subject.getTLModelObject() instanceof TLBusinessObject) {
			tlObj.setBusinessObjectRef((TLBusinessObject) subject.getTLModelObject());
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
			List<TLResourceParentRef> parents = new ArrayList<TLResourceParentRef>(tlObj.getParentRefs());
			for (TLResourceParentRef ref : parents)
				tlObj.removeParentRef(ref);
			return;
		}
		TLResourceParentRef toRemove = null;
		for (TLResourceParentRef ref : tlObj.getParentRefs()) {
			if (ref.getParentResourceName() != null) {
				String rn = ref.getParentResourceName();
				if (ref.getParentResourceName().equals(name))
					toRemove = ref;
			}
		}
		if (toRemove != null) {
			tlObj.removeParentRef(toRemove);
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

	@Override
	protected void addMOChildren() {
		if (tlObj != null) {
			for (TLResourceParentRef parent : tlObj.getParentRefs())
				new ParentRef(parent);
			for (TLParamGroup tlp : tlObj.getParamGroups())
				new ParamGroup(tlp);
			for (TLAction action : tlObj.getActions())
				new ActionNode(action);
			for (TLActionFacet af : tlObj.getActionFacets())
				new ActionFacet(af);
		}
	}

	@Override
	public Collection<String> getValidationMessages() {
		ValidationFindings findings = TLModelCompileValidator.validateModelElement((TLModelElement) tlObj);
		ArrayList<String> msgs = new ArrayList<String>();
		for (String f : findings.getValidationMessages(FindingType.ERROR, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
			msgs.add(f);
		for (String f : findings.getValidationMessages(FindingType.WARNING, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
			msgs.add(f);
		return msgs;
	}

	@Override
	public ValidationFindings getValidationFindings() {
		return TLModelCompileValidator.validateModelElement((TLModelElement) tlObj);
	}

	@Override
	public boolean isValid() {
		return TLModelCompileValidator.validateModelElement(tlObj).count(FindingType.ERROR) == 0;
	}

	@Override
	public boolean isValid_NoWarnings() {
		return TLModelCompileValidator.validateModelElement((TLModelElement) tlObj).count(FindingType.WARNING) == 0;
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
		return subject != null ? null : ModelNode.getUndefinedNode();
	}
}

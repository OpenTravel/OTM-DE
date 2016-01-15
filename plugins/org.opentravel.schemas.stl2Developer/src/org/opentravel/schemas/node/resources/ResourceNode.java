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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
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
import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
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
			tlObj.setAbstract(Boolean.valueOf(value));
			LOGGER.debug("Set abstract to: " + tlObj.isAbstract());
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
			LOGGER.debug("Set first class to: " + tlObj.isFirstClass());
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
	}

	public class ExtensionListener implements ResourceFieldListener {
		@Override
		public boolean set(String name) {
			// setSubject(name);
			return false;
		}
	}

	/**************************************************************
	 * 
	 */
	public ResourceNode(LibraryMember mbr) {
		super(mbr);
		ListenerFactory.setListner(this);

		assert (getModelObject() != null);
		tlObj = getTLModelObject();
		assert tlObj != null;
		if (getSubject() == null)
			// assert getType() == ModelNode.getUndefinedNode();
			LOGGER.debug("No subject assigned: " + getType());
		addMOChildren();
	}

	@Override
	public boolean setAssignedType(Node type) {
		LOGGER.debug("Tried to set assigned type: " + getType());
		return false;
	}

	// /**
	// *
	// * @param mbr
	// * - tlResource to control
	// * @param libraryMember
	// * - in the library where this resource will be added
	// */
	// public ResourceNode(TLResource mbr, Node libraryMember) {
	// this(mbr);
	// libraryMember.getLibrary().addMember(this);
	// }

	/**
	 * Create a resource in the library of the libraryMember.
	 */
	public ResourceNode(Node libraryMember) {
		super(new TLResource());
		tlObj = getTLModelObject();
		if (libraryMember == null || libraryMember.getName().isEmpty())
			tlObj.setName("NewResource"); // must be named to add to library
		else
			tlObj.setName(libraryMember.getName() + "Resource");
		if (libraryMember != null)
			libraryMember.getLibrary().addMember(this);
	}

	/**
	 * Create a resource in the library of the libraryMember.
	 */
	public ResourceNode(BusinessObjectNode businessObject) {
		super(new ResourceBuilder().buildTL(businessObject));
		tlObj = getTLModelObject();
		businessObject.getLibrary().addMember(this);
	}

	public void addChild(ResourceMemberInterface child) {
		if (!getChildren().contains(child))
			getChildren().add((Node) child);
	}

	public String getMsgKey() {
		return MSGKEY;
	}

	@Override
	public boolean canExtend() {
		return true;
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new ResourceNode(createMinorTLVersion(this)));
	}

	public List<ActionNode> getActions() {
		ArrayList<ActionNode> actions = new ArrayList<ActionNode>();
		for (Node child : getChildren())
			if (child instanceof ActionNode)
				actions.add((ActionNode) child);
		return actions;
	}

	public List<ActionFacet> getActionFacets() {
		ArrayList<ActionFacet> facets = new ArrayList<ActionFacet>();
		for (Node child : getChildren())
			if (child instanceof ActionFacet)
				facets.add((ActionFacet) child);
		return facets;
	}

	/**
	 * @return An array of action facet names and NONE
	 */
	public String[] getActionFacetsNames() {
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

	@Override
	public Node getAssignedType() {
		return getSubject();
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.RESOURCE;
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

	@Override
	public Node getExtendsType() {
		return getTypeClass().getTypeNode();
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Extensions
		new ResourceField(fields, tlObj.getExtension().getExtendsEntityName(), MSGKEY + ".fields.extension",
				ResourceFieldType.Enum, new ExtensionListener(), getPeerNames());

		// Business Object = Get a list of business objects to use as enum values
		new ResourceField(fields, tlObj.getBusinessObjectRefName(), MSGKEY + ".fields.businessObject",
				ResourceFieldType.Enum, new SubjectListener(), getSubjectCandidates());

		// Base Path
		new ResourceField(fields, tlObj.getBasePath(), MSGKEY + ".fields.basePath", ResourceFieldType.String,
				new BasePathListener());

		// Parent Ref = Use an enum list to present all the possible parents and all peers
		new ResourceField(fields, Arrays.toString(getParentRefNames()), MSGKEY + ".fields.parentRef",
				ResourceFieldType.EnumList, new ParentRefListener(), getPeerNames());

		// Abstract - yes/no button
		new ResourceField(fields, Boolean.toString(tlObj.isAbstract()), MSGKEY + ".fields.abstract",
				ResourceFieldType.CheckButton, new AbstractListener());

		// First Class - yes/no button
		new ResourceField(fields, Boolean.toString(tlObj.isFirstClass()), MSGKEY + ".fields.firstClass",
				ResourceFieldType.CheckButton, new FirstClassListener());

		return fields;

	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Resource);
	}

	@Override
	public String getLabel() {
		if (getSubject() == null)
			// if (getExtendsType() == null)
			return super.getLabel();
		else
			return super.getLabel() + "(Exposes: " + getSubject() + ")";
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
	public List<Node> getNavChildren() {
		return Collections.emptyList();
	}

	public List<ParamGroup> getParameterGroups() {
		ArrayList<ParamGroup> facets = new ArrayList<ParamGroup>();
		for (Node child : getChildren())
			if (child instanceof ParamGroup)
				facets.add((ParamGroup) child);
		return facets;
	}

	/**
	 * @return list of the parameter groups by name and NONE
	 */
	public String[] getParamGroupNames() {
		List<ParamGroup> paramGroups = getParameterGroups();
		String[] groupNames = new String[paramGroups.size() + 1];
		int i = 0;
		groupNames[i] = ResourceField.NONE;
		for (Node n : paramGroups)
			groupNames[i++] = n.getName();
		return groupNames;
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
		String[] peers = new String[getParent().getChildren().size()];
		int i = 0;
		peers[i++] = ResourceField.NONE;
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

	/**
	 * @return a list of business objects by name including "NONE"
	 */
	public String[] getSubjectCandidates() {
		List<Node> subjects = new ArrayList<Node>();
		for (Node n : getLibrary().getDescendants_NamedTypes())
			if (n instanceof BusinessObjectNode)
				subjects.add(n);
		String[] names = new String[subjects.size() + 1];
		int i = 0;
		names[i++] = ResourceField.NONE;
		for (Node n : subjects)
			names[i++] = n.getName();
		return names;
	}

	public String[] getSubjectFacets() {
		if (getSubject() == null)
			return new String[0];
		String[] fs = new String[subject.getChildren().size()];
		int i = 0;
		for (Node facet : subject.getChildren())
			if (facet instanceof FacetNode)
				fs[i++] = facet.getLabel();
		return fs;
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public TLResource getTLModelObject() {
		if (tlObj == null)
			tlObj = (TLResource) modelObject.getTLModelObj();
		return tlObj;
	};

	@Override
	public boolean hasNavChildren() {
		return false;
		// return !getChildren().isEmpty();
	}

	@Override
	public boolean isAssignedByReference() {
		return true;
	}

	@Override
	public boolean isMergeSupported() {
		return false;
	}

	@Override
	public boolean isNamedType() {
		return false;
	}

	@Override
	public boolean isNameEditable() {
		return true;
	}

	@Override
	public boolean isTypeUser() {
		return true;
	}

	public void setBasePath(String path) {
		// if (!path.endsWith("/"))
		// path = path + "/";
		tlObj.setBasePath(path);
		LOGGER.debug("Set base path to " + path + ": " + tlObj.getBasePath());
	}

	@Override
	public void setName(String n) {
		this.setName(n, false);
	}

	@Override
	public void setName(String n, boolean doFamily) {
		super.setName(n, doFamily);
		for (Node user : getTypeUsers()) {
			if (user instanceof PropertyNode)
				user.setName(n);
		}
	}

	public boolean setExtension(String name) {
		TLExtension ext = new TLExtension();
		ext.setExtendsEntity(getPeerByName(name).getTLModelObject());
		tlObj.setExtension(ext);
		LOGGER.debug("Set extension to " + name + ": " + tlObj.getExtension().getExtendsEntityName());
		return false;
	}

	public void setSubject(String name) {
		if (name.equals(ResourceField.NONE)) {
			tlObj.setBusinessObjectRef(null);
			LOGGER.debug("Set subject to null.");
		}
		for (Node n : getLibrary().getDescendants_NamedTypes())
			if (n instanceof BusinessObjectNode && n.getName().equals(name)) {
				tlObj.setBusinessObjectRef((TLBusinessObject) n.getTLModelObject());
				LOGGER.debug("Set subect to " + name + ": " + tlObj.getBusinessObjectRefName());
			}
	}

	/**
	 * If named resource is not a parent, set it. If it is, remove it.
	 * 
	 * @param name
	 */
	public void toggleParent(String name) {
		if (name.equals("NONE")) {
			for (TLResourceParentRef ref : tlObj.getParentRefs())
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
			LOGGER.debug("Removed parent : " + toRemove.getParentResourceName());
		} else {
			TLResourceParentRef ref = new TLResourceParentRef();
			Node owner = getPeerByName(name);
			if (owner != null) {
				ref.setParentResource((TLResource) owner.getTLModelObject());
				tlObj.addParentRef(ref);
				LOGGER.debug("Added parent " + name + ": " + ref.getParentResourceName());
			}
		}
	}

	@Override
	protected void addMOChildren() {
		if (tlObj != null) {
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
}

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
package org.opentravel.schemas.node.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Parent Reference controller. Provides getters, setters and listeners for parameter group fields.
 * 
 * @author Dave
 *
 */
public class ParentRef extends ResourceBase<TLResourceParentRef> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParentRef.class);
	private String MSGKEY = "rest.ResourceParentRef";

	public class ParamGroupListener implements ResourceFieldListener {
		@Override
		public boolean set(String name) {
			return setParamGroup(name);
		}
	}

	class ParentRefListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setParent(value);
			return false;
		}
	}

	class PathTemplateListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setPathTemplate(value);
			return false;
		}
	}

	/********************************************************************************
	 * 
	 */
	public ParentRef(TLResourceParentRef tlParentRef) {
		super(tlParentRef);
		createPathTemplate();
	}

	/**
	 * Create an empty ParentRef and add to parent's children
	 * 
	 * @param parent
	 */
	public ParentRef(ResourceNode parent) {
		super(new TLResourceParentRef(), parent);

		createPathTemplate();
		getParent().getTLModelObject().addParentRef(tlObj);
		tlObj.setParentResource(getParent().getTLModelObject());
	}

	@Override
	public void addChildren() {
	}

	@Override
	public void delete() {
		if (tlObj.getOwner() != null)
			tlObj.getOwner().removeParentRef(tlObj);
		getParent().removeChild(this);
		setPathTemplate(null);
		super.delete();
	}

	@Override
	public String getComponentType() {
		return "Resource Parent";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Parent
		new ResourceField(fields, getParentResourceName(), MSGKEY + ".parent", ResourceFieldType.Enum,
				new ParentRefListener(), getParentCandidateNames());

		// Parameter Group
		new ResourceField(fields, getParameterGroupName(), MSGKEY + ".parentParamGroup", ResourceFieldType.Enum,
				new ParamGroupListener(), getParentParamGroupNames());

		// Path Template - simple String
		new ResourceField(fields, tlObj.getPathTemplate(), MSGKEY + ".pathTemplate",
				ResourceField.ResourceFieldType.String, new PathTemplateListener());

		return fields;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ResourceParentRef);
	}

	@Override
	public String getName() {
		return tlObj.getParentResourceName() + "Ref";
		// return tlObj.getName() != null ? tlObj.getName() : "";
	}

	public ParamGroup getParameterGroup() {
		return (ParamGroup) Node.GetNode(tlObj.getParentParamGroup());
		// return null;
	}

	public String getParameterGroupName() {
		return tlObj.getParentParamGroupName();
	}

	private static final String[] EmptyStringArray = {};

	public String[] getParentParamGroupNames() {
		if (getParentResource() != null)
			return getParentResource().getParameterGroupNames(true);
		return EmptyStringArray;
	}

	public String getUrlContribution() {
		return tlObj.getPathTemplate() != null ? tlObj.getPathTemplate() : "";
	}

	/**
	 * If the path template is empty, get a candidate from the parent resource.
	 * 
	 * @return
	 */
	private String createPathTemplate() {
		String contribution = tlObj.getPathTemplate();
		if (contribution == null || contribution.isEmpty()) {
			// See if the parent resource has a grand-parent with contribution
			ParamGroup pg = getParameterGroup();
			if (pg != null && !deleted)
				contribution += getParentResource().getBasePath() + pg.getPathTemplate();
			setPathTemplate(contribution);
		}
		return contribution;
	}

	public String getPathTemplate() {
		return tlObj.getPathTemplate();
	}

	@Override
	public ResourceNode getParent() {
		return (ResourceNode) parent;
	}

	public String[] getParentCandidateNames() {
		List<ResourceNode> list = getParentCandidates();
		String[] candidates = new String[list.size()];
		int i = 0;
		for (ResourceNode rn : list) {
			candidates[i++] = rn.getNameWithPrefix();
		}
		return candidates;
	}

	public List<ResourceNode> getParentCandidates() {
		List<ResourceNode> candidates = new ArrayList<ResourceNode>();
		for (Node rn : getOwningComponent().getSiblings())
			candidates.add((ResourceNode) rn);
		candidates.addAll(getModelNode().getAllResources());
		return candidates;
	}

	/**
	 * 
	 * @return the resource node that this parent reference is pointing to (not the parent of this ParentRef).
	 */
	public ResourceNode getParentResource() {
		return tlObj.getParentResource() != null ? (ResourceNode) getNode(tlObj.getParentResource().getListeners())
				: null;
	}

	/**
	 * @return parent name with prefix
	 */
	public String getParentResourceName() {
		return getParentResource() != null ? getParentResource().getNameWithPrefix() : "";
	}

	@Override
	public TLResourceParentRef getTLModelObject() {
		return tlObj;
	}

	@Override
	public TLResource getTLOwner() {
		return tlObj.getOwner();
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public boolean isNameEditable() {
		return true;
	}

	/**
	 * Set the name of this parameter group
	 */
	@Override
	public void setName(final String name) {
		// tlObj.setName(name);
	}

	public boolean setParamGroup(String name) {
		ParamGroup pg = null;
		for (ParamGroup group : getParentResource().getParameterGroups(true))
			if (group.getName().equals(name))
				pg = group;
		// set the group and update the path template
		if (pg != null) {
			tlObj.setParentParamGroup(pg.getTLModelObject());
			setPathTemplate(""); // force the create to work
			createPathTemplate();
		} else {
			tlObj.setParentParamGroup(null);
			tlObj.setPathTemplate("/");
		}
		return true;
	}

	public void setParent(String name) {
		ResourceNode parentResource = null;
		for (ResourceNode p : getParentCandidates())
			if (p.getNameWithPrefix().equals(name))
				parentResource = p;
		if (parentResource != null) {
			tlObj.setParentResource(parentResource.getTLModelObject());

			String template = "/" + parentResource.getSubjectName();
			// If the parent only has one ID parameter group then use it
			List<ParamGroup> pgs = parentResource.getParameterGroups(true);
			if (pgs.size() == 1) {
				setParamGroup(pgs.get(0).getName());
				template += "/" + pgs.get(0).getPathTemplate();
			}

			// Set the path template
			setPathTemplate(template);

			// Update Examples
			getParent().updateExamples();

		}

		// LOGGER.debug("Set Parent resource to " + name + ": " + tlObj.getParentResourceName());
	}

	public void setPathTemplate(String path) {
		if (path != null)
			path = path.replaceAll("//", "/");
		tlObj.setPathTemplate(path);
		// LOGGER.debug("Set Path template to " + path + ": " + tlObj.getPathTemplate());
	}

}

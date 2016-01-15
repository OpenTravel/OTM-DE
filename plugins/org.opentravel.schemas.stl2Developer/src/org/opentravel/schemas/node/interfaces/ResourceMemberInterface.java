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
package org.opentravel.schemas.node.interfaces;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.resources.ResourceField;

/**
 * All resource controllers must implement this interface.
 * 
 * @author Dave
 *
 */
public interface ResourceMemberInterface {

	/**
	 * Delete this node and all of its underlying children. Does not delete nodes that are not delete-able. Removes from
	 * their parent's child list. Removes all nodes from type and base-type user lists. Removes INodeListeners and TL
	 * Entity.
	 */
	public void delete();

	/**
	 * Returns the live children list.
	 * 
	 * @return
	 */
	public List<Node> getChildren();

	public String getComponentType();

	public String getDescription();

	/**
	 * @return an array of key/value pairs where the keys are defined in messages.properties
	 */
	public List<ResourceField> getFields();

	/**
	 * @return the image used to represent this node in the GUI.
	 */
	public Image getImage();

	public String getLabel();

	public String getName();

	public INode getOwningComponent();

	public INode getParent();

	public TLModelElement getTLModelObject();

	public String getTooltip();

	public ValidationFindings getValidationFindings();

	public Collection<String> getValidationMessages();

	public boolean hasChildren();

	public boolean isNameEditable();

	/**
	 * @return true if there are no validation errors on this item.
	 */
	public boolean isValid();

	public void setDescription(String description);

	public void setName(String name);

	/**
	 * @return true if there are no validation warnings on this item.
	 */
	boolean isValid_NoWarnings();

}

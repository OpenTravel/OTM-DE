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
package org.opentravel.schemas.node.facets;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for custom, choice, query and update facets.
 * 
 * Contextual facets can be either in the library or in a different library from the object they contextualize.
 * 
 * contextual facets collaborate with their owning object. They add facets without having dependacies between the
 * object's library to the facet's library.
 * 
 * Contextual facets enable OTM to have dependency injection or Inversion of Control.
 * 
 * @author Dave Hollander
 * 
 */
public class ContextualFacetNode extends FacetNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextualFacetNode.class);

	private ContributedFacetNode whereContributed = null;

	public ContextualFacetNode(TLContextualFacet tlObj) {
		super(tlObj);
		setContext();

		// If this is not a local facet, then set the contributed facet node onto the owner
		Node owner = getOwningComponent();
		if (!isLocal() && owner != null) {
			LOGGER.debug("contributed facet: ");
			whereContributed = new ContributedFacetNode(this);
			owner.linkChild(whereContributed);
			whereContributed.setLibrary(owner.getLibrary());
		}
	}

	public ContextualFacetNode() {
		// super();
		// Contributed facets do not have MO or TL objects.
	}

	@Override
	public String getDecoration() {
		if (OTM16Upgrade.otm16Enabled) {
			if (!isLocal())
				if (getOwningComponent().getLibrary() != getLibrary())
					return " : Contributes to " + getOwningComponent().getLibrary();
				else
					return " : Contributed from " + getLibrary();
		}
		return "";
	}

	@Override
	public Image getImage() {
		if (OTM16Upgrade.otm16Enabled) {
			if (whereContributed == null)
				return Images.getImageRegistry().get(Images.Facet);
			if (!isLocal())
				return Images.getImageRegistry().get(Images.ContextualFacet);
		}
		return Images.getImageRegistry().get(Images.Facet);
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public String getName() {
		return getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public Node getOwningComponent() {
		return Node.GetNode(getTLModelObject().getOwningEntity());
	}

	@Override
	public TLContextualFacet getTLModelObject() {
		return (TLContextualFacet) modelObject.getTLModelObj();
	}

	@Override
	public FacetMO getModelObject() {
		return (FacetMO) modelObject;
	}

	@Override
	public boolean isInheritedProperty() {
		return getModelObject().isInherited();
	}

	/**
	 * @return true if this facet is declared in the same library as the object it contributes to. Always true for
	 *         versions 1.5 and earlier
	 */
	@Override
	public boolean isLocal() {
		return OTM16Upgrade.otm16Enabled ? getTLModelObject().isLocalFacet() : true;
	}

	/**
	 * @return true if this facet is re-nameable.
	 */
	@Override
	public boolean isRenameable() {
		return true;
	}

	public void print() {
		if (OTM16Upgrade.otm16Enabled) {
			LOGGER.debug("Contextual facet: " + getName());
			LOGGER.debug("   Label: " + getLabel());
			LOGGER.debug("   Is Local? " + getTLModelObject().isLocalFacet());
			LOGGER.debug("   Owner: " + getOwningComponent());
			LOGGER.debug("   Type: " + getTLModelObject().getFacetType());
		} else
			LOGGER.debug("Contextual facet as Version 1.5 facet.");
	}

	@Override
	public void setContext() {
		if (getLibrary() != null)
			getTLModelObject().setContext(getLibrary().getDefaultContextId());
		// very common
		// else
		// LOGGER.warn("Can't set initial context on " + this);
	}

	/**
	 * Set context to library's default context ID.
	 * 
	 * @param context
	 *            is IGNORED. Context is not used on facets in version 1.6 and later.
	 */
	public void setContext(String context) {
		setContext();
		// if (context == null)
		// context = getLibrary().getDefaultContextId();
		// getTLModelObject().setContext(context);
	}

	@Override
	public void setName(String n) {
		String name = n;
		// Strip the object name and "query" string if present.
		name = NodeNameUtils.stripFacetPrefix(this, name);
		if (getModelObject() != null) {
			getTLModelObject().setName(name);
			// rename their type users as well.
			for (TypeUser user : getWhereAssigned()) {
				user.setName(getName());
			}
		}
	}

}

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

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
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
 * NOTE: because the TLModelObject comes from the contributor, the identity from GetNode() will be the contributor.
 * 
 * @author Dave Hollander
 * 
 */
public class ContributedFacetNode extends ContextualFacetNode implements FacadeInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContributedFacetNode.class);

	private ContextualFacetNode contributor = null;
	private TLContextualFacet tlContributor = null;

	public ContributedFacetNode(TLContextualFacet tlCF) {
		// Hold on to the contextual facet for resolution by type resolver
		setContributor(tlCF);
		LOGGER.debug("Contributed Facet Created: " + tlCF.getLocalName());
	}

	/**
	 * Create contributed facet for the passed owner. Add to TL Model and owner node.
	 * 
	 * @param tlCF
	 * @param owner
	 */
	public ContributedFacetNode(TLContextualFacet tlCF, ContextualFacetOwnerInterface owner) {
		this(tlCF);
		// Make sure this contextual facet is owned by the owning tlObject
		addToTLParent(owner.getTLModelObject());
		((Node) owner).linkChild(this);
	}

	@Override
	public void addProperties(List<Node> properties, boolean clone) {
		getContributor().addProperties(properties, clone);
	}

	@Override
	public void addProperty(PropertyNode property) {
		getContributor().addProperty(property);
	}

	@Override
	public boolean canOwn(ContextualFacetNode targetCF) {
		return getContributor() != null ? getContributor().canOwn(targetCF) : false;
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		return getContributor() != null ? getContributor().canOwn(type) : false;
	}

	@Override
	public void copyFacet(PropertyOwnerNode sourceFacet) {
		getContributor().copyFacet(sourceFacet);
	}

	@Override
	public INode createProperty(final Node type) {
		return getContributor().createProperty(type);
	}

	@Override
	public void delete() {
		Node oldParent = getParent();
		assert oldParent.getChildren().contains(this);

		// Remove where contributed and delete from TL Model then normal delete
		tlContributor = null; // used by getContributor for lazy evaluation
		if (getContributor() != null) {
			getContributor().removeFromTLParent();
			getContributor().setWhereContributed(null);
		}
		if (!inherited) {
			((TypeProvider) this).removeAll();
			unlinkNode();
			deleted = true;
			setLibrary(null);
			// super.delete(); // Delete will remove children.
		}
		assert !oldParent.getChildren().contains(this);
		assert getTLModelObject() == null || getTLModelObject().getOwningEntity() == null;
	}

	/**
	 * Getter for the contributor
	 */
	public Node get() {
		return contributor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Node> getChildren() {
		// contributor can be null and children called during constructor super() call.
		return (List<Node>) (getContributor() != null ? getContributor().getChildren() : Collections.emptyList());
	}

	@Override
	public String getComponentType() {
		return getTLModelObject() != null ? getLabel() : "";
	}

	/**
	 * Get the contextual facet that this contributed facet represents. Resolved by lazy evaluation when accessed
	 * 
	 * @return
	 */
	public ContextualFacetNode getContributor() {
		if (contributor == null)
			setContributor(null); // try to resolve using listeners
		return contributor;
	}

	@Override
	public String getDecoration() {
		String decoration = "";
		// decoration += " :CF " + nodeID;
		if (inherited)
			decoration += " : Inherited ";
		if (getContributor() != null) {
			decoration += "   : " + getContributor().getLabel();
			// contributedFacetNode overloads getLibrary
			if (getContributor().getLibrary() != this.library)
				decoration += " contributed from " + getLibrary();
			else
				decoration += " contributed from this library";
		}
		return decoration;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ContextualFacet);
		// return Images.getImageRegistry().get(Images.ContributedFacet);
	}

	@Override
	public String getLabel() {
		return super.getLabel() + " (Contributed)";
	}

	@Override
	public LibraryNode getLibrary() {
		return getContributor() == null ? null : getContributor().getLibrary();
	}

	public String getLocalName() {
		return getContributor() == null ? "" : getContributor().getLocalName();
	}

	@Override
	public FacetMO getModelObject() {
		return getContributor() == null ? null : getContributor().getModelObject();
	}

	@Override
	public TLContextualFacet getTLModelObject() {
		return getContributor() == null ? tlContributor : getContributor().getTLModelObject();
	}

	@Override
	public boolean linkChild(final Node child) {
		return false;
	}

	public void print() {
		if (OTM16Upgrade.otm16Enabled) {
			LOGGER.debug("Contributed facet: " + getName());
			LOGGER.debug("   Label: " + getLabel());
			LOGGER.debug("   Is Local? " + getTLModelObject().isLocalFacet());
			LOGGER.debug("   Owner: " + getOwningComponent());
			LOGGER.debug("   Type: " + getTLModelObject().getFacetType());
		} else
			LOGGER.debug("Contextual facet as Version 1.5 facet.");
	}

	/**
	 * Set the contributing Contextual Facet. Use the listener on the TLContextualFacet to find the contributing
	 * ContextualFacetNode
	 * 
	 * @param tlCF
	 *            the contributing facet. if NULL the existing TL Contributor is used.
	 */
	public void setContributor(TLContextualFacet tlCF) {
		if (tlCF != null)
			tlContributor = tlCF;
		Node n = GetNode(tlContributor);
		if (n instanceof ContextualFacetNode)
			contributor = (ContextualFacetNode) n;
		if (contributor != null)
			contributor.setWhereContributed(this);
		// else
		// LOGGER.warn("Could not set contributor on : " + this);
	}

	public void setOwner(ContextualFacetOwnerInterface owner) {
	}

	@Override
	protected void addToTLParent(TLFacetOwner tlOwner) {
		if (contributor != null)
			contributor.addToTLParent(tlOwner);
	}

	@Override
	protected void removeFromTLParent() {
		if (contributor != null)
			contributor.removeFromTLParent();
	}

}

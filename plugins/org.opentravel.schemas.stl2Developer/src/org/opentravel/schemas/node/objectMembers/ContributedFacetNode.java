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

package org.opentravel.schemas.node.objectMembers;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.FacetProviderChildrenHandler;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Used in OTM Version 1.6 and later for custom, choice, query and update facets.
 * 
 * Contributed facets are a facade for contextual facets. Contributed facets are children of the object being
 * contributed to. The contributed facet is a child of the object being injected and the contributed facet's contributor
 * field identifies the contextual facet.
 * <p>
 * In version 1.6 and later, contextual facets can be either in the library or in a different library from the object
 * they contextualize.
 * <p>
 * These implement InheritedInterface because they are already a facade and even if inherited they would only appear in
 * the navigator tree once.
 * 
 * Contextual facets enable OTM to have dependency injection or Inversion of Control. NOTE: because the TLModelObject
 * comes from the contributor, the identity from GetNode() will be the contributor.
 * 
 * @author Dave Hollander
 * 
 */
public class ContributedFacetNode extends FacadeBase implements FacetInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger( ContributedFacetNode.class );

    private ContextualFacetNode contributor = null;
    private TLContextualFacet tlContributor = null;

    /**
     * Empty constructor for use in creating facades.
     */
    // @Deprecated
    public ContributedFacetNode() {
        // no-op
    }

    @Override
    public TLFacetType getFacetType() {
        return tlContributor.getFacetType();
    }

    /**
     * Create contributed facet and simply set TL contributor.
     * 
     * @param tlCF
     */
    public ContributedFacetNode(TLContextualFacet tlCF) {
        // Hold on to the contextual facet for resolution by type resolver
        setContributor( tlCF );
        childrenHandler = null; // contributor owns the children not this
        // LOGGER.debug("Contributed Facet Created: " + tlCF.getLocalName());
    }

    /**
     * Create contributed facet for the passed owner. Add to TL Model and owner node.
     * 
     * @param tlCF
     * @param owner
     */
    public ContributedFacetNode(TLContextualFacet tlCF, ContextualFacetOwnerInterface owner) {
        this( tlCF );
        // Clear parent children cache so they rebuild on next getChildren() call
        ((Node) owner).getChildrenHandler().clear();
        parent = (Node) owner;
    }

    @Override
    public boolean isNavChild(boolean deep) {
        return true;
    }

    @Override
    public void add(List<PropertyNode> properties, boolean clone) {
        // NO-OP
    }

    @Override
    public boolean isExtensionPointTarget() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemas.node.interfaces.FacetInterface#add(org.opentravel.schemas.node.properties.PropertyNode)
     */
    @Override
    public void add(PropertyNode property) {
        // NO-OP
    }

    @Override
    public void add(PropertyNode pn, int index) {
        // NO-OP
    }

    @Override
    public boolean canOwn(PropertyNode pn) {
        return getContributor().canOwn( pn.getPropertyType() );
    }

    @Override
    public boolean canOwn(PropertyNodeType type) {
        return getContributor().canOwn( type );
    }

    public void clear() {
        if (getChildrenHandler() != null)
            getChildrenHandler().clear();
    }

    /**
     * Set contributor and tlContributor to null. Set deleted to true.
     */
    public void clearContributor() {
        contributor = null;
        tlContributor = null;
        deleted = true;
        setParent( null );
    }

    @Override
    public void copy(FacetInterface facet) {
        // NO-OP
    }

    @Override
    public PropertyNode createProperty(Node type) {
        // NO-OP
        return null;
    }

    @Override
    public void delete() {
        clear();
        if (getOwningComponent() != null && getOwningComponent().getChildrenHandler() != null)
            getOwningComponent().getChildrenHandler().clear();

        deleted = true;
        if (contributor != null && !contributor.isDeleted())
            getContributor().delete();
    }

    /**
     * Getter for the contributor
     */
    @Override
    public ContextualFacetNode get() {
        return contributor;
    }

    @Override
    public PropertyNode get(String name) {
        return getContributor().get( name );
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Node> getChildren() {
        // contributor can be null and children called during constructor super() call.
        return (List<Node>) (get() != null ? get().getChildren() : Collections.emptyList());
    }

    @Override
    public FacetProviderChildrenHandler getChildrenHandler() {
        return contributor != null ? contributor.getChildrenHandler() : null;
    }

    /**
     * The string to present in the navigator tree and other library trees. Is label unless overridden.
     * 
     * @return
     */
    @Override
    public String getNavigatorName() {
        return getContributor() != null ? getContributor().getNavigatorName() : "";
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
            setContributor( null ); // try to resolve using listeners
        return contributor;
    }

    @Override
    public String getDecoration() {
        String decoration = "   ";
        // decoration += " :CF " + nodeID;
        if (isInherited())
            decoration += "Inherited ";
        if (getContributor() != null) {
            decoration += " " + getContributor().getLabel();
            // contributedFacetNode overloads getLibrary
            if (getLibrary() != this.getParent().getLibrary())
                decoration += " contributed from " + getLibrary();
            else
                decoration += " contributed from this library";
        }
        return decoration;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get( Images.ContextualFacet );
        // return Images.getImageRegistry().get(Images.ContributedFacet);
    }

    @Override
    public String getLabel() {
        return getContributor().getLabel() + " (Contributed)";
    }

    /**
     * The library where the contributor facet is a library member. NOT the parent object.
     */
    @Override
    public LibraryNode getLibrary() {
        return getContributor() == null ? null : getContributor().getLibrary();
    }

    /**
     * @return local name from contributor or else the name of the TLContributor
     */
    public String getLocalName() {
        return getContributor() == null ? tlContributor.getName() : getContributor().getLocalName();
    }

    @Override
    public String getName() {
        return getContributor() == null ? "" : getContributor().getName();
    }

    /**
     * @return the owning entity as the object being contributed to
     */
    @Override
    public LibraryMemberInterface getOwningComponent() {
        // 9/18/2017 - for version 1.6, contributed facets are owned by object contributed to
        assert tlContributor != null;
        if (parent == null)
            parent = Node.GetNode( tlContributor.getOwningEntity() );
        return (LibraryMemberInterface) getParent();
    }

    @Override
    public List<PropertyNode> getProperties() {
        return getContributor().getProperties();
    }

    @Override
    public TLContextualFacet getTLModelObject() {
        return get() == null ? tlContributor : get().getTLModelObject();
    }

    /**
     * Contributed facets are never edited. Editing happens on the contextual facet only.
     */
    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isFacet(TLFacetType facetType) {
        return getContributor().isFacet( facetType );
    }

    @Override
    public void removeProperty(PropertyNode pn) {
        // NO-OP
    }

    /**
     * Set the contributing Contextual Facet. Use the listener on the TLContextualFacet to find the contributing
     * ContextualFacetNode. If missing, the tlContributor will be used when calls to getContributor() are made to find
     * the node.
     * 
     * @param tlCF the contributing facet. if NULL the existing TL Contributor is used.
     */
    public void setContributor(TLContextualFacet tlCF) {
        if (tlCF != null)
            tlContributor = tlCF; // override value set on construction
        Node n = GetNode( tlContributor );
        if (n instanceof ContextualFacetNode)
            contributor = (ContextualFacetNode) n;
        if (contributor != null)
            contributor.setWhereContributed( this );
        // Happens a lot when projects are loaded
        // else
        // LOGGER.warn("Could not set contributor because the listener is missing.");
    }

    @Override
    public void setDeleted(boolean value) {
        super.setDeleted( value );
        if (getContributor() != null && !getContributor().isDeleted())
            getContributor().setDeleted( value );
    }

}

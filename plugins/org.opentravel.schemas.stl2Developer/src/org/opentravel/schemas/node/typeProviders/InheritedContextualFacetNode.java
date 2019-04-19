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

package org.opentravel.schemas.node.typeProviders;

import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for inherited custom, choice, query and update facets.
 * <p>
 * Only used for version 1.6 and later where contextual facets are library members.
 * <p>
 * This node is in a library.
 * <p>
 * This node <i>has</i> a TL model object that is different than the inherited TL model object.
 * 
 * @author Dave Hollander
 * 
 */
public class InheritedContextualFacetNode extends ContextualFacetNode implements InheritedInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger( InheritedContextualFacetNode.class );

    private ContextualFacetNode inheritedFrom = null;

    /**
     * Create a facade for an inherited contextual facet.
     * 
     * @param tlObj the TL object unique to this inheritance
     * @param from the contextual facet inherited from
     * @param library the library of the owner
     */
    public InheritedContextualFacetNode(TLContextualFacet tlGhost, ContextualFacetNode from, Node parent) {
        super();
        tlObj = tlGhost;
        inheritedFrom = from;
        this.parent = parent;
        setLibrary( parent.getLibrary() );

        // Set two listeners - one on this and one on the base
        ListenerFactory.setIdentityListner( this );
        new InheritanceDependencyListener( this );

        assert tlGhost != from.getTLModelObject(); // must be a TL object unique to this parent object
    }

    @Override
    public ContextualFacetNode getInheritedFrom() {
        return inheritedFrom;
    }

    @Override
    public LibraryNode getLibrary() {
        return getInheritedFrom().getLibrary();
    }

    @Override
    public boolean canOwn(TLFacetType type) {
        return false;
    }

    /**
     * Don't show these in the nav tree.
     * 
     * @param deep
     * @return
     */
    @Override
    public boolean isNavChild(boolean deep) {
        return false;
    }

    @Override
    protected void removeFromTLParent() {}

    @Override
    public boolean canOwn(AbstractContextualFacet targetCF) {
        return false;
    }

    @Override
    protected void addToTLParent(ContextualFacetOwnerInterface owner) {}

    @Override
    public ContextualFacetNode copy(LibraryNode destLib) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void delete() {
        close();
    }

    @Override
    public String getDecoration() {
        return "  Inherited" + getInheritedFrom().getDecoration();
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isEnabled_AddProperties() {
        return false;
    }

    @Override
    public void print() {
        super.print();
        LOGGER.debug( "Inherited from: " + inheritedFrom );
    }

    /**
     * Set the name of this contextual (custom or query) facet. Name is simply the facet name and not its global type
     * name.
     */
    @Override
    public void setName(String n) {}

    /**
     * Add this contextual facet to the owner.
     * <p>
     * if it can be a library member (v1.6 and later) then create contributed facet. Removes existing contributed facet
     * if needed.
     * 
     * @param owner
     * @param newFacet
     */
    @Override
    public void add(ContextualFacetOwnerInterface owner) {}
}

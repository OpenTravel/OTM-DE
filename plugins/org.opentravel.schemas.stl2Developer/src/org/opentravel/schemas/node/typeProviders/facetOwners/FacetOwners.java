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

package org.opentravel.schemas.node.typeProviders.facetOwners;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.SharedFacetNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.TypeProviders;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for <b>all</b> type providers whose children include facets.
 * <p>
 * Role is to manage assignments including where used.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class FacetOwners extends TypeProviders implements FacetOwner {
    private static final Logger LOGGER = LoggerFactory.getLogger( FacetOwners.class );

    public FacetOwners() {
        whereAssignedHandler = new WhereAssignedHandler( this );
    }

    public FacetOwners(final TLModelElement obj) {
        super( obj );
        whereAssignedHandler = new WhereAssignedHandler( this );
        if (!isInherited())
            assert Node.GetNode( getTLModelObject() ) == this;
    }

    /**
     * Add a facet to this owner. Only contextual facets can be added and only in unmanaged or head versions.
     * 
     * @param name
     * @param type a contextual facet type that can be owned by this facet owner
     * @return the new contextual facet (not contributed)
     */
    // TODO - consider allowing them in minor and use createMinorVersionOfComponent()
    public AbstractContextualFacet addFacet(String name, TLFacetType type) {
        if (!isEditable_newToChain()) {
            isEditable_newToChain();
            throw new IllegalArgumentException( "Not editable - Can not add facet to " + this );
        }
        if (!type.isContextual() || !canOwn( type ))
            return null;
        if (!(this instanceof ContextualFacetOwnerInterface))
            return null;

        TLContextualFacet tlCf = ContextualFacetNode.createTL( name, type );
        AbstractContextualFacet cf = NodeFactory.createContextualFacet( tlCf );
        cf.add( (ContextualFacetOwnerInterface) this );

        assert cf.getParent() instanceof NavNode;
        assert getChildren().contains( ((ContextualFacetNode) cf).getWhereContributed() );
        assert getLibrary().contains( cf );
        return cf;
    }

    public abstract boolean canOwn(TLFacetType type);

    @Override
    public LibraryMemberInterface clone(LibraryNode targetLib, String nameSuffix) {
        if (getLibrary() == null || !getLibrary().isEditable()) {
            LOGGER.warn( "Could not clone node because library " + getLibrary() + " it is not editable." );
            return null;
        }

        LibraryMemberInterface clone = null;

        // Use the compiler to create a new TL src object.
        TLModelElement newLM = (TLModelElement) cloneTLObj();
        if (newLM != null) {
            clone = NodeFactory.newLibraryMember( (LibraryMember) newLM );
            assert clone != null;
            if (nameSuffix != null)
                clone.setName( clone.getName() + nameSuffix );
            for (AliasNode alias : clone.getAliases())
                alias.setName( alias.getName() + nameSuffix );
            if (targetLib != null)
                targetLib.addMember( clone );
        }
        return clone;
    }

    @Override
    public FacetInterface getFacet(final TLFacetType facetType) {
        for (final Node n : getChildren()) {
            if (n instanceof FacetInterface) {
                TLFacetType type = ((FacetInterface) n).getFacetType();
                if (type != null && type.equals( facetType ))
                    return (FacetInterface) n;
            }
        }
        return null;
    }

    @Override
    public AttributeFacetNode getFacet_Attributes() {
        return null;
    }

    @Override
    public abstract FacetInterface getFacet_Default();

    @Override
    public FacetProviderNode getFacet_Detail() {
        return (FacetProviderNode) getFacet( TLFacetType.DETAIL );
    }

    @Override
    public FacetProviderNode getFacet_ID() {
        return (FacetProviderNode) getFacet( TLFacetType.ID );
    }

    @Override
    public FacetInterface getFacet_Simple() {
        return getFacet( TLFacetType.SIMPLE );
    }

    @Override
    public FacetProviderNode getFacet_Summary() {
        return (FacetProviderNode) getFacet( TLFacetType.SUMMARY );
    }

    @Override
    public TLFacetType getFacetType() {
        return getTLModelObject() instanceof TLAbstractFacet ? ((TLAbstractFacet) getTLModelObject()).getFacetType()
            : null;
    }

    public SharedFacetNode getFacet_Shared() {
        return (SharedFacetNode) getFacet( TLFacetType.SHARED );
    }
}

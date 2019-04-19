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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.handlers.children.FacetProviderChildrenHandler;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Common base class for contextual facets - version 1.5 and version 1.6 and later .
 * 
 * @author Dave Hollander
 * 
 */
public abstract class AbstractContextualFacet extends FacetProviderNode implements ContextualFacetOwnerInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractContextualFacet.class );

    /**
     * Create a TLContextualFacet. Not added to object or library.
     * 
     * @param name
     * @param type
     * @return
     */
    public static TLContextualFacet createTL(String name, TLFacetType type) {
        TLContextualFacet newFacet = new TLContextualFacet();
        newFacet.setName( name ); // May be invalid!
        newFacet.setFacetType( type );
        return newFacet;
    }

    public AbstractContextualFacet() {}

    public AbstractContextualFacet(TLContextualFacet tlObj) {
        super( tlObj );

        if (!isInherited())
            assert Node.GetNode( getTLModelObject() ) == this;
    }

    /**
     * Add this contextual facet to the owner.
     * <p>
     * If it can be a library member (v1.6 and later) then create contributed facet. Removes existing contributed facet
     * if needed.
     * 
     * @param owner
     */
    public abstract void add(ContextualFacetOwnerInterface owner);

    /**
     * Add this contextual facet to owning entity. Overloaded by each contextual facet type.
     * 
     */
    protected abstract void addToTLParent(ContextualFacetOwnerInterface owner);

    /**
     * @return true if the contextual facet could be a library member
     */
    public boolean canBeLibraryMember() {
        return true;
    }

    @Override
    public abstract boolean canOwn(AbstractContextualFacet targetCF);

    /**
     * Test the TL object to assure aliases are unique
     * 
     * @return
     */
    public boolean checkAliasesAreUnique(boolean fix) {
        TLContextualFacet tl = getTLModelObject();
        ArrayList<String> aliasNames = new ArrayList<>();
        for (TLAlias tla : tl.getAliases()) {
            if (aliasNames.contains( tla.getName() )) {
                if (fix) {
                    tl.removeAlias( tla );
                    getChildrenHandler().clear();
                } else
                    assert (false);
            } else
                aliasNames.add( tla.getName() );
        }
        return true;
    }

    @Override
    public Node clone(Node parent, String nameSuffix) {
        return super.clone( parent, nameSuffix );
    }

    @Override
    public abstract LibraryMember cloneTL() throws IllegalArgumentException;

    /**
     * Close contextual facet. * Close removes from GUI model without changing the underlying OTM model.
     * 
     * <p>
     * Removes this facet from the TL Parent. The TL contextual facet owner <b>does</b> change because the facet will
     * not longer be in scope but there will be no changes to the saved .otm file.
     */
    @Override
    public void close() {
        removeFromTLParent();
    }

    @Override
    public void delete() {
        // Normal delete then the contributed facet
        super.delete();
        removeFromTLParent();
    }

    /**
     * Find a contextual facet in the baseOwner with same local name and facet type
     * 
     * @param baseOwner
     * @return matching facet or null if not found
     */
    public AbstractContextualFacet findMatchingFacet(ContextualFacetOwnerInterface baseOwner) {
        if (baseOwner != null) {
            for (AbstractContextualFacet cf : baseOwner.getContextualFacets( false ))
                if (cf.getLocalName().equals( this.getLocalName() ))
                    if (cf.getFacetType() == getFacetType())
                        return cf;
        }
        return null;
    }

    @Override
    public FacetProviderChildrenHandler getChildrenHandler() {
        return (FacetProviderChildrenHandler) childrenHandler;
    }

    @Override
    public ContributedFacetNode getContributedFacet(TLContextualFacet tlCf) {
        return null;
    }

    @Override
    public String getDecoration() {
        String decoration = "";
        decoration += "   : " + getLabel();
        decoration += "  (Version: " + getTlVersion();
        if (!isInHead2() || this instanceof InheritedInterface)
            decoration += " Not Editable";
        decoration += ")";
        return decoration.isEmpty() ? " " : decoration;
    }

    /**
     * Use the owning component of the contributor to find the extension base. Then find the child with the same name.
     */
    @Override
    public Node getExtendsType() {
        LibraryMemberInterface owner = getOwningComponent();
        if (getWhereContributed() != null)
            owner = getWhereContributed().getOwningComponent();
        if (!(owner instanceof ExtensionOwner))
            return null;
        Node baseOwner = ((Node) owner).getExtendsType();
        Node baseFacet = null;
        if (baseOwner instanceof ContextualFacetOwnerInterface)
            baseFacet = findMatchingFacet( (ContextualFacetOwnerInterface) baseOwner );

        assert baseFacet != null ? getFacetType() == ((AbstractContextualFacet) baseFacet).getFacetType() : true;
        assert !(baseFacet instanceof ContributedFacetNode);

        return baseFacet;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get( Images.Facet );
    }

    @Override
    public Node getInheritedFrom() {
        return null;
    }

    @Override
    public String getLabel() {
        return getTLModelObject() != null ? getFacetType().toString() : "";
    }

    @Override
    public abstract LibraryNode getLibrary();

    /**
     * Get the local name. The local name is just the portion unique to this facet without parent or base contribution.
     */
    public String getLocalName() {
        if (getTLModelObject() == null)
            return "";
        return getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
    }

    /**
     * Get the the full name complete with parent contribution of this contextual facet.
     * <p>
     * <b>Note:</b> this the local name from the TL model object.
     * 
     * @see getLocalName()
     */
    @Override
    public String getName() {
        return getTLModelObject() == null ? "" : getTLModelObject().getLocalName();
    }

    @Override
    public String getNavigatorName() {
        return getLocalName();
    }

    /**
     * @return the owning entity reported by the TL Model Object. If no owning entity then this is returned.
     */
    @Override
    public abstract LibraryMemberInterface getOwningComponent();

    @Override
    public TLContextualFacet getTLModelObject() {
        return (TLContextualFacet) tlObj;
    }

    public abstract ContributedFacetNode getWhereContributed();

    @Override
    public boolean isDeleteable() {
        if (isInherited())
            return false;
        return getOwningComponent().isEditable();
    }

    @Override
    public boolean isEditable() {
        if (getLibrary() == null)
            return false;
        if (getChain() == null)
            return getLibrary().isEditable();
        if (isInHead())
            return getLibrary().isEditable();
        return false;
        // return isInHead() || getChain() == null ;
    }

    @Override
    public boolean isEnabled_AddProperties() {
        if (isDeleted() || !isEditable())
            return false; // not editable
        if (getChain() == null)
            return true; // editable and not in a chain
        // If in a chain, it must be the head library
        return isInHead();
    }

    @Override
    public boolean isExtensionPointTarget() {
        return false;
    }

    @Override
    public boolean isInHead() {
        // Owning component used in isInHead() does not control editing of contextual facets because they may be in
        // different libraries.
        if (getChain() == null || getChain().getHead() == null)
            return false;
        return getChain().getHead() == getLibrary();
    }

    /**
     * Contextual facets are only named entities if their parent is a NavNode not an object.
     */
    @Override
    @Deprecated
    public boolean isNamedEntity() {
        if (getParent() instanceof VersionNode)
            return getParent().getParent() instanceof NavNode;
        return getParent() instanceof NavNode;
    }

    @Override
    public boolean isRenameable() {
        return isEditable() && !isInherited();
    }

    /**
     * @return true if name and namespace are equal to other node
     */
    @Override
    protected boolean nameEquals(final INode other) {
        if (this == other)
            return true;
        if (other == null)
            return false;

        String thisName = getLocalName();
        String otherName = other.getName();
        if (other instanceof AbstractContextualFacet)
            otherName = getLocalName();

        if (thisName == null) {
            if (otherName != null) {
                return false;
            }
        } else if (!thisName.equals( otherName )) {
            return false;
        }
        if (getNamespace() == null) {
            if (other.getNamespace() != null) {
                return false;
            }
        } else if (!getNamespace().equals( other.getNamespace() )) {
            return false;
        }
        return true;
    }

    public abstract void print();

    /**
     * Remove this contextual facet from owning entity. Overloaded by each contextual facet type.
     * 
     */
    protected abstract void removeFromTLParent();

    /**
     * Set context to library's default context ID.
     * 
     * @param context is IGNORED. Context is not used on facets in version 1.6 and later.
     */
    // @Override
    @Deprecated
    public void setContext(String context) {
        // setContext();
    }

    /**
     * Simple Setter
     */
    public void setInheritedFrom(Node owner) {
        // NO-OP - inheritedFrom = owner;
    }

    /**
     * Set the name of this contextual (custom or query) facet. Name is simply the facet name and not its global type
     * name.
     */
    @Override
    public abstract void setName(String n);

    /**
     * Add this facet to the owner's TL facet owner. Then add to library of owner if the library is not set. Create
     * contributed facet if canBeLibrarymember (v1.6)
     * 
     * @param owner
     */
    public void setOwner(ContextualFacetOwnerInterface owner) {
        add( owner ); // Add to owning object and it's library if needed
    }

    // Drives sort order in navigation menu
    @Override
    public String toString() {
        return getLocalName();
    }

}

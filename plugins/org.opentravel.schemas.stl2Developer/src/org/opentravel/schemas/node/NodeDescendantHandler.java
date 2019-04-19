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

package org.opentravel.schemas.node;

import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleMemberInterface;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Handler for getDescendants methods.
 *
 * @see GetDescendents_Tests
 * 
 * @author Dave Hollander
 * 
 */
public class NodeDescendantHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger( NodeDescendantHandler.class );

    /**
     * @return a new list containing all unique children nodes and their descendants. No filtering; includes aggregate,
     *         version and navNodes.
     */
    public List<Node> getDescendants(Node root) {
        HashSet<Node> ret = new HashSet<>();
        for (final Node n : root.getChildren())
            if (!n.isDeleted()) {
                ret.add( n );
                ret.addAll( n.getDescendants() );
            }
        return new ArrayList<>( ret );
    }

    /**
     * Gets all the types assigned to this type and all the types assigned to those types, etc. Sends back a list of
     * unique types. Types used recursively are only added to the list once.
     * 
     * @param currentLibraryOnly - only list types in this library.
     * @return new list of assigned types or empty list.
     */
    public List<Node> getDescendants_AssignedTypes(Node root, boolean currentLibraryOnly) {
        HashSet<Node> foundTypes = new HashSet<>();
        foundTypes = getDescendants_AssignedTypes( root, currentLibraryOnly, foundTypes );
        foundTypes.remove( root ); // may have been found in an addAll iteration
        return new ArrayList<>( foundTypes );
    }

    // the public method uses this then removes the original object from the list.
    private HashSet<Node> getDescendants_AssignedTypes(Node root, boolean currentLibraryOnly,
        HashSet<Node> foundTypes) {
        Node assignedType = null;
        for (TypeUser n : root.getDescendants_TypeUsers()) {
            if (n.getAssignedType() != null) {
                assignedType = (Node) ((Node) n.getAssignedType()).getOwningComponent();
                if (!currentLibraryOnly || (assignedType.getLibrary() == root.getLibrary()))
                    if (foundTypes.add( assignedType )) {
                        foundTypes
                            .addAll( getDescendants_AssignedTypes( assignedType, currentLibraryOnly, foundTypes ) );
                    }
            }
        }
        return foundTypes;
    }

    /**
     * Gets the descendants that are extension owners.
     * 
     * @return new list of all descendants that are extension owners.
     */
    public List<ExtensionOwner> getDescendants_ExtensionOwners(Node root) {
        final ArrayList<ExtensionOwner> ret = new ArrayList<>();
        for (final Node n : root.getChildren()) {
            if (n instanceof ExtensionOwner)
                ret.add( (ExtensionOwner) n );

            if (hasDescendents( n ))
                ret.addAll( getDescendants_ExtensionOwners( n ) );
        }
        return ret;
    }

    public List<ContextualFacetOwnerInterface> getDescendants_ContextualFacetOwners(Node root) {
        final ArrayList<ContextualFacetOwnerInterface> ret = new ArrayList<>();
        for (final Node n : root.getDescendants())
            if (n instanceof ContextualFacetOwnerInterface)
                ret.add( (ContextualFacetOwnerInterface) n );
        return ret;
    }

    public List<ContributedFacetNode> getDescendants_ContributedFacets(Node root) {
        final ArrayList<ContributedFacetNode> ret = new ArrayList<>();
        for (final Node n : root.getChildren()) {
            if (n instanceof ContributedFacetNode)
                ret.add( (ContributedFacetNode) n );

            if (hasDescendents( n ))
                ret.addAll( getDescendants_ContributedFacets( n ) );
        }
        return ret;
    }

    /**
     * 
     * @return new list of all contextual facets including contributed facets
     */
    public List<ContextualFacetNode> getDescendants_ContextualFacets(Node root) {
        final ArrayList<ContextualFacetNode> ret = new ArrayList<>();
        for (final Node n : root.getChildren()) {
            if (n instanceof ContextualFacetNode)
                ret.add( (ContextualFacetNode) n );

            if (hasDescendents( n ))
                ret.addAll( getDescendants_ContextualFacets( n ) );
        }
        return ret;

    }

    /**
     * return new list of NamedEntities. Traverse via hasChildren. For version chains, it returns the newest version
     * using the version node and does not touch aggregates.
     */
    // @Override
    public List<LibraryMemberInterface> getDescendants_LibraryMembers(Node root) {
        final ArrayList<LibraryMemberInterface> ret = new ArrayList<>();
        for (final Node n : root.getChildren()) {
            if (n instanceof LibraryMemberInterface)
                ret.add( (LibraryMemberInterface) n );

            if (hasDescendents( n ))
                ret.addAll( getDescendants_LibraryMembers( n ) );
        }
        return ret;
    }

    /**
     * Gets the descendants that are type SimpleComponentNode. Does not return navigation nodes.
     * 
     * @return new list of all descendants that simple components.
     */
    public ArrayList<SimpleMemberInterface> getDescendants_SimpleMembers(Node root) {
        final ArrayList<SimpleMemberInterface> ret = new ArrayList<>();
        for (final Node n : root.getChildren()) {
            if (n instanceof SimpleMemberInterface)
                ret.add( (SimpleMemberInterface) n );

            if (hasDescendents( n ))
                ret.addAll( getDescendants_SimpleMembers( n ) );
        }
        return ret;
    }

    /**
     * Gets the descendants that are type providers (can be assigned as a type). Does not return navigation nodes.
     * 
     * @return new list of all descendants that can be assigned as a type.
     */
    public List<TypeProvider> getDescendants_TypeProviders(Node root) {
        final ArrayList<TypeProvider> ret = new ArrayList<>();
        for (final Node n : root.getChildren()) {
            if (n instanceof TypeProvider)
                ret.add( (TypeProvider) n );

            if (hasDescendents( n ))
                ret.addAll( n.getDescendants_TypeProviders() );
        }
        return ret;
    }

    /**
     * Gets the descendants that are type users (can be assigned a type). Does not return navigation nodes.
     * {@link #getChildren_TypeUsers() Use getChildren_TypeUsers() for only immediate children.}
     * 
     * @return new list of all descendants that can be assigned a type.
     */
    public List<TypeUser> getDescendants_TypeUsers(Node root) {
        final ArrayList<TypeUser> ret = new ArrayList<>();
        for (final Node n : root.getChildren()) {
            if (n instanceof TypeUser)
                ret.add( (TypeUser) n );

            if (hasDescendents( n ))
                ret.addAll( n.getDescendants_TypeUsers() );
        }
        return ret;
    }

    /**
     * Return false for structures that duplicate objects in the model, including contributed facets, non-version
     * aggregates and where used nodes.
     * 
     * @param n
     * @return
     */
    private boolean hasDescendents(Node n) {
        if (n.isDeleted())
            return false;
        // Do not traverse contributed facets, the actual facet will be used
        if (n instanceof ContributedFacetNode)
            return false;
        // Only search the libraries under the version aggregate
        if (n instanceof AggregateNode && !(n instanceof VersionAggregateNode))
            return false;
        // Skip where used nodes
        if (n instanceof WhereUsedNodeInterface)
            return false;
        return n.hasChildren();
    }

}

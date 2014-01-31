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
package org.opentravel.schemas.types;

import java.util.List;

import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;

/**
 * Implementations of this interface are type definitions or other assignable nodes that represent
 * type definitions such as Aliases.
 * 
 * Note - the only way to add a user is via the TypeUser interface. (user.setAssignedType())
 * 
 * @author Dave Hollander
 * 
 */
public interface TypeProvider {

    /**
     * @return a list of nodes that use this as a type definition or base type
     */
    public List<Node> getWhereUsed();

    /**
     * @return the component node used to represent users of this type.
     */
    public INode getTypeNode();

    /**
     * @return a list of nodes that use this as a type definition or base type
     */
    public List<Node> getTypeUsers();

    /**
     * @return (where used count) the number of type users which are nodes that use this as a type
     *         definition or base type
     */
    public int getTypeUsersCount();

    /**
     * @return true if this object can be used as an assigned type or base type
     */
    public boolean isTypeProvider();

    /**
     * @return true if this node can be assigned to an attribute or simple property
     */
    public boolean isAssignableToSimple();

    /**
     * @return true if this node can be assigned to an attribute, simple property or VWA attribute
     */
    public boolean isAssignableToVWA();

    /**
     * @return true if this node can be assigned to an element reference
     */
    public boolean isAssignableToElementRef();

    /**
     * @return the node which owns this type class
     */
    public INode getTypeOwner();

}

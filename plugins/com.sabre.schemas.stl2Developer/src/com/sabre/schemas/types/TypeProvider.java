package com.sabre.schemas.types;

import java.util.List;

import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.Node;

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

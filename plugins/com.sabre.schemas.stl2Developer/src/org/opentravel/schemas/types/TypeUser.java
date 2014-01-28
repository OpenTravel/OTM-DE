package org.opentravel.schemas.types;

import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;

import com.sabre.schemacompiler.model.NamedEntity;

public interface TypeUser {

    /**
     * @return true if the passed node can be assigned to as the type.
     */
    public boolean canAssign(Node type);

    /**
     * @return the node of the assigned type
     */
    public INode getAssignedType();

    /**
     * @return the node of the assigned type
     */
    public ModelObject<?> getAssignedModelObject();

    /**
     * @return the node of the assigned type
     */
    public NamedEntity getAssignedTLObject();

    /**
     * @return true if a type can be assigned to this node.
     */
    public boolean isTypeUser();

    /**
     * Remove the assigned type. Removes this node from typeNode's list of typeUsers. Sets typeNode
     * = null.
     */
    public void removeAssignedType();

    /**
     * Set Assigned Type. Sets the Assigned type node and add this owner to that user list. This
     * method assures their is a target and that the owner is editable. Sets the type class
     * properties as well as the TLModel type
     * 
     * @return true if assignment could be made, false otherwise
     */
    public boolean setAssignedType(Node n);
}

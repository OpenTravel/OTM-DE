/**
 * 
 */
package com.sabre.schemas.node;

import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemas.modelObject.ModelObject;

/**
 * @author Dave Hollander
 * 
 */
public interface SimpleComponentInterface {

    public boolean isSimpleTypeProvider();

    /**
     * @return the node representing the base type.
     */
    public INode getBaseType();

    /**
     * @return the model object underlying this node.
     */
    public ModelObject<?> getModelObject(); // The Model Object

    /**
     * @return the TL Object underlying this node's model object
     */
    public NamedEntity getTLOjbect();
}

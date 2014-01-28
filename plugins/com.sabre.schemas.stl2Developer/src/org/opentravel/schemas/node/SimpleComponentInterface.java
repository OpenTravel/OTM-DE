/**
 * 
 */
package org.opentravel.schemas.node;

import org.opentravel.schemas.modelObject.ModelObject;

import com.sabre.schemacompiler.model.NamedEntity;

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

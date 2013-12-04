/**
 * 
 */
package com.sabre.schemas.node;

/**
 * Nodes implementing this interface represent named types that have structure that contains other
 * nodes. In the GUI, they are presented in Complex Type folders. When compiled, they will create
 * complex types.
 * 
 * NOTE: The service components (service, operation and messages) do <b>not</b> implement this
 * interface.
 * 
 * NOTE: To determine how properties will be assigned types use
 * {@link INode#isAssignedByReference()}, <b>not</b> this interface.
 * 
 * @author Dave Hollander
 * 
 */
public interface ComplexComponentInterface {

    /**
     * @return the type assigned to the simple facet or null if none.
     */
    public ComponentNode getSimpleType();

    /**
     * @return false if simple type could not be set.
     */
    public boolean setSimpleType(Node type);

    /**
     * @return the simple facet or null if none.
     */
    public SimpleFacetNode getSimpleFacet();

    public ComponentNode getAttributeFacet(); // VWA only

    public ComponentNode getSummaryFacet();

    public ComponentNode getDetailFacet();

    /**
     * Create aliases for complex types used by two or more properties.
     * 
     * @return
     */
    public void createAliasesForProperties();

    public boolean isNamedType();
}

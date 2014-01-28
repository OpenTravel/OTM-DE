/**
 * 
 */
package org.opentravel.schemas.node;

import org.opentravel.schemas.node.properties.RoleNode;

import com.sabre.schemacompiler.model.TLRoleEnumeration;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class RoleFacetNode extends FacetNode {

    public RoleFacetNode(TLRoleEnumeration tlObj) {
        super(tlObj);
    }

    /**
     * Add a role property for the name. This can be any member of a core object.
     * 
     * @param name
     *            - role names
     */
    public void addRole(String name) {
        new RoleNode(this, name);
    }

    /**
     * Add a role property for each of the names in the array. This can be any member of a core
     * object.
     * 
     * @param names
     *            - array of role names
     */
    public void addRoles(String[] names) {
        for (String name : names)
            addRole(name);
    }

    @Override
    public INode createProperty(final Node type) {
        return new RoleNode(this, type.getName());
    }

    @Override
    public RoleFacetNode getRoleFacet() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.FacetNode#isCustomFacet()
     */
    @Override
    public boolean isCustomFacet() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.FacetNode#isDefaultFacet()
     */
    @Override
    public boolean isDefaultFacet() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.FacetNode#isDetailListFacet()
     */
    @Override
    public boolean isDetailListFacet() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.FacetNode#isListFacet()
     */
    @Override
    public boolean isListFacet() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.FacetNode#isQueryFacet()
     */
    @Override
    public boolean isQueryFacet() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.FacetNode#isRoleFacet()
     */
    @Override
    public boolean isRoleFacet() {
        return true;
    }

}

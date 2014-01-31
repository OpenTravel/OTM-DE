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
/**
 * 
 */
package org.opentravel.schemas.node;

import org.opentravel.schemas.node.properties.RoleNode;

import org.opentravel.schemacompiler.model.TLRoleEnumeration;

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

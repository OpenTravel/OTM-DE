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
package org.opentravel.schemas.bugs;

import org.junit.Test;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.objectMembers.OperationNode.ServiceOperationTypes;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.opentravel.schemacompiler.saver.LibrarySaveException;

/**
 * @author Pawel Jedruch
 * 
 */
public class RemoveBOAliasWithService extends BaseProjectTest {

    @Test
    public void npeOneRemoveBOWithAliasUsedInQueryOperation() throws LibrarySaveException {
        LibraryNode local1 = LibraryNodeBuilder.create("LocalOne",
                testProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                testProject, pc);

        BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject("BO").addAlias("BOAlias")
                .get();
        local1.addMember(bo);
        ServiceNode service = ComponentNodeBuilder.createService("Service", local1)
                .createCRUDQOperations(bo).get();
        OperationNode newOperation = new OperationNode(service, "Query",
                ServiceOperationTypes.QUERY, bo);

        newOperation.getDescendants_TypeUsers().get(0).setAssignedType(getAliasNode(bo));
        bo.delete();

    }

    private AliasNode getAliasNode(BusinessObjectNode bo) {
        for (Node child : bo.getChildren()) {
            if (child instanceof AliasNode) {
                return (AliasNode) child;
            }
        }
        return null;
    }
}

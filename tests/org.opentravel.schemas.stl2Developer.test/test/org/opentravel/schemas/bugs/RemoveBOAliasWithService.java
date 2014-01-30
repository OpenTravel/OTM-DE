/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.bugs;

import org.junit.Test;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.OperationNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.OperationNode.ResourceOperationTypes;
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
                defaultProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);

        BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject("BO").addAlias("BOAlias")
                .get();
        local1.addMember(bo);
        ServiceNode service = ComponentNodeBuilder.createService("Service", local1)
                .createCRUDQOperations(bo).get();
        OperationNode newOperation = new OperationNode(service, "Query",
                ResourceOperationTypes.QUERY, bo);

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

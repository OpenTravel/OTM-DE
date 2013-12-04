/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.bugs;

import org.junit.Test;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemas.node.AliasNode;
import com.sabre.schemas.node.BusinessObjectNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.OperationNode;
import com.sabre.schemas.node.OperationNode.ResourceOperationTypes;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.utils.BaseProjectTest;
import com.sabre.schemas.utils.ComponentNodeBuilder;
import com.sabre.schemas.utils.LibraryNodeBuilder;

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

/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.node;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.sabre.schemacompiler.repository.ProjectItem;
import com.sabre.schemas.types.TypeResolver;
import com.sabre.schemas.utils.BaseProjectTest;

/**
 * @author Pawel Jedruch
 * 
 */
public class XsdNodeTest extends BaseProjectTest {

    @Test
    public void shouldCreateCorrectVWA() {
        List<ProjectItem> items = pc.addLibrariesToTLProject(defaultProject.getProject(),
                Collections.singletonList(new File("Resources/CreateVWAFromExtened.xsd")));
        ProjectItem pi = items.get(0);
        LibraryNode libNode = new LibraryNode(pi, defaultProject);
        TypeResolver tr = new TypeResolver();
        tr.resolveTypes(libNode);
        for (Node n : libNode.getDescendentsNamedTypes()) {
            if (n instanceof VWA_Node) {
                VWA_Node vwa = (VWA_Node) n;
                Assert.assertTrue(vwa.getSimpleFacet().getSimpleAttribute().getTypeClass()
                        .verifyAssignment());
            }
        }
    }
}

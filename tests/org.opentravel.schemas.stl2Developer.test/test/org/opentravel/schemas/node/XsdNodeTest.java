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
package org.opentravel.schemas.node;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.utils.BaseProjectTest;

import org.opentravel.schemacompiler.repository.ProjectItem;

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

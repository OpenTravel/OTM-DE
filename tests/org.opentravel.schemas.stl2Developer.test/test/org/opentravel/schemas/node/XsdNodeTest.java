
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


package org.opentravel.schemas.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.opentravel.schemas.utils.PropertyNodeBuilder;
import org.osgi.framework.Version;

import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.saver.LibrarySaveException;

/**
 * @author Pawel Jedruch
 * 
 */
public class LibraryNodeTest extends BaseProjectTest {

    @Test
    public void shouldNotDuplicatedContextOnImport() throws LibrarySaveException {
        LibraryNode importFrom = LibraryNodeBuilder.create("ImportFrom",
                defaultProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);

        TLContext c = new TLContext();
        c.setContextId("ContextID");
        c.setApplicationContext("newContext");
        importFrom.getTLLibrary().addContext(c);
        importFrom.addContexts();
        BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject("BO")
                .addCustomFacet("name", c.getContextId()).addCustomFacet("name2", c.getContextId())
                .get();
        importFrom.addMember(bo);

        LibraryNode importTo = LibraryNodeBuilder.create("ImportTo",
                defaultProject.getNamespace() + "/Test/TO", "to", new Version(1, 0, 0)).build(
                defaultProject, pc);

        importTo.importNode(bo);

        Assert.assertEquals(2, importTo.getContextIds().size());
        Assert.assertNotNull(importTo.getTLLibrary().getContext(c.getContextId()));
    }

    @Test
    public void moveNodeFromOneToOther() throws LibrarySaveException {
        LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom",
                defaultProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);

        SimpleTypeNode moved = ComponentNodeBuilder.createSimpleObject("MyString")
                .assignType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE)).get();
        moveFrom.addMember(moved);

        PropertyNode withAssignedType = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT)
                .assign(moved).build();
        BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject("BO").get();
        bo.getSummaryFacet().addProperty(withAssignedType);
        moveFrom.addMember(bo);

        LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo",
                defaultProject.getNamespace() + "/Test/TO", "to", new Version(1, 0, 0)).build(
                defaultProject, pc);

        moveFrom.moveMember(moved, moveTo);

        Assert.assertSame(moveTo, moved.getLibrary());
        assertTypeAssigments(moved, withAssignedType);
    }

    private void assertTypeAssigments(Node moved, Node withAssignedType) {
        // make sure that after move assigned pointing to the same node
        Assert.assertSame(moved, withAssignedType.getTypeNode());
        // make sure that after move TLObjects are pointing to the same TLType
        Assert.assertSame(moved.getModelObject().getTLModelObj(),
                withAssignedType.getTLTypeObject());
    }

    @Test
    public void moveBOToOther() throws LibrarySaveException {
        LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom",
                defaultProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        // Create CO
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("CO").get(moveFrom);
        // Create attribute assigned to CO detail
        PropertyNode withAssignedType = PropertyNodeBuilder.create(PropertyNodeType.ATTRIBUTE)
                .build();
        // CreateVWA
        VWA_Node vwa = ComponentNodeBuilder.createVWA("VWA").addAttribute(withAssignedType).get();
        moveFrom.addMember(vwa);
        withAssignedType.getTypeClass().setAssignedType(co);

        LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo",
                defaultProject.getNamespace() + "/Test/TO", "to", new Version(1, 0, 0)).build(
                defaultProject, pc);

        moveFrom.moveMember(co, moveTo);

        assertTypeAssigments(co, withAssignedType);
    }

    /**
     * @throws LibrarySaveException
     */
    @Test
    public void importNodesLocallyShouldReplaceTypesInDestination() throws LibrarySaveException {
        // given
        LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom",
                defaultProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("COBase").get(moveFrom);
        PropertyNode element = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coBase)
                .setName(coBase.getName()).build();
        CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject("COExt")
                .addToSummaryFacet(element).get(moveFrom);

        LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo",
                defaultProject.getNamespace() + "/Test/TO", "to", new Version(1, 0, 0)).build(
                defaultProject, pc);

        // when
        moveTo.importNodes(moveFrom.getDescendentsNamedTypes(), false);

        // then
        assertSame(coBase, element.getAssignedType());
        Node movedBase = moveTo.findNodeByName("COBase");
        Node movedExt = moveTo.findNodeByName("COExt");
        assertSame(movedBase, movedExt.findNode(movedBase.getName(), movedExt.getNamespace())
                .getAssignedType());
    }

    @Test
    public void importNodesGloballyShouldReplaceBaseTypes() throws LibrarySaveException {
        // given
        LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom",
                defaultProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("COBase").get(moveFrom);
        CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject("COExt").extend(coBase)
                .get(moveFrom);

        LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo",
                defaultProject.getNamespace() + "/Test/TO", "to", new Version(1, 0, 0)).build(
                defaultProject, pc);

        // when
        moveTo.importNodes(moveFrom.getDescendentsNamedTypes(), true);

        // then
        assertEquals(2, moveTo.getDescendentsNamedTypes().size());
        Node movedBase = moveTo.findNodeByName("COBase");
        Node movedExt = moveTo.findNodeByName("COExt");
        assertFalse(movedExt.isInstanceOf(coBase));
        assertTrue(movedExt.isInstanceOf(movedBase));
    }

    @Test
    public void importNodesLocallyShouldReplaceBaseTypes() throws LibrarySaveException {
        // given
        LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom",
                defaultProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("COBase").get(moveFrom);
        CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject("COExt").extend(coBase)
                .get(moveFrom);

        LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo",
                defaultProject.getNamespace() + "/Test/TO", "to", new Version(1, 0, 0)).build(
                defaultProject, pc);

        // when
        moveTo.importNodes(moveFrom.getDescendentsNamedTypes(), false);

        // then
        assertEquals(2, moveTo.getDescendentsNamedTypes().size());
        Node movedBase = moveTo.findNodeByName("COBase");
        Node movedExt = moveTo.findNodeByName("COExt");
        // coBase.getTypeClass().getBaseUsers();
        // movedBase.getTypeClass().getBaseUsers();
        // movedExt.getExtendsType()
        assertTrue(coExt.isInstanceOf(coBase));
        assertTrue(movedExt.isInstanceOf(movedBase));
    }
}

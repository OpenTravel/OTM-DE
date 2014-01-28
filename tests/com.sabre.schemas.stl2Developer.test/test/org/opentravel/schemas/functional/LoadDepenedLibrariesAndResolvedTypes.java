/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.functional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.repository.RepositoryIntegrationTestBase;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.opentravel.schemas.utils.PropertyNodeBuilder;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.saver.LibrarySaveException;

/**
 * @author Pawel Jedruch
 * 
 */
public class LoadDepenedLibrariesAndResolvedTypes extends RepositoryIntegrationTestBase {

    private ProjectNode uploadProject;
    private LibraryNode baseLib;
    private LibraryNode extLib;

    @Override
    public RepositoryNode getRepositoryForTest() {
        for (RepositoryNode rn : rc.getAll()) {
            if (rn.isRemote()) {
                return rn;
            }
        }
        throw new IllegalStateException("Missing remote repository. Check your configuration.");
    }

    @Before
    public void beforeEachTest2() throws RepositoryException, LibrarySaveException {
        uploadProject = createProject("RepositoryProject", getRepositoryForTest(), "dependencies");
        baseLib = LibraryNodeBuilder.create("Base", uploadProject.getNamespace(), "o1",
                new Version(1, 0, 0)).build(uploadProject, pc);
        SimpleTypeNode simpleInBase = ComponentNodeBuilder.createSimpleObject("MyString")
                .assignType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE)).get();
        baseLib.addMember(simpleInBase);

        extLib = LibraryNodeBuilder.create("Ext", uploadProject.getNamespace(), "o1",
                new Version(1, 0, 0)).build(uploadProject, pc);
        PropertyNode withAssignedType = PropertyNodeBuilder.create(PropertyNodeType.ATTRIBUTE)
                .setName("Attribute").build();
        VWA_Node vwa = ComponentNodeBuilder.createVWA("VWA").addAttribute(withAssignedType).get();
        extLib.addMember(vwa);
        withAssignedType.getTypeClass().setAssignedType(simpleInBase);
    }

    @Test
    public void manageOneByOneStartingFromBaseLibrary() throws RepositoryException {
        LibraryChainNode baseChain = rc.manage(getRepositoryForTest(),
                Collections.singletonList(baseLib)).get(0);
        LibraryChainNode extChain = rc.manage(getRepositoryForTest(),
                Collections.singletonList(extLib)).get(0);

        assertAllLibrariesLoadedCorrectly(baseChain, extChain);
    }

    @Test
    public void manageBothLibraries() throws RepositoryException, LibrarySaveException {
        List<LibraryChainNode> chains = rc.manage(getRepositoryForTest(),
                Arrays.asList(extLib, baseLib));
        LibraryChainNode extChain = findLibrary(extLib.getName(), chains);
        LibraryChainNode baseChain = findLibrary(baseLib.getName(), chains);

        assertAllLibrariesLoadedCorrectly(baseChain, extChain);
    }

    @Test
    public void manageOnlyLibWithIncludes() throws RepositoryException, LibrarySaveException {
        List<LibraryChainNode> chains = rc.manage(getRepositoryForTest(),
                Collections.singletonList(extLib));
        LibraryChainNode extChain = findLibrary(extLib.getName(), chains);
        LibraryChainNode baseChain = findLibrary(baseLib.getName(), chains);

        assertAllLibrariesLoadedCorrectly(baseChain, extChain);

    }

    private void assertAllLibrariesLoadedCorrectly(LibraryChainNode baseChain,
            LibraryChainNode extChain) {
        // clean up project
        mc.getLibraryController().remove(Collections.singletonList(baseChain));
        
        //find repository item before delete.
        RepositoryItemNode nodeToRetrive = findRepositoryItem(extChain, getRepositoryForTest());
        mc.getLibraryController().remove(Collections.singletonList(extChain));
        Assert.assertEquals(0, uploadProject.getChildren().size());

        // load only library with dependencies
        pc.add(uploadProject, nodeToRetrive.getItem());
        Assert.assertEquals(2, uploadProject.getChildren().size());

        // make sure that base library is loaded and type are resolved
        LibraryChainNode lib = findLibrary(extLib.getName(), uploadProject.getChildren());
        VWA_Node vwaNode = (VWA_Node) lib.getDescendants_NamedTypes().get(0);
        AttributeNode attr = (AttributeNode) vwaNode.getAttributeFacet().getChildren().get(0);
        Assert.assertNotSame(ModelNode.getUnassignedNode(), attr.getTypeNode());
        Assert.assertSame(attr.getTypeNode().getModelObject().getTLModelObj(),
                attr.getTLTypeObject());
    }

    private LibraryChainNode findLibrary(String name, Collection<? extends Node> libs) {
        for (Node n : libs) {
            if (n instanceof LibraryChainNode) {
                LibraryChainNode lch = (LibraryChainNode) n;
                if (name.equals(lch.getHead().getName()))
                    return lch;
            }
        }
        return null;
    }
}

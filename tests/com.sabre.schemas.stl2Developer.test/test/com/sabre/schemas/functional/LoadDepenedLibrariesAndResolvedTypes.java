/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.functional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemas.controllers.repository.RepositoryIntegrationTestBase;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFinders;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.node.SimpleTypeNode;
import com.sabre.schemas.node.VWA_Node;
import com.sabre.schemas.node.properties.AttributeNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.trees.repository.RepositoryNode;
import com.sabre.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import com.sabre.schemas.utils.ComponentNodeBuilder;
import com.sabre.schemas.utils.LibraryNodeBuilder;
import com.sabre.schemas.utils.PropertyNodeBuilder;

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
        mc.getLibraryController().remove(Collections.singletonList(extChain));
        Assert.assertEquals(0, uploadProject.getChildren().size());

        // load only library with dependencies
        RepositoryItemNode nodeToRetrive = findRepositoryItem(extChain, getRepositoryForTest());
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

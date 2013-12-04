/*
 * Copyright (c) 2012, Sabre Inc.
 */
package com.sabre.schemas.controllers;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLNamespaceImport;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemas.node.CoreObjectNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFinders;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.node.SimpleTypeNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.utils.BaseProjectTest;
import com.sabre.schemas.utils.ComponentNodeBuilder;
import com.sabre.schemas.utils.LibraryNodeBuilder;
import com.sabre.schemas.utils.PropertyNodeBuilder;

/**
 * @author Pawel Jedruch
 * 
 */
public class DefaultProjectControllerTest extends BaseProjectTest {

    @Test
    public void closeShouldRemoveProject() throws LibrarySaveException {
        ProjectNode toCloseProject = createProject("ToClose", rc.getLocalRepository(), "close");
        pc.close(toCloseProject);
        Assert.assertFalse(Node.getModelNode().getChildren().contains(toCloseProject));
    }

    @Test
    public void closeAllShouldRemoveProject() throws LibrarySaveException {
        ProjectNode toCloseProject = createProject("ToClose", rc.getLocalRepository(), "close");
        pc.closeAll();
        Assert.assertFalse(Node.getModelNode().getChildren().contains(toCloseProject));
    }

    @Test
    public void closeAllShouldRemoveDefaultProject() throws LibrarySaveException {
        pc.closeAll();
        Assert.assertFalse(Node.getModelNode().getChildren().contains(defaultProject));
    }

    @Test
    public void closeShouldReloadDefaultProject() throws LibrarySaveException {
        LibraryNode lib = LibraryNodeBuilder.create("TestLib",
                pc.getDefaultProject().getNamespace(), "a", Version.emptyVersion).build(
                pc.getDefaultProject(), pc);
        ProjectNode defaultProjectBeforeClose = pc.getDefaultProject();

        Assert.assertEquals(1, defaultProjectBeforeClose.getLibraries().size());
        pc.close(pc.getDefaultProject());

        ProjectNode defaultProjectAfterClose = pc.getDefaultProject();
        Assert.assertNotSame(defaultProjectBeforeClose, defaultProjectAfterClose);
        Assert.assertEquals(1, defaultProjectAfterClose.getLibraries().size());
        LibraryNode libAfterClose = defaultProjectAfterClose.getLibraries().get(0);
        Assert.assertNotSame(lib, libAfterClose);
        Assert.assertEquals(lib.getName(), libAfterClose.getName());
    }

    @Test
    public void crossLibraryLinks() throws LibrarySaveException {
        LibraryNode local1 = LibraryNodeBuilder.create("LocalOne",
                defaultProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        SimpleTypeNode so = ComponentNodeBuilder.createSimpleObject("SO")
                .assignType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE)).get();
        local1.addMember(so);

        LibraryNode local2 = LibraryNodeBuilder.create("LocalTwo",
                defaultProject.getNamespace() + "/Test/Two", "o2", new Version(1, 0, 0)).build(
                defaultProject, pc);
        PropertyNode property = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT)
                .setName("Reference").assign(so).build();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("CO").addToSummaryFacet(property)
                .get();
        local2.addMember(co);
        mc.getLibraryController().saveAllLibraries(false);
        Set<String> expectedImports = new HashSet<String>();
        for (TLNamespaceImport imported : local2.getTLLibrary().getNamespaceImports()) {
            expectedImports.add(imported.getNamespace());
        }

        mc.getLibraryController().remove(defaultProject.getLibraries());

        File local2File = URLUtils.toFile(local2.getTLLibrary().getLibraryUrl());
        defaultProject.add(Collections.singletonList(local2File));
        LibraryNode reopenedLibrary = defaultProject.getLibraries().get(0);
        TLLibrary tlLib = reopenedLibrary.getTLLibrary();

        Set<String> actaulsImports = new HashSet<String>();
        for (TLNamespaceImport imported : tlLib.getNamespaceImports()) {
            actaulsImports.add(imported.getNamespace());
        }

        for (String e : expectedImports) {
            if (!actaulsImports.contains(e)) {
                fail("Missing imported namespace: " + e);
            }
        }
    }

}

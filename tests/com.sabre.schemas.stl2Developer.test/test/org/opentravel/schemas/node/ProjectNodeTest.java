package org.opentravel.schemas.node;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.opentravel.schemas.utils.PropertyNodeBuilder;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.saver.LibraryModelSaver;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemacompiler.util.URLUtils;

public class ProjectNodeTest extends BaseProjectTest {

    @Test
    public void loadShouldResolveDependencyForAllLibrariesInProject() throws LibrarySaveException {
        // Create sample base library
        LibraryNode libBaseToClose = LibraryNodeBuilder.create("BaseToClose",
                defaultProject.getNamespace() + "/close", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        SimpleTypeNode baseSimpleObject = ComponentNodeBuilder.createSimpleObject("BaseSO").get(
                libBaseToClose);

        // Use sampled library
        LibraryNode libUsingBase = LibraryNodeBuilder.create("UsingBase",
                defaultProject.getNamespace() + "/close", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        CoreObjectNode usingCO = ComponentNodeBuilder.createCoreObject("UsingCO").get(libUsingBase);
        PropertyNode attrWithSO = PropertyNodeBuilder.create(PropertyNodeType.ATTRIBUTE)
                .addToComponent(usingCO.getSummaryFacet()).assign(baseSimpleObject).build();

        // save name and namespace before closing, used later to find it from reloaded object
        String baseSimpleObjectName = baseSimpleObject.getName();
        String baseSimpleObjectNamespace = baseSimpleObject.getNamespace();

        // save library before close
        LibraryModelSaver lms = new LibraryModelSaver();
        lms.saveLibrary(libBaseToClose.getTLLibrary());

        libBaseToClose.close();
        Assert.assertTrue(attrWithSO.isUnAssigned());

        // load library
        defaultProject.add(Collections.singletonList(URLUtils.toFile(libBaseToClose.getTLaLib()
                .getLibraryUrl())));

        // make sure all types are resolved
        Assert.assertFalse(attrWithSO.isUnAssigned());
        Node reloadedBaseSimpleObject = mc.getModelController().getModel()
                .findNode(baseSimpleObjectName, baseSimpleObjectNamespace);
        Assert.assertSame(reloadedBaseSimpleObject, attrWithSO.getAssignedType());
    }

    @Test
    public void loadShouldResolveDependencyElementForAllLibrariesInProject()
            throws LibrarySaveException {
        // Create sample base library
        LibraryNode libBaseToClose = LibraryNodeBuilder.create("BaseToClose",
                defaultProject.getNamespace() + "/close", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        SimpleTypeNode baseSimpleObject = ComponentNodeBuilder.createSimpleObject("BaseSO").get(
                libBaseToClose);

        // Use sampled library
        LibraryNode libUsingBase = LibraryNodeBuilder.create("UsingBase",
                defaultProject.getNamespace() + "/close", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        CoreObjectNode usingCO = ComponentNodeBuilder.createCoreObject("UsingCO").get(libUsingBase);
        PropertyNode attrWithSO = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT)
                .addToComponent(usingCO.getSummaryFacet()).assign(baseSimpleObject).build();

        // save name and namespace before closing, used later to find it from reloaded object
        String baseSimpleObjectName = baseSimpleObject.getName();
        String baseSimpleObjectNamespace = baseSimpleObject.getNamespace();

        // save library before close
        LibraryModelSaver lms = new LibraryModelSaver();
        lms.saveLibrary(libBaseToClose.getTLLibrary());

        libBaseToClose.close();
        Assert.assertTrue(attrWithSO.isUnAssigned());

        // load library
        defaultProject.add(Collections.singletonList(URLUtils.toFile(libBaseToClose.getTLaLib()
                .getLibraryUrl())));

        // make sure all types are resolved
        Assert.assertFalse(attrWithSO.isUnAssigned());
        Node reloadedBaseSimpleObject = mc.getModelController().getModel()
                .findNode(baseSimpleObjectName, baseSimpleObjectNamespace);
        Assert.assertSame(reloadedBaseSimpleObject, attrWithSO.getAssignedType());
    }
}

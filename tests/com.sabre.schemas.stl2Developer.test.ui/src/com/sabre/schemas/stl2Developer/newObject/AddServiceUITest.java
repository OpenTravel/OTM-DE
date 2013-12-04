/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.newObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.saver.LibraryModelSaver;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.OperationNode;
import com.sabre.schemas.node.OperationNode.ResourceOperationTypes;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.stl2Developer.ui.MenuHelper;
import com.sabre.schemas.stl2Developer.ui.helper.NavigatorViewHelper;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.trees.library.LibraryTreeLabelProvider;
import com.sabre.schemas.utils.ComponentNodeBuilder;
import com.sabre.schemas.utils.LibraryNodeBuilder;

/**
 * @author Pawel Jedruch
 * 
 */
public class AddServiceUITest {

    private static final SWTWorkbenchBot bot = new SWTWorkbenchBot();
    private NavigatorViewHelper navigatorView;

    @Before
    public void beforeEachTest() {
        navigatorView = new NavigatorViewHelper();
    }

    @Test
    @Ignore("for gtk native new file is differently on workaround with pressing keyboard doesn't work")
    public void addServiceWithSelectedBO() throws LibrarySaveException {
        LibraryNode l = createLibrary("Name");
        l.addMember(ComponentNodeBuilder.createBusinessObject("BO").get());
        saveLibrary(l);

        MenuHelper.openLibrary(bot, URLUtils.toFile(l.getTLLibrary().getLibraryUrl()));
        navigatorView.seletLibraryInDefaultProject(navigatorView.getLabelProvider().getText(l));
        navigatorView.newService(new LibraryTreeLabelProvider().getText(l), "Built-In Libraries",
                "STL2_Deprecated_Model", "Complex Objects", "FlightLeg_Identifier");

        ServiceNode service = getService(l);
        String serviceLabel = navigatorView.getLabelProvider().getText(service);

        SWTBotTreeItem serviceItem = navigatorView.getLibrary(
                new LibraryTreeLabelProvider().getText(l)).getNode(serviceLabel);
        serviceItem.expand();

        HashSet<String> operationTypes = new HashSet<String>();
        for (String nodes : serviceItem.getNodes()) {
            String type = nodes.replaceFirst(OperationNode.OPERATION_PREFIX, "");
            assertIsValidOperationType(type);
            operationTypes.add(type);
        }

        assertEquals("Missing operation. Should create all opeartion except QUERY.",
                ResourceOperationTypes.values().length - 1, operationTypes.size());
    }

    private void assertIsValidOperationType(String type) {
        for (ResourceOperationTypes op : ResourceOperationTypes.values()) {
            if (op.displayName.equals(type)) {
                return;
            }
        }
        fail(type + ": is incorrect. Valid types: "
                + Arrays.asList(ResourceOperationTypes.values()));
    }

    private ServiceNode getService(LibraryNode l) {
        for (Node child : l.getChildren()) {
            if (child instanceof ServiceNode) {
                return (ServiceNode) child;
            }
        }
        return null;
    }

    private LibraryNode createLibrary(String name) throws LibrarySaveException {
        final MainController mc = OtmRegistry.getMainController();
        ProjectController pc = mc.getProjectController();
        ProjectNode defaultProject = pc.getDefaultProject();
        return LibraryNodeBuilder.create(name, defaultProject.getNamespace(), name.substring(0, 1),
                new Version(1, 0, 0)).build(defaultProject, pc);
    }

    private void saveLibrary(LibraryNode l) throws LibrarySaveException {
        new LibraryModelSaver().saveLibrary(l.getTLLibrary());
    }
}

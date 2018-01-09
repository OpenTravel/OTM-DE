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
package org.opentravel.schemas.stl2Developer.newObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.OperationNode.ServiceOperationTypes;
import org.opentravel.schemas.stl2Developer.ui.MenuHelper;
import org.opentravel.schemas.stl2Developer.ui.helper.NavigatorViewHelper;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.library.LibraryTreeLabelProvider;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;

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

		SWTBotTreeItem serviceItem = navigatorView.getLibrary(new LibraryTreeLabelProvider().getText(l)).getNode(
				serviceLabel);
		serviceItem.expand();

		HashSet<String> operationTypes = new HashSet<String>();
		for (String nodes : serviceItem.getNodes()) {
			String type = nodes.replaceFirst(NodeNameUtils.OPERATION_PREFIX, "");
			assertIsValidOperationType(type);
			operationTypes.add(type);
		}

		assertEquals("Missing operation. Should create all opeartion except QUERY.",
				ServiceOperationTypes.values().length - 1, operationTypes.size());
	}

	private void assertIsValidOperationType(String type) {
		for (ServiceOperationTypes op : ServiceOperationTypes.values()) {
			if (op.displayName.equals(type)) {
				return;
			}
		}
		fail(type + ": is incorrect. Valid types: " + Arrays.asList(ServiceOperationTypes.values()));
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

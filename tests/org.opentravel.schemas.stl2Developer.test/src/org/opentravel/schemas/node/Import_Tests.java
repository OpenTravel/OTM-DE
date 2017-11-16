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
/**
 * 
 */
package org.opentravel.schemas.node;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class Import_Tests {
	private static final Logger LOGGER = LoggerFactory.getLogger(Import_Tests.class);

	ModelNode model = null;
	LoadFiles lf = new LoadFiles();
	MockLibrary ml = new MockLibrary();
	LibraryNode ln = null;
	MainController mc;
	ProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeEachTest() {
		// mc = OtmRegistry.getMainController(); // don't do this - it messes up the project controller
		// if (mc == null)
		mc = new MainController();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void ImportTest() throws Exception {
		NodeTesters nt = new NodeTesters();

		LibraryNode sourceLib = lf.loadFile5Clean(mc);
		LibraryNode destLib = lf.loadFile1(mc);
		final String DestLibNs = "http://www.opentravel.org/Sandbox/junits/ns1/v1";
		// Make sure they loaded OK.
		ml.check(sourceLib);
		ml.check(destLib);

		// LOGGER.debug("\n");
		LOGGER.debug("Start Import ***************************");
		// int destTypes = destLib.getDescendants_NamedTypes().size();

		// make sure that destLib is editable (move to project with correct ns)
		String projectFile = MockLibrary.createTempFile("TempProject", ".otp");
		ProjectNode project = pc.create(new File(projectFile), destLib.getNamespace(), "Name", "");
		destLib = pc.add(project, destLib.getTLModelObject()).getLibrary();
		// FIXME - copy test lib so it is editable
		// assertTrue(destLib.isEditable());
		// Will not be editable if testFile1 is not editable

		// Make sure the source is still OK
		ml.check(sourceLib);
		ml.check(destLib);

		// Make sure the imported nodes are OK.
	}

	@Test
	public void importNode() {
		ml = new MockLibrary();

		LibraryNode target = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(target, "testBO");
		CoreObjectNode core = ml.addCoreObjectToLibrary(target, "testCore");

		target.importNode(bo);
		target.importNode(core);
		Assert.assertEquals(3, target.getDescendants_LibraryMembers().size());
	}

	@Test
	public void createAliases() {
		ml = new MockLibrary();

		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "testBO");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "testCore");
		FacetNode summary = bo.getFacet_Summary();
		int coreKids = core.getChildren().size();
		int startingCount = summary.getChildren().size();

		// Add 3 core objects as property types to see the aliases get made.
		ElementNode prop1, prop2, prop3 = null;
		prop1 = new ElementNode(summary, "P1");
		prop1.setAssignedType(core);
		prop2 = new ElementNode(summary, "P2");
		prop2.setAssignedType(core);
		prop3 = new ElementNode(summary, "P3");
		prop3.setAssignedType(core);

		bo.createAliasesForProperties();
		// FIXME - i now have 3 aliases on the core with the same name because the assign type changed the properties to
		// the same name
		Assert.assertTrue(startingCount + 3 == summary.getChildren().size()); // 1 + the three added
		Assert.assertEquals(coreKids + 3, core.getChildren().size());
		// Assert.assertEquals("P1_testCore", prop1.getName());
		// Assert.assertEquals("P1_testCore", prop1.getTypeName());
		// Assert.assertEquals("P1_testCore", prop1.getTLTypeObject().getLocalName());

	}
}

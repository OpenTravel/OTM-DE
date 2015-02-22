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

import junit.framework.Assert;

import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class Import_Tests {
	private static final Logger LOGGER = LoggerFactory.getLogger(Import_Tests.class);

	TestNode nt = new NodeTesters().new TestNode();
	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc = new MainController();
	ProjectController pc = mc.getProjectController();
	ProjectNode defaultProject = pc.getDefaultProject();

	@Test
	public void ImportTest() throws Exception {
		NodeTesters nt = new NodeTesters();

		LibraryNode sourceLib = lf.loadFile5Clean(mc);
		LibraryNode destLib = lf.loadFile1(mc);

		// Make sure they loaded OK.
		sourceLib.visitAllNodes(nt.new TestNode());
		destLib.visitAllNodes(nt.new TestNode());

		// LOGGER.debug("\n");
		LOGGER.debug("Start Import ***************************");
		// int destTypes = destLib.getDescendants_NamedTypes().size();

		// make sure that destLib is editable (move to project with correct ns)
		String projectFile = MockLibrary.createTempFile("TempProject", ".otp");
		ProjectNode project = pc.create(new File(projectFile), destLib.getNamespace(), "Name", "");
		destLib = pc.add(project, destLib.getTLaLib());
		Assert.assertTrue(destLib.isEditable());

		// Make sure the source is still OK
		sourceLib.visitAllNodes(nt.new TestNode());

		// Make sure the imported nodes are OK.
		destLib.visitAllNodes(nt.new TestNode());
	}

	@Test
	public void ImportInManagedTest() throws Exception {
		NodeTesters nt = new NodeTesters();

		LibraryNode sourceLib = lf.loadFile5Clean(mc);
		LibraryNode destLib = lf.loadFile1(mc);
		LibraryChainNode lcn = new LibraryChainNode(destLib);
		lcn.add(sourceLib.getProjectItem());

		// Make sure they loaded OK.
		sourceLib.visitAllNodes(nt.new TestNode());
		destLib.visitAllNodes(nt.new TestNode());

		// LOGGER.debug("\n");
		LOGGER.debug("Start Import ***************************");
		// int destTypes = destLib.getDescendants_NamedTypes().size();

		// make sure that destLib is editable (move to project with correct ns)
		String projectFile = MockLibrary.createTempFile("TempProject", ".otp");
		ProjectNode project = pc.create(new File(projectFile), destLib.getNamespace(), "Name", "");
		destLib = pc.add(project, destLib.getTLaLib());
		Assert.assertTrue(destLib.isEditable());

		// Make sure the source is still OK
		sourceLib.visitAllNodes(nt.new TestNode());

		// Make sure the imported nodes are OK.
		destLib.visitAllNodes(nt.new TestNode());
	}

	@Test
	public void importNode() {
		ml = new MockLibrary();

		LibraryNode target = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(target, "testBO");
		CoreObjectNode core = ml.addCoreObjectToLibrary(target, "testCore");
		int beforeImportFamilies = familyCount(target);

		target.importNode(bo);
		target.importNode(core);
		Assert.assertEquals(3, target.getDescendants_NamedTypes().size());
		Assert.assertEquals(beforeImportFamilies, familyCount(target));
	}

	private int familyCount(LibraryNode ln) {
		int count = 0;
		for (Node n : ln.getDescendants())
			if (n instanceof FamilyNode)
				count++;
		return count;
	}

	@Test
	public void createAliases() {
		ml = new MockLibrary();

		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "testBO");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "testCore");
		FacetNode summary = bo.getSummaryFacet();
		int coreKids = core.getChildren().size();

		// Add 3 core objects as property types to see the aliases get made.
		ElementNode prop1, prop2, prop3 = null;
		prop1 = new ElementNode(summary, "P1");
		prop1.setAssignedType(core);
		prop2 = new ElementNode(summary, "P2");
		prop2.setAssignedType(core);
		prop3 = new ElementNode(summary, "P3");
		prop3.setAssignedType(core);

		bo.createAliasesForProperties();
		Assert.assertTrue(summary.getChildren().size() == 4);
		Assert.assertEquals(coreKids + 3, core.getChildren().size());
		Assert.assertEquals("P1_testCore", prop1.getName());
		Assert.assertEquals("P1_testCore", prop1.getTypeName());
		Assert.assertEquals("P1_testCore", prop1.getTLTypeObject().getLocalName());

	}
}

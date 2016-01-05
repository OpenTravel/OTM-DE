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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
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
public class InheritedChildren_Tests {
	private final static Logger LOGGER = LoggerFactory.getLogger(ComponentNode.class);

	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	BusinessObjectNode baseBO, extensionBO;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		baseBO = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		extensionBO = ml.addBusinessObjectToLibrary(ln, "ExtensionBO");
		extensionBO.setExtendsType(baseBO);
		Assert.assertFalse(extensionBO.getExtendsTypeName().isEmpty());
	}

	@Test
	public void initTest() {
		List<Node> inherited = null;
		FacetNode f;
		inherited = extensionBO.getInheritedChildren();
		Assert.assertTrue(inherited.isEmpty()); // none on the BO
		inherited = extensionBO.getSummaryFacet().getInheritedChildren();
		Assert.assertFalse(inherited.isEmpty());
		Assert.assertEquals(1, inherited.size());
		Assert.assertTrue(inherited.get(0).isInheritedProperty());

		// FIXME - removed the facet node constuctor, use bo method
		// f = new FacetNode(baseBO, "Custom", "", TLFacetType.CUSTOM);
		// inherited = extensionBO.getInheritedChildren();
		// Assert.assertEquals(0, inherited.size());
		LOGGER.debug("Done");
	}

	@Test
	public void settingBase() {
		BusinessObjectNode bo2 = ml.addBusinessObjectToLibrary(ln, "Bo2");
		FacetNode sf = bo2.getSummaryFacet();
		List<?> children = sf.getChildren();
		bo2.setExtendsType(baseBO);
		Assert.assertEquals(sf, bo2.getSummaryFacet());
		List<?> inherited = sf.getInheritedChildren();
		LOGGER.debug("Done");
	}
}

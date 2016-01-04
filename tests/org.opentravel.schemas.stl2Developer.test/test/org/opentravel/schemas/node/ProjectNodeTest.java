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
package org.opentravel.schemas.node;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.opentravel.schemas.utils.PropertyNodeBuilder;
import org.osgi.framework.Version;

public class ProjectNodeTest extends BaseProjectTest {

	@Test
	public void loadShouldResolveDependencyForAllLibrariesInProject() throws LibrarySaveException {
		// Create sample base library
		LibraryNode libBaseToClose = LibraryNodeBuilder.create("BaseToClose", defaultProject.getNamespace() + "/close",
				"o1", new Version(1, 0, 0)).build(defaultProject, pc);
		SimpleTypeNode baseSimpleObject = ComponentNodeBuilder.createSimpleObject("BaseSO").get(libBaseToClose);

		// Use sampled library
		LibraryNode libUsingBase = LibraryNodeBuilder.create("UsingBase", defaultProject.getNamespace() + "/close",
				"o1", new Version(1, 0, 0)).build(defaultProject, pc);
		CoreObjectNode usingCO = ComponentNodeBuilder.createCoreObject("UsingCO").get(libUsingBase);
		PropertyNode attrWithSO = PropertyNodeBuilder.create(PropertyNodeType.ATTRIBUTE)
				.addToComponent((ComponentNode) usingCO.getSummaryFacet()).assign(baseSimpleObject).build();

		// save name and namespace before closing, used later to find it from reloaded object
		String baseSimpleObjectName = baseSimpleObject.getName();
		String baseSimpleObjectNamespace = baseSimpleObject.getNamespace();

		// save library before close
		LibraryModelSaver lms = new LibraryModelSaver();
		lms.saveLibrary(libBaseToClose.getTLLibrary());

		libBaseToClose.close();
		Assert.assertTrue(attrWithSO.isUnAssigned());

		// load library
		defaultProject.add(Collections.singletonList(URLUtils.toFile(libBaseToClose.getTLaLib().getLibraryUrl())));

		// make sure all types are resolved
		Assert.assertFalse(attrWithSO.isUnAssigned());
		Node reloadedBaseSimpleObject = mc.getModelController().getModel()
				.findNode(baseSimpleObjectName, baseSimpleObjectNamespace);
		Assert.assertSame(reloadedBaseSimpleObject, attrWithSO.getAssignedType());
	}

	@Test
	public void loadShouldResolveDependencyElementForAllLibrariesInProject() throws LibrarySaveException {
		// Create sample base library
		LibraryNode libBaseToClose = LibraryNodeBuilder.create("BaseToClose", defaultProject.getNamespace() + "/close",
				"o1", new Version(1, 0, 0)).build(defaultProject, pc);
		SimpleTypeNode baseSimpleObject = ComponentNodeBuilder.createSimpleObject("BaseSO").get(libBaseToClose);

		// Use sampled library
		LibraryNode libUsingBase = LibraryNodeBuilder.create("UsingBase", defaultProject.getNamespace() + "/close",
				"o1", new Version(1, 0, 0)).build(defaultProject, pc);
		CoreObjectNode usingCO = ComponentNodeBuilder.createCoreObject("UsingCO").get(libUsingBase);
		PropertyNode attrWithSO = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT)
				.addToComponent((ComponentNode) usingCO.getSummaryFacet()).assign(baseSimpleObject).build();

		// save name and namespace before closing, used later to find it from reloaded object
		String baseSimpleObjectName = baseSimpleObject.getName();
		String baseSimpleObjectNamespace = baseSimpleObject.getNamespace();

		// save library before close
		LibraryModelSaver lms = new LibraryModelSaver();
		lms.saveLibrary(libBaseToClose.getTLLibrary());

		libBaseToClose.close();
		Assert.assertTrue(attrWithSO.isUnAssigned());

		// load library
		defaultProject.add(Collections.singletonList(URLUtils.toFile(libBaseToClose.getTLaLib().getLibraryUrl())));

		// make sure all types are resolved
		Assert.assertFalse(attrWithSO.isUnAssigned());
		Node reloadedBaseSimpleObject = mc.getModelController().getModel()
				.findNode(baseSimpleObjectName, baseSimpleObjectNamespace);
		Assert.assertSame(reloadedBaseSimpleObject, attrWithSO.getAssignedType());
	}
}

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

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.node.handlers.NamespaceHandler;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class NamespaceHandler_Tests extends BaseTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(NamespaceHandler_Tests.class);

	@Test
	public void mockLibTest() {
		String ns = "http://foo.bar";
		final String ns2 = "http://foo.bar/too";
		final String pre = "aaa";

		LibraryNode ln = ml.createNewLibrary(ns, "test", defaultProject);
		ln.setNSPrefix(pre);
		Assert.assertFalse(ln.getNamespace().isEmpty());
		Assert.assertFalse(ln.getPrefix().isEmpty());
		assert ln.getPrefix().equals(pre);
		assert ln.getNamespace().startsWith(ns); // version will be added

		// Test setting to a namespace not in the handler registry.
		ln.setNamespace(ns2);
		Assert.assertFalse(ln.getNamespace().isEmpty());
		Assert.assertFalse(ln.getPrefix().isEmpty());
		assert ln.getPrefix().equals(pre);
		assert ln.getNamespace().startsWith(ns2);
	}

	@Test
	public void nsHandlerTest() throws Exception {
		// MainController mc = OtmRegistry.getMainController();
		final String testNS = "http://www.opentravel.org/ns/TEST";

		final String testExtension = "Test";
		final String testVersionError = "0.1"; // period is error
		final String testVerison = "0.1.0"; // as will be used
		final String versionID = "v0_1"; // as used in ns
		final String expectedFullNS = mc.getRepositoryController().getLocalRepository().getNamespace() + "/Test/"
				+ versionID;

		lf.loadTestGroupA(mc);
		for (LibraryNode ln : Node.getAllLibraries()) {
			ml.check(ln);

			// Make sure the handlers are assigned correctly.
			Assert.assertNotNull(NamespaceHandler.getNamespaceHandler(ln));
			NamespaceHandler handler = NamespaceHandler.getNamespaceHandler(ln);
			Assert.assertNotNull(ln.getNsHandler());
			Assert.assertTrue(handler == ln.getNsHandler());
		}

		for (LibraryNode ln : Node.getAllUserLibraries()) {
			NamespaceHandler handler = NamespaceHandler.getNamespaceHandler(ln);
			String namespace = ln.getNamespace();
			String prefix = ln.getPrefix();

			Assert.assertFalse(namespace.isEmpty());
			Assert.assertFalse(prefix.isEmpty());
			String handlerPrefix = handler.getPrefix(namespace);
			if (handlerPrefix == null || !handlerPrefix.equals(prefix))
				LOGGER.error("Prefixes do not match.");
			assertTrue("Library and handler must have same prefix.", prefix.equals(handler.getPrefix(namespace)));

			// Just make sure these do not cause error
			handler.getNSBase();
			handler.getNSExtension(namespace);
			handler.getNSVersion(namespace);
			handler.createValidNamespace(namespace, testVersionError);
			handler.createValidNamespace(namespace, testExtension, testVersionError);

			// Set - should handle error of duplicate name/ns without failing
			handler.setNamespacePrefix(testNS, "TST");
			handler.setLibraryNamespace(ln, testNS);

			LOGGER.debug("Namespace tests on " + ln + " complete.");
		}

		for (ProjectNode pn : Node.getAllProjects()) {
			NamespaceHandler handler = NamespaceHandler.getNamespaceHandler(pn);
			Assert.assertNotNull(handler);
		}

		// Test the namespace string functions
		NamespaceHandler handler = NamespaceHandler.getNamespaceHandler(mc.getProjectController().getDefaultProject());
		Assert.assertNotNull(handler);

		String nsb = handler.getNSBase();
		String ns = handler.createValidNamespace(nsb, testVersionError);
		ns = handler.createValidNamespace(nsb, testExtension, testVersionError);
		Assert.assertTrue(ns.equals(expectedFullNS));

		Assert.assertTrue(nsb.equals(handler.getNSBase()));
		Assert.assertTrue(testExtension.equals(handler.getNSExtension(ns)));
		String version = handler.getNSVersion(ns);
		Assert.assertTrue(testVerison.equals(handler.getNSVersion(ns)));

		Assert.assertTrue(handler.isValidNamespace(ns).isEmpty());
		Assert.assertFalse(handler.isValidNamespace("foo:urn:junk").isEmpty());
		Assert.assertFalse(handler.isValidNamespace("http://tempuri.org/ns").isEmpty());
	}
}

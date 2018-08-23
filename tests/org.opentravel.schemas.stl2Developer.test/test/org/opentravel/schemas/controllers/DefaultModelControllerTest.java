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
package org.opentravel.schemas.controllers;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemas.stl2developer.OtmRegistry;

/**
 * @author Pawel Jedruch
 * 
 */
public class DefaultModelControllerTest {

	private static MainController mc;
	private static DefaultModelController dc;

	@BeforeClass
	public static void boforeTests() {
		mc = OtmRegistry.getMainController();
		dc = (DefaultModelController) mc.getModelController();
	}

	// FIXME - find a home for these documentation test utilities
	private TLDocumentation createSampleDoc() {
		TLDocumentation doc = new TLDocumentation();
		doc.addDeprecation(createDocItem("deprecation"));
		doc.addImplementer(createDocItem("implementer"));
		doc.addMoreInfo(createDocItem("moreinfo"));
		doc.addReference(createDocItem("reference"));
		doc.addOtherDoc(createDocItem("otherdoc", "context"));
		doc.setDescription("Description");
		return doc;
	}

	private void assertDocumentationEquals(TLDocumentation expected, TLDocumentation actual) {
		Assert.assertEquals(expected.getDescription(), actual.getDescription());
		assertListemItemEquals(expected.getDeprecations(), actual.getDeprecations());
		assertListemItemEquals(expected.getImplementers(), actual.getImplementers());
		assertListemItemEquals(expected.getMoreInfos(), actual.getMoreInfos());
		assertListemItemEquals(expected.getReferences(), actual.getReferences());
		assertListemItemEquals(expected.getOtherDocs(), actual.getOtherDocs());

	}

	private void assertListemItemEquals(List<? extends TLDocumentationItem> expected,
			List<? extends TLDocumentationItem> actual) {
		Assert.assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertDocumentationItemEquals(expected.get(i), actual.get(i));
		}
	}

	private void assertDocumentationItemEquals(TLDocumentationItem actual, TLDocumentationItem expected) {
		Assert.assertTrue(actual.getClass().isInstance(expected));
		if (actual instanceof TLAdditionalDocumentationItem) {
			((TLAdditionalDocumentationItem) actual).getContext()
					.equals(((TLAdditionalDocumentationItem) expected).getContext());
		}
		Assert.assertEquals(actual.getText(), expected.getText());
	}

	private TLDocumentationItem createDocItem(String text) {
		TLDocumentationItem item = new TLDocumentationItem();
		item.setText(text);
		return item;
	}

	private TLAdditionalDocumentationItem createDocItem(String text, String context) {
		TLAdditionalDocumentationItem item = new TLAdditionalDocumentationItem();
		item.setText(text);
		item.setContext(context);
		return item;
	}

}

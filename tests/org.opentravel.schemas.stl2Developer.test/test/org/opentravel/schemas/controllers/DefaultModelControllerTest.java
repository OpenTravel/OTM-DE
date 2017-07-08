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
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.PropertyNodeBuilder;

/**
 * @author Pawel Jedruch
 * 
 */
public class DefaultModelControllerTest {

	private static MainController mc;
	private static DefaultModelController dc;

	@BeforeClass
	public static void boforeTests() {
		mc = new MainController();
		dc = (DefaultModelController) mc.getModelController();
	}

	@Test
	public void changeToSimpleShouldChangeSimpleType() {
		ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
		PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType).build();
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p).get();
		TLCoreObject tco = (TLCoreObject) co.getTLModelObject();

		boolean res = dc.changeToSimple(p);

		Assert.assertTrue(res);
		Assert.assertSame(coType, co.getSimpleType());
		Assert.assertEquals(1, co.getFacet_Simple().getChildren().size());

		// 2/3/2015 dmh added tests to assure properties are removed
		Assert.assertEquals(0, co.getFacet_Summary().getChildren().size()); // checks node structure
		Assert.assertEquals(0, tco.getSummaryFacet().getElements().size()); // checks TL Model structure
		// 2/3/2015 dmh added VWA test
		ComponentNode simple = ComponentNodeBuilder.createSimpleObject("simple").get();
		PropertyNode p2 = PropertyNodeBuilder.create(PropertyNodeType.ATTRIBUTE).assign(simple).build();
		VWA_Node vwa = ComponentNodeBuilder.createVWA("NVA").addAttribute(p2).get();
		TypeProvider vwaType = vwa.getSimpleType();
		TLValueWithAttributes tvwa = (TLValueWithAttributes) vwa.getTLModelObject();
		Assert.assertEquals(vwaType, vwa.getSimpleType());
		res = dc.changeToSimple(p2);
		Assert.assertTrue(res);
		Assert.assertEquals(p2.getType(), vwa.getSimpleType());
		Assert.assertEquals(0, vwa.getAttributeFacet().getChildren().size()); // checks node structure
		Assert.assertEquals(0, tvwa.getAttributes().size()); // checks TL Model structure
	}

	@Test
	public void changeToSimpleShouldAlwaysBeAttribute() {
		ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
		PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType).build();
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p).get();

		dc.changeToSimple(p);

		Assert.assertTrue(co.getFacet_Simple().getSimpleAttribute() instanceof SimpleAttributeNode);
	}

	@Test
	public void changeToSimpleShouldRemoveProperty() {
		ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
		PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType).build();
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p).get();

		boolean res = dc.changeToSimple(p);

		Assert.assertTrue(res);
		Assert.assertTrue(co.getFacet_Summary().getChildren().isEmpty());
	}

	@Test
	public void changeToSimpleSimplePropertyShouldReturnFalse() {
		ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
		PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType).build();
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p).get();

		boolean res = dc.changeToSimple((PropertyNode) co.getFacet_Simple().getSimpleAttribute());

		Assert.assertFalse(res);
	}

	@Test
	public void changeToSimpleShouldCopyDocumentation() {
		ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
		PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType)
				.setDocumentation(createSampleDoc()).build();
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p).get();

		dc.changeToSimple(p);
		assertDocumentationEquals(createSampleDoc(), co.getFacet_Simple().getSimpleAttribute().getDocumentation());
	}

	@Test
	public void changeFromSimpleNotSimpleAttributeShouldReturnFalse() {
		PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).build();
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p).get();
		ComponentNode newProperty = dc.moveSimpleToFacet(p, (ComponentNode) co.getFacet_Summary());
		Assert.assertNull(newProperty);
	}

	@Test
	public void changeFromSimpleShouldCreateNewProperty() {
		ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").get();
		co.getFacet_Simple().getSimpleAttribute().setAssignedType((TypeProvider) coType);

		// make sure that summary is empty
		Assert.assertEquals(0, co.getFacet_Summary().getChildren().size());
		ComponentNode newProperty = dc.moveSimpleToFacet(co.getFacet_Simple().getSimpleAttribute(),
				(ComponentNode) co.getFacet_Summary());

		Assert.assertNotNull(newProperty);
		Assert.assertEquals(1, co.getFacet_Summary().getChildren().size());
		Assert.assertSame(newProperty, co.getFacet_Summary().getChildren().get(0));
		Assert.assertSame(coType, newProperty.getType());
	}

	@Test
	public void changeFromSimpleShouldLeftSimpleAttributeWithEmpty() {
		ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").get();
		co.getFacet_Simple().getSimpleAttribute().setAssignedType((TypeProvider) vwaType);

		dc.moveSimpleToFacet(co.getFacet_Simple().getSimpleAttribute(), (ComponentNode) co.getFacet_Summary());

		Assert.assertSame(ModelNode.getEmptyNode(), co.getFacet_Simple().getSimpleAttribute().getType());

	}

	@Test
	public void changeFromSimpleShouldCreateAttributeForVWA() {
		ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();
		VWA_Node co = ComponentNodeBuilder.createVWA("VWA").get();

		co.getFacet_Simple().getSimpleAttribute().setAssignedType((TypeProvider) vwaType);

		ComponentNode newProperty = dc.moveSimpleToFacet(co.getFacet_Simple().getSimpleAttribute(),
				(ComponentNode) co.getFacet_Summary());

		Assert.assertTrue(newProperty instanceof AttributeNode);
	}

	@Test
	public void changeFromSimpleShouldCreateElementForNotVWA() {
		ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").get();
		co.getFacet_Simple().getSimpleAttribute().setAssignedType((TypeProvider) vwaType);

		ComponentNode newProperty = dc.moveSimpleToFacet(co.getFacet_Simple().getSimpleAttribute(),
				(ComponentNode) co.getFacet_Summary());

		Assert.assertTrue(newProperty instanceof ElementNode);
	}

	@Test
	public void changeFromSimpleShouldStripSimpleSuffix() {
		ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();
		String typeName = "NewCoreObject";
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject(typeName).get();
		co.getFacet_Simple().getSimpleAttribute().setAssignedType((TypeProvider) vwaType);

		ComponentNode newProperty = dc.moveSimpleToFacet(co.getFacet_Simple().getSimpleAttribute(),
				(ComponentNode) co.getFacet_Summary());

		Assert.assertEquals(typeName, newProperty.getName());
	}

	@Test
	public void changeFromSimpleShouldStripSimpleSuffixVWA() {
		ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();

		VWA_Node co = ComponentNodeBuilder.createVWA("VWA").get();
		co.getFacet_Simple().getSimpleAttribute().setAssignedType((TypeProvider) vwaType);

		String name = co.getFacet_Simple().getSimpleAttribute().getName();
		ComponentNode newProperty = dc.moveSimpleToFacet(co.getFacet_Simple().getSimpleAttribute(),
				(ComponentNode) co.getFacet_Summary());

		Assert.assertEquals(NodeNameUtils.fixAttributeName(name), newProperty.getName());
	}

	@Test
	public void changeFromSimpleShouldCopyDocumentation() {
		VWA_Node vwaType = ComponentNodeBuilder.createVWA("Type").get();

		VWA_Node co = ComponentNodeBuilder.createVWA("VWA").get();
		SimpleAttributeNode san = (SimpleAttributeNode) co.getFacet_Simple().getSimpleAttribute();
		TLDocumentation doc = createSampleDoc();
		san.getModelObject().setDocumentation(doc);
		san.setAssignedType(vwaType);

		ComponentNode newProperty = dc.moveSimpleToFacet(co.getFacet_Simple().getSimpleAttribute(),
				(ComponentNode) co.getFacet_Summary());
		Assert.assertNotSame(doc, newProperty.getDocumentation());
		assertDocumentationEquals(doc, newProperty.getDocumentation());

	}

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
			((TLAdditionalDocumentationItem) actual).getContext().equals(
					((TLAdditionalDocumentationItem) expected).getContext());
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

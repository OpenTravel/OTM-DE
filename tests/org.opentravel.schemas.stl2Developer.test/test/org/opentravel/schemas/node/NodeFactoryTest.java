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

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.types.TypeProvider;

/**
 * @author Pawel Jedruch
 * 
 */
public class NodeFactoryTest {

	@Test
	public void createElementNodeShouldPersistType() {
		TLProperty tl = new TLProperty();
		XSDSimpleType simpleType = new XSDSimpleType("string", null);
		tl.setType(simpleType);
		ElementNode element = (ElementNode) NodeFactory.newMember(null, tl);
		TLProperty afterCreate = (TLProperty) element.getModelObject().getTLModelObj();
		Assert.assertSame(simpleType, afterCreate.getType());
	}

	@Test
	public void guiTypeAccess() {
		TLProperty tl = new TLProperty();
		TLSimple tlSimple = new TLSimple();
		XSDSimpleType xsdSimple = new XSDSimpleType("string", null);
		tlSimple.setParentType(xsdSimple);
		tl.setType(tlSimple);
		Assert.assertSame(tlSimple, tl.getType());

		ElementNode element = (ElementNode) NodeFactory.newMember(null, tl);
		TLProperty afterCreate = (TLProperty) element.getModelObject().getTLModelObj();
		Assert.assertSame(tlSimple, afterCreate.getType());

		// 3/7/2016 dmh - new typehandler will not try to assign name.
		String s = element.getTypeName();
		s = element.getTypeNameWithPrefix();
		TypeProvider n = element.getAssignedType();
		Assert.assertTrue(element.getTypeName().isEmpty()); // properties view
		Assert.assertTrue(element.getTypeNameWithPrefix().isEmpty()); // facet view
		Assert.assertNull(element.getAssignedType());

		// // These will all return UNASSIGNED since resolver has not run.
		// // getTypeName tries to fix the Type node resulting in a unassigned node.
		// Assert.assertFalse(element.getTypeName().isEmpty()); // properties view
		// Assert.assertFalse(element.getTypeNameWithPrefix().isEmpty()); // facet view
		// Assert.assertNotNull(element.getAssignedType());
	}
}

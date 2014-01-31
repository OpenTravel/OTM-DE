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
package org.opentravel.schemas.modelObject;

import org.junit.Test;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * @author Pawel Jedruch
 * 
 */
public class ValueWithAttributesAttributeFacetMOTest {

    @Test
    public void infinityLoopOnVWA() {
        TLValueWithAttributes vwaFirst = new TLValueWithAttributes();
        TLValueWithAttributes vwaSecond = new TLValueWithAttributes();
        TLAttribute attrOfSecondType = new TLAttribute();
        attrOfSecondType.setType(vwaSecond);
        vwaFirst.addAttribute(attrOfSecondType);
        // make loop
        TLAttribute attrOfFirstType = new TLAttribute();
        attrOfFirstType.setType(vwaFirst);
        vwaSecond.addAttribute(attrOfFirstType);

        // stack over flow here. Infinity loop
        PropertyCodegenUtils.getInheritedIndicators(vwaFirst);
        PropertyCodegenUtils.getInheritedAttributes(vwaFirst);
    }

}

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
package org.opentravel.schemas.node.handlers.children;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.codegen.util.EnumCodegenUtils;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;

public class EnumerationOpenChildrenHandler extends CachingChildrenHandler<Node, EnumerationOpenNode> {

	public EnumerationOpenChildrenHandler(final EnumerationOpenNode obj) {
		super(obj);
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		List<TLModelElement> kids = new ArrayList<TLModelElement>();
		for (TLModelElement tl : getChildren_TLValues())
			kids.add(tl);
		return kids;
	}

	public List<TLEnumValue> getChildren_TLValues() {
		return owner.getTLModelObject().getValues();
	}

	@Override
	public List<TLModelElement> getInheritedChildren_TL() {
		final List<TLModelElement> inheritedKids = new ArrayList<TLModelElement>();
		// false prevents Codegen utils from returning non-inherited values
		inheritedKids.addAll(EnumCodegenUtils.getInheritedValues(owner.getTLModelObject(), false));
		return inheritedKids;
	}

	// // TODO - why isn't this behavior in super type?
	// @Override
	// protected void initInherited() {
	// initRunning = true;
	// inheritedOwner = owner.getExtendsType();
	// inherited = modelTLs(getInheritedChildren_TL(), inheritedOwner);
	// initRunning = false;
	// }

	// /**
	// * @see org.opentravel.schemas.modelObject.ModelObject#getInheritedChildren()
	// */
	// @Override
	// public List<?> getInheritedChildren() {
	// final List<TLModelElement> inheritedKids = new ArrayList<TLModelElement>();
	// final TLOpenEnumeration openEnum = getTLModelObj();
	// TLAttribute other = new TLAttribute();
	// other.setName("Other_" + openEnum.getName());
	// inheritedKids.add(other);
	// inheritedKids.addAll(getInheritedValues());
	// // The Codegen utils also insert non-inherited values
	// // inheritedKids.addAll(EnumCodegenUtils.getInheritedValues(getTLModelObj()));
	// return inheritedKids;
	// }
	//
	// /**
	// * @return list of values found in previous versions of this open enum
	// */
	// private List<TLEnumValue> getInheritedValues() {
	// List<TLEnumValue> valueList = new ArrayList<TLEnumValue>();
	// // TLOpenEnumeration tlOE = getTLModelObj();
	// TLAbstractEnumeration oe = getExtension(getTLModelObj());
	// while (oe != null) {
	// valueList.addAll(oe.getValues());
	// if (oe.getExtension() != null)
	// oe = getExtension(oe);
	// else
	// oe = null;
	// }
	// return valueList;
	// }

}

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
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.resources.ActionFacet;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.InheritedResourceMember;
import org.opentravel.schemas.node.resources.ParamGroup;
import org.opentravel.schemas.node.resources.ParentRef;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceChildrenHandler extends StaticChildrenHandler<Node, ResourceNode> {
	@SuppressWarnings("unused")
	private final static Logger LOGGER = LoggerFactory.getLogger(ResourceChildrenHandler.class);

	public ResourceChildrenHandler(final ResourceNode obj) {
		super(obj);
		// initChildren();
	}

	// Since the TL Model and Node model are so different do the mapping here.
	@Override
	public void initChildren() {
		initRunning = true;
		TLResource tlResource = owner.getTLModelObject();
		if (tlResource != null) {
			for (TLResourceParentRef parent : tlResource.getParentRefs())
				new ParentRef(parent);
			for (TLParamGroup tlp : tlResource.getParamGroups())
				new ParamGroup(tlp);
			for (TLAction action : tlResource.getActions())
				new ActionNode(action);
			for (TLActionFacet af : tlResource.getActionFacets())
				new ActionFacet(af);
		}
		initRunning = false;
	}

	@Override
	public void clear() {
		// NO-OP
	}

	// Only used when an error is detected to recover.
	public void reset() {
		children.clear();
		initChildren();
	}

	@Override
	public void initInherited() {
		initRunning = true;
		TLResource tlResource = owner.getTLModelObject();
		if (tlResource.getExtension() != null) {
			inherited = new ArrayList<Node>();
			TLResource base = (TLResource) tlResource.getExtension().getExtendsEntity();
			if (base instanceof TLResource) {
				for (TLParamGroup tlInherited : base.getParamGroups())
					inherited.add(new InheritedResourceMember(tlInherited));
				for (TLActionFacet tlInherited : base.getActionFacets())
					inherited.add(new InheritedResourceMember(tlInherited));
				for (TLAction tlInherited : base.getActions())
					inherited.add(new InheritedResourceMember(tlInherited));
			}
		} else
			inherited = Collections.emptyList();

		initRunning = false;
	}

	/**
	 * Get everything EXCEPT the simple facet
	 */
	@Override
	public List<TLModelElement> getChildren_TL() {
		// assert false; // used by library node
		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		// kids.add(owner.getTLModelObject().getSummaryFacet());
		// kids.add(owner.getTLModelObject().getDetailFacet());
		// kids.add(owner.getTLModelObject().getDetailListFacet());
		// kids.add(owner.getTLModelObject().getSimpleListFacet());
		// kids.addAll(owner.getTLModelObject().getAliases());
		// kids.add(owner.getTLModelObject().getRoleEnumeration());
		return kids;
	}

	protected List<Node> modelTLs(List<TLModelElement> list) {
		assert false; // NOT USED
		List<Node> kids = new ArrayList<Node>();
		for (TLModelElement t : list) {
			ComponentNode fn = NodeFactory.newChild(owner, t);
			fn.setParent(owner);
			kids.add(fn);
		}
		return kids;
	}

}

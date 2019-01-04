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
package org.opentravel.schemas.node.typeProviders;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for <b>all</b> simple type providers.
 * <p>
 * Role is to manage assignments including where used.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class SimpleTypeProviders extends TypeProviders implements SimpleMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTypeProviders.class);

	public SimpleTypeProviders() {
		// For types that have no TL
		whereAssignedHandler = new WhereAssignedHandler(this);
	}

	public SimpleTypeProviders(final TLModelElement obj) {
		super(obj);
		whereAssignedHandler = new WhereAssignedHandler(this);
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;
	}

	@Override
	public boolean isRenameableWhereUsed() {
		return true;
	}

	@Override
	public boolean isAssignableToSimple() {
		return true;
	}

	public boolean isSimpleList() {
		return false;
	}

	@Override
	public LibraryMemberInterface clone(LibraryNode targetLib, String nameSuffix) {
		if (getLibrary() == null || !getLibrary().isEditable()) {
			LOGGER.warn("Could not clone node because library " + getLibrary() + " it is not editable.");
			return null;
		}

		LibraryMemberInterface clone = null;

		// Use the compiler to create a new TL src object.
		TLModelElement newLM = (TLModelElement) cloneTLObj();
		if (newLM != null) {
			clone = NodeFactory.newLibraryMember((LibraryMember) newLM);
			assert clone != null;
			if (nameSuffix != null)
				clone.setName(clone.getName() + nameSuffix);
			for (AliasNode alias : clone.getAliases())
				alias.setName(alias.getName() + nameSuffix);
			targetLib.addMember(clone);
		}
		return clone;
	}

}

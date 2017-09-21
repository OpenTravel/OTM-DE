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

import java.util.List;

import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for most Library Members. Contextual facets extend facets not this.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class LibraryMemberBase extends TypeProviderBase implements LibraryMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryMemberBase.class);

	// public LibraryMemberBase() {
	// }
	//
	public LibraryMemberBase(final TLModelElement obj) {
		super(obj);
	}

	public LibraryMember cloneTL() {
		LibraryElement clone = super.cloneTLObj();
		// TODO - why is choice done here? Why not BO and CFs also?
		if (clone instanceof TLChoiceObject) {
			List<TLContextualFacet> tlCFs = ((TLChoiceObject) clone).getChoiceFacets();
			ComponentNode n;
			for (TLContextualFacet tlcf : tlCFs) {
				n = NodeFactory.newComponent_UnTyped(tlcf);
				getLibrary().addMember(n);
			}
		}
		assert clone instanceof LibraryMember;
		return (LibraryMember) clone;
	}

	@Override
	public LibraryMemberInterface copy(LibraryNode destLib) throws IllegalArgumentException {
		if (destLib == null)
			destLib = getLibrary();

		// Clone the TL object
		LibraryMember tlCopy = cloneTL();

		// Create contextual facet from the copy
		Node copy = NodeFactory.newComponent_UnTyped(tlCopy);
		if (!(copy instanceof LibraryMemberInterface))
			throw new IllegalArgumentException("Unable to copy " + this);
		LibraryMemberInterface lm = (LibraryMemberInterface) copy;

		// Fix any contexts
		((Node) lm).fixContexts();

		destLib.addMember((Node) lm);
		return lm;
	}

	@Override
	public LibraryNode getLibrary() {
		return library;
	}

	@Override
	public void setLibrary(LibraryNode library) {
		this.library = library;
	}
}

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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for <b>most</b> Library Members. Contextual facets extend facets not this.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class LibraryMemberBase extends TypeProviderBase implements LibraryMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryMemberBase.class);

	protected LibraryNode owningLibrary = null;

	// public LibraryMemberBase() {
	// }
	//
	public LibraryMemberBase(final LibraryMember obj) {
		super((TLModelElement) obj);
		owningLibrary = (LibraryNode) Node.GetNode(obj.getOwningLibrary());

		// while it is true that if it is a library member it must have library, sometimes the library is added after
		// the node is created.
		// if (owningLibrary == null)
		// assert this instanceof ImpliedNode;
	}

	@Override
	public Node clone(Node parent, String nameSuffix) {
		if (getLibrary() == null || !getLibrary().isEditable()) {
			LOGGER.warn("Could not clone node because library " + getLibrary() + " it is not editable.");
			return null;
		}

		LibraryMemberInterface clone = null;

		// Use the compiler to create a new TL src object.
		// FIXME - this looks right, but cloneTLObj() doesn't
		TLModelElement newLM = (TLModelElement) cloneTLObj();
		if (newLM != null) {
			clone = NodeFactory.newLibraryMember((LibraryMember) newLM);
			if (nameSuffix != null)
				clone.setName(clone.getName() + nameSuffix);
			for (AliasNode alias : clone.getAliases())
				alias.setName(alias.getName() + nameSuffix);
			getLibrary().addMember(clone);
		}
		return (Node) clone;
	}

	public LibraryMember cloneTL() {
		LibraryElement clone = super.cloneTLObj();
		// TODO - why is choice done here? Why not BO and CFs also?
		if (clone instanceof TLChoiceObject) {
			List<TLContextualFacet> tlCFs = ((TLChoiceObject) clone).getChoiceFacets();
			LibraryMemberInterface n;
			for (TLContextualFacet tlcf : tlCFs) {
				n = NodeFactory.newLibraryMember(tlcf);
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
		LibraryMemberInterface copy = NodeFactory.newLibraryMember(tlCopy);
		if (!(copy instanceof LibraryMemberInterface))
			throw new IllegalArgumentException("Unable to copy " + this);
		LibraryMemberInterface lm = (LibraryMemberInterface) copy;

		// Fix any contexts
		((Node) lm).fixContexts();

		destLib.addMember(lm);
		return lm;
	}

	@Override
	public void deleteTL() {
		AbstractLibrary owningLib = null;
		if (getTLModelObject() instanceof TLLibraryMember)
			owningLib = ((TLLibraryMember) getTLModelObject()).getOwningLibrary();
		if (owningLib != null)
			owningLib.removeNamedMember((LibraryMember) getTLModelObject());
	}

	@Override
	public List<AliasNode> getAliases() {
		List<AliasNode> aliases = new ArrayList<AliasNode>();
		for (Node n : getChildren())
			if (n instanceof AliasNode)
				aliases.add((AliasNode) n);
		return aliases;
	}

	@Override
	public LibraryNode getLibrary() {
		return owningLibrary;
	}

	@Override
	public void setLibrary(LibraryNode library) {
		owningLibrary = library;
	}
}

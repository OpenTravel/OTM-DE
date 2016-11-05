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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.listeners.NodeIdentityListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TL Model Attribute for use with Simple Facets.
 * 
 * The TL Model does not have attributes on simple facets or value with attributes while the GUI model does. This class
 * simulates a TL Model element for use with simple facets.
 */
public class TLnSimpleAttribute extends TLModelElement implements TLEquivalentOwner, TLDocumentationOwner,
		TLExampleOwner {
	private static final Logger LOGGER = LoggerFactory.getLogger(TLnSimpleAttribute.class);

	// the VWA or CoreObject that owns this simple facet.
	private TLModelElement parentObject;

	public TLnSimpleAttribute() {
		parentObject = null;
		throw new IllegalArgumentException("Invalid constructor for TLnSimpleAttribute.");
	}

	/**
	 * NOTE - caller MUST set parent entity since it is not known to the TLSimpleFacet.
	 * 
	 * @param parentEntity
	 */
	public TLnSimpleAttribute(TLModelElement parentEntity) {
		parentObject = parentEntity;
	}

	@Override
	public void addEquivalent(final int index, final TLEquivalent equivalent) {
		final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
		if (equivalentOwner != null) {
			equivalentOwner.addEquivalent(index, equivalent);
		}
	}

	@Override
	public void addEquivalent(final TLEquivalent tle) {
		final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
		if (equivalentOwner != null) {
			equivalentOwner.addEquivalent(tle);
		}
	}

	@Override
	public void addExample(final int index, final TLExample example) {
		final TLExampleOwner exampleOwner = getExampleOwner();
		if (exampleOwner != null) {
			exampleOwner.addExample(index, example);
		}
	}

	@Override
	public void addExample(final TLExample example) {
		final TLExampleOwner exampleOwner = getExampleOwner();
		if (exampleOwner != null) {
			exampleOwner.addExample(example);
		}
	}

	// /////////////////////////////////////////////////////////////////////
	//
	// Listeners - leave listener on this facade for identity but also add
	// non-identity listeners to the parent to catch the tl model events.
	//
	@Override
	public void addListener(ModelElementListener listener) {
		super.addListener(listener);
		if (!(listener instanceof NodeIdentityListener))
			if (parentObject instanceof TLCoreObject)
				((TLCoreObject) parentObject).getSimpleFacet().addListener(listener);
			else
				parentObject.addListener(listener);

	}

	@Override
	public void removeListener(ModelElementListener listener) {
		super.removeListener(listener);
		if (!(listener instanceof NodeIdentityListener))
			if (parentObject instanceof TLCoreObject)
				((TLCoreObject) parentObject).getSimpleFacet().removeListener(listener);
			else
				parentObject.removeListener(listener);
	}

	@Override
	public LibraryElement cloneElement(AbstractLibrary tlLib) {
		TLnSimpleAttribute tlSa = new TLnSimpleAttribute(parentObject);
		return tlSa;
	}

	@Override
	public TLDocumentation getDocumentation() {
		if (parentObject instanceof TLValueWithAttributes) {
			return ((TLValueWithAttributes) parentObject).getDocumentation();
		}
		if (parentObject instanceof TLCoreObject) {
			TLSimpleFacet simpleFacet = ((TLCoreObject) parentObject).getSimpleFacet();
			if (simpleFacet != null)
				return simpleFacet.getDocumentation();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#getEquivalent(java.lang.String)
	 */
	@Override
	public TLEquivalent getEquivalent(final String context) {
		final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
		if (equivalentOwner != null) {
			return equivalentOwner.getEquivalent(context);
		}
		return null;
	}

	@Override
	public List<TLEquivalent> getEquivalents() {
		final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
		if (equivalentOwner != null) {
			return equivalentOwner.getEquivalents();
		}
		return new ArrayList<TLEquivalent>();
	}

	public String getExample() {
		final TLExampleOwner exampleOwner = getExampleOwner();

		if (exampleOwner != null) {
			final List<TLExample> exampleList = exampleOwner.getExamples();
			String example = null;

			if (exampleList.size() > 0) {
				example = exampleList.get(0).getValue();
			}
			return example;
		}

		return "";
	}

	@Override
	public TLExample getExample(final String contextId) {
		final TLExampleOwner exampleOwner = getExampleOwner();
		if (exampleOwner != null) {
			return exampleOwner.getExample(contextId);
		}
		return null;
	}

	@Override
	public List<TLExample> getExamples() {
		final TLExampleOwner exampleOwner = getExampleOwner();
		if (exampleOwner != null) {
			return exampleOwner.getExamples();
		}
		return new ArrayList<TLExample>();
	}

	public String getName() {
		if (parentObject instanceof TLValueWithAttributes) {
			return ((TLValueWithAttributes) parentObject).getLocalName() + "_Value";
		} else if (parentObject instanceof TLCoreObject) {
			return ((TLCoreObject) parentObject).getLocalName() + "_Simple";
		}
		return "Undefined";
	}

	@Override
	public AbstractLibrary getOwningLibrary() {
		if (parentObject instanceof TLValueWithAttributes) {
			return ((TLValueWithAttributes) parentObject).getOwningLibrary();
		} else if (parentObject instanceof TLCoreObject) {
			return ((TLCoreObject) parentObject).getOwningLibrary();
		}
		return null;
	}

	@Override
	public TLModel getOwningModel() {
		return null;
	}

	/**
	 * @return the parentObject
	 */
	public TLModelElement getParentObject() {
		return parentObject;
	}

	public TLEquivalent getTLEquivalent(final int index) {
		if (parentObject instanceof TLValueWithAttributes) {
			return ((TLValueWithAttributes) parentObject).getEquivalents().get(index);
		}
		if (parentObject instanceof TLCoreObject) {
			return ((TLCoreObject) parentObject).getEquivalents().get(index);
		}
		return null;
	}

	public NamedEntity getType() {
		if (parentObject == null)
			throw new IllegalStateException("TLnSimpleAttribute not initialized properly.");

		NamedEntity type = null;
		if (parentObject instanceof TLSimpleFacet)
			type = ((TLSimpleFacet) parentObject).getSimpleType();
		else if (parentObject instanceof TLCoreObject)
			type = ((TLCoreObject) parentObject).getSimpleFacet().getSimpleType();
		else if (parentObject instanceof TLValueWithAttributes) {
			// should never be true
			type = ((TLValueWithAttributes) parentObject).getParentType();
			assert (false);
		}

		return type;
	}

	@Override
	public String getValidationIdentity() {
		return parentObject != null ? parentObject.getValidationIdentity() : "";
	}

	public boolean isMandatory() {
		return true;
	}

	@Override
	public void moveDown(final TLEquivalent equivalent) {

	}

	@Override
	public void moveDown(final TLExample example) {

	}

	@Override
	public void moveUp(final TLEquivalent equivalent) {

	}

	@Override
	public void moveUp(final TLExample example) {

	}

	@Override
	public void removeEquivalent(final TLEquivalent equivalent) {
		final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
		if (equivalentOwner != null) {
			equivalentOwner.removeEquivalent(equivalent);
		}
	}

	@Override
	public void removeExample(final TLExample example) {
		final TLExampleOwner exampleOwner = getExampleOwner();
		if (exampleOwner != null) {
			exampleOwner.removeExample(example);
		}
	}

	@Override
	public void setDocumentation(final TLDocumentation doc) {
		if (parentObject instanceof TLValueWithAttributes) {
			((TLValueWithAttributes) parentObject).setDocumentation(doc);
		}
		if (parentObject instanceof TLCoreObject) {
			TLSimpleFacet simpleFacet = ((TLCoreObject) parentObject).getSimpleFacet();
			if (simpleFacet != null)
				simpleFacet.setDocumentation(doc);
		}
	}

	/**
	 * @param parentObject
	 *            the parentObject to set
	 */
	public void setParentObject(TLModelElement parentObject) {
		this.parentObject = parentObject;
	}

	public void setType(final NamedEntity srcType) {
		if (srcType == null)
			return;

		if (!(srcType instanceof TLAttributeType)) {
			LOGGER.error("Invalid argument: " + srcType.getValidationIdentity());
			// return;
			throw new IllegalArgumentException("Can not set simple attribute type to argument: " + srcType);
		}

		if (parentObject instanceof TLSimpleFacet)
			((TLSimpleFacet) parentObject).setSimpleType(srcType);
		else if (parentObject instanceof TLCoreObject)
			((TLCoreObject) parentObject).getSimpleFacet().setSimpleType(srcType);
		else if (parentObject instanceof TLValueWithAttributes) {
			// should never happen
			((TLValueWithAttributes) parentObject).setParentType((TLAttributeType) srcType);
			assert (false);
		}

	}

	@Override
	public void sortEquivalents(final Comparator<TLEquivalent> comparator) {
		final TLEquivalentOwner equivalentOwner = getEquivalentOwner();
		if (equivalentOwner != null) {
			equivalentOwner.sortEquivalents(comparator);
		}
	}

	@Override
	public void sortExamples(final Comparator<TLExample> comparator) {
		final TLExampleOwner exampleOwner = getExampleOwner();
		if (exampleOwner != null) {
			exampleOwner.sortExamples(comparator);
		}
	}

	private TLEquivalentOwner getEquivalentOwner() {
		TLEquivalentOwner equivalentOwner = null;

		if (parentObject instanceof TLValueWithAttributes) {
			equivalentOwner = (TLEquivalentOwner) parentObject;
		}
		if (parentObject instanceof TLCoreObject) {
			equivalentOwner = ((TLCoreObject) parentObject).getSimpleFacet();
		}
		return equivalentOwner;
	}

	private TLExampleOwner getExampleOwner() {
		TLExampleOwner exampleOwner = null;

		if (parentObject instanceof TLValueWithAttributes) {
			exampleOwner = (TLExampleOwner) parentObject;
		}
		if (parentObject instanceof TLCoreObject) {
			exampleOwner = ((TLCoreObject) parentObject).getSimpleFacet();
		}
		return exampleOwner;
	}

}

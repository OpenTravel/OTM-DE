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
package org.opentravel.schemas.wizards.validators;

import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.wizards.SimpleNameWizard;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ContextChangeValidator implements FormValidator {

	private final SimpleNameWizard wizard;
	private final TLContextReferrer referrer;

	public ContextChangeValidator(final TLContextReferrer referrer, final SimpleNameWizard wizard) {
		this.wizard = wizard;
		this.referrer = referrer;
	}

	@Override
	public void validate() throws ValidationException {
		final String context = wizard.getText();
		// final TLContext context = ((TLLibrary)
		// referrer.getOwningLibrary()).getContext(appContext);
		final String contextId = context != null ? context : "";
		if (!canBeChanged(referrer, contextId)) {
			throw new ValidationException(Messages.getString("error.changeContext"));
		}
	}

	private boolean canBeChanged(final TLContextReferrer referrer, final String context) {
		if (referrer.getContext() == null || referrer.getContext().equals(context)) {
			return true;
		}
		if (referrer instanceof TLEquivalent) {
			final TLEquivalent eq = ((TLEquivalent) referrer).getOwningEntity().getEquivalent(context);
			return eq == null;
		}
		if (referrer instanceof TLExample) {
			final TLExample ex = ((TLExample) referrer).getOwningEntity().getExample(context);
			return ex == null;
		}
		if (referrer instanceof TLAdditionalDocumentationItem) {
			final TLAdditionalDocumentationItem doc = ((TLAdditionalDocumentationItem) referrer)
					.getOwningDocumentation().getOtherDoc(context);
			return doc == null;
		}
		if (referrer instanceof TLFacet) {
			final TLFacet facet = (TLFacet) referrer;
			final TLFacetOwner object = facet.getOwningEntity();
			if (object instanceof TLBusinessObject) {
				final TLBusinessObject tlBo = (TLBusinessObject) object;
				if (facet.getFacetType().equals(TLFacetType.CUSTOM)) {
					return tlBo.getCustomFacet(context) == null;
				} else if (facet.getFacetType().equals(TLFacetType.QUERY)) {
					return tlBo.getQueryFacet(context) == null;
				}
			}
		}
		return true;
	}

	@Override
	public void validate(Node selectedNode) throws ValidationException {
		// TODO Auto-generated method stub

	}

}

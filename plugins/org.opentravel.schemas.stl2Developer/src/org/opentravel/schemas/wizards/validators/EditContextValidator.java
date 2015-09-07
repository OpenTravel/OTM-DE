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

import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.wizards.EditContextWizard;

/**
 * @author Agnieszka Janowska
 * 
 */
public class EditContextValidator implements FormValidator {

	private final EditContextWizard wizard;
	private final TLLibrary library;

	public EditContextValidator(final TLLibrary library, final EditContextWizard wizard) {
		this.library = library;
		this.wizard = wizard;
	}

	@Override
	public void validate() throws ValidationException {
		final TLContext contextObject = wizard.getContext();
		final String context = contextObject.getContextId();
		final String appCtx = contextObject.getApplicationContext();
		if (appCtx == null || appCtx.isEmpty()) {
			throw new ValidationException(Messages.getString("error.newContextApplicationEmpty"));
		}
		if (context == null || context.isEmpty()) {
			throw new ValidationException(Messages.getString("error.newContextIdEmpty"));
		}
		final TLContext origContextById = library.getContext(context);
		final TLContext origContextByApp = library.getContextByApplicationContext(appCtx);
		if (origContextById != null && origContextByApp != null && !origContextByApp.equals(origContextById)) {
			throw new ValidationException(Messages.getString("error.editContextNotMatching"));
		}
	}

	@Override
	public void validate(Node selectedNode) throws ValidationException {
		// TODO Auto-generated method stub

	}

}

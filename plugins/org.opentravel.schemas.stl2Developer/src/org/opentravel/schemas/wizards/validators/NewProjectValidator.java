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

import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewProjectWizardPage;

/**
 * @author Dave Hollander
 * 
 */
public class NewProjectValidator implements FormValidator {

	NewProjectWizardPage page;

	public NewProjectValidator() {
	}

	public void setPage(NewProjectWizardPage page) {
		this.page = page;
	}

	@Override
	public void validate() throws ValidationException {
		if (page == null)
			return;
		String nsID = page.getNamespace();

		// Test to see if the selected ns/ID is already in use.
		ProjectController pc = OtmRegistry.getMainController().getProjectController();

		if (nsID == null || nsID.isEmpty())
			throw new ValidationException(Messages.getString("wizard.newProject.error.missingNS"));

		if (nsID.equals(pc.getDefaultUnmanagedNS()))
			throw new ValidationException(Messages.getString("wizard.newProject.error.defaultNS"));

		for (String gns : pc.getOpenGovernedNamespaces()) {
			if (nsID.equals(gns)) {
				throw new ValidationException(Messages.getString("wizard.newProject.error.nsGoverned"));
			}
		}
	}

	@Override
	public void validate(Node selectedNode) throws ValidationException {
		// TODO Auto-generated method stub

	}

}

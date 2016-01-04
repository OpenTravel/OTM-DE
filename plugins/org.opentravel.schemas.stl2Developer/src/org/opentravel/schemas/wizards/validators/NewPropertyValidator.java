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

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.wizards.NewPropertiesWizard;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewPropertyValidator implements FormValidator {

	private final Node parentNode;
	private final NewPropertiesWizard wizard;

	public NewPropertyValidator(final Node parent, final NewPropertiesWizard wizard) {
		parentNode = parent;
		this.wizard = wizard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.wizards.FormValidator#validate()
	 */
	@Override
	public void validate() throws ValidationException {
		if (wizard == null)
			return;
		validate(wizard.getSelectedNode());
	}

	@Override
	public void validate(Node selectedNode) throws ValidationException {
		if (selectedNode == null || !(selectedNode instanceof PropertyNode))
			throw new ValidationException(Messages.getString("error.newPropertyType"));

		PropertyNode n = (PropertyNode) selectedNode;

		if (n.getName() == null || n.getName().isEmpty()) {
			throw new ValidationException(Messages.getString("error.newPropertyName"));
		}
		if (n.getPropertyType() == null || n.getPropertyType() == PropertyNodeType.UNKNOWN) {
			throw new ValidationException(Messages.getString("error.newPropertyType") + " (" + n.getName() + ")");
		}

		// Will not be unique because is already added to parent.
		// if (!parentNode.isUnique(n)) {
		// throw new ValidationException(n.getName() + ": " + Messages.getString("error.newProperty"));
		// }
	}

}

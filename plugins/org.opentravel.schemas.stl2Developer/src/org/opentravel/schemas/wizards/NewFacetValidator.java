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
package org.opentravel.schemas.wizards;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.EditNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Messages;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewFacetValidator implements FormValidator {

	private final Node componentNode;
	private final NewFacetWizard wizard;
	private final TLFacetType facetType;

	public NewFacetValidator(final Node componentNode, final TLFacetType facetType, final NewFacetWizard wizard) {
		this.componentNode = componentNode;
		this.facetType = facetType;
		this.wizard = wizard;
	}

	@Override
	public void validate() throws ValidationException {
		final EditNode n = new EditNode(wizard.getName());
		// TODO - why does validate have side effect?
		n.setLibrary(componentNode.getLibrary());
		if (!componentNode.isFacetUnique(n)) {
			throw new ValidationException(Messages.getString("error.newFacet"));
		}
		if (TLFacetType.CUSTOM.equals(facetType) && isEmpty(wizard.getName())) {
			throw new ValidationException(Messages.getString("error.newFacet.custom.empty"));
		}
	}

	private boolean isEmpty(String name) {
		if (name != null)
			return name.isEmpty();
		return true;
	}

}

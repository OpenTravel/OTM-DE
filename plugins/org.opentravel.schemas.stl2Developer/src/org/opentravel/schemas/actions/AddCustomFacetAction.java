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
package org.opentravel.schemas.actions;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.commands.ContextualFacetHandler;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

/**
 * Front end for ContextualFacetHandler()
 * 
 * @see ContextualFacetHandler#addContextualFacet(ChoiceObjectNode)
 * 
 * @author Dave Hollander
 * 
 */
public class AddCustomFacetAction extends OtmAbstractAction {
	private static StringProperties propsDefault = new ExternalizedStringProperties("action.addCustom");

	// // TODO - what is this? It is not in the addQueryFacetAction
	// OtmAbstractHandler handler = new OtmAbstractHandler() {
	// @Override
	// public Object execute(ExecutionEvent event) throws ExecutionException {
	// return null;
	// }
	// };

	/**
	 *
	 */
	public AddCustomFacetAction() {
		super(propsDefault);
	}

	public AddCustomFacetAction(final StringProperties props) {
		super(props);
	}

	@Override
	public void run() {
		LibraryMemberInterface current = getOwnerOfNavigatorSelection();
		if (current instanceof BusinessObjectNode)
			new ContextualFacetHandler().addContextualFacet((BusinessObjectNode) current, TLFacetType.CUSTOM);
	}

	@Override
	public boolean isEnabled() {
		// Unmanaged or in the most current (head) library in version chain.
		LibraryMemberInterface n = getOwnerOfNavigatorSelection();
		return n instanceof BusinessObjectNode ? n.isEditable_newToChain() : false;
	}

}

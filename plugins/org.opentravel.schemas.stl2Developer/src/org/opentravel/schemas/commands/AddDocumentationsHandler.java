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
/**
 * 
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.DocumentationHandler;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.SetDocumentationWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for adding documentation items to a set of nodes. Uses setDocumentation wizard to get the documentation text.
 * 
 * @author Dave Hollander
 * 
 */
public class AddDocumentationsHandler extends OtmAbstractHandler {
	public static String COMMAND_ID = "org.opentravel.schemas.commands.addDocumentations";
	private static final Logger LOGGER = LoggerFactory.getLogger(AddDocumentationsHandler.class);

	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {

		SetDocumentationWizard wiz = new SetDocumentationWizard();
		wiz.run(OtmRegistry.getActiveShell());
		String text = wiz.getDocText();
		SetDocumentationWizard.DocTypes docType = wiz.getDocType();

		for (Node node : mc.getSelectedNodes_NavigatorView()) {
			saveDoc(node, docType, text);
			mc.postStatus("Adding Documentation to " + node);
			// LOGGER.debug("Adding Documentation to "+node+" doc = "+wiz.getDocText());
		}
		mc.refresh();
		return null;
	}

	private void saveDoc(Node n, SetDocumentationWizard.DocTypes type, String text) {
		if (n == null || type == null || text.isEmpty())
			return;
		DocumentationHandler dh = n.getDocHandler();
		if (dh == null)
			return;

		switch (type) {
		case Description:
			dh.addDescription(text);
			break;
		case Deprecation:
			dh.addDeprecation(text);
			break;
		case MoreInformation:
			dh.addMoreInfo(text);
			break;
		case ReferenceLink:
			dh.addReference(text);
			break;
		case Implementer:
			dh.addImplementer(text);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.commands.OtmHandler#getID()
	 */
	@Override
	public String getID() {
		return COMMAND_ID;
	}

}

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

import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.SetDocumentationWizard;

/**
 * Manage a library in a repository.
 * 
 * @author Dave Hollander
 * 
 */
public class CommitLibraryAction extends OtmAbstractAction {
	private static StringProperties propDefault = new ExternalizedStringProperties("action.library.commit");

	public CommitLibraryAction() {
		super(propDefault);
	}

	public CommitLibraryAction(final StringProperties props) {
		super(props);
	}

	@Override
	public void run() {
		for (LibraryNode ln : mc.getSelectedLibraries()) {
			SetDocumentationWizard wizard = new SetDocumentationWizard(ln);
			wizard.run(OtmRegistry.getActiveShell());
			if (!wizard.wasCanceled()) {
				String remark = wizard.getDocText();
				mc.getRepositoryController().commit(ln, remark);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		INode n = getMainController().getCurrentNode_NavigatorView();
		if (n instanceof LibraryNavNode)
			n = (INode) ((LibraryNavNode) n).getThisLib();
		if (n == null || n.getLibrary() == null)
			n = null;
		else if (n instanceof ComponentNode)
			n = n.getLibrary().getChain();

		if (n == null || !(n instanceof LibraryChainNode))
			return false;

		if (((LibraryChainNode) n).getHead() == null || ((LibraryChainNode) n).getHead().getProjectItem() == null
				|| ((LibraryChainNode) n).getHead().getProjectItem().getState() == null)
			return false;

		return ((LibraryChainNode) n).getHead().getProjectItem().getState().equals(RepositoryItemState.MANAGED_WIP);
	}

}

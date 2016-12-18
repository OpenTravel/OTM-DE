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

import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

/**
 * Finalize action - finalize current version of a library.
 * 
 * @author Dave Hollander
 * 
 */
public class FinalizeLibraryAction extends OtmAbstractAction {
	private static StringProperties propDefault = new ExternalizedStringProperties("action.library.finalize");

	public FinalizeLibraryAction() {
		super(propDefault);
	}

	public FinalizeLibraryAction(final StringProperties props) {
		super(props);
	}

	@Override
	public void run() {
		for (LibraryNode ln : mc.getSelectedLibraries()) {
			mc.getRepositoryController().markFinal(ln);
		}
	}

	@Override
	public boolean isEnabled() {
		INode n = getMainController().getCurrentNode_NavigatorView();

		// Find the effective library
		if (n instanceof LibraryNavNode)
			n = n.getLibrary();
		if (n instanceof ComponentNode)
			n = n.getLibrary();
		if (n instanceof LibraryChainNode)
			n = ((LibraryChainNode) n).getLibrary();

		if (n instanceof LibraryNode) {
			// If it is final then return false.
			TLLibraryStatus status = ((LibraryNode) n).getStatus();
			if (((LibraryNode) n).getStatus().equals(TLLibraryStatus.FINAL))
				return false;

			// Check repo state
			RepositoryItemState state = ((LibraryNode) n).getProjectItem().getState();
			switch (state) {
			case MANAGED_LOCKED:
			case MANAGED_UNLOCKED:
			case MANAGED_WIP:
				return true;

			case UNMANAGED:
			case BUILT_IN:
			default:
				return false;
			}
		}
		return false;
	}

}

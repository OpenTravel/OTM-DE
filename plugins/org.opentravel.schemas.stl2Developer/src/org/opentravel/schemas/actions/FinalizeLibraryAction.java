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
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finalize action - finalize current version of a library.
 * 
 * @author Dave Hollander
 * 
 *         Obsolete - see {@link LifeCycleAction}
 */
public class FinalizeLibraryAction extends OtmAbstractAction {
	private static StringProperties propDefault = new ExternalizedStringProperties("action.library.finalize");
	private static final Logger LOGGER = LoggerFactory.getLogger(FinalizeLibraryAction.class);

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

		if (n.getLibrary() instanceof LibraryNode) {
			LibraryNode ln = n.getLibrary();
			if (ln.getProjectItem() == null)
				return false;

			// RepositoryItemState state = ln.getProjectItem().getState();
			// TLLibraryStatus status = ln.getProjectItem().getStatus();
			// LOGGER.debug(ln + " status = " + status + " state = " + state + " next = "
			// + ln.getStatus().nextStatus());
			// Is too late, shows up in next call
			// this.setText(ln.getStatus().nextStatus().toString());

			// Don't allow lock unless library is in a project with managing namespace
			if (!ln.isInProjectNS())
				return false;
			// If it is final then return false.
			if (ln.getStatus().equals(TLLibraryStatus.FINAL))
				return false;
			// if (!status.nextStatus().equals(TLLibraryStatus.FINAL))
			// return false;

			// Check repo state
			switch (ln.getProjectItem().getState()) {
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

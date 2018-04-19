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

import org.eclipse.jface.resource.ImageDescriptor;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.StringProperties;

/**
 * Manage a library in a repository.
 * 
 * @author Dave Hollander
 * 
 */
public class LockLibraryAction extends OtmAbstractAction {
	private static StringProperties propDefault = new ExternalizedStringProperties("action.library.lock");

	public LockLibraryAction() {
		super(propDefault);
	}

	public LockLibraryAction(final StringProperties props) {
		super(props);
	}

	@Override
	public void run() {
		mc.getRepositoryController().lock();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Images.getImageRegistry().getDescriptor(Images.Lock);
	}

	// enable when Work-in-progress item.
	@Override
	public boolean isEnabled(Node node) {
		Node n = getMainController().getCurrentNode_NavigatorView();
		if (n == null || n.getLibrary() == null)
			return false;

		// Get effective library
		LibraryNode ln = n.getLibrary();
		// Components can be in older versions
		if (ln.getChain() != null)
			ln = ln.getChain().getHead();

		if (ln.getProjectItem() == null || ln.getStatus() == null || ln.getEditStatus() == null)
			return false;

		// RepositoryItemState state = ln.getProjectItem().getState();
		// NodeEditStatus status = n.getEditStatus();
		// TLLibraryStatus status2 = ln.getStatus();
		// Don't allow lock unless library is in a project with managing namespace
		if (!ln.isInProjectNS())
			return false;
		// Don't allow library to be locked if it is already locked.
		if (ln.getProjectItem().getLockedByUser() != null)
			return false;
		if (ln.getStatus().equals(TLLibraryStatus.FINAL))
			return false;
		return n.getEditStatus().equals(NodeEditStatus.MANAGED_READONLY);
	}
}

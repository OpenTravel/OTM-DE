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
import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

/**
 * Create a major/minor/patch version of the library
 *
 * @author Dave Hollander
 * 
 */
public class VersionAction extends OtmAbstractAction {
	private static StringProperties propsDefault = new ExternalizedStringProperties("action.library.version.major");
	private static StringProperties propsMajor = new ExternalizedStringProperties("action.library.version.major");
	private static StringProperties propsMinor = new ExternalizedStringProperties("action.library.version.minor");
	private static StringProperties propsPatch = new ExternalizedStringProperties("action.library.version.patch");
	private VersionType versionType = null;

	public enum VersionType {
		MAJOR, MINOR, PATCH
	}

	public VersionAction(VersionType type) {
		super(propsDefault);
		versionType = type;
		// Override the default properties
		switch (versionType) {
		case MAJOR:
			initialize(propsMajor);
			break;
		case MINOR:
			initialize(propsMinor);
			break;
		case PATCH:
			initialize(propsPatch);
			break;
		}
	}

	// public VersionAction(final StringProperties props) {
	// super(props);
	// }

	/**
	 * run repository controller create major version
	 */
	@Override
	public void run() {
		for (Node node : mc.getSelectedNodes_NavigatorView()) {
			RepositoryController rc = mc.getRepositoryController();
			node = node.getLibrary();
			if (!(node.getLibrary() instanceof LibraryNode))
				return;
			LibraryNode ln = node.getLibrary();
			if (ln.getTLModelObject().getOwningModel() == null)
				return;

			switch (versionType) {
			case MAJOR:
				mc.postStatus("Major Version " + ln);
				rc.createMajorVersion(ln);
				break;
			case MINOR:
				mc.postStatus("Minor Version " + node);
				rc.createMinorVersion(ln);
				break;
			case PATCH:
				mc.postStatus("Patch Version " + node);
				rc.createPatchVersion(ln);
				break;
			}
		}
		mc.refresh();
	}

	@Override
	public boolean isEnabled() {
		LibraryNode ln = null;
		Node n = mc.getSelectedNode_NavigatorView();
		if (n != null)
			ln = n.getLibrary();
		// Don't allow lock unless library is in a project with managing namespace
		if (ln == null || !ln.isInProjectNS())
			return false;
		if (!ln.getStatus().equals(TLLibraryStatus.FINAL))
			return false;
		return ln.isManaged();
	}
}

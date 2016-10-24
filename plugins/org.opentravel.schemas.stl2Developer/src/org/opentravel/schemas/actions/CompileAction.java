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

import java.util.List;

import org.opentravel.schemas.navigation.GlobalSelectionProvider;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;

/**
 * UNUSED (10/15/2016 dmh)
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public class CompileAction extends AbstractGlobalSelectionAction {

	public static final String ID = "action.compile";

	public CompileAction() {
		super(ID, GlobalSelectionProvider.NAVIGATION_VIEW);
		new ExternalizedStringProperties(getId()).initializeAction(this);
	}

	@Override
	public void run() {
		// MainController mc = OtmRegistry.getMainController();
		// Node cur = getSourceValue().get(0);
		// if (cur == null || cur instanceof ModelNode)
		// // FIXME - is this still needed? If not, remove and remove getFile from modelNode
		// mc.getModelController().compileModel(mc.getModelNode());
		// else {
		// if (!(cur instanceof ProjectNode))
		// cur = cur.getLibrary().getProject();
		// mc.getModelController().compileModel((ProjectNode) cur);
		// }
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Node> getSourceValue() {
		return (List<Node>) super.getSourceValue();
	}

	@Override
	protected boolean isEnabled(Object object) {
		return getSourceValue().size() == 1;
	}

}

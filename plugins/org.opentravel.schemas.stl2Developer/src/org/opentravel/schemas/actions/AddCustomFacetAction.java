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

import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

/**
 * @author Agnieszka Janowska
 * 
 */
public class AddCustomFacetAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.addCustom");

    /**
	 *
	 */
    public AddCustomFacetAction() {
        super(propsDefault);
    }

    public AddCustomFacetAction(final StringProperties props) {
        super(props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        mc.addCustomFacet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        // Unmanged or in the most current (head) library in version chain.
        Node n = mc.getCurrentNode_NavigatorView().getOwningComponent();
        return n instanceof BusinessObjectNode ? n.isEditable() && n.isNewToChain() : false;
    }

}

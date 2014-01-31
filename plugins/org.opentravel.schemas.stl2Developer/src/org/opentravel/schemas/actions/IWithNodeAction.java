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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.opentravel.schemas.node.Node;

/**
 * Implementing this action will allow action to control state based on passed node. Base class has
 * to make sure that after calling setCurrentNode() will fire notification event for
 * {@link IAction#ENABLED} property.
 * 
 * It is possible to reuse {@link AbstractWithNodeAction} class as a minimal requirement to handle
 * correctly notification after setting node.
 * 
 * @author Pawel Jedruch
 */
public interface IWithNodeAction {

    /**
     * Client has to make sure that after this action the notification event will be fired.
     * 
     * @param curNode
     *            - pass node on which action will decide if is enabled or not.
     */
    public void setCurrentNode(Node curNode);

    /**
     * Example implementation that can be extended by action in order to update state after changing
     * node.
     * 
     */
    abstract class AbstractWithNodeAction extends Action implements IWithNodeAction {

        public AbstractWithNodeAction() {
            super();
        }

        public AbstractWithNodeAction(String text, ImageDescriptor image) {
            super(text, image);
        }

        public AbstractWithNodeAction(String text, int style) {
            super(text, style);
        }

        public AbstractWithNodeAction(String text) {
            super(text);
        }

        protected Node currentNode;

        @Override
        public boolean isEnabled() {
            return isEnabled(currentNode);
        }

        public boolean isEnabled(Node currentNode) {
            return super.isEnabled();
        }

        @Override
        public final void setCurrentNode(Node curNode) {
            boolean wasEnabled = isEnabled();
            currentNode = curNode;
            boolean enabled = isEnabled();
            firePropertyChange(ENABLED, wasEnabled, enabled);
        }
    }

}

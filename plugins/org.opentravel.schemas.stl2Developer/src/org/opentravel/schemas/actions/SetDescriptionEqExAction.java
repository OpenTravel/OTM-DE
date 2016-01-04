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

import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.commands.AddNodeHandler2;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a prototype for migrating actions out of the OtmActions.
 * 
 * This action handler is for descriptions, equivalents and examples. This data is part of all nodes and TL objects.
 * Other types of descriptions need to be added before being set. See AddDocItemAction().
 * 
 * @author Dave Hollander
 * 
 */
public class SetDescriptionEqExAction extends OtmAbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(SetDescriptionEqExAction.class);

	private final static StringProperties propsDefault = new ExternalizedStringProperties("action.setName");

	public class SetDescriptionEvent {
		public Node owner;
		public String value;
		public DescriptionType type;

		public SetDescriptionEvent(Node owner, String value, DescriptionType type) {
			this.owner = owner;
			this.value = value;
			this.type = type;
		}
	}

	public enum DescriptionType {
		DESCRIPTION, EXAMPLE, EQUIVALENT
	}

	public SetDescriptionEqExAction(final MainWindow mainWindow) {
		super(mainWindow, propsDefault);
	}

	public SetDescriptionEqExAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	@Override
	public void runWithEvent(Event event) {
		Node owner;
		String value;
		if (event.data instanceof SetDescriptionEvent) {
			owner = ((SetDescriptionEvent) event.data).owner;
			value = ((SetDescriptionEvent) event.data).value;

			AddNodeHandler2 handler = new AddNodeHandler2(); // version creator is in abstract command handler
			if (!owner.isEditable_newToChain()) {
				if (owner.getChain() != null) {
					// owner = handler.createVersionExtension(owner);
					// if (owner == null)
					DialogUserNotifier.openWarning("Warning", "Could not create patch version of " + owner
							+ ". Not Implemented.");
					return;
				}
			}

			switch (((SetDescriptionEvent) event.data).type) {
			case DESCRIPTION:
				owner.setDescription(value);
				break;
			case EXAMPLE:
				if (owner instanceof PropertyNode)
					((PropertyNode) owner).setExample(value);
				else if (owner instanceof SimpleTypeNode)
					((SimpleTypeNode) owner).setExample(value);
				break;
			case EQUIVALENT:
				if (owner instanceof PropertyNode)
					((PropertyNode) owner).setEquivalent(value);
				else if (owner instanceof SimpleTypeNode)
					((SimpleTypeNode) owner).setEquivalent(value);
				break;
			default:
				break;

			}
		}
	}
}

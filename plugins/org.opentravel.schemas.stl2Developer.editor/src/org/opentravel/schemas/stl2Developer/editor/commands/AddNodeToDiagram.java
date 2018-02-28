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
package org.opentravel.schemas.stl2Developer.editor.commands;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class AddNodeToDiagram extends Command {

	private Node newNode;
	private Diagram diagram;
	private Point location;

	public AddNodeToDiagram(Node newObject, Diagram model, Point location) {
		this.newNode = newObject;
		this.diagram = model;
		this.location = location;
	}

	@Override
	public void execute() {
		diagram.addChild(UINode.getOwner(newNode), location);
	}

	@Override
	public boolean canExecute() {
		return newNode != null && newNode.getOwningComponent() != null && newNode instanceof ComponentNode;
	}
}

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
package org.opentravel.schemas.stl2Developer.editor.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class UnsupportedNodeEditPart extends GenericEditPart<Node> {

	public UnsupportedNodeEditPart(UINode model) {
		super(model);
	}

	@Override
	protected IFigure createFigure() {
		return new UnsupporetedFigure();
	}

	public class UnsupporetedFigure extends RectangleFigure {

		public UnsupporetedFigure() {
			ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
			layout.setStretchMinorAxis(false);
			layout.setSpacing(2);
			setLayoutManager(layout);
			setPreferredSize(100, 100);
			Node node = getNodeModel();
			if (node instanceof ResourceMemberInterface)
				add(new Label(node.getComponentType() + ":" + node.getName()));
			else
				add(new Label("???: " + getNodeModel().getName()));
		}

	}

}

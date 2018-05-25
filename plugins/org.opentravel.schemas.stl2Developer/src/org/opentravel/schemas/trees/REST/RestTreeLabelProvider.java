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
package org.opentravel.schemas.trees.REST;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.properties.Fonts;
import org.opentravel.schemas.stl2developer.OtmRegistry;

/**
 * UNUSED
 * 
 * @see RestTreeStyledLabelProvider
 * 
 */
public class RestTreeLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {

	@Override
	public String getText(final Object element) {
		if (element instanceof ResourceMemberInterface) {
			return ((ResourceMemberInterface) element).getLabel();
		}
		return "Unknown object type";
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof ResourceMemberInterface)
			return ((ResourceMemberInterface) element).getImage();
		return null;
	}

	@Override
	public Font getFont(Object element) {
		if (element instanceof Node) {
			Node node = (Node) element;
			if (!node.isEditable())
				return Fonts.getFontRegistry().get(Fonts.readOnlyItem);
			else if (node.isInherited())
				return Fonts.getFontRegistry().get(Fonts.inheritedItem);
			// return Fonts.getFontRegistry().get(Fonts.defaultContext); // BOLD
		}
		return null;
	}

	@Override
	public Color getForeground(Object element) {
		Color color = null;
		if (element instanceof Node)
			if (!((Node) element).isEditable())
				color = OtmRegistry.getMainWindow().getColorProvider().getColor(SWT.COLOR_DARK_GRAY);
		return color;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}

}

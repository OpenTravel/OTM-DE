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
package org.opentravel.schemas.trees.library;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.properties.Fonts;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryTreeLabelProvider extends LabelProvider implements IFontProvider, IColorProvider,
		IStyledLabelProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTreeLabelProvider.class);

	@Override
	public String getText(final Object element) {
		if (element instanceof AbstractContextualFacet)
			return ((AbstractContextualFacet) element).getName();
		if (element instanceof Node)
			return ((Node) element).getNavigatorName();
		return "Unknown object type.";
	}

	@Override
	public Image getImage(final Object element) {
		Image image;
		image = ((Node) element).getImage();
		return image;
	}

	/**
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	@Override
	public Font getFont(Object element) {
		Font font = null;

		if (element instanceof Node) {
			final Node n = (Node) element;
			if (!n.isEditable())
				font = Fonts.getFontRegistry().get(Fonts.readOnlyItem);
			else if (n instanceof InheritedInterface)
				// else if (n.isInherited() || NodeUtils.checker(n).isInheritedFacet().get())
				font = Fonts.getFontRegistry().get(Fonts.inheritedItem);
			// is this inherited from an earlier version?
			else if (n.getChain() != null && n.getLibrary() != n.getChain().getHead())
				font = Fonts.getFontRegistry().get(Fonts.inheritedItem);
		}
		return font;
	}

	/**
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	@Override
	public Color getForeground(Object element) {
		Color color = null;

		if (element instanceof Node) {
			final Node n = (Node) element;
			if (n instanceof ContributedFacetNode)
				color = OtmRegistry.getMainWindow().getColorProvider().getColor(SWT.COLOR_DARK_RED);
			else if (n.isDeprecated())
				color = OtmRegistry.getMainWindow().getColorProvider().getColor(SWT.COLOR_DARK_MAGENTA);
			else if (n instanceof InheritedInterface)
				color = OtmRegistry.getMainWindow().getColorProvider().getColor(SWT.COLOR_DARK_BLUE);
			else if (!n.isEditable())
				color = OtmRegistry.getMainWindow().getColorProvider().getColor(SWT.COLOR_DARK_GRAY);
			// editable is also indicated by font style, so set other colors first.
			else if (n.getChain() != null && n.getLibrary() != n.getChain().getHead())
				color = OtmRegistry.getMainWindow().getColorProvider().getColor(SWT.COLOR_DARK_BLUE);
		}
		return color;
	}

	/**
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	@Override
	public Color getBackground(Object element) {
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString(getText(element));
	}

}

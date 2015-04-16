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
package org.opentravel.schemas.trees.context;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemas.node.ContextNode;
import org.opentravel.schemas.properties.Fonts;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.ContextsView;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ContextTreeLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {

	@Override
	public String getText(final Object element) {
		if (element instanceof ContextNode) {
			return ((ContextNode) element).getLabel();
		}
		return "Unknown object type";
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof ContextNode) {
			return ((ContextNode) element).getImage();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	@Override
	public Font getFont(Object element) {
		if (element instanceof ContextNode) {
			ContextNode cn = (ContextNode) element;
			ContextsView view = OtmRegistry.getContextsView();
			if (view != null) {
				if (!cn.getLibraryNode().isEditable())
					return Fonts.getFontRegistry().get(Fonts.readOnlyItem);

				TLContext tlCtx = ((ContextNode) element).getModelObject();
				TLContext defaultCtx = null;
				if (tlCtx != null) {
					if (tlCtx.getOwningLibrary() != null)
						defaultCtx = view.getContextController().getDefaultContext(tlCtx.getOwningLibrary());
					if (defaultCtx != null && defaultCtx.equals(tlCtx)) {
						return Fonts.getFontRegistry().get(Fonts.defaultContext);
					}
				}
			}
		}
		return null;
	}

	@Override
	public Color getForeground(Object element) {
		Color color = null;
		if (element instanceof ContextNode) {
			final ContextNode n = (ContextNode) element;
			if (!n.getLibraryNode().isEditable())
				color = OtmRegistry.getMainWindow().getColorProvider().getColor(SWT.COLOR_DARK_GRAY);
		}
		return color;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}

}

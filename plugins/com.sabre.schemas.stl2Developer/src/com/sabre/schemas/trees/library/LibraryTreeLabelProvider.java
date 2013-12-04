/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.trees.library;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.ImpliedNode;
import com.sabre.schemas.node.ImpliedNodeType;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.controllers.NodeUtils;
import com.sabre.schemas.properties.Fonts;
import com.sabre.schemas.stl2developer.OtmRegistry;

public class LibraryTreeLabelProvider extends LabelProvider implements IFontProvider,
        IColorProvider, IStyledLabelProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTreeLabelProvider.class);

    @Override
    public String getText(final Object element) {
        String label = "Unknown object type.";
        if (element instanceof INode) {
            final Node n = (Node) element;
            label = n.getLabel();

            // TODO - migrate this into node model.
            if (n.isLocal())
                label = n.getName() + " [local] ";

            // Add the extended label
            // TODO - use IDecorator to add counts. IFontDecorator,ILabelDecorator
            if (n instanceof ImpliedNode
                    && ((ImpliedNode) n).getImpliedType().equals(ImpliedNodeType.Duplicate))
                label = label + " (" + n.getChildren().size() + ")";
            else if (n.isComponent() && ((ComponentNode) n).isTypeProvider()) {
                ComponentNode cn = (ComponentNode) n;
                label = label + " (" + cn.getTypeUsersCount() + ")";
            }
        }
        return label;
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
            else if (n.isInheritedProperty() || NodeUtils.checker(n).isInheritedFacet().get()) {
                font = Fonts.getFontRegistry().get(Fonts.inheritedItem);
            }
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
            if (!n.isEditable())
                color = OtmRegistry.getMainWindow().getColorProvider()
                        .getColor(SWT.COLOR_DARK_GRAY);
            else if (n.isInheritedProperty() || NodeUtils.checker(n).isInheritedFacet().get()) {
                color = OtmRegistry.getMainWindow().getColorProvider()
                        .getColor(SWT.COLOR_DARK_BLUE);
            } else if (n.isDeprecated()) {
                color = OtmRegistry.getMainWindow().getColorProvider()
                        .getColor(SWT.COLOR_DARK_MAGENTA);
            }
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

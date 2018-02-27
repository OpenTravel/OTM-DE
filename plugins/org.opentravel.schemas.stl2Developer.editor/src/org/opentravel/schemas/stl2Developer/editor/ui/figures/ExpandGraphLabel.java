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
package org.opentravel.schemas.stl2Developer.editor.ui.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ExpandGraphLabel extends Figure implements ActionListener {

	private Expander expander = null;

	private List<IExpandListener> listeners = new ArrayList<IExpandListener>();

	public class Expander extends Clickable {
		private Triangle triangle;

		public Expander() {
			setStyle(Clickable.STYLE_TOGGLE);
			triangle = new Triangle();
			triangle.setSize(10, 10);
			triangle.setBackgroundColor(ColorConstants.black);
			triangle.setForegroundColor(ColorConstants.black);
			triangle.setFill(true);
			close();
			triangle.setLocation(new Point(5, 3));
			this.setLayoutManager(new FreeformLayout());
			this.add(triangle);
			this.setPreferredSize(15, 15);
			this.addActionListener(ExpandGraphLabel.this);
		}

		public void open() {
			triangle.setDirection(Triangle.SOUTH);
		}

		public void close() {
			triangle.setDirection(Triangle.EAST);
		}

		@Override
		public void setSelected(boolean value) {
			super.setSelected(value);
			doClick();
		}

	}

	private Label label = null;
	private IFigure container;
	private IFigure title;

	public ExpandGraphLabel() {

		ToolbarLayout layout = new ExpandableToolbarLayout();
		layout.setSpacing(0);
		setLayoutManager(layout);

		title = createTitleFigure();
		add(title);
		container = createContainerFigure();
		add(container);
		// expander.setSelected(true);
		container.setVisible(isExpanded());

		this.setBackgroundColor(NodeWithFacetsFigure.classColor);
		this.setOpaque(true);
		setBorder(new LineBorder(ColorConstants.black, 1));
	}

	private IFigure createContainerFigure() {
		IFigure container = new Figure();
		ToolbarLayout layout = new ExpandableToolbarLayout();
		layout.setSpacing(0);
		container.setLayoutManager(layout);
		return container;
	}

	private IFigure createTitleFigure() {
		Figure titleF = new Figure();
		this.label = new Label() {

			/*
			 * This method is overwritten so that the text is not truncated. (non-Javadoc)
			 * 
			 * @see org.eclipse.draw2d.Label#paintFigure(org.eclipse.draw2d.Graphics)
			 */
			@Override
			protected void paintFigure(Graphics graphics) {
				if (isOpaque()) {
					super.paintFigure(graphics);
				}
				Rectangle bounds = getBounds();
				graphics.translate(bounds.x, bounds.y);
				if (getIcon() != null) {
					graphics.drawImage(getIcon(), getIconLocation());
				}
				if (!isEnabled()) {
					graphics.translate(1, 1);
					graphics.setForegroundColor(ColorConstants.buttonLightest);
					graphics.drawText(getSubStringText(), getTextLocation());
					graphics.translate(-1, -1);
					graphics.setForegroundColor(ColorConstants.buttonDarker);
				}
				graphics.drawText(getText(), getTextLocation());
				graphics.translate(-bounds.x, -bounds.y);
			}

		};
		expander = new Expander();
		titleF.setFont(Display.getDefault().getSystemFont());
		ToolbarLayout layout = new ToolbarLayout(true);
		layout.setSpacing(5);
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		titleF.setLayoutManager(layout);
		titleF.add(this.expander);
		titleF.add(this.label);
		titleF.setOpaque(true);
		return titleF;
	}

	public void showExpanedSymbol() {
		if (expander.getParent() != getTitle())
			getTitle().add(expander, 0);
	}

	public void hideExpanedSymbol() {
		if (expander.getParent() == getTitle())
			getTitle().remove(expander);
	}

	public void toogle() {
		expander.setSelected(!expander.isSelected());
		if (isExpanded()) {
			collapse();
		} else {
			expand();
		}
	}

	public void toogleAll() {
		if (isExpanded()) {
			collapseAllChildren();
		} else {
			expandAllChildren();
		}
	}

	public void expandAllChildren() {
		changeState(this, true);
	}

	public void collapseAllChildren() {
		changeState(this, false);
	}

	private void changeState(IFigure parent, boolean expand) {
		if (parent instanceof ExpandGraphLabel) {
			if (expand)
				((ExpandGraphLabel) parent).expand();
			else
				((ExpandGraphLabel) parent).collapse();
		}
		List kids = parent.getChildren();
		for (Object child : kids) {
			changeState((IFigure) child, expand);
		}
	}

	public void expand() {
		expander.setSelected(true);
	}

	public void collapse() {
		expander.setSelected(false);
	}

	private void fireExpandNotyfication(boolean expanded) {
		for (IExpandListener l : listeners) {
			if (expanded) {
				l.expanded();
			} else {
				l.collapsed();
			}
		}
	}

	public void addExpandListener(IExpandListener listener) {
		listeners.add(listener);
	}

	public void removeExpandListener(IExpandListener listener) {
		listeners.remove(listener);
	}

	public boolean isExpanded() {
		return expander.isSelected();
	}

	private void hideNestedFigures() {
		container.setVisible(false);
	}

	private void showNestedFigures() {
		container.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		boolean selected = expander.isSelected();
		if (selected) {
			expander.open();
			showNestedFigures();
			fireExpandNotyfication(true);
		} else {
			expander.close();
			hideNestedFigures();
			fireExpandNotyfication(false);
		}
	}

	public void setTextT(String string) {
		this.setPreferredSize(null);
		this.label.setText(string);
		this.add(label);
		getLayoutManager().layout(this);
		this.invalidate();
		this.revalidate();
		this.validate();
	}

	public void setText(String string) {
		this.label.setText(string);
	}

	public void setImage(Image image) {
		this.label.setIcon(image);
	}

	@Override
	public void setLocation(Point p) {
		super.setLocation(p);
	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
	}

	public interface IExpandListener {
		public void expanded();

		public void collapsed();

	}

	public IFigure getContainer() {
		return container;
	}

	public IFigure getTitle() {
		return title;
	}

	class ExpandableToolbarLayout extends ToolbarLayout {

		@Override
		protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
			Insets insets = container.getInsets();
			if (isHorizontal()) {
				wHint = -1;
				if (hHint >= 0)
					hHint = Math.max(0, hHint - insets.getHeight());
			} else {
				hHint = -1;
				if (wHint >= 0)
					wHint = Math.max(0, wHint - insets.getWidth());
			}

			List children = container.getChildren();
			Dimension prefSize = calculateChildrenSize(children, wHint, hHint, true);
			// Do a second pass, if necessary
			if (wHint >= 0 && prefSize.width > wHint) {
				prefSize = calculateChildrenSize(children, prefSize.width, hHint, true);
			} else if (hHint >= 0 && prefSize.width > hHint) {
				prefSize = calculateChildrenSize(children, wHint, prefSize.width, true);
			}

			prefSize.height += Math.max(0, children.size() - 1) * spacing;
			return transposer.t(prefSize).expand(insets.getWidth(), insets.getHeight())
					.union(getBorderPreferredSize(container));
		}

		private Dimension calculateChildrenSize(List children, int wHint, int hHint, boolean preferred) {
			Dimension childSize;
			IFigure child;
			int height = 0, width = 0;
			for (int i = 0; i < children.size(); i++) {
				child = (IFigure) children.get(i);
				if (child.isShowing()) {
					childSize = transposer.t(preferred ? getChildPreferredSize(child, wHint, hHint)
							: getChildMinimumSize(child, wHint, hHint));
					height += childSize.height;
					width = Math.max(width, childSize.width);
				}
			}
			return new Dimension(width, height);
		}

		@Override
		public Dimension getMinimumSize(IFigure container, int w, int h) {
			if (container.isShowing()) {
				return super.getMinimumSize(container, w, h);
			}
			return null;
		}

	}

}

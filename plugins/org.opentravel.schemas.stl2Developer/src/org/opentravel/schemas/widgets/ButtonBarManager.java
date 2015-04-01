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
package org.opentravel.schemas.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Manager for a {@link ButtonBar}, similar to {@link ToolBarManager}. It's recommended to use this class instead of
 * direct access to {@link ButtonBar}
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ButtonBarManager extends ContributionManager {

	private ButtonBar buttonBar;
	private final int itemStyle;

	public ButtonBarManager(final int itemStyle) {
		this.itemStyle = itemStyle;
	}

	public void enable(boolean enabled) {
		buttonBar.setEnabled(enabled);
		buttonBar.update();
		// TODO make buttons grey

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#update(boolean)
	 */
	@Override
	public void update(final boolean force) {
		if (isDirty() || force) {

			if (buttonBarExist()) {

				// clean contains all active items without double separators
				final IContributionItem[] items = getItems();
				final List<IContributionItem> clean = new ArrayList<IContributionItem>(items.length);
				IContributionItem separator = null;
				for (int i = 0; i < items.length; ++i) {
					final IContributionItem ci = items[i];
					if (!isChildVisible(ci)) {
						continue;
					}
					if (ci.isSeparator()) {
						// delay creation until necessary
						// (handles both adjacent separators, and separator at
						// end)
						separator = ci;
					} else {
						if (separator != null) {
							if (clean.size() > 0) {
								clean.add(separator);
							}
							separator = null;
						}
						clean.add(ci);
					}
				}

				// determine obsolete items (removed or non active)
				List<Button> mi = buttonBar.getButtons();
				final List<Button> toRemove = new ArrayList<Button>(mi.size());
				for (final Button b : mi) {
					// there may be null items in a buttonbar
					if (b == null) {
						continue;
					}

					final Object data = b.getData();
					if (data == null || !clean.contains(data)
							|| (data instanceof IContributionItem && ((IContributionItem) data).isDynamic())) {
						toRemove.add(b);
					}
				}

				// Turn redraw off if the number of items to be added
				// is above a certain threshold, to minimize flicker,
				// otherwise the buttonbar can be seen to redraw after each item.
				// Do this before any modifications are made.
				// We assume each contribution item will contribute at least one
				// buttonbar item.
				final boolean useRedraw = (clean.size() - (mi.size() - toRemove.size())) >= 3;
				try {
					if (useRedraw) {
						buttonBar.setRedraw(false);
					}

					// remove obsolete items
					for (int i = toRemove.size(); --i >= 0;) {
						final Button item = toRemove.get(i);
						if (!item.isDisposed()) {
							item.dispose();
						}
					}

					// add new items
					IContributionItem src, dest;
					mi = buttonBar.getButtons();
					int firstDif = 0;
					// skipping all the items that are the same till the first difference
					for (int i = 0; i < items.length; i++) {
						src = items[i];
						// get corresponding item in SWT widget
						if (i < mi.size()) {
							dest = (IContributionItem) mi.get(i).getData();
						} else {
							dest = null;
						}

						if (dest != null && src.equals(dest)) {
							continue;
						}

						if (dest != null && dest.isSeparator() && src.isSeparator()) {
							mi.get(i).setData(src);
							continue;
						}
						firstDif = i;
						break;
					}
					// removing the rest of the list because we need to recreate it
					for (int i = firstDif; i < mi.size(); i++) {
						mi.get(i).dispose();
					}
					for (int i = firstDif; i < items.length; i++) {
						src = items[i];
						src.fill(buttonBar);
					}

					setDirty(false);

					// turn redraw back on if we turned it off above
				} finally {
					if (useRedraw) {
						buttonBar.setRedraw(true);
					}
				}

				final Point beforePack = buttonBar.getSize();
				buttonBar.pack(true);
				buttonBar.layoutButtons();
				final Point afterPack = buttonBar.getSize();

				// If the BB didn't change size then we're done
				if (beforePack.equals(afterPack)) {
					return;
				}

				// OK, we need to re-layout the BB
				buttonBar.getParent().layout();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionManager#add(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void add(final IAction action) {
		for (final IContributionItem ci : getItems()) {
			if (ci instanceof ActionContributionItem) {
				final ActionContributionItem aci = (ActionContributionItem) ci;
				if (aci.getAction().equals(action)) {
					return;
				}
			}
		}
		super.add(action);
	}

	public void switchActions(final IAction oldOne, final IAction newOne) {
		for (final IContributionItem ci : getItems()) {
			if (ci instanceof ActionContributionItem) {
				final ActionContributionItem aci = (ActionContributionItem) ci;
				if (aci.getAction().equals(oldOne)) {
					final int i = indexOf(ci);
					if (i < 0) {
						return;
					}
					insertAfter(aci.getId(), newOne);
					remove(aci);
					update(true);
					return;
				}
			}
		}
	}

	public Composite createControl(final Composite parent) {
		if (!buttonBarExist() && parent != null) {
			buttonBar = new ButtonBar(parent, itemStyle);
			update(true);
		}
		return buttonBar;
	}

	public Composite createControl(final FormToolkit toolkit, final Composite parent) {
		if (!buttonBarExist() && parent != null) {
			buttonBar = new ButtonBar(parent, itemStyle);
			buttonBar.setBackground(toolkit.getColors().getBackground());
			update(true);
		}
		return buttonBar;
	}

	public ButtonBar getControl() {
		return buttonBar;
	}

	private boolean buttonBarExist() {
		return buttonBar != null && !buttonBar.isDisposed();
	}

	public void dispose() {
		if (buttonBarExist()) {
			buttonBar.dispose();
		}
		buttonBar = null;

		final IContributionItem[] items = getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].dispose();
		}
	}

	private boolean isChildVisible(final IContributionItem item) {
		Boolean v;

		final IContributionManagerOverrides overrides = getOverrides();
		if (overrides == null) {
			v = null;
		} else {
			v = getOverrides().getVisible(item);
		}

		if (v != null) {
			return v.booleanValue();
		}
		return item.isVisible();
	}

}

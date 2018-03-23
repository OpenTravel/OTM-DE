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
package org.opentravel.schemas.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It is responsible for managing the navigation history. If any {@link Viewer} want to contribute to this stack the
 * should add this class as {@link ISelectionChangedListener}. Note this class is not thread-safety but it should be.
 * 
 * <pre>
 * Viewer.addSelectionChangedListener(OtmRegistry.getMainController().getNavigationController())
 * </pre>
 * 
 * @author Pawel Jedruch
 * 
 */
// TODO pawel: add stack size
// TODO pawel: should be thread-safety
public class DefaultNavigationService implements ISelectionListener, INavigationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNavigationService.class);

	class IdSelectionChangedEvent extends SelectionChangedEvent {
		private static final long serialVersionUID = 1L;

		public IdSelectionChangedEvent(String id, ISelectionProvider source, ISelection selection) {
			super(source, selection);
			partId = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((partId == null) ? 0 : partId.hashCode());
			result = prime * result + ((selection == null) ? 0 : selection.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IdSelectionChangedEvent other = (IdSelectionChangedEvent) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (partId == null) {
				if (other.partId != null)
					return false;
			} else if (!partId.equals(other.partId))
				return false;
			if (selection == null) {
				if (other.selection != null)
					return false;
			} else if (!selection.equals(other.selection))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			return true;
		}

		public String getPartId() {
			return partId;
		}

		@Override
		public String toString() {
			return selection.toString();
		}

		private final String partId;

		private DefaultNavigationService getOuterType() {
			return DefaultNavigationService.this;
		}
	}

	private final Deque<TreeSet<IdSelectionChangedEvent>> backStack = new LinkedList<TreeSet<IdSelectionChangedEvent>>();
	private final Deque<TreeSet<IdSelectionChangedEvent>> forwardStack = new LinkedList<TreeSet<IdSelectionChangedEvent>>();
	private TreeSet<IdSelectionChangedEvent> currentSelection;
	private ISelectionHandler selectionHandler = new NullSelectionHandler();

	private List<ISelectionChangedListener> listeners = new CopyOnWriteArrayList<ISelectionChangedListener>();

	private Map<String, Integer> partsId = Collections.emptyMap();

	/**
	 * 
	 * @param partsId
	 *            - based on this attribute the Navigation Service will filter incoming events from
	 *            {@link ISelectionService}. Order of selection depends on priority for given event. The events from
	 *            parts with smaller id will be called before this one with higher id. The event from the same source
	 *            (same id) will override saved one.
	 */
	public DefaultNavigationService(Map<String, Integer> partsId) {
		currentSelection = createEmptySelection();
		this.partsId = partsId;
	}

	private TreeSet<IdSelectionChangedEvent> createEmptySelection() {
		TreeSet<IdSelectionChangedEvent> selection = new TreeSet<IdSelectionChangedEvent>(
				new Comparator<IdSelectionChangedEvent>() {

					@Override
					public int compare(IdSelectionChangedEvent o1, IdSelectionChangedEvent o2) {
						return partsId.get(o1.getPartId()).compareTo(partsId.get(o2.getPartId()));
					}
				});
		return selection;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection == null)
			throw new IllegalArgumentException("Cannot add null selection to history");
		// LOGGER.debug("Starting selection changed.");
		if (isPartSupported(part.getSite().getId())) {
			ISelectionProvider provider = part.getSite().getSelectionProvider();
			// FIXME - this enables the navigation service to track where the user has been.
			// But, it also throws new events that cause concurrent modification exception when
			// RestResourceView is exposed but never used.
			//
			// if (provider != null) {
			// IdSelectionChangedEvent event = new IdSelectionChangedEvent(part.getSite().getId(), provider, selection);
			// LOGGER.debug("Throwing event: " + event.getPartId());
			// selectionChanged(event);
			// }
		}
		// LOGGER.debug("Finished selection changed.");
	}

	private boolean isPartSupported(String id) {
		return partsId.keySet().contains(id);
	}

	private void selectionChanged(IdSelectionChangedEvent event) {
		if (isSameSelection(currentSelection, event)) {
			return;
		}
		if (event.getSelection().isEmpty() && !shouldSaveEmptySelection(currentSelection, event)) {
			return;
		}

		synchronized (this) {
			if (!currentSelection.isEmpty()) {
				backStack.addFirst(currentSelection);
			}

			currentSelection = appendNewEvent(currentSelection, event);

			if (!forwardStack.isEmpty()) {
				forwardStack.clear();
			}
			fireStackChange(event);
		}
	}

	private boolean shouldSaveEmptySelection(TreeSet<IdSelectionChangedEvent> selection, IdSelectionChangedEvent event) {
		for (IdSelectionChangedEvent e : selection) {
			if (e.getPartId().equals(event.getPartId())) {
				return true;
			}
		}
		return false;
	}

	private boolean isSameSelection(TreeSet<IdSelectionChangedEvent> currentSelection, IdSelectionChangedEvent event) {
		NavigableSet<IdSelectionChangedEvent> subSet = currentSelection.subSet(event, true, event, true);
		if (!subSet.isEmpty()) {
			for (IdSelectionChangedEvent e : subSet) {
				if (e.equals(event))
					return true;
			}
		}
		return false;
	}

	private TreeSet<IdSelectionChangedEvent> appendNewEvent(TreeSet<IdSelectionChangedEvent> currentSelection,
			IdSelectionChangedEvent event) {
		TreeSet<IdSelectionChangedEvent> newSelection = new TreeSet<IdSelectionChangedEvent>(currentSelection);
		if (newSelection.contains(event)) {
			newSelection.remove(event);
		}
		newSelection.add(event);
		return newSelection;
	}

	@Override
	public void goBackward() {
		TreeSet<IdSelectionChangedEvent> event = backStack.pollFirst();
		LOGGER.debug("Go Backward to " + event);
		if (event != null) {
			forwardStack.addFirst(currentSelection);
			currentSelection = event;
			performSelection(event);
		}
	}

	private void performSelection(TreeSet<IdSelectionChangedEvent> events) {
		selectionHandler.setSelection(events);
		for (IdSelectionChangedEvent event : events) {
			fireStackChange(event);
		}
	}

	@Override
	public void goForward() {
		TreeSet<IdSelectionChangedEvent> event = forwardStack.pollFirst();
		LOGGER.debug("Go Forward to " + event);
		if (event != null) {
			backStack.addFirst(currentSelection);
			currentSelection = event;
			performSelection(event);
		}
	}

	@Override
	public Collection<SelectionChangedEvent> getBackwardEvent() {
		if (backStack.peekFirst() == null)
			return Collections.emptyList();
		return new ArrayList<SelectionChangedEvent>(backStack.peekFirst());
	}

	@Override
	public Collection<SelectionChangedEvent> getForwardEvent() {
		if (forwardStack.peekFirst() == null)
			return Collections.emptyList();
		return new ArrayList<SelectionChangedEvent>(forwardStack.peekFirst());
	}

	@Override
	public Collection<SelectionChangedEvent> getCurrentSelection() {
		return new ArrayList<SelectionChangedEvent>(currentSelection);
	}

	public Collection<String> getSelectionProviderIDs() {
		return partsId.keySet();
	}

	public void addChangeListeners(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListeners(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	private void fireStackChange(SelectionChangedEvent event) {
		for (ISelectionChangedListener l : listeners) {
			l.selectionChanged(event);
		}
	}

	@Override
	public void setSelectionHandler(ISelectionHandler handler) {
		selectionHandler = handler;
	}

	class NullSelectionHandler implements ISelectionHandler {

		@Override
		public void setSelection(TreeSet<IdSelectionChangedEvent> events) {
			// do nothing

		}
	};

}

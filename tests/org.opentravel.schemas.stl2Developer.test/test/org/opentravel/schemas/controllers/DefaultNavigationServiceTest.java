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
package org.opentravel.schemas.controllers;

import org.junit.Before;

/**
 * @author Pawel Jedruch
 * 
 */
//
// May 2018 - navigation is no longer displayed in the GUI so this service is not needed.
//
public class DefaultNavigationServiceTest {

	// private DefaultNavigationService controller;
	// private TestSelectionChangeNotifier selectionNotifier;
	// private ISelectionProvider mockSelectionProvider;

	@Before
	public void beforeEachTest() {
		// Map<String, Integer> map = new HashMap<String, Integer>();
		// map.put("id1", 1);
		// controller = new DefaultNavigationService(map);
		// selectionNotifier = new TestSelectionChangeNotifier(controller);
		// mockSelectionProvider = Mockito.mock(ISelectionProvider.class);
	}

	// @Test
	// public void goBackwardShouldDoNothingForEmptyStacks() {
	//
	// controller.goBackward();
	//
	// Assert.assertTrue(controller.getBackwardEvent().isEmpty());
	// Assert.assertTrue(controller.getForwardEvent().isEmpty());
	// }
	//
	// @Test
	// public void goForwardShouldDoNothingForEmptyStacks() {
	// controller.goForward();
	//
	// Assert.assertTrue(controller.getBackwardEvent().isEmpty());
	// Assert.assertTrue(controller.getForwardEvent().isEmpty());
	// }
	//
	// @Test(expected = IllegalArgumentException.class)
	// public void shouldThrowExceptionForNullEvent() {
	// controller.selectionChanged(mockIWorkbenchPart(""), null);
	// }
	//
	// @Test
	// public void shouldSaveSelection() {
	// ISelection event1 = mockSelection();
	// selectionNotifier.selectionChanged(true, event1);
	//
	// assertSelectionSames(Arrays.asList(event1), controller.getCurrentSelection());
	// Assert.assertTrue(controller.getBackwardEvent().isEmpty());
	// Assert.assertTrue(controller.getForwardEvent().isEmpty());
	// }
	//
	// @Test
	// public void shouldNotSaveSelection() {
	// ISelection event1 = mockSelection();
	// selectionNotifier.selectionChanged(false, event1);
	//
	// Assert.assertTrue(controller.getCurrentSelection().isEmpty());
	// }
	//
	// @Test
	// public void shouldSaveSelectionWithCorrectOrder() {
	// ISelection event1 = mockSelection();
	// ISelection event2 = mockSelection();
	// selectionNotifier.selectionChanged(true, event1);
	// selectionNotifier.selectionChanged(true, event2);
	//
	// assertSelectionSames(Arrays.asList(event1), controller.getBackwardEvent());
	// assertSelectionSames(Arrays.asList(event2), controller.getCurrentSelection());
	// }
	//
	// @Test
	// public void shouldSaveSelectionIgnoreDuplicatedEvents() {
	// ISelection event1 = mockSelection();
	// Object selected = new Object();
	// ISelection event2a = mockSelection(selected);
	// ISelection event2b = mockSelection(selected);
	// selectionNotifier.selectionChanged(true, event1);
	// selectionNotifier.selectionChanged(true, event2a);
	// selectionNotifier.selectionChanged(true, event2b);
	//
	// assertSelectionSames(Arrays.asList(event1), controller.getBackwardEvent());
	// assertSelectionSames(Arrays.asList(event2a), controller.getCurrentSelection());
	// Assert.assertTrue(controller.getForwardEvent().isEmpty());
	// }
	//
	// @Test
	// public void shouldGoBack() {
	// ISelection event1 = mockSelection();
	// ISelection event2 = mockSelection();
	// selectionNotifier.selectionChanged(true, event1);
	// selectionNotifier.selectionChanged(true, event2);
	//
	// controller.goBackward();
	//
	// Assert.assertTrue(controller.getBackwardEvent().isEmpty());
	// assertSelectionSames(Arrays.asList(event2), controller.getForwardEvent());
	// }
	//
	// @Test
	// public void shouldGoBackAndForward() {
	// ISelection event1 = mockSelection();
	// ISelection event2 = mockSelection();
	// selectionNotifier.selectionChanged(true, event1);
	// selectionNotifier.selectionChanged(true, event2);
	//
	// controller.goBackward();
	// controller.goForward();
	//
	// Assert.assertTrue(controller.getForwardEvent().isEmpty());
	// assertSelectionSames(Arrays.asList(event1), controller.getBackwardEvent());
	// assertSelectionSames(Arrays.asList(event2), controller.getCurrentSelection());
	// }
	//
	// @Test
	// public void shouldClearForwardStackOnNewEvent() {
	// ISelection event1 = mockSelection();
	// ISelection event2 = mockSelection();
	// selectionNotifier.selectionChanged(true, event1);
	// selectionNotifier.selectionChanged(true, event2);
	//
	// controller.goBackward();
	// ISelection event3 = mockSelection();
	// selectionNotifier.selectionChanged(true, event3);
	//
	// Assert.assertTrue(controller.getForwardEvent().isEmpty());
	// assertSelectionSames(Arrays.asList(event1), controller.getBackwardEvent());
	// }
	//
	// @Test
	// public void shouldBatchEventsWithDifferentIds() {
	// Map<String, Integer> map = new HashMap<String, Integer>();
	// map.put("id1", 1);
	// map.put("id2", 2);
	// controller = new DefaultNavigationService(map);
	// selectionNotifier = new TestSelectionChangeNotifier(controller);
	// ISelection event1 = mockSelection();
	// ISelection event2 = mockSelection();
	// selectionNotifier.selectionChanged(event1, "id1");
	// selectionNotifier.selectionChanged(event2, "id2");
	//
	// assertSelectionSames(Arrays.asList(event1, event2), controller.getCurrentSelection());
	// }
	//
	// @Test
	// public void shouldRemoveEventWithSameId() {
	// Map<String, Integer> map = new HashMap<String, Integer>();
	// map.put("id1", 1);
	// map.put("id2", 1);
	// controller = new DefaultNavigationService(map);
	// selectionNotifier = new TestSelectionChangeNotifier(controller);
	// ISelection event1 = mockSelection();
	// ISelection event2 = mockSelection();
	// selectionNotifier.selectionChanged(event1, "id1");
	// selectionNotifier.selectionChanged(event2, "id2");
	//
	// assertSelectionSames(Arrays.asList(event2), controller.getCurrentSelection());
	// }
	//
	// @Test
	// public void shouldBatchEventsWithDifferentIdsCorrectOrder() {
	// Map<String, Integer> map = new HashMap<String, Integer>();
	// map.put("id1", 1);
	// map.put("id2", 2);
	// map.put("id3", 3);
	// controller = new DefaultNavigationService(map);
	// selectionNotifier = new TestSelectionChangeNotifier(controller);
	// ISelection event1 = mockSelection();
	// ISelection event2 = mockSelection();
	// ISelection event3 = mockSelection();
	// selectionNotifier.selectionChanged(event1, "id3");
	// selectionNotifier.selectionChanged(event2, "id2");
	// selectionNotifier.selectionChanged(event3, "id1");
	//
	// assertSelectionSames(Arrays.asList(event3, event2, event1),
	// controller.getCurrentSelection());
	// }
	//
	// @Test
	// public void shouldSaveEmptySelectionForExisitingPart() {
	// Map<String, Integer> map = new HashMap<String, Integer>();
	// map.put("id3", 3);
	// controller = new DefaultNavigationService(map);
	// selectionNotifier = new TestSelectionChangeNotifier(controller);
	// ISelection event1 = mockSelection();
	// selectionNotifier.selectionChanged(event1, "id3");
	// selectionNotifier.selectionChanged(new EmptySelection(), "id3");
	//
	// assertSelectionSames(Collections.singletonList(new EmptySelection()),
	// controller.getCurrentSelection());
	// assertSelectionSames(Collections.singletonList(event1), controller.getBackwardEvent());
	// }
	//
	// @Test
	// public void shouldSaveEmptySelectionForExisitingPartAfterNonEmptySelection() {
	// Map<String, Integer> map = new HashMap<String, Integer>();
	// map.put("id3", 3);
	// controller = new DefaultNavigationService(map);
	// selectionNotifier = new TestSelectionChangeNotifier(controller);
	// ISelection event1 = mockSelection();
	// selectionNotifier.selectionChanged(event1, "id3");
	// selectionNotifier.selectionChanged(new EmptySelection(), "id3");
	// ISelection event2 = mockSelection();
	// selectionNotifier.selectionChanged(event2, "id3");
	//
	// assertSelectionSames(Collections.singletonList(event2), controller.getCurrentSelection());
	// assertSelectionSames(Collections.singletonList(new EmptySelection()),
	// controller.getBackwardEvent());
	// }
	//
	// @Test
	// public void shouldIgnoreEmptySelectionFromPartWithCurrentyNonSelection() {
	// Map<String, Integer> map = new HashMap<String, Integer>();
	// String notSelected = "NotSelected";
	// String part1 = "Part1";
	// map.put(part1, 3);
	// map.put(notSelected, 3);
	// controller = new DefaultNavigationService(map);
	// selectionNotifier = new TestSelectionChangeNotifier(controller);
	// ISelection event1 = mockSelection();
	// selectionNotifier.selectionChanged(event1, part1);
	// selectionNotifier.selectionChanged(new EmptySelection(), notSelected);
	//
	// assertSelectionSames(Collections.singletonList(event1), controller.getCurrentSelection());
	// assertSelectionSames(new ArrayList<ISelection>(), controller.getBackwardEvent());
	// }
	//
	// private ISelection mockSelection() {
	// return new StructuredSelection(new Object());
	// }
	//
	// private ISelection mockSelection(Object selectedObject) {
	// return new StructuredSelection(selectedObject);
	// }
	//
	// class TestSelectionChangeNotifier {
	//
	// private DefaultNavigationService listener;
	//
	// public TestSelectionChangeNotifier(DefaultNavigationService listener) {
	// this.listener = listener;
	// }
	//
	// public void selectionChanged(boolean supported, ISelection selection) {
	// String id = getId(supported);
	// IWorkbenchPart part = mockIWorkbenchPart(id);
	// listener.selectionChanged(part, selection);
	// }
	//
	// private String getId(boolean supported) {
	// String id = null;
	// if (supported) {
	// id = listener.getSelectionProviderIDs().iterator().next();
	// } else {
	// // double check to make sure id doesn't exist in list of views
	// Assert.assertFalse(listener.getSelectionProviderIDs().contains(id));
	// }
	// return id;
	// }
	//
	// public void selectionChanged(ISelection selection, String providerId) {
	// IWorkbenchPart part = mockIWorkbenchPart(providerId);
	// listener.selectionChanged(part, selection);
	// }
	// };
	//
	// private IWorkbenchPart mockIWorkbenchPart(String id) {
	// IWorkbenchPart part = Mockito.mock(IWorkbenchPart.class);
	// IWorkbenchPartSite mockSite = Mockito.mock(IWorkbenchPartSite.class);
	// Mockito.when(part.getSite()).thenReturn(mockSite);
	// Mockito.when(mockSite.getId()).thenReturn(id);
	// Mockito.when(mockSite.getSelectionProvider()).thenReturn(mockSelectionProvider);
	// return part;
	// }
	//
	// private void assertSelectionSames(Collection<? extends ISelection> expected,
	// Collection<SelectionChangedEvent> actual) {
	// Collection<ISelection> actualSelections = Collections2.transform(actual,
	// new Function<SelectionChangedEvent, ISelection>() {
	//
	// @Override
	// public ISelection apply(SelectionChangedEvent event) {
	// return event.getSelection();
	// }
	// });
	// assertCollectionEquals(expected, actualSelections);
	// }
	//
	// private void assertCollectionEquals(Collection<?> expected, Collection<?> actualSelections) {
	// if (expected.size() != actualSelections.size())
	// Assert.fail("Different collections sizes. Expected: " + expected.size() + ", but was: "
	// + actualSelections.size());
	//
	// Iterator<?> actualI = actualSelections.iterator();
	// for (Object e : expected) {
	// Object actual = actualI.next();
	// if (!e.equals(actual)) {
	// Assert.fail("Expected object: " + e + ", but was " + actual);
	// }
	// }
	// }
	//
	// static class EmptySelection implements ISelection {
	//
	// @Override
	// public boolean isEmpty() {
	// return true;
	// }
	//
	// @Override
	// public int hashCode() {
	// return 0;
	// }
	//
	// @Override
	// public boolean equals(Object obj) {
	// return obj instanceof EmptySelection;
	// }
	//
	// }
}

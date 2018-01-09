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

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.OtmActions;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Button event otmHandlers
 * 
 * @author Dave Hollander
 * 
 */

public class OtmHandlers {
	private final static Logger LOGGER = LoggerFactory.getLogger(OtmHandlers.class);

	private static boolean suspend = false;
	private Node dragSourceNode;
	private Node dragTargetNode;
	private MainController mc;

	/**
	 * @param userNotifier
	 */
	public OtmHandlers() {
		this.mc = OtmRegistry.getMainController();

	}

	public static void suspendHandlers() {
		suspend = true;
	}

	public static void enableHandlers() {
		// to make sure that all events all handled before enabling handlers.
		Display.getDefault().readAndDispatch();
		suspend = false;
	}

	public final class ButtonSelectionHandler extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Object o = e.widget.getData();
			if (o instanceof OtmEventData) {
				final OtmEventData wd = (OtmEventData) o;

				if (e.widget instanceof Button) {
					// used for check boxes
					wd.setSelection(((Button) e.widget).getSelection());
				} else if (e.widget instanceof Spinner) {
					final Spinner spinner = (Spinner) e.widget;
					final int selection = spinner.getSelection();
					wd.setInt(selection);
				}
				// Do the action
				wd.getActionHandler().doEvent(wd);
			}
		}
	}

	/**
	 * ************************************************************************* ****** Drag-n-drop Controls
	 */

	/**
	 * Register a control widget to listen for drop events.
	 * 
	 * @param widget
	 *            - control widget to enable for drop events
	 * @param actionContext
	 *            - class instance of action handlers to use for the event
	 * @param action
	 *            - int describing which action to take on drop
	 * @param initContext
	 *            - class instance with context store for drop event handling
	 */
	public void enableDropTarget(final Control widget, final OtmActions actionContext, final int action,
			final OtmWidgets initContext) {
		final Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		final int operations = DND.DROP_MOVE | DND.DROP_COPY;
		final DropTarget target = new DropTarget(widget, operations);
		target.setTransfer(types);
		target.addDropListener(new PropertyDropTarget(actionContext, action));
	}

	/**
	 * Drag-n-drop onto a property type field. As a drag-n-drop, there is no widget associated with the event. The
	 * nodeID is used and will be passed to the operation. DropTargetListener - stop any drop from happening on the
	 * current target by setting the event.detail field to DND_DROP_NONE.
	 */
	public final class PropertyDropTarget extends DropTargetAdapter {
		// each target creates its own class, i.e. new PropertyDropTarget()
		private final OtmEventData thisEvent;

		public PropertyDropTarget(final OtmActions actions, final int action) {
			super();
			// LOGGER.debug("PropertyDropTarget. Action = " + action);
			final OtmEventData ed = new OtmEventData();
			ed.setActionHandler(actions);
			ed.setBusinessEvent(action);
			ed.setText("");
			thisEvent = ed; // Event data unique to this instance of the class.
		}

		/**
		 * dropAccept is called just before the drop is performed. The drop target is given the chance to change the
		 * nature of the drop or veto the drop by setting the event.detail field Note - event data is null! Can not find
		 * the dragged object (source) from the event passed to this handler.
		 */
		@Override
		public void dropAccept(final DropTargetEvent event) {
			// LOGGER.debug("dropAccept() - event = " + event);
			if (event.item instanceof TableItem) {
				final TableItem ti = (TableItem) event.item;

				if ((ti != null) && (ti.getData() != null) && ((ti.getData() instanceof ComponentNode))) {
					// The node presented in the table row dropped on.
					final ComponentNode tNode = (ComponentNode) ti.getData();

					if (dragSourceNode != null) {
						if (tNode instanceof AttributeNode) {
							// boolean isValidAttributeDrop = tNode.isVWASimpleFacetAttribute() ?
							// dragSourceNode
							// .isVWASimpleAssignable() : dragSourceNode.isSimpleAssignable();

							// if (!isValidAttributeDrop) {
							if (!tNode.canAssign(dragSourceNode)) {
								event.detail = DND.DROP_NONE;
								DialogUserNotifier.openInformation("WARNING",
										"You can not assign a complex type to an attribute.");
								return;
							}
						}
						if (!tNode.isEditable()) {
							LOGGER.debug("Drop target is not editable.");
							DialogUserNotifier.openInformation("WARNING", "Object is not editable, drop was ignored.");
							event.detail = DND.DROP_NONE;
						}
						if (tNode instanceof RoleFacetNode) {
							event.detail = DND.DROP_NONE;
							return;
						}
						if (!dragSourceNode.isAssignable()) {
							event.detail = DND.DROP_NONE;
							return;
						}
					}
				}
			} else if (event.item instanceof TreeItem) {
				// LOGGER.debug("Accept dropping onto tree item?");
				final TreeItem ti = (TreeItem) event.item;
				if ((ti != null) && (ti.getData() != null && (ti.getData() instanceof Node))) {
					dragTargetNode = (Node) ti.getData();
					// LOGGER.debug("Allow drop onto tree item: "+dragTargetNode.getName());
				}
			}
		}

		/**
		 * dragEnter is called when the cursor has entered the drop target boundaries. For facet table, it is the whole
		 * table not just items.
		 */
		@Override
		public void dragEnter(final DropTargetEvent event) {
			super.dragEnter(event);
		}

		/**
		 * Drop - called to complete the dnd processing. Marshal the data and do the event handler.
		 */
		@Override
		public void drop(final DropTargetEvent event) {
			final OtmEventData ed = thisEvent;// OtmWidgets.getOtmEventData(thisKey);
			// LOGGER.debug("drop; event is "+ event);
			ed.setDragCopy(true);
			// isDragCopy = true;
			if (event.detail == DND.DROP_MOVE)
				ed.setDragCopy(false);
			// isDragCopy = false;
			// LOGGER.debug("Operation is copy? "+ isDragCopy);
			ed.setText((String) event.data);
			ed.widget = event.item;
			ed.getActionHandler().doEvent(ed);
		}
	}

	/**
	 * Drag listener for the tree view see: http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
	 * 
	 * @author Dave Hollander
	 * 
	 *         Set up Drag-n-Drop. http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
	 */
	public void enableDragSource(final Control source, final MainWindow initContext) {
		final Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		final int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		final DragSource src = new DragSource(source, operations);
		src.setTransfer(types);
		src.addDragListener(new OtmTreeDragSourceListener(initContext));
	}

	/**
	 * @return the dragTargetNode
	 */
	public Node getDragTargetNode() {
		return dragTargetNode;
	}

	/**
	 * Removes invalid XML characters from the given string.
	 * 
	 * @param str
	 *            the string value to process
	 * @return String
	 */

	public static String stripInvalidXMLCharacters(final String str) {
		final StringBuffer result = new StringBuffer();

		for (final char ch : str.toCharArray()) {
			if (isValid(ch)) {
				result.append(ch);
			}
		}
		return result.toString();
	}

	/**
	 * see http://www.w3.org/TR/xml/#charsets @ *
	 * 
	 * @param ch
	 * @return
	 */
	private static boolean isValid(char ch) {
		return (ch == 0x9) || (ch == 0xA) || (ch == 0xD) || ((ch >= 0x20) && (ch <= 0xD7FF))
				|| ((ch >= 0xE000) && (ch <= 0xFFFD)) || ((ch >= 0x10000) && (ch <= 0x10FFFF));
	}

	private final class OtmTreeDragSourceListener implements DragSourceListener {

		public OtmTreeDragSourceListener(final MainWindow mainWindow) {
			super();
		}

		/**
		 * This event gives the application the chance to decide if a drag should be started.
		 */
		@Override
		public void dragStart(final DragSourceEvent event) {
			// the selection event is processed before this event which will change
			// the facet table to the node being dragged.
			// LOGGER.debug("drag start --------------------------------");
			// 10/7/2015 - clean up and added curNode null check. DnD problems went away.

			event.doit = false;
			if (mc.getCurrentNode_NavigatorView() != null) {
				Node curNode = mc.getCurrentNode_NavigatorView();
				// 10/13/2015 dmh - i don't know what the compare is for. added null check
				if (curNode == dragSourceNode && mc.getPrevTreeNode() != null) {
					curNode = mc.getPrevTreeNode();
					// LOGGER.debug("drag start used prev node: " + curNode);
				}
				if (curNode != null) {
					if (curNode instanceof LibraryNode) {
						LOGGER.debug("Why drag families or libraries?");
					}
					if (curNode.isAssignable()) {
						dragSourceNode = curNode;
						event.doit = true;
						// LOGGER.debug("drag start set dragSourceNode: " + curNode);
					}
				} else
					LOGGER.debug("drag start - cur node is null");

				// Push the previous node onto the facet table.
				final OtmView view = OtmRegistry.getTypeView();
				if (view != null) {
					// LOGGER.debug("drag start restored type view to: " + view.getPreviousNode());
					// If the doc view is linked then it will have change selection, otherwise
					// restore the previous node.
					if (view.getCurrentNode() == dragSourceNode)
						view.restorePreviousNode();
				}
			}
		}

		/**
		 * Loads a dnd event with data. Called only AFTER the drop target has accepted the event but before the drop.
		 */
		@Override
		public void dragSetData(final DragSourceEvent event) {
			// DND only supports limited data formats, such as text used here.
			// LOGGER.debug("dragSetData - Dragging " + event.data); // null event
			if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
				event.data = mc.getCurrentNode_NavigatorView().getNodeID();
				// LOGGER.debug("dragSetData - Dragging - set data to " + event.data);
			}
		}

		@Override
		public void dragFinished(final DragSourceEvent event) {
			// copy|link|move|target_Move
			// System.out.println("OtmHandlers:dragFinished - event: "+event);
		}

	}

	/**
	 * If dirty, do the event. Can't wait for focus event because a CR may not cause focus to change.
	 */
	protected static final class TextDefaultListener implements Listener {
		@Override
		public void handleEvent(final Event e) {
			if (suspend) {
				return;
			}
			final Object o = e.widget.getData();
			if (o instanceof OtmEventData) {
				final OtmEventData wd = (OtmEventData) o;
				wd.setEvent(e);
				if (e.widget instanceof Text) {
					final boolean focus = wd.hasFocus();
					wd.setFocus(false); // do this first to prevent refresh from triggering modify
					wd.setText(((Text) e.widget).getText());
					if (wd.isDirty()) {
						wd.setDirty(false);
						wd.getActionHandler().doEvent(wd);
					}
					wd.setFocus(focus);
					if (focus) {
						((Text) e.widget).setFocus();
					}
				}
			}
		}
	}

	/**
	 * if there have been changes to the widget, call its action handler. Focus is controlled to prevent TextModify
	 * events when the widget has text loaded in from being processed.
	 * 
	 * @author Dave Hollander
	 * 
	 */
	protected static final class TextFocusListener implements FocusListener {
		@Override
		public void focusLost(final FocusEvent e) {
			if (suspend) {
				return;
			}
			final Object o = e.widget.getData();
			if (o instanceof OtmEventData) {
				final OtmEventData wd = (OtmEventData) o;
				wd.setFocus(false); // do this first to prevent refresh from
									// triggering modify
				if (e.widget instanceof Text) {
					final Text t = (Text) e.widget;

					wd.setText((t.getText() == null) ? "" : stripInvalidXMLCharacters(t.getText().trim()));

					if (wd.isDirty()) {
						wd.setDirty(false);
						wd.getActionHandler().doEvent(wd);
					}
				}
			}
		}

		@Override
		public void focusGained(final FocusEvent e) {
			final Object o = e.widget.getData();
			if (o instanceof OtmEventData) {
				final OtmEventData wd = (OtmEventData) o;
				wd.setFocus(true);
			}
		}
	}

	/**
	 * This is public for use with Combo
	 * 
	 * Fires when data is set into the widget and when characters are typed. Question - do i want to make changes now or
	 * on focus loss? if i do it now, all the setters will need to return change/no-change state doing it while typing
	 * is distracting and causes the cursor to move. Wait until done then do the event.
	 * 
	 * hasFocus is used to ignore events from initial data setting
	 * 
	 * @author Dave Hollander
	 * 
	 */
	public static final class TextModifyListener implements ModifyListener {
		@Override
		public void modifyText(final ModifyEvent e) {
			if ((e == null) || e.widget == null) {
				return;
			}
			if (suspend) {
				return;
			}

			final Object o = e.widget.getData();
			if (o instanceof OtmEventData) {
				final OtmEventData eventData = (OtmEventData) o;
				// OtmEventData wd = OtmWidgets.getOtmEventData((String)
				// e.widget.getData());

				if (e.widget instanceof Text) {
					final Text t = (Text) e.widget;
					// force layout after inserting text
					t.getParent().layout();

					eventData.setText((t.getText() == null) ? "" : stripInvalidXMLCharacters(t.getText().trim()));

					if (eventData.hasFocus()) {
						eventData.setDirty(true);
					}
				} else if (e.widget instanceof Combo) {

					eventData.setText(((Combo) e.widget).getText());
					if (eventData.getText().startsWith("* "))
						eventData.setText(eventData.getText().substring(2)); // context combo
					eventData.getActionHandler().doEvent(eventData);
				}
			}

		}
	}

}

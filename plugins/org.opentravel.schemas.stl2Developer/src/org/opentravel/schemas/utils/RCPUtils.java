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
package org.opentravel.schemas.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RCPUtils {
	private final static Logger LOGGER = LoggerFactory.getLogger(RCPUtils.class);

	public static boolean executeCommand(String id, Event event, IWorkbenchPartSite site) throws ExecutionException {
		ICommandService cmdService = (ICommandService) site.getService(ICommandService.class);
		Command cmd = cmdService.getCommand(id);
		// Defined in plugin.xml, isHandled is true if handler can be loaded.
		// Enabled defined in plugin.xml handler definition
		if (cmd.isDefined() && cmd.isHandled() && cmd.isEnabled()) {
			IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
			try {
				handlerService.executeCommand(id, event);
				return true;
			} catch (NotDefinedException e) {
				handleException(e, id);
			} catch (NotEnabledException e) {
				handleException(e, id);
			} catch (NotHandledException e) {
				handleException(e, id);
			}
		}
		return false;
	}

	/**
	 * Check if given part is currently active or not.
	 * 
	 * @param part
	 * @return false for inactive part, true if part is active.
	 * 
	 */
	public static boolean isPartActive(IWorkbenchPart part) {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart() == part;
	}

	private static void handleException(CommandException e, String cmdId) {
		LOGGER.debug("Exception during executing command: " + cmdId + ", error: " + e.getMessage());
	}

	/**
	 * Create menu contribution based on command id.
	 * 
	 * @param site
	 *            - a service locator that is most appropriate for this contribution. Typically the local
	 *            IWorkbenchWindow or IWorkbenchPartSite will be sufficient. Must not be null.
	 * @param commandId
	 *            - the defined command.
	 * @param label
	 *            - A label for this item. For null value the label from command definition will be used.
	 * @param tooltip
	 *            - A tooltip for this item. For null value the description from command definition will be used.
	 *            Tooltips are currently only valid for toolbar contributions.
	 * @param icon
	 *            - An icon for this item. May be null.
	 * @return A contribution item represents a contribution to a shared UI resource such as a menu or tool bar for
	 *         defined command.
	 * 
	 * @throws IllegalStateException
	 *             if given command id is not defined.
	 */
	public static IContributionItem createCommandContributionItem(IServiceLocator site, String commandId, String label,
			String tooltip, ImageDescriptor icon) {
		ICommandService service = (ICommandService) site.getService(ICommandService.class);
		Command cmd = service.getCommand(commandId);
		if (!cmd.isDefined()) {
			throw new IllegalStateException("Command not defined: " + commandId);
		}
		try {
			CommandContributionItemParameter p = new CommandContributionItemParameter(site, "", commandId, SWT.PUSH);
			if (label == null) {
				p.label = cmd.getName();
			} else {
				p.label = label;
			}
			if (tooltip == null) {
				p.tooltip = cmd.getDescription();
			} else {
				p.tooltip = tooltip;
			}
			p.icon = icon;
			CommandContributionItem item = new CommandContributionItem(p);
			return item;
		} catch (NotDefinedException e) {
			throw new IllegalStateException("Command not defined: " + commandId);
		}
	}

	/**
	 * Copy given text to system clipboard.
	 * 
	 * @param string
	 *            - text to save in clipboard, can be null.
	 */
	public static void copyToClipboard(String string) {
		if (string != null) {
			Clipboard cb = new Clipboard(Display.getDefault());
			TextTransfer textTransfer = TextTransfer.getInstance();
			cb.setContents(new Object[] { string }, new Transfer[] { textTransfer });
		}
	}

	/**
	 * Trying to find view from active workbench. If view is not created then this method will force initialization of
	 * given view.
	 * 
	 * @param id
	 *            - view id
	 * @return - view or null if workbench is not running or view is not defined
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IViewPart> T findOrCreateView(String id) {
		if (PlatformUI.isWorkbenchRunning() && !PlatformUI.getWorkbench().isStarting()) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (page != null)
				return (T) page.findView(id);
		}
		return null;
	}

	/**
	 * @param selection
	 *            must by {@link StructuredSelection}.
	 * @param clazz
	 * @return extract object of provided type or his sub-types from selection.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> extractObjects(ISelection selection, Class<T> clazz) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection ss = (StructuredSelection) selection;
			List<T> ret = new ArrayList<T>(ss.size());
			for (Object o : ss.toList()) {
				if (clazz.isAssignableFrom(o.getClass())) {
					ret.add((T) o);
				}
			}
			return ret;
		}
		return Collections.emptyList();
	}
}
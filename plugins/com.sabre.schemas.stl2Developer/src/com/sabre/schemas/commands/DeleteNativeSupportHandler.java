/*
 * Copyright (c) 2012, Sabre Inc.
 */
package com.sabre.schemas.commands;

import java.util.HashSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.views.OtmView;

/**
 * Handler that check for active {@link OtmView} if supports native delete (current SWT widget with
 * focus support Delete). If native delete is supported then it will not execute the method
 * executeAfterCheck() but will execute {@link OtmView#performNativeDelete(String)}
 * 
 * @author Pawel Jedruch
 * 
 */
public class DeleteNativeSupportHandler extends AbstractHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(DeleteNativeSupportHandler.class);

    private static final HashSet<Class<? extends Widget>> suppportedWidgets = new HashSet<Class<? extends Widget>>();
    static {
        suppportedWidgets.add(Combo.class);
        suppportedWidgets.add(Text.class);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        Control control = activePart.getSite().getShell().getDisplay().getFocusControl();
        boolean handled = performNativeDelete(control);
        if (!handled) {
            LOGGER.debug("Delete request ignored or not supported for view: "
                    + activePart.getSite().getId());
        }
        return null;
    }

    private boolean performNativeDelete(Control control) {
        if (isControlSupported(control)) {
            Event e = new Event();
            e.character = SWT.DEL;
            e.type = SWT.KeyDown;
            // has to send key up for gtk platform
            if (control.getDisplay().post(e)) {
                e.character = SWT.DEL;
                e.type = SWT.KeyUp;
                return control.getDisplay().post(e);
            }
        }
        return false;
    }

    public static boolean isControlSupported(Control control) {
        if (control == null)
            return false;

        return suppportedWidgets.contains(control.getClass());
    }
}

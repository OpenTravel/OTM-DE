/*
 * Copyright (c) 2012, Sabre Inc.
 */
package org.opentravel.schemas.utils;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.opentravel.schemas.commands.DeleteNativeSupportHandler;

/**
 * For given control this will activate the handler for global delete command. Given control should
 * support native delete action. Good example of such widget is {@link Text}.
 * 
 * @author Pawel Jedruch
 * 
 */
public class NativeDeleteFocusListener implements FocusListener, DisposeListener {

    private IHandlerActivation activation;
    private DeleteNativeSupportHandler handler = new DeleteNativeSupportHandler();
    private IHandlerService service;

    private static final NativeDeleteFocusListener INSTANCE = new NativeDeleteFocusListener();

    public static void attachListener(Control control) {
        if (DeleteNativeSupportHandler.isControlSupported(control)) {
            control.addFocusListener(INSTANCE);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        deactivate(activation);
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
        deactivate(activation);
    }

    @Override
    public void focusGained(FocusEvent e) {
        service = getService();
        // deactivate previously activated command.
        deactivate(activation);
        Widget control = e.widget;
        if (!isReadOnly(control)) {
            // when closing window with ESC key the FocusLost is not called, in this case use
            // disposeListener
            control.addDisposeListener(INSTANCE);
            activation = service.activateHandler(ActionFactory.DELETE.getCommandId(), handler,
                    new HighPriorityExpression());
        }

    }

    private boolean isReadOnly(Widget widget) {
        return (widget.getStyle() & SWT.READ_ONLY) != 0;
    }

    class HighPriorityExpression extends Expression {

        @Override
        public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
            return EvaluationResult.TRUE;
        }

        @Override
        public void collectExpressionInfo(ExpressionInfo info) {
            info.addVariableNameAccess(ISources.ACTIVE_MENU_NAME);
        }

    }

    private void deactivate(IHandlerActivation activation) {
        if (activation != null) {
            service.deactivateHandler(activation);
            this.activation = null;
            this.service = null;
        }
    }

    private IHandlerService getService() {
        IHandlerService service = (IHandlerService) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActivePart().getSite()
                .getService(IHandlerService.class);
        return service;
    }

}

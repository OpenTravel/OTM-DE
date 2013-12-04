/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class AbstractGlobalSelectionAction extends Action implements
        IPropertyChangeListener {

    private String sourceName;

    private Object sourceValue;

    public AbstractGlobalSelectionAction(String id, String sourceName) {
        this(id, PlatformUI.getWorkbench(), sourceName);
    }

    public AbstractGlobalSelectionAction(String id, IServiceLocator locator, String sourceName) {
        super();
        this.sourceName = sourceName;
        setId(id);
        setActionDefinitionId(id);
        initEvalutionListener(locator);
    }

    private void initEvalutionListener(IServiceLocator locator) {
        final IEvaluationService evaluationService = (IEvaluationService) locator
                .getService(IEvaluationService.class);
        ISourceProviderService service = (ISourceProviderService) locator
                .getService(ISourceProviderService.class);
        final ISourceProvider sourceProvider = service.getSourceProvider(sourceName);
        evaluationService.addEvaluationListener(new Expression() {

            @Override
            public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
                sourceValue = sourceProvider.getCurrentState().get(sourceName);
                return EvaluationResult.valueOf(isEnabled(sourceValue));
            }

            @Override
            public void collectExpressionInfo(ExpressionInfo info) {
                info.addVariableNameAccess(sourceName);
            }

        }, this, ENABLED);

    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (ENABLED.equals(event.getProperty())) {
            setEnabled((Boolean) event.getNewValue());
        }
    }

    public Object getSourceValue() {
        return sourceValue;
    }

    protected abstract boolean isEnabled(Object object);

}

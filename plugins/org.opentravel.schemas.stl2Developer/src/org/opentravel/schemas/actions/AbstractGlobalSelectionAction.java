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
package org.opentravel.schemas.actions;

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
//
// 5/15/2018 dmh - seems unused
//
public abstract class AbstractGlobalSelectionAction extends Action implements IPropertyChangeListener {

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
		final IEvaluationService evaluationService = (IEvaluationService) locator.getService(IEvaluationService.class);
		ISourceProviderService service = (ISourceProviderService) locator.getService(ISourceProviderService.class);
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
			Boolean enabledValue = (Boolean) event.getNewValue();

			if (enabledValue == null) {
				enabledValue = Boolean.FALSE;
			}
			setEnabled(enabledValue);
		}
	}

	public Object getSourceValue() {
		return sourceValue;
	}

	protected abstract boolean isEnabled(Object object);

}

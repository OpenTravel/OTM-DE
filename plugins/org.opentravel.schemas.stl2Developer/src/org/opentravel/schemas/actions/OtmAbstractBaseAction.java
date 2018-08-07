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

import java.util.concurrent.atomic.AtomicInteger;

import org.opentravel.schemas.actions.IWithNodeAction.AbstractWithNodeAction;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.PropertyType;
import org.opentravel.schemas.properties.StringProperties;

/**
 * @author Agnieszka Janowska
 * 
 */
public abstract class OtmAbstractBaseAction extends AbstractWithNodeAction {

	private static final AtomicInteger ACTION_COUNTER = new AtomicInteger();

	protected OtmAbstractBaseAction() {
		this(AS_PUSH_BUTTON);
	}

	protected OtmAbstractBaseAction(final int style) {
		super("", style);
	}

	protected OtmAbstractBaseAction(final StringProperties props) {
		this();
		initialize(props);
	}

	protected OtmAbstractBaseAction(final StringProperties props, final int style) {
		this(style);
		initialize(props);
	}

	protected void initialize(final StringProperties props) {
		if (props != null) {
			this.setText(props.get(PropertyType.TEXT));
			this.setToolTipText(props.get(PropertyType.TOOLTIP));
			this.setImageDescriptor(Images.getImageRegistry().getDescriptor(props.get(PropertyType.IMAGE)));
			this.setId(this.getText() + ACTION_COUNTER.incrementAndGet());
			this.setEnabled(true);
		}
	}

}

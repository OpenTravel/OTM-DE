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
/**
 * 
 */
package org.opentravel.schemas.node.handlers;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.controllers.ContextController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One value Equivalent and Example handler to be assigned to properties. Assures that only one EQ or Example exist for
 * the property. Owner must exist and have a TLEquivalentOnwer/TLExample source object. Value and context data is stored
 * in the TL model object.
 * 
 * Note: only context IDs are used because these are guaranteed unique within a library.
 * 
 * @author Dave Hollander
 *
 */
public class EqExOneValueHandler implements IValueWithContextHandler, ModelElementListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(EqExOneValueHandler.class);

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		LOGGER.debug("ower event: " + event.getType());
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		// Do NOT set the value. There is no value stored in this handler, it is read from the tl model.
		// LOGGER.debug("change event: " + event.getNewValue());
	}

	public enum ValueWithContextType {
		EXAMPLE, EQUIVALENT;
	}

	private Node owner = null;
	private TLModelElement tlOwner = null;
	private ValueWithContextType type = null;

	/**
	 * 
	 */
	public EqExOneValueHandler(PropertyNode owner, ValueWithContextType type) {
		assert (owner != null);
		assert (owner.getTLModelObject() != null);
		// Not all equ owners are example owners
		// assert (owner.getTLModelObject() instanceof TLExampleOwner);

		this.owner = owner;
		this.type = type;
		tlOwner = owner.getTLModelObject();

		switch (type) {
		case EQUIVALENT:
			if (!(tlOwner instanceof TLEquivalentOwner))
				tlOwner = owner.getParent().getTLModelObject();
			assert tlOwner instanceof TLEquivalentOwner;
			break;
		case EXAMPLE:
			if (!(tlOwner instanceof TLExampleOwner))
				tlOwner = owner.getParent().getTLModelObject();
			assert tlOwner instanceof TLExampleOwner;
			break;
		default:
			break;
		}
		assert owner.getLibrary() != null;
	}

	public EqExOneValueHandler(SimpleTypeNode owner, ValueWithContextType type) {
		assert (owner != null);
		assert (owner.getTLModelObject() != null);
		// Not all equivalent owners are example owners such as Enumerations
		// assert (owner.getTLModelObject() instanceof TLExampleOwner);

		this.owner = owner;
		this.type = type;
		tlOwner = owner.getTLModelObject();
	}

	@Override
	public int getCount() {
		int size = 0;
		switch (type) {
		case EXAMPLE:
			size = ((TLExampleOwner) tlOwner).getExamples().size();
			break;
		case EQUIVALENT:
			size = ((TLEquivalentOwner) tlOwner).getEquivalents().size();
			break;
		}
		return size;
	}

	@Override
	public String get(String context) {
		if (owner.getLibrary() == null)
			return "";
		if (context == null)
			context = owner.getLibrary().getDefaultContextId();
		switch (type) {
		case EXAMPLE:
			return ((TLExampleOwner) tlOwner).getExample(context) != null ? ((TLExampleOwner) tlOwner).getExample(
					context).getValue() : "";
		case EQUIVALENT:
			return ((TLEquivalentOwner) tlOwner).getEquivalent(context) != null ? ((TLEquivalentOwner) tlOwner)
					.getEquivalent(context).getDescription() : "";
		}
		return "";
	}

	@Override
	public void set(String value, String context) {
		if (owner.getLibrary() == null) {
			LOGGER.warn("Must have owner library to set example and equivalent values.");
			return;
		}
		if (!owner.isEditable()) {
			LOGGER.warn("Owner must be editable to set example and equivalent values.");
			return;
		}
		if (context == null && !confirmContextExists(context))
			context = owner.getLibrary().getDefaultContextId();

		// There can only be one value. Clear any if they exist.
		clearValues();
		if (value == null)
			return; // just remove value

		// Set Value
		switch (type) {
		case EXAMPLE:
			clearValues();
			if (((TLExampleOwner) tlOwner).getExample(context) != null) {
				((TLExampleOwner) tlOwner).getExample(context).setValue(value);
			} else {
				TLExample tle = new TLExample();
				tle.addListener(this);
				tle.setContext(context);
				tle.setValue(value);
				((TLExampleOwner) tlOwner).addExample(tle);
			}
			break;
		case EQUIVALENT:
			if (((TLEquivalentOwner) tlOwner).getEquivalent(context) != null) {
				((TLEquivalentOwner) tlOwner).getEquivalent(context).setDescription(value);
			} else {
				TLEquivalent tle = new TLEquivalent();
				tle.addListener(this);
				tle.setContext(context);
				tle.setDescription(value);
				((TLEquivalentOwner) tlOwner).addEquivalent(tle);
			}
			break;
		}
	}

	private void clearValues() {
		switch (type) {
		case EXAMPLE:
			List<TLExample> tleList = new ArrayList<TLExample>(((TLExampleOwner) tlOwner).getExamples());
			for (TLExample tle : tleList) {
				tle.removeListener(this);
				((TLExampleOwner) tlOwner).removeExample(tle);
			}
			break;
		case EQUIVALENT:
			List<TLEquivalent> tleqList = new ArrayList<TLEquivalent>(((TLEquivalentOwner) tlOwner).getEquivalents());
			for (TLEquivalent tle : tleqList) {
				tle.removeListener(this);
				((TLEquivalentOwner) tlOwner).removeEquivalent(tle);
			}
			break;
		}
	}

	// @Override
	// public boolean change(String sourceContext, String targetContext) {
	// if (!confirmContextExists(targetContext))
	// targetContext = owner.getLibrary().getDefaultContextId();
	//
	// if (tlOwner.getExample(targetContext) != null)
	// return false;
	// if (tlOwner.getExample(sourceContext) != null) {
	// tlOwner.getExample(sourceContext).setContext(targetContext);
	// return true;
	// }
	// return false;
	// }

	/**
	 * Fix all the examples or equivalents on this property. Assures there is only one and it has the correct context.
	 * Set the context to the passed context IF and ONLY IF that context is in the TL Library AND context controller.
	 * Uses default context otherwise.
	 */
	@Override
	public void fix(String sourceContext) {
		if (sourceContext == null || !confirmContextExists(sourceContext))
			sourceContext = owner.getLibrary().getDefaultContextId();
		switch (type) {
		case EXAMPLE:
			List<TLExample> examples = new ArrayList<TLExample>(((TLExampleOwner) tlOwner).getExamples());
			if (examples.isEmpty())
				return;
			TLExample keepThisTLE = examples.get(0);
			if (examples.size() == 1)
				examples.get(0).setContext(sourceContext);
			else {
				// There are more than one. Select one and convert the rest.
				for (TLExample tle : examples)
					if (tle.getContext().equals(sourceContext))
						keepThisTLE = tle;

				for (TLExample tle : examples)
					if (tle != keepThisTLE) {
						convertToDoc(tle);
						tle.removeListener(this);
						((TLExampleOwner) tlOwner).removeExample(tle);
					} else
						tle.setContext(sourceContext);
			}
			break;
		case EQUIVALENT:
			List<TLEquivalent> equivalents = new ArrayList<TLEquivalent>(((TLEquivalentOwner) tlOwner).getEquivalents());
			if (equivalents.isEmpty())
				return;
			TLEquivalent keepThisTLEq = equivalents.get(0);
			if (equivalents.size() == 1)
				equivalents.get(0).setContext(sourceContext);
			else {
				// There are more than one. Select one and convert the rest.
				for (TLEquivalent tle : equivalents)
					if (tle.getContext().equals(sourceContext))
						keepThisTLEq = tle;

				for (TLEquivalent tle : equivalents)
					if (tle != keepThisTLEq) {
						convertToDoc(tle);
						tle.removeListener(this);
						((TLEquivalentOwner) tlOwner).removeEquivalent(tle);
					} else
						tle.setContext(sourceContext);
			}
			break;
		}
	}

	/**
	 * Add the equivalent value to the documentation.
	 */
	private void convertToDoc(TLExample tle) {
		owner.getDocHandler().addImplementer("Example value: " + tle.getContext() + " = " + get(tle.getContext()));
	}

	private void convertToDoc(TLEquivalent tle) {
		owner.getDocHandler().addImplementer("Equivalent value: " + tle.getContext() + " = " + get(tle.getContext()));
	}

	private TLLibrary getOwnerLibrary() {
		AbstractLibrary tlLib = null;
		switch (type) {
		case EXAMPLE:
			tlLib = ((TLExampleOwner) tlOwner).getOwningLibrary();
			break;
		case EQUIVALENT:
			tlLib = ((TLEquivalentOwner) tlOwner).getOwningLibrary();
			break;
		}
		return tlLib instanceof TLLibrary ? (TLLibrary) tlLib : null;
	}

	private boolean confirmContextExists(String context) {
		TLLibrary tlLib = getOwnerLibrary();
		if (tlLib instanceof TLLibrary)
			for (TLContext ctx : tlLib.getContexts())
				if (ctx.getContextId().equals(context))
					return confirmContextExistsInController(context);
		return false;
	}

	private boolean confirmContextExistsInController(String context) {
		ContextController cc = OtmRegistry.getMainController().getContextController();
		return cc.getAvailableContextIds(owner.getLibrary()).contains(context);
	}

	@Override
	public String getContextID() {
		switch (type) {
		case EXAMPLE:
			return ((TLExampleOwner) tlOwner).getExamples().size() > 0 ? ((TLExampleOwner) tlOwner).getExamples()
					.get(0).getContext() : "";
		case EQUIVALENT:
			return ((TLEquivalentOwner) tlOwner).getEquivalents().size() > 0 ? ((TLEquivalentOwner) tlOwner)
					.getEquivalents().get(0).getContext() : "";
		}
		return "";
	}

	@Override
	public String getApplicationContext() {
		String appContext = "";
		TLContext ctx = null;
		if (getOwnerLibrary() instanceof TLLibrary)
			ctx = getOwnerLibrary().getContext(getContextID());
		if (ctx != null)
			appContext = ctx.getApplicationContext();
		return appContext;
	}

}

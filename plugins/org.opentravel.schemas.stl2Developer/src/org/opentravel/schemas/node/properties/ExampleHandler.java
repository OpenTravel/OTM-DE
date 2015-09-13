/**
 * 
 */
package org.opentravel.schemas.node.properties;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemas.controllers.ContextController;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Equivalent handler to assign ONLY to properties that can have equivalents. Owner must exist and have a
 * TLEquivalentOnwer source object. Equivalent data is stored in the TL model object.
 * 
 * @author Dave Hollander
 *
 */
public class ExampleHandler implements IValueWithContextHandler, ModelElementListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleHandler.class);

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		LOGGER.debug("ower event: " + event.getType());
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		LOGGER.debug("change event: " + event.getNewValue());
	}

	PropertyNode owner = null;
	TLExampleOwner tlOwner = null;
	ContextController cc = OtmRegistry.getMainController().getContextController();

	/**
	 * 
	 */
	public ExampleHandler(PropertyNode owner) {
		assert (owner != null);
		assert (owner.getTLModelObject() != null);
		assert (owner.getTLModelObject() instanceof TLEquivalentOwner);

		this.owner = owner;
		tlOwner = (TLExampleOwner) owner.getTLModelObject();
	}

	@Override
	public int getCount() {
		return tlOwner.getExamples().size();
	}

	@Override
	public String get(String context) {
		return tlOwner.getExample(context) != null ? tlOwner.getExample(context).getValue() : "";
	}

	@Override
	public boolean set(String value, String context) {
		if (!confirmContextExists(context))
			context = cc.getDefaultContextId(owner.getLibrary());

		if ((value == null || value.isEmpty()) && tlOwner.getExample(context) != null) {
			tlOwner.getExample(context).removeListener(this);
			tlOwner.removeExample(tlOwner.getExample(context));
		} else {
			if (tlOwner.getExample(context) != null) {
				tlOwner.getExample(context).setValue(value);
			} else {
				TLExample tle = new TLExample();
				tle.addListener(this);
				tle.setContext(context);
				tle.setValue(value);
				tlOwner.addExample(tle);
			}
			fix(context); // ONLY Allow 1 example per property!
		}
		return true;
	}

	@Override
	public boolean change(String sourceContext, String targetContext) {
		if (!confirmContextExists(targetContext))
			targetContext = cc.getDefaultContextId(owner.getLibrary());

		if (tlOwner.getExample(targetContext) != null)
			return false;
		if (tlOwner.getExample(sourceContext) != null) {
			tlOwner.getExample(sourceContext).setContext(targetContext);
			return true;
		}
		return false;
	}

	@Override
	public void fix(String sourceContext) {
		if (!confirmContextExists(sourceContext))
			sourceContext = cc.getDefaultContextId(owner.getLibrary());

		List<TLExample> toRemove = new ArrayList<TLExample>();
		for (TLExample tle : tlOwner.getExamples())
			if (!tle.getContext().equals(sourceContext)) {
				convertToDoc(tle);
				toRemove.add(tle);
			}
		for (TLExample tle : toRemove) {
			tle.removeListener(this);
			tlOwner.removeExample(tle);
		}
	}

	/**
	 * Add the equivalent value to the documentation.
	 */
	private void convertToDoc(TLExample tle) {
		owner.addImplementer("Example value: " + tle.getContext() + " = " + get(tle.getContext()));
	}

	@Override
	public boolean areValid() {
		for (TLExample tle : tlOwner.getExamples())
			if (!confirmContextExists(tle.getContext()))
				return false;
		return true;
	}

	private boolean confirmContextExists(String context) {
		if (tlOwner.getOwningLibrary() instanceof TLLibrary)
			for (TLContext ctx : ((TLLibrary) tlOwner.getOwningLibrary()).getContexts())
				if (ctx.getContextId().equals(context))
					return confirmContextExistsInController(context);
		return false;
	}

	private boolean confirmContextExistsInController(String context) {
		return cc.getAvailableContextIds(owner.getLibrary()).contains(context);
	}

}

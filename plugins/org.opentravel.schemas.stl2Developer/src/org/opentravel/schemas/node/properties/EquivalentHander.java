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
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemas.controllers.ContextController;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Equivalent handler to assign ONLY to properties that can have equivalents. Owner must exist and have a
 * TLEquivalentOnwer source object. Equivalent data is stored in the TL model object.
 * 
 * This handler only allows one equivalent on any property.
 * 
 * @author Dave Hollander
 *
 */
public class EquivalentHander implements IValueWithContextHandler, ModelElementListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(EquivalentHander.class);

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		LOGGER.debug("ower event: " + event.getType());
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		LOGGER.debug("change event: " + event.getNewValue());
	}

	PropertyNode owner = null;
	TLEquivalentOwner tlOwner = null;
	ContextController cc = OtmRegistry.getMainController().getContextController();

	/**
	 * 
	 */
	public EquivalentHander(PropertyNode owner) {
		assert (owner != null);
		assert (owner.getTLModelObject() != null);
		assert (owner.getTLModelObject() instanceof TLEquivalentOwner);

		this.owner = owner;
		tlOwner = (TLEquivalentOwner) owner.getTLModelObject();
	}

	@Override
	public int getCount() {
		return tlOwner.getEquivalents().size();
	}

	@Override
	public String get(String context) {
		return tlOwner.getEquivalent(context) != null ? tlOwner.getEquivalent(context).getDescription() : "";
	}

	@Override
	public boolean set(String value, String context) {
		if (!confirmContextExists(context))
			context = cc.getDefaultContextId(owner.getLibrary());

		if ((value == null || value.isEmpty()) && tlOwner.getEquivalent(context) != null) {
			tlOwner.getEquivalent(context).removeListener(this);
			tlOwner.removeEquivalent(tlOwner.getEquivalent(context));
		} else {
			if (tlOwner.getEquivalent(context) != null) {
				tlOwner.getEquivalent(context).setDescription(value);
			} else {
				TLEquivalent tle = new TLEquivalent();
				tle.addListener(this);
				tle.setContext(context);
				tle.setDescription(value);
				tlOwner.addEquivalent(tle);
			}
			fix(context); // ONLY ALLOW One Value!
		}
		return true;
	}

	@Override
	public boolean change(String sourceContext, String targetContext) {
		if (!confirmContextExists(targetContext))
			targetContext = cc.getDefaultContextId(owner.getLibrary());
		if (tlOwner.getEquivalent(targetContext) != null)
			return false;
		if (tlOwner.getEquivalent(sourceContext) != null) {
			tlOwner.getEquivalent(sourceContext).setContext(targetContext);
			return true;
		}
		return false;
	}

	@Override
	public void fix(String sourceContext) {
		List<TLEquivalent> toRemove = new ArrayList<TLEquivalent>();
		for (TLEquivalent tle : tlOwner.getEquivalents())
			if (!tle.getContext().equals(sourceContext)) {
				convertToDoc(tle);
				toRemove.add(tle);
			}
		for (TLEquivalent tle : toRemove) {
			tle.removeListener(this);
			tlOwner.removeEquivalent(tle);
		}
	}

	/**
	 * Add the equivalent value to the documentation.
	 */
	private void convertToDoc(TLEquivalent tle) {
		owner.addImplementer("Equivalent value: " + tle.getContext() + " = " + get(tle.getContext()));
	}

	@Override
	public boolean areValid() {
		for (TLEquivalent tle : tlOwner.getEquivalents())
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

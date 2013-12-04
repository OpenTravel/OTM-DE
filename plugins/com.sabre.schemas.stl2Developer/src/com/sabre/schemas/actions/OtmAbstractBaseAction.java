/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import java.util.concurrent.atomic.AtomicInteger;

import com.sabre.schemas.actions.IWithNodeAction.AbstractWithNodeAction;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.properties.PropertyType;
import com.sabre.schemas.properties.StringProperties;

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

    private void initialize(final StringProperties props) {
        this.setText(props.get(PropertyType.TEXT));
        this.setToolTipText(props.get(PropertyType.TOOLTIP));
        this.setImageDescriptor(Images.getImageRegistry().getDescriptor(
                props.get(PropertyType.IMAGE)));
        this.setId(this.getText() + ACTION_COUNTER.incrementAndGet());
        this.setEnabled(true);
    }

}

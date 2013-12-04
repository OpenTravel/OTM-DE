/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.views.propertyview;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

public interface PropertySetter {
    public IPropertyDescriptor getDescriptor();

    public void setValue(Object value);

    public Object getValue();

    public String getId();
}
/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.properties;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Agnieszka Janowska
 * 
 */
public class DefaultStringProperties implements StringProperties {

    private final Map<PropertyType, String> properties;

    public DefaultStringProperties(final Map<PropertyType, String> properties) {
        this.properties = new EnumMap<PropertyType, String>(properties);
    }

    /**
	 *
	 */
    public DefaultStringProperties() {
        properties = new EnumMap<PropertyType, String>(PropertyType.class);
    }

    @Override
    public String get(final PropertyType propType) {
        return properties.get(propType);
    }

    @Override
    public StringProperties set(final PropertyType propType, final String value) {
        properties.put(propType, value);
        return this;
    }
}

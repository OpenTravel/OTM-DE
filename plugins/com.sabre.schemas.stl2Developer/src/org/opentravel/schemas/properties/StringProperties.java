/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.properties;

/**
 * @author Agnieszka Janowska
 * 
 */
public interface StringProperties {

    StringProperties set(PropertyType propType, String value);

    String get(PropertyType propType);

}

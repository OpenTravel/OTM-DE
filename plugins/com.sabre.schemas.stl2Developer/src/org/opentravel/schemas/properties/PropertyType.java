/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.properties;

public enum PropertyType {

    TEXT("text"), TOOLTIP("tooltip"), IMAGE("image");

    private String propString;

    private PropertyType(final String propString) {
        this.propString = propString;
    }

    /**
     * @return the propString
     */
    public String getPropString() {
        return propString;
    }

    @Override
    public String toString() {
        return getPropString();
    }
}

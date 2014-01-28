/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node;

public enum SubType {
    // facet sub-types
    SUMMARY("Summary"), INFO("Info"), ROLES("Roles"), SIMPLE_FACET("SimpleFacet"), ID("ID"), DETAIL(
            "Detail"), QUERY("Query"), CUSTOM("Custom"),

    ATTRIBUTE("Attribute"), ELEMENT("Element"), INDICATOR("Indicator"),

    // message
    MESSAGE("Message"), OPERATION("Operation"), REQUEST("Request"), RESPONSE("Response"), NOTIFICATION(
            "Notification"),

    // term sub-types
    SIMPLE("Simple"), SIMPLE_XSD("SimpleXSD"), ENUMERATION("Enumeration"), ENUMERATION_WITH_DOC(
            "Enumeration With Doc"), VALUE_WITH_ATTRS("Value With Attributes"), CORE_OBJECT(
            "Core Object"), BUSINESS_OBJECT("Business Object"), SERVICE("Service"), UNKNOWN(
            "Unknown");

    private final String value;

    SubType(final String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SubType fromValue(final String v) {

        for (final SubType c : SubType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return SubType.UNKNOWN;
    }
}

/**
 * 
 */
package org.opentravel.schemas.node;

/**
 * @author Dave Hollander
 * 
 */
public enum ComponentNodeType {
    CLOSED_ENUM("Enumeration Closed"), ALIAS("Alias"), OPEN_ENUM("Enumeration Open"), VWA(
            "Value With Attributes"), CORE("Core Object"), BUSINESS("Business Object"), SERVICE(
            "Service"), MESSAGE("Message"), OPERATION("Operation"), REQUEST("Request"), RESPONSE(
            "Response"), NOTIFICATION("Notification"), EXTENSION_POINT("Extension Point Facet"), SIMPLE(
            "Simple Object");

    private String desc;

    ComponentNodeType(String name) {
        this.desc = name;
    }

    public String getDescription() {
        return desc;
    }

    public String value() {
        return desc;
    }

    @Override
    public String toString() {
        return desc;
    }

    public static ComponentNodeType fromString(String string) {
        for (ComponentNodeType type : ComponentNodeType.values()) {
            if (type.getDescription().equals(string))
                return type;
        }
        return null;
    }
}

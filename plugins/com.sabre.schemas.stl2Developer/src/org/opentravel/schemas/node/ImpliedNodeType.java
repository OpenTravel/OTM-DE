package org.opentravel.schemas.node;

import com.sabre.schemacompiler.model.LibraryMember;

public enum ImpliedNodeType {

    Empty("Undefined", ImpliedNode.Undefined), XSD_Atomic("XSD_Atomic", ImpliedNode.XSD_Atomic), UnassignedType(
            "Unassigned", ImpliedNode.missing), Duplicate("Duplicate", ImpliedNode.duplicate), Indicator(
            "Indicator", ImpliedNode.indicator), Union("Union", ImpliedNode.union), String(
            "DefaultString", ImpliedNode.defaultString);

    private String impliedNodeType;
    private LibraryMember impliedTLObject;

    private ImpliedNodeType(String impliedNodeType, LibraryMember tlObject) {
        this.impliedNodeType = impliedNodeType;
        this.impliedTLObject = tlObject;
    }

    /**
     * @return the impliedNodeType
     */
    public String getImpliedNodeType() {
        return impliedNodeType;
    }

    /**
     * @return the impliedTLObject
     */
    public LibraryMember getTlObject() {
        return impliedTLObject;
    }

}

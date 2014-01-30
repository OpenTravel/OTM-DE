
package org.opentravel.schemas.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.RenamableFacet;
import org.opentravel.schemas.node.controllers.NodeUtils;
import org.opentravel.schemas.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeTester extends PropertyTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeTester.class);

    public static final String IS_DELETEABLE = "isDeleteable";
    public static final String HAS_TYPE = "hasType";
    public static final String IS_IN_TLLIBRARY = "isInTLLibrary";
    public static final String IS_EDITABLE = "isEditable";
    public static final String IS_OWNER_LIBRARY_EDITABLE = "isOwnerLibraryEditable";
    public static final String CAN_ASSIGN = "canAssign";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        if (!(receiver instanceof Node)) {
            return false;
        }
        Node node = (Node) receiver;
        if (IS_DELETEABLE.equals(property)) {
            return canDelete(node);
        } else if (HAS_TYPE.equals(property)) {
            return hasType(node);
        } else if (IS_IN_TLLIBRARY.equals(property)) {
            return node.isInTLLibrary();
        } else if (IS_EDITABLE.equals(property)) {
            return node.isEditable();
        } else if (IS_OWNER_LIBRARY_EDITABLE.equals(property)) {
            return isOwnerLibraryEditable(node);
        } else if (CAN_ASSIGN.equals(property)) {
            if (args[0] instanceof Node)
                return canAssign(node, (Node) args[0]);
        }
        return false;
    }

    private boolean canDelete(Node node) {
        // we can not use isDeleteable in RenambleFacet because it prevents delete from code. Check
        // deleteVisitor.
        if (node instanceof RenamableFacet) {
            return !NodeUtils.checker(node).isInheritedFacet().get();
        } else {
            return node.isDeleteable();
        }
    }

    private boolean isOwnerLibraryEditable(Node node) {
        return node.getChain() != null ? node.getChain().isEditable() : node.isEditable();
    }

    private boolean hasType(Node node) {
        Type type = node.getTypeClass();
        if (type != null) {
            return type.getTypeNode() != null;
        }
        return false;
    }

    public boolean canAssign(Node property, Node type) {
        if (property == null || type == null)
            return false;
        return property.canAssign(type);
    }
}
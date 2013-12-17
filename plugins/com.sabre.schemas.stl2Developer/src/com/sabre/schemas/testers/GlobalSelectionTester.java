package com.sabre.schemas.testers;

import org.eclipse.core.expressions.PropertyTester;

import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeEditStatus;
import com.sabre.schemas.node.OperationNode;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.node.properties.SimpleAttributeNode;

public class GlobalSelectionTester extends PropertyTester {

    public static final String CANADD = "canAdd";
    private static final String NAMESPACE = "stl2Developer.selection";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (CANADD.equals(property)) {
            return canAdd((Node) receiver);
        }
        return false;
    }

    private boolean canAdd(Node node) {
        if (node == null || node.getParent() == null)
            return false;
        if (node.isDeleted() || !node.isEditable())
            return false;
        if (node instanceof ProjectNode)
            return false;
        if (node instanceof LibraryChainNode)
            return false;
        if (node instanceof OperationNode)
            return false;

        // Can we add children?
        if (node.isBusinessObject() || node.isCoreObject() || node.isExtensionPointFacet()) {
            return true; // always can have properties added.
        }
        if (node.isValueWithAttributes() || node.isEnumeration()) {
            if (node.getEditStatus().equals(NodeEditStatus.FULL)
                    || node.getEditStatus().equals(NodeEditStatus.MINOR))
                return true;
            else
                return false;
        }

        if (node.isMessage()) {
            if (node.getEditStatus().equals(NodeEditStatus.FULL))
                return true;
            else
                return false;
        }
        if (node instanceof ServiceNode)
            if (node.getEditStatus().equals(NodeEditStatus.FULL))
                return true;
            else
                return false;

        // Facets. Unless a simple facet, same as parent.
        if (node.isSimpleFacet())
            return false;
        if (node.isListFacet())
            return false;
        if (node.isRoleFacet() && !node.getEditStatus().equals(NodeEditStatus.FULL))
            return false;
        if (node.isFacet())
            return canAdd(node.getParent());

        // Can we add siblings?
        if (node instanceof SimpleAttributeNode)
            return false;
        if (node.isRoleProperty() && !node.getEditStatus().equals(NodeEditStatus.FULL))
            return false;
        if (node.isProperty())
            if (node == node.getOwningComponent())
                return false; // ERROR
            else
                return canAdd(node.getOwningComponent());

        return false;
    }

    /**
     * @return The fully qualified property name, NAMESPACE + property name.
     */
    public static String getFullName(String property) {
        return NAMESPACE + "." + property;
    }

}

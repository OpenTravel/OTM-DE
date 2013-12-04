package com.sabre.schemas.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.modelObject.TLEmpty;
import com.sabre.schemas.node.Node.NodeVisitor;
import com.sabre.schemas.node.properties.AttributeNode;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.IndicatorElementNode;
import com.sabre.schemas.node.properties.IndicatorNode;

/**
 * Node visitors for generic, commonly used functions.
 * 
 * Sample Usage: NodeVisitor visitor = new NodeVisitors().new validateNodeTypes();
 * curNode.visitAllNodes(visitor);
 * 
 * @author Dave Hollander
 * 
 */
public class NodeVisitors {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeVisitors.class);

    /**
     * Close this node. Do not change the model. Does delete type assignments. Does not delete
     * children nor change view contents. Use delete visitor if changes are to be made to the TL
     * model.
     * 
     * @author Dave Hollander
     * 
     */
    public class closeVisitor implements NodeVisitor {

        @Override
        public void visit(INode n) {
            // LOGGER.debug("CloseVisitor: closing " + n);
            Node node = (Node) n;

            // Type class - clear out the where used and type class
            if (node.getTypeClass() != null)
                node.getTypeClass().clear();

            // Use override behavior because Library nodes must clear out context.
            if (node instanceof LibraryNode) {
                ((LibraryNode) node).close(false); // just the library, not its members
            }

            // Unlink from tree
            node.deleted = true;
            if (node.getParent() != null && node.getParent().getChildren() != null) {
                node.getParent().getChildren().remove(node);
                if (node.getParent().isFamily())
                    node.getParent().updateFamily();
            }
            node.setParent(null);
            node.setLibrary(null);

            // LOGGER.debug("CloseVisitor: closed  " + n);
        }
    }

    /**
     * Delete this node and its and TL model. Does delete type assignments. Does not delete children
     * nor change view contents. Use close visitor if no changes are to be made to the TL model.
     * 
     * @author Dave Hollander
     * 
     */
    public class deleteVisitor implements NodeVisitor {

        @Override
        public void visit(INode n) {
            // LOGGER.debug("DeleteVisitor: deleting " + n);
            Node node = (Node) n;

            if (!node.isDeleteable())
                return;

            // Type class - delete the where used and assignments to this type
            if (node.isTypeProvider())
                node.getTypeClass().delete();

            // Remove from where used list
            if (node.isTypeUser()) {
                if (node.getTypeClass() != null && node.getTypeClass().getTypeNode() != null
                        && node.getTypeClass().getTypeNode().getTypeUsers() != null)
                    node.getTypeClass().getTypeNode().getTypeUsers().remove(node);
            }

            // Use override behavior because Library nodes must clear out
            // project and context.
            if (node instanceof LibraryNode) {
                ((LibraryNode) node).delete(false); // just the library, not its members
            }

            // Unlink from tree
            node.deleted = true;
            if (node.getParent() != null && node.getParent().getChildren() != null) {
                node.getParent().removeChild(node);
                // node.getParent().getChildren().remove(node);
                if (node.getParent().isFamily())
                    node.getParent().updateFamily();
            }
            node.setParent(null);
            node.setLibrary(null);

            // Remove the TL entity from the TL Model.
            if (node.modelObject != null) {
                node.modelObject.delete();
                node.modelObject = node.newModelObject(new TLEmpty());
            }

            // If this is in a chain, remove its associated version node.
            if (node.getVersionNode() != null) {
                node.getVersionNode().deleted = true;
                node.getVersionNode().head = null;
                node.getVersionNode().getParent().getChildren().remove(node.getVersionNode());
            }
            // LOGGER.debug("DeleteVisitor: deleted  " + n);
        }
    }

    /**
     * Assure node name conforms to the rules
     * 
     * @author Dave Hollander
     * 
     */
    public class FixNames implements NodeVisitor {

        @Override
        public void visit(INode in) {
            Node n = (Node) in;
            if (n instanceof ElementNode)
                n.setName(NodeNameUtils.fixElementName(n));
            else if (n instanceof AttributeNode)
                n.setName(NodeNameUtils.fixAttributeName(n.getName()));
            else if (n instanceof IndicatorNode)
                n.setName(NodeNameUtils.fixIndicatorName(n.getName()));
            else if (n.isSimpleType())
                n.setName(NodeNameUtils.fixSimpleTypeName(n.getName()));
            else if (n.isEnumeration())
                n.setName(NodeNameUtils.fixEnumerationName(n.getName()));
            else if (n.isValueWithAttributes())
                n.setName(NodeNameUtils.fixVWAName(n.getName()));
            else if (n.isCoreObject())
                n.setName(NodeNameUtils.fixCoreObjectName(n.getName()));
            else if (n.isBusinessObject())
                n.setName(NodeNameUtils.fixBusinessObjectName(n.getName()));
            else if (n.isAlias())
                n.setName(NodeNameUtils.adjustCaseOfName(PropertyNodeType.ALIAS, n.getName()));
            else if (n instanceof IndicatorElementNode)
                n.setName(NodeNameUtils.fixIndicatorElementName(n.getName()));
            else if (n.isID_Reference()) {
                n.setName(NodeNameUtils.fixIdReferenceName(n));
            }
        }

    }

}

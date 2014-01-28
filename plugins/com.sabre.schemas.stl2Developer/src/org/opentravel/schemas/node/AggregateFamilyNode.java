/**
 * 
 */
package org.opentravel.schemas.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregate Family Node groups types with the same name prefix under type aggregates
 * (simple/complex).
 * 
 * @author Dave Hollander
 * 
 */
public class AggregateFamilyNode extends FamilyNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(FamilyNode.class);

    /**
     * Create a family node for type aggregates. There are no back links in type aggregates.
     * 
     * @param n
     * @param peer
     */
    public AggregateFamilyNode(AggregateNode parent, Node n, Node peer) {
        super(NodeNameUtils.makeFamilyName(n.getName()), parent);
        setLibrary(n.getLibrary());

        getChildren().add(n);
        getChildren().add(peer);
        parent.getChildren().remove(peer);
    }

    protected void remove(Node node) {
        getChildren().remove(node);
        updateFamily();
    }

    @Override
    protected void updateFamily() {
        // If only one is left, move it up.
        final Node parent = getParent();
        if (getChildren().size() == 1) {
            final Node child = getChildren().get(0);
            parent.getChildren().add(child);
        }
        if (!parent.getChildren().remove(this))
            LOGGER.info("Error removing " + this.getName() + " from " + parent.getName());
    }
}

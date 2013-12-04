/**
 * 
 */
package com.sabre.schemas.node;

/**
 * The version aggregate node collects libraries that are in a chain. The library chain displays it
 * children which are Aggregate Node and a Version Aggregate Node.
 * 
 * Children this node are only allowed to be libraries.
 * 
 * @author Dave Hollander
 * 
 */
public class VersionAggregateNode extends AggregateNode {

    public VersionAggregateNode(AggregateType type, Node parent) {
        super(type, parent);
    }

    public void add(LibraryNode ln) {
        getChildren().add(ln);
        ln.getParent().getChildren().remove(ln);
        ln.setParent(this);
    }

    public void add(Node n) {
        throw (new IllegalStateException("Version aggregates can not contain "
                + n.getClass().getSimpleName()));
    }
}

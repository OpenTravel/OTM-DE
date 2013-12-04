/**
 * 
 */
package com.sabre.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.sabre.schemas.properties.Images;

/**
 * Aggregate nodes are navigation nodes that collect types from all libraries in a version chain.
 * 
 * @author Dave Hollander
 * 
 */
public class AggregateNode extends NavNode {
    private AggregateType type;

    public enum AggregateType {
        SimpleTypes("Simple Objects"), ComplexTypes("Complex Objects"), Service("Service"), Versions(
                "Versions");
        private final String label;

        private AggregateType(String label) {
            this.label = label;
        }

        private String label() {
            return label;
        }
    }

    public AggregateNode(AggregateType type, Node parent) {
        super(type.label(), parent);
        this.type = type;
        setLibrary(parent.getLibrary());
    }

    /**
     * Adds node to the aggregate node's children list if appropriate.
     * 
     * @param node
     * @return
     */
    public boolean add(ComponentNode node) {
        // Type safety
        switch (type) {
            case ComplexTypes:
                if (!(node instanceof ComplexComponentInterface))
                    throw new IllegalStateException("Can't add to complex aggregate.");
                break;
            case SimpleTypes:
                if (!(node instanceof SimpleComponentInterface))
                    throw new IllegalStateException("Can't add to simple aggregate.");
                break;
            case Service:
                if (!(node instanceof ServiceNode || (node instanceof OperationNode)))
                    throw new IllegalStateException("Can't add to service aggregate.");
                break;
            default:
                throw new IllegalStateException("Unknown object type: "
                        + node.getClass().getSimpleName());
        }

        // Add if not found or replacing the existing node is older?
        boolean toBeAdded = true;
        int doFamily = 0;
        String familyName = NodeNameUtils.makeFamilyName(node.getName());
        for (Node n : getChildren()) {
            if (n.getName().startsWith(familyName))
                doFamily++;

            if (n.getName().equals(node.getName())) {
                // Is it "later-in-time" than the one found?
                if (node.getLibrary().getTLaLib().isLaterVersion(n.getLibrary().getTLaLib())) {
                    getChildren().remove(n);
                    toBeAdded = true;
                    doFamily--;
                } else
                    toBeAdded = false;
                break;
            }
        }

        // Handle families
        if (toBeAdded)
            if (doFamily > 0)
                addToFamily(node);
            else
                getChildren().add(node);
        return toBeAdded;
    }

    private void addToFamily(ComponentNode node) {
        String familyName = NodeNameUtils.makeFamilyName(node.getName());
        List<Node> kids = new ArrayList<Node>(getChildren());
        for (Node n : kids) {
            if ((n instanceof AggregateFamilyNode) && n.getFamily().equals(familyName))
                // add to existing family
                n.getChildren().add(node);
            else if (n.getFamily().equals(familyName))
                // create new family
                new AggregateFamilyNode(this, node, n);
        }
    }

    protected void remove(Node node) {
        if (!getChildren().remove(node)) {
            // if it was not found, it must be in a family node
            ArrayList<Node> kids = new ArrayList<Node>(getChildren());
            for (Node n : kids) {
                if ((n instanceof AggregateFamilyNode) && (n.family.equals(node.family))) {
                    ((AggregateFamilyNode) n).remove(node);
                }
            }
        }
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get("aggregateFolder");
    }

    /*
     * (non-Javadoc) // * @see com.sabre.schemas.node.Node#getLibrary() //
     */
    @Override
    public LibraryNode getLibrary() {
        return parent.getLibrary();
    }

    /*
     * For the non-version aggregates, skip over the version node These are used in the navigator
     * menul.
     */
    @Override
    public List<Node> getNavChildren() {
        if (type.equals(AggregateType.Versions)) {
            return super.getChildren();
        } else {
            ArrayList<Node> kids = new ArrayList<Node>();
            for (Node child : getChildren()) {
                if (child instanceof VersionNode)
                    kids.add(((VersionNode) child).getHead());
            }
            return kids;
        }
    }

    /**
     * To get all providers, we only want to get type providers from their original libraries. If we
     * used the other aggregates, the earlier versions would not be found. If we include them types
     * in the other aggregates would be duplicates.
     */
    @Override
    public boolean hasChildren_TypeProviders() {
        return type.equals(AggregateType.Versions) && getChildren().size() > 0 ? true : false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isEditable()
     */
    @Override
    public boolean isEditable() {
        if (getParent() != null)
            return getParent().isEditable();
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isInTLLibrary()
     */
    @Override
    public boolean isInTLLibrary() {
        return parent.isInTLLibrary();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NavNode#isLibraryContainer()
     */
    @Override
    public boolean isLibraryContainer() {
        return type == AggregateType.Versions ? true : false;
    }

}

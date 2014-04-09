/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.properties.Images;

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
        String familyName = NodeNameUtils.makeFamilyName(node.getName());
        for (Node n : getChildren()) {
            if (!n.isFamily() && n.getName().equals(node.getName())) {
                // Is it "later-in-time" than the one found?
                if (node.getLibrary().getTLaLib().isLaterVersion(n.getLibrary().getTLaLib())) {
                    getChildren().remove(n);
                    insertPreviousVersion(node, (ComponentNode) n);
                    return true;
                } else {
                    return false;
                }
            }
        }
        AggregateFamilyNode family = findFamilyNode(getChildren(), familyName);
        List<Node> nodes = findPrefixedNodes(getChildren(), familyName + "_");
        if (!nodes.isEmpty()) {
            if (family == null) {
                family = new AggregateFamilyNode(this, familyName);
            }
            List<Node> kids = new ArrayList<Node>(nodes);
            for (Node n : kids) {
                getChildren().remove(n);
                family.getChildren().add(n);
            }
        }
        if (family != null) {
            family.getChildren().add(node);
        } else {
            getChildren().add(node);
        }
        return true;
    }

    private AggregateFamilyNode findFamilyNode(List<Node> children, String familyName) {
        for (Node child : children) {
            if (child instanceof AggregateFamilyNode) {
                if (familyName.equals(child.getName())) {
                    return (AggregateFamilyNode) child;
                }
            }
        }
        return null;
    }

    private List<Node> findPrefixedNodes(List<Node> children, String prefix) {
        List<Node> ret = new ArrayList<Node>();
        for (Node c : children) {
            if (c.getName().startsWith(prefix)) {
                ret.add(c);
            }
        }
        return ret;
    }

    // Insert node in versions list.
    // Update all the newest object links.
    private void insertPreviousVersion(ComponentNode newest, ComponentNode toBePlaced) {
        toBePlaced.getVersionNode().setNewestVersion(newest);
        if (toBePlaced.getVersionNode().getPreviousVersion() == null) {
            newest.getVersionNode().setPreviousVersion(toBePlaced);
            return;
        }

        toBePlaced.getVersionNode().setNewestVersion(newest);
        VersionNode toBePlacedVN = toBePlaced.getVersionNode();
        ComponentNode n = toBePlacedVN.getPreviousVersion();
        while (n != null) {
            n.getVersionNode().setNewestVersion(newest);
            if (toBePlaced.getLibrary().getTLaLib().isLaterVersion(n.getLibrary().getTLaLib())) {
                n.getVersionNode().setPreviousVersion(toBePlaced);
                toBePlacedVN.setPreviousVersion(n.getVersionNode().getPreviousVersion());
                n = toBePlaced;
            }
            n = n.getVersionNode().getPreviousVersion();
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
     * (non-Javadoc) // * @see org.opentravel.schemas.node.Node#getLibrary() //
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
                    kids.add(((VersionNode) child).getNewestVersion());
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
     * @see org.opentravel.schemas.node.Node#isEditable()
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
     * @see org.opentravel.schemas.node.Node#isInTLLibrary()
     */
    @Override
    public boolean isInTLLibrary() {
        return parent.isInTLLibrary();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NavNode#isLibraryContainer()
     */
    @Override
    public boolean isLibraryContainer() {
        return type == AggregateType.Versions ? true : false;
    }

}

package com.sabre.schemas.node;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.sabre.schemas.properties.Images;

/**
 * 
 * Version nodes are used in the Versions aggregate to isolate actual component nodes from their
 * parent library. For libraries that are part of a chain, all links to component nodes will be
 * through a version node. For the non-version aggregate nodes, the links are directly to the most
 * current component node.
 * 
 * @author Dave Hollander
 * 
 */
public class VersionNode extends ComponentNode {
    

    protected ComponentNode head; // link to the latest/newest version of this object
    protected ComponentNode prevVersion; // link to the preceding version. If null, it is new to the
                                         // chain.

    /**
     * Creates the version node and inserts into the library before the passed node. This does NOT
     * place this node into the Aggregates. Set previous version to null (new to chain).
     */
    public VersionNode(ComponentNode node) {
        super(node.getTLModelObject());
        getChildren().add(node);
        head = node;
        prevVersion = null;
        node.setVersionNode(this);

        if (node.getLibrary() == null)
            throw new IllegalStateException("Version Head library is null.");
        setLibrary(node.getLibrary());

        // Insert this between parent and node.
        if (node.getParent() == null)
            throw new IllegalStateException("Version node - " + node + " parent is null.");
        setParent(node.getParent());
        node.getParent().getChildren().remove(node);
        node.getParent().getChildren().add(this);
        node.setParent(this);
    }

    /**
     * Return the actual node wrapped by this version node.
     * 
     * @return
     */
    public Node getVersionedObject() {
        return getChildren().get(0);
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.libraryChain);
        // return head.getImage();
    }

    // TESTING ONLY
    // @Override
    // public List<Node> getChildren() {
    // return super.getChildren();
    // }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#hasChildren_TypeProviders()
     */
    @Override
    public boolean hasChildren_TypeProviders() {
        // Type providers are delivered from their version nodes.
        return head != null;
    }

    // NOTE - this gives the desired result.
    // Version nodes that are also the latest do not have children while clicking on one that has
    // been extended will show the base type.
    @Override
    public boolean hasNavChildrenWithProperties() {
        return false;
    }
    
    @Override
    public List<Node> getNavChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean hasNavChildren() {
        return false;
    }

    @Override
    public boolean isTypeProvider() {
        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    /**
     * @return the newest version of the object (version head).
     */
    public Node getNewestVersion() {
        return head;
    }

    public void setNewestVersion(ComponentNode head) {
        this.head = head;
    }

    /**
     * @return the newest version of the object (version head).
     */
    public ComponentNode getPreviousVersion() {
        return prevVersion;
    }

    public void setPreviousVersion(ComponentNode previous) {
        this.prevVersion = previous;
    }
}

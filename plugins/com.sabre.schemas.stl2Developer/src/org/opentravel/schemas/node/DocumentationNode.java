/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.controllers.NodeImageProvider;
import org.opentravel.schemas.node.controllers.NodeLabelProvider;
import org.opentravel.schemas.node.controllers.NodeModelController;
import org.opentravel.schemas.node.controllers.NodeValueController;
import org.opentravel.schemas.node.controllers.NullDocItemNodeModelController;
import org.opentravel.schemas.node.controllers.NullNodeImageProvider;
import org.opentravel.schemas.node.controllers.NullNodeLabelProvider;
import org.opentravel.schemas.node.controllers.NullNodeValueController;

import org.opentravel.schemacompiler.model.TLDocumentationItem;

/**
 * @author Agnieszka Janowska
 * 
 */
public class DocumentationNode {
    public static enum DocumentationNodeType {
        DOCUMENTATION_TYPE_ROOT, DOCUMENTATION_ITEM, OTHER_ITEM;
    }

    private TLDocumentationItem tlDocItem;
    private DocumentationNode parent;
    private List<DocumentationNode> children = new LinkedList<DocumentationNode>();
    private NodeLabelProvider nodeLabelProvider = new NullNodeLabelProvider();
    private NodeImageProvider nodeImageProvider = new NullNodeImageProvider();
    private NodeValueController nodeValueController = new NullNodeValueController();
    private NodeModelController<TLDocumentationItem> nodeModelController = new NullDocItemNodeModelController();
    private DocumentationNodeType type = DocumentationNodeType.DOCUMENTATION_ITEM;

    public DocumentationNode() {
    }

    public DocumentationNode(TLDocumentationItem docItem) {
        this.setDocItem(docItem);
    }

    public void setLabelProvider(NodeLabelProvider nodeLabelProvider) {
        this.nodeLabelProvider = nodeLabelProvider;
    }

    public void setImageProvider(NodeImageProvider nodeImageProvider) {
        this.nodeImageProvider = nodeImageProvider;
    }

    public void setModelController(NodeModelController<TLDocumentationItem> nodeModelController) {
        this.nodeModelController = nodeModelController;
    }

    public NodeModelController<TLDocumentationItem> getModelController() {
        return nodeModelController;
    }

    public void setValue(String object) {
        nodeValueController.setValue(object);
    }

    public String getValue() {
        return nodeValueController.getValue();
    }

    public Image getImage() {
        return nodeImageProvider.getImage();
    }

    public void setValueController(NodeValueController nodeValuesController) {
        this.nodeValueController = nodeValuesController;
    }

    public void addChild(DocumentationNode child) {
        children.add(child);
        child.setParent(this);
    }

    public void addChild(int index, DocumentationNode child) {
        children.add(index, child);
        child.setParent(this);
    }

    public void removeChild(DocumentationNode child) {
        children.remove(child);
        child.setParent(null);
    }

    public List<DocumentationNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    // labels are stored with the provider on node creation.
    public String getLabel() {
        return nodeLabelProvider.getLabel();
    }

    public DocumentationNodeType getType() {
        return type;
    }

    public void setType(DocumentationNodeType type) {
        this.type = type;
    }

    public boolean isTypeRoot() {
        return DocumentationNodeType.DOCUMENTATION_TYPE_ROOT.equals(this.type);
    }

    public boolean isDocItem() {
        return DocumentationNodeType.DOCUMENTATION_ITEM.equals(this.type);
    }

    /**
     * @return the parent
     */
    public DocumentationNode getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(DocumentationNode parent) {
        this.parent = parent;
    }

    public DocumentationNode getOwningTypeRoot() {
        if (isTypeRoot()) {
            return this;
        } else if (getParent() != null) {
            return getParent().getOwningTypeRoot();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (!isDocItem()) {
            final int prime = 31;
            int result = 1;
            int labelHash = 0;
            if (nodeLabelProvider != null && nodeLabelProvider.getLabel() != null) {
                labelHash = nodeLabelProvider.getLabel().hashCode();
            }
            result = prime * result + labelHash;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }
        return super.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DocumentationNode other = (DocumentationNode) obj;
        if (getType() == null) {
            if (other.getType() != null)
                return false;
        } else if (!getType().equals(other.getType())) {
            return false;
        }
        if (getLabel() == null) {
            if (other.getLabel() != null) {
                return false;
            }
        } else if (!getLabel().equals(other.getLabel())) {
            return false;
        }
        if (!parentsEqual(getParent(), other.getParent()))
            return false;
        if (getDocItem() == null) {
            if (other.getDocItem() != null) {
                return false;
            }
        } else if (!getDocItem().equals(other.getDocItem())) {
            return false;
        }

        return true;
    }

    private boolean parentsEqual(DocumentationNode thisParent, DocumentationNode otherParent) {
        if (thisParent == null) {
            if (otherParent != null) {
                return false;
            }
        } else if (!thisParent.equals(otherParent)) {
            return false;
        }
        return true;
    }

    /**
     * @return the tlDocItem
     */
    public TLDocumentationItem getDocItem() {
        return tlDocItem;
    }

    /**
     * @param tlDocItem
     *            the tlDocItem to set
     */
    public void setDocItem(TLDocumentationItem docItem) {
        this.tlDocItem = docItem;
    }

}

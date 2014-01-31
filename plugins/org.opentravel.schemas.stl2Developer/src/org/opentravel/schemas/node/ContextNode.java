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
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.controllers.NodeImageProvider;
import org.opentravel.schemas.node.controllers.NodeLabelProvider;
import org.opentravel.schemas.node.controllers.NodeModelController;
import org.opentravel.schemas.node.controllers.NodeValueController;
import org.opentravel.schemas.node.controllers.NullContextNodeModelController;
import org.opentravel.schemas.node.controllers.NullNodeImageProvider;
import org.opentravel.schemas.node.controllers.NullNodeLabelProvider;
import org.opentravel.schemas.node.controllers.NullNodeValueController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.TLContext;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ContextNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextNode.class);

    public static enum ContextNodeType {
        LIBRARY_ROOT, CONTEXT_ITEM, OTHER_ITEM;
    }

    private TLContext modelObject;
    private ContextNode parent;
    private LibraryNode libraryNode = null; // Link to the node tree for this context

    private List<ContextNode> children = new ArrayList<ContextNode>();
    // private List<ContextNode> children = new LinkedList<ContextNode>();
    private NodeLabelProvider nodeLabelProvider = new NullNodeLabelProvider();
    private NodeImageProvider nodeImageProvider = new NullNodeImageProvider();
    private NodeValueController appContextController = new NullNodeValueController();
    private NodeValueController contextIdController = new NullNodeValueController();
    private NodeValueController descriptionController = new NullNodeValueController();
    private NodeModelController<TLContext> nodeModelController = new NullContextNodeModelController();
    private ContextNodeType type = ContextNodeType.CONTEXT_ITEM;

    public ContextNode() {
    }

    public ContextNode(TLContext modelObject) {
        this.setModelObject(modelObject);
    }

    public void setLabelProvider(NodeLabelProvider nodeLabelProvider) {
        this.nodeLabelProvider = nodeLabelProvider;
    }

    public void setImageProvider(NodeImageProvider nodeImageProvider) {
        this.nodeImageProvider = nodeImageProvider;
    }

    public void setModelController(NodeModelController<TLContext> nodeModelController) {
        this.nodeModelController = nodeModelController;
    }

    public NodeModelController<TLContext> getModelController() {
        return nodeModelController;
    }

    public void setApplicationContext(String object) {
        appContextController.setValue(object);
    }

    public String getApplicationContext() {
        return appContextController.getValue();
    }

    public void setContextId(String object) {
        contextIdController.setValue(object);
    }

    public void setDescription(String object) {
        descriptionController.setValue(object);
    }

    public String getDescription() {
        return descriptionController.getValue();
    }

    public String getContextId() {
        return contextIdController.getValue();
    }

    public Image getImage() {
        return nodeImageProvider.getImage();
    }

    public void setApplicationContextController(NodeValueController nodeValuesController) {
        this.appContextController = nodeValuesController;
    }

    public NodeValueController getContextIdController() {
        return contextIdController;
    }

    public void setContextIdController(NodeValueController contextIdController) {
        this.contextIdController = contextIdController;
    }

    public NodeValueController getDescriptionController() {
        return descriptionController;
    }

    public void setDescriptionController(NodeValueController descriptionController) {
        this.descriptionController = descriptionController;
    }

    public void addChild(ContextNode child) {
        children.add(child);
        child.setParent(this);
    }

    public void addChild(int index, ContextNode child) {
        children.add(index, child);
        child.setParent(this);
    }

    public void removeChild(ContextNode child) {
        // Context node equality based on label, change the label to assure it is unique
        if (child.isLibraryRoot() && child.contextIdController instanceof NullNodeValueController) {
            // if this is null, the value will be null and therefore equal.
            // Must manually remove the item.
            List<ContextNode> newChildren = new ArrayList<ContextNode>();
            for (ContextNode c : children) {
                if (c.getLibraryNode() != child.getLibraryNode())
                    newChildren.add(c);
            }
            children = newChildren;
        } else {
            child.setContextId("___ToBeRemoved___");
            children.remove(child);
            child.setParent(null);
        }
    }

    public void removeChildren() {
        List<ContextNode> kids = new ArrayList<ContextNode>(getChildren());
        for (ContextNode kid : kids) {
            if (kid.children.size() > 0) {
                kid.removeChildren();
                // LOGGER.debug("removed child: " + getLabel());
            }
            children.remove(kid);
            kid.parent = null;
        }
    }

    public List<ContextNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public String getLabel() {
        return nodeLabelProvider.getLabel();
    }

    public ContextNodeType getType() {
        return type;
    }

    public void setType(ContextNodeType type) {
        this.type = type;
    }

    public boolean isLibraryRoot() {
        return ContextNodeType.LIBRARY_ROOT.equals(this.type);
    }

    public boolean isContextItem() {
        return ContextNodeType.CONTEXT_ITEM.equals(this.type);
    }

    /**
     * @return the parent
     */
    public ContextNode getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(ContextNode parent) {
        this.parent = parent;
    }

    public ContextNode getOwningLibraryRoot() {
        if (isLibraryRoot()) {
            return this;
        } else if (getParent() != null) {
            return getParent().getOwningLibraryRoot();
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
        if (!isContextItem()) {
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
        ContextNode other = (ContextNode) obj;
        if (!this.isContextItem() && !other.isContextItem()) {
            if (getLabel() == null) {
                if (other.getLabel() != null) {
                    return false;
                }
            } else if (!getLabel().equals(other.getLabel())) {
                return false;
            }
            return true;
        }
        return super.equals(obj);
    }

    /**
     * @return the modelObject
     */
    public TLContext getModelObject() {
        return modelObject;
    }

    /**
     * @param modelObject
     *            the modelObject to set
     */
    public void setModelObject(TLContext modelObject) {
        this.modelObject = modelObject;
    }

    public void setLibraryNode(LibraryNode ln) {
        libraryNode = ln;
    }

    public LibraryNode getLibraryNode() {
        return libraryNode;
    }

}

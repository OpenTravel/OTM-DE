/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLAbstractFacet;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAliasOwner;
import com.sabre.schemacompiler.model.TLModelElement;

/**
 * Aliases are displayed as properties but are assignable as type references. They provide an
 * alternate name for their parentNode facet or business object
 * 
 * @author Dave Hollander
 * 
 */
public class AliasNode extends ComponentNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(AliasNode.class);

    public AliasNode() {
        super();
    }

    public AliasNode(final TLModelElement obj) {
        super(obj);
    }

    /**
     * Add a new alias to a core or business object parent.
     * 
     * @param parent
     * @param tlObj
     */
    public AliasNode(final Node parent, final TLAlias tlObj) {
        this(tlObj);
        if (parent != null) {
            parent.linkChild(this, false); // link without doing family tests.
            setLibrary(parent.getLibrary());
            if (parent instanceof BusinessObjectNode || parent instanceof CoreObjectNode) {
                parent.getModelObject().addAlias(tlObj);
                createChildrenAliases(parent, tlObj);
            }
        }
    }

    /**
     * Create a new alias complete with new TL model and link to parentNode
     * 
     * @param parentNode
     * @param en
     */
    public AliasNode(final Node parent, final String name) {
        // Do not use the parent form of this constructor. The alias must be named before children
        // are created.
        this(new TLAlias());
        setName(name);
        ((TLAlias) getTLModelObject()).setName(name);
        parent.linkChild(this, false); // link without doing family tests.
        setLibrary(parent.getLibrary());
        if (parent instanceof BusinessObjectNode || parent instanceof CoreObjectNode) {
            parent.getModelObject().addAlias((TLAlias) getTLModelObject());
            createChildrenAliases(parent, (TLAlias) getTLModelObject());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#delete()
     */
    @Override
    public void delete() {
        deleteAliasList visitor = new deleteAliasList();
        touchSiblingAliases(visitor);
        for (AliasNode n : visitor.getToBeDeleted())
            n.superDelete();
    }

    private void superDelete() {
        super.delete();
    }

    private void createChildrenAliases(Node owner, TLAlias tla) {
        if (owner == null)
            return;

        for (Node n : owner.getChildren()) {
            if (n instanceof FacetNode)
                ((FacetNode) n).updateAliasNodes();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#getOwningComponent()
     */
    @Override
    public Node getOwningComponent() {
        return getParent() != null ? getParent().getOwningComponent() : this;
    }

    @Override
    public String getPropertyRole() {
        return "Alias";
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.Alias);
    }

    @Override
    public boolean isFacetAlias() {
        final Object model = modelObject.getTLModelObj();
        if (model instanceof TLAlias) {
            final TLAliasOwner owner = ((TLAlias) model).getOwningEntity();
            if (owner == null || owner instanceof TLAbstractFacet) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAssignable() {
        return getParent() != null ? getParent().isAssignable() : false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isAssignedByReference()
     */
    @Override
    public boolean isAssignedByReference() {
        return getParent() != null ? getParent().isAssignedByReference() : false;
    }

    @Override
    public boolean isSimpleAssignable() {
        return getParent() != null ? getParent().isSimpleAssignable() : false;
    }

    @Override
    public String getComponentType() {
        return "Alias: " + getName();
    }

    @Override
    public List<Node> getNavChildren() {
        return null;
    }

    @Override
    public boolean hasNavChildren() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#setName(java.lang.String)
     */
    @Override
    public void setName(String n) {
        if (getParent() == null)
            return; // happens during initial node creation
        if (!(getParent() instanceof ComplexComponentInterface)) {
            LOGGER.warn("Can't set name unless the parent is the component, not a facet. "
                    + getName());
            return;
        }
        touchSiblingAliases(new renameAlias(this, n));
    }

    // Why is this here? I think it will always give same result as getOwningComponent(). Only used
    // for delete and setName.
    private ComponentNode findOwningComponent() {
        ComponentNode component = (ComponentNode) getParent();
        if (component instanceof ComplexComponentInterface)
            return component;

        component = (ComponentNode) getParent().getParent();
        if (component instanceof ComplexComponentInterface)
            return component;
        else
            return null;
    }

    /**
     * Execute visitor on this node and all others with the same name root under the parent
     * component.
     * 
     * @param visitor
     * @param a
     */
    private void touchSiblingAliases(Visitor visitor) {
        // find parent component.
        String thisAlias = getName();
        Node component = findOwningComponent();
        List<Node> peers = findOwningComponent().getDescendants();
        String rootAlias = "";
        for (Node node : peers) {
            if (node.isAlias() && node.getParent() == findOwningComponent()) {
                if (thisAlias.startsWith(node.getName())) {
                    rootAlias = node.getName();
                }
            }
        }
        visitor.visit(this);
        for (Node peer : peers) {
            if (peer.isAlias() && peer != this) {
                if (peer.getName().startsWith(rootAlias)) {
                    visitor.visit(peer);
                }
            }
        }
    }

    private interface Visitor {
        public void visit(Node n);
    }

    /**
     * Visitor class that creates list of aliases.
     * 
     * @author Dave Hollander
     * 
     */
    private class deleteAliasList implements Visitor {
        List<AliasNode> toBeDeleted = new ArrayList<AliasNode>();

        /**
         * @return the toBeDeleted
         */
        public List<AliasNode> getToBeDeleted() {
            return toBeDeleted;
        }

        @Override
        public void visit(Node n) {
            if (n.isAlias())
                toBeDeleted.add((AliasNode) n);
        }

    }

    private class renameAlias implements Visitor {
        String newName = "";
        String aliasName = "";

        public renameAlias(AliasNode leadAlias, String newName) {
            this.newName = newName;
            this.aliasName = leadAlias.getName();
        }

        @Override
        public void visit(Node n) {
            if (n.getModelObject() == null)
                throw new IllegalStateException("Model Object on " + getName() + " is null.");

            String remainder = n.getName().substring(aliasName.length());
            String fullNewName = newName;
            if (remainder.length() > 0)
                fullNewName = newName + remainder;
            n.getModelObject().setName(fullNewName);
            for (Node user : n.getTypeUsers()) {
                user.setName(fullNewName);
            }
        }
    }

}

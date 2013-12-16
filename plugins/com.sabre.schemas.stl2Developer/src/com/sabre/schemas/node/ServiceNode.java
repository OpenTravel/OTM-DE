/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.node;

import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemas.node.OperationNode.ResourceOperationTypes;
import com.sabre.schemas.properties.Images;

/**
 * @author Dave Hollander
 * 
 */
public class ServiceNode extends ComponentNode {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceNode.class);

    public ServiceNode(final TLService tlSvc, LibraryNode ln) {
        super(tlSvc);
        addMOChildren();

        if (ln == null)
            throw new IllegalArgumentException("Null library for the service.");

        // Make sure the library only has one service.
        final TLLibrary tlLib = (TLLibrary) ln.getTLaLib();
        if (ln.getServiceRoot() != null) {
            ln.getServiceRoot().delete();
        }
        if (tlLib.getService() != tlSvc) {
            tlLib.setService(tlSvc);
        }

        if (tlSvc.getName() == null || tlSvc.getName().isEmpty())
            setName(ln.getName() + "_Service");
        ln.getChildren().add(this);
        setParent(ln);
        setLibrary(ln);
        ln.setServiceRoot(this);

        if (!(tlSvc instanceof TLService))
            throw new IllegalArgumentException("Invalid object to create service from.");
    }

    /**
     * Create a new service (node and underlying TL model). Note: If the passed node's library does
     * not have a service, the service is linked into the library.
     * 
     * @param n
     *            node to get the library from. If it is a business object, CRUD operations will be
     *            create with it as the subject.
     */
    public ServiceNode(final Node n) {
        this(new TLService(), n.getLibrary());
        setDescription(n.getDescription());
        setName(n.getName(), false);
        addCRUDQ_Operations(n);
        // If a chain, the wrap the service in a version and add to chain aggregate.
        if (n.getLibrary().isInChain())
            n.getLibrary().getChain().add(this);
    }

    /**
     * Add CRUDQ operations to service. Set message elements to subject. Query operations are made
     * for each query facet.
     * 
     * @param nodeInterface
     */
    public void addCRUDQ_Operations(Node subject) {
        if (!subject.isBusinessObject())
            return;
        BusinessObjectNode bo = (BusinessObjectNode) subject;
        for (ResourceOperationTypes op : ResourceOperationTypes.values())
            if (!op.equals(ResourceOperationTypes.QUERY))
                new OperationNode(this, op.displayName, op, subject);
        for (Node n : bo.getQueryFacets())
            new OperationNode(this, n.getLabel(), ResourceOperationTypes.QUERY, subject);
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.Service);
    }

    @Override
    public String getLabel() {
        return "Service: " + getName();
    }

    @Override
    public boolean isService() {
        return true;
    }

    @Override
    public boolean isAssignable() {
        return false;
    }

    @Override
    public boolean isTypeProvider() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.INode#hasChildren_TypeProviders()
     */
    @Override
    public boolean hasChildren_TypeProviders() {
        // enable if we want to have messages assignable as types.
        return false;
    }

}

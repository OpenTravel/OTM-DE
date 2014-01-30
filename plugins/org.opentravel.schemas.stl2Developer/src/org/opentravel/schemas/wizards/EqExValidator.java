/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.wizards;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.Messages;

/**
 * @author Agnieszka Janowska
 * 
 */
public class EqExValidator implements FormValidator {

    private final Node parentNode;
    private final NewPropertiesWizard wizard;

    public EqExValidator(final Node parent, final NewPropertiesWizard wizard) {
        parentNode = parent;
        this.wizard = wizard;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.wizards.FormValidator#validate()
     */
    @Override
    public void validate() throws ValidationException {
        final PropertyNode n = wizard.getSelectedNode();
        if (n == null) {
            return;
        }
        n.setLibrary(parentNode.getLibrary());
        if (n.getName() == null || n.getName().isEmpty()) {
            throw new ValidationException(Messages.getString("error.newPropertyName"));
        }
        if (n.getPropertyType() == null || n.getPropertyType() == PropertyNodeType.UNKNOWN) {
            throw new ValidationException(Messages.getString("error.newPropertyType") + " ("
                    + n.getName() + ")");
        }
        // if (n.getModelObject() == null || n.getModelObject().getTLType() == null) {
        // if (n.getPropertyType() == PropertyNodeType.ATTRIBUTE || n.getPropertyType() ==
        // PropertyNodeType.ELEMENT) {
        // throw new ValidationException(Messages.getString("error.newPropertyTLType") + " (" +
        // n.getName() + ")");
        // }
        // }
        if (!parentNode.isUnique(n)) {
            throw new ValidationException(Messages.getString("error.newProperty"));
        }
    }

}

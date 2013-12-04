/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.properties.Messages;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewPropertyValidator implements FormValidator {

    private final Node parentNode;
    private final NewPropertiesWizard wizard;

    public NewPropertyValidator(final Node parent, final NewPropertiesWizard wizard) {
        parentNode = parent;
        this.wizard = wizard;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.wizards.FormValidator#validate()
     */
    @Override
    public void validate() throws ValidationException {
        final PropertyNode n = wizard.getSelectedNode();
        if (n == null) {
            return;
        }
        // TODO validators should NOT set anything!
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

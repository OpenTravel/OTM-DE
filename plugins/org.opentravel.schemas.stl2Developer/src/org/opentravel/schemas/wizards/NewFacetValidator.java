
package org.opentravel.schemas.wizards;

import org.opentravel.schemas.node.EditNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Messages;

import org.opentravel.schemacompiler.model.TLFacetType;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewFacetValidator implements FormValidator {

    private final Node componentNode;
    private final NewFacetWizard wizard;
    private final TLFacetType facetType;

    public NewFacetValidator(final Node componentNode, final TLFacetType facetType,
            final NewFacetWizard wizard) {
        this.componentNode = componentNode;
        this.facetType = facetType;
        this.wizard = wizard;
    }

    @Override
    public void validate() throws ValidationException {
        final EditNode n = new EditNode(wizard.getName());
        n.setLibrary(componentNode.getLibrary());
        if (!componentNode.isFacetUnique(n)) {
            throw new ValidationException(Messages.getString("error.newFacet"));
        }
        if (TLFacetType.CUSTOM.equals(facetType) && isEmpty(wizard.getName())) {
            throw new ValidationException(Messages.getString("error.newFacet.custom.empty"));
        }
    }

    private boolean isEmpty(String name) {
        if (name != null)
            return name.isEmpty();
        return true;
    }

}

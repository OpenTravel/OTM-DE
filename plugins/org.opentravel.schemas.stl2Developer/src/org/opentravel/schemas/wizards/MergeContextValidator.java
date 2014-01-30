
package org.opentravel.schemas.wizards;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemas.node.ContextNode;
import org.opentravel.schemas.node.Node;

/**
 * @author Dave Hollander
 * 
 */
public class MergeContextValidator implements FormValidator {

    private final ContextNode context;
    private final MergeContextNodeWizard wizard;

    public MergeContextValidator(final ContextNode context, final MergeContextNodeWizard wizard) {
        this.context = context;
        this.wizard = wizard;
    }

    /**
     * Check to see if anything that uses the context also uses the merge into context.
     */
    @Override
    public void validate() throws ValidationException {
        ContextNode mergeIntoContext = wizard.getContext();
        Node node = context.getLibraryNode();
        List<String> usedContexts = new ArrayList<String>(node.getContextIds());
        if (usedContexts.contains(mergeIntoContext.getContextId()))
            throw new ValidationException("Validation results: Conflict!");
    }
}

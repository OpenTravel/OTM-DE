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

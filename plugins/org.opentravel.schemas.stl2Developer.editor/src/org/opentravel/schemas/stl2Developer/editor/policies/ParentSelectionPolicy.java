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
package org.opentravel.schemas.stl2Developer.editor.policies;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editpolicies.SelectionEditPolicy;
import org.opentravel.schemas.stl2Developer.editor.parts.DiagramEditPart;

/**
 * In order to get top-level element (children of Diagram) this policy should be installed in
 * EditPart. After that each getTargetEditPart
 * 
 * @author Pawel Jedruch
 * 
 */
public class ParentSelectionPolicy extends SelectionEditPolicy {

    @Override
    public EditPart getTargetEditPart(Request request) {
        return getParentEP(getHost());
    }

    private EditPart getParentEP(EditPart ep) {
        while (!(ep.getParent() instanceof DiagramEditPart)) {
            ep = ep.getParent();
        }
        return ep;
    }

    @Override
    protected void hideSelection() {
    }

    @Override
    protected void showSelection() {
    }

}
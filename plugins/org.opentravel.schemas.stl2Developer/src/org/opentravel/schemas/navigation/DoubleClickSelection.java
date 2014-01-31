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
package org.opentravel.schemas.navigation;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.opentravel.schemas.views.TypeView;

/**
 * This class is used as marked to notify the {@link ISelectionListener} about selection changed
 * caused by double-click.
 * 
 * Example usage:
 * 
 * <pre>
 * 
 * &#064;Override
 * public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 *     if (selection instanceof DoubleClickSelection) {
 *         // doubleClick
 *     } else {
 *         // default
 *     }
 * }
 * </pre>
 * 
 * @see TypeView#selectionChanged(org.eclipse.ui.IWorkbenchPart,
 *      org.eclipse.jface.viewers.ISelection)
 * @author Pawel Jedruch
 * 
 */
public class DoubleClickSelection extends StructuredSelection {

    public DoubleClickSelection(StructuredSelection selection) {
        super(selection.toList());
    }

}
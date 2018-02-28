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
package org.opentravel.schemas.stl2Developer.editor.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;

/**
 * @author Pawel Jedruch
 * 
 */
public class GEFUtils {

	@SuppressWarnings("unchecked")
	public static <T> List<T> extractModels(List<EditPart> selection, Class<T> clazz) {
		List<T> nodes = new ArrayList<T>(selection.size());
		for (EditPart ep : selection) {
			Object model = ep.getModel();
			if (clazz.isAssignableFrom(model.getClass())) {
				nodes.add((T) model);
			}
		}
		return nodes;
	}

	public static EditPart getEditPartToSelect(EditPart ep) {
		SelectionRequest req = new SelectionRequest();
		req.setType(RequestConstants.REQ_SELECTION);
		return ep != null ? ep.getTargetEditPart(req) : null;
	}

}

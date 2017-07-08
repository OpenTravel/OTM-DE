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
package org.opentravel.schemas.trees.REST;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.opentravel.schemas.node.resources.ActionFacet;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ActionRequest;
import org.opentravel.schemas.node.resources.ActionResponse;
import org.opentravel.schemas.node.resources.InheritedResourceMember;
import org.opentravel.schemas.node.resources.ParamGroup;

/**
 * Provide sort order for Rest Tree nodes.
 * 
 * @author Dave
 *
 */
// 8/1/2015 dmh - Since 3.2 It is recommended to use ViewerComparator instead of ViewerSorter.
public class RestTreeComparator extends ViewerComparator {
	@Override
	public int category(Object element) {
		if (element instanceof ParamGroup)
			return 10;
		else if (element instanceof ActionFacet)
			return 20;
		else if (element instanceof ActionNode)
			return 30;
		else if (element instanceof ActionRequest)
			return 40;
		else if (element instanceof ActionResponse)
			return 50;
		else if (element instanceof InheritedResourceMember)
			return category(((InheritedResourceMember) element).get()) + 2;
		return super.category(element);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);

		if (cat1 != cat2) {
			return cat1 - cat2;
		}

		// Make requests first (greater than response)
		if (e1 instanceof ActionRequest)
			return 1;

		return super.compare(viewer, e1, e2);
	}
}
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

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;

/**
 * Provides names and images for the REST resource tree.
 */
public class RestTreeStyledLabelProvider extends LabelProvider implements IStyledLabelProvider {

	@Override
	public String getText(final Object element) {
		if (element instanceof ResourceMemberInterface) {
			return ((ResourceMemberInterface) element).getName();
		}
		return "Unknown object type";
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof ResourceMemberInterface) {
			return ((ResourceMemberInterface) element).getImage();
		}
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString(getText(element));
	}

}

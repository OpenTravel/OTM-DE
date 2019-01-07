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
package org.opentravel.schemas.trees.type;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;

/**
 * @deprecated 4/26/2012 - dmh - this is no longer used (I think) - see LibraryTreeLabelProvider
 * 
 *             1/7/2019 - added asserts
 * 
 * @author Dave Hollander
 * 
 *         to do - use styled provider as explained in
 *         http://www.vogella.de/articles/EclipseJFaceTree/article.html#example
 * 
 */
@Deprecated
public class TypeTreeLabelProvider extends LabelProvider {

	@Override
	public Image getImage(final Object element) {
		assert false;
		return ((Node) element).getImage();
	}

	@Override
	public String getText(final Object element) {
		assert false;
		return ((INode) element).getName();
	}

}

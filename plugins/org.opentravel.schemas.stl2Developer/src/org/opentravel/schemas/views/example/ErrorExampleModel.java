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
package org.opentravel.schemas.views.example;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Images;

/**
 * May 30, 2018 - examples removed from OTM-DE
 * 
 * @author Pawel Jedruch
 * 
 */
@Deprecated
public class ErrorExampleModel extends ExampleModel {

	/**
	 * @param child
	 */
	public ErrorExampleModel(Node child) {
		super(child);
		this.setLabelProvider(new LabelProvider() {

			@Override
			public Image getImage(Object element) {
				return Images.getImageRegistry().get(Images.Error);
			}

			@Override
			public String getText(Object element) {
				return getNode().getName();
			}

		});
	}

}

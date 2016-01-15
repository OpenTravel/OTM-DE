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
package org.opentravel.schemas.node.resources;

import java.util.ArrayList;
import java.util.Collection;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.listeners.INodeListener;
import org.opentravel.schemas.node.listeners.ListenerFactory;

/**
 * Base generic class for all resource nodes EXCEPT ResourceNode which is a full node. Provides alterative to
 * ModelObjects used in full nodes. Includes some utility classes for matching TL enumerations to GUI strings.
 * 
 * @author Dave
 *
 * @param <TL>
 *            - the matching TLModel elements
 */
public abstract class ResourceBase<TL> extends Node implements ResourceMemberInterface {
	/**
	 * Set the object, add a listener and add children.
	 * 
	 * @param obj
	 */
	public ResourceBase(TL obj) {
		this.tlObj = obj;
		if (tlObj instanceof TLModelElement)
			ListenerFactory.setListner(this);
		addChildren();
	}

	public static String[] getHttpMethodStrings() {
		int i = 0;
		String[] values = new String[TLHttpMethod.values().length];
		for (TLHttpMethod l : TLHttpMethod.values())
			values[i++] = l.toString();
		return values;

	}

	public static String[] getMimeTypeStrings() {
		int i = 0;
		String[] values = new String[TLMimeType.values().length];
		for (TLMimeType l : TLMimeType.values())
			values[i++] = l.toString();
		return values;
	}

	/**
	 * @return - string array with all possible locations
	 */
	public static String[] getParamLocations() {
		int i = 0;
		String[] values = new String[TLParamLocation.values().length];
		for (TLParamLocation l : TLParamLocation.values())
			values[i++] = l.toString();
		return values;
	}

	public static String[] getReferenceTypeStrings() {
		int i = 0;
		String[] values = new String[TLReferenceType.values().length];
		for (TLReferenceType l : TLReferenceType.values())
			values[i++] = l.toString();
		return values;
	}

	protected TL tlObj;

	public void addChildren() {
	}

	@Override
	public String getDescription() {
		return (tlObj instanceof TLDocumentationOwner) && ((TLDocumentationOwner) tlObj).getDocumentation() != null ? ((TLDocumentationOwner) tlObj)
				.getDocumentation().getDescription() : "";
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public ResourceNode getOwningComponent() {
		Node node = this;
		while (!(node instanceof ResourceNode && node != null))
			node = node.getParent();
		return (ResourceNode) node;
	}

	@Override
	public TLModelElement getTLModelObject() {
		return (TLModelElement) tlObj;
	}

	@Override
	public ValidationFindings getValidationFindings() {
		return TLModelCompileValidator.validateModelElement((TLModelElement) tlObj);
	}

	@Override
	public Collection<String> getValidationMessages() {
		ValidationFindings findings = TLModelCompileValidator.validateModelElement((TLModelElement) tlObj, false);
		ArrayList<String> msgs = new ArrayList<String>();
		for (String f : findings.getValidationMessages(FindingType.ERROR, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
			msgs.add(f);
		for (String f : findings.getValidationMessages(FindingType.WARNING, FindingMessageFormat.MESSAGE_ONLY_FORMAT))
			msgs.add(f);
		return msgs;
	}

	@Override
	public boolean hasNavChildren() {
		return !getChildren().isEmpty();
	}

	@Override
	public boolean isValid() {
		// Set false when checking ONLY this object and its children
		return TLModelCompileValidator.validateModelElement((TLModelElement) tlObj, false).count(FindingType.ERROR) == 0;
		// return TLModelCompileValidator.validateModelElement((TLModelElement) tlObj).count(FindingType.ERROR) == 0;
	}

	@Override
	public boolean isValid_NoWarnings() {
		return TLModelCompileValidator.validateModelElement((TLModelElement) tlObj, false).count(FindingType.WARNING) == 0;
	}

	@Override
	public void setDescription(final String description) {
		if (tlObj instanceof TLDocumentationOwner) {
			TLDocumentationOwner docOwner = (TLDocumentationOwner) tlObj;
			TLDocumentation doc = docOwner.getDocumentation();
			if (doc == null) {
				doc = new TLDocumentation();
				docOwner.setDocumentation(doc);
			}
			doc.setDescription(description);
		}
	}

	protected void clearListeners() {
		if (tlObj instanceof TLModelElement) {
			ArrayList<ModelElementListener> listeners = new ArrayList<ModelElementListener>(
					((TLModelElement) tlObj).getListeners());
			for (ModelElementListener l : listeners)
				if (l instanceof INodeListener)
					((TLModelElement) tlObj).removeListener(l);
		}
	}

}

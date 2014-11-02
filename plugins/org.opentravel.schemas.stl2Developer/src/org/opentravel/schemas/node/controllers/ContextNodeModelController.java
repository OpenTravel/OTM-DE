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
package org.opentravel.schemas.node.controllers;

import java.util.List;

import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ContextNodeModelController implements NodeModelController<TLContext> {
	/**
     * 
     */
	private final TLLibrary library;

	/**
	 * @param library
	 */
	public ContextNodeModelController(TLLibrary library) {
		if (library == null)
			throw new IllegalStateException("Null library when creating a context node controller.");
		this.library = library;
	}

	@Override
	public TLContext createChild() {
		TLContext context = new TLContext();
		library.addContext(context);
		return context;
	}

	@Override
	public void removeChild(TLContext child) {
		library.removeContext(child);
	}

	@Override
	public List<TLContext> getChildren() {
		return library.getContexts();
	}

	@Override
	public TLContext getChild(int index) {
		throw new UnsupportedOperationException("Cannot retrieve context by index");
	}

	@Override
	public void moveChildUp(TLContext child) {
	}

	@Override
	public void moveChildDown(TLContext child) {
	}

	@Override
	public TLContext getChild(Object key) {
		return library.getContext(key.toString());
	}
}

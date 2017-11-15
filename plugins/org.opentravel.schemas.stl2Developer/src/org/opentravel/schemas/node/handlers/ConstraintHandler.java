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
package org.opentravel.schemas.node.handlers;

import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle constraints on simple types.
 * 
 * @author Dave Hollander
 * 
 */

public class ConstraintHandler {
	static final Logger LOGGER = LoggerFactory.getLogger(ConstraintHandler.class);

	TLSimple tlObj = null;
	Node owner = null;

	/**
	 * Create a handler for this object
	 * 
	 * @param tlSimple
	 *            is the tl object with constraints
	 * @param node
	 *            owning node used for set permissions
	 */
	public ConstraintHandler(TLSimple tlSimple, Node node) {
		tlObj = tlSimple;
		owner = node;
	}

	public int getMaxLen() {
		return tlObj.getMaxLength();
	}

	public int getMinLen() {
		return tlObj.getMinLength();
	}

	public String getPattern() {
		return tlObj.getPattern();
	}

	public int getFractionDigits() {
		return tlObj.getFractionDigits();
	}

	public int getTotalDigits() {
		return tlObj.getTotalDigits();
	}

	public String getMinInclusive() {
		// LOGGER.debug("get Min Inclusive: " + tlObj.getMinInclusive());
		return tlObj.getMinInclusive();
	}

	public String getMaxInclusive() {
		return tlObj.getMaxInclusive();
	}

	public String getMinExclusive() {
		return tlObj.getMinExclusive();
	}

	public String getMaxExclusive() {
		return tlObj.getMaxExclusive();
	}

	public void setPattern(String pattern) {
		if (pattern.isEmpty())
			pattern = null;
		if (owner.isEditable_newToChain() && tlObj != null) {
			tlObj.setPattern(pattern);
		}
	}

	public void setMinLength(final int length) {
		if (owner.isEditable_newToChain() && tlObj != null) {
			tlObj.setMinLength(length);
		}
	}

	public void setMaxLength(final int length) {
		if (owner.isEditable_newToChain() && tlObj != null) {
			tlObj.setMaxLength(length);
		}
	}

	public void setFractionDigits(final int length) {
		if (owner.isEditable_newToChain() && tlObj != null) {
			tlObj.setFractionDigits(length);
		}
	}

	public void setTotalDigits(final int length) {
		if (owner.isEditable_newToChain() && tlObj != null) {
			tlObj.setTotalDigits(length);
		}
	}

	public void setMinInclusive(String value) {
		if (value.isEmpty())
			value = null;
		if (owner.isEditable_newToChain() && tlObj != null) {
			tlObj.setMinInclusive(value);
			// LOGGER.debug("Set min inclusive to: " + value);
		}
	}

	public void setMaxInclusive(String value) {
		if (value.isEmpty())
			value = null;
		if (owner.isEditable_newToChain() && tlObj != null) {
			tlObj.setMaxInclusive(value);
		}
	}

	public void setMinExclusive(String value) {
		if (value.isEmpty())
			value = null;
		if (owner.isEditable_newToChain() && tlObj != null) {
			tlObj.setMinExclusive(value);
		}
	}

	public void setMaxExclusive(String value) {
		if (value.isEmpty())
			value = null;
		if (owner.isEditable_newToChain() && tlObj != null) {
			tlObj.setMaxExclusive(value);
		}
	}

}

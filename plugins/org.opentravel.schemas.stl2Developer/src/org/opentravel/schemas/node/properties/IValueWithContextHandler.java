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
package org.opentravel.schemas.node.properties;

/**
 * Interface values that are associated with a context.
 * 
 * @author Dave
 *
 */
public interface IValueWithContextHandler {

	/**
	 * Get count of all
	 */
	public int getCount();

	/**
	 * Get value for a context.
	 * 
	 * @param context
	 *            The context used to select which value. Default context used if context is null.
	 * @return value for the effective context. Empty string if no value is defined for the context.
	 */
	public String get(String context);

	public String getContextID();

	public String getApplicationContext();

	/**
	 * Set value for a specific context.
	 * 
	 * @param value
	 *            Value to be set. Value will overwrite existing value if one exists for that context. If value is null
	 *            or empty the value for the context will be removed.
	 * @param context
	 *            If context is null or not defined for the owners library then the default context will be used.
	 */
	public void set(String value, String context);

	// /**
	// * Change the context string for the value specified by context.
	// *
	// * @return Return false and do nothing if no value is found with source context, the target context is already
	// * assigned, or the target context is not defined.
	// */
	// public boolean change(String sourceContext, String targetContext);

	/**
	 * Fix all contexts. TLLibraries may have multiple values (loaded from old libraries). This method will collapse
	 * them down to the value associated with the passed context. The value with the source context will remain if it
	 * exists. All other values will be converted into documentation.
	 */
	public void fix(String sourceContext);

	// /**
	// * Test all values and insure all contexts are declared.
	// */
	// public boolean areValid();
}

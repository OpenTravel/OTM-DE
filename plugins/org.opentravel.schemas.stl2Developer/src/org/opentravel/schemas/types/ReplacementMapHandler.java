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
/**
 * 
 */
package org.opentravel.schemas.types;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemas.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a map of type providers indexed by name and object type. The map contains all the type-provider descendants
 * (NOT the provider because the provider may have a name collision with its default facet.)
 * <p>
 * The map is used by passing a type provider to the get() method and it will return with the closest matching type
 * provider from the map. If no provider is found, the replacement is returned.
 * 
 * @author Dave Hollander
 * 
 */
public class ReplacementMapHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReplacementMapHandler.class);

	private final TypeProvider replacement;
	java.util.HashMap<String, TypeProvider> replacementTypes = new java.util.HashMap<>();

	/*********************************************************************************
	 * 
	 * Create a where replacement map handler for this type provider and its descendants.
	 */
	public ReplacementMapHandler(TypeProvider replacement) {
		this.replacement = replacement;
		buildMap(replacement);
		// LOGGER.debug("Built a replacement map for " + replacement);
	}

	/**
	 * Create map of replacement candidates being all descendants of the replacement
	 */
	private void buildMap(TypeProvider replacement) {

		for (TypeProvider r : ((Node) replacement).getDescendants_TypeProviders()) {
			replacementTypes.put(r.getName(), r);
			if (r.getTLModelObject() instanceof TLFacet) {
				QName sen = XsdCodegenUtils.getSubstitutableElementName((TLFacet) r.getTLModelObject());
				if (!sen.getLocalPart().equals(r.getName())) {
					// happens with contextual facets
					// LOGGER.debug("Adding substitutable name to map : " + sen.getLocalPart() + " for " + r.getName());
					replacementTypes.put(sen.getLocalPart(), r);
				}
			}
		}
	}

	public TypeProvider get(TypeProvider child) {
		TypeProvider newProvider = replacementTypes.get(child.getName());
		// Replacement may have different structure so default to using the base object.
		if (newProvider == null) {
			// LOGGER.debug("ReplaceAll equivalent not found for " + child.getName() + " using " + replacement);
			newProvider = replacement;
		}

		return newProvider;
	}

}

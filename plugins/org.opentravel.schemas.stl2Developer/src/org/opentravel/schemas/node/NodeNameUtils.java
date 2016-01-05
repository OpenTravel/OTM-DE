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
package org.opentravel.schemas.node;

import java.util.Collection;
import java.util.EnumSet;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemas.modelObject.XsdModelingUtils;
import org.opentravel.schemas.node.NodeVisitors.FixNames;
import org.opentravel.schemas.node.properties.ElementReferenceNode;

/**
 * Static utilities for manipulating the names of various objects.
 * 
 * @author Dave Hollander
 * 
 */
public class NodeNameUtils {
	public static final String IndicatorSuffix = "Ind";
	public static final String IndicatorBannedPrefix = "is";
	public static final String SIMPLE_LIST_SUFFIX = "Simple_List";
	public static final String DETAIL_LIST_SUFFIX = "Detail_List";
	private static final String UNASIGNED_NAME = Node.UNDEFINED_PROPERTY_TXT;
	public static final String ENUM_PREFIX = "Enum_";
	private static final String ID_RERFERENCE_SUFFIX = "Ref";
	public static final String SIMPLE_SUFFIX = "_Simple";

	private static final FixNames FIXNAME_INSTANCE = new NodeVisitors().new FixNames();;

	/**
	 * Change the initial letter case as needed for the type of property.
	 * 
	 * @param type
	 * @param nameToChange
	 * @return
	 */
	public static String adjustCaseOfName(final PropertyNodeType type, String nameToChange) {
		if (nameToChange != null && !nameToChange.isEmpty()) {
			if (lowerCasePropertyTypes().contains(type)) {
				final char[] stringArray = nameToChange.toCharArray();
				stringArray[0] = Character.toLowerCase(stringArray[0]);
				nameToChange = new String(stringArray);
			} else {
				final char[] stringArray = nameToChange.toCharArray();
				stringArray[0] = Character.toUpperCase(stringArray[0]);
				nameToChange = new String(stringArray);
			}
		}
		return nameToChange;
	}

	/**
	 * Assure name conforms to the rules for attributes.
	 * 
	 * @param name
	 * @return
	 */
	public static String fixAttributeName(String name) {
		stripTypeSuffix(name);
		return adjustCaseOfName(PropertyNodeType.ATTRIBUTE, name);
	}

	/**
	 * Make sure a business object name conforms to the rules.
	 */
	public static String fixBusinessObjectName(String name) {
		return toInitialCap((stripTypeSuffix(name)));
	}

	/**
	 * Assure a complex type name conforms with the rules.
	 */
	public static String fixComplexTypeName(String name) {
		return toInitialCap((stripTypeSuffix(name)));
	}

	/**
	 * Make sure a core object name conforms to the rules. Strip of "Type" suffix.
	 * 
	 * @param name
	 * @return
	 */
	public static String fixCoreObjectName(String name) {
		return toInitialCap((stripTypeSuffix(name)));
	}

	// /**
	// * Strip off the automatically supplied suffix
	// *
	// * @param node
	// */
	// @Deprecated
	// public static void removeIndicatorSuffix(Node node) {
	// if (node instanceof IndicatorNode) {
	// if (node.getName().endsWith(NodeNameUtils.IndicatorSuffix))
	// node.setName(node.getName().substring(0, node.getName().length() - 3));
	// }
	// }

	/**
	 * Get the correct element name based on the type assigned to the node. If the type will be assigned in the XSD
	 * schema by type reference, then the global element name must be used. The codeGenUtils know which types are done
	 * by reference.
	 * 
	 * If no type is assigned, then the name will be adjusted simply by assuring capitalization.
	 * 
	 * @param n
	 * @return string containing adjusted element name.
	 */

	public static String fixElementName(Node n) {
		String name = null;
		QName qName = null;
		if (n.getTLTypeObject() != null) {
			// Type by reference to XML elements must use element name.
			qName = PropertyCodegenUtils.getDefaultXmlElementName(n.getTLTypeObject(),
					n instanceof ElementReferenceNode);
			if (qName != null)
				name = qName.getLocalPart();
		}
		if (qName == null) {
			// Is a simple or unassigned type. Use their name or suggest one.
			name = n.getName();
			if (name.isEmpty() && n.getTypeNode() == null) {
				name = UNASIGNED_NAME;
			} else {
				if (n.getTypeNode() != null && n.getTypeNode().isSimpleListFacet()) {
					name = n.getTypeNode().getName();
					name = makePlural(stripUnderScore(stripSuffix(name, SIMPLE_LIST_SUFFIX)));
				}
			}
			if (n instanceof ElementReferenceNode)
				if (n.getTypeNode() != null)
					name = n.getTypeNode().getName() + ID_RERFERENCE_SUFFIX;
				else
					name = ID_RERFERENCE_SUFFIX;

			name = adjustCaseOfName(PropertyNodeType.ELEMENT, name);
		}
		return name != null ? name : UNASIGNED_NAME;
	}

	/**
	 * Fix an XSD type name. Remove "type" or "Type" and assure initial cap DO NOT USE when a type has been assigned.
	 * Use {@link #fixElementName(Node)} instead.
	 * 
	 * @param name
	 *            - an xsd type QName
	 * @return - an OTM conforming QName
	 */
	public static String fixElementName(String name) {
		if (name == null || name.isEmpty())
			name = UNASIGNED_NAME;
		name = adjustCaseOfName(PropertyNodeType.ELEMENT, name);
		return name;
	}

	public static String fixElementRefName(String name) {
		if (name == null || name.isEmpty())
			name = UNASIGNED_NAME;
		if (!name.endsWith(ID_RERFERENCE_SUFFIX))
			name = name + ID_RERFERENCE_SUFFIX;
		name = adjustCaseOfName(PropertyNodeType.ELEMENT, name);
		return name;
	}

	/**
	 * Make sure an enumeration conforms to the rules.
	 */
	public static String fixEnumerationName(String name) {
		return name;
		// 12/7/2015 - return (name.startsWith(ENUM_PREFIX)) ? name : ENUM_PREFIX + name;
	}

	/**
	 * Make sure an enumeration value conforms to the rules.
	 */
	public static String fixEnumerationValue(String value) {
		return value;
	}

	public static String fixFacetName(String name) {
		return toInitialCap((stripTypeSuffix(name)));
	}

	public static String fixIdReferenceName(Node n) {
		if (!(n instanceof ElementReferenceNode)) {
			return "";
		}
		return fixElementName(n);
	}

	/**
	 * Assure the indicator element name has the proper suffix, not the banned prefix (is) and initial cap.
	 * 
	 * @param name
	 * @return
	 */
	public static String fixIndicatorElementName(String name) {
		if (name.startsWith(IndicatorBannedPrefix))
			name = name.substring(IndicatorBannedPrefix.length());
		name = adjustCaseOfName(PropertyNodeType.INDICATOR_ELEMENT, name);
		return name.endsWith(IndicatorSuffix) ? name : name.concat(IndicatorSuffix);
	}

	/**
	 * Assure the indicator name has the proper suffix, not the banned prefix (is) and initial lower case.
	 * 
	 * @param name
	 * @return
	 */
	public static String fixIndicatorName(String name) {
		if (name.startsWith(IndicatorBannedPrefix))
			name = name.substring(IndicatorBannedPrefix.length());
		name = adjustCaseOfName(PropertyNodeType.INDICATOR, name);
		return name.endsWith(IndicatorSuffix) ? name : name.concat(IndicatorSuffix);
	}

	/**
	 * Call {@link FixNames} visitor for this node all his children
	 */
	public static void fixName(Node node) {
		node.visitAllNodes(FIXNAME_INSTANCE);
	}

	/**
	 * Assure a simple type name conforms with the rules.
	 */
	public static String fixSimpleTypeName(String name) {
		return toInitialCap((stripTypeSuffix(name)));
	}

	/**
	 * Make sure a Value With Attributes name conforms to the rules. Strip of "Type" suffix.
	 * 
	 * @param name
	 * @return
	 */
	public static String fixVWAName(String name) {
		return toInitialCap((stripTypeSuffix(name)));
	}

	/**
	 * Parse the name at the first underscore "_" and return the second token
	 * 
	 * @return the portion after the underscore, the whole name if no underscore or an empty string
	 */
	public static String getGivenName(final String name) {
		if (name == null || name.isEmpty()) {
			return "";
		}
		final int i = name.indexOf("_") + 1;
		return i > 0 ? name.substring(i) : name;
	}

	/**
	 * Parse the name at the last underscore "_" and return the second token
	 * 
	 * @return the portion after the underscore, the whole name if no underscore or an empty string
	 */
	public static String getNameSuffix(final String name) {
		if (name == null || name.isEmpty()) {
			return "";
		}
		final int i = name.lastIndexOf("_") + 1;
		return i > 0 ? name.substring(i) : name;
	}

	/**
	 * Function to Parse the name at the first underscore "_" and return the first token
	 * 
	 * @return the portion before the underscore or an empty string
	 */
	public static String makeFamilyName(final String name) {
		if (name == null || name.isEmpty()) {
			return "";
		}
		final int i = name.indexOf("_");
		return i > 0 ? name.substring(0, i) : name;
	}

	/**
	 * Split a string looking like {urn:scim:schemas:core:1.0}Status as created by qName.toString() back into a qname.
	 * If no namespace, the chameleon namespace constant used by the compiler is used.
	 * 
	 * @param qname
	 * @return
	 */
	public static QName qnameFromString(String qname) {
		int startBrace = qname.indexOf("{");
		int endBrace = qname.indexOf("}");
		String localPart = qname.substring(endBrace + 1);
		String namespace = "";
		if (startBrace != 0 || endBrace < startBrace) {
			// Without braces assume there is no namespace (chameleon)
			namespace = XsdModelingUtils.ChameleonNS;
		} else
			namespace = qname.substring(startBrace + 1, endBrace);
		return new QName(namespace, localPart);
	}

	public static String stipSimpleSuffix(String name) {
		if (name == null || name.isEmpty())
			return "";
		if (name.endsWith(SIMPLE_SUFFIX))
			name = name.substring(0, name.indexOf(SIMPLE_SUFFIX));
		return name;
	}

	/**
	 * Remove the name of the object and facet type from the facet node's name. Return to suffix.
	 * 
	 * @param fn
	 * @param newName
	 *            use this name instead of name from FacetNode. Used for rename.
	 * @return
	 */
	public static String stripFacetPrefix(FacetNode fn, String newName) {
		String name = fn.getName();
		if (newName != null) {
			name = newName;
		}
		String parent = fn.getOwningComponent().getName();
		if (name.startsWith(parent))
			name = name.substring(parent.length());
		if (name.startsWith("_"))
			name = name.substring(1);
		if (fn.isQueryFacet()) {
			String facetType = fn.getFacetType().getIdentityName();
			if (name.startsWith(facetType))
				name = name.substring(facetType.length());
			if (name.startsWith("_"))
				name = name.substring(1);
		}
		return name;
	}

	public static String stripIndicatorSuffix(String name) {
		if (name.endsWith(NodeNameUtils.IndicatorSuffix))
			return (name.substring(0, name.length() - 3));
		else
			return name;
	}

	public static String stripTypeSuffix(String name) {
		return name;
		// if (name == null || name.isEmpty())
		// return "";
		// if (name.endsWith("Type"))
		// name = name.substring(0, name.indexOf("Type"));
		// if (name.endsWith("type"))
		// name = name.substring(0, name.indexOf("type"));
		// if (name.endsWith("_"))
		// name = name.substring(0, name.length() - 1);
		// return name;

	}

	private static Collection<PropertyNodeType> lowerCasePropertyTypes() {
		return EnumSet.of(PropertyNodeType.ATTRIBUTE, PropertyNodeType.INDICATOR, PropertyNodeType.ID);
	}

	private static String makePlural(String string) {
		if (string.endsWith("s"))
			return string + "es";
		return string + "s";
	}

	private static String stripSuffix(String str, String prefix) {
		if (str != null && str.endsWith(prefix))
			return str.substring(0, str.indexOf(prefix));
		else
			return str;
	}

	private static String stripUnderScore(String name) {
		return stripSuffix(name, "_");
	}

	private static String toInitialCap(String name) {
		String firstChar = name.substring(0, 1);
		String rest = (String) name.subSequence(1, name.length());
		return firstChar.toUpperCase().concat(rest);
	}

	public NodeNameUtils() {
	}

}

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
package org.opentravel.schemas.utils;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.XsdNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Any;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.AttributeGroupRef;
import org.w3._2001.xmlschema.ComplexType;
import org.w3._2001.xmlschema.Documentation;
import org.w3._2001.xmlschema.ExplicitGroup;
import org.w3._2001.xmlschema.ExtensionType;
import org.w3._2001.xmlschema.Facet;
import org.w3._2001.xmlschema.GroupRef;
import org.w3._2001.xmlschema.LocalElement;
import org.w3._2001.xmlschema.LocalSimpleType;
import org.w3._2001.xmlschema.NoFixedFacet;
import org.w3._2001.xmlschema.NumFacet;
import org.w3._2001.xmlschema.Pattern;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TotalDigits;

/**
 * These utilities create TL Model objects from XSD models. Builders are primary entry points that return the built
 * objects. Creators build supporting TL objects and return them. Makers are dedicated functions that create and save
 * their objects.
 * 
 * @author Dave Hollander
 * 
 */
public class XsdModelingUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(XsdModelingUtils.class);

	public static final QName XSD_Boolean = new QName("http://www.w3.org/2001/XMLSchema", "boolean");
	public static final QName XSD_String = new QName("http://www.w3.org/2001/XMLSchema", "string");
	public static final QName XSD_Short = new QName("http://www.w3.org/2001/XMLSchema", "short");
	public static final QName XSD_UnsignedShort = new QName("http://www.w3.org/2001/XMLSchema", "unsignedShort");
	public static final QName XSD_AnySimpleType = new QName("http://www.w3.org/2001/XMLSchema", "anySimpleType");
	public static final QName XSD_NonNegativeInterger = new QName("http://www.w3.org/2001/XMLSchema",
			"nonNegativeInteger");
	private static final QName XSD_UnsignedLong = new QName("http://www.w3.org/2001/XMLSchema", "unsignedLong");

	public static final String ChameleonNS = "http://chameleon.anonymous/ns";
	public static final String XSD_RequiredAttribute = "required";
	public static final int maxRepeatCount = 100000;
	// the string from the maxOccur *
	public static final String XSD_MaxRepeatString = "unbounded";
	// Do NOT use a suffix or else the type resolver will never find the group.
	// private static final String choiceGroupSuffix = "_CG";
	private static final String choiceGroupSuffix = "";

	private static final String UndefinedXSDTypeName = "Undefined_XSD_Type_Name";
	private static final String AttributeGroupPrefix = "AttributeGroup: ";
	private static final String ExtensionPrefix = "Extension: ";
	private static final String LocalAnonymousTypePrefix = "LocalAnonymousType: ";
	private static final String typePrefix = "Assigned XSD Type: ";
	private static final String unionPrefix = "Assigned XSD Union: ";

	/**
	 * Make utility class accessible.
	 */
	public XsdModelingUtils() {
	}

	/**
	 * Build a core object to represent the xsd complex type. The created TL model is added to the xsdNode's library.
	 * 
	 * @param complexType
	 *            - the type to model
	 * @param name
	 *            - name to give the type.
	 * @param xsdNode
	 *            - parent of the created core.
	 * @return
	 */
	public static TLLibraryMember buildCoreObject(final ComplexType complexType, String name, XsdNode xsdNode) {
		// LOGGER.debug("BuildCoreObject " + name + " for an xsd complex type.");

		ExplicitGroup xsdSequence = null;
		// ExplicitGroup xsdChoice = null;
		ExtensionType xsdExtension = null;
		List<Annotated> attributeList = null;

		// Initialize a TL core object to represent the complex type.
		final TLCoreObject newTLCoreObject = new TLCoreObject();
		newTLCoreObject.setSimpleFacet(new TLSimpleFacet());
		newTLCoreObject.getSimpleFacet().setSimpleType(ModelNode.getEmptyType());

		if (name == null || name.isEmpty())
			name = UndefinedXSDTypeName;
		if (complexType.getChoice() != null)
			name = name + choiceGroupSuffix; // add _CG for Core type name
		newTLCoreObject.setName(name);
		makeDoc(complexType.getAnnotation(), newTLCoreObject);
		xsdNode.getLibrary().getGeneratedLibrary().addNamedMember(newTLCoreObject);

		// If there is an extension, make it or record it in the documentation.
		if (complexType.getComplexContent() != null) {
			xsdExtension = complexType.getComplexContent().getExtension();
			if (!makeExtension(newTLCoreObject, xsdExtension))
				saveAssignedXsdType(ExtensionPrefix, newTLCoreObject, xsdExtension.getBase());
		}

		// Create attributes from the complexType or the extension.
		attributeList = complexType.getAttributeOrAttributeGroup();
		if (xsdExtension != null)
			attributeList = xsdExtension.getAttributeOrAttributeGroup();

		for (final Object p : attributeList) {
			if (p instanceof Attribute) {
				final Attribute attr = (Attribute) p;
				// If it is an indicator, create one and return
				if (attr.getType() != null && attr.getType().equals(XSD_Boolean))
					makeIndicator(attr.getName(), attr.getAnnotation(), newTLCoreObject.getSummaryFacet());
				else
					makeAttr(attr, xsdNode, newTLCoreObject.getSummaryFacet());
			}
			if (p instanceof AttributeGroupRef) {
				makeAttrGrp((AttributeGroupRef) p, xsdNode, newTLCoreObject.getSummaryFacet());
			}
		}
		if (complexType.getSequence() != null)
			xsdSequence = complexType.getSequence();
		else if (complexType.getChoice() != null)
			xsdSequence = complexType.getChoice();
		else if (xsdExtension != null)
			xsdSequence = xsdExtension.getSequence();
		if (xsdSequence != null) {
			for (final Object p : xsdSequence.getParticle())
				makeProperty(p, name, xsdNode, newTLCoreObject.getSummaryFacet());
		}

		return newTLCoreObject;
	}

	/**
	 * Build a core object to represent the ChoiceGroup. The created TL model is added to the xsdNode's library.
	 * 
	 * @param cg
	 *            - the type to model
	 * @param name
	 *            - name to give the type.
	 * @param xsdNode
	 *            - parent of the created core.
	 * @return
	 */
	protected static TLLibraryMember buildCoreObjectCG(final ExplicitGroup cg, String name, XsdNode xsdNode) {
		// LOGGER.debug("buildCoreObjectCG() " + name + " for an xsd explicit group.");

		// Initialize a TL core object to represent the complex type.
		final TLCoreObject newTLCoreObject = new TLCoreObject();
		newTLCoreObject.setSimpleFacet(new TLSimpleFacet());
		newTLCoreObject.getSimpleFacet().setSimpleType(ModelNode.getEmptyType());

		newTLCoreObject.setName(name);

		makeDoc(cg.getAnnotation(), newTLCoreObject);
		xsdNode.getLibrary().getGeneratedLibrary().addNamedMember(newTLCoreObject);
		if (newTLCoreObject.getName() == null || newTLCoreObject.getName().isEmpty())
			newTLCoreObject.setName("UNDEFINED");

		// Get the sequence containing list of properties and attributes
		if (cg != null) {
			for (final Object p : cg.getParticle())
				makeProperty(p, name, xsdNode, newTLCoreObject.getSummaryFacet());
		}
		// TODO - TEST - does this make attributes too?
		return newTLCoreObject;
	}

	/**
	 * Build a TL simple object to represent the xsd simple type. May be a simple or closed enumeration. The created TL
	 * model is added to the xsdNode's library.
	 * 
	 * @param simpleType
	 *            - the type to model, if null, then it is a built-in type so just create simple.
	 * @param name
	 *            - name to give the type.
	 * @param xsdNode
	 *            - parent of the created core.
	 * @return - the tl library member created.
	 */
	public static TLLibraryMember buildSimpleObject(SimpleType simpleType, String name, XsdNode xsdNode) {
		// Initialize a core object to represent the complex type.
		TLLibraryMember tlObj = null;
		final TLSimple tls = new TLSimple();
		final TLClosedEnumeration tlce = new TLClosedEnumeration();
		int maxLength = 0;
		int minLength = 0;
		Restriction jRes = null;
		String unionName = "";

		if (simpleType != null) {
			jRes = simpleType.getRestriction();
		}
		if (jRes != null) {
			for (final Object f : jRes.getFacets()) {
				if (f instanceof Pattern) {
					tls.setPattern(((Pattern) f).getValue());
				} else if (f instanceof TotalDigits) {
					tls.setTotalDigits(string2Int(((TotalDigits) f).getValue()));
				} else if (f instanceof JAXBElement) {
					final JAXBElement<?> je = (JAXBElement<?>) f;

					if (je.getName().getLocalPart().equals("enumeration")) {
						if (je.getValue() instanceof NoFixedFacet) {
							final NoFixedFacet jF = (NoFixedFacet) je.getValue();
							final TLEnumValue tlev = new TLEnumValue();
							tlev.setLiteral(jF.getValue());
							tlce.addValue(tlev);
							makeDoc(jF.getAnnotation(), tlev);
						}
					} else if (je.getName().getLocalPart().equals("fractionDigits")) {
						if (je.getValue() instanceof NumFacet)
							tls.setFractionDigits(string2Int(((NumFacet) je.getValue()).getValue()));
					} else if (je.getName().getLocalPart().equals("minLength")) {
						if (je.getValue() instanceof NumFacet)
							minLength = string2Int(((NumFacet) je.getValue()).getValue());
						tls.setMinLength(minLength);
					} else if (je.getName().getLocalPart().equals("maxLength")) {
						if (je.getValue() instanceof NumFacet)
							maxLength = string2Int(((NumFacet) je.getValue()).getValue());
						tls.setMaxLength(maxLength);
					} else if (je.getName().getLocalPart().equals("minInclusive")) {
						if (je.getValue() instanceof Facet)
							tls.setMinInclusive(((Facet) je.getValue()).getValue());
					} else if (je.getName().getLocalPart().equals("maxInclusive")) {
						if (je.getValue() instanceof Facet)
							tls.setMaxInclusive(((Facet) je.getValue()).getValue());
					} else if (je.getName().getLocalPart().equals("minExclusive")) {
						if (je.getValue() instanceof Facet)
							tls.setMinExclusive(((Facet) je.getValue()).getValue());
					} else if (je.getName().getLocalPart().equals("maxExclusive")) {
						if (je.getValue() instanceof Facet)
							tls.setMaxExclusive(((Facet) je.getValue()).getValue());
					}
					// else {
					// LOGGER.warn("Warning - build simple object - unhandled facet type: "
					// + je.getName());
					// }
				}
			}
		} else if (simpleType != null) {
			if (simpleType.getUnion() != null) {
				unionName = parseUnion(simpleType, xsdNode.getPrefix());
			} else if (simpleType.getList() != null) {
				// for a list just set a flag then get the type down below.
				tls.setListTypeInd(true);
			}
			// else
			// LOGGER.debug("Simple type " + name + " is not a restriction. " + simpleType.getClass());
		}
		// else
		// LOGGER.debug("No Simple Type for "+xsdNode.getName());

		// finish up
		if (tlce.getValues().size() > 0) {
			tlce.setName(name);
			// tlce.setName(NodeNameUtils.fixEnumerationName(name));
			makeDoc(simpleType.getAnnotation(), tlce);
			tlObj = tlce;
		} else {
			// use the original name in the xsd library. fix on import.
			tls.setName(name);
			makeEqu(name, xsdNode.getNamespace(), tls);
			if (simpleType != null) {
				makeDoc(simpleType.getAnnotation(), tls);
				if (simpleType.getList() != null)
					saveAssignedXsdType(tls, simpleType.getList().getItemType(), xsdNode.getNamespace());
			}
			if (jRes != null) {
				saveAssignedXsdType(tls, jRes.getBase(), xsdNode.getNamespace());
			} else if (!unionName.isEmpty())
				saveAssignedXsdUnion(tls, unionName);
			tlObj = tls;
		}
		tlObj.setOwningLibrary(xsdNode.getLibrary().getTLaLib());
		return tlObj;
	}

	/**
	 * Build a TL model for the TOP LEVEL xsd complex type. Builds a core, VWA or Business object depending on the type
	 * of a complex xsd model it receives.
	 * 
	 * @param tlObj
	 *            - the LibraryMember to be created
	 * @param xsdType
	 *            - the complex type to analyze and build from
	 * @param xsdNode
	 *            - the node that contains the modelObject used for namespace, library and linkage
	 * @return
	 */
	protected static LibraryMember buildTLModel(LibraryMember tlObj, TopLevelComplexType xsdType, XsdNode xsdNode) {
		if (!(xsdType instanceof TopLevelComplexType))
			// LOGGER.warn("Error - buildTLModel used for non-top level type.");
			if (xsdNode == null || xsdNode.getNamespace().isEmpty()) {
				LOGGER.error("Early Exit - buildTL model needs a valid xsdNode and namespace.");
			}

		if ((xsdType.getSimpleContent() != null)
				|| ((xsdType.getComplexContent() == null) && (xsdType.getSequence() == null) && (xsdType.getChoice() == null))) {
			// Create VWA for simple content or complex content that is not sequence or choice
			tlObj = buildVWA(xsdType, xsdNode);
		} else if ((xsdType.getComplexContent() != null) || (xsdType.getSequence() != null)) {
			// Create core object from a top level sequence group.
			tlObj = buildCoreObject(xsdType, xsdType.getName(), xsdNode);
		} else if ((xsdType.getComplexContent() != null) || (xsdType.getChoice() != null)) {
			// Create core object from top level choice group.
			tlObj = buildCoreObject(xsdType, xsdType.getName(), xsdNode);
		} else {
			// LOGGER.info("builtTLModel() - Built BO from unhandled jaxb type: " + xsdType.getName() + " : " +
			// xsdType);
			tlObj = new TLBusinessObject();
			((TLBusinessObject) tlObj).setName(xsdType.getName());
		}
		return tlObj;
	}

	/**
	 * Create a value with attributes
	 * 
	 * @param tlc
	 * @return
	 */
	protected static LibraryMember buildVWA(final ComplexType tlc, XsdNode xsdNode) {
		// LOGGER.debug("\t - value with attrs");
		final TLValueWithAttributes tlVWA = new TLValueWithAttributes();
		List<Annotated> attributeList = null;
		QName simpleType = null;

		// Get the sequence and list of attributes based on how the complex type
		// is structured
		if (tlc.getSimpleContent() != null) {
			final ExtensionType extension = tlc.getSimpleContent().getExtension();

			if (extension != null) {
				attributeList = extension.getAttributeOrAttributeGroup();
				simpleType = extension.getBase();
			}
		} else {
			attributeList = tlc.getAttributeOrAttributeGroup();
		}

		// tlVWA.setName(NodeNameUtils.fixVWAName(tlc.getName()));
		tlVWA.setName(tlc.getName());
		makeDoc(tlc.getAnnotation(), tlVWA);
		xsdNode.getLibrary().getGeneratedLibrary().addNamedMember(tlVWA);

		if (simpleType != null) {
			saveAssignedXsdType(tlVWA, simpleType, xsdNode.getNamespace());
		} else
			tlVWA.setParentType(ModelNode.getEmptyType());

		final TLEquivalent equ = new TLEquivalent();
		equ.setContext(xsdNode.getNamespace());
		equ.setDescription(tlc.getName());
		tlVWA.addEquivalent(equ);

		// Add attributes
		if (attributeList != null) {
			for (final Annotated a : attributeList) {
				if (a instanceof Attribute) {
					makeAttr((Attribute) a, xsdNode, (TLAttributeOwner) tlVWA);
				} else if (a instanceof AttributeGroupRef) {
					makeAttrGrp((AttributeGroupRef) a, xsdNode, tlVWA);
				}
			}
		}
		return tlVWA;
	}

	/**
	 * Create core object and node for the complex type then links the node into the complexRoot of the xsdNode's
	 * library NOTE - should not be used for Top Level complex types.
	 * 
	 * @param ct
	 *            - xsd complex type
	 * @param name
	 *            - name to give the core object
	 * @param xsdNode
	 *            - library to link it into
	 * @return
	 */
	protected static Node createLocalComplexType(ComplexType ct, String name, XsdNode xsdNode) {
		if (ct instanceof TopLevelComplexType) {
			// LOGGER.warn("Using create complex type on a Top Level type - error. Name: " + name);
		}
		TLLibraryMember newLM = buildCoreObject(ct, name, xsdNode);
		LibraryMemberInterface cn = NodeFactory.newLibraryMember(newLM);
		xsdNode.getLibrary().addLocalMember(xsdNode, (ComponentNode) cn);
		cn.setLibrary(xsdNode.getLibrary()); // links to LibraryNode and
												// TLLibrary
		// //
		// LOGGER.debug("createComplexType() - new node named: "+n.getName()+" added to complex root of lib: "+xsdNode.getLibrary().getName());
		return (Node) cn;
	}

	protected static Node createLocalChoiceGroup(ExplicitGroup ct, String name, XsdNode xsdNode) {

		TLLibraryMember newLM = buildCoreObjectCG(ct, name, xsdNode);
		LibraryMemberInterface cn = NodeFactory.newLibraryMember(newLM);
		xsdNode.getLibrary().addLocalMember(xsdNode, (ComponentNode) cn);
		// links to LibraryNode and TLLibrary
		cn.setLibrary(xsdNode.getLibrary());
		// // LOGGER.debug("createLocalChoiceGroup() - new core object " + cn + " added to library "
		// + xsdNode.getLibrary());
		return (Node) cn;
	}

	/**
	 * Create a simple type for the xsd simple type.
	 * 
	 * @param simpleType
	 * @param propName
	 * @param xsdNode
	 * @return
	 */
	private static Node createLocalSimpleType(LocalSimpleType simpleType, String name, XsdNode xsdNode) {
		TLLibraryMember newLM = buildSimpleObject(simpleType, name, xsdNode);
		LibraryMemberInterface n = NodeFactory.newLibraryMember(newLM);
		xsdNode.getLibrary().addLocalMember(xsdNode, (ComponentNode) n);
		return (Node) n;
	}

	protected static void makeAttrGrp(final AttributeGroupRef agr, XsdNode xsdNode, TLAttributeOwner owner) {
		final TLAttribute tla = new TLAttribute();
		owner.addAttribute(tla);
		// tla.setName(NodeNameUtils.fixAttributeName(agr.getRef().getLocalPart()));
		tla.setName(agr.getRef().getLocalPart());
		makeDoc(agr.getAnnotation(), tla);
		saveAssignedXsdType(AttributeGroupPrefix, tla, agr.getRef());
		tla.setType(ModelNode.getEmptyType());
		// LOGGER.debug("Made attribute group reference: " + tla.getName());
	}

	protected static void makeAttr(final Attribute attr, XsdNode xsdNode, TLAttributeOwner facet) {
		// String namespace = xsdNode.getNamespace();
		String name = "";
		if (attr == null)
			return;
		// LOGGER.debug("build attribute "+attr.getName()+" in namespace "+namespace+".");

		final TLAttribute tla = new TLAttribute();
		if (attr.getName() == null || attr.getName().isEmpty())
			if (attr.getRef() != null)
				name = attr.getRef().getLocalPart();
			else
				name = "anonymous";
		else
			name = attr.getName();
		tla.setName(name);

		makeEqu(name, xsdNode.getNamespace(), tla);
		makeDoc(attr.getAnnotation(), tla);
		if (attr.getType() != null && !attr.getType().toString().isEmpty()) {
			// Resolve the types after the libraries are loaded in.
			saveAssignedXsdType(tla, attr.getType(), xsdNode.getNamespace());
		} else if (attr.getRef() != null)
			saveAssignedXsdType(tla, attr.getRef(), xsdNode.getNamespace());

		if (attr.getUse().equals(XSD_RequiredAttribute))
			tla.setMandatory(true);

		if (attr.getSimpleType() != null) {
			// Make a new simple type from the local anonymous simple type.
			// LOGGER.debug("Make local simple type: " + attr.getName());

			String propName = xsdNode.getName() + "_" + attr.getName();
			if (attr.getSimpleType().getRestriction() != null) {
				Node typeNode = createLocalSimpleType(attr.getSimpleType(), propName, xsdNode);
				if (typeNode != null && (typeNode.getTLModelObject() instanceof TLAttributeType)) {
					tla.setType((TLAttributeType) typeNode.getTLModelObject());
					saveAssignedXsdType(LocalAnonymousTypePrefix, tla, typeNode.getName());
				} else
					saveAssignedXsdType(LocalAnonymousTypePrefix, tla, propName);
			}
			// else
			// LOGGER.debug("TODO - simple type that is not restriction on " + propName);
		}

		// TODO - attr.getType could be null--there could be a locally defined
		// type. See stl:AirportTax
		facet.addAttribute(tla);
	}

	/**
	 * Create a TLDocumentation object. The description within the documentation is set to annotation content if any.
	 * multiple values concatenated into a single string
	 * 
	 * @param annotation
	 * @return - the new TLDocumentation object (always)
	 */
	public static TLDocumentation createDoc(final Annotation annotation) {
		TLDocumentation doc = new TLDocumentation();
		if (annotation != null && annotation.getAppinfoOrDocumentation() != null
				&& annotation.getAppinfoOrDocumentation().size() > 0) {
			for (final Object x : annotation.getAppinfoOrDocumentation()) {
				if (x instanceof Documentation) {
					final StringBuilder descString = new StringBuilder();

					for (final Object list : ((Documentation) x).getContent()) {
						if (descString.length() > 0) {
							descString.append("\n");
						}
						descString.append(list.toString());
					}
					doc.setDescription(descString.toString());
				}
			}
		}
		return doc;
	}

	/**
	 * Create a TLExtension object and its base to an type found by qName.
	 * 
	 * @param tlObj
	 *            - core object to add extension to
	 * @param extension
	 *            - XSD extension element
	 * @return true if the extension has been processed, false if the name needs to be saved for later processing.
	 */
	protected static boolean makeExtension(TLCoreObject tlObj, ExtensionType extension) {
		if (extension == null || tlObj == null || extension.getBase() == null)
			return true;

		Node type = null;
		TLExtension tlEx = new TLExtension();
		type = NodeFinders.findNodeByQName(extension.getBase());
		if (type == null) {

			// LOGGER.debug("Could not find extension base: " + extension.getBase());
			// It may have not been loaded yet.
			// FIXME - 1. assure the implementer document can be seen.
			// FIXME - 2. add processing of Extension to type resolver.
			return false;
		} else {
			if (type.getTLModelObject() instanceof TLPropertyType)
				tlEx.setExtendsEntity((TLPropertyType) type.getTLModelObject());
			tlObj.setExtension(tlEx);
			// LOGGER.debug("Extended " + tlObj.getName() + " with extension base " + tlEx.getExtendsEntityName());
		}
		return true;
	}

	/**
	 * Make an TL Indicator using the passed name and annotation then add it to the facet.
	 * 
	 * @param name
	 * @param annotation
	 *            - xsd annotation element
	 * @param facet
	 */
	private static void makeIndicator(String name, Annotation annotation, TLFacet facet) {
		TLIndicator tli = null;
		tli = new TLIndicator();
		makeDoc(annotation, tli);
		// tli.setName(NodeNameUtils.fixIndicatorName(name));
		tli.setName(name);
		facet.addIndicator(tli);
		// LOGGER.debug("Made indicator: "+name);
	}

	/**
	 * Create a documentation/description tl object and concat the annotation after the prefix
	 * 
	 * @param prefix
	 * @param annotation
	 * @return
	 */
	protected static void makeDoc(String prefix, Annotation annotation, TLDocumentationOwner owner) {
		String info = prefix + " ";
		TLDocumentation doc = createDoc(annotation);
		if ((doc.getDescription() != null) && !(doc.getDescription().isEmpty()))
			doc.setDescription(info.concat(doc.getDescription()));
		else
			doc.setDescription(info);
		owner.setDocumentation(doc);
	}

	/**
	 * Create a TLDocumentation object. The description within the documentation is set to annotation content if any.
	 * multiple values concatenated into a single string
	 * 
	 * @param annotation
	 * @return - the new TLDocumentation object (always)
	 */
	public static void makeDoc(Annotation annotation, TLDocumentationOwner owner) {
		owner.setDocumentation(createDoc(annotation));
	}

	/**
	 * Using the JaxBEelement, create a property and add it to the facet. Recurses via createComplexType() for locally
	 * defined types.
	 * 
	 * @param p
	 *            - JAXBElement to consume. Member the XSD LocalElement class.
	 * @param facet
	 *            - parent for the new property
	 * @param objName
	 *            - name of the core object used if new types are created from locally defined types
	 * @param namespace
	 *            - assigned to equivalent context and as namespace of newly created types.
	 * @param xsdNode
	 * @return
	 */
	protected static void makeProperty(Object p, String objName, XsdNode xsdNode, TLFacet facet) {
		// LOGGER.debug("MakeProperty() named " + objName);
		QName typeQName = null;
		String propName = "unknown";
		boolean isChoiceGroup = false;
		Node typeNode = null;
		final TLProperty tlp = new TLProperty();

		if (p instanceof Any)
			return; // Not supported.
		if (!(p instanceof JAXBElement<?>)) {
			LOGGER.error("Early Exit - property is not a JaxB element.");
			return;
		}
		if (xsdNode == null || xsdNode.getNamespace().isEmpty()) {
			LOGGER.error("Early Exit - make property needs a valid xsdNode and namespace.");
			return;
		}

		// If it is a group or group reference, process those
		Object jaxbValue = ((JAXBElement<?>) p).getValue();
		if (jaxbValue == null) {
			// LOGGER.debug("Null jaxB value; makeProperty skipping " + objName);
			return;
		}
		if (jaxbValue instanceof ExplicitGroup) {
			ExplicitGroup jaxbGroup = (ExplicitGroup) jaxbValue;
			if (((JAXBElement<?>) p).getName().getLocalPart() == "choice")
				makeChoiceGroup(objName, tlp, xsdNode, jaxbGroup);
			else
				makePropertySet(objName, facet, xsdNode, jaxbGroup);
			return;
		} else if (jaxbValue instanceof GroupRef) {
			makeProperty(tlp, facet, xsdNode, (GroupRef) jaxbValue);
			return;
		} else if (!(jaxbValue instanceof LocalElement)) {
			// LOGGER.warn("makeProperty() unhandled value for p = " + p);
			return;
		}

		// Standard local element. Create a property for it.
		final LocalElement le = (LocalElement) jaxbValue;

		// If it is an indicator, create one and return
		if (le.getType() != null && le.getType().equals(XSD_Boolean)) {
			makeIndicator(le.getName(), le.getAnnotation(), facet);
			return;
		}

		facet.addElement(tlp);
		makeEqu(le.getName(), xsdNode.getNamespace(), tlp);

		if (!le.getMinOccurs().equals(BigInteger.ZERO))
			tlp.setMandatory(true);

		if (!le.getMaxOccurs().isEmpty())
			tlp.setRepeat(string2RepeatCnt(le.getMaxOccurs()));

		// for choice group elements make property optional
		// if (isChoiceGroup)
		// tlp.setMandatory(false);

		// assign the type.
		if (le.getRef() != null) {
			// LOGGER.debug("  process element reference: "+le.getRef());
			propName = le.getRef().getLocalPart();
			typeQName = le.getRef();
		} else if (le.getType() != null) {
			// LOGGER.debug("  process type reference: "+le.getName());
			propName = le.getName();
			typeQName = le.getType();
		} else if (le.getSimpleType() != null) {
			// LOGGER.debug("Process simple type: "+le.getName());
			propName = objName + "_" + le.getName();
			if (le.getSimpleType().getRestriction() != null) {
				typeQName = le.getSimpleType().getRestriction().getBase();
				typeNode = createLocalSimpleType(le.getSimpleType(), propName, xsdNode);
				// ModelNode.getTempLibrary().addMember(typeNode);
				xsdNode.getLibrary().addMember((LibraryMemberInterface) typeNode);
				// LOGGER.debug("Made a simple type "+typeNode.getNameWithPrefix());
			}
			// else
			// LOGGER.debug("TODO - simple type that is not restriction on " + objName + "." + propName);
		} else if (le.getComplexType() != null) {
			// LOGGER.debug("Process locally defined complex type.");
			if (le.getName() != null)
				propName = (objName + "_" + le.getName());
			else
				propName = (objName + "_" + "anonymous");
			typeNode = createLocalComplexType(le.getComplexType(), propName, xsdNode);
			typeQName = new QName(xsdNode.getNamespace(), le.getName());
			// LOGGER.debug("Created local complex type " + propName + ".");
		} else {
			// LOGGER.debug("Generic property handler. le.gettype = "+le.getType());
			propName = UndefinedXSDTypeName;
			if (le.getName() != null && (!le.getName().isEmpty()))
				propName = le.getName();
			typeQName = XSD_String; // no type specified so just use string
			// LOGGER.info("String used because no type defined for property on " + objName + "/" + propName);
		}

		// Make sure all the property data is saved
		if (propName.isEmpty()) {
			propName = UndefinedXSDTypeName;
			// LOGGER.debug("make property created a property without name.");
		}
		tlp.setName(propName); // must be done before setPropertyType
		if (isChoiceGroup)
			makeDoc("Choose one of the elements listed", le.getAnnotation(), tlp);
		else
			makeDoc(le.getAnnotation(), tlp);

		saveAssignedXsdType(tlp, typeQName, xsdNode.getNamespace());
		if (typeNode != null)
			setPropertyType(tlp, typeNode);

		return;
	}

	public static void makeProperty(TLProperty tlp, TLFacet facet, XsdNode xsdNode, GroupRef gr) {
		String propName = gr.getRef().getLocalPart();
		QName typeQName = gr.getRef();
		tlp.setName(propName); // must be done before setPropertyType
		makeDoc("Choice Group Reference to " + propName + ". ", gr.getAnnotation(), tlp);
		saveAssignedXsdType(tlp, typeQName, xsdNode.getNamespace());
		facet.addElement(tlp);
	}

	/**
	 * Make core object for the locally defined choice group and set it as the property's type.
	 */
	public static void makeChoiceGroup(final String objName, final TLProperty tlp, final XsdNode xsdNode,
			final ExplicitGroup choicegroup) {
		// LOGGER.debug("Process locally defined complex type.");
		Node typeNode;

		String propName = objName + choiceGroupSuffix;
		if (choicegroup.getName() != null)
			propName = (objName + "_" + choicegroup.getName() + choiceGroupSuffix);

		typeNode = createLocalChoiceGroup(choicegroup, propName, xsdNode);
		makeDoc(choicegroup.getAnnotation(), tlp);
		setPropertyType(tlp, typeNode);
	}

	/**
	 * Make a set of properties from the locally define explicit group and add them to the facet.
	 */
	public static void makePropertySet(final String objName, final TLFacet facet, final XsdNode xsdNode,
			final ExplicitGroup localGroup) {
		if (localGroup != null) {
			for (Object e : localGroup.getParticle()) {
				if (((JAXBElement<?>) e).getValue() instanceof LocalElement) {
					String name = ((LocalElement) ((JAXBElement<?>) e).getValue()).getName();
					makeProperty(e, name, xsdNode, facet);
				}
			}
		}
	}

	/**
	 * Make an TLEquivalent object and add it to the owner.
	 * 
	 * @param value
	 * @param context
	 * @param owner
	 *            - owner
	 */
	public static void makeEqu(String value, String context, TLEquivalentOwner owner) {
		if (context.isEmpty() || value == null || owner == null) {
			// LOGGER.error("Early Exit - Equivalent needs a valid context.");
			return;
		}
		final TLEquivalent equ = new TLEquivalent();
		if (!value.isEmpty())
			equ.setDescription(value);
		equ.setContext(context);
		owner.addEquivalent(equ);
		// LOGGER.debug("make Equ value = "+value+" in context "+context);
	}

	/**
	 * Save the XSD assigned type string into the TL object's implementers documentation. If no namespace is provided,
	 * the chameleon namespace is used.
	 */
	protected static void saveAssignedXsdType(TLDocumentationOwner owner, QName assignedType, String ns) {
		if (assignedType == null)
			saveAssignedXsdType(typePrefix, owner, XSD_String.toString());
		else {
			if (assignedType.getNamespaceURI().isEmpty()) {
				QName chameleonName = new QName(ChameleonNS, assignedType.getLocalPart());
				saveAssignedXsdType(typePrefix, owner, chameleonName.toString());
			} else
				saveAssignedXsdType(typePrefix, owner, assignedType.toString());
		}
	}

	protected static void saveAssignedXsdUnion(TLDocumentationOwner owner, String unionName) {
		saveAssignedXsdType(unionPrefix, owner, unionName);
	}

	protected static void saveAssignedXsdType(String prefix, TLDocumentationOwner owner, QName assignedType) {
		saveAssignedXsdType(prefix, owner, assignedType.toString());
	}

	private static void saveAssignedXsdType(String prefix, TLDocumentationOwner tlp, String assignedType) {
		if (tlp.getDocumentation() == null)
			tlp.setDocumentation(new TLDocumentation());
		TLDocumentation doc = tlp.getDocumentation();

		// If XSD schema type, make sure it is a supported type.
		// TODO - additional tests to see if it is other unsupported xsd types.
		QName typeQName = NodeNameUtils.qnameFromString(assignedType);
		if (typeQName == null || typeQName.equals(XSD_Short) || typeQName.equals(XSD_UnsignedShort)
				|| typeQName.equals(XSD_UnsignedLong) || typeQName.equals(XSD_AnySimpleType)
				|| typeQName.equals(XSD_NonNegativeInterger)) {
			String info = "Changed type from " + typeQName + ".";
			// if ((doc.getDescription() != null) && !(doc.getDescription().isEmpty()))
			// doc.setDescription(doc.getDescription().concat(info));
			// else
			// doc.setDescription(info);
			TLDocumentationItem implementers = new TLDocumentationItem();
			implementers.setText(info);
			tlp.getDocumentation().addImplementer(implementers);
			assignedType = XSD_String.toString();
		}

		TLDocumentationItem implementers = new TLDocumentationItem();
		implementers.setText(prefix + assignedType);
		tlp.getDocumentation().addImplementer(implementers);
	}

	/**
	 * Get the XSD Assigned type string from the model object's TL object.
	 * 
	 * @owner - any TL object that implements the documentation owner interface
	 * @return - the type qName if any, empty qName if not.
	 */
	public static QName getAssignedXsdType(TLDocumentationOwner owner) {
		String type = "";
		if ((owner.getDocumentation() != null) && !owner.getDocumentation().getImplementers().isEmpty())
			for (TLDocumentationItem item : owner.getDocumentation().getImplementers())
				if (item.getText().startsWith(typePrefix)) {
					type = item.getText().substring(typePrefix.length());
				}
		// LOGGER.debug("getAssignedXsdType for docOwner is returning: "+type);
		return NodeNameUtils.qnameFromString(type);
	}

	public static QName getAssignedXsdType(Node xn) {
		if (xn == null)
			return null;
		final List<TLDocumentationItem> items = xn.getDocHandler().getImplementers();
		if (items == null)
			return null;

		String type = "";
		for (TLDocumentationItem item : items)
			if (item.getText().startsWith(typePrefix))
				type = item.getText().substring(typePrefix.length());
		// LOGGER.debug("getAssignedXsdType for node "+xn.getName()+" is returning: "+type);
		return NodeNameUtils.qnameFromString(type);
	}

	public static String getAssignedXsdUnion(Node xn) {
		if (xn == null)
			return null;
		final List<TLDocumentationItem> items = xn.getDocHandler().getImplementers();
		if (items == null)
			return null;

		String type = "";
		for (TLDocumentationItem item : items)
			if (item.getText().startsWith(unionPrefix)) {
				type = item.getText().substring(unionPrefix.length());
			}
		// if (!type.isEmpty())
		// LOGGER.debug("getAssignedXsdUnion is returning: "+type+" for node "+xn.getName());
		return type;
	}

	/**
	 * Sets the property type and, if a complex type, overrides the name. Enforces rule that complex types must have the
	 * same name as its type.
	 * 
	 * @param tlp
	 * @param typeNode
	 * @return - true if set OK, false on error
	 */
	private static boolean setPropertyType(TLProperty tlp, Node typeNode) {
		if (typeNode == null || typeNode.getTLModelObject() == null) {
			LOGGER.warn("Assert Error - set property type does not have MO or mo/tlMO.");
			return false;
		}

		Object typeObj = typeNode.getTLModelObject();
		if (typeObj instanceof TLPropertyType) {
			tlp.setType((TLPropertyType) typeObj);
			if (typeObj instanceof XSDComplexType) {
				tlp.setName(typeNode.getName());
			}
			return true;
		}
		// else
		// LOGGER.warn(" set property type error - not a TLPropertyType");
		return false;
	}

	/**
	 * Convert the string into an integer. * is set to maxRepeat.
	 * 
	 * @param string
	 * @return - the integer, 0 if error or the string was 0
	 */
	public static int string2Int(String string) {
		int i = 0;
		try {
			i = Integer.parseInt(string.trim());
		} catch (NumberFormatException nfe) {
			LOGGER.warn("string2Int exception: string = " + string + "error msg: " + nfe.getMessage());
		}
		return i;
	}

	public static int string2RepeatCnt(String string) {
		return (string.trim().contains(XSD_MaxRepeatString)) ? maxRepeatCount : string2Int(string);
	}

	// TODO - after use in XsdSimpleMO is fixed, eliminate.
	public static String parseUnion(final XSDSimpleType type) {
		// LOGGER.debug("Parsing union from XSDSimpleType instead of simpleType.");
		return parseUnion(type.getJaxbType(), type.getOwningLibrary().getPrefix());

	}

	private static String parseUnion(final SimpleType type, String Prefix) {

		final List<QName> memberTypes = type.getUnion().getMemberTypes();
		final StringBuilder localPartUnion = new StringBuilder();
		String sep = "";
		final String separator = "+";
		for (final QName member : memberTypes) {
			if (member.getPrefix().length() != 0) {
				localPartUnion.append(sep).append(member.getPrefix());
			} else {
				localPartUnion.append(sep).append(Prefix);
			}
			localPartUnion.append(":").append(member.getLocalPart());
			sep = separator;
		}
		// LOGGER.debug("Parsing union from XSDSimpleType" +
		// localPartUnion.toString());
		return localPartUnion.toString();

	}

}

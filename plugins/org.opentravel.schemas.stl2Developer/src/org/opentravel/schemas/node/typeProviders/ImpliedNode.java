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
package org.opentravel.schemas.node.typeProviders;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.Images;

/**
 * Node to use for exposed objects that are not in the TL model.
 * 
 * usage tip: if (impliedType.equals(ImpliedNodeType.Union))
 * 
 * @author Dave Hollander
 * 
 */
public class ImpliedNode extends SimpleTypeProviders {
	protected ImpliedNodeType impliedType;
	public static final String OTA_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/Common_v01_00";

	public ImpliedNode(TLLibraryMember mbr) {
		super(ImpliedNodeType.Empty.getTlObject());
		impliedType = ImpliedNodeType.Empty;

		assert GetNode(getTLModelObject()) == this; // make sure the identity listener works
	}

	public ImpliedNode(ImpliedNodeType type) {
		super(type.getTlObject());
		impliedType = type;
	}

	/**
	 * Implied nodes belong the model, not a library so we must supply a namespace and prefix
	 */
	@Override
	public String getNamespace() {
		return "http://opentravel.org/ns/IMPLIED";
	}

	@Override
	public String getPrefix() {
		return "IMPLIED";
	}

	@Override
	public LibraryMemberInterface clone(LibraryNode targetLib, String nameSuffix) {
		return null;
	}

	/**
	 * @return the impliedType
	 */
	public ImpliedNodeType getImpliedType() {
		return impliedType;
	}

	@Override
	public String getName() {
		return impliedType.getImpliedNodeType();
	}

	@Override
	public String getLabel() {
		return "Implied: " + getName();
	}

	@Override
	public LibraryNode getLibrary() {
		return null;
	}

	@Override
	public String getAssignedTypeName() {
		return impliedType.getImpliedNodeType();
	}

	/**
	 * Set parent to passed value and clear where assigned handler.
	 * 
	 * @param parent
	 *            to assign or null
	 */
	public void initialize(Node parent) {
		setParent(parent);
		getWhereAssignedHandler().clear();
	}

	protected static TLSimple XSD_Atomic = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "XSD_Atomic " + OTA_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return "XSD_Atomic";
		}
	};
	public static TLSimple Undefined = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "Undefined " + OTA_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return "Undefined";
		}
	};
	protected static TLSimple defaultString = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "DefaultString " + ModelNode.XSD_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return "Empty";
		}
	};
	protected static TLSimple missing = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "Unassigned-missingAssignment " + OTA_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return Node.UNDEFINED_PROPERTY_TXT;
		}
	};
	protected static TLSimple indicator = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "OTA_Indicator " + OTA_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return "OTA_Indicator";
		}
	};
	protected static TLSimple union = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "Union " + OTA_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return "XSD_Union";
		}
	};
	protected static TLSimple duplicate = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "DuplicateTypes";
		}

		@Override
		public String getLocalName() {
			return "Duplicates";
		}
	};

	@Override
	public void delete() {
		super.delete();
		assert false;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public boolean isNamedEntity() {
		return true;
	}

	@Override
	public boolean isAssignable() {
		return true;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDSimpleType);
	}

	@Override
	public TLLibraryMember getTLModelObject() {
		if (impliedType != null && impliedType.equals(ImpliedNodeType.UnassignedType))
			return null;
		return (TLLibraryMember) tlObj;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.SIMPLE;
	}

	@Override
	public String getDecoration() {
		return "";
	}

	@Override
	public void setName(String name) {
		// DO NOTHING
	}

	@Override
	public List<AliasNode> getAliases() {
		return null;
	}

}

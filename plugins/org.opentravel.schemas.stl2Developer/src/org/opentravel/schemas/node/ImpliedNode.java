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

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.types.TypeProvider;

/**
 * Node to use for exposed objects that are not in the TL model.
 * 
 * usage tip: if (impliedType.equals(ImpliedNodeType.Union))
 * 
 * @author Dave Hollander
 * 
 */
public class ImpliedNode extends SimpleTypeNode implements TypeProvider {
	protected ImpliedNodeType impliedType;

	public ImpliedNode(LibraryMember mbr) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#getNamePrefix()
	 */
	@Override
	public String getNamePrefix() {
		return "IMPLIED";
	}

	/**
	 * @return the impliedType
	 */
	public ImpliedNodeType getImpliedType() {
		return impliedType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.ComponentNode#getAssignedName()
	 */
	@Override
	public String getName() {
		return impliedType.getImpliedNodeType();
	}

	@Override
	public String getLabel() {
		return "Implied: " + getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.ComponentNode#getAssignedName()
	 */
	@Override
	public String getTypeName() {
		return impliedType.getImpliedNodeType();
	}

	// public void clear() {
	// getTypeClass().clear();
	// }

	public void initialize(Node parent) {
		// getTypeClass().clear();
		// getTypeClass().setTypeNode(null);
		setParent(parent);
	}

	protected static TLSimple XSD_Atomic = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "XSD_Atomic " + Node.OTA_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return "XSD_Atomic";
		}
	};
	protected static TLSimple Undefined = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "Undefined " + Node.OTA_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return "Undefined";
		}
	};
	protected static TLSimple defaultString = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "DefaultString " + Node.XSD_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return "Empty";
		}
	};
	protected static TLSimple missing = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "Unassigned-missingAssignment " + Node.OTA_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return Node.UNDEFINED_PROPERTY_TXT;
		}
	};
	protected static TLSimple indicator = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "OTA_Indicator " + Node.OTA_NAMESPACE;
		}

		@Override
		public String getLocalName() {
			return "OTA_Indicator";
		}
	};
	protected static TLSimple union = new TLSimple() {
		@Override
		public String getValidationIdentity() {
			return "Union " + Node.OTA_NAMESPACE;
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
	public boolean isSimpleType() {
		return true;
	}

	@Override
	public boolean isSimpleTypeProvider() {
		return true;
	}

	@Override
	public boolean isTypeProvider() {
		return true;
	}

	@Override
	public boolean isAssignable() {
		return true;
	}

}

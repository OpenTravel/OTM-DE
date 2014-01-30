/**
 * 
 */
package org.opentravel.schemas.node;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLSimple;

/**
 * Node to use for exposed objects that are not in the TL model.
 * 
 * usage tip: if (impliedType.equals(ImpliedNodeType.Union))
 * 
 * @author Dave Hollander
 * 
 */
public class ImpliedNode extends SimpleTypeNode {
    protected ImpliedNodeType impliedType;

    public ImpliedNode(LibraryMember mbr) {
        super(ImpliedNodeType.Empty.getTlObject());
        impliedType = ImpliedNodeType.Empty;
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

    public void clear() {
        getTypeClass().clear();
    }

    public void initialize(Node parent) {
        getTypeClass().clear();
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
            return "Unassigned";
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

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.SimpleTypeNode#isSimpleType()
     */
    @Override
    public boolean isSimpleType() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.SimpleTypeNode#isSimpleTypeProvider()
     */
    @Override
    public boolean isSimpleTypeProvider() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.SimpleTypeNode#isTypeProvider()
     */
    @Override
    public boolean isTypeProvider() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.SimpleTypeNode#isTypeUser()
     */
    @Override
    public boolean isTypeUser() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isAssignable()
     */
    @Override
    public boolean isAssignable() {
        return true;
    }

}

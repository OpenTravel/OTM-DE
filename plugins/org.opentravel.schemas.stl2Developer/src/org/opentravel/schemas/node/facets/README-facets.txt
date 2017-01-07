Facets

Basic Tenets:
	- Facets contain properties.
	- Facets may be re-nameable. Some facet names are determined by their parent.
	- Some facets are named types
	- Some facets are library members
	
Types (TLFacetType)
	There is a mismatch between the GUI facet types and compiler model's facet types. 
	Types with an astrict * are not TL model types.
	TLFacetType - assigned to TLFacet instead of class.
	
	- * Role
	- Custom
	- Choice
	- Query
	- Shared
	- Detail
	- Summary
	- ID
	- * simple
	- * simpleList
	- * DetailList
	- * List
	- * ExtensionPoint
	- * Operation
		- Request
		- Response
		- Notification



Constructors 
	-Fixed with PropertyOwnerNode.
		why does facetNode have so many constructors? they just call super(tlObj)
	- NodeFactory.createFacet() and newComponentMember()
	- ExtensionInheritancePage.
	- When a business object is created, the component node super constructor calls addMOChildren() for facets as does the BO constructor. addMOChildren() uses the NodeFactory.newComponentMember() method
	- AddQueryFacetAction uses NodeFactory for properties but uses:
				FacetNode newFacet = bo.addFacet(wizard.getName(), facetType);
			which uses NodeFactory
	
Contextual (Injection) Facets

Tests
test/FacetNodeTest - uses PowerMockRunner
FacetMOTest - tests sorting of properties
ValueWithAttributesAttributeFacetMOTest - test codegen utils loop stack overflow
FacetNodeBuilder
		FacetNode facetNode = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").build();
		FacetNode facetNode = FacetNodeBuilder.create(ln).addAttributes("A1", "A2", "A3").build();

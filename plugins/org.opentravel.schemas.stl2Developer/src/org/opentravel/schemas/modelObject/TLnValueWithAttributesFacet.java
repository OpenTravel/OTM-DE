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
package org.opentravel.schemas.modelObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * An substitute TLModelElement to allow constructors to work on the Value With Attributes attribute
 * facet which does not exist in the TL Model.
 */
public class TLnValueWithAttributesFacet extends TLAbstractFacet implements TLAttributeOwner,
        TLIndicatorOwner, TLEquivalentOwner, TLExampleOwner {
    private final TLValueWithAttributes tlVWA;

    public TLnValueWithAttributesFacet(final TLValueWithAttributes vwa) {
        super();
        tlVWA = vwa;
    }

    public TLValueWithAttributes getValueWithAttributes() {
        return tlVWA;
    }

    public void delete() {
    }

	/**
	 * @see org.opentravel.schemacompiler.model.TLMemberFieldOwner#getMemberFields()
	 */
	@Override
	public List<TLMemberField<?>> getMemberFields() {
		List<TLMemberField<?>> memberFields = new ArrayList<>();
		
		memberFields.addAll( getAttributes() );
		memberFields.addAll( getIndicators() );
		return memberFields;
	}

    /**
	 * @see org.opentravel.schemacompiler.model.TLMemberFieldOwner#getMemberField(java.lang.String)
	 */
	@Override
	public TLMemberField<?> getMemberField(String fieldName) {
		TLMemberField<?> memberField = getAttribute( fieldName );
		
		if (memberField == null) {
			memberField = getIndicator( fieldName );
		}
		return memberField;
	}

	@Override
    public void addAttribute(final TLAttribute attribute) {
        tlVWA.addAttribute(attribute);
    }

    @Override
    public List<TLAttribute> getAttributes() {
        return tlVWA.getAttributes();
    }

    @Override
    public void addIndicator(final TLIndicator indicator) {
        tlVWA.addIndicator(indicator);
    }

    @Override
    public List<TLIndicator> getIndicators() {
        return tlVWA.getIndicators();
    }

    @Override
    public String getValidationIdentity() {
        return tlVWA.getValidationIdentity();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#getAttribute(java.lang.String)
     */
    @Override
    public TLAttribute getAttribute(final String attributeName) {
        return tlVWA.getAttribute(attributeName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#addAttribute(int,
     * org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void addAttribute(final int index, final TLAttribute attribute) {
        tlVWA.addAttribute(index, attribute);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLAttributeOwner#removeAttribute(org.opentravel.schemacompiler.
     * model.TLAttribute)
     */
    @Override
    public void removeAttribute(final TLAttribute attribute) {
        tlVWA.removeAttribute(attribute);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLAttributeOwner#moveUp(org.opentravel.schemacompiler.model.TLAttribute
     * )
     */
    @Override
    public void moveUp(final TLAttribute attribute) {
        tlVWA.moveUp(attribute);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#moveDown(org.opentravel.schemacompiler.model.
     * TLAttribute)
     */
    @Override
    public void moveDown(final TLAttribute attribute) {
        tlVWA.moveDown(attribute);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#sortAttributes(java.util.Comparator)
     */
    @Override
    public void sortAttributes(final Comparator<TLAttribute> comparator) {
        tlVWA.sortAttributes(comparator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#getIndicator(java.lang.String)
     */
    @Override
    public TLIndicator getIndicator(final String indicatorName) {
        return tlVWA.getIndicator(indicatorName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#addIndicator(int,
     * org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void addIndicator(final int index, final TLIndicator indicator) {
        tlVWA.addIndicator(index, indicator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLIndicatorOwner#removeIndicator(org.opentravel.schemacompiler.
     * model.TLIndicator)
     */
    @Override
    public void removeIndicator(final TLIndicator indicator) {
        tlVWA.addIndicator(indicator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLIndicatorOwner#moveUp(org.opentravel.schemacompiler.model.TLIndicator
     * )
     */
    @Override
    public void moveUp(final TLIndicator indicator) {
        tlVWA.moveUp(indicator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#moveDown(org.opentravel.schemacompiler.model.
     * TLIndicator)
     */
    @Override
    public void moveDown(final TLIndicator indicator) {
        tlVWA.moveDown(indicator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#sortIndicators(java.util.Comparator)
     */
    @Override
    public void sortIndicators(final Comparator<TLIndicator> comparator) {
        tlVWA.sortIndicators(comparator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#getExamples()
     */
    @Override
    public List<TLExample> getExamples() {
        return tlVWA.getExamples();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#getExample(java.lang.String)
     */
    @Override
    public TLExample getExample(final String contextId) {
        return tlVWA.getExample(contextId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLExampleOwner#addExample(org.opentravel.schemacompiler.model.TLExample
     * )
     */
    @Override
    public void addExample(final TLExample example) {
        tlVWA.addExample(example);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#addExample(int,
     * org.opentravel.schemacompiler.model.TLExample)
     */
    @Override
    public void addExample(final int index, final TLExample example) {
        tlVWA.addExample(index, example);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLExampleOwner#removeExample(org.opentravel.schemacompiler.model
     * .TLExample)
     */
    @Override
    public void removeExample(final TLExample example) {
        tlVWA.removeExample(example);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLExampleOwner#moveUp(org.opentravel.schemacompiler.model.TLExample
     * )
     */
    @Override
    public void moveUp(final TLExample example) {
        tlVWA.moveUp(example);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLExampleOwner#moveDown(org.opentravel.schemacompiler.model.TLExample
     * )
     */
    @Override
    public void moveDown(final TLExample example) {
        tlVWA.moveDown(example);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#sortExamples(java.util.Comparator)
     */
    @Override
    public void sortExamples(final Comparator<TLExample> comparator) {
        tlVWA.sortExamples(comparator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#getEquivalents()
     */
    @Override
    public List<TLEquivalent> getEquivalents() {
        return tlVWA.getEquivalents();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#getEquivalent(java.lang.String)
     */
    @Override
    public TLEquivalent getEquivalent(final String context) {
        return tlVWA.getEquivalent(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(org.opentravel.schemacompiler.model
     * .TLEquivalent)
     */
    @Override
    public void addEquivalent(final TLEquivalent equivalent) {
        tlVWA.addEquivalent(equivalent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(int,
     * org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void addEquivalent(final int index, final TLEquivalent equivalent) {
        tlVWA.addEquivalent(index, equivalent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLEquivalentOwner#removeEquivalent(org.opentravel.schemacompiler
     * .model.TLEquivalent)
     */
    @Override
    public void removeEquivalent(final TLEquivalent equivalent) {
        tlVWA.removeEquivalent(equivalent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#moveUp(org.opentravel.schemacompiler.model.
     * TLEquivalent)
     */
    @Override
    public void moveUp(final TLEquivalent equivalent) {
        tlVWA.moveUp(equivalent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.model.TLEquivalentOwner#moveDown(org.opentravel.schemacompiler.model.
     * TLEquivalent)
     */
    @Override
    public void moveDown(final TLEquivalent equivalent) {
        tlVWA.moveDown(equivalent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#sortEquivalents(java.util.Comparator)
     */
    @Override
    public void sortEquivalents(final Comparator<TLEquivalent> comparator) {
        tlVWA.sortEquivalents(comparator);
    }
}

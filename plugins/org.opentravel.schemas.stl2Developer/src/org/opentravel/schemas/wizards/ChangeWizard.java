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
package org.opentravel.schemas.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.modelObject.ValueWithAttributesAttributeFacetMO;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.SubType;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ChangeWizard extends ValidatingWizard implements Cancelable {

	private ChangeWizardPage page;
	private boolean canceled;
	private final List<SubType> allowedObjectTypes = new ArrayList<SubType>();
	private final List<ExtentedTLFacetType> allowedFacetTypes = new ArrayList<ExtentedTLFacetType>();
	private final ComponentNode editedNode;

	/**
	 * Set up the change wizard for the passed node.
	 * 
	 * @param editedNode
	 */
	public ChangeWizard(final ComponentNode editedNode) {
		this.editedNode = editedNode;
		allowedObjectTypes
				.addAll(Arrays.asList(SubType.BUSINESS_OBJECT, SubType.CORE_OBJECT, SubType.VALUE_WITH_ATTRS));
		allowedFacetTypes.addAll(Arrays.asList(ExtentedTLFacetType.values()));
	}

	/**
	 * The enum exist only because there is missing TLFacetType.ATTRIBUTES, describing the VWA facet with attributes.
	 * After adding this type to TLFacetType this enum can be deleted.
	 */
	enum ExtentedTLFacetType {
		ID(TLFacetType.ID), SUMMARY(TLFacetType.SUMMARY), DETAIL(TLFacetType.DETAIL), SIMPLE(TLFacetType.SIMPLE), VWA_ATTRIBUTES(
				ValueWithAttributesAttributeFacetMO.DISPLAY_NAME);

		private TLFacetType tlType;
		private String identityName;

		private ExtentedTLFacetType(TLFacetType tlType) {
			this.tlType = tlType;
		}

		private ExtentedTLFacetType(String identityName) {
			this((TLFacetType) null);
			this.identityName = identityName;
		}

		public TLFacetType toTLFacetType() {
			return tlType;
		}

		public String getIdentityName() {
			if (tlType != null) {
				return tlType.getIdentityName();
			}
			return identityName;
		}

		public static ExtentedTLFacetType valueOf(ComponentNode facet) {
			if (facet.isVWA_AttributeFacet()) {
				return VWA_ATTRIBUTES;
			}
			if (facet.getFacetType() == null) {
				return null;
			}
			for (ExtentedTLFacetType tl : values()) {
				if (tl.tlType == facet.getFacetType()) {
					return tl;
				}
			}
			return null;
		}

		public boolean equals(TLFacetType type) {
			return tlType == type;
		}

	}

	@Override
	public void addPages() {
		page = new ChangeWizardPage("Change Object", "Perform transforms on the object", getValidator(), editedNode,
				allowedObjectTypes, allowedFacetTypes);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		canceled = false;
		page.historyClear();
		return true;
	}

	@Override
	public boolean performCancel() {
		canceled = true;
		page.undoAllOperation();
		return true;
	}

	public void run(final Shell shell) {
		final WizardDialog dialog = new WizardDialog(shell, this);
		dialog.setPageSize(600, 400);
		dialog.create();
		dialog.open();
	}

	@Override
	public boolean wasCanceled() {
		return canceled;
	}

	public ComponentNode getEditedComponent() {
		return page.getEditedComponent();
	}
}

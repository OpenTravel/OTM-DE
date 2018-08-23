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
import org.opentravel.schemas.actions.ChangeActionController;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.SubType;
import org.opentravel.schemas.node.facets.AttributeFacetNode;

/**
 * Change Wizard. Change owning library, object type or property facet parent. Keep a history of changes to allow
 * reverting changes.
 * <p>
 * Wizard is constructed with the node to be changed.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ChangeWizard extends ValidatingWizard implements Cancelable {

	private ChangeWizardPage page;
	private boolean canceled;
	private final List<SubType> allowedObjectTypes = new ArrayList<>();
	private final List<ExtendedTLFacetType> allowedFacetTypes = new ArrayList<>();
	private final ComponentNode editedNode;
	private final ChangeActionController changeActionController;

	/**
	 * Set up the change wizard for the passed node.
	 * 
	 * @param editedNode
	 * @param changeActionController
	 */
	public ChangeWizard(final ComponentNode editedNode, ChangeActionController changeActionController) {
		this.editedNode = editedNode;
		// TODO - expose for use in pages, testing and controller
		allowedObjectTypes
				.addAll(Arrays.asList(SubType.BUSINESS_OBJECT, SubType.CORE_OBJECT, SubType.VALUE_WITH_ATTRS));
		allowedFacetTypes.addAll(Arrays.asList(ExtendedTLFacetType.values()));
		this.changeActionController = changeActionController;
	}

	/**
	 * The enum exist only because there is missing TLFacetType.ATTRIBUTES, describing the VWA facet with attributes.
	 * After adding this type to TLFacetType this enum can be deleted.
	 */
	public static final String DISPLAY_NAME = "Attributes";

	public enum ExtendedTLFacetType {
		ID(TLFacetType.ID),
		SUMMARY(TLFacetType.SUMMARY),
		DETAIL(TLFacetType.DETAIL),
		SIMPLE(TLFacetType.SIMPLE),
		VWA_ATTRIBUTES(DISPLAY_NAME);

		private TLFacetType tlType;
		private String identityName;

		private ExtendedTLFacetType(TLFacetType tlType) {
			this.tlType = tlType;
		}

		private ExtendedTLFacetType(String identityName) {
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

		public static ExtendedTLFacetType valueOf(ComponentNode facet) {
			if (facet instanceof AttributeFacetNode) {
				return VWA_ATTRIBUTES;
			}
			if (facet.getFacetType() == null) {
				return null;
			}
			for (ExtendedTLFacetType tl : values()) {
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
				allowedObjectTypes, allowedFacetTypes, changeActionController);
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

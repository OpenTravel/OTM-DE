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
package org.opentravel.schemas.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.Activator;
import org.opentravel.schemas.stl2developer.OtmRegistry;

public class GeneralPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "stl2Developer.GeneralPreferencePage";
	public static final String NAMESPACE_MANAGED = "stl2Developer.namespace.managed";
	public static final String OTMV16 = "stl2Developer.otmVersion.16";

	public GeneralPreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(NAMESPACE_MANAGED, Messages.getString("preferences.general.namespace.managed"),
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(OTMV16, Messages.getString("preferences.general.otmVersion16"),
				getFieldEditorParent()));

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(OTMV16)) {
					boolean enabled = (boolean) event.getNewValue();
					OTM16Upgrade.otm16Enabled = enabled;
				}

			}
		});
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Managed namespaces must conform to the OTM rule. Base namespaces are controlled by repositories. Projects select
	 * a base namespace. When managed, libraries must have a namespace root that is managed by a repository.
	 * 
	 * User can control if operating in managed mode via control on preference page.
	 * 
	 * @return
	 */
	public static boolean areNamespacesManaged() {
		if (Activator.getDefault() == null)
			return true; // let junits make changes
		return Activator.getDefault().getPreferenceStore().getBoolean(NAMESPACE_MANAGED);
	}

	public static boolean isOTM16Enabled() {
		if (Activator.getDefault() == null)
			return true; // let junits make changes
		return Activator.getDefault().getPreferenceStore().getBoolean(OTMV16);
	}

	/**
	 * Update the library editable status to reflect managed namespace setting.
	 */
	@Override
	public boolean performOk() {
		boolean state = super.performOk();
		// TODO: remove this at attach IPreferenceChangeListener in view need to refresh
		OtmRegistry.getMainController().getLibraryController().updateLibraryStatus();
		return state;
	}

}

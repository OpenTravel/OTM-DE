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
package org.opentravel.schemas.views.propertyview;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.propertyview.desc.IFormPropertyDescriptor;
import org.opentravel.schemas.views.propertyview.desc.IReadonlyPropertyDescriptor;
import org.opentravel.schemas.views.propertyview.editors.FormCellEditor;
import org.opentravel.schemas.widgets.WidgetFactory;

class PropertySheetPage extends Page {
	// private static final Logger LOGGER = LoggerFactory.getLogger(PropertySheetPage.class);
	private static final String UNDEFINED_CATEGORY_KEY = Messages.getString("view.properties.basicCategory");
	private ScrolledForm form;
	private IPropertySource source;
	private NodePropertySourceFactory factory = new NodePropertySourceFactory();
	private final FormToolkit toolkit;
	private LocalResourceManager resManager;
	private ISelection currentSelection;
	private List<IExternalDependencies> disposeList = new ArrayList<>();

	public PropertySheetPage(Node node) {
		source = factory.createPropertySource(node);
		toolkit = WidgetFactory.createFormToolkit(PlatformUI.getWorkbench().getDisplay());
	}

	/**
	 * @param currentPart
	 * @param currentSelection
	 */
	public void selectionChanged(IWorkbenchPart currentPart, ISelection selection) {
		// LOGGER.debug("Selection Changed.");
		if (isSameSelection(currentSelection, selection))
			return;
		currentSelection = selection;
		IStructuredSelection sel = (IStructuredSelection) currentSelection;
		IPropertySource newSource = factory.createPropertySource((Node) sel.getFirstElement());
		if (newSource != null) {
			removeAll();
			source = newSource;
			initForm(form.getBody(), newSource);
		}

	}

	private boolean isSameSelection(ISelection currentSelection, ISelection newSelection) {
		if (newSelection == null) {
			if (currentSelection == null) {
				return true;
			} else {
				return false;
			}
		} else {
			return newSelection.equals(currentSelection);
		}
	}

	private void removeAll() {
		for (Control c : form.getBody().getChildren()) {
			c.dispose();
		}
		for (IExternalDependencies ed : disposeList) {
			ed.dispose();
		}
		disposeList.clear();
		form.getForm().setText(null);
	}

	@Override
	public void createControl(Composite parent) {
		form = toolkit.createScrolledForm(parent);
		resManager = new LocalResourceManager(JFaceResources.getResources(), form);
		GridLayoutFactory.fillDefaults().extendedMargins(10, 10, 10, 10).numColumns(2).applyTo(form.getBody());
		toolkit.paintBordersFor(form.getBody());
		if (source == null)
			source = factory.createPropertySource((Node) OtmRegistry.getNavigatorView().getCurrentNode());
		if (source != null) {
			initForm(form.getBody(), source);
		} else {
			createEmptyForm(form.getBody());
		}
	}

	private void createEmptyForm(Composite body) {
		form.setText("Please select Library, Project or Repository");
	}

	private void initForm(Composite parent, IPropertySource source) {
		IPropertyDescriptor[] descs = source.getPropertyDescriptors();
		Map<String, List<IPropertyDescriptor>> categories = getCategories(descs);
		for (Entry<String, List<IPropertyDescriptor>> e : categories.entrySet()) {
			createSection(parent, e.getKey(), e.getValue());
		}
		form.reflow(true);
	}

	private void createSection(Composite parent, String key, List<IPropertyDescriptor> value) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		section.setText(key);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).span(2, -1).applyTo(section);

		parent = toolkit.createComposite(section, SWT.WRAP);
		section.setClient(parent);
		GridLayoutFactory.fillDefaults().extendedMargins(10, 10, 10, 10).numColumns(2).applyTo(parent);
		for (IPropertyDescriptor pd : value) {
			createField(parent, pd);
		}
	}

	private void createField(Composite parent, final IPropertyDescriptor pd) {
		Label label = toolkit.createLabel(parent, pd.getDisplayName());
		label.setToolTipText(pd.getDescription());

		Object value = source.getPropertyValue(pd.getId());
		if (value instanceof IPropertySource) {
			Composite composite = toolkit.createComposite(parent);
			composite.setLayout(new RowLayout());
			for (IPropertyDescriptor desc : ((IPropertySource) value).getPropertyDescriptors()) {
				createPropertyDescritpor(composite, desc, null, (IPropertySource) value);
			}
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(composite);
		} else {
			GridData layoutData = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create();
			createPropertyDescritpor(parent, pd, layoutData, source);
		}

	}

	private void createPropertyDescritpor(Composite parent, IPropertyDescriptor pd, Object layoutData,
			IPropertySource source) {
		Control cellControl = null;
		GridData customGD = null;
		CellEditor ce = null;
		if (pd instanceof IFormPropertyDescriptor) {
			ce = ((IFormPropertyDescriptor) pd).createPropertyEditor(toolkit);
			customGD = ((IFormPropertyDescriptor) pd).getCustomGridData();
			PropertyCellEditorListener l = new PropertyCellEditorListener(pd.getId(), ce, source);
			ce.addListener(l);
		} else {
			ce = new ReadOnlyCellEditor(toolkit);
		}
		ce.create(parent);
		if (pd instanceof IReadonlyPropertyDescriptor) {
			ce.getControl().setEnabled(!((IReadonlyPropertyDescriptor) pd).isReadonly());
		}
		if (pd instanceof IExternalDependencies) {
			((IExternalDependencies) pd).init(this);
			disposeList.add((IExternalDependencies) pd);
		}
		ce.setValue(source.getPropertyValue(pd.getId()));
		cellControl = ce.getControl();
		cellControl.setToolTipText(pd.getDescription());
		if (customGD != null) {
			cellControl.setLayoutData(customGD);
		} else {
			cellControl.setLayoutData(layoutData);
		}
	}

	private Map<String, List<IPropertyDescriptor>> getCategories(IPropertyDescriptor[] propertyDescriptors) {
		Map<String, List<IPropertyDescriptor>> categories = new LinkedHashMap<>();
		for (IPropertyDescriptor desc : propertyDescriptors) {
			String cat = desc.getCategory();
			if (cat == null) {
				cat = UNDEFINED_CATEGORY_KEY;
			}
			List<IPropertyDescriptor> descs = categories.get(cat);
			if (descs == null) {
				descs = new ArrayList<>(5);
				categories.put(cat, descs);
			}
			descs.add(desc);
		}
		return categories;
	}

	public void refresh() {
		IStructuredSelection sel = (IStructuredSelection) currentSelection;
		if (sel != null) {
			IPropertySource newSource = factory.createPropertySource((Node) sel.getFirstElement());
			if (newSource != null) {
				removeAll();
				source = newSource;
				initForm(form.getBody(), newSource);
			}
		}
	}

	@Override
	public Control getControl() {
		return form;
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

	class PropertyCellEditorListener implements ICellEditorListener {

		private Object id;
		private CellEditor editor;
		private IPropertySource source;

		public PropertyCellEditorListener(Object id, CellEditor editor, IPropertySource source) {
			this.id = id;
			this.editor = editor;
			this.source = source;
		}

		@Override
		public void applyEditorValue() {
			try {
				source.setPropertyValue(id, editor.getValue());
			} catch (IllegalArgumentException ex) {
				DialogUserNotifier.openError("ERROR", ex.getMessage(), ex);
				// LOGGER.info("Error on setting property [" + id + "]: " + ex.getMessage());
			}
			refresh();
			OtmRegistry.getMainController().refresh();
		}

		@Override
		public void cancelEditor() {
		}

		@Override
		public void editorValueChanged(boolean oldValidState, boolean newValidState) {
		}

	}

	class ReadOnlyCellEditor extends FormCellEditor {

		private Text text;
		private final FormToolkit toolkit;

		public ReadOnlyCellEditor(FormToolkit toolkit) {
			this.toolkit = toolkit;
		}

		@Override
		protected Control createControl(Composite parent) {
			text = toolkit.createText(parent, "");
			text.setBackground(resManager.createColor(new RGB(240, 240, 240)));
			text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
			text.setEditable(false);
			return text;
		}

		@Override
		protected Object doGetValue() {
			return text.getText();
		}

		@Override
		protected void doSetFocus() {
			text.setFocus();
		}

		@Override
		protected void doSetValue(Object value) {
			if (value == null) {
				value = "";
			}
			text.setText((String) value);
		}

	}

	public String getName() {
		if (source != null)
			return source.toString();
		return "";
	}

}
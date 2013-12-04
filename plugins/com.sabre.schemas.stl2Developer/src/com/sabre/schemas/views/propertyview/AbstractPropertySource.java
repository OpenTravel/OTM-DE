/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.views.propertyview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.sabre.schemas.preferences.GeneralPreferencePage;
import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.stl2developer.Activator;
import com.sabre.schemas.views.propertyview.desc.IFormPropertyDescriptor;
import com.sabre.schemas.views.propertyview.editors.LabelCellEditor;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class AbstractPropertySource<T> implements IPropertySource {

    public static final String CAT_BASIC = Messages.getString("view.properties.basicCategory");
    public static final String CAT_NAMESPACE = Messages
            .getString("view.properties.library.namespaceCategory");
    public static final String CAT_REMOTE = Messages
            .getString("view.properties.library.remoteCategory");

    enum BasicProperties implements PropertyInfo {
        NAMESPACE_POLICY(Messages.getString("view.properties.library.label.namespacePolicy"),
                CAT_NAMESPACE, Messages.getString("view.properties.library.label.namespacePolicy"));
        private final String displayName;
        private final String category;
        private final String tooltip;

        private BasicProperties(String displayName, String category, String tooltip) {
            this.displayName = displayName;
            this.category = category;
            this.tooltip = tooltip;
        }

        @Override
        public String getTooltip() {
            return tooltip;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getId() {
            return this.name();
        }

    }

    protected T source;
    private Map<String, PropertySetter> settersById = new HashMap<String, PropertySetter>();

    public AbstractPropertySource(T source) {
        this.source = source;
    }

    public PropertySetter createNamespacePolicy() {
        return new EnumPropertySetter(BasicProperties.NAMESPACE_POLICY) {

            @Override
            public void setValue(Object value) {
                // DO NOTHING
            }

            @Override
            public Object getValue() {
                if (GeneralPreferencePage.areNamespacesManaged())
                    return Messages.getString("OtmW.NSPolicy.Enabled");
                else
                    return Messages.getString("OtmW.NSPolicy.Disabled");
            }

            @Override
            public PropertyDescriptor createPropertyDescriptor() {
                return new NamespacePropertyDescriptor(getId(), getDisplayName());
            }
        };
    }

    class NamespacePropertyDescriptor extends PropertyDescriptor implements IExternalDependencies,
            IFormPropertyDescriptor {

        public NamespacePropertyDescriptor(Object id, String displayName) {
            super(id, displayName);
        }

        private IPropertyChangeListener changeListener;

        @Override
        public void init(final PropertySheetPage propertySheetPage) {
            changeListener = new IPropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (GeneralPreferencePage.NAMESPACE_MANAGED.equals(event.getProperty())) {
                        propertySheetPage.refresh();
                    }
                }
            };
            Activator.getDefault().getPreferenceStore().addPropertyChangeListener(changeListener);
        }

        @Override
        public void dispose() {
            Activator.getDefault().getPreferenceStore()
                    .removePropertyChangeListener(changeListener);

        }

        @Override
        public CellEditor createPropertyEditor(FormToolkit toolkit) {
            return new LabelCellEditor(toolkit);
        }

        @Override
        public GridData getCustomGridData() {
            return null;
        }
    };

    public interface PropertyInfo {

        public String getTooltip();

        public String getCategory();

        public String getDisplayName();

        public String getId();

    }

    public abstract class EnumPropertySetter implements PropertySetter {

        private PropertyInfo property;

        public EnumPropertySetter(PropertyInfo property) {
            this.property = property;
        }

        @Override
        public final IPropertyDescriptor getDescriptor() {
            PropertyDescriptor pd = createPropertyDescriptor();
            pd.setCategory(getCategoryName());
            pd.setDescription(getTooltip());
            return pd;
        }

        public abstract PropertyDescriptor createPropertyDescriptor();

        @Override
        public final String getId() {
            return property.getId();
        }

        public final String getDisplayName() {
            return property.getDisplayName();
        }

        public final String getCategoryName() {
            return property.getCategory();
        }

        public final String getTooltip() {
            return property.getTooltip();
        }

    }

    @Override
    public T getEditableValue() {
        return source;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        Collection<PropertySetter> setters = initSetters();
        List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>(setters.size());
        for (PropertySetter ps : setters) {
            settersById.put(ps.getId(), ps);
            descs.add(ps.getDescriptor());
        }
        return descs.toArray(new IPropertyDescriptor[descs.size()]);
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (settersById.containsKey(id)) {
            return settersById.get(id).getValue();
        }
        return null;
    }

    @Override
    public boolean isPropertySet(Object id) {
        return true;
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        settersById.get(id).setValue(value);
    }

    public abstract Collection<PropertySetter> initSetters();

}

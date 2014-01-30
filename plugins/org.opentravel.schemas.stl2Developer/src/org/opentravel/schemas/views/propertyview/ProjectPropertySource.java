/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.views.propertyview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.preferences.GeneralPreferencePage;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.propertyview.desc.ComboboxPropertyDescriptor;
import org.opentravel.schemas.views.propertyview.desc.TextFormPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectPropertySource extends AbstractPropertySource<ProjectNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectPropertySource.class);

    public ProjectPropertySource(ProjectNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return Messages.getString("view.properties.project.title");
    }

    enum ProjectProperties implements PropertyInfo {
        NAME(Messages.getString("view.properties.library.label.name"), CAT_BASIC, Messages
                .getString("view.properties.library.tooltip.name")),
        PATH(Messages.getString("view.properties.library.label.path"), CAT_BASIC, Messages
                .getString("view.properties.library.tooltip.path")),
        MANAGED_ROOT(Messages.getString("view.properties.library.label.managedRoot"),
                CAT_NAMESPACE, Messages.getString("view.properties.library.tooltip.managedRoot")),
        EXTENSION(Messages.getString("view.properties.library.label.extension"), CAT_NAMESPACE,
                Messages.getString("view.properties.library.tooltip.extension")),
        NAMESPACE(Messages.getString("view.properties.library.label.namespace"), CAT_NAMESPACE,
                Messages.getString("view.properties.library.tooltip.namespace"));

        private final String displayName;
        private final String category;
        private final String tooltip;

        @Override
        public String getTooltip() {
            return tooltip;
        }

        private ProjectProperties(String displayName, String category) {
            this(displayName, category, "");
        }

        private ProjectProperties(String displayName, String category, String tooltip) {
            this.displayName = displayName;
            this.category = category;
            this.tooltip = tooltip;
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

    @Override
    public Collection<PropertySetter> initSetters() {
        List<PropertySetter> setters = new ArrayList<PropertySetter>();
        setters.add(createName());
        setters.add(createPath());
        setters.add(createNamespacePolicy());
        setters.add(createNamespace());
        setters.add(createManagedRoot());
        setters.add(createExtension());
        return setters;
    }

    private PropertySetter createNamespace() {
        return new ProjectPropertySetter(ProjectProperties.NAMESPACE) {

            @Override
            public void setValue(Object value) {
                source.setNamespace((String) value);
            }

            @Override
            public Object getValue() {
                return source.getNamespace();
            }

            @Override
            public PropertyDescriptor getPropertyDescriptor() {
                if (GeneralPreferencePage.areNamespacesManaged()) {
                    return new PropertyDescriptor(getId(), getDisplayName());
                } else {
                    return new TextFormPropertyDescriptor(getId(), getDisplayName());
                }
            }
        };
    }

    private PropertySetter createName() {
        return new ProjectPropertySetter(ProjectProperties.NAME) {

            @Override
            public void setValue(Object value) {
                source.setName((String) value);
                OtmRegistry.getMainController().getProjectController().save(source);
                OtmRegistry.getMainController().refresh(source);
            }

            @Override
            public Object getValue() {
                return source.getName();
            }

            @Override
            public PropertyDescriptor getPropertyDescriptor() {
                return new TextFormPropertyDescriptor(getId(), getDisplayName());
            }

        };
    }

    private PropertySetter createExtension() {
        return new ProjectPropertySetter(ProjectProperties.EXTENSION) {

            @Override
            public void setValue(Object value) {
                updateNamespace(source, source.getNSRoot(), (String) value);
            }

            @Override
            public Object getValue() {
                return source.getNSExtension();
            }

            @Override
            public PropertyDescriptor getPropertyDescriptor() {
                if (GeneralPreferencePage.areNamespacesManaged()) {
                    return new TextFormPropertyDescriptor(getId(), getDisplayName());
                } else {
                    return new PropertyDescriptor(getId(), getDisplayName());
                }
            }

        };
    }

    private PropertySetter createPath() {
        return new ProjectPropertySetter(ProjectProperties.PATH) {

            @Override
            public void setValue(Object value) {
            }

            @Override
            public Object getValue() {
                return source.getPath();
            }

            @Override
            public PropertyDescriptor getPropertyDescriptor() {
                return new PropertyDescriptor(getId(), getDisplayName());
            }

        };
    }

    private PropertySetter createManagedRoot() {
        return new ProjectPropertySetter(ProjectProperties.MANAGED_ROOT) {

            @Override
            public void setValue(Object value) {
                updateNamespace(source, (String) value, source.getNSExtension());
            }

            @Override
            public Object getValue() {
                return source.getNSRoot();
            }

            @Override
            public PropertyDescriptor getPropertyDescriptor() {
                return new ComboboxPropertyDescriptor(getId(), getDisplayName(), getNamespaces());
            }

        };
    }

    private void updateNamespace(final ProjectNode node, String baseRoot, String extensions) {
        String newNS = baseRoot;
        if (newNS.isEmpty())
            newNS = extensions;
        else if (!extensions.isEmpty()) {
            newNS = ProjectNode.appendExtension(baseRoot, extensions);
        }

        updateNamespace(node, newNS);
    }

    private void updateNamespace(ProjectNode node, String namespace) {
        if (!node.getNamespace().equals(namespace)) {
            node.setNamespace(namespace);
            OtmRegistry.getMainController().getLibraryController().updateLibraryStatus();
            LOGGER.debug("TEST - change project's namespace to " + namespace);
        }
    }

    private Collection<String> getNamespaces() {
        Set<String> namespaces = new LinkedHashSet<String>();
        if (!source.getNSRoot().isEmpty()) {
            namespaces.add(source.getNSRoot());
        }
        for (final String namespace : OtmRegistry.getMainController().getRepositoryController()
                .getRootNamespaces()) {
            namespaces.add(namespace);
        }
        return namespaces;
    }

    abstract class ProjectPropertySetter extends EnumPropertySetter {

        public ProjectPropertySetter(PropertyInfo property) {
            super(property);
        }

        @Override
        public PropertyDescriptor createPropertyDescriptor() {
            if (isDefaultProject(source) || source.isBuiltIn()) {
                return new PropertyDescriptor(getId(), getDisplayName());
            } else {
                return getPropertyDescriptor();
            }
        }

        public abstract PropertyDescriptor getPropertyDescriptor();

        private boolean isDefaultProject(ProjectNode project) {
            return project.equals(OtmRegistry.getMainController().getProjectController()
                    .getDefaultProject());
        }

    }
}
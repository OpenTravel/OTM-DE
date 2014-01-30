
package org.opentravel.schemas.views.propertyview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RefreshPolicy;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryChainNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryNameNode;
import org.opentravel.schemas.views.propertyview.desc.ComboboxPropertyDescriptor;

import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;

public class RepositoryPropertySource extends AbstractPropertySource<RepositoryNode> {

    public RepositoryPropertySource(RepositoryNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return Messages.getString("view.properties.repository.title");
    }

    enum RepositoryProperties implements PropertyInfo {
        NAME(Messages.getString("view.properties.repository.label.name"), CAT_BASIC),
        URL(Messages.getString("view.properties.repository.label.url"), CAT_BASIC),
        MANAGED_NAMESPACE(Messages.getString("view.properties.repository.label.managedNamespace"),
                CAT_BASIC),
        CURRENT_USER(Messages.getString("view.properties.repository.label.currentUser"), CAT_BASIC),
        REFRESH_POLICY(Messages.getString("view.properties.repository.label.refresh"), CAT_BASIC,
                Messages.getString("view.properties.repository.tooltip.refresh")),
        PERMISSON(Messages.getString("view.properties.repository.label.permission"), CAT_BASIC),
        LOCKED_BY(Messages.getString("view.properties.repository.label.lockedBy"), CAT_BASIC);

        private final String displayName;
        private final String category;
        private final String tooltip;

        @Override
        public String getTooltip() {
            return tooltip;
        }

        private RepositoryProperties(String displayName, String category) {
            this(displayName, category, "");
        }

        private RepositoryProperties(String displayName, String category, String tooltip) {
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

        Repository repository = source.getRepository();
        PropertySetter urlSetter = createURL(repository);
        if (urlSetter != null) {
            setters.add(urlSetter);
        }

        if (!(source instanceof RepositoryNameNode)) {
            setters.add(createNamespace(source));
            setters.add(createPermission(source));
        }

        if (repository instanceof RemoteRepositoryClient) {
            setters.add(createCurrentUser((RemoteRepositoryClient) repository));
            setters.add(createRefreshPolicy((RemoteRepositoryClient) repository));
        }
        if (source instanceof RepositoryChainNode) {
            setters.add(createLockedBy(((RepositoryChainNode) source).getItem()));
        }
        return setters;
    }

    private PropertySetter createPermission(final RepositoryNode source) {
        return new ReadonlyEnumSetter(RepositoryProperties.PERMISSON) {
            @Override
            public void setValue(Object value) {
            }

            @Override
            public Object getValue() {
                return source.getPermission();
            }

        };
    }

    private PropertySetter createNamespace(final RepositoryNode source) {
        return new ReadonlyEnumSetter(RepositoryProperties.MANAGED_NAMESPACE) {

            @Override
            public void setValue(Object value) {
            }

            @Override
            public Object getValue() {
                return source.getNamespace();
            }

        };
    }

    private PropertySetter createURL(Repository repository) {
        if (repository instanceof RemoteRepositoryClient) {
            return createURLSetter((RemoteRepositoryClient) repository);
        } else if (source.getRepository() instanceof RepositoryManager) {
            return createLocalURL((RepositoryManager) repository);
        }
        return null;
    }

    private PropertySetter createLockedBy(final RepositoryItem repositoryItem) {
        return new ReadonlyEnumSetter(RepositoryProperties.LOCKED_BY) {

            @Override
            public void setValue(Object value) {
            }

            @Override
            public Object getValue() {
                return repositoryItem.getLockedByUser();
            }

        };
    }

    public PropertySetter createName() {
        return new ReadonlyEnumSetter(RepositoryProperties.NAME) {

            @Override
            public void setValue(Object value) {
                // do nothing
            }

            @Override
            public Object getValue() {
                return source.getRepository().getDisplayName();
            }

        };
    }

    private PropertySetter createLocalURL(final RepositoryManager repository) {
        return new ReadonlyEnumSetter(RepositoryProperties.URL) {

            @Override
            public void setValue(Object value) {
            }

            @Override
            public Object getValue() {
                return repository.getRepositoryLocation().toString();
            }

        };
    }

    private PropertySetter createURLSetter(final RemoteRepositoryClient remoteRepositoryClient) {
        return new ReadonlyEnumSetter(RepositoryProperties.URL) {

            @Override
            public void setValue(Object value) {

            }

            @Override
            public Object getValue() {
                return remoteRepositoryClient.getEndpointUrl();
            }

        };
    }

    private PropertySetter createRefreshPolicy(final RemoteRepositoryClient repository) {
        return new ReadonlyEnumSetter(RepositoryProperties.REFRESH_POLICY) {

            @Override
            public void setValue(Object value) {
                RefreshPolicy rp = RefreshPolicy.fromValue((String) value);
                repository.setRefreshPolicy(rp);
            }

            @Override
            public Object getValue() {
                return repository.getRefreshPolicy().value();
            }

            @Override
            public PropertyDescriptor createPropertyDescriptor() {
                List<String> str = new ArrayList<String>();
                for (RefreshPolicy rp : RefreshPolicy.values()) {
                    str.add(rp.value());
                }
                ComboboxPropertyDescriptor pd = new ComboboxPropertyDescriptor(getId(),
                        getDisplayName(), str);
                return pd;
            }

        };

    }

    private PropertySetter createCurrentUser(final RemoteRepositoryClient remoteRepositoryClient) {
        return new ReadonlyEnumSetter(RepositoryProperties.CURRENT_USER) {

            @Override
            public void setValue(Object value) {

            }

            @Override
            public Object getValue() {
                String userId = remoteRepositoryClient.getUserId();
                if (userId == null) {
                    userId = Messages.getString("repository.user.anonymous");
                }
                return userId;
            }
        };
    }

    abstract class ReadonlyEnumSetter extends EnumPropertySetter {

        public ReadonlyEnumSetter(
                org.opentravel.schemas.views.propertyview.AbstractPropertySource.PropertyInfo property) {
            super(property);
        }

        @Override
        public PropertyDescriptor createPropertyDescriptor() {
            return new PropertyDescriptor(getId(), getDisplayName());
        }

    }

}
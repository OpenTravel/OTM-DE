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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemas.node.handlers.NamespaceHandler;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.preferences.GeneralPreferencePage;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.propertyview.desc.CheckboxPropertyDescriptor;
import org.opentravel.schemas.views.propertyview.desc.ComboboxPropertyDescriptor;
import org.opentravel.schemas.views.propertyview.desc.TextFormPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryPropertySource extends AbstractPropertySource<LibraryNode> {

	@Override
	public String toString() {
		return Messages.getString("view.properties.library.title");
	}

	enum LibProperties implements PropertyInfo {
		NAME(Messages.getString("view.properties.library.label.name"), CAT_BASIC, Messages
				.getString("view.properties.library.tooltip.name")), PATH(Messages
				.getString("view.properties.library.label.path"), CAT_BASIC, Messages
				.getString("view.properties.library.tooltip.path")), COMMENTS(Messages
				.getString("view.properties.library.label.comments"), CAT_BASIC, Messages
				.getString("view.properties.library.tooltip.comments")), DEFAULT(Messages
				.getString("view.properties.library.label.default"), CAT_BASIC, Messages
				.getString("view.properties.library.tooltip.default")), PREFIX(Messages
				.getString("view.properties.library.label.prefix"), CAT_NAMESPACE, Messages
				.getString("view.properties.library.tooltip.prefix")), MANAGED_ROOT(Messages
				.getString("view.properties.library.label.managedRoot"), CAT_NAMESPACE, Messages
				.getString("view.properties.library.tooltip.managedRoot")), EXTENSION(Messages
				.getString("view.properties.library.label.extension"), CAT_NAMESPACE, Messages
				.getString("view.properties.library.tooltip.extension")), VERSION(Messages
				.getString("view.properties.library.label.version"), CAT_NAMESPACE, Messages
				.getString("view.properties.library.tooltip.version")), NAMESPACE(Messages
				.getString("view.properties.library.label.namespace"), CAT_NAMESPACE, Messages
				.getString("view.properties.library.tooltip.namespace")), REPOSITORY(Messages
				.getString("view.properties.library.label.repository"), CAT_REMOTE, Messages
				.getString("view.properties.library.tooltip.repository")), STATE(Messages
				.getString("view.properties.library.label.state"), CAT_REMOTE, Messages
				.getString("view.properties.library.tooltip.state")), STATUS(Messages
				.getString("view.properties.library.label.status"), CAT_REMOTE, Messages
				.getString("view.properties.library.tooltip.status")), LOCKED_BY(Messages
				.getString("view.properties.library.label.locked"), CAT_REMOTE, Messages
				.getString("view.properties.library.tooltip.locked")), FINALIZE(Messages
				.getString("view.properties.library.label.final"), CAT_REMOTE, Messages
				.getString("view.properties.library.tooltip.final")), BUTTON_BAR("", CAT_REMOTE), COMMIT(Messages
				.getString("view.properties.library.label.commit"), "", Messages
				.getString("view.properties.library.tooltip.commit")), LOCK(Messages
				.getString("view.properties.library.label.lock"), "", Messages
				.getString("view.properties.library.tooltip.lock")), MANAGE(Messages
				.getString("view.properties.library.label.manage"), "", Messages
				.getString("view.properties.library.tooltip.manage")), HISTORY(Messages
				.getString("view.properties.library.label.commitHistory"), CAT_REMOTE, Messages
				.getString("view.properties.library.tooltip.commitHistory"));

		private final String displayName;
		private final String category;
		private final String tooltip;

		@Override
		public String getTooltip() {
			return tooltip;
		}

		private LibProperties(String displayName, String category) {
			this(displayName, category, "");
		}

		private LibProperties(String displayName, String category, String tooltip) {
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

	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryPropertySource.class);

	public LibraryPropertySource(LibraryNode node) {
		super(node);
	}

	@Override
	public Collection<PropertySetter> initSetters() {
		// need to refresh library editable state before getting properites
		if (source == null || source.isDeleted())
			return Collections.emptyList();
		source.updateLibraryStatus();
		List<PropertySetter> setters = new ArrayList<PropertySetter>();
		setters.add(createName());
		setters.add(createPath());
		setters.add(createComments());
		setters.add(createMarkDefault());
		setters.add(createNamespacePolicy());
		setters.add(createNamespace());
		setters.add(createPrefix());
		setters.add(createManagedRoot());
		setters.add(createExtension());
		setters.add(createVersion());
		setters.add(createRepository());
		setters.add(createStatus());
		setters.add(createCommitHistory());
		setters.add(createButtonBar());
		return setters;
	}

	private PropertySetter createButtonBar() {
		return new PropertySetter() {

			@Override
			public Object getValue() {
				return new AbstractPropertySource<LibraryNode>(source) {

					@Override
					public Collection<PropertySetter> initSetters() {
						List<PropertySetter> l = new ArrayList<PropertySetter>();
						// 5/3/2017 dmh - works but not needed
						// switch (source.getProjectItem().getState()) {
						// case UNMANAGED:
						// l.add(createManage());
						// break;
						// case MANAGED_UNLOCKED:
						// if (TLLibraryStatus.DRAFT.equals(source.getStatus()))
						// l.add(createLock());
						// break;
						// case MANAGED_WIP:
						// l.add(createCommit());
						// l.add(createFinalize());
						// break;
						// default:
						// break;
						// }
						return l;
					}

				};
			}

			@Override
			public String getId() {
				return LibProperties.BUTTON_BAR.name();
			}

			@Override
			public IPropertyDescriptor getDescriptor() {
				PropertyDescriptor pd = new PropertyDescriptor(getId(), "");
				pd.setCategory(LibProperties.BUTTON_BAR.getCategory());
				return pd;
			}

			@Override
			public void setValue(Object value) {
			}
		};
	}

	private PropertySetter createName() {
		return new EnabledNodeSetter(LibProperties.NAME) {

			@Override
			protected PropertyDescriptor getPropertyDescriptor() {
				if (source.isManaged()) {
					return new PropertyDescriptor(getId(), getDisplayName());
				} else {
					return new TextFormPropertyDescriptor(getId(), getDisplayName());
				}
			}

			@Override
			public void setValue(Object value) {
				source.setName((String) value);
			}

			@Override
			public String getValue() {
				return source.getName();
			}

		};
	}

	private PropertySetter createMarkDefault() {
		return new EnumPropertySetter(LibProperties.DEFAULT) {

			@Override
			public void setValue(Object value) {
				if ((Boolean) value == true) {
					source.setAsDefault();
				}
			}

			@Override
			public Object getValue() {
				return source.isDefaultLibrary();
			}

			@Override
			public PropertyDescriptor createPropertyDescriptor() {
				return new CheckboxPropertyDescriptor(getId(), getDisplayName());
			}

		};
	}

	private PropertySetter createStatus() {
		return new EnabledNodeSetter(LibProperties.STATUS) {

			@Override
			public void setValue(Object value) {
				// DO NOTHING
			}

			@Override
			public Object getValue() {
				return OtmRegistry.getMainController().getLibraryController().getLibraryStatus(source);
			}

			@Override
			public PropertyDescriptor getPropertyDescriptor() {
				return new PropertyDescriptor(getId(), getDisplayName());
			}
		};
	}

	private PropertySetter createCommitHistory() {
		return new EnabledNodeSetter(LibProperties.HISTORY) {

			@Override
			public void setValue(Object value) {
				// DO NOTHING
			}

			@Override
			public Object getValue() {
				List<RepositoryItemCommit> histories = source.getCommitHistory();
				RepositoryItemCommit item = null;
				if (histories != null)
					item = histories.get(0);
				return item != null ? item.getRemarks() + " by " + item.getUser() + " on " + item.getEffectiveOn() : "";
			}

			@Override
			public PropertyDescriptor getPropertyDescriptor() {
				return new PropertyDescriptor(getId(), getDisplayName());
			}
		};
	}

	private PropertySetter createRepository() {
		return new EnabledNodeSetter(LibProperties.REPOSITORY) {

			@Override
			public void setValue(Object value) {
				// DO NOTHING
			}

			@Override
			public Object getValue() {
				return source.getRepositoryDisplayName();
			}

			@Override
			public PropertyDescriptor getPropertyDescriptor() {
				return new PropertyDescriptor(getId(), getDisplayName());
			}
		};
	}

	private PropertySetter createNamespace() {
		return new EnabledNodeSetter(LibProperties.NAMESPACE) {

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
				if (GeneralPreferencePage.areNamespacesManaged() || source.isManaged()) {
					return new PropertyDescriptor(getId(), getDisplayName());
				} else {
					return new TextFormPropertyDescriptor(getId(), getDisplayName());
				}
			}
		};
	}

	private PropertySetter createVersion() {
		return new EnumPropertySetter(LibProperties.VERSION) {

			@Override
			public void setValue(Object value) {
				// TODO: should we call here changeManagedNamespace ???
				source.setVersion((String) value);
			}

			@Override
			public Object getValue() {
				return source.getVersion();
			}

			@Override
			public PropertyDescriptor createPropertyDescriptor() {
				if (GeneralPreferencePage.areNamespacesManaged() || source.isManaged()) {
					return new PropertyDescriptor(getId(), getDisplayName());
				} else {
					return new TextFormPropertyDescriptor(getId(), getDisplayName());
				}
			}

		};
	}

	private PropertySetter createExtension() {
		return new EnumPropertySetter(LibProperties.EXTENSION) {

			@Override
			public void setValue(Object value) {
				changeManagedNamespace(source, source.getNSBase(), (String) value, source.getVersion());
			}

			@Override
			public Object getValue() {
				return source.getNSExtension();
			}

			@Override
			public PropertyDescriptor createPropertyDescriptor() {
				if (!GeneralPreferencePage.areNamespacesManaged() || !source.isEditable() || source.isManaged()) {
					return new PropertyDescriptor(getId(), getDisplayName());
				} else {
					return new TextFormPropertyDescriptor(getId(), getDisplayName());
				}
			}

		};
	}

	private PropertySetter createManagedRoot() {
		return new EnumPropertySetter(LibProperties.MANAGED_ROOT) {

			@Override
			public void setValue(Object value) {
				changeManagedNamespace(source, (String) value, source.getNSExtension(), source.getVersion());
			}

			@Override
			public Object getValue() {
				return source.getNSBase();
			}

			@Override
			public PropertyDescriptor createPropertyDescriptor() {
				ComboboxPropertyDescriptor pd = new ComboboxPropertyDescriptor(getId(), getDisplayName(),
						getNamespaces(source));
				if (!GeneralPreferencePage.areNamespacesManaged() || source.isManaged()) {
					pd.setReadonly(true);
				} else if (!source.isEditable() && !source.isAbsLibEditable()) {
					pd.setReadonly(true);
				}
				pd.setCategory(getCategoryName());
				pd.setDescription(getTooltip());
				return pd;
			}
		};
	}

	public void changeManagedNamespace(LibraryNode node, String base, String extension, String version) {
		NamespaceHandler handler = node.getNsHandler();
		String ns = handler.createValidNamespace(base, extension, version);
		String currentNS = node.getNamespace();
		if (!ns.equals(currentNS)) {
			node.setNamespace(ns);
			OtmRegistry.getMainController().getLibraryController().updateLibraryStatus();
			OtmRegistry.getMainController().refresh(node); // select the current node
			// LOGGER.debug("Changed " + node + " namespace to " + ns);
		}
	}

	private Collection<String> getNamespaces(LibraryNode node) {
		Set<String> namespaces = new LinkedHashSet<String>();
		namespaces.add(node.getParent().getNamespace());
		namespaces.addAll(NamespaceHandler.getManagedRootNamespaces());
		return stripAll(namespaces);
	}

	private Collection<String> stripAll(Set<String> namespaces) {
		List<String> ret = new ArrayList<String>(namespaces.size());
		for (String ns : namespaces) {
			ret.add(stripSlash(ns));
		}
		return ret;
	}

	private String stripSlash(String namespace) {
		if (namespace.endsWith("/")) {
			return namespace.substring(0, namespace.length() - 1);
		} else {
			return namespace;
		}
	}

	private PropertySetter createPrefix() {
		return new EnabledNodeSetter(LibProperties.PREFIX) {

			@Override
			public void setValue(Object value) {
				source.setNSPrefix((String) value);
			}

			@Override
			public Object getValue() {
				return source.getPrefix();
			}

			@Override
			public PropertyDescriptor getPropertyDescriptor() {
				return new TextFormPropertyDescriptor(getId(), getDisplayName());
			}
		};
	}

	private PropertySetter createComments() {
		return new EnabledNodeSetter(LibProperties.COMMENTS) {

			@Override
			public void setValue(Object value) {
				source.setComments((String) value);
			}

			@Override
			public Object getValue() {
				return source.getComments();
			}

			@Override
			public PropertyDescriptor getPropertyDescriptor() {
				return new TextFormPropertyDescriptor(getId(), getDisplayName(), true);
			}
		};
	}

	private PropertySetter createPath() {
		return new EnabledNodeSetter(LibProperties.PATH) {

			@Override
			public void setValue(Object value) {
				source.setPath((String) value);
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

	abstract class EnabledNodeSetter extends EnumPropertySetter {

		public EnabledNodeSetter(PropertyInfo property) {
			super(property);
		}

		@Override
		public PropertyDescriptor createPropertyDescriptor() {
			PropertyDescriptor pd;
			if (!source.isEditable()) {
				pd = new PropertyDescriptor(getId(), getDisplayName());
			} else {
				pd = getPropertyDescriptor();
			}
			return pd;
		}

		protected PropertyDescriptor getPropertyDescriptor() {
			return null;
		}

	}

}
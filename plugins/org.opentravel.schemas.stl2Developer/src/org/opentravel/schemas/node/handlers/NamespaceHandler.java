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
package org.opentravel.schemas.node.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.preferences.GeneralPreferencePage;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to manage namespace:prefix pairs for a project. A factory is provided to allow this static class to keep track
 * of all the handlers.
 * 
 * @author Dave Hollander, Agnieszka Janowska
 * 
 */
// 1/5/2017 dmh
// FIXME - this is way too complicated. Reduce dependency on stored namespaces. Make into utils and only one handler.
// Consider adding to LibraryModelManager.
// Remember - libraries and projects have namespaces
// Remember - the relationship between library and project is not 1 to 1
//
public class NamespaceHandler {
	static final Logger LOGGER = LoggerFactory.getLogger(NamespaceHandler.class);
	// private final ModelNode model;
	private final ProjectNode project;
	private final Map<String, String> namespacesAndPrefixes = new HashMap<String, String>();
	private static Map<ProjectNode, NamespaceHandler> instances = new HashMap<ProjectNode, NamespaceHandler>();

	// TODO: read this from OTA2ExtensionProvider from ota2 extension plugin
	public static final String OTA2_VERSION_SCHEME_IDENTIFIER = "OTA2";
	private VersionScheme scheme;

	/**
	 * Factory method. Retrieve a handler for each namespace owner (library). If one does not exist for the containing
	 * library, one is created.
	 * 
	 * @param library
	 *            {@code LibraryNode} managed by this {@link NamespaceHandler}
	 * @return instance of NamespaceHandler
	 */
	public static NamespaceHandler getNamespaceHandler(final LibraryNode library) {
		ProjectNode pn = null;
		if (library.getProject() != null)
			pn = library.getProject();
		else
			throw new IllegalStateException("Tried to get a namespace handler with out having a project.");

		return getNamespaceHandler(pn);
	}

	public static NamespaceHandler getNamespaceHandler(final ProjectNode project) {
		if (!instances.containsKey(project)) {
			final NamespaceHandler handler = new NamespaceHandler(project);
			instances.put(project, handler);
		}
		return instances.get(project);
	}

	private NamespaceHandler(final ProjectNode project) {
		this.project = project;
		// TODO - get scheme from library not from factory.
		try {
			scheme = VersionSchemeFactory.getInstance().getVersionScheme(OTA2_VERSION_SCHEME_IDENTIFIER);
		} catch (VersionSchemeException e) {
			e.printStackTrace();
		}

	}

	// FIXME - there is no strong relationship between library and project
	// What is instances for ???
	/**
	 * Get Prefix for the namespace. Checks all projects.
	 * 
	 * @return Returns the prefix or null.
	 */
	public String getPrefix(final String namespace) {
		String prefix = null;
		for (NamespaceHandler nsHandler : instances.values()) {
			prefix = nsHandler.getPrefixFromProject(namespace);
			if (isDefined(prefix))
				break;
		}
		return prefix;
	}

	/**
	 * Return the prefix from this handler's project or null
	 */
	protected String getPrefixFromProject(final String namespace) {
		return namespacesAndPrefixes.get(namespace);
	}

	/**
	 * @return Set of all namespaces in the project.
	 */
	// 1/4/2017 unused
	@Deprecated
	public List<String> getNamespaces() {
		List<String> namespaces = new ArrayList<String>();
		for (String ns : namespacesAndPrefixes.keySet())
			namespaces.add(ns);
		return namespaces;
	}

	// /**
	// * Rename the old namespace to the new one in all projects.
	// * Note: rename does not remove the old namespace.
	// */
	// public void rename(final String oldNS, final String newNS) {
	// for (NamespaceHandler nsHandler : instances.values()) {
	// nsHandler.renameInProject(oldNS, newNS);
	// }
	// }

	/**
	 * Rename the old namespace to the new one using this projects ns handler. Does <b>not</b> change namespace assigned
	 * to any library.
	 */
	public void rename(final String oldNS, final String newNS) {
		if (!namespacesAndPrefixes.containsKey(oldNS))
			return;

		String prefix = namespacesAndPrefixes.get(oldNS);
		// if (prefix == null || prefix.isEmpty()) {
		// LOGGER.error("Null or empty namespace prefix ");
		// }
		namespacesAndPrefixes.remove(oldNS);
		namespacesAndPrefixes.put(newNS, prefix);
	}

	/**
	 * Registers namespace and corresponding prefix of the given library with {@code this} project's
	 * NamespaceHandler.<b>Because every library is obliged to register</b>, the library constructor registers every
	 * library.
	 * 
	 * The library's prefix is checked against all registered namespaces. If namespace was already registered in any
	 * project, the prefix of the library is changed to the registered prefix so it's consistent within the entire
	 * model.
	 * 
	 * @param library
	 *            {@link LibraryNode} whose namespace is to be registered
	 * @return the prefix registered to the library's namespace
	 */
	public String registerLibrary(final LibraryNode library) {
		final String namespace = library.getNamespace();
		final String candidatePrefix = isDefined(library.getPrefix()) ? library.getPrefix() : "";
		String prefix = "";

		if (namespace == null || namespace.isEmpty()) {
			LOGGER.error("Null or empty namespace on library " + library.getName());
			throw new IllegalArgumentException("Null or empty namespace.");
		}

		// Find the right prefix - if already defined for the namespace, use the earlier definition
		prefix = getPrefix(namespace);
		if (!isDefined(prefix))
			prefix = candidatePrefix;

		if (prefix == null || prefix.isEmpty()) {
			LOGGER.error("Null or empty namespace prefix on library " + library.getName());
			prefix = "default";
			// throw new IllegalArgumentException("Null or empty namespace prefix.");
		}

		// If new, register the namespace/prefix set
		if (!namespacesAndPrefixes.containsKey(namespace)) {
			namespacesAndPrefixes.put(namespace, prefix);
		}

		// Update library if needed
		if (!prefix.equals(candidatePrefix))
			setPrefix(library, prefix);

		// LOGGER.info("Registered namespace (" + prefix + ")" + namespace + " for library " + library);
		return prefix;
	}

	/**
	 * Set the namespace of this library to the passed namespace. Assigns prefix if prefix is already defined for the
	 * namespace. Does <b>not</b> enforce namespace controls.
	 * 
	 * @param library
	 * @param newNamespace
	 * @return
	 */
	public boolean setLibraryNamespace(final LibraryNode library, final String newNamespace) {
		if (!isDefined(newNamespace)) {
			LOGGER.warn("Cannot change namespace of " + library + " to null or empty");
			return false;
		}
		String prefix = getPrefix(newNamespace);
		if (!isDefined(prefix))
			prefix = "";
		// if (prefix.isEmpty())
		// LOGGER.debug("Setting empty prefix on namespace " + newNamespace);

		if (!namespacesAndPrefixes.containsKey(newNamespace)) {
			namespacesAndPrefixes.put(newNamespace, prefix);
		}
		try {
			setNamespace(library, newNamespace);
		} catch (IllegalArgumentException e) {
			LOGGER.debug("Error setting namespace on " + library + " to " + newNamespace + " : "
					+ e.getLocalizedMessage());
			return false;
		}
		setPrefix(library, prefix);
		return true;
	}

	/**
	 * Set the prefix assigned to this namespace. Does <b>not</b> change any libraries or projects.
	 * 
	 * @param namespace
	 * @param newPrefix
	 */
	public void setNamespacePrefix(final String namespace, String newPrefix) {
		if (!isDefined(namespace) || !isDefined(newPrefix)) {
			LOGGER.warn("Tried to assign new prefix but the prefix or namespace was empty");
			return;
		}
		if (namespacesAndPrefixes.containsKey(namespace))
			namespacesAndPrefixes.put(namespace, newPrefix);
		else
			LOGGER.error("Cannot assign prefix to missing namespace " + namespace);
	}

	/**
	 * Simply set the library's namespace
	 */
	private void setNamespace(LibraryNode lib, String namespace) throws IllegalArgumentException {
		lib.getTLaLib().setNamespace(namespace);
	}

	/**
	 * Simply set the library's prefix.
	 */
	private void setPrefix(LibraryNode lib, String prefix) {
		lib.getTLaLib().setPrefix(prefix);
	}

	/**
	 * Test for null, empty or Node.Undefined
	 */
	private boolean isDefined(final String string) {
		return !(string == null || string.isEmpty() || string.equals(Node.UNDEFINED_PROPERTY_TXT));
	}

	/**
	 * Get the base namespace from the project
	 * 
	 * @return
	 */
	public String getNSBase() {
		return project.getNamespace();
	}

	/**
	 * Get the base namespace using the version scheme.
	 * 
	 * @param namespace
	 * @return
	 */
	public String getNSBase(String namespace) {
		String baseNS = removeVersion(namespace);
		String mr = findManagedRoot(baseNS);
		if (mr != null) {
			return mr;
		} else {
			return baseNS;
		}
	}

	public String removeVersion(String namespace) {
		try {
			return scheme.getBaseNamespace(namespace);
		} catch (IllegalArgumentException e) {
			// LOGGER.error("Namespace does not conform to scheme: " + namespace + "  " + e.getLocalizedMessage());
			return namespace;
		}
	}

	public String getNSExtension(String namespace) {
		String base = getNSBase(namespace);
		String extension = removeVersion(namespace).substring(base.length());
		// Strip leading /
		extension = extension.trim();
		if (extension.startsWith("/"))
			extension = extension.substring(1);
		return extension;
	}

	public String getNSVersion(String namespace) {
		String version = "";
		try {
			version = scheme.getVersionIdentifier(namespace);
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getLocalizedMessage() + " " + namespace);
		}
		return version;
	}

	public String getNS_Major(String namespace) {
		String version = "";
		try {
			version = scheme.getMajorVersion(scheme.getVersionIdentifier(namespace));
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getLocalizedMessage() + " " + namespace);
		}
		return version;
	}

	public String getNS_Minor(String namespace) {
		String version = "";
		try {
			version = scheme.getMinorVersion(scheme.getVersionIdentifier(namespace));
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getLocalizedMessage() + " " + namespace);
		}
		return version;
	}

	public String getNS_Patch(String namespace) {
		String version = "";
		try {
			version = scheme.getPatchLevel(scheme.getVersionIdentifier(namespace));
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getLocalizedMessage() + " " + namespace);
		}
		return version;
	}

	public String createValidNamespace(String base, String extension, String version) {
		String ns = base;
		if (extension != null && !extension.isEmpty()) {
			if (base.endsWith("/"))
				ns = base + extension;
			else
				ns = base + "/" + extension;
		}
		return createValidNamespace(ns, version);
	}

	public String createValidNamespace(String namespace, String version) {
		String ns = namespace;
		try {
			ns = scheme.setVersionIdentifier(namespace, version);
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getLocalizedMessage() + " " + version);
		}
		return ns;
	}

	/**
	 * 
	 * @param namespace
	 * @return empty string if valid or the reason it is not.
	 */
	public String isValidNamespace(String namespace) {
		String result = "";
		if (GeneralPreferencePage.areNamespacesManaged() && !namespace.startsWith(getNSBase()))
			return Messages.getString("library.validation.managedNS");
		try {
			if (!scheme.isValidNamespace(namespace))
				result = "Namespace does not conform to namespace policies.";
		} catch (IllegalArgumentException e) {
			result = "Error " + e.getLocalizedMessage();
			LOGGER.error(e.getLocalizedMessage() + " " + namespace);
		}
		return result;
	}

	private String findManagedRoot(String namespace) {
		for (String ns : getManagedRootNamespaces()) {
			if (!ns.isEmpty() && namespace.startsWith(ns)) {
				return ns;
			}
		}
		return null;
	}

	public static Collection<String> getManagedRootNamespaces() {
		Set<String> namespaces = new LinkedHashSet<String>();
		namespaces.addAll(OtmRegistry.getMainController().getProjectController().getSuggestedNamespaces());
		namespaces.addAll(OtmRegistry.getMainController().getRepositoryController().getRootNamespaces());
		return namespaces;
	}

	/**
	 * Get the base namespace for this node. This is simple string manipulation and assumes the last path component
	 * matching "\v" is the version information.
	 * 
	 * @param n
	 * @return this node's namespace with the last path component stripped off.
	 */
	public static String getNSBase(Node n) {
		String str = "";
		if (n != null)
			str = n.getNamespace();
		int lastSlash = str.lastIndexOf("/v");
		if (str != null && lastSlash > 0)
			str = str.substring(0, lastSlash);
		return str;
	}
}

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
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage a list of nodes that are in a version chain. Each node represents the same object in a different minor
 * version. May be shared between multiple version nodes.
 * 
 * @author Dave Hollander
 * 
 */
public class VersionManager {
	static final Logger LOGGER = LoggerFactory.getLogger(VersionManager.class);

	private List<Node> versions = null; // order: 0 is always newest

	/**
	 * 
	 * @param owner
	 *            is the version node representing this set of versioned objects. MUST be in a chain.
	 */
	public VersionManager() {
		versions = new ArrayList<>();
		// may not be set yet: assert owner.getChain() != null;
	}

	public Node getOldestVersion() {
		return versions.isEmpty() ? null : versions.get(versions.size() - 1);
	}

	public Node get() {
		return versions.isEmpty() ? null : versions.get(0);
	}

	public List<Node> getAll() {
		return versions;
	}

	public Node getHead() {
		return get();
	}

	public boolean isNewer(Node nodeToTest, Node n) {
		if (nodeToTest == null || n == null)
			return false;
		if (nodeToTest.getLibrary() == n.getLibrary())
			return nodeToTest.getName().compareTo(n.getName()) > 0; // for testing only
		return nodeToTest.isLaterVersion(n); // based on library version scheme
	}

	/**
	 * @return the previous version of the object (if any).
	 */
	public Node getPreviousVersion() {
		return versions.size() > 1 ? versions.get(1) : null;
	}

	public void add(Node nodeToAdd) {
		// if (versions.isEmpty()) versions.add(nodeToAdd);
		// else {
		if (versions.contains(nodeToAdd))
			return;

		int index = versions.size();
		for (Node n : versions)
			if (isNewer(nodeToAdd, n)) {
				index = versions.indexOf(n);
				break;
			}
		versions.add(index, nodeToAdd);
	}

	/**
	 * @return true if versions list contained n
	 */
	public boolean remove(Node n) {
		return versions.remove(n);
	}

	/**
	 * @return new list containing any older versions of this object
	 */
	public List<Node> getOlderVersions(Node version) {
		int i = versions.indexOf(version);
		List<Node> older = new ArrayList<>();
		for (Node n : versions)
			if (isNewer(version, n))
				older.add(n);
		return older;
	}

	/**
	 * @return true if this version object chain contains the passed node
	 */
	public boolean contains(Node node) {
		return versions.contains(node);
	}

	public void close() {
		for (Node n : versions)
			n.close();
		versions.clear();
	}
}

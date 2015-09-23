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
package org.opentravel.schemas.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemas.node.ContextNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.controllers.ContextNodeModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for the contexts used across the user interfaces.
 * 
 * @author Dave Hollander
 * 
 */
public class ContextModelManager {
	/*
	 * The context model has an outer map with the library as the key. The inner map then user the contextID as a key to
	 * the Context containing a node and TLContext object.
	 * 
	 * The maps manage the content exposed to the gui via the nodeManager (ContextNodeModelManager)
	 */
	private class Context {
		protected String contextId;
		protected TLContext ctx = new TLContext();
		protected ContextNode cNode = null;
		boolean isDefault = false;

		/**
		 * Creates new context object complete with new TLContext object.
		 */
		protected Context(String app, String id) {
			this.contextId = id;
			this.ctx.setApplicationContext(app);
			this.ctx.setContextId(id);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ContextModelManager.class);
	private Map<LibraryNode, Map<String, Context>> contextMap = new HashMap<LibraryNode, Map<String, Context>>();
	ContextNodeModelManager nodeManager;
	ContextNode root = null;
	private int uniqueNum = 1;
	private String ClearedName = "cleared";

	public ContextModelManager() {
		nodeManager = new ContextNodeModelManager(this);
		root = nodeManager.createContextTreeRoot();
	}

	/**
	 * Add contexts from the library to this model.
	 * 
	 * @param ln
	 */
	public void addLibraryContexts(LibraryNode ln) {
		if (ln == null || !ln.isTLLibrary() || ln.getTLLibrary() == null)
			throw new IllegalArgumentException("Incorrect libary to add contexts to.");

		if (ln.isInChain()) {
			if (ln.getChain().getHead() != null && ln != ln.getChain().getHead()) {
				addLibraryContexts(ln.getChain().getHead());
				return;
			}
		}

		TLLibrary tll = ln.getTLLibrary();
		ContextNode lcn = null;

		if (!contextMap.containsKey(ln)) {
			contextMap.put(ln, new HashMap<String, Context>());
			lcn = nodeManager.createLibraryContextNode(ln);
			lcn.setLibraryNode(ln);
			nodeManager.addNodeToParent(lcn, getRoot());

			for (TLContext tlc : tll.getContexts()) {
				if (tlc.getContextId().isEmpty()) {
					LOGGER.debug("Context ID is empty for context " + tlc.getApplicationContext());
					// Patch - don't allow empty context id
					// TODO - still need to prevent it.
					TLLibrary targetLibrary = tlc.getOwningLibrary();
					for (TLContext ctx : targetLibrary.getContexts()) {
						if (ctx.getContextId().isEmpty())
							ctx.setContextId("Imported");
					}
				}

				ContextNode leaf = addNode(ln, tlc);
				nodeManager.addNodeToParent(leaf, lcn);
			}
			setDefaultContext(ln);
			// LOGGER.debug("Added contexts from library " + ln.getName() + ". Map size = "
			// + contextMap.size() + " Library map entries: " + contextMap.get(ln).size());
		} else {
			LOGGER.debug("Context Map already contained library: " + ln);
		}
		searchForCNode(null);
	}

	// public ContextNode addNode(LibraryNode ln, Context context) {
	// context.cNode = nodeManager.createItemContextNode(context.ctx);
	// context.cNode.setLibraryNode(ln);
	// contextMap.get(ln).put(context.contextId, context);
	// return context.cNode;
	// }

	public ContextNode addNode(LibraryNode ln, TLContext tlc) {
		Context nc = new Context(tlc.getApplicationContext(), tlc.getContextId());
		nc.cNode = nodeManager.createItemContextNode(tlc);
		nc.cNode.setLibraryNode(ln);
		nc.ctx = tlc;
		Map<String, Context> cMap;
		Context ret = null;
		cMap = contextMap.get(ln);
		if (cMap == null) {
			LOGGER.error("No map entry for library " + ln);
			return null;
		}
		// if (cMap.containsValue(tlc.getApplicationContext())) {
		// TODO - this test does not work. Value is a manager whose ctx.getApplicationContext must be tested.
		// LOGGER.error("Error - map already contains app context.");
		// }
		ret = cMap.put(tlc.getContextId(), nc);
		if (ret != null) {
			LOGGER.error("Multiple context ID in same library. Corrected.");
			tlc.setContextId(tlc.getContextId() + uniqueNum++);
			addNode(ln, tlc);
		}
		return nc.cNode;
	}

	/**
	 * Clear the map of all contexts.
	 */
	public void clear() {
		contextMap.clear();
		if (root != null) {
			root.removeChildren();
		}
	}

	/**
	 * Clear the map of all contexts for the given library.
	 */
	public void clear(LibraryNode ln) {
		if (contextMap.size() <= 0)
			return;
		if (ln == null)
			return;
		if (contextMap.get(ln) == null)
			return;
		if (contextMap.get(ln).values() == null)
			return;

		// LOGGER.debug("Clearing library " + ln.getName() + " from context map. Size is now: "
		// + contextMap.size());

		Collection<Context> contexts = new ArrayList<Context>(contextMap.get(ln).values());
		for (Context c : contexts) {
			if (c == null) {
				LOGGER.warn("No contexts found for library " + ln);
				continue;
			}
			if (c.cNode.getParent() != null)
				c.cNode.getParent().removeChild(c.cNode);
			else
				LOGGER.warn("cNode " + c + " did not have a parent.");
			c.cNode.removeChildren();
			c.cNode.setParent(null);
		}

		contextMap.remove(ln);

		if (root == null) {
			LOGGER.error("Clearing context for library but the root is null!");
			return;
		}
		for (ContextNode cn : root.getChildren()) {
			if (cn.getLabel() == null || ln.getName() == null)
				break;
			// Names may not be unique, use library object itself
			if (cn.getLibraryNode() == ln) {
				// LOGGER.debug("Removing cNode for library "+ cn.getLibraryNode()+" - "+
				// cn.getLibraryNode().getTLaLib().getLibraryUrl());
				cn.getParent().removeChild(cn);
				break;
			}
		}
		// LOGGER.debug("Cleared library " + ln.getName() + " from context map. Size is now: "
		// + contextMap.size());
		// searchForId(null);
	}

	/**
	 * @return the cloned context class which is NOT added to the map or TLLibrary.
	 */
	public Context clone(Context context) {
		String appCtx = context.ctx.getApplicationContext();
		String id = context.contextId;
		Context clone = new Context(appCtx, id);
		clone.cNode = clone(context.cNode);
		return clone;
	}

	/**
	 * @return the new ContextNode cloned from the selected node, complete with new TLContext which has been added to
	 *         TLLibrary
	 */
	public ContextNode clone(ContextNode selected) {
		ContextNode newNode = newContext(selected.getLibraryNode());
		setContextId(newNode.getModelObject(), selected.getContextId() + "_Copy");
		setApplicationContext(newNode.getModelObject(), selected.getApplicationContext() + "_Copy");
		newNode.setDescription(selected.getDescription());
		return newNode;
	}

	/**
	 * Create a copy of the passed TLContext and add it as a new context node in the target library's context list. If
	 * the context already existed, nothing is done.
	 * 
	 * @param tlc
	 *            - context to add
	 * @param targetLib
	 *            - library to add the context to
	 */
	public void copyContext(TLContext tlc, LibraryNode targetLib) {
		if (contextMap.get(targetLib).get(tlc.getContextId()) != null) {
			// LOGGER.debug("No copy made because context " + targetLib.getName() +
			// " already exists.");
			return;
		}

		Context nc = new Context(tlc.getApplicationContext(), tlc.getContextId());
		targetLib.getTLLibrary().addContext(nc.ctx);

		nc.cNode = nodeManager.createItemContextNode(nc.ctx);
		nc.cNode.setLibraryNode(targetLib);
		nodeManager.addNodeToParent(nc.cNode, getLibNode(targetLib));
		contextMap.get(targetLib).put(tlc.getContextId(), nc);
		LOGGER.debug("Copied context " + tlc.getContextId() + " to " + targetLib.getName() + ".");
	}

	public ContextNode createContextNode(ContextNode selected) {
		addNode(selected.getLibraryNode(), selected.getModelObject());
		return null;
	}

	// public ContextNode createContextNode(LibraryNode ln, String id) {
	// TLContext tlc = new TLContext();
	// tlc.setContextId(id);
	// tlc.setApplicationContext(getApplicationContext(ln, id));
	// return nodeManager.createItemContextNode(tlc);
	// }

	private LibraryNode findLibraryNode(AbstractLibrary tlib) {
		if (!(tlib instanceof TLLibrary))
			return null;
		LibraryNode ln = null;
		for (LibraryNode tln : contextMap.keySet()) {
			if (tln.getTLaLib() == tlib) {
				ln = tln;
				break;
			}
		}
		return ln;
	}

	/**
	 * @return the application context associated with the library and contextId
	 */
	public String getApplicationContext(LibraryNode ln, String id) {
		if (contextMap.get(ln) == null)
			return "";
		if (contextMap.get(ln).get(id) == null)
			return "";
		return contextMap.get(ln).get(id).ctx.getApplicationContext();
	}

	public List<String> getAvailableContextIds(AbstractLibrary tlib) {
		LibraryNode ln = findLibraryNode(tlib);
		if (ln == null)
			return Collections.emptyList();
		if (contextMap.get(ln) == null)
			return Collections.emptyList();
		return new ArrayList<String>(contextMap.get(ln).keySet());
	}

	public List<String> getAvailableContextIds(LibraryNode ln) {
		if (ln == null)
			return Collections.emptyList();
		if (contextMap.get(ln) == null)
			return Collections.emptyList();
		return new ArrayList<String>(contextMap.get(ln).keySet());
	}

	public TLContext getDefaultContext(TLLibrary tlib) {
		if (contextMap.size() <= 0) {
			LOGGER.debug("Context access with no contexts recorded.");
			return null;
		}
		if (tlib == null)
			throw new IllegalArgumentException("No library to determine which context is default.");

		TLContext ctx = null;
		LibraryNode ln = findLibraryNode(tlib);
		String id = getDefaultContextId(ln);
		if (contextMap.get(ln) != null && contextMap.get(ln).get(id) != null)
			ctx = contextMap.get(ln).get(id).ctx;
		if (ctx == null)
			throw new IllegalStateException("Missing default context for " + tlib.getName());
		// LOGGER.debug("Get default "+ctx.getContextId()+" for library "+ln.getName());
		return ctx;
	}

	public String getDefaultContextId(LibraryNode ln) {
		String id = "";
		if (ln == null)
			return id;
		if (contextMap.get(ln) == null)
			return id;

		for (Entry<String, Context> e : contextMap.get(ln).entrySet()) {
			if (e.getValue().isDefault) {
				id = e.getValue().contextId;
				break;
			}
		}
		if (id.isEmpty())
			id = setDefaultContext(ln, id);
		return id;
	}

	private ContextNode getLibNode(LibraryNode ln) {
		for (ContextNode cn : root.getChildren())
			if (cn.getLibraryNode().equals(ln))
				return cn;
		return null;
	}

	public ContextNode getNode(LibraryNode ln, String id) {
		if (contextMap.get(ln) == null)
			return null;
		if (contextMap.get(ln).get(id) == null)
			return null;

		return contextMap.get(ln).get(id).cNode;
	}

	/**
	 * @return the nodeManager
	 */
	public ContextNodeModelManager getNodeManager() {
		return nodeManager;
	}

	/**
	 * @return the root
	 */
	public ContextNode getRoot() {
		if (root == null)
			root = nodeManager.createContextTreeRoot();
		if (root == null)
			throw new IllegalStateException("Could not initialize the context tree.");
		return root;
	}

	/**
	 * Merge the selected context node into the target node.
	 */
	public void merge(ContextNode selected, ContextNode target) {
		if (selected == null || target == null)
			throw new IllegalArgumentException("Must have context nodes to merge.");
		if (selected.getLibraryNode() == null)
			throw new IllegalArgumentException("Selected context node must have a library.");

		// Change all users of selected to target and remove selected from TL Model
		LibraryNode sLib = selected.getLibraryNode();
		sLib.mergeContext(target.getContextId());

		// Remove selected from map.
		contextMap.get(sLib).remove(selected.getContextId());
		nodeManager.removeContextNodeFromParent(selected);

		// LOGGER.info("Merged contexts " + selected.getContextId() + " and " +
		// target.getContextId());
	}

	/**
	 * Create a new context in the passed library.
	 */
	public ContextNode newContext(LibraryNode ln) {
		Context nc = new Context("New Application Context", "NewContext");
		ln.getTLLibrary().addContext(nc.ctx);
		nc.cNode = nodeManager.createItemContextNode(nc.ctx);
		nc.cNode.setLibraryNode(ln);
		nodeManager.addNodeToParent(nc.cNode, getLibNode(ln));
		contextMap.get(ln).put("NewContext", nc);
		return nc.cNode;
	}

	/**
	 * Clear all default indicators on the library
	 */
	private void resetDefaultContext(LibraryNode ln) {
		for (Context c : contextMap.get(ln).values()) {
			c.isDefault = false;
		}
	}

	/**
	 * Set the application context
	 */
	public String setApplicationContext(TLContext tlc, String newAppCtx) {
		LibraryNode ln = findLibraryNode(tlc.getOwningLibrary());
		Map<String, Context> cMap = contextMap.get(ln);
		Context c = cMap.get(tlc.getContextId());
		if (c == null) {
			LOGGER.debug("Missing Context record for id " + tlc.getContextId());
			LOGGER.debug("Known values are: " + cMap.keySet());
			// throw new IllegalStateException("Missing Context record for id "+tlc.getContextId());
		}
		// TEST - make sure this is a unique application context for this library
		for (Context ctx : cMap.values()) {
			if (ctx.ctx.getApplicationContext().equals(newAppCtx)) {
				LOGGER.warn("Application context was not unique. Appended " + uniqueNum);
				newAppCtx = newAppCtx + uniqueNum++;
			}
		}
		c.ctx.setApplicationContext(newAppCtx);
		LOGGER.debug("Set Application Context to: " + newAppCtx);
		return newAppCtx; // may be changed to be unique
	}

	/**
	 * Set Context ID to a new value.
	 */
	public String setContextId(TLContext tlc, String newId) {
		LibraryNode ln = findLibraryNode(tlc.getOwningLibrary());
		Map<String, Context> cMap = contextMap.get(ln);
		if (ln == null || cMap == null)
			return newId;
		Context c = cMap.get(tlc.getContextId());
		if (c != null) {
			// This node has the same id value as the desired value.
			// If the application value is the same, then remove before adding.
			// Else make the ID unique then add it.
			String existingAppCtx = tlc.getApplicationContext();
			String useThisId = newId;
			boolean removeFirst = false;
			for (Context ctx : cMap.values()) {
				if (ctx.ctx.getApplicationContext() != null && ctx.ctx.getApplicationContext().equals(existingAppCtx))
					removeFirst = true;
				else if (newId.equals(useThisId)) {
					// LOGGER.warn("Application context ID was not unique. Appended "+uniqueNum);
					useThisId = newId + uniqueNum++;
				}
			}
			if (removeFirst)
				cMap.remove(tlc.getContextId());
			c.contextId = newId;
			c.ctx.setContextId(newId);
			cMap.put(newId, c);
		} else {
			LOGGER.error("Could not find context to set new id value. " + tlc.getContextId());
		}
		// LOGGER.debug("Known values are: " + cMap.keySet());
		// LOGGER.debug("Set Context id to: " + newId);
		return newId;
	}

	/**
	 * Using the model node, get all libraries and add all found contexts into the map.
	 */
	public void setContexts() {
		ModelNode modelNode = Node.getModelNode();
		if (modelNode == null)
			return;

		for (LibraryNode ln : modelNode.getUserLibraries()) {
			setContexts(ln);
		}
	}

	public void setContexts(LibraryNode ln) {
		if (ln == null)
			return;
		if (!ln.isTLLibrary())
			return;

		if (!contextMap.containsKey(ln)) {
			addLibraryContexts(ln);
		}
		LOGGER.debug("Set contexts for library: " + ln.getName() + ".");
	}

	/**
	 * Sets the default to the context node.
	 * 
	 * @return true if successful, false if another context ID was set to default.
	 */
	public boolean setDefaultContext(ContextNode cn) {
		return (setDefaultContext(cn.getLibraryNode(), cn.getContextId()).equals(cn.getContextId()));
	}

	public String setDefaultContext(LibraryNode ln) {
		return setDefaultContext(ln, "XXXXXXXXXX1XXXXXXXXXytkldjlskdja;d"); // use the not found
																			// behavior
	}

	/**
	 * Sets the default to the passed contextId if found, to the first contextId if not.
	 * 
	 * @return default context id
	 */
	public String setDefaultContext(LibraryNode ln, String contextID) {
		String retId = "";
		if (ln == null)
			return retId;
		if (!ln.isTLLibrary())
			return retId;
		if (!contextMap.containsKey(ln))
			return retId;
		if (contextMap.get(ln).size() <= 0)
			return retId;

		resetDefaultContext(ln);
		if ((contextID != null) && (!contextID.isEmpty()) && contextMap.get(ln).get(contextID) != null) {
			contextMap.get(ln).get(contextID).isDefault = true;
			retId = contextID;
		} else {
			Map<String, Context> cm = contextMap.get(ln);
			for (Context c : cm.values()) {
				c.isDefault = true;
				retId = c.contextId;
				break;
			}
		}
		// LOGGER.debug("Set default for library "+ln.getName()+" to "+retId);
		return retId;
	}

	/**
	 * Search entire context map to find all equal cNodes.
	 */
	private int searchForCNode(ContextNode cNode) {
		int foundCount = 0;
		for (LibraryNode lib : contextMap.keySet()) {
			for (Context c : contextMap.get(lib).values()) {
				if (c.cNode == cNode)
					foundCount++;
			}
		}
		return foundCount;
	}

	/**
	 * Search the context map and node tree looking for the passed id string. Debugging Utils private boolean
	 * searchForId(String id) { if (id == null || id.isEmpty()) id = ClearedName; boolean ret = false;
	 * 
	 * // makes sure all cNodes in the map are unique. for (LibraryNode lib : contextMap.keySet()) { for ( Context c :
	 * contextMap.get(lib).values()) { if (searchForCNode(c.cNode) > 1) LOGGER.error("Shared cNode! "+c.contextId); } }
	 * 
	 * 
	 * // Context map for (LibraryNode lib : contextMap.keySet()) { for ( String thisId : contextMap.get(lib).keySet())
	 * { // LOGGER.debug("Testing context id of "+thisId); if (thisId.startsWith(id)) {
	 * LOGGER.debug("Found context id of "+thisId); ret = true; } } // Look into the TL Library TLLibrary tlLib =
	 * lib.getTLLibrary(); for (TLContext tlc : tlLib.getContexts()) { //
	 * LOGGER.debug("Testing tl context id of "+tlc.getContextId()); if (tlc.getContextId().startsWith(id)) {
	 * LOGGER.debug("Found tl context id of "+tlc.getContextId()); ret = true; } } }
	 * 
	 * // Check the node tree List<ContextNode> children = root.getChildren(); for (ContextNode cn : root.getChildren())
	 * { LOGGER.debug(""); LOGGER.debug("Testing node context library of "+cn.getLabel
	 * ()+" - "+cn.getLibraryNode().getTLaLib().getLibraryUrl()); if
	 * (cn.getType().equals(ContextNode.ContextNodeType.LIBRARY_ROOT)) { if (cn.getChildren().isEmpty())
	 * LOGGER.debug("No children of library node: "+cn.getLabel()+" - "+
	 * cn.getLibraryNode().getTLaLib().getLibraryUrl()); for (ContextNode childCN : cn.getChildren()) {
	 * LOGGER.debug("Testing node context id of "+childCN.getLabel()+" class = "+childCN); if (childCN.getLabel() ==
	 * null) { LOGGER.debug("Found NULL context id of "+childCN); } else if (childCN.getLabel().startsWith(id)) {
	 * LOGGER.debug("Found context id of "+childCN.getLabel()); ret = true; } } } } LOGGER.debug(""); return ret; }
	 */
}

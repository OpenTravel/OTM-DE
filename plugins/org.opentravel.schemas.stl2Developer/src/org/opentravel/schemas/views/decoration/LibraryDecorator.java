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
package org.opentravel.schemas.views.decoration;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.controllers.MainController.IRefreshListener;
import org.opentravel.schemas.controllers.ValidationManager;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryChainNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryInstanceNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryRootNsNode;
import org.opentravel.schemas.types.whereused.WhereUsedNode;

public class LibraryDecorator extends BaseLabelProvider implements ILightweightLabelDecorator, IRefreshListener {

	public LibraryDecorator() {
		OtmRegistry.getMainController().addRefreshListener(this);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		if (element instanceof LibraryNode) {
			return true;
		}
		return false;
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		// Decorate Library Nav Nodes as if they were the library the link to a project.
		if (!(element instanceof Node))
			return;

		Node node = (Node) element;
		// If versioned, get the head of the versioned object chain
		if (node instanceof VersionNode)
			node = ((VersionNode) node).get();

		if (node instanceof LibraryNavNode)
			node = (Node) ((LibraryNavNode) node).getThisLib();

		if (node instanceof LibraryNode) {
			decoration.addSuffix(getLibraryDecoration((LibraryNode) node));
			addOverlay(node, decoration);
		} else if (node instanceof LibraryChainNode) {
			decoration.addSuffix(getLibraryDecoration(((LibraryChainNode) node).getHead()));
			addOverlay(node, decoration);

		} else if (node instanceof RepositoryInstanceNode)
			decoration.addSuffix(getRepositoryNameDecoration((RepositoryInstanceNode) node));
		else if (node instanceof RepositoryChainNode)
			decoration.addSuffix(getNamespaceDecoration((RepositoryChainNode) node));
		else if (node instanceof RepositoryItemNode)
			decoration.addSuffix(getRepositoryItemDecoration(((RepositoryItemNode) node).getItem()));
		else if (node instanceof RepositoryRootNsNode) {
			decoration.addSuffix(getNamespaceDecoration((RepositoryRootNsNode) node));

		} else if (node instanceof ResourceNode) {
			decoration.addSuffix(((ResourceNode) node).getDecoration());
			addOverlay(node, decoration);

		} else if (node instanceof ResourceMemberInterface) {
			decoration.addSuffix(node.getDecoration());
			addOverlay(node, decoration);
			// if (!node.isValid())
			// decoration.addOverlay(errorDesc(), IDecoration.BOTTOM_LEFT);
			// else if (!node.isValid_NoWarnings())
			// decoration.addOverlay(warningDesc(), IDecoration.BOTTOM_LEFT);

		} else if (node instanceof WhereUsedNode) {
			decoration.addSuffix(node.getDecoration());

		} else if (node instanceof InheritedInterface)
			decoration.addSuffix(node.getDecoration());
		else if (node instanceof NavNode) {
			decoration.addSuffix(node.getDecoration());
		} else if (node instanceof LibraryMemberInterface) {
			decoration.addSuffix(node.getDecoration());
			addOverlay(node, decoration);
		} else if (node instanceof ContextualFacetNode) {
			decoration.addSuffix(node.getDecoration());
			addOverlay(node, decoration);
		} else if (node instanceof ContributedFacetNode) {
			decoration.addSuffix(node.getDecoration());
			addOverlay(node, decoration);
		}

	}

	private static ImageDescriptor errorDesc = Images.getImageRegistry().getDescriptor(Images.ErrorDecoration);
	private static ImageDescriptor warningDesc = Images.getImageRegistry().getDescriptor(Images.WarningDecoration);

	private void addOverlay(Node node, IDecoration decoration) {
		if (node instanceof InheritedInterface)
			return;
		ValidationFindings findings = ValidationManager.validate(node);
		if (findings != null)
			if (!ValidationManager.isValid(findings, FindingType.ERROR))
				decoration.addOverlay(errorDesc, IDecoration.BOTTOM_LEFT);
			else if (!ValidationManager.isValid(findings, FindingType.WARNING))
				decoration.addOverlay(warningDesc, IDecoration.BOTTOM_LEFT);
	}

	private String getRepositoryNameDecoration(RepositoryInstanceNode element) {
		if (element.getRepository() instanceof RemoteRepositoryClient) {
			RemoteRepositoryClient rc = (RemoteRepositoryClient) element.getRepository();
			String userID = rc.getUserId();
			if (rc.getUserId() == null) {
				userID = Messages.getString("repository.user.anonymous");
			}
			return surround("User: " + userID);
		}
		return "";
	}

	private String getNamespaceDecoration(RepositoryChainNode node) {
		StringBuilder sb = new StringBuilder();
		sb.append(" ");
		sb.append(node.getChildren().size());
		sb.append(surround(node.getPermission()));
		return sb.toString();
	}

	private String getNamespaceDecoration(RepositoryRootNsNode node) {
		StringBuilder sb = new StringBuilder();
		sb.append(" ");
		sb.append(getChildCount(node));
		sb.append(surround(node.getPermission()));
		return sb.toString();
	}

	@Override
	public void refresh() {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}

	@Override
	public void refresh(INode node) {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this, node));
	}

	private String getRepositoryItemDecoration(RepositoryItem item) {
		String status = translateStatusState(item.getStatus(), item.getState(), item.getLockedByUser(), false);
		return getDecoration(status, item.getVersion());
	}

	public static String translateStatusState(TLLibraryStatus status, RepositoryItemState state, String lockedBy,
			boolean isEditableInGui) {
		return translateStatusState(status, state, lockedBy, isEditableInGui, false);
	}

	public static String translateStatusState(TLLibraryStatus status, RepositoryItemState state, String lockedBy,
			boolean isEditableInGui, boolean isMinor) {
		if (state == RepositoryItemState.BUILT_IN)
			return "Built-in";

		if (status == null)
			return "NULL Status";
		if (status.equals(TLLibraryStatus.FINAL))
			return "Final";
		if (status.equals(TLLibraryStatus.UNDER_REVIEW))
			return "Under Review";
		if (status.equals(TLLibraryStatus.OBSOLETE))
			return "Obsolete";

		switch (state) {
		case MANAGED_LOCKED:
			return "Locked: " + lockedBy;
		case MANAGED_UNLOCKED:
			return "Draft";
		case MANAGED_WIP:
			if (isEditableInGui)
				if (isMinor)
					return "Editable - Minor";
				else
					return "Editable";
			else
				return "Locked: " + lockedBy;
		case UNMANAGED:
			return "";
		case BUILT_IN:
			// should be handled before checking if final
			return "Built-in";
		default:
			return "Unknown";
		}
	}

	private String getChildCount(RepositoryRootNsNode node) {
		int count = node.getChildren().size();
		return "(" + count + ")";
	}

	private String getLibraryDecoration(LibraryNode lib) {
		if (lib == null)
			return " ";
		return "  " + getDecoration(getLibraryVersion(lib), getLibraryStatus(lib));
	}

	public String getLibraryStatus(LibraryNode lib) {
		return OtmRegistry.getMainController().getLibraryController().getLibraryStatus(lib);
	}

	public String getLibraryVersion(LibraryNode lib) {
		return lib.getLibraryVersion();
	}

	private String getDecoration(String status, String version) {
		if (status.isEmpty() && version.isEmpty())
			return "";
		return surround(status + " " + version);
	}

	private String surround(String txt) {
		if (txt != null && !txt.isEmpty()) {
			return " [" + txt + "]";
		}
		return "";
	}

	@Override
	public void dispose() {
	}

}

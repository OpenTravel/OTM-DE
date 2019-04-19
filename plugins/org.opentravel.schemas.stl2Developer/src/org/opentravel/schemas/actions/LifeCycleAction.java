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

package org.opentravel.schemas.actions;

import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * change life cycle status of a library
 * 
 * This action handler handles all life cycles based on constructor's passed target status setting.
 *
 * @author Dave Hollander
 * 
 */
public class LifeCycleAction extends OtmAbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger( LifeCycleAction.class );

    private static StringProperties propsDefault =
        new ExternalizedStringProperties( "action.library.lifecycle.review" );
    private static StringProperties propsFinal =
        new ExternalizedStringProperties( "action.library.lifecycle.finalize" );
    private static StringProperties propsReview = new ExternalizedStringProperties( "action.library.lifecycle.review" );
    private static StringProperties propsObsolete =
        new ExternalizedStringProperties( "action.library.lifecycle.obsolete" );
    private TLLibraryStatus targetStatus = null;

    public LifeCycleAction(TLLibraryStatus targetStatus) {
        super( propsDefault );
        this.targetStatus = targetStatus;
        // Override the default properties
        switch (targetStatus) {
            case FINAL:
                initialize( propsFinal );
                break;
            case UNDER_REVIEW:
                initialize( propsReview );
                break;
            case OBSOLETE:
                initialize( propsObsolete );
                break;
            default:
                break;
        }
    }

    /**
     * run repository controller create major version
     */
    @Override
    public void run() {
        RepositoryController rc = mc.getRepositoryController();
        if (rc == null) {
            this.notifyResult( false );
            return;
        }
        for (Node node : mc.getSelectedNodes_NavigatorView()) {
            node = node.getLibrary();
            if (!(node.getLibrary() instanceof LibraryNode)) {
                DialogUserNotifier.openWarning( "Error", "Internal error - no owning librar." );
                continue;
            }
            LibraryNode ln = node.getLibrary();
            if (ln.getTLModelObject().getOwningModel() == null) {
                DialogUserNotifier.openWarning( "Error", "Internal error - no owning model." );
                continue;
            }
            switch (targetStatus) {
                case FINAL:
                    mc.postStatus( "Finalize " + ln );
                    break;
                case UNDER_REVIEW:
                    mc.postStatus( "Under Review " + node );
                    break;
                case OBSOLETE:
                    mc.postStatus( "Obsolete " + node );
                    break;
                default:
                    return;
            }
            this.notifyResult( rc.promote( ln, targetStatus ) );
        }
    }

    @Override
    public boolean isEnabled() {
        LibraryNode ln = null;
        Node n = mc.getSelectedNode_NavigatorView();
        if (n != null)
            ln = n.getLibrary();
        if (ln == null)
            return false;
        if (ln.getProjectItem() == null)
            return false;

        // RepositoryItemState state = ln.getProjectItem().getState();
        // TLLibraryStatus status = ln.getProjectItem().getStatus();
        // LOGGER.debug(ln + " status = " + status + " state = " + state + " next = " +
        // ln.getStatus().nextStatus());

        // Don't allow lock unless library is in a project with managing namespace
        if (!ln.isInProjectNS())
            return false;
        // Make sure the library is ready for the next status state
        if (ln.getProjectItem().getStatus() == null || ln.getProjectItem().getStatus().nextStatus() == null)
            return false;
        if (!ln.getProjectItem().getStatus().nextStatus().equals( targetStatus ))
            return false;

        // Obsolete only be enabled if the library is 1.6 and 1.6 mode
        if (targetStatus != null && targetStatus.equals( TLLibraryStatus.OBSOLETE )) {
            if (!RepositoryUtils.isOTM16Library( ln.getProjectItem().getContent() ))
                return false;
        }
        return ln.isManaged();
    }
}

package org.opentravel.schemas.actions;

import org.opentravel.schemas.commands.RepositoryHandler;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

public class SyncRepositoryAction extends OtmAbstractAction {
    private static final StringProperties propsDefault = new ExternalizedStringProperties(
            "action.repository.sync");

    public SyncRepositoryAction(StringProperties props) {
        super(props);
    }

    public SyncRepositoryAction() {
        super(propsDefault);
    }

    @Override
    public void run() {
        new RepositoryHandler().sync();
    }
}

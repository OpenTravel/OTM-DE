package com.sabre.schemas.actions;

import com.sabre.schemas.commands.RepositoryHandler;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;

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

package org.opentravel.schemas.modelObject.events;

import org.opentravel.schemacompiler.event.OwnershipEvent;

public abstract class OwnershipEventListener<S, V> extends
        SourceEventListener<S, OwnershipEvent<S, V>> {

    public OwnershipEventListener(S source) {
        super(source);
    }

    @Override
    public void processModelEvent(OwnershipEvent<S, V> event) {

    }

}
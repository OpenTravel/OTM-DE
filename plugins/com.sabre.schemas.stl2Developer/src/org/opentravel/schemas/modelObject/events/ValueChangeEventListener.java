package org.opentravel.schemas.modelObject.events;

import org.opentravel.schemacompiler.event.ValueChangeEvent;

public abstract class ValueChangeEventListener<S, V> extends
        SourceEventListener<S, ValueChangeEvent<S, V>> {

    public ValueChangeEventListener(S source) {
        super(source);
    }

    @Override
    public void processModelEvent(ValueChangeEvent<S, V> event) {

    }

}

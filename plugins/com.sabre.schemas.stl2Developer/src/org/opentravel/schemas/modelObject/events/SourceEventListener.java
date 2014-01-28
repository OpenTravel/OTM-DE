package org.opentravel.schemas.modelObject.events;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventListener;
import com.sabre.schemacompiler.event.ModelEventType;

public abstract class SourceEventListener<S, E extends ModelEvent<S>> implements
        ModelEventListener<E, S> {

    protected final S source;

    public SourceEventListener(S source) {
        this.source = source;
    }

    public S getSource() {
        return source;
    }

    public boolean supported(ModelEventType type) {
        return true;
    }

    @Override
    public Class<?> getEventClass() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<S> getSourceObjectClass() {
        return (Class<S>) getSource().getClass();
    }

}
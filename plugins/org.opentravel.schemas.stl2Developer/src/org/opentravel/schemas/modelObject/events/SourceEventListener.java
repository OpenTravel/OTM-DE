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
package org.opentravel.schemas.modelObject.events;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventListener;
import org.opentravel.schemacompiler.event.ModelEventType;

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
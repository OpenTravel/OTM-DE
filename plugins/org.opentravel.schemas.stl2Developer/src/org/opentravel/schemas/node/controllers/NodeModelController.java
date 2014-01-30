/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node.controllers;

import java.util.List;

/**
 * Controls underlaying model on behalf of Node
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface NodeModelController<T> {

    T createChild();

    void removeChild(T child);

    List<T> getChildren();

    T getChild(int index);

    T getChild(Object key);

    void moveChildUp(T child);

    void moveChildDown(T child);

}

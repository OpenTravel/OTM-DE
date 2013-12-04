/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.types;

import java.util.ConcurrentModificationException;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeNameUtils;

/**
 * @author Pawel Jedruch
 * 
 */
public class PostTypeChange {

    /**
     * Because changing name from {@link Type#setAssignedType(Node)} in case of setting the family
     * will cause {@link ConcurrentModificationException} (in context of TypeResolver.resolveTypes
     * visitor), this method is in purpose of catching the new type assignments. From GUI this can
     * be achieved from different actions (e.g. DND, or assign button).
     * 
     * <p>
     * Later it should be removed and replaced by future model notifications or other future that
     * will eliminate {@link ConcurrentModificationException}.
     * </p>
     * 
     * @param property
     *            - property to whom assigned new type
     * @param newType
     *            - newType assigned
     */
    public static void notyfications(Node property, Node newType) {
        NodeNameUtils.fixName(property);

    }

}

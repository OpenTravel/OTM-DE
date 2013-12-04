/*
 * Copyright (c) 2012, Sabre Inc.
 */
package com.sabre.schemas.testers;

import org.eclipse.core.expressions.PropertyTester;

import com.sabre.schemacompiler.repository.RemoteRepository;
import com.sabre.schemas.trees.repository.RepositoryNode;
import com.sabre.schemas.trees.repository.RepositoryNode.RepositoryNameNode;

public class RepositoryTypeTester extends PropertyTester {

    public static final String TYPE_REMOTE = "remote";
    public static final String TYPE_LOCAL = "local";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (!(receiver instanceof RepositoryNode)) {
            return false;
        }
        RepositoryNameNode node = (RepositoryNameNode) receiver;
        if (TYPE_REMOTE.equals(property)) {
            return isRemoteRepository(node);
        } else if (TYPE_LOCAL.equals(property)) {
            return !isRemoteRepository(node);
        }
        return false;
    }

    private boolean isRemoteRepository(RepositoryNode selected) {
        return selected.getRepository() instanceof RemoteRepository;
    }
}

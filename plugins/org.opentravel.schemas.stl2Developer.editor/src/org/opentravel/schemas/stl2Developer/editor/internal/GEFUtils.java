/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;

/**
 * @author Pawel Jedruch
 * 
 */
public class GEFUtils {

    @SuppressWarnings("unchecked")
    public static <T> List<T> extractModels(List<EditPart> selection, Class<T> clazz) {
        List<T> nodes = new ArrayList<T>(selection.size());
        for (EditPart ep : selection) {
            Object model = ep.getModel();
            if (clazz.isAssignableFrom(model.getClass())) {
                nodes.add((T) model);
            }
        }
        return nodes;
    }

    public static EditPart getEditPartToSelect(EditPart ep) {
        SelectionRequest req = new SelectionRequest();
        req.setType(RequestConstants.REQ_SELECTION);
        return ep.getTargetEditPart(req);
    }

}

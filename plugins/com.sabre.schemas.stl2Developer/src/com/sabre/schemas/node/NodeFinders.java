/**
 * 
 */
package com.sabre.schemas.node;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.modelObject.XsdModelingUtils;

/**
 * @author Dave Hollander
 * 
 */
public class NodeFinders {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFinders.class);

    /**
     * Find a node by name. NOTE - node could be a property that is the same as a object.
     * 
     * @return the first node that matches the Name and Namespace in the model starting from the
     *         root.
     */
    public static Node findNodeByName(final String name, final String ns) {
        Node n = Node.getModelNode().findNode(name, ns);
        // LOGGER.debug("findNodeByName returning: "+n);
        return n;
    }

    /**
     * @return the first type provider node that matches the QName in the model starting from the
     *         root or null. If QName is not found, the local part name is tried with the chameleon
     *         namespace.
     */
    public static Node findTypeProviderByQName(final QName qname) {
        if (qname == null)
            return null;

        Node n = Node.getModelNode().findNode_TypeProvider(qname.getLocalPart(),
                qname.getNamespaceURI());
        if (n == null)
            n = Node.getModelNode().findNode_TypeProvider(qname.getLocalPart(),
                    XsdModelingUtils.ChameleonNS);
        // LOGGER.debug("findNodeByQName ("+qname+") returning: "+n);
        return n;
    }

    /**
     * @return the first type provider node in the passed libary that matches the QName in the model
     *         starting from the root or null. If QName is not found, the local part name is tried
     *         with the chameleon namespace.
     */
    public static Node findTypeProviderByQName(final QName qname, Node ln) {
        if (qname == null)
            return null;
        if (ln == null)
            ln = Node.getModelNode();

        Node n = ln.findNode_TypeProvider(qname.getLocalPart(), qname.getNamespaceURI());
        if (n == null)
            n = ln.findNode_TypeProvider(qname.getLocalPart(), XsdModelingUtils.ChameleonNS);
        // LOGGER.debug("findNodeByQName ("+qname+") returning: "+n);
        return n;
    }

    /**
     * @return the first node that matches the QName in the model starting from the root or null.
     */
    public static Node findNodeByQName(final QName qname) {
        if (qname == null)
            return null;
        Node n = Node.getModelNode().findNode(qname.getLocalPart(), qname.getNamespaceURI());
        // LOGGER.debug("findNodeByQName ("+qname+") returning: "+n);
        return n;
    }

    /**
     * @return the first node that matches the validationIdentity from tl model starting from the
     *         root.
     */
    public static Node findNodeByValidationIentity(final String validationIdentity) {
        return Node.getModelNode().findNode(validationIdentity);
    }

    /**
     * @return the first node that matches the unique node ID starting from the model root.
     */
    public static Node findNodeByID(final String ID) {
        return Node.getModelNode().findNodeID(ID);
    }

}

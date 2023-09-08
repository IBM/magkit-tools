package com.aperto.magnolia.edittools.export;

import info.magnolia.importexport.exporter.YamlExporter;
import org.apache.jackrabbit.commons.NamespaceHelper;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author ala.abudheileh
 * 06.09.2023
 */
public class YamlSinglePageExporter extends YamlExporter {

    private final Set _shareables = new HashSet();
    private boolean _share = false;
    private final Session _session;
    private final NamespaceHelper _helper;
    private final boolean _recurse;




    public YamlSinglePageExporter(Session session, ContentHandler handler, boolean recurse, boolean binary) {
        super(session, handler, recurse, binary);
        _recurse = recurse;
        _session = session;
        _helper = new NamespaceHelper(session);
    }

    @Override
    protected void exportNodes(Node node)
            throws RepositoryException, SAXException {
        if (_recurse && !_share) {
            NodeIterator iterator = node.getNodes();
            while (iterator.hasNext()) {
                Node child = iterator.nextNode();
                if (Objects.equals(child.getPrimaryNodeType().getName(), "mgnl:page")) {
                    continue;
                }
                exportNode(child);
            }
        }
    }

    private void exportNode(Node node)
            throws RepositoryException, SAXException {

        _share = node.isNodeType(_helper.getJcrName("mix:shareable"))
                && !_shareables.add(node.getUUID());

        if (node.getDepth() == 0) {
            exportNode(NamespaceHelper.JCR, "root", node);
        } else {
            String name = node.getName();
            int colon = name.indexOf(':');
            if (colon == -1) {
                exportNode("", name, node);
            } else {
                String uri = _session.getNamespaceURI(name.substring(0, colon));
                exportNode(uri, name.substring(colon + 1), node);
            }
        }
    }
}
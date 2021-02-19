package com.aperto.magnolia.edittools.m6.action;

import com.aperto.magkit.utils.NodeUtils;
import com.google.inject.Inject;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.location.LocationController;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Node;

/**
 * Action to open the jump to the browser sub app with the current location.
 *
 * @author frank.sommer
 * @since 15.01.16
 */
public class OpenPageBrowserLocationAction extends OpenAppViewLocationAction {

    private final ValueContext<Node> _valueContext;

    @Inject
    public OpenPageBrowserLocationAction(final OpenPageBrowserLocationActionDefinition definition, final ValueContext<Node> valueContext, final LocationController locationController) {
        super(definition, locationController);
        _valueContext = valueContext;
    }

    @Override
    protected String getNodePath() {
        return _valueContext.getSingle()
            .map(node -> NodeUtils.getAncestorOrSelf(node, NodeUtils.IS_PAGE))
            .map(NodeUtil::getPathIfPossible)
            .orElse(StringUtils.EMPTY);
    }
}
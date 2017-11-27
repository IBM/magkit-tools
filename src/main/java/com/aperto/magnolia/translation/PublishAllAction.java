package com.aperto.magnolia.translation;

import com.google.inject.Inject;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.framework.action.ActivationAction;
import info.magnolia.ui.framework.action.ActivationActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Get parent translation folder node and activate all sub nodes recursive.
 *
 * @author Janine.Naumann
 */
public class PublishAllAction extends ActivationAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishAllAction.class);

    @Inject
    public PublishAllAction(ActivationActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named("admincentral") EventBus admincentralEventBus, SubAppContext uiContext, SimpleTranslator i18n) {
        super(definition, item, commandsManager, admincentralEventBus, uiContext, i18n);
    }

    @Override
    protected List<JcrItemAdapter> getSortedItems(final Comparator comparator) {
        List<JcrItemAdapter> sortedItems = super.getSortedItems(comparator);
        try {
            Session session = MgnlContext.getJCRSession("translation");
            Node rootNode = session.getRootNode();
            Iterable<Node> nodesToActivate = NodeUtil.getNodes(rootNode, TranslationNodeTypes.Translation.NAME);
            sortedItems = new ArrayList<>();
            for (Node node : nodesToActivate) {
                sortedItems.add(new JcrNodeAdapter(node));
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error getting all child items.", e);
        }
        return sortedItems;
    }
}
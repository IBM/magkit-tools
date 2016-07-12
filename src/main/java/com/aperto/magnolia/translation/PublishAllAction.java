package com.aperto.magnolia.translation;

import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.framework.action.ActivationAction;
import info.magnolia.ui.framework.action.ActivationActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

import static info.magnolia.context.Context.ATTRIBUTE_RECURSIVE;
import static java.lang.Boolean.TRUE;

/**
 * Get parent translation folder node and activate all sub nodes recursive.
 *
 * @author Janine.Naumann
 */
@SuppressWarnings("rawtypes")
public class PublishAllAction extends ActivationAction {

    private final List<JcrItemAdapter> _items;
    private Map<JcrItemAdapter, Exception> _failedItems;
    private final UiContext _uiContext;
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishAllAction.class);

    @Inject
    public PublishAllAction(ActivationActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named("admincentral") EventBus admincentralEventBus, SubAppContext uiContext, SimpleTranslator i18n) {
        super(definition, item, commandsManager, admincentralEventBus, uiContext, i18n);
        _uiContext = uiContext;
        _items = new ArrayList<>(1);
        Item parentNode = null;
        try {
            if (item.getJcrItem().getDepth() > 0) {
                parentNode = item.getJcrItem().getAncestor(1);
            }
        } catch (RepositoryException e) {
            LOGGER.warn("Can't translation root for activation.", e);
        }
        if (parentNode != null) {
            _items.add(new JcrNodeAdapter((Node) parentNode));
        }
    }

    @Override
    protected Map<String, Object> buildParams(final Item jcrItem) {
        Map<String, Object> params = super.buildParams(jcrItem);
        params.put(ATTRIBUTE_RECURSIVE, TRUE);
        return params;
    }

    @Override
    public void execute() throws ActionExecutionException {
        _failedItems = new LinkedHashMap<>();

        for (Object item : getItems(getItemComparator())) {
            setCurrentItem((JcrItemAdapter) item);
            try {
                executeOnItem((JcrItemAdapter) item);
            } catch (Exception ex) {
                _failedItems.put((JcrItemAdapter) item, ex);
            }
        }
        setCurrentItem(null);

        if (_failedItems.isEmpty()) {
            String message = getSuccessMessage();
            if (StringUtils.isNotBlank(message)) {
                _uiContext.openNotification(MessageStyleTypeEnum.INFO, true, message);
            }
        } else {
            String message = getErrorNotification();
            if (StringUtils.isNotBlank(message)) {
                _uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, message);
            }
        }
    }

    protected String getErrorNotification() {
        String failureMessage = getFailureMessage();
        if (failureMessage != null) {
            StringBuilder notification = new StringBuilder(failureMessage);
            notification.append("<ul>");
            for (JcrItemAdapter item : _failedItems.keySet()) {
                Exception ex = _failedItems.get(item);
                notification.append("<li>").append("<b>");
                notification.append(JcrItemUtil.getItemPath(item.getJcrItem())).append("</b>: ").append(ex.getMessage());
                notification.append("</li>");
            }
            notification.append("</ul>");
            return notification.toString();
        }
        return null;
    }

    private List<JcrItemAdapter> getItems(Comparator<JcrItemAdapter> comparator) {
        return Ordering.from(comparator).sortedCopy(_items);
    }
}
package com.aperto.magnolia.translation.csv;

import au.com.bytecode.opencsv.CSVReader;
import com.aperto.magnolia.translation.TranslationNodeTypes;
import com.aperto.magnolia.translation.setup.TranslationModule;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PN_KEY;
import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;
import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static info.magnolia.objectfactory.Components.getComponent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Translation import csv action.
 *
 * @author frank.sommer
 * @since 1.0.5
 */
public class ImportCsvAction extends AbstractAction<ConfiguredActionDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportCsvAction.class);

    private final Item _item;
    private final EditorValidator _validator;
    private final EditorCallback _callback;
    private final EventBus _eventBus;
    private final Collection<Locale> _locales;
    private final ContentConnector _contentConnector;

    @Inject
    public ImportCsvAction(final ConfiguredActionDefinition definition, final Item item, final EditorValidator validator, final EditorCallback callback, @Named(AdmincentralEventBus.NAME) final EventBus eventBus, final ContentConnector contentConnector, final I18nContentSupport i18nContentSupport) {
        super(definition);
        _item = item;
        _validator = validator;
        _callback = callback;
        _eventBus = eventBus;
        _contentConnector = contentConnector;
        _locales = i18nContentSupport.getLocales();
    }

    @Override
    public void execute() throws ActionExecutionException {
        _validator.showValidation(true);
        if (_validator.isValid()) {
            AbstractJcrNodeAdapter item = (AbstractJcrNodeAdapter) _item;
            AbstractJcrNodeAdapter importXml = item.getChild("importCsv");
            if (importXml != null) {
                String path = getPathIfPossible(item.getJcrItem());
                final TranslationModule module = getComponent(TranslationModule.class);
                if (StringUtils.equals(path, "/") && isNotBlank(module.getBasePath())) {
                    path = module.getBasePath();
                }
                doCsvImport(path, importXml);
            }

            _callback.onSuccess(getDefinition().getName());
            _eventBus.fireEvent(new ContentChangedEvent(_contentConnector.getDefaultItemId()));
        } else {
            LOGGER.info("Validation error(s) occurred. No Import performed.");
        }
    }

    private void doCsvImport(final String basePath, final AbstractJcrNodeAdapter importXml) {
        try (InputStream inputStream = ((BinaryImpl) importXml.getItemProperty("jcr:data").getValue()).getStream()) {
            CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream, getPropertyValue("encoding", UTF_8.name())), getPropertyValue("separator", ",").charAt(0));
            List<String[]> lines = csvReader.readAll();
            if (CollectionUtils.isNotEmpty(lines)) {
                Map<Integer, String> indexedPropertyNames = detectColumns(lines.get(0));
                persistTranslations(basePath, indexedPropertyNames, lines);
            } else {
                LOGGER.warn("No lines in csv file, skip ...");
            }
        } catch (RepositoryException | IOException e) {
            LOGGER.error("Error importing csv data.", e);
        }
    }

    private String getPropertyValue(final String propertyId, final String defaultValue) {
        String encoding = defaultValue;
        Property encProperty = _item.getItemProperty(propertyId);
        if (encProperty != null) {
            encoding = defaultIfEmpty((String) encProperty.getValue(), defaultValue);
        }
        return encoding;
    }

    private void persistTranslations(final String basePath, final Map<Integer, String> indexedPropertyNames, final List<String[]> lines) {
        try {
            Session jcrSession = MgnlContext.getJCRSession(WS_TRANSLATION);
            Node baseNode;
            if (isEmpty(basePath)) {
                baseNode = jcrSession.getRootNode();
            } else {
                baseNode = jcrSession.getNode(basePath);
            }
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i);
                String key = values[0];
                String keyNodeName = getComponent(NodeNameHelper.class).getValidatedName(key);

                Node t9nNode;
                if (!baseNode.hasNode(keyNodeName)) {
                    t9nNode = baseNode.addNode(keyNodeName, TranslationNodeTypes.Translation.NAME);
                    PropertyUtil.setProperty(t9nNode, PN_KEY, key);
                } else {
                    t9nNode = baseNode.getNode(keyNodeName);
                }

                for (Map.Entry<Integer, String> column : indexedPropertyNames.entrySet()) {
                    Integer keyIndex = column.getKey();
                    if (keyIndex > 0) {
                        PropertyUtil.setProperty(t9nNode, column.getValue(), values[keyIndex]);
                    }
                }
                NodeTypes.LastModified.update(t9nNode);
            }
            jcrSession.save();
        } catch (RepositoryException e) {
            LOGGER.error("Error persisting CSV to JCR.", e);
        }
    }

    private Map<Integer, String> detectColumns(final String[] headings) {
        Map<Integer, String> cols = new TreeMap<>();
        int index = 0;
        for (String heading : headings) {
            if (heading.equals(TranslationCsvWriter.COLUMN_KEY)) {
                cols.put(index, PN_KEY);
            } else {
                for (Locale locale : _locales) {
                    if (heading.equals(locale.getDisplayName())) {
                        cols.put(index, PREFIX_NAME + locale.getLanguage());
                        break;
                    }
                }
            }
            index++;
        }
        return cols;
    }
}

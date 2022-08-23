package com.aperto.magnolia.translation.setup;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.objectfactory.Components;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static info.magnolia.jcr.util.PropertyUtil.setProperty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * Task for adding missing translations from legacy properties to app.
 *
 * @author frank.sommer
 * @since 27.11.2017
 */
public class AddTranslationsToAppTask extends AbstractTask {
    private final String _baseName;
    private final String[] _languages;
    private final String _basePath;
    private final NodeNameHelper _nodeNameHelper;

    public AddTranslationsToAppTask(String baseName, String... languages) {
        this("Add translations for " + baseName,
            "Add translations for " + baseName + " in languages " + join(languages, ", "),
            baseName,
            EMPTY,
            languages
        );
    }

    public AddTranslationsToAppTask(String name, String description, String baseName, String basePath, String... languages) {
        super(name, description);
        _baseName = baseName;
        _languages = languages;
        _basePath = StringUtils.defaultString(basePath) + "/";
        _nodeNameHelper = Components.getComponent(NodeNameHelper.class);
    }

    @Override
    public void execute(final InstallContext installContext) {
        if (isNotBlank(_baseName) && _languages != null) {
            try {
                Session session = installContext.getJCRSession(WS_TRANSLATION);
                for (String language : _languages) {
                    Locale locale = retrieveLocale(language);
                    ResourceBundle bundle = ResourceBundle.getBundle(_baseName, locale);
                    for (String key : bundle.keySet()) {
                        String keyNodeName = _nodeNameHelper.getValidatedName(key);
                        if (session.itemExists(_basePath + keyNodeName)) {
                            Node translationNode = session.getNode(_basePath + keyNodeName);
                            String currentTranslation = getString(translationNode, Translation.PREFIX_NAME + language, "");
                            if (isNotEmpty(currentTranslation)) {
                                installContext.info("Translation already set, skip " + keyNodeName + " ...");
                            } else {
                                String translation = trimToEmpty(bundle.getString(key));
                                setProperty(translationNode, Translation.PREFIX_NAME + language, translation);
                            }
                        } else {
                            installContext.info("Translation node " + keyNodeName + " does not exists.");
                        }
                    }
                }
            } catch (RepositoryException e) {
                installContext.error("Can not write translations.", e);
            }
        }
    }

    private Locale retrieveLocale(String language) {
        Locale locale;
        if (language.contains("_")) {
            locale = new Locale(substringBefore(language, "_"), substringAfter(language, "_"));
        } else {
            locale = new Locale(language);
        }
        return locale;
    }
}

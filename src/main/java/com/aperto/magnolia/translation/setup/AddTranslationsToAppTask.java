package com.aperto.magnolia.translation.setup;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.cms.core.Path.getValidatedLabel;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static info.magnolia.jcr.util.PropertyUtil.setProperty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
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

    public AddTranslationsToAppTask(String baseName, String... languages) {
        super("Add translations for " + baseName, "Add translations for " + baseName + " in languages " + join(languages, ", "));
        _baseName = baseName;
        _languages = languages;
    }

    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        if (isNotBlank(_baseName) && _languages != null) {
            try {
                Session session = installContext.getJCRSession(WS_TRANSLATION);
                for (String language : _languages) {
                    ResourceBundle bundle = ResourceBundle.getBundle(_baseName, new Locale(language));
                    for (String key : bundle.keySet()) {
                        String keyNodeName = getValidatedLabel(key);
                        if (session.itemExists("/" + keyNodeName)) {
                            Node translationNode = session.getNode("/" + keyNodeName);
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
}

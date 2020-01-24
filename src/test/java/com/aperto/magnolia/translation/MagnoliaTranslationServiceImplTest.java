package com.aperto.magnolia.translation;

import com.aperto.magkit.mockito.ComponentsMockUtils;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.i18nsystem.DefaultMessageBundlesLoader;
import info.magnolia.i18nsystem.FixedLocaleProvider;
import info.magnolia.i18nsystem.util.MessageFormatterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.aperto.magkit.mockito.ComponentsMockUtils.clearComponentProvider;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the translation service.
 *
 * @author frank.sommer
 * @since 24.01.2020
 */
public class MagnoliaTranslationServiceImplTest {

    private FixedLocaleProvider _localeProvider;
    private MagnoliaTranslationServiceImpl _translationService;

    @Before
    public void setUp() {
        _localeProvider = new FixedLocaleProvider(Locale.GERMAN);

        MessagesManager messagesManager = ComponentsMockUtils.mockComponentInstance(MessagesManager.class);
        when(messagesManager.getDefaultLocale()).thenReturn(Locale.ENGLISH);

        Provider<DefaultMessageBundlesLoader> messageProvider = mock(Provider.class);
        DefaultMessageBundlesLoader messageBundlesLoader = mock(DefaultMessageBundlesLoader.class);
        when(messageProvider.get()).thenReturn(messageBundlesLoader);

        _translationService = new MagnoliaTranslationServiceImpl(null, messageProvider) {
            @Override
            String doMessageQuery(final String key, final String i18nProperty) {
                Map<String, String> messagesFromApp = new HashMap<>();
                messagesFromApp.put("invalid'key", "---");
                messagesFromApp.put("empty.key", "");
                messagesFromApp.put("blank.key", " ");
                messagesFromApp.put("existing.key", "valueFromApp");
                messagesFromApp.put("quote.key", "key'without");
                messagesFromApp.put("placeholder.key", "key'with {0}");
                messagesFromApp.put("escaped.key", "key''with {0}");
                return messagesFromApp.get(key);
            }
        };
    }

    /**
     * Do no search query and handle like a message key.
     */
    @Test
    public void invalidKey() {
        assertThat(_translationService.translate(_localeProvider, "", new String[]{"invalid'key"}), equalTo("invalid'key"));
    }

    /**
     * On missing key, the key itself is returned.
     */
    @Test
    public void missingKey() {
        assertThat(_translationService.translate(_localeProvider, "", new String[]{"missing.key"}), equalTo("missing.key"));
    }

    /**
     * Handles empty key like missing key.
     */
    @Test
    public void emptyKey() {
        assertThat(_translationService.translate(_localeProvider, "", new String[]{"empty.key"}), equalTo("empty.key"));
    }

    /**
     * Blank value is a valid value.
     */
    @Test
    public void blankKey() {
        assertThat(_translationService.translate(_localeProvider, "", new String[]{"blank.key"}), equalTo(" "));
    }

    /**
     * Existing value is a valid value.
     */
    @Test
    public void existingKey() {
        assertThat(_translationService.translate(_localeProvider, "", new String[]{"existing.key"}), equalTo("valueFromApp"));
    }

    /**
     * Values with single quote with placeholder and without.
     */
    @Test
    public void replacementEscaping() {
        assertThat(_translationService.translate(_localeProvider, "", new String[]{"quote.key"}), equalTo("key'without"));

        String placeholderValue = _translationService.translate(_localeProvider, "", new String[]{"placeholder.key"});
        assertThat(placeholderValue, equalTo("key''with {0}"));
        assertThat(MessageFormatterUtils.format(placeholderValue, Locale.GERMAN, "replacement"), equalTo("key'with replacement"));

        placeholderValue = _translationService.translate(_localeProvider, "", new String[]{"escaped.key"});
        assertThat(placeholderValue, equalTo("key''with {0}"));
        assertThat(MessageFormatterUtils.format(placeholderValue, Locale.GERMAN, "replacement"), equalTo("key'with replacement"));
    }

    @After
    public void tearDown() {
        clearComponentProvider();
    }
}
package com.aperto.magnolia.translation;

import com.aperto.magkit.mockito.ComponentsMockUtils;
import com.google.common.collect.ImmutableMap;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.i18nsystem.DefaultMessageBundlesLoader;
import info.magnolia.i18nsystem.FixedLocaleProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
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

        Provider messageProvider = mock(Provider.class);
        DefaultMessageBundlesLoader messageBundlesLoader = mock(DefaultMessageBundlesLoader.class);
        when(messageProvider.get()).thenReturn(messageBundlesLoader);

        _translationService = new MagnoliaTranslationServiceImpl(null, messageProvider) {
            @Override
            String doMessageQuery(final String key, final String i18nProperty) {
                Map<String, String> messagesFromApp = ImmutableMap.of("invalid'key", "---", "empty.key", "", "blank.key", " ", "existing.key", "valueFromApp");
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

    @After
    public void tearDown() {
        clearComponentProvider();
    }
}
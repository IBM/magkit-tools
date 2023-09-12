package com.aperto.magnolia.translation;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.User;
import info.magnolia.ui.field.EditorPropertyDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static com.aperto.magkit.mockito.ComponentsMockUtils.mockComponentInstance;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubUser;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the form creation.
 *
 * @author frank.sommer
 * @since 23.08.2022
 */
public class TranslationFormDefinitionTest {
    @Before
    public void setUp() throws Exception {
        final User user = mock(User.class);
        when(user.getLanguage()).thenReturn("en");
        mockWebContext(stubUser(user));
    }

    @Test
    public void testLocaleFields() {
        final I18nContentSupport i18nContentSupport = mockComponentInstance(I18nContentSupport.class);
        when(i18nContentSupport.getLocales()).thenReturn(List.of(Locale.ENGLISH, Locale.UK));

        final TranslationFormDefinition translationFormDefinition = new TranslationFormDefinition();
        final List<EditorPropertyDefinition> properties = translationFormDefinition.getProperties();

        assertThat(properties.size(), is(2));
        assertThat(properties.get(0).getLabel(), equalTo("English"));
        assertThat(properties.get(1).getLabel(), equalTo("English (United Kingdom)"));
    }

    @After
    public void tearDown() {
        cleanContext();
    }
}

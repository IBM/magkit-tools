package de.ibmix.magkit.tools.t9n;

/*-
 * #%L
 * magkit-tools-t9n
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.userprofile.LocaleSettingsProfile;
import info.magnolia.cms.security.userprofile.UserProfileManager;
import info.magnolia.ui.contentapp.configuration.column.ColumnDefinition;
import info.magnolia.ui.contentapp.configuration.column.ConfiguredColumnDefinition;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubDefaultLocale;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubUser;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockUser;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link TranslationListViewDefinition}.
 *
 * @author wolf.bubenik IBM iX
 * @since 2025-12-05
 */
public class TranslationListViewDefinitionTest {
    private static final List<Pair<String, String>> COLUMNS = List.of(Pair.of("key", "Key"), Pair.of("translation_en", "English"));

    private TranslationListViewDefinition<String> _viewDefinition;

    @BeforeEach
    public void setUp() throws RepositoryException {
        final UserProfileManager userProfileManager = ComponentsMockUtils.mockComponentInstance(UserProfileManager.class);
        when(userProfileManager.getUserProfile(any(), eq(LocaleSettingsProfile.class))).thenAnswer(invocation -> {
            final User user = invocation.getArgument(0);
            final LocaleSettingsProfile settingsProfile = new LocaleSettingsProfile();
            settingsProfile.setLanguage(new Locale(substringBefore(user.getName(), "_")));
            return settingsProfile;
        });

        final User user = mockUser("en_user");
        mockWebContext(stubUser(user));
        mockI18nContentSupport(stubDefaultLocale(Locale.ENGLISH));

        _viewDefinition = new TranslationListViewDefinition<>();

        final ConfiguredColumnDefinition<String> keyCol = new ConfiguredColumnDefinition<>();
        keyCol.setName(TranslationNodeTypes.Translation.PN_KEY);
        keyCol.setLabel("Key");
        _viewDefinition.setColumns(List.of(keyCol));
    }

    /**
     * Verifies that getColumns returns translation columns followed by parent columns.
     */
    @Test
    public void testDefaultTranslationColumns() {
        final List<ColumnDefinition<String>> columns = _viewDefinition.getColumns();
        assertEquals(2, columns.size(), "Should have 2 translation columns (key + 1 locale)");

        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition<String> column = columns.get(i);
            final Pair<String, String> expectedColumn = COLUMNS.get(i);
            assertEquals(expectedColumn.getKey(), column.getName(), "Column at position " + i + " should be the " + expectedColumn.getKey() + " column");
            assertTrue(column.isSortable(), "Column " + column.getName() + " should be sortable");
            assertEquals(expectedColumn.getValue(), column.getLabel(), expectedColumn.getValue() + " label should be present");
        }
    }

    /**
     * Verifies that user locale influences translation column labels.
     */
    @Test
    public void testGermanEditorColumnLabels() throws RepositoryException {
        final User germanUser = mockUser("de_user");
        mockWebContext(stubUser(germanUser));
        mockI18nContentSupport(stubDefaultLocale(Locale.FRENCH));

        final List<ColumnDefinition<String>> columns = _viewDefinition.getColumns();
        assertEquals(2, columns.size(), "Should have 2 translation columns (key + 1 locale)");

        final ColumnDefinition<String> localeColumn = columns.get(1);
        assertEquals("Französisch", localeColumn.getLabel(), "French locale should display in German user's language");
    }

    /**
     * Verifies further language columns.
     */
    @Test
    public void testFurtherLanguageColumns() throws RepositoryException {
        final User germanUser = mockUser("de_user");
        mockWebContext(stubUser(germanUser));

        _viewDefinition.setFurtherLanguages("de, de_CH");
        final List<ColumnDefinition<String>> columns = _viewDefinition.getColumns();
        assertEquals(4, columns.size(), "Should have 4 translation columns (key + default locale + 2 further locales)");

        final ColumnDefinition<String> firstColumn = columns.get(1);
        assertEquals("Englisch", firstColumn.getLabel());
        final ColumnDefinition<String> secondColumn = columns.get(2);
        assertEquals("Deutsch", secondColumn.getLabel());
        final ColumnDefinition<String> thirdColumn = columns.get(3);
        assertEquals("Deutsch (Schweiz)", thirdColumn.getLabel());
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }
}


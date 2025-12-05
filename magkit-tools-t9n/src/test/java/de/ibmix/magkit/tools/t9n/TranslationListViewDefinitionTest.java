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

import info.magnolia.cms.security.User;
import info.magnolia.ui.contentapp.configuration.column.ColumnDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubLocales;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubUser;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockUser;
import static de.ibmix.magkit.test.cms.security.UserStubbingOperation.stubLanguage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TranslationListViewDefinition}.
 *
 * @author wolf.bubenik IBM iX
 * @since 2025-12-05
 */
public class TranslationListViewDefinitionTest {

    @BeforeEach
    public void setUp() throws RepositoryException {
        cleanContext();
        final User user = mockUser("TestUser", stubLanguage("en"));
        mockWebContext(stubUser(user));
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    /**
     * Verifies that getColumns returns translation columns followed by parent columns.
     */
    @Test
    public void testGetColumnsOrdersTranslationColumnsFirst() {
        final TranslationListViewDefinition<String> viewDefinition = new TranslationListViewDefinition<>();
        final List<ColumnDefinition<String>> columns = viewDefinition.getColumns();

        assertNotNull(columns, "Columns list should not be null");
        assertTrue(columns.size() > 0, "Should contain at least translation columns");

        final ColumnDefinition<String> firstColumn = columns.get(0);
        assertEquals("key", firstColumn.getName(), "First column should be the key column");
        assertNull(firstColumn.getLabel(), "Key column label should be null");
    }

    /**
     * Verifies translation columns are created for each configured locale.
     */
    @Test
    public void testTranslationColumnsCreatedForEachLocale() throws RepositoryException {
        mockI18nContentSupport(stubLocales(Locale.ENGLISH, Locale.GERMANY, Locale.FRANCE));

        final TranslationListViewDefinition<String> viewDefinition = new TranslationListViewDefinition<>();
        final List<ColumnDefinition<String>> columns = viewDefinition.getColumns();

        assertEquals(4, columns.size(), "Should have 4 translation columns (key + 3 locales)");

        final ColumnDefinition<String> keyColumn = columns.get(0);
        assertEquals("key", keyColumn.getName(), "First column should be key");
        assertEquals("key", keyColumn.getPropertyName(), "Property name should be key");
        assertNull(keyColumn.getLabel(), "Key column label should be null");

        final ColumnDefinition<String> englishColumn = columns.get(1);
        assertEquals(TranslationNodeTypes.Translation.PREFIX_NAME + Locale.ENGLISH, englishColumn.getName());
        assertEquals(TranslationNodeTypes.Translation.PREFIX_NAME + Locale.ENGLISH, englishColumn.getPropertyName());
        assertEquals("English", englishColumn.getLabel(), "English locale should display as 'English'");

        final ColumnDefinition<String> germanyColumn = columns.get(2);
        assertEquals(TranslationNodeTypes.Translation.PREFIX_NAME + Locale.GERMANY, germanyColumn.getName());
        assertEquals("German", germanyColumn.getLabel(), "Germany locale should display language name");

        final ColumnDefinition<String> franceColumn = columns.get(3);
        assertEquals(TranslationNodeTypes.Translation.PREFIX_NAME + Locale.FRANCE, franceColumn.getName());
        assertEquals("French", franceColumn.getLabel(), "France locale should display language name");
    }

    /**
     * Verifies that all translation columns are sortable.
     */
    @Test
    public void testAllTranslationColumnsAreSortable() {
        final TranslationListViewDefinition<String> viewDefinition = new TranslationListViewDefinition<>();
        final List<ColumnDefinition<String>> columns = viewDefinition.getColumns();

        for (ColumnDefinition<String> column : columns) {
            assertTrue(column.isSortable(), "Column " + column.getName() + " should be sortable");
        }
    }

    /**
     * Verifies that all translation columns have fixed width of 300 pixels.
     */
    @Test
    public void testAllTranslationColumnsHaveFixedWidth() {
        final TranslationListViewDefinition<String> viewDefinition = new TranslationListViewDefinition<>();
        final List<ColumnDefinition<String>> columns = viewDefinition.getColumns();

        for (ColumnDefinition<String> column : columns) {
            assertEquals(300, column.getWidth(), "Column " + column.getName() + " should have width of 300");
        }
    }

    /**
     * Verifies that buildTranslationColumn creates properly configured columns.
     */
    @Test
    public void testBuildTranslationColumnConfiguration() {
        final TranslationListViewDefinition<String> viewDefinition = new TranslationListViewDefinition<>();

        final ColumnDefinition<String> column = viewDefinition.buildTranslationColumn("testColumn", "Test Label");

        assertNotNull(column, "Built column should not be null");
        assertEquals("testColumn", column.getName(), "Column name should match");
        assertEquals("testColumn", column.getPropertyName(), "Property name should match column name");
        assertEquals("Test Label", column.getLabel(), "Column label should match");
        assertTrue(column.isSortable(), "Column should be sortable");
        assertEquals(300, column.getWidth(), "Column width should be 300");
    }

    /**
     * Verifies that buildTranslationColumn handles null labels correctly.
     */
    @Test
    public void testBuildTranslationColumnWithNullLabel() {
        final TranslationListViewDefinition<String> viewDefinition = new TranslationListViewDefinition<>();

        final ColumnDefinition<String> column = viewDefinition.buildTranslationColumn("keyColumn", null);

        assertNotNull(column, "Built column should not be null");
        assertEquals("keyColumn", column.getName(), "Column name should match");
        assertNull(column.getLabel(), "Column label should be null");
    }

    /**
     * Verifies that user locale influences translation column labels.
     */
    @Test
    public void testUserLocaleInfluencesColumnLabels() throws RepositoryException {
        final User germanUser = mockUser("DeutschUser", stubLanguage("de"));
        mockWebContext(stubUser(germanUser));
        mockI18nContentSupport(stubLocales(Locale.FRANCE, Locale.ITALY));

        final TranslationListViewDefinition<String> viewDefinition = new TranslationListViewDefinition<>();
        final List<ColumnDefinition<String>> columns = viewDefinition.getColumns();

        assertEquals(3, columns.size(), "Should have 3 translation columns (key + 2 locales)");

        final ColumnDefinition<String> franceColumn = columns.get(1);
        assertEquals("Französisch", franceColumn.getLabel(),
            "French locale should display in German user's language");

        final ColumnDefinition<String> italyColumn = columns.get(2);
        assertEquals("Italienisch", italyColumn.getLabel(),
            "Italian locale should display in German user's language");
    }

    /**
     * Verifies correct behavior with single locale configuration.
     */
    @Test
    public void testSingleLocaleConfiguration() throws RepositoryException {
        mockI18nContentSupport(stubLocales(Locale.ENGLISH));

        final TranslationListViewDefinition<String> viewDefinition = new TranslationListViewDefinition<>();
        final List<ColumnDefinition<String>> columns = viewDefinition.getColumns();

        assertEquals(2, columns.size(), "Should have 2 columns (key + 1 locale)");

        final ColumnDefinition<String> keyColumn = columns.get(0);
        assertEquals("key", keyColumn.getName(), "First column should be key");

        final ColumnDefinition<String> englishColumn = columns.get(1);
        assertEquals("English", englishColumn.getLabel(), "English locale should be present");
    }

    /**
     * Verifies handling of multiple locales with complex locale objects.
     */
    @Test
    public void testMultipleLocalesWithCountryCode() throws RepositoryException {
        mockI18nContentSupport(stubLocales(Locale.US, Locale.CANADA, Locale.CANADA_FRENCH));

        final TranslationListViewDefinition<String> viewDefinition = new TranslationListViewDefinition<>();
        final List<ColumnDefinition<String>> columns = viewDefinition.getColumns();

        assertEquals(4, columns.size(), "Should have 4 columns (key + 3 locales)");

        for (ColumnDefinition<String> column : columns) {
            assertNotNull(column.getName(), "Column name should not be null");
            assertNotNull(column.getPropertyName(), "Column property name should not be null");
            assertTrue(column.isSortable(), "Column should be sortable");
            assertEquals(300, column.getWidth(), "Column width should be 300");
        }
    }
}


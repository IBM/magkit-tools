/*-
 * #%L
 * IBM iX Magnolia Kit Tools Translation
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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

package de.ibmix.magkit.tools.t9n.csv;

import com.vaadin.server.Page;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.cms.core.FileSystemHelper;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.jcr.Node;
import javax.jcr.query.Query;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubLocales;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PN_KEY;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PREFIX_NAME;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExportTranslationAsCsvAction} verifying CSV export behavior.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-19
 */
class ExportTranslationAsCsvActionTest {

    @TempDir
    private File _tempDir;

    private ConfiguredActionDefinition _definition;
    private ValueContext<Node> _valueContext;
    private I18nContentSupport _i18nContentSupport;
    private FileSystemHelper _fileSystemHelper;

    @BeforeEach
    void setUp() throws Exception {
        _definition = mock(ConfiguredActionDefinition.class);
        _valueContext = mock(ValueContext.class);
        _i18nContentSupport = mockI18nContentSupport(stubLocales(Locale.ENGLISH, Locale.GERMAN));
        _fileSystemHelper = mockComponentInstance(FileSystemHelper.class);
        when(_fileSystemHelper.getTempDirectory()).thenReturn(_tempDir);
        mockWebContext(stubJcrSession(WS_TRANSLATION));
        Page currentPage = mock(Page.class);
        UI currentUi = mock(UI.class);
        when(currentUi.getPage()).thenReturn(currentPage);
        CurrentInstance.set(UI.class, currentUi);
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    /**
     * Verifies export of selected translation nodes produces CSV with expected data.
     */
    @Test
    void executeExportsSelectedNodes() throws Exception {
        Node node1 = mockNode(WS_TRANSLATION, "/greeting", stubProperty(PN_KEY, "greeting"), stubProperty(PREFIX_NAME + "en", "Hello"), stubProperty(PREFIX_NAME + "de", "Hallo"));
        Node node2 = mockNode(WS_TRANSLATION, "/farewell", stubProperty(PN_KEY, "farewell"), stubProperty(PREFIX_NAME + "en", "Bye"), stubProperty(PREFIX_NAME + "de", "Tschuess"));

        when(_valueContext.get()).thenReturn(List.of(node1, node2).stream());
        when(_valueContext.getSingle()).thenReturn(Optional.of(node1));

        Set<String> beforeNames = existingCsvNames();
        ExportTranslationAsCsvAction action = new ExportTranslationAsCsvAction(_definition, _valueContext, _i18nContentSupport, _fileSystemHelper);
        action.execute();
        File csvFile = findNewCsv(beforeNames);
        assertNotNull(csvFile);

        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), UTF_8))) {
            List<String[]> lines = reader.readAll();
            assertEquals(3, lines.size());
            assertEquals("Key", lines.get(0)[0]);
            assertEquals("farewell", lines.get(1)[0]);
            assertEquals("Bye", lines.get(1)[1]);
            assertEquals("Tschuess", lines.get(1)[2]);
            assertEquals("greeting", lines.get(2)[0]);
            assertEquals("Hello", lines.get(2)[1]);
            assertEquals("Hallo", lines.get(2)[2]);
        }
    }

    /**
     * Verifies containsOnlyRootNode returns true for root node selection.
     */
    @Test
    void containsOnlyRootNodeTrueForRootSelection() throws Exception {
        Node root = MgnlContext.getJCRSession(WS_TRANSLATION).getRootNode();
        when(_valueContext.getSingle()).thenReturn(Optional.of(root));
        when(_valueContext.get()).thenReturn(List.of(root).stream());
        ExportTranslationAsCsvAction action = new ExportTranslationAsCsvAction(_definition, _valueContext, _i18nContentSupport, _fileSystemHelper);
        Method m = ExportTranslationAsCsvAction.class.getDeclaredMethod("containsOnlyRootNode");
        m.setAccessible(true);
        assertTrue((boolean) m.invoke(action));
    }

    /**
     * Verifies containsOnlyRootNode returns false for non-root node selection.
     */
    @Test
    void containsOnlyRootNodeFalseForNonRootSelection() throws Exception {
        Node node = mockNode(WS_TRANSLATION, "/sample", stubProperty(PN_KEY, "sample"));
        when(_valueContext.getSingle()).thenReturn(Optional.of(node));
        when(_valueContext.get()).thenReturn(List.of(node).stream());
        ExportTranslationAsCsvAction action = new ExportTranslationAsCsvAction(_definition, _valueContext, _i18nContentSupport, _fileSystemHelper);
        assertFalse(action.containsOnlyRootNode());
    }

    /**
     * Verifies containsOnlyRootNode returns false when valueContext is empty.
     */
    @Test
    void containsOnlyRootNodeFalseWhenValueContextEmpty() {
        when(_valueContext.getSingle()).thenReturn(Optional.empty());
        ExportTranslationAsCsvAction action = new ExportTranslationAsCsvAction(_definition, _valueContext, _i18nContentSupport, _fileSystemHelper);
        assertFalse(action.containsOnlyRootNode());
    }

    /**
     * Verifies retrieveTranslationNodes uses query when root node is selected.
     */
    @Test
    void retrieveTranslationNodesUsesQueryForRootNode() throws Exception {
        Node root = MgnlContext.getJCRSession(WS_TRANSLATION).getRootNode();
        Node node1 = mockNode(WS_TRANSLATION, "/queryNode1", stubProperty(PN_KEY, "queryNode1"));
        Node node2 = mockNode(WS_TRANSLATION, "/queryNode2", stubProperty(PN_KEY, "queryNode2"));

        ContextMockUtils.mockQueryResult(WS_TRANSLATION, Query.JCR_SQL2, "select * from [mgnl:translation]", node1, node2);

        when(_valueContext.getSingle()).thenReturn(Optional.of(root));
        when(_valueContext.get()).thenReturn(java.util.stream.Stream.of(root));

        ExportTranslationAsCsvAction action = new ExportTranslationAsCsvAction(_definition, _valueContext, _i18nContentSupport, _fileSystemHelper);
        List<Node> result = action.retrieveTranslationNodes();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(node1));
        assertTrue(result.contains(node2));
    }

    /**
     * Verifies retrieveTranslationNodes uses valueContext when specific nodes are selected.
     */
    @Test
    void retrieveTranslationNodesUsesValueContextForSelection() throws Exception {
        Node node1 = mockNode(WS_TRANSLATION, "/selected1", stubProperty(PN_KEY, "selected1"));
        Node node2 = mockNode(WS_TRANSLATION, "/selected2", stubProperty(PN_KEY, "selected2"));

        when(_valueContext.getSingle()).thenReturn(Optional.of(node1));
        when(_valueContext.get()).thenReturn(List.of(node1, node2).stream());

        ExportTranslationAsCsvAction action = new ExportTranslationAsCsvAction(_definition, _valueContext, _i18nContentSupport, _fileSystemHelper);
        List<Node> result = action.retrieveTranslationNodes();

        assertEquals(2, result.size());
        assertTrue(result.contains(node1));
        assertTrue(result.contains(node2));
    }

    /**
     * Verifies getEntries collects all locale properties for each node.
     */
    @Test
    void getEntriesCollectsLocaleProperties() throws Exception {
        Node node1 = mockNode(WS_TRANSLATION, "/key1", stubProperty(PN_KEY, "key1"), stubProperty(PREFIX_NAME + "en", "Value1"), stubProperty(PREFIX_NAME + "de", "Wert1"));
        Node node2 = mockNode(WS_TRANSLATION, "/key2", stubProperty(PN_KEY, "key2"), stubProperty(PREFIX_NAME + "en", "Value2"), stubProperty(PREFIX_NAME + "de", "Wert2"));

        when(_valueContext.getSingle()).thenReturn(Optional.of(node1));
        when(_valueContext.get()).thenReturn(java.util.stream.Stream.of(node1, node2));

        ExportTranslationAsCsvAction action = new ExportTranslationAsCsvAction(_definition, _valueContext, _i18nContentSupport, _fileSystemHelper);
        java.util.Map<String, java.util.Map<String, String>> entries = action.getEntries(List.of(Locale.ENGLISH, Locale.GERMAN));

        assertEquals(2, entries.size());
        assertTrue(entries.containsKey("key1"));
        assertTrue(entries.containsKey("key2"));
        assertEquals("Value1", entries.get("key1").get(PREFIX_NAME + "en"));
        assertEquals("Wert1", entries.get("key1").get(PREFIX_NAME + "de"));
        assertEquals("Value2", entries.get("key2").get(PREFIX_NAME + "en"));
        assertEquals("Wert2", entries.get("key2").get(PREFIX_NAME + "de"));
    }

    /**
     * Verifies getEntries skips nodes without key property.
     */
    @Test
    void getEntriesSkipsNodesWithoutKey() throws Exception {
        Node nodeWithKey = mockNode(WS_TRANSLATION, "/withKey", stubProperty(PN_KEY, "withKey"), stubProperty(PREFIX_NAME + "en", "Value"));
        Node nodeWithoutKey = mockNode(WS_TRANSLATION, "/withoutKey", stubProperty(PREFIX_NAME + "en", "Orphan"));

        when(_valueContext.getSingle()).thenReturn(Optional.of(nodeWithKey));
        when(_valueContext.get()).thenReturn(List.of(nodeWithKey, nodeWithoutKey).stream());

        ExportTranslationAsCsvAction action = new ExportTranslationAsCsvAction(_definition, _valueContext, _i18nContentSupport, _fileSystemHelper);
        java.util.Map<String, java.util.Map<String, String>> entries = action.getEntries(List.of(Locale.ENGLISH));

        assertEquals(1, entries.size());
        assertTrue(entries.containsKey("withKey"));
    }

    /**
     * Verifies execute handles null file gracefully without calling streamFile.
     */
    @Test
    void executeDoesNotStreamWhenFileIsNull() throws Exception {
        Node node = mockNode(WS_TRANSLATION, "/test", stubProperty(PN_KEY, "test"));
        when(_valueContext.getSingle()).thenReturn(Optional.of(node));
        when(_valueContext.get()).thenReturn(java.util.stream.Stream.of(node));
        when(_fileSystemHelper.getTempDirectory()).thenReturn(new File("/nonexistent/path"));

        ExportTranslationAsCsvAction action = new ExportTranslationAsCsvAction(_definition, _valueContext, _i18nContentSupport, _fileSystemHelper);
        action.execute();

        File[] files = _tempDir.listFiles((d, n) -> n.endsWith(".csv"));
        assertTrue(files == null || files.length == 0);
    }

    private Set<String> existingCsvNames() {
        Set<String> names = new HashSet<>();
        File[] files = _tempDir.listFiles((d, n) -> n.endsWith(".csv"));
        if (files != null) {
            for (File f : files) {
                names.add(f.getName());
            }
        }
        return names;
    }

    private File findNewCsv(Set<String> beforeNames) {
        File[] files = _tempDir.listFiles((d, n) -> n.endsWith(".csv"));
        if (files != null) {
            for (File f : files) {
                if (!beforeNames.contains(f.getName())) {
                    return f;
                }
            }
        }
        return null;
    }
}

package de.ibmix.magkit.tools.t9n.csv;

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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.observation.DatasourceObservation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;

import javax.jcr.Node;
import javax.jcr.Session;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Optional;

import de.ibmix.magkit.tools.t9n.setup.TranslationModule;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.PropertyUtil;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubLocales;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubUser;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockUser;
import static de.ibmix.magkit.test.cms.security.UserStubbingOperation.stubLanguage;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PN_KEY;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PREFIX_NAME;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ImportCsvAction} covering key import scenarios.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-19
 */
class ImportCsvActionTest {

    @TempDir
    private File _tempDir;

    private CommitActionDefinition _definition;
    private CloseHandler _closeHandler;
    private ValueContext<Node> _valueContext;
    private FormView<Node> _formView;
    private Datasource<Node> _datasource;
    private DatasourceObservation.Manual _datasourceObservation;
    private I18nContentSupport _i18nContentSupport;
    private TranslationModule _translationModule;

    @BeforeEach
    void setUp() throws Exception {
        _definition = mock(CommitActionDefinition.class);
        _closeHandler = mock(CloseHandler.class);
        _valueContext = mock(ValueContext.class);
        _formView = mock(FormView.class);
        _datasource = mock(Datasource.class);
        _datasourceObservation = mock(DatasourceObservation.Manual.class);

        _i18nContentSupport = mockI18nContentSupport(stubLocales(Locale.ENGLISH, Locale.GERMAN));

        NodeNameHelper nodeNameHelper = mockComponentInstance(NodeNameHelper.class);
        when(nodeNameHelper.getValidatedName(ArgumentMatchers.anyString())).thenAnswer(inv -> inv.getArguments()[0]);

        _translationModule = mockComponentInstance(TranslationModule.class);

        final User user = mockUser("Paul", stubLanguage("fr"));
        mockWebContext(stubUser(user), stubJcrSession(WS_TRANSLATION));
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    /**
     * Verifies that write() does nothing when no CSV file is provided.
     */
    @Test
    void writeDoesNothingWithoutCsvFile() throws Exception {
        when(_formView.getPropertyValue("importCsv")).thenReturn(Optional.empty());
        when(_translationModule.getBasePath()).thenReturn("");

        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        action.write();

        Session session = MgnlContext.getJCRSession(WS_TRANSLATION);
        Node root = session.getRootNode();
        assertFalse(root.getNodes().hasNext());
    }

    /**
     * Verifies import creates a new translation node at root when base path is empty.
     */
    @Test
    void importCreatesNodesAtRootWhenBasePathEmpty() throws Exception {
        File csv = new File(_tempDir, "t1.csv");
        Files.writeString(csv.toPath(), "Key,Englisch,Deutsch\nhello,Hello,Hallo\n");
        when(_formView.getPropertyValue("importCsv")).thenReturn(Optional.of(csv));
        when(_formView.getPropertyValue("encoding")).thenReturn(Optional.empty());
        when(_formView.getPropertyValue("separator")).thenReturn(Optional.empty());
        when(_translationModule.getBasePath()).thenReturn("");

        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        action.write();

        Session session = MgnlContext.getJCRSession(WS_TRANSLATION);
        Node root = session.getRootNode();
        assertTrue(root.hasNode("hello"));
        Node node = root.getNode("hello");
        assertEquals("hello", PropertyUtil.getString(node, PN_KEY));
        assertEquals("Hello", PropertyUtil.getString(node, PREFIX_NAME + "en"));
        assertEquals("Hallo", PropertyUtil.getString(node, PREFIX_NAME + "de"));
        verify(session).save();
    }

    /**
     * Verifies import places nodes under configured base path.
     */
    @Test
    void importCreatesNodesUnderBasePath() throws Exception {
        Node base = mockNode(WS_TRANSLATION, "/base");
        File csv = new File(_tempDir, "t2.csv");
        Files.writeString(csv.toPath(), "Key,Englisch,Deutsch\nwelcome,Welcome,Willkommen\n");
        when(_formView.getPropertyValue("importCsv")).thenReturn(Optional.of(csv));
        when(_formView.getPropertyValue("encoding")).thenReturn(Optional.empty());
        when(_formView.getPropertyValue("separator")).thenReturn(Optional.empty());
        when(_translationModule.getBasePath()).thenReturn("/base");

        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        action.write();

        assertTrue(base.hasNode("welcome"));
        Node node = base.getNode("welcome");
        assertEquals("welcome", PropertyUtil.getString(node, PN_KEY));
        assertEquals("Welcome", PropertyUtil.getString(node, PREFIX_NAME + "en"));
        assertEquals("Willkommen", PropertyUtil.getString(node, PREFIX_NAME + "de"));
        verify(MgnlContext.getJCRSession(WS_TRANSLATION)).save();
    }

    /**
     * Verifies existing node is updated instead of recreated.
     */
    @Test
    void importUpdatesExistingNode() throws Exception {
        Node node = mockNode(WS_TRANSLATION, "/base/existing", stubProperty(PN_KEY, "existing"), stubProperty(PREFIX_NAME + "en", "Old"), stubProperty(PREFIX_NAME + "de", "Alt"));
        File csv = new File(_tempDir, "t3.csv");
        Files.writeString(csv.toPath(), "Key,Englisch,Deutsch\nexisting,New,Neu\n");
        when(_formView.getPropertyValue("importCsv")).thenReturn(Optional.of(csv));
        when(_formView.getPropertyValue("encoding")).thenReturn(Optional.empty());
        when(_formView.getPropertyValue("separator")).thenReturn(Optional.empty());
        when(_translationModule.getBasePath()).thenReturn("/base");

        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        action.write();

        assertEquals("New", PropertyUtil.getString(node, PREFIX_NAME + "en"));
        assertEquals("Neu", PropertyUtil.getString(node, PREFIX_NAME + "de"));
        verify(MgnlContext.getJCRSession(WS_TRANSLATION)).save();
    }

    /**
     * Verifies import with custom encoding.
     */
    @Test
    void importUsesCustomEncoding() throws Exception {
        File csv = new File(_tempDir, "t4.csv");
        Files.writeString(csv.toPath(), "Key,English,German\ntest,Test,Test\n", java.nio.charset.StandardCharsets.ISO_8859_1);
        Option encodingOption = mock(Option.class);
        when(encodingOption.getValue()).thenReturn("ISO-8859-1");
        when(_formView.getPropertyValue("importCsv")).thenReturn(Optional.of(csv));
        when(_formView.getPropertyValue("encoding")).thenReturn(Optional.of(encodingOption));
        when(_formView.getPropertyValue("separator")).thenReturn(Optional.empty());
        when(_translationModule.getBasePath()).thenReturn("");

        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        action.write();

        Session session = MgnlContext.getJCRSession(WS_TRANSLATION);
        Node root = session.getRootNode();
        assertTrue(root.hasNode("test"));
    }

    /**
     * Verifies import with custom separator.
     */
    @Test
    void importUsesCustomSeparator() throws Exception {
        File csv = new File(_tempDir, "t5.csv");
        Files.writeString(csv.toPath(), "Key;Englisch;Deutsch\nkey1;Val1;Wert1\n");
        Option separatorOption = mock(Option.class);
        when(separatorOption.getValue()).thenReturn(";");
        when(_formView.getPropertyValue("importCsv")).thenReturn(Optional.of(csv));
        when(_formView.getPropertyValue("encoding")).thenReturn(Optional.empty());
        when(_formView.getPropertyValue("separator")).thenReturn(Optional.of(separatorOption));
        when(_translationModule.getBasePath()).thenReturn("");

        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        action.write();

        Session session = MgnlContext.getJCRSession(WS_TRANSLATION);
        Node root = session.getRootNode();
        assertTrue(root.hasNode("key1"));
        Node node = root.getNode("key1");
        assertEquals("Val1", PropertyUtil.getString(node, PREFIX_NAME + "en"));
        assertEquals("Wert1", PropertyUtil.getString(node, PREFIX_NAME + "de"));
    }

    /**
     * Verifies import of multiple rows creates multiple nodes.
     */
    @Test
    void importCreatesMultipleNodes() throws Exception {
        File csv = new File(_tempDir, "t6.csv");
        Files.writeString(csv.toPath(), "Key,Englisch,Deutsch\nkey1,Value1,Wert1\nkey2,Value2,Wert2\nkey3,Value3,Wert3\n");
        when(_formView.getPropertyValue("importCsv")).thenReturn(Optional.of(csv));
        when(_formView.getPropertyValue("encoding")).thenReturn(Optional.empty());
        when(_formView.getPropertyValue("separator")).thenReturn(Optional.empty());
        when(_translationModule.getBasePath()).thenReturn("");

        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        action.write();

        Session session = MgnlContext.getJCRSession(WS_TRANSLATION);
        Node root = session.getRootNode();
        assertTrue(root.hasNode("key1"));
        assertTrue(root.hasNode("key2"));
        assertTrue(root.hasNode("key3"));
        assertEquals("Value2", PropertyUtil.getString(root.getNode("key2"), PREFIX_NAME + "en"));
        assertEquals("Wert3", PropertyUtil.getString(root.getNode("key3"), PREFIX_NAME + "de"));
    }

    /**
     * Verifies detectColumns recognizes locale by display name in current locale.
     */
    @Test
    void detectColumnsMatchesLocaleDisplayNameInCurrentLocale() {
        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        String[] headings = {"Key", "Englisch", "Deutsch"};
        java.util.Map<Integer, String> result = action.detectColumns(headings);

        assertEquals(3, result.size());
        assertEquals(PN_KEY, result.get(0));
        assertEquals(PREFIX_NAME + "en", result.get(1));
        assertEquals(PREFIX_NAME + "de", result.get(2));
    }

    /**
     * Verifies detectColumns recognizes locale by default display name or user local display name and ignores unknown column headings.
     */
    @Test
    void detectColumnsMatchesLocaleDisplayNameDefault() {
        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        String[] headings = {"Key", "anglais", "Unknown", "Deutsch"};
        java.util.Map<Integer, String> result = action.detectColumns(headings);

        assertEquals(3, result.size());
        assertEquals(PN_KEY, result.get(0));
        assertEquals(PREFIX_NAME + "en", result.get(1));
        //TODO: Chaek if we should insert a null entry for the unknown column heading.
        assertEquals(PREFIX_NAME + "de", result.get(3));
    }

    /**
     * Verifies getPropertyValue returns default when property is not present.
     */
    @Test
    void getPropertyValueReturnsDefaultWhenMissing() {
        when(_formView.getPropertyValue("missing")).thenReturn(Optional.empty());
        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        String result = action.getPropertyValue("missing", "default");
        assertEquals("default", result);
    }

    /**
     * Verifies getPropertyValue returns option value when present.
     */
    @Test
    void getPropertyValueReturnsOptionValue() {
        Option option = mock(Option.class);
        when(option.getValue()).thenReturn("customValue");
        when(_formView.getPropertyValue("present")).thenReturn(Optional.of(option));
        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        String result = action.getPropertyValue("present", "default");
        assertEquals("customValue", result);
    }

    /**
     * Verifies getTranslationModule caches the component instance.
     */
    @Test
    void getTranslationModuleCachesInstance() {
        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        TranslationModule module1 = action.getTranslationModule();
        TranslationModule module2 = action.getTranslationModule();
        assertEquals(module1, module2);
    }

    /**
     * Verifies import handles empty CSV file gracefully.
     */
    @Test
    void importHandlesEmptyCsvFile() throws Exception {
        File csv = new File(_tempDir, "empty.csv");
        Files.writeString(csv.toPath(), "");
        when(_formView.getPropertyValue("importCsv")).thenReturn(Optional.of(csv));
        when(_formView.getPropertyValue("encoding")).thenReturn(Optional.empty());
        when(_formView.getPropertyValue("separator")).thenReturn(Optional.empty());
        when(_translationModule.getBasePath()).thenReturn("");

        ImportCsvAction action = new ImportCsvAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _i18nContentSupport);
        action.write();

        Session session = MgnlContext.getJCRSession(WS_TRANSLATION);
        Node root = session.getRootNode();
        assertFalse(root.getNodes().hasNext());
    }
}

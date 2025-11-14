package de.ibmix.magkit.tools.t9n;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Translation
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

import de.ibmix.magkit.test.jcr.query.QueryMockUtils;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import org.apache.jackrabbit.api.query.JackrabbitQueryResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static de.ibmix.magkit.test.jcr.query.QueryMockUtils.mockQueryManager;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the translation task.
 *
 * @author frank.sommer
 * @since 21.09.2023
 */
public class AddTranslationsTaskTest {

    private AddTranslationsTask _addTranslationsTask;
    private InstallContext _installContext;

    @BeforeEach
    public void setUp() throws Exception {
        final NodeNameHelper nodeNameHelper = mockComponentInstance(NodeNameHelper.class);
        when(nodeNameHelper.getValidatedName(anyString())).then(invocation -> invocation.getArguments()[0]);

        final ResourceOrigin resourceOrigin = mockComponentInstance(ResourceOrigin.class);

        final Resource germanResource = mock(Resource.class);
        when(germanResource.openStream()).thenReturn(getClass().getResourceAsStream("/i18n/messages_de.properties"));
        when(resourceOrigin.getByPath("/i18n/messages_de.properties")).thenReturn(germanResource);

        final Resource englishResource = mock(Resource.class);
        when(englishResource.openStream()).thenReturn(getClass().getResourceAsStream("/i18n/messages_en.properties"));
        when(resourceOrigin.getByPath("/i18n/messages_en.properties")).thenReturn(englishResource);

        _addTranslationsTask = new AddTranslationsTask("i18n.messages", Locale.ENGLISH, Locale.GERMAN);

        final Session session = mockSession(WS_TRANSLATION);

        _installContext = mock(InstallContext.class);
        when(_installContext.getJCRSession(WS_TRANSLATION)).thenReturn(session);

        mockWebContext(stubJcrSession(WS_TRANSLATION));
        mockQueryManager(WS_TRANSLATION);
    }

    @Test
    public void addTranslationEntryAndTranslation() {
        final ArrayDelegateTask task = mock(ArrayDelegateTask.class);
        _addTranslationsTask.addTranslationNodeTasks(task, _installContext);

        verify(task, times(2)).addTask(any());
    }

    @Test
    public void addOnlyTranslation() throws Exception {
        final QueryManager queryManager = mockQueryManager(WS_TRANSLATION);
        final Node node = mockNode("ui.key");
        final JackrabbitQueryResult queryResult = QueryMockUtils.mockQueryResult(node);
        final Query query = mock(Query.class);
        when(query.execute()).thenReturn(queryResult);
        when(queryManager.createQuery("select * from [mgnl:translation] where key = 'ui.key'", Query.JCR_SQL2)).thenReturn(query);

        final ArrayDelegateTask task = mock(ArrayDelegateTask.class);
        _addTranslationsTask.addTranslationNodeTasks(task, _installContext);

        verify(task, times(1)).addTask(any());
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }
}

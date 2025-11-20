package de.ibmix.magkit.tools.t9n.setup;

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

import info.magnolia.module.ModuleLifecycleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link TranslationModule}.
 *
 * @author IBM iX
 * @since 2025-11-20
 */
public class TranslationModuleTest {

    private TranslationModule _translationModule;
    private ModuleLifecycleContext _moduleLifecycleContext;

    @BeforeEach
    public void setUp() {
        _translationModule = new TranslationModule();
        _moduleLifecycleContext = mock(ModuleLifecycleContext.class);
    }

    @Test
    public void testDefaultBehaviourWithLivecycle() {
        assertNull(_translationModule.getBasePath());

        _translationModule.start(_moduleLifecycleContext);
        verifyNoInteractions(_moduleLifecycleContext);
        assertNull(_translationModule.getBasePath());

        _translationModule.stop(_moduleLifecycleContext);
        verifyNoInteractions(_moduleLifecycleContext);
        assertNull(_translationModule.getBasePath());
    }

    @Test
    public void setBasePathWithLivecycle() {
        assertNull(_translationModule.getBasePath());
        _translationModule.setBasePath("/example/path");
        assertEquals("/example/path", _translationModule.getBasePath());

        _translationModule.start(_moduleLifecycleContext);
        assertEquals("/example/path", _translationModule.getBasePath());
        verifyNoInteractions(_moduleLifecycleContext);

        _translationModule.stop(_moduleLifecycleContext);
        assertEquals("/example/path", _translationModule.getBasePath());
        verifyNoInteractions(_moduleLifecycleContext);
    }

    @Test
    public void testMultipleBasePathChanges() {
        _translationModule.setBasePath("/path1");
        assertEquals("/path1", _translationModule.getBasePath());

        _translationModule.setBasePath(EMPTY);
        assertEquals(EMPTY, _translationModule.getBasePath());

        _translationModule.setBasePath("/path2");
        assertEquals("/path2", _translationModule.getBasePath());

        _translationModule.setBasePath(null);
        assertNull(_translationModule.getBasePath());
    }
}


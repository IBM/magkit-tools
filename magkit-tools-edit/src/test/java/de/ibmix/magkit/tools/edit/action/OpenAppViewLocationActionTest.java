package de.ibmix.magkit.tools.edit.action;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
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

import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.ContentBrowserSubApp.BrowserLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link OpenAppViewLocationAction}.
 *
 * @author wolf.bubenik
 * @since 2025-11-18
 */
class OpenAppViewLocationActionTest {

    private LocationController _locationController;
    private OpenAppViewLocationActionDefinition _definition;

    @BeforeEach
    void setUp() {
        _locationController = mock(LocationController.class);
        _definition = new OpenAppViewLocationActionDefinition();
        _definition.setAppName("testApp");
        _definition.setSubAppId("browser");
        _definition.setViewType("treeview");
    }

    @Test
    void testExecuteWithValidNodePath() throws ActionExecutionException {
        String nodePath = "/test/path";
        TestOpenAppViewLocationAction action = new TestOpenAppViewLocationAction(_definition, _locationController, nodePath);

        action.execute();

        verify(_locationController).goTo(any(BrowserLocation.class));
    }

    @Test
    void testExecuteWithEmptyNodePath() throws ActionExecutionException {
        String nodePath = "";
        TestOpenAppViewLocationAction action = new TestOpenAppViewLocationAction(_definition, _locationController, nodePath);

        action.execute();

        verify(_locationController).goTo(any(BrowserLocation.class));
    }

    @Test
    void testExecuteWithRootPath() throws ActionExecutionException {
        String nodePath = "/";
        TestOpenAppViewLocationAction action = new TestOpenAppViewLocationAction(_definition, _locationController, nodePath);

        action.execute();

        verify(_locationController).goTo(any(BrowserLocation.class));
    }

    @Test
    void testExecuteWithDifferentViewType() throws ActionExecutionException {
        _definition.setViewType("listview");
        String nodePath = "/test/path";
        TestOpenAppViewLocationAction action = new TestOpenAppViewLocationAction(_definition, _locationController, nodePath);

        action.execute();

        verify(_locationController).goTo(any(BrowserLocation.class));
    }

    @Test
    void testExecuteWithNullNodePath() throws ActionExecutionException {
        TestOpenAppViewLocationAction action = new TestOpenAppViewLocationAction(_definition, _locationController, null);

        action.execute();

        verify(_locationController).goTo(any(BrowserLocation.class));
    }

    @Test
    void testExecuteThrowsException() {
        TestOpenAppViewLocationAction action = new TestOpenAppViewLocationAction(_definition, _locationController, null, true);

        assertThrows(ActionExecutionException.class, action::execute);
    }

    @Test
    void testTreeViewConstant() {
        assertEquals("treeview", OpenAppViewLocationAction.TREE_VIEW);
    }

    private static class TestOpenAppViewLocationAction extends OpenAppViewLocationAction {

        private final String _nodePath;
        private final boolean _throwException;

        TestOpenAppViewLocationAction(OpenAppViewLocationActionDefinition definition, LocationController locationController, String nodePath) {
            super(definition, locationController);
            _nodePath = nodePath;
            _throwException = false;
        }

        TestOpenAppViewLocationAction(OpenAppViewLocationActionDefinition definition, LocationController locationController, String nodePath, boolean throwException) {
            super(definition, locationController);
            _nodePath = nodePath;
            _throwException = throwException;
        }

        @Override
        protected String getNodePath() throws ActionExecutionException {
            if (_throwException) {
                throw new ActionExecutionException("Test exception");
            }
            return _nodePath;
        }
    }
}


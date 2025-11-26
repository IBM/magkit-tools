package de.ibmix.magkit.tools.edit.setup;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
 * %%
 * Copyright (C) 2025 IBM iX
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

import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for {@link EditToolsVersionHandler} verifying added conditional bootstrap tasks.
 *
 * @author wolf.bubenik
 * @since 2025-11-19
 */
public class EditToolsVersionHandlerTest {

    private final EditToolsVersionHandler _handler = new EditToolsVersionHandler();

    /**
     * Verifies that default update tasks list contains the two conditional tasks appended.
     */
    @Test
    public void testDefaultUpdateTasksContainConditionalTasks() {
        Version version = Version.parseVersion("1.0.0");
        List<Task> tasks = _handler.getDefaultUpdateTasks(version);
        assertNotNull(tasks);
        assertTrue(tasks.size() >= 2);
        Task last = tasks.get(tasks.size() - 1);
        Task secondLast = tasks.get(tasks.size() - 2);
        String lastName = last.getName();
        String secondLastName = secondLast.getName();
        assertTrue("Add status bar config".equals(lastName) || "Add move workspace config".equals(lastName));
        assertTrue("Add status bar config".equals(secondLastName) || "Add move workspace config".equals(secondLastName));
        assertNotEquals(lastName, secondLastName);
    }
}

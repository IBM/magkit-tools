package de.ibmix.magkit.tools.app;

/*-
 * #%L
 * magkit-tools-app
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

import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.form.FormViewReduced;

/**
 * Base view interface for tool sub-applications combining form input and result display.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Manages form view for user input</li>
 *   <li>Handles action trigger events via listener pattern</li>
 *   <li>Supports view refresh operations</li>
 * </ul>
 *
 * @author frank.sommer
 * @since 1.5.0
 */
public interface ResultView extends View {
    /**
     * Sets the listener for action events.
     *
     * @param listener the listener to handle action triggers
     */
    void setListener(Listener listener);

    /**
     * Sets the form view component for user input.
     *
     * @param formView the form view to display
     */
    void setFormView(View formView);

    /**
     * Refreshes the view by rebuilding the form with a new instance.
     */
    void refresh();

    /**
     * Returns the current form view instance.
     *
     * @return the current form view
     */
    FormViewReduced getCurrentFormView();

    /**
     * Listener interface for handling button action events.
     */
    interface Listener {
        /**
         * Called when an action button is triggered.
         */
        void onActionTriggered();
    }
}

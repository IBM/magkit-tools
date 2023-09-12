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

import static info.magnolia.jcr.util.NodeTypes.MGNL_PREFIX;

/**
 * Node type of translation.
 *
 * @author diana.racho (IBM iX)
 */
public final class TranslationNodeTypes {
    public static final String WS_TRANSLATION = "translation";

    private TranslationNodeTypes() {
    }

    /**
     * Represents the mgnl:translation node type.
     */
    public static final class Translation {

        public static final String PN_KEY = "key";

        private Translation() {
        }

        // Node type name
        public static final String NAME = MGNL_PREFIX + "translation";
        public static final String PREFIX_NAME = "translation_";
    }
}

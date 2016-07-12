package com.aperto.magnolia.translation;

import static info.magnolia.jcr.util.NodeTypes.MGNL_PREFIX;

/**
 * Node type of translation.
 *
 * @author diana.racho (Aperto AG)
 */
public final class TranslationNodeTypes {

    private TranslationNodeTypes() {
    }

    /**
     * Represents the mgnl:translation node type.
     */
    public static final class Translation {

        private Translation() {
        }

        // Node type name
        public static final String NAME = MGNL_PREFIX + "translation";
        public static final String PREFIX_NAME = "translation_";
    }
}
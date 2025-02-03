package io.github.arcaneplugins.polyconomy.plugin.core.config.translations

import io.github.arcaneplugins.polyconomy.plugin.core.Platform
import java.util.function.Supplier

abstract class TranslationHandler(
    val platform: Platform,
) {

    abstract fun placeholderify(
        str: String,
        placeholders: Map<String, Supplier<String>>,
    ): String

    abstract fun joinStrings(
        strs: Collection<String>,
    ): String

}
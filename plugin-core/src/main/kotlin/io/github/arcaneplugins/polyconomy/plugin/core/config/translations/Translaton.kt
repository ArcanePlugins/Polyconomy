package io.github.arcaneplugins.polyconomy.plugin.core.config.translations

import io.github.arcaneplugins.polyconomy.plugin.core.Platform
import org.spongepowered.configurate.CommentedConfigurationNode

abstract class Translaton(
    val platform: Platform,
    val cfgPath: Array<String>,
    val defaultVal: List<String>,
) {

    val node: CommentedConfigurationNode
        get() = platform.translationsCfg.rootNode.node(*cfgPath)

    fun rawStr(): String {
        return if (node.isList) {
            node.getList(String::class.java)?.joinToString(" ") ?: defaultVal.joinToString(" ")
        } else {
            node.string ?: defaultVal.joinToString(" ")
        }
    }

    fun rawList(): List<String> {
        return if (node.isList) {
            node.getList(String::class.java) ?: defaultVal
        } else {
            val strValue = node.string
            return if (strValue == null) {
                defaultVal
            } else {
                listOf(strValue)
            }
        }
    }

}
package io.github.arcaneplugins.polyconomy.plugin.bukkit.config.translations

import de.themoep.minedown.adventure.MineDown
import dev.jorel.commandapi.CommandAPI
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.core.config.translations.TranslationHandler
import io.github.arcaneplugins.polyconomy.plugin.core.config.translations.Translaton
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.function.Supplier

class TranslationHandlerImpl(
    val plugin: Polyconomy,
): TranslationHandler(
    platform = plugin
) {

    class TranslationImpl(
        val handler: TranslationHandlerImpl,
        cfgPath: Array<String>,
        defaultVal: List<String>,
    ) : Translaton(
        platform = handler.plugin,
        cfgPath = cfgPath,
        defaultVal = defaultVal
    ) {

        fun convertPapiPlaceholders(str: String, sender: CommandSender): String {
            if (sender !is Player) {
                return str
            }

            return PlaceholderAPI.setPlaceholders(sender, str)
        }

        fun sendTo(sender: CommandSender, placeholders: Map<String, Supplier<String>> = emptyMap()) {
            if (node.isList) {
                rawList().forEach {
                    handler.audiences.sender(sender).sendMessage(
                        handler.formatify(
                            str = convertPapiPlaceholders(it, sender),
                            placeholders = placeholders,
                        )
                    )
                }
            } else {
                handler.audiences.sender(sender).sendMessage(
                    handler.formatify(
                        str = convertPapiPlaceholders(rawStr(), sender),
                        placeholders = placeholders,
                    )
                )
            }
        }

    }

    override fun placeholderify(str: String, placeholders: Map<String, Supplier<String>>): String {
        var strMut = str

        for (pair in placeholders) {
            val id: String = pair.key
            val supplier: Supplier<String> = pair.value

            if (strMut.contains(id)) {
                strMut = strMut.replace("%${id}%", supplier.get())
            }
        }

        return strMut
    }

    override fun joinStrings(vararg strs: String): String {
        return strs.joinToString(listSeparator.rawStr())
    }

    private fun formatify(str: String, placeholders: Map<String, Supplier<String>>): Component {
        return MineDown.parse(
            placeholderify(str, placeholders.plus("prefix" to Supplier { prefix.rawStr()}))
        )
    }

    private val audiences = BukkitAudiences.create(plugin)

    val listSeparator = TranslationImpl(this,
        arrayOf("list-separator"),
        listOf("&7, &f"))
    val prefix = TranslationImpl(this,
        arrayOf("prefix"),
        listOf("&b&lPC:&7 "))
    private val commandGenericErrorCommandFailure = TranslationImpl(this,
        arrayOf("command", "generic", "error", "command-failure"),
        listOf("err^"))
    val commandBalanceErrorNoPlayer = TranslationImpl(this,
        arrayOf("command", "balance", "error", "no-player"),
        listOf("%prefix% Enter the username of the player you wish to check."))
    val commandBalanceView = TranslationImpl(this,
        arrayOf("command", "balance", "view"),
        listOf("%prefix% Player &f%target-name%&7 has &f%balance%&7 (currency: &f%currency%&7)"))
    val commandBalancetopErrorPageTooLow = TranslationImpl(this,
        arrayOf("command", "balancetop", "error", "page-too-low"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 Page number &f%page%&7 is too low; it must be at least &f1&7."))
    val commandBalancetopErrorAlreadySearching = TranslationImpl(this,
        arrayOf("command", "balancetop", "error", "already-searching"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 You are already searching for the top balances; please wait for this search to complete."))
    val commandBalancetopProcessingRequqest = TranslationImpl(this,
        arrayOf("command", "balancetop", "processing-request"), // todo add to translations.yml
        listOf("%prefix% Processing request..."))
    val commandBalancetopHeader = TranslationImpl(this,
        arrayOf("command", "balancetop", "header"), // todo add to translations.yml
        listOf("&8&m+-----+&b Balance Top &8(&7Page &f%page%&8 - &f%currency%&8) &m+-----+&r"))
    val commandBalancetopNoEntriesOnPage = TranslationImpl(this,
        arrayOf("command", "balancetop", "no-entries-on-page"), // todo add to translations.yml
        listOf("&7&iThere are no entries to display on this page."))
    val commandBalancetopEntry = TranslationImpl(this,
        arrayOf("command", "balancetop", "entry"), // todo add to translations.yml
        listOf("&8  %rank%.&f  %target-name%&7: &f%balance%"))
    val commandGenericUnknownCurrency = TranslationImpl(this,
        arrayOf("command", "generic", "unknown-currency"), // todo add to translations.yml
        listOf("Unknown currency: ")
    )
    val commandGenericIllformedLocale = TranslationImpl(this,
        arrayOf("command", "generic", "illformed-locale"), // todo add to translations.yml
        listOf("Illformed locale: ")
    )
    val commandGenericIllformedIdentifier = TranslationImpl(this,
        arrayOf("command", "generic", "illformed-identifier"), // todo add to translations.yml
        listOf("Illformed identifier: ")
    )
    val commandPayErrorNotYourself = TranslationImpl(this,
        arrayOf("command", "pay", "error", "not-yourself"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 You can't pay yourself."))
    val commandPayErrorAmountTooLow = TranslationImpl(this,
        arrayOf("command", "pay", "error", "amount-too-low"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 Payment amount &f%amount%&7 is too low; it must be greater than &f0&7."))
    val commandPayErrorCantAfford = TranslationImpl(this,
        arrayOf("command", "pay", "error", "cant-afford"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 You can't afford a payment of amount &f%amount%&7 in currency &f%currency%&7; your balance is &f%balance%&7."))
    val commandPaySuccess = TranslationImpl(this,
        arrayOf("command", "pay", "success"), // todo add to translations.yml
        listOf("%prefix% Paid &f%amount%&7 to &f%target-name%&7 in currency &f%currency%&7. They now have &f%target-balance%&7. Your new balance is &f%balance%&7."))
    val commandPolyconomySubroutineDbCleanupStart = TranslationImpl(this,
        arrayOf("command", "polyconomy", "subroutine", "db-cleanup", "start"), // todo add to translations.yml
        listOf("%prefix% Starting database cleanup..."))
    val commandPolyconomySubroutineDbCleanupComplete = TranslationImpl(this,
        arrayOf("command", "polyconomy", "subroutine", "db-cleanup", "complete"), // todo add to translations.yml
        listOf("%prefix% Database cleanup complete."))
    val commandPolyconomySubroutineH2ServerErrorImplementation = TranslationImpl(this,
        arrayOf("command", "polyconomy", "subroutine", "h2-server", "error", "implementation"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 You aren't running the H2 database as your storage implementation. (Do you need to reload or adjust this?)"))
    val commandPolyconomySubroutineH2ServerErrorAlreadyRunning = TranslationImpl(this,
        arrayOf("command", "polyconomy", "subroutine", "h2-server", "error", "already-running"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 The H2 debug webserver is already running."))
    val commandPolyconomySubroutineH2ServerStarted = TranslationImpl(this,
        arrayOf("command", "polyconomy", "subroutine", "h2-server", "started"),
        listOf("%prefix% Started H2 debug web server. To stop the server, please restart your server."))
    val commandGenericNotYetImplemented = TranslationImpl(this,
        arrayOf("command", "generic", "error", "not-yet-implemented"),
        listOf("%prefix% &cError:&7 Not yet implemented."))

    fun commandApiFailure(): Exception {
        return CommandAPI.failWithString(commandGenericErrorCommandFailure.rawStr())
    }

}
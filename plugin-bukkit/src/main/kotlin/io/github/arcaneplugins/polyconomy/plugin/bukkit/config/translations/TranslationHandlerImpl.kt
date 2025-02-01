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

    override fun joinStrings(strs: Collection<String>): String {
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
        listOf("&b&lPolyconomy:&7 "))
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
        arrayOf("command", "balancetop", "error", "page-too-low"),
        listOf("%prefix% &cError:&7 Page number &f%page%&7 is too low; it must be at least &f1&7."))
    val commandBalancetopErrorAlreadySearching = TranslationImpl(this,
        arrayOf("command", "balancetop", "error", "already-searching"),
        listOf("%prefix% &cError:&7 You are already searching for the top balances; please wait for this search to complete."))
    val commandBalancetopProcessingRequqest = TranslationImpl(this,
        arrayOf("command", "balancetop", "processing-request"),
        listOf("%prefix% Processing request..."))
    val commandBalancetopHeader = TranslationImpl(this,
        arrayOf("command", "balancetop", "header"),
        listOf("&8&m+-----+&b Balance Top &8(&7Page &f%page%&8 - &f%currency%&8) &m+-----+&r"))
    val commandBalancetopNoEntriesOnPage = TranslationImpl(this,
        arrayOf("command", "balancetop", "no-entries-on-page"),
        listOf("&7&iThere are no entries to display on this page."))
    val commandBalancetopEntry = TranslationImpl(this,
        arrayOf("command", "balancetop", "entry"),
        listOf("&8  %rank%.&f  %target-name%&7: &f%balance%"))
    val commandGenericErrorUnknownCurrency = TranslationImpl(this,
        arrayOf("command", "generic", "error", "unknown-currency"),
        listOf("Unknown currency: "))
    val commandGenericErrorIllformedLocale = TranslationImpl(this,
        arrayOf("command", "generic", "error", "illformed-locale"),
        listOf("Illformed locale: "))
    val commandGenericErrorIllformedIdentifier = TranslationImpl(this,
        arrayOf("command", "generic", "error", "illformed-identifier"),
        listOf("Illformed identifier: "))
    val commandPayErrorNotYourself = TranslationImpl(this,
        arrayOf("command", "pay", "error", "not-yourself"),
        listOf("%prefix% &cError:&7 You can't pay yourself."))
    val commandGenericAmountZeroOrLess = TranslationImpl(this,
        arrayOf("command", "generic", "error", "amount-zero-or-less"),
        listOf("%prefix% &cError:&7 Amount &f%amount%&7 is too low; it must be greater than &f0&7."))
    val commandPayErrorCantAfford = TranslationImpl(this,
        arrayOf("command", "pay", "error", "cant-afford"),
        listOf("%prefix% &cError:&7 You can't afford a payment of amount &f%amount%&7 in currency &f%currency%&7; your balance is &f%balance%&7."))
    val commandPaySuccess = TranslationImpl(this,
        arrayOf("command", "pay", "success"),
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
        arrayOf("command", "polyconomy", "subroutine", "h2-server", "started"), // todo add to translations.yml
        listOf("%prefix% Started H2 debug web server. To stop the server, please restart your server."))
    val commandGenericNotYetImplemented = TranslationImpl(this,
        arrayOf("command", "generic", "error", "not-yet-implemented"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 Not yet implemented."))
    val commandPolyconomyCurrencyRegisterStarted = TranslationImpl(this,
        arrayOf("command", "polyconomy", "currency", "register", "started"), // todo add to translations.yml
        listOf("%prefix% Registering currency &f%currency%&7..."))
    val commandPolyconomyCurrencyRegisterErrorAlreadyExists = TranslationImpl(this,
        arrayOf("command", "polyconomy", "currency", "error", "already-exists"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 The currency &f%currency%&7 already exists."))
    val commandPolyconomyCurrencyRegisterSuccess = TranslationImpl(this,
        arrayOf("command", "polyconomy", "currency", "success"), // todo add to translations.yml
        listOf("%prefix% Currency &f%currency%&7 registered successfully."))
    val commandPolyconomyCurrencyUnregisterStarted = TranslationImpl(this,
        arrayOf("command", "polyconomy", "currency", "unregister", "started"), // todo add to translations.yml
        listOf("%prefix% Unregistering currency &f%currency%&7..."))
    val commandPolyconomyCurrencyUnregisterComplete = TranslationImpl(this,
        arrayOf("command", "polyconomy", "currency", "unregister", "complete"), // todo add to translations.yml
        listOf("%prefix% Unregistered currency &f%currency%&7 successfully."))
    val commandPolyconomyCurrencyUnregisterErrorIsPrimary = TranslationImpl(this,
        arrayOf("command", "polyconomy", "currency", "unregister", "error", "is-primary"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 You can't unregister currency &f%currency%&7 because it is a &fprimary currency&7.",
            "%prefix% Consider making a different (new, if needed) currency a primary one so you can unregister &f%currency%&7."))
    val commandPolyconomyDepositComplete = TranslationImpl(this,
        arrayOf("command", "polyconomy", "deposit", "complete"), // todo add to translations.yml
        listOf("%prefix% Deposited &f%amount%&7 into &f%target-name%&7's account in currency &f%currency%&7."))
    val commandPolyconomyReloadStarted = TranslationImpl(this,
        arrayOf("command", "polyconomy", "reload", "started"), // todo add to translations.yml
        listOf("%prefix% Reloading..."))
    val commandPolyconomyReloadSuccess = TranslationImpl(this,
        arrayOf("command", "polyconomy", "reload", "success"), // todo add to translations.yml
        listOf("%prefix% Reloaded successfully."))
    val commandPolyconomyReloadErrorGeneric = TranslationImpl(this,
        arrayOf("command", "polyconomy", "reload", "error", "generic"), // todo add to translations.yml
        listOf("%prefix% &cError:&7 An unexpected error occurred whilst reloading the plugin. Please check Console for more details. &fIf you have made changes to your configs, use a Online YAML Parser to check the syntax is correct.&7 Message: &8%message%"))
    val commandPolyconomyResetCompleted = TranslationImpl(this,
        arrayOf("command", "polyconomy", "reset", "completed"), // todo add to translations.yml
        listOf("%prefix% Reset &f%target-name%&7's balance in currency &f%currency%&7; they now have &f%target-balance%&7."))
    val commandPolyconomyVersionView = TranslationImpl(this,
        arrayOf("command", "polyconomy", "version", "view"), // todo add to translations.yml
        listOf("&f&l%name% v%version% by ArcanePlugins",
            "&8  \u2022&b Authors:&7 %authors%",
            "&8  \u2022&b Description:&7 %description%",
            "&8  \u2022&b Website:&7 %website%",
            "&8  \u2022&b Support:&7 %support%"))
    val commandPolyconomySetCompleted = TranslationImpl(this,
        arrayOf("command", "polyconomy", "set", "completed"), // todo add to translations.yml
        listOf("%prefix% Set &f%target-name%&7's balance to &f%amount%&7 in currency &f%currency%&7."))
    val commandPolyconomyWithdrawErrorCantAfford = TranslationImpl(this,
        arrayOf("command", "polyconomy", "withdraw", "error", "cant-afford"), // todo add to translations.yml
        listOf("%prefix% &f%target-name%&7 can't afford a withdrawal of &f%amount%&7 in currency &f%currency%&7."))
    val commandPolyconomyWithdrawCompleted = TranslationImpl(this,
        arrayOf("command", "polyconomy", "withdraw", "completed"), // todo add to translations.yml
        listOf("%prefix% Withdrawn &f%amount%&7 from &f%target-name%&7's account in currency &f%currency%&7."))

    fun commandApiFailure(): Exception {
        return CommandAPI.failWithString(commandGenericErrorCommandFailure.rawStr())
    }

}
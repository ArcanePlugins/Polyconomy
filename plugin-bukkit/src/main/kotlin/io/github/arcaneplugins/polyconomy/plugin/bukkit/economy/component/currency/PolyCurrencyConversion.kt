package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency

import java.math.BigDecimal

class PolyCurrencyConversion(
    val from: PolyCurrency,
    val to: PolyCurrency,
    val rate: BigDecimal
)
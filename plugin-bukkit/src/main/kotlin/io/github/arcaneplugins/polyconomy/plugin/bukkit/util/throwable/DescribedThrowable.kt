package io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable

/**
 * Wraps over a [Throwable] but indicates that it has already been explained in best detail possible
 * to the end user, so no further explanation is needed.
 */
class DescribedThrowable(
    throwable: Throwable
) : Throwable(throwable)
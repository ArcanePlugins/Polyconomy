package io.github.arcaneplugins.polyconomy.plugin.bukkit.util

import com.google.common.base.Stopwatch
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * A simple wrapper over Guava's [Stopwatch] for use in basic timers, such as measuring how long
 * it takes for [Polyconomy.onEnable] to complete.
 *
 * Instantiate [PolyStopwatch], and it will immediately begin its timer.
 *
 * When you are ready to end the timer and print its duration, run [stop].
 *
 * The internal Guava [Stopwatch] object can be accessed via the [internalStopwatch] variable.
 */
class PolyStopwatch {

    companion object {
        private val DF: DecimalFormat = DecimalFormat("0.0##")

        private const val NANOSECS_PER_SEC: Float = 1_000_000_000f
    }

    val internalStopwatch: Stopwatch = Stopwatch.createStarted()

    fun stop(): String {
        return """~${DF.format(internalStopwatch.stop().elapsed(TimeUnit.NANOSECONDS) / NANOSECS_PER_SEC)}s"""
    }

}
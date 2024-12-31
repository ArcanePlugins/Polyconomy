package io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.test

import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.TestResult
import java.util.function.Consumer

interface Tester {

    fun checkAndInit(): Boolean

    fun runTests(resultConsumer: Consumer<TestResult>)

}
package io.github.arcaneplugins.ecoworks.plugin.bukkit.testing

import io.github.arcaneplugins.ecoworks.plugin.bukkit.Ecoworks
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.test.VaultLegacyTester
import java.util.function.Consumer

class TestManager(val plugin: Ecoworks) {

    fun testVaultLegacy(
        resultConsumer: Consumer<TestResult>
    ) {
        val tester = VaultLegacyTester(plugin)
        val canTest = tester.checkAndInit()
        if (!canTest) {
            resultConsumer.accept(TestResult(TestResult.Companion.Status.OBSERVATION, "checkAndInit", "Failed init checks"))
        }
        tester.runTests(resultConsumer)
    }

    fun testVaultUnlocked(
        resultConsumer: Consumer<TestResult>
    ) {
        resultConsumer.accept(TestResult(TestResult.Companion.Status.FAILURE, "NOT IMPLEMENTED", "NOT IMPLEMENTED"))
        // TODO
    }

    fun testTreasury(
        resultConsumer: Consumer<TestResult>
    ) {
        resultConsumer.accept(TestResult(TestResult.Companion.Status.FAILURE, "NOT IMPLEMENTED", "NOT IMPLEMENTED"))
        // TODO
    }

    fun testPolyconomy(
        resultConsumer: Consumer<TestResult>
    ) {
        resultConsumer.accept(TestResult(TestResult.Companion.Status.FAILURE, "NOT IMPLEMENTED", "NOT IMPLEMENTED"))
        // TODO
    }

}
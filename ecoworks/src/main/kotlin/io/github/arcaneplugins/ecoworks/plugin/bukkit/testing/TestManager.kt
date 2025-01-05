package io.github.arcaneplugins.ecoworks.plugin.bukkit.testing

import io.github.arcaneplugins.ecoworks.plugin.bukkit.Ecoworks
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.test.VaultLegacyTester
import java.util.function.Consumer

class TestManager(val plugin: Ecoworks) {

    companion object {
        const val DESCRIPTION_FAILED_INIT = "Failed init checks, please check console logs."
    }

    fun testVaultLegacy(
        resultConsumer: Consumer<TestResult>,
    ) {
        val tester = VaultLegacyTester(plugin)
        val canTest = tester.checkAndInit()
        if (!canTest) {
            resultConsumer.accept(
                TestResult(
                    TestResult.Companion.Status.FAILURE,
                    "checkAndInit",
                    DESCRIPTION_FAILED_INIT
                )
            )
            return
        }
        tester.runTests(resultConsumer)
    }

    fun testVaultUnlocked(
        resultConsumer: Consumer<TestResult>,
    ) {
        resultConsumer.accept(TestResult(TestResult.Companion.Status.FAILURE, "NOT IMPLEMENTED", "NOT IMPLEMENTED"))
    }

    fun testTreasury(
        resultConsumer: Consumer<TestResult>,
    ) {
        resultConsumer.accept(TestResult(TestResult.Companion.Status.FAILURE, "NOT IMPLEMENTED", "NOT IMPLEMENTED"))
    }

    fun testPolyconomy(
        resultConsumer: Consumer<TestResult>,
    ) {
        resultConsumer.accept(TestResult(TestResult.Companion.Status.FAILURE, "NOT IMPLEMENTED", "NOT IMPLEMENTED"))
    }

}
package io.oira.fluxeco.core.util

import io.oira.fluxeco.FluxEco
import io.oira.fluxeco.core.manager.ConfigManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

object Threads {

    private val plugin: FluxEco = FluxEco.instance
    private val cfg = ConfigManager(plugin, "config.yml").getConfig()

    lateinit var executor: ExecutorService
        private set
    lateinit var scheduledExecutor: ScheduledExecutorService
        private set
    private var shutdown = false

    fun load() {

        val threads = maxOf(2, cfg.getString("advanced.max-pool-size")?.toIntOrNull() ?: 6)
        executor = Executors.newFixedThreadPool(threads)
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

        plugin.logger.info("FluxEco executor started with $threads threads.")
    }

    fun runAsync(runnable: Runnable) {
        if (shutdown || !::executor.isInitialized) return
        executor.execute(runnable)
    }

    fun <T> getAsync(runnable: () -> T): T? {
        if (shutdown || !::executor.isInitialized) return null
        return CompletableFuture.supplyAsync(runnable, executor).join()
    }

    fun close() {
        try {
            shutdown = true
            if (::executor.isInitialized) executor.shutdown()
            if (::scheduledExecutor.isInitialized) scheduledExecutor.shutdown()
            plugin.logger.info("Shutting down FluxEco async executor and scheduler.")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

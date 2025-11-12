package io.oira.fluxeco.core.manager

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

class ConfigManager(private val plugin: JavaPlugin, private val fileName: String) {

    private val file: File = File(plugin.dataFolder, fileName)
    private var config: FileConfiguration

    init {
        ensureFileExists()
        config = YamlConfiguration.loadConfiguration(file)
        instances.add(this)
    }

    private fun ensureFileExists() {
        if (!file.exists()) {
            plugin.saveResource(fileName, false)
        }
    }

    fun getConfig(): FileConfiguration {
        ensureFileExists()
        return config
    }

    fun saveConfig() {
        try {
            config.save(file)
        } catch (e: IOException) {
            plugin.logger.severe("Could not save config $fileName: ${e.message}")
        }
    }

    fun reloadConfig() {
        ensureFileExists()
        config = YamlConfiguration.loadConfiguration(file)
    }

    companion object {
        private val instances = mutableListOf<ConfigManager>()

        fun reloadAll() {
            instances.forEach { it.reloadConfig() }
        }
    }
}

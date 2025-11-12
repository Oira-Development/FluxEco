package io.oira.fluxeco.core.util

import io.oira.fluxeco.core.manager.ConfigManager
import org.bukkit.plugin.java.JavaPlugin
import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {

    private lateinit var configManager: ConfigManager

    private var dateFormatPattern = "yyyy-MM-dd HH:mm:ss"
    private lateinit var dateFormat: SimpleDateFormat

    fun init(plugin: JavaPlugin, manager: ConfigManager) {
        configManager = manager
        reload()
    }

    fun reload() {
        val config = configManager.getConfig()
        dateFormatPattern = config.getString("format.date-format", "yyyy-MM-dd HH:mm:ss") ?: "yyyy-MM-dd HH:mm:ss"
        dateFormat = SimpleDateFormat(dateFormatPattern)
    }

    fun format(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}

/*
 * FluxEco
 * Copyright (C) 2025 Harfull
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.oira.fluxeco.lamp

import io.oira.fluxeco.FluxEco
import io.oira.fluxeco.command.BalanceCommand
import io.oira.fluxeco.command.BaltopCommand
import io.oira.fluxeco.command.EcoCommand
import io.oira.fluxeco.command.FluxEcoCommand
import io.oira.fluxeco.command.HistoryCommand
import io.oira.fluxeco.command.PayAlertsCommand
import io.oira.fluxeco.command.PayCommand
import io.oira.fluxeco.command.PayToggleCommand
import io.oira.fluxeco.command.StatsCommand
import io.oira.fluxeco.lamp.annotation.ConfigPermissionFactory
import io.oira.fluxeco.manager.ConfigManager
import io.oira.fluxeco.redis.RedisManager
import org.bukkit.Bukkit
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.orphan.OrphanCommand
import revxrsal.commands.orphan.Orphans
import kotlin.reflect.KClass

object CommandManager {

    private val plugin: FluxEco = FluxEco.Companion.instance
    private val config = ConfigManager(plugin, "commands.yml").getConfig()

    private val commandMap: Map<String, KClass<out OrphanCommand>> = mapOf(
        "pay" to PayCommand::class,
        "economy" to EcoCommand::class,
        "balance" to BalanceCommand::class,
        "balance-top" to BaltopCommand::class,
        "transaction-history" to HistoryCommand::class,
        "pay-alerts" to PayAlertsCommand::class,
        "pay-toggle" to PayToggleCommand::class,
        "stats" to StatsCommand::class,
        "fluxeco" to FluxEcoCommand::class
    )

    fun register() {
        try {
            val lamp = BukkitLamp.builder(plugin)
                .permissionFactory(ConfigPermissionFactory(plugin))
                .parameterTypes { types ->
                    types.addParameterType(
                        AsyncOfflinePlayer::class.java,
                        AsyncOfflinePlayer.parameterType()
                    )
                }
                .suggestionProviders { suggestions ->
                    suggestions.addProvider(AsyncOfflinePlayer::class.java) {
                        val names = mutableSetOf<String>()

                        Bukkit.getOnlinePlayers()
                            .filter {
                                !it.hasMetadata("vanished") ||
                                        it.getMetadata("vanished").all { meta -> !meta.asBoolean() }
                            }
                            .forEach { names.add(it.name) }

                        if (RedisManager.isEnabled) {
                            RedisManager.getCache()
                                ?.getAllPlayerNames()
                                ?.let(names::addAll)
                        }

                        names
                    }
                }
                .build()

            commandMap.forEach { (key, clazz) ->
                if (key == "fluxeco") {
                    val command = clazz.constructors.first().call()
                    lamp.register(
                        Orphans
                            .path("fluxeco")
                            .handler(command)
                    )
                    return@forEach
                }

                val section = config.getConfigurationSection("commands.$key") ?: return@forEach
                if (!section.getBoolean("enabled", true)) return@forEach

                val aliases = section.getStringList("aliases")
                if (aliases.isEmpty()) return@forEach

                val command = clazz.constructors.first().call()

                lamp.register(
                    Orphans
                        .path(*aliases.toTypedArray())
                        .handler(command)
                )
            }

        } catch (e: Exception) {
            plugin.logger.severe("Failed to register commands: ${e.message}")
            e.printStackTrace()
        }
    }
}
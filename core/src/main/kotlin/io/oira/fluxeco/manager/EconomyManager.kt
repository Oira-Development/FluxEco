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

package io.oira.fluxeco.manager

import io.oira.fluxeco.api.model.Balance
import io.oira.fluxeco.FluxEco
import io.oira.fluxeco.data.DatabaseManager
import io.oira.fluxeco.data.mongodb.repository.MongoBalanceRepository
import io.oira.fluxeco.data.table.Balances
import io.oira.fluxeco.util.Threads
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.CompletableFuture

object EconomyManager {

    private val plugin: FluxEco = FluxEco.instance
    private val configManager = ConfigManager(plugin, "config.yml")

    private val currencyName = configManager.getConfig().getString("currency.name", "Dollar") ?: "Dollar"
    private val currencyNamePlural = configManager.getConfig().getString("currency.name-plural", "Dollars") ?: "Dollars"
    private val startingBalance = configManager.getConfig().getDouble("general.starting-balance", 0.0)

    fun getBalance(uuid: UUID): Double {
        return CacheManager.getBalance(uuid)
    }

    fun getBalanceAsync(uuid: UUID): CompletableFuture<Double> =
        CompletableFuture.supplyAsync({ getBalance(uuid) }, Threads.executor)

    fun hasBalance(uuid: UUID, amount: Double): Boolean {
        return getBalance(uuid) >= amount
    }

    fun hasBalanceAsync(uuid: UUID, amount: Double): CompletableFuture<Boolean> =
        CompletableFuture.supplyAsync({ hasBalance(uuid, amount) }, Threads.executor)

    fun getAllBalances(): List<Balance> {
        return if (DatabaseManager.isMongoDB()) {
            MongoBalanceRepository.getAllBalances()
        } else {
            transaction(DatabaseManager.getDatabase()) {
                Balances.selectAll().map {
                    Balance(
                        uuid = UUID.fromString(it[Balances.uuid]),
                        balance = it[Balances.balance]
                    )
                }
            }
        }
    }

    fun getAllBalancesAsync(): CompletableFuture<List<Balance>> =
        CompletableFuture.supplyAsync({ getAllBalances() }, Threads.executor)

    fun setBalance(uuid: UUID, amount: Double): Int {
        CacheManager.setBalance(uuid, amount)
        Threads.runAsync {
            CacheManager.refreshBaltop()
        }
        return 1
    }

    fun setBalanceAsync(uuid: UUID, amount: Double): CompletableFuture<Int> =
        CompletableFuture.supplyAsync({ setBalance(uuid, amount) }, Threads.executor)

    fun addBalance(uuid: UUID, amount: Double): Int {
        val newBalance = getBalance(uuid) + amount
        return setBalance(uuid, newBalance)
    }

    fun addBalanceAsync(uuid: UUID, amount: Double): CompletableFuture<Int> =
        CompletableFuture.supplyAsync({ addBalance(uuid, amount) }, Threads.executor)

    fun subtractBalance(uuid: UUID, amount: Double): Boolean {
        val current = getBalance(uuid)
        if (current < amount) return false
        setBalance(uuid, current - amount)
        return true
    }

    fun subtractBalanceAsync(uuid: UUID, amount: Double): CompletableFuture<Boolean> =
        CompletableFuture.supplyAsync({ subtractBalance(uuid, amount) }, Threads.executor)

    fun transfer(from: UUID, to: UUID, amount: Double): Boolean {
        if (!subtractBalance(from, amount)) return false
        addBalance(to, amount)
        return true
    }

    fun transferAsync(from: UUID, to: UUID, amount: Double): CompletableFuture<Boolean> =
        CompletableFuture.supplyAsync({ transfer(from, to, amount) }, Threads.executor)

    fun createBalance(uuid: UUID) {
        setBalance(uuid, startingBalance)
    }

    fun createBalanceAsync(uuid: UUID): CompletableFuture<Void> =
        CompletableFuture.runAsync({ createBalance(uuid) }, Threads.executor)

    fun deleteBalance(uuid: UUID) {
        CacheManager.removeBalance(uuid, 0.0)
        CacheManager.clearBalance(uuid)

        Threads.runAsync {
            if (DatabaseManager.isMongoDB()) {
                MongoBalanceRepository.deleteBalance(uuid)
            } else {
                transaction(DatabaseManager.getDatabase()) {
                    Balances.deleteWhere { Balances.uuid eq uuid.toString() }
                }
            }
        }
    }

    fun deleteBalanceAsync(uuid: UUID): CompletableFuture<Void> =
        CompletableFuture.runAsync({ deleteBalance(uuid) }, Threads.executor)

    fun deleteAllBalances() {
        CacheManager.clearAllCaches()

        Threads.runAsync {
            if (DatabaseManager.isMongoDB()) {
                MongoBalanceRepository.deleteAllBalances()
            } else {
                transaction(DatabaseManager.getDatabase()) {
                    Balances.deleteAll()
                }
            }
        }
    }

    fun deleteAllBalancesAsync(): CompletableFuture<Void> =
        CompletableFuture.runAsync({ deleteAllBalances() }, Threads.executor)

    fun getCurrencyName(amount: Double): String =
        if (amount == 1.0) currencyName else currencyNamePlural
}
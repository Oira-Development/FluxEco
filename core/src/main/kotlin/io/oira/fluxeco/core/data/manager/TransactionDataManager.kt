package io.oira.fluxeco.core.data.manager

import io.oira.fluxeco.core.data.DatabaseManager
import io.oira.fluxeco.core.data.model.Transaction
import io.oira.fluxeco.core.data.model.TransactionType
import io.oira.fluxeco.core.data.table.Transactions
import io.oira.fluxeco.core.util.Threads
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import java.util.concurrent.CompletableFuture

object TransactionDataManager {

    fun getTransactions(uuid: UUID): List<Transaction> = transaction(DatabaseManager.getDatabase()) {
        Transactions.selectAll().where { Transactions.playerUuid eq uuid.toString() }
            .orderBy(Transactions.date, SortOrder.DESC)
            .map {
                Transaction(
                    id = it[Transactions.id],
                    playerUuid = UUID.fromString(it[Transactions.playerUuid]),
                    type = TransactionType.valueOf(it[Transactions.type]),
                    amount = it[Transactions.amount],
                    senderUuid = UUID.fromString(it[Transactions.senderUuid]),
                    receiverUuid = UUID.fromString(it[Transactions.receiverUuid]),
                    date = it[Transactions.date]
                )
            }
    }

    fun getTransactionsAsync(uuid: UUID): CompletableFuture<List<Transaction>> {
        val future = CompletableFuture<List<Transaction>>()
        Threads.runAsync {
            try {
                val result = getTransactions(uuid)
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    fun createTransaction(playerUuid: UUID, type: TransactionType, amount: Double, senderUuid: UUID, receiverUuid: UUID): Int = transaction(DatabaseManager.getDatabase()) {
        Transactions.insert {
            it[Transactions.playerUuid] = playerUuid.toString()
            it[Transactions.type] = type.name
            it[Transactions.amount] = amount
            it[Transactions.senderUuid] = senderUuid.toString()
            it[Transactions.receiverUuid] = receiverUuid.toString()
            it[Transactions.date] = System.currentTimeMillis()
        } get Transactions.id
    }

    fun createTransactionAsync(playerUuid: UUID, type: TransactionType, amount: Double, senderUuid: UUID, receiverUuid: UUID): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        Threads.runAsync {
            try {
                val result = createTransaction(playerUuid, type, amount, senderUuid, receiverUuid)
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    fun deleteTransactions(uuid: UUID): Int = transaction(DatabaseManager.getDatabase()) {
        Transactions.deleteWhere { Transactions.playerUuid eq uuid.toString() }
    }

    fun deleteAllTransactions(): Int = transaction(DatabaseManager.getDatabase()) {
        Transactions.deleteAll()
    }
}

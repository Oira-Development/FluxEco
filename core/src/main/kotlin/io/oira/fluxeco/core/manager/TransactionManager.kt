package io.oira.fluxeco.core.manager

import io.oira.fluxeco.FluxEco
import io.oira.fluxeco.core.data.manager.TransactionDataManager
import io.oira.fluxeco.core.data.model.Transaction
import io.oira.fluxeco.core.data.model.TransactionType
import io.oira.fluxeco.core.util.Threads
import java.util.UUID

object TransactionManager {

    private val plugin: FluxEco = FluxEco.instance

    fun getTransactionHistory(uuid: UUID): List<Transaction> {
        return TransactionDataManager.getTransactions(uuid)
    }

    fun recordTransfer(from: UUID, to: UUID, amount: Double) {
        Threads.runAsync {
            TransactionDataManager.createTransaction(from, TransactionType.SENT, amount, from, to)
            TransactionDataManager.createTransaction(to, TransactionType.RECEIVED, amount, from, to)
        }
    }

    fun recordAdminDeduct(player: UUID, amount: Double, adminUuid: UUID = UUID(0, 0)) {
        Threads.runAsync {
            TransactionDataManager.createTransaction(player, TransactionType.ADMIN_DEDUCTED, amount, adminUuid, player)
        }
    }

    fun recordAdminReceive(player: UUID, amount: Double, adminUuid: UUID = UUID(0, 0)) {
        Threads.runAsync {
            TransactionDataManager.createTransaction(player, TransactionType.ADMIN_RECEIVED, amount, adminUuid, player)
        }
    }
}

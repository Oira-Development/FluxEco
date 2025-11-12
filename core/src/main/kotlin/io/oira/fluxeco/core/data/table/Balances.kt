package io.oira.fluxeco.core.data.table

import org.jetbrains.exposed.sql.Table

object Balances : Table("balances") {
    val uuid = varchar("uuid", 36).uniqueIndex()
    val balance = double("balance").default(0.0)

    override val primaryKey = PrimaryKey(uuid)
}

package io.oira.fluxeco.core.data.table

import org.jetbrains.exposed.sql.Table

object PlayerSettings : Table("player_settings") {
    val uuid = varchar("uuid", 36).uniqueIndex()
    val togglePayments = bool("toggle_payments").default(true)
    val payAlerts = bool("pay_alerts").default(true)

    override val primaryKey = PrimaryKey(uuid)
}

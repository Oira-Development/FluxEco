package io.oira.fluxeco.core.data.table

import org.jetbrains.exposed.sql.Table

object PlayerProfiles : Table("player_profiles") {
    val uuid = varchar("uuid", 36).uniqueIndex()
    val name = varchar("name", 16)
    val skinUrl = text("skin_url").nullable()
    val capeUrl = text("cape_url").nullable()
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(uuid)
}

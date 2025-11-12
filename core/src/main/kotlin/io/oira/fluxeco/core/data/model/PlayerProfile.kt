package io.oira.fluxeco.core.data.model

import java.util.UUID

data class PlayerProfile(
    val uuid: UUID,
    val name: String,
    val skinUrl: String?,
    val capeUrl: String?,
    val updatedAt: Long
)

package io.github.darefox.bot.models

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class TempRoomSettingsEntry(
    val creatorRoomId: String?,
    val categoryId: String?
)
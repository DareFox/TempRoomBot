package change.group.bot.models

import dev.kord.common.entity.Overwrite
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class TempRoomEntry(
    val name: String,
    val authorId: String,
    val overwrites: Set<Overwrite>,
    val previousRoomId: String?
)
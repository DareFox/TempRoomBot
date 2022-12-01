package change.group.bot.models

@kotlinx.serialization.Serializable
data class PongCounterEntry(
    val authorSnowflake: String,
    val counter: Int,
)
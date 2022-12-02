package io.github.darefox.bot.util

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.common.entity.Snowflake

/**
 * Convert string snowflake a.k.a ID to [Snowflake] object
 *
 * @sample snowUsage
 */
fun String.toSnow(): Snowflake {
    return Snowflake(this.toULong())
}

private fun snowUsage() {
    val stringID = "388694518918021121"
    val snowflake = stringID.toSnow() // Snowflake(388694518918021121)

    /* ... some extension.kt */
    class SnowFlakeUsageExtension : Extension() {
        override val name: String = "SnowFlakeUsage"

        override suspend fun setup() {
            publicSlashCommand {
                guild(snowflake)
            }
        }
    }
}